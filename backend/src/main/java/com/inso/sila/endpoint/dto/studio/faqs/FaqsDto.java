package com.inso.sila.endpoint.dto.studio.faqs;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record FaqsDto(
       Long faqId,
       @NotBlank(message = "Question cannot be blank")
       @Size(max = 1000, message = "Question must not exceed 1000 characters")
       String question,
       @NotBlank(message = "Answer cannot be blank")
       @Size(max = 1000, message = "Answer must not exceed 1000 characters")
       String answer
) {
}
