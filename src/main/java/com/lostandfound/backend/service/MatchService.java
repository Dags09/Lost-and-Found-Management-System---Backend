package com.lostandfound.backend.service;

import com.lostandfound.backend.dto.ItemPublicResponse;
import com.lostandfound.backend.dto.MatchResponse;
import com.lostandfound.backend.exception.ApiException;
import com.lostandfound.backend.models.Items;
import com.lostandfound.backend.models.Match;
import com.lostandfound.backend.models.enumRoleStatusTypes.ItemStatus;
import com.lostandfound.backend.models.enumRoleStatusTypes.ItemType;
import com.lostandfound.backend.models.enumRoleStatusTypes.MatchStatus;
import com.lostandfound.backend.repository.ItemRepository;
import com.lostandfound.backend.repository.MatchRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Suggests which found item may be a lost one. It only SUGGESTS: the owner still submits a claim
 * with proof and staff still approve it, so a wrong suggestion cannot hand anyone an item.
 */
@Service
public class MatchService {

    /** Score needed (out of 100) for a pair to be suggested. See MatchScorer. */
    static final int THRESHOLD = 55;
    /** A new lost report is shown at most this many suggestions. */
    static final int MAX_PER_LOST = 5;
    /** How many open items of the same category one new report is compared against. */
    static final int CANDIDATES = 200;

    private final ItemRepository items;
    private final MatchRepository matches;
    private final NotificationService notifications;

    public MatchService(ItemRepository items, MatchRepository matches, NotificationService notifications) {
        this.items = items;
        this.matches = matches;
        this.notifications = notifications;
    }

    /** A found item was reported (or edited): compare it with every open lost report in its category. */
    @Transactional
    public int matchNewFound(Items found) {
        if (found.getType() != ItemType.FOUND || found.getStatus() != ItemStatus.OPEN
                || found.getCategoryId() == null) return 0;

        int created = 0;
        for (Items lost : items.findOpenByTypeAndCategory(
                ItemType.LOST, found.getCategoryId(), PageRequest.of(0, CANDIDATES))) {
            if (Objects.equals(lost.getReporterId(), found.getReporterId())) continue;   // your own report
            int score = MatchScorer.score(facts(lost), facts(found));
            if (score < THRESHOLD) continue;
            if (matches.existsByLostItemIdAndFoundItemId(lost.getId(), found.getId())) continue;

            save(lost, found, score);
            notifications.matchFound(lost.getReporterId(),
                    "A found item may be yours: \"" + found.getName() + "\". Open your lost report to review it.",
                    lost.getId(), found.getId());
            created++;
        }
        return created;
    }

    /** A lost report was made (or edited): compare it with every open found item in its category. */
    @Transactional
    public int matchNewLost(Items lost) {
        if (lost.getType() != ItemType.LOST || lost.getStatus() != ItemStatus.OPEN
                || lost.getCategoryId() == null) return 0;

        record Scored(Items found, int score) {}
        List<Scored> good = new ArrayList<>();
        for (Items found : items.findOpenByTypeAndCategory(
                ItemType.FOUND, lost.getCategoryId(), PageRequest.of(0, CANDIDATES))) {
            if (Objects.equals(lost.getReporterId(), found.getReporterId())) continue;
            int score = MatchScorer.score(facts(lost), facts(found));
            if (score >= THRESHOLD
                    && !matches.existsByLostItemIdAndFoundItemId(lost.getId(), found.getId()))
                good.add(new Scored(found, score));
        }
        good.sort(Comparator.comparingInt(Scored::score).reversed());

        List<Scored> top = good.size() > MAX_PER_LOST ? good.subList(0, MAX_PER_LOST) : good;
        for (Scored s : top) save(lost, s.found(), s.score());

        if (!top.isEmpty())
            notifications.matchFound(lost.getReporterId(),
                    top.size() == 1
                            ? "We found 1 item that may be yours. Open your lost report to review it."
                            : "We found " + top.size() + " items that may be yours. Open your lost report to review them.",
                    lost.getId(), null);
        return top.size();
    }

    /** The owner's open suggestions, best first. Items that are gone (claimed, expired) are left out. */
    @Transactional(readOnly = true)
    public List<MatchResponse> listForLost(String lostItemId, String userId) {
        requireOwnLostReport(lostItemId, userId);
        List<Match> suggested = matches.findByLostItemIdAndStatusOrderByScoreDesc(lostItemId, MatchStatus.SUGGESTED);
        if (suggested.isEmpty()) return List.of();

        Map<String, Items> foundById = items.findAllById(
                        suggested.stream().map(Match::getFoundItemId).toList())
                .stream().collect(Collectors.toMap(Items::getId, Function.identity()));

        return suggested.stream()
                .filter(m -> {
                    Items f = foundById.get(m.getFoundItemId());
                    return f != null && f.getStatus() == ItemStatus.OPEN;
                })
                .map(m -> MatchResponse.from(m, ItemPublicResponse.from(foundById.get(m.getFoundItemId()))))
                .toList();
    }

    /** "That's not mine": hides the suggestion for good. */
    @Transactional
    public void dismiss(String lostItemId, String matchId, String userId) {
        requireOwnLostReport(lostItemId, userId);
        Match m = matches.findByIdAndLostItemId(matchId, lostItemId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Suggestion not found"));
        m.setStatus(MatchStatus.DISMISSED);
        matches.save(m);
    }

    /**
     * Called inside the approval transaction. If the claimant has an open lost report that was
     * matched to this found item, that report is solved: CLOSED, and the match CONFIRMED.
     * Every other suggestion pointing at the item is dismissed, because the item is gone now.
     */
    @Transactional
    public void settleAfterApproval(String foundItemId, String claimantId) {
        List<Match> open = matches.findByFoundItemIdAndStatus(foundItemId, MatchStatus.SUGGESTED);
        if (open.isEmpty()) return;

        Map<String, Items> lostById = items.findAllById(
                        open.stream().map(Match::getLostItemId).toList())
                .stream().collect(Collectors.toMap(Items::getId, Function.identity()));

        for (Match m : open) {
            Items lost = lostById.get(m.getLostItemId());
            if (lost != null && claimantId.equals(lost.getReporterId()) && lost.getStatus() == ItemStatus.OPEN) {
                lost.setStatus(ItemStatus.CLOSED);
                items.save(lost);
                m.setStatus(MatchStatus.CONFIRMED);
            } else {
                m.setStatus(MatchStatus.DISMISSED);
            }
        }
        matches.saveAll(open);
    }

    // ---- helpers -------------------------------------------------------

    private void save(Items lost, Items found, int score) {
        Match m = new Match();
        m.setLostItemId(lost.getId());
        m.setFoundItemId(found.getId());
        m.setScore(score / 100.0);
        matches.save(m);
    }

    /** 404 missing (or not a lost report), 403 not yours. */
    private void requireOwnLostReport(String lostItemId, String userId) {
        Items lost = items.findById(lostItemId)
                .filter(i -> i.getType() == ItemType.LOST)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Lost report not found"));
        if (!userId.equals(lost.getReporterId()))
            throw new ApiException(HttpStatus.FORBIDDEN, "You can only see matches for your own reports");
    }

    private static MatchScorer.Facts facts(Items i) {
        return new MatchScorer.Facts(i.getName(), i.getDescription(), i.getColor(), i.getBrand(), i.getDateOccurred());
    }
}