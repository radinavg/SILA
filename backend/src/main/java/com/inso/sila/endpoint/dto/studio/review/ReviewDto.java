package com.inso.sila.endpoint.dto.studio.review;

import com.inso.sila.endpoint.dto.user.UserDetailDto;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record ReviewDto(
       Long reviewId,
       String text,
       Integer rating,
       UserDetailDto user,
       LocalDateTime createdAt
){
}
