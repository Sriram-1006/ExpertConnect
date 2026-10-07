package ExpertConnect.dto;

import ExpertConnect.entity.Category;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateQuestionRequest(
        @NotBlank(message = "title is required")
        @Size(min = 5, max = 200, message = "title must be between 5 and 200 characters")
        String title,

        @NotBlank(message = "description is required")
        @Size(min = 10, max = 5000, message = "description must be between 10 and 5000 characters")
        String description,

        @NotNull(message = "category is required")
        Category category
) {
}
