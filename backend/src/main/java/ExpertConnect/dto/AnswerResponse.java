package ExpertConnect.dto;

import java.time.LocalDateTime;

public record AnswerResponse(
        Long id,
        Long questionId,
        Long expertId,
        String expertName,
        String content,
        boolean accepted,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
