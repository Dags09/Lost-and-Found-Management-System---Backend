package com.lostandfound.backend.mail;

import java.time.Instant;

/**
 * Describes one claim email. Published inside a transaction; the email is only sent
 * after that transaction commits (see {@link EmailService}).
 *
 * @param kind        which email to send
 * @param to          claimant's email address
 * @param claimantName claimant's display name
 * @param itemName    name of the claimed item
 * @param claimId     claim reference shown in the email
 * @param at          when the claim was submitted / reviewed
 * @param note        staff note (rejection reason or approval note), may be null
 * @param placeName   where to collect the item (approved only), may be null
 * @param placeContact contact number of that place, may be null
 * @param placePerson  person to ask for at that place, may be null
 */
public record ClaimEmailEvent(
        Kind kind,
        String to,
        String claimantName,
        String itemName,
        String claimId,
        Instant at,
        String note,
        String placeName,
        String placeContact,
        String placePerson) {

    public enum Kind { SUBMITTED, APPROVED, REJECTED }
}