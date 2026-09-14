package net.likelion.bebc25.sns.dto;

import net.likelion.bebc25.sns.domain.Member;

import java.time.LocalDateTime;

public record MemberProfileResponse(
        Long id,
        String email,
        String nickname,
        String profileImage,
        String role,
        LocalDateTime createdAt
) {
    public static MemberProfileResponse from(Member member) {
        return new MemberProfileResponse(
                member.getId(),
                member.getEmail(),
                member.getNickname(),
                member.getProfileImage(),
                member.getRole(),
                member.getCreatedAt()
        );
    }
}
