package com.inso.sila.endpoint.dto.studio.membership;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record MembershipDto(
       Long membershipId,
       @NotBlank(message = "Membership name cannot be blank")
       @Size(max = 255, message = "Membership name must not exceed 255 characters")
       String name,
       Integer duration,
       @Min(value = 0, message = "Minimum duration of the membership cannot be negative")
       Integer minDuration,
       @Min(value = 0, message = "Price of the membership cannot be negative")
       Float price
){
}
