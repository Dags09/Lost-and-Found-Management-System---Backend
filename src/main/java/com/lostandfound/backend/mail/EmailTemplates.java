package com.lostandfound.backend.mail;

import org.springframework.web.util.HtmlUtils;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

/**
 * Builds the HTML for claim emails. Every dynamic value is HTML-escaped, because item names,
 * notes and people's names are typed by users.
 */
final class EmailTemplates {

    private static final String BASE_STYLES = """
        body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
               line-height: 1.6; color: #1f2937; background-color: #f8fafc; margin: 0; padding: 20px; }
        .container { max-width: 650px; margin: 0 auto; background-color: #ffffff; border-radius: 12px;
                     box-shadow: 0 4px 6px -1px rgba(0,0,0,.1), 0 2px 4px -1px rgba(0,0,0,.06); overflow: hidden; }
        .header { color: white; padding: 40px 32px; text-align: center; }
        .header h1 { margin: 0; font-size: 28px; font-weight: 700; letter-spacing: -0.025em; }
        .content { padding: 40px 32px; }
        .body-text { font-size: 18px; color: #475569; line-height: 1.7; margin: 0 0 24px 0; }
        .card { border-radius: 10px; padding: 28px; margin: 8px 0 32px 0; background:#f1f5f9; border:2px solid #e2e8f0; }
        .detail-table { width: 100%; border-collapse: collapse; }
        .detail-table td { padding: 0 0 16px 0; vertical-align: top; border-bottom: 2px solid #e2e8f0; }
        .detail-table tr + tr td { padding-top: 16px; }
        .detail-table tr:last-child td { border-bottom: none; padding-bottom: 0; }
        .detail-label { font-weight: 600; color: #1e293b; font-size: 16px; width: 40%; }
        .detail-value { color: #475569; font-size: 16px; font-weight: 500; text-align: right; word-break: break-word; }
        .note { background:#ffffff; border-left: 4px solid #cbd5e1; border-radius: 6px; padding: 14px 16px;
                margin: 8px 0 32px 0; color:#475569; font-size:16px; white-space: pre-wrap; word-break: break-word; }
        .section-title { font-size: 18px; font-weight: 700; color: #1e293b; margin: 32px 0 8px 0; }
        .banner { border-radius: 12px; padding: 32px; margin: 32px 0; text-align: center; }
        .banner-icon { font-size: 48px; margin-bottom: 16px; }
        .banner-text { font-size: 22px; font-weight: 700; margin: 0; }
        .footer { background-color: #f8fafc; padding: 32px; text-align: center; border-top: 2px solid #e2e8f0; }
        .footer-text { color: #64748b; font-size: 14px; margin: 0; line-height: 1.6; }
        @media only screen and (max-width: 600px) {
            body { padding: 8px !important; }
            .container { border-radius: 8px !important; }
            .header { padding: 24px 16px !important; }
            .header h1 { font-size: 20px !important; }
            .content { padding: 24px 16px !important; }
            .card { padding: 16px !important; }
            .banner { padding: 20px 12px !important; }
            .body-text { font-size: 15px !important; }
            .detail-table td { display: block !important; width: 100% !important; text-align: left !important; }
            .detail-table td.detail-label { font-size: 13px !important; padding-bottom: 2px !important; border-bottom: none !important; }
            .detail-table td.detail-value { font-size: 14px !important; }
            .footer { padding: 16px !important; }
            .footer-text { font-size: 12px !important; }
        }
        """;

    private static final String HEADER_GRAD = "linear-gradient(135deg,#1d4ed8 0%,#1e3a8a 100%)";

    private final String platformName;
    private final ZoneId zone;

    EmailTemplates(String platformName, ZoneId zone) {
        this.platformName = platformName;
        this.zone = zone;
    }

    String subject(ClaimEmailEvent e) {
        return switch (e.kind()) {
            case SUBMITTED -> "We received your claim - " + platformName;
            case APPROVED -> "Your claim was approved - " + platformName;
            case REJECTED -> "Update on your claim - " + platformName;
        };
    }

    String html(ClaimEmailEvent e) {
        return switch (e.kind()) {
            case SUBMITTED -> submitted(e);
            case APPROVED -> approved(e);
            case REJECTED -> rejected(e);
        };
    }

    // ---- the three emails ----------------------------------------------

    private String submitted(ClaimEmailEvent e) {
        return page("Claim received", "linear-gradient(135deg,#dbeafe 0%,#bfdbfe 100%)", "#3b82f6", "#2563eb",
                "📨", "Claim received!", """
            <p class="body-text">Hello <strong>%s</strong>,</p>
            <p class="body-text">Thank you. We received your claim and our staff will review it soon.
               We will email you as soon as there is a decision.</p>
            <p class="section-title">🧾 Claim Details</p>
            %s
            <p style="margin:32px 0 0 0; color:#64748b; font-size:16px; line-height:1.6;">
                Please keep your proof ready in case staff need to ask you more questions.</p>
            """.formatted(esc(e.claimantName()),
                        card(rows(
                                row("Item:", e.itemName(), ""),
                                row("Claim ID:", "#" + shortId(e.claimId()), ""),
                                row("Submitted:", date(e), ""),
                                row("Status:", "Pending review", "color:#d97706; font-weight:600;")))));
    }

    private String approved(ClaimEmailEvent e) {
        StringBuilder place = new StringBuilder();
        if (e.placeName() != null) {
            List<String> r = new java.util.ArrayList<>();
            r.add(row("Place:", e.placeName(), "color:#1d4ed8; font-weight:700;"));
            if (e.placePerson() != null) r.add(row("Ask for:", e.placePerson(), ""));
            if (e.placeContact() != null) r.add(row("Contact:", e.placeContact(), ""));
            place.append("<p class=\"section-title\">📍 Where to Collect</p>")
                    .append(card(rows(r.toArray(String[]::new))));
        }
        String note = e.note() == null ? "" :
                "<p class=\"section-title\">💬 Note from Staff</p><div class=\"note\">" + esc(e.note()) + "</div>";

        return page("Claim approved", "linear-gradient(135deg,#ecfdf5 0%,#d1fae5 100%)", "#a7f3d0", "#059669",
                "✅", "Your claim was approved!", """
            <p class="body-text">Hello <strong>%s</strong>,</p>
            <p class="body-text">Good news! Staff confirmed that <strong>%s</strong> is yours.
               Please collect it using the details below.</p>
            <p class="section-title">🧾 Claim Details</p>
            %s
            %s
            %s
            <p style="margin:32px 0 0 0; color:#64748b; font-size:16px; line-height:1.6;">
                Bring a valid student or staff ID when you collect your item.</p>
            """.formatted(esc(e.claimantName()), esc(e.itemName()),
                        card(rows(
                                row("Item:", e.itemName(), ""),
                                row("Claim ID:", "#" + shortId(e.claimId()), ""),
                                row("Status:", "Approved", "color:#059669; font-weight:600;")))
                        , place, note));
    }

    private String rejected(ClaimEmailEvent e) {
        String reason = e.note() == null ? "" :
                "<p class=\"section-title\">💬 Reason</p><div class=\"note\">" + esc(e.note()) + "</div>";
        return page("Claim update", "linear-gradient(135deg,#fef2f2 0%,#fee2e2 100%)", "#fca5a5", "#dc2626",
                "❌", "Claim not approved", """
            <p class="body-text">Hello <strong>%s</strong>,</p>
            <p class="body-text">Unfortunately, we could not approve your claim for <strong>%s</strong>.</p>
            <p class="section-title">🧾 Claim Details</p>
            %s
            %s
            <p style="margin:32px 0 0 0; color:#64748b; font-size:16px; line-height:1.6;">
                If the item is yours, you can submit a new claim with clearer proof.</p>
            """.formatted(esc(e.claimantName()), esc(e.itemName()),
                        card(rows(
                                row("Item:", e.itemName(), ""),
                                row("Claim ID:", "#" + shortId(e.claimId()), ""),
                                row("Status:", "Not approved", "color:#dc2626; font-weight:600;")))
                        , reason));
    }

    // ---- building blocks -------------------------------------------------

    private String page(String title, String bannerGrad, String bannerBorder, String bannerColor,
                        String icon, String bannerText, String body) {
        return """
            <!DOCTYPE html>
            <html lang="en">
            <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <title>%s</title>
            <style>
            %s
            .header { background: %s; }
            </style>
            </head>
            <body>
            <div class="container">
              <div class="header"><h1>%s</h1></div>
              <div class="content">
                <div class="banner" style="background:%s; border:2px solid %s;">
                  <div class="banner-icon">%s</div>
                  <p class="banner-text" style="color:%s;">%s</p>
                </div>
                %s
              </div>
              <div class="footer">
                <p class="footer-text">This is an automated message from %s.<br>Please do not reply to this email.</p>
              </div>
            </div>
            </body>
            </html>
            """.formatted(esc(title), BASE_STYLES, HEADER_GRAD, esc(platformName),
                bannerGrad, bannerBorder, icon, bannerColor, esc(bannerText), body, esc(platformName));
    }

    private static String rows(String... rows) { return String.join("", rows); }

    private static String card(String rows) {
        return "<div class=\"card\"><table class=\"detail-table\" role=\"presentation\">" + rows + "</table></div>";
    }

    /** valueStyle is developer-supplied CSS, never user input. */
    private static String row(String label, String value, String valueStyle) {
        return "<tr><td class=\"detail-label\">" + esc(label) + "</td>"
                + "<td class=\"detail-value\" style=\"" + valueStyle + "\">" + esc(value) + "</td></tr>";
    }

    private String date(ClaimEmailEvent e) {
        if (e.at() == null) return "-";
        return DateTimeFormatter.ofPattern("d MMMM yyyy, h:mm a", Locale.ENGLISH).withZone(zone).format(e.at());
    }

    private static String shortId(String id) {
        return id == null ? "-" : id.substring(0, Math.min(8, id.length())).toUpperCase(Locale.ROOT);
    }

    static String esc(String s) { return s == null ? "" : HtmlUtils.htmlEscape(s); }
}