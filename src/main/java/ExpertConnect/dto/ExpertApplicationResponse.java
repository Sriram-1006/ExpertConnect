package ExpertConnect.dto;

import ExpertConnect.entity.Category;
import ExpertConnect.entity.VerificationStatus;

import java.time.LocalDateTime;

public record ExpertApplicationResponse(
        Long id,
        Long userId,
        String userName,
        Category expertise,
        String bio,
        Integer experience,
        VerificationStatus verificationStatus,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
