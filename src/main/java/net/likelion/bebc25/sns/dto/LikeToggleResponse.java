package net.likelion.bebc25.sns.dto;

public record LikeToggleResponse(
        boolean liked, // 좋아요 활성화 상태 여부 (true: 좋아요 등록, false: 좋아요 취소)
        int likeCount // 게시글의 총 좋아요 수
){}
