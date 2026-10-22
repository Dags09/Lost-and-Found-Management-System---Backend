package com.lostandfound.backend.service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.Set;

/**
 * Scores how likely a lost report and a found item are the same object. Pure logic with no
 * database or Spring, so it is easy to test. Points out of 100:
 *   same category 15, colour 20 (12 if one contains the other), brand 20, name + description
 *   words 30, dates 15 (within 14 days; 8 within 60 days).
 * An item found BEFORE it was lost cannot be the same object, so that pair gets -1.
 */
public final class MatchScorer {

    private MatchScorer() {}

    public static final int CATEGORY_POINTS = 15;


    /** Just the facts the scorer needs, so it does not depend on the database entities. */
    public record Facts(String name, String description, String color, String brand, LocalDate date) {}

    private static final Set<String> NO_BRAND = Set.of("unknown", "none", "n/a", "na", "no brand", "nobrand", "-", "?");
    private static final Set<String> STOP = Set.of(
            "the", "and", "with", "has", "have", "was", "for", "from", "this", "that", "its", "one", "are", "very");

    /** @return 0-100, or -1 when the pair is impossible. */
    public static int score(Facts lost, Facts found) {
        if (lost.date() != null && found.date() != null && found.date().isBefore(lost.date().minusDays(1)))
            return -1;

        int points = CATEGORY_POINTS;
        points += colorPoints(lost.color(), found.color());
        points += brandPoints(lost.brand(), found.brand());
        points += textPoints(lost, found);
        points += datePoints(lost.date(), found.date());
        return Math.min(points, 100);
    }

    /** 20 for the same colour, 12 if one contains the other ("dark blue" / "blue"). */
    static int colorPoints(String a, String b) {
        String x = norm(a), y = norm(b);
        if (x.isEmpty() || y.isEmpty()) return 0;
        if (x.equals(y)) return 20;
        return (x.contains(y) || y.contains(x)) ? 12 : 0;
    }

    /** 20 for the same brand. "unknown", "none", "n/a" and similar earn nothing. */
    static int brandPoints(String a, String b) {
        String x = norm(a), y = norm(b);
        if (x.isEmpty() || y.isEmpty() || NO_BRAND.contains(x) || NO_BRAND.contains(y)) return 0;
        return x.equals(y) ? 20 : 0;
    }

    /**
     * Up to 30: the share of the shorter text's words that also appear in the other.
     * Words under 3 letters and common filler words are ignored.
     */
    static int textPoints(Facts lost, Facts found) {
        Set<String> a = words(norm(lost.name()) + " " + norm(lost.description()));
        Set<String> b = words(norm(found.name()) + " " + norm(found.description()));
        if (a.isEmpty() || b.isEmpty()) return 0;
        Set<String> shared = new HashSet<>(a);
        shared.retainAll(b);
        double overlap = (double) shared.size() / Math.min(a.size(), b.size());
        return (int) Math.round(30 * overlap);
    }

    /** 15 if the dates are within 14 days of each other, 8 within 60 days, otherwise 0. */
    static int datePoints(LocalDate lost, LocalDate found) {
        if (lost == null || found == null) return 0;
        long days = Math.abs(ChronoUnit.DAYS.between(lost, found));
        if (days <= 14) return 15;
        if (days <= 60) return 8;
        return 0;
    }

    /** Null-safe trim + lowercase. */
    private static String norm(String s) {
        return s == null ? "" : s.trim().toLowerCase();
    }

    /** Distinct meaningful words of a text. */
    private static Set<String> words(String text) {
        Set<String> out = new HashSet<>();
        for (String w : norm(text).split("[^\\p{L}\\p{N}]+"))
            if (w.length() >= 3 && !STOP.contains(w)) out.add(w);
        return out;
    }
}