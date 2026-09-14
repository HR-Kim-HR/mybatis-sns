package net.likelion.bebc25.sns.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PostUpdateRequest(
        @Schema(description =  "수정할 본문 내용", example = "수정할 본문 내용입니다.", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "수정할 본문 내용은 필수 입니다.")
        @Size(max = 1000, message = "본문은 1000자 이하여야 합니다.")
        String content,

        @Schema(description = "수정할 이미지 URL", example = "http://s3.aws.com/hello.png", nullable = true)
        String imageUrl
) {}