package com.lostandfound.backend.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;

/**
 * One uploaded picture, stored as bytes inside PostgreSQL (bytea).
 * Kept in its own table so listing items never drags image bytes along.
 */
@Entity
@Table(name = "images")
public class ImageFile {

    public enum Purpose { ITEM, PROOF }

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(length = 36)
    private String id;

    @Column(name = "content_type", nullable = false, length = 50)
    private String contentType;

    @Column(name = "size_bytes", nullable = false)
    private long sizeBytes;

    // ITEM = public item photo, PROOF = claim proof (private, only claimant + staff)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Purpose purpose;

    // bytea. Do NOT use @Lob here: on PostgreSQL that maps to a "large object" (oid), not bytea.
    @JdbcTypeCode(SqlTypes.VARBINARY)
    @Column(nullable = false, columnDefinition = "bytea")
    private byte[] data;

    @Column(name = "created_at", insertable = false, updatable = false)
    private Instant createdAt;

    public ImageFile() {}

    public ImageFile(String contentType, Purpose purpose, byte[] data) {
        this.contentType = contentType;
        this.purpose = purpose;
        this.data = data;
        this.sizeBytes = data.length;
    }

    public String getId() { return id; }
    public String getContentType() { return contentType; }
    public long getSizeBytes() { return sizeBytes; }
    public Purpose getPurpose() { return purpose; }
    public byte[] getData() { return data; }
    public Instant getCreatedAt() { return createdAt; }
}