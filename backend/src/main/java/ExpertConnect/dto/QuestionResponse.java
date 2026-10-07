package ExpertConnect.dto;

import ExpertConnect.entity.Category;

import java.time.LocalDateTime;

public record QuestionResponse(
        Long id,
        Long askerId,
        String askerName,
        String title,
        String description,
        Category category,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
