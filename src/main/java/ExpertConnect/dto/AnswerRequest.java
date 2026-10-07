package ExpertConnect.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AnswerRequest(
        @NotBlank(message = "content is required")
        @Size(min = 10, max = 5000, message = "content must be between 10 and 5000 characters")
        String content
) {
}
