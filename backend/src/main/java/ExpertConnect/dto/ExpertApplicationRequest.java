package ExpertConnect.dto;

import ExpertConnect.entity.Category;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ExpertApplicationRequest(
        @NotNull(message = "expertise is required")
        Category expertise,

        @NotBlank(message = "bio is required")
        @Size(min = 20, max = 1000, message = "bio must be between 20 and 1000 characters")
        String bio,

        @NotNull(message = "experience is required")
        @Min(value = 0, message = "experience must be non-negative")
        @Max(value = 70, message = "experience must be at most 70 years")
        Integer experience
) {
}
