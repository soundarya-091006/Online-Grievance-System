package com.safereport.dto.response;

import com.safereport.entity.Evidence;
import lombok.*;

import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class EvidenceResponse {

    private Long id;
    private String fileName;
    private String fileType;
    private Long fileSize;
    private String description;
    private LocalDateTime uploadedAt;

    // Flattened uploader info — no lazy proxy involved
    private Long uploadedById;
    private String uploadedByName;

    /**
     * Maps an Evidence entity to this DTO while the Hibernate session is still open.
     * All lazy associations are accessed HERE, inside the transaction.
     */
    public static EvidenceResponse from(Evidence ev) {
        EvidenceResponseBuilder b = EvidenceResponse.builder()
                .id(ev.getId())
                .fileName(ev.getFileName())
                .fileType(ev.getFileType())
                .fileSize(ev.getFileSize())
                .description(ev.getDescription())
                .uploadedAt(ev.getUploadedAt());

        // Safely resolve the lazy User proxy while session is open
        if (ev.getUploadedBy() != null) {
            try {
                b.uploadedById(ev.getUploadedBy().getId());
                b.uploadedByName(ev.getUploadedBy().getFullName());
            } catch (Exception ignored) {
                // proxy unresolvable — leave names null
            }
        }

        return b.build();
    }
}

