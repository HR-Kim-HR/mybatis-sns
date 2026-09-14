package net.likelion.bebc25.sns.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@Builder
public class PostCreateRequest {

    @Schema(hidden = true)
    private Long id;

    @Schema(hidden = true)
    private Long memberId;

    @Schema(description = "게시글 본문 내용", example = "스프링 부트 학습중", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "본문 내용은 필수 입니다.")
    @Size(max = 1000, message = "본문은 1000자 이하여야 합니다.")
    private String content;

    @Schema(description = "첨부 이미지 URL", example = "https://sample.com/images/hello.png", nullable = true)
    private String imageUrl;

    public PostCreateRequest(Long memberId, String content, String imageUrl) {
        this.memberId = memberId;
        this.content = content;
        this.imageUrl = imageUrl;
    }

}
