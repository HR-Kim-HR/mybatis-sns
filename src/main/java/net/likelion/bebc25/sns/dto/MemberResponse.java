package net.likelion.bebc25.sns.dto;

public record MemberResponse(
        Long id,
        String nickname,
        String profileImage
) {}