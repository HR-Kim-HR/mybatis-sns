package net.likelion.bebc25.sns.dto;

import java.time.LocalDateTime;

public record CommentResponse(
        Long id,
        Long commenterId,
        String commenterNickname,
        String content,
        LocalDateTime createdAt
) {}