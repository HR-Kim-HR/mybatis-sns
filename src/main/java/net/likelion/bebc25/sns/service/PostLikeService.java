package net.likelion.bebc25.sns.service;

import net.likelion.bebc25.sns.dto.LikeToggleResponse;

public interface PostLikeService {
    LikeToggleResponse toggleLike(Long memberId, Long postId);
}
