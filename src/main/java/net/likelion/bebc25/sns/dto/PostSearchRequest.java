package net.likelion.bebc25.sns.dto;

import java.util.List;

public record PostSearchRequest(
        String keyword,
        Long memberId,
        List<Long> targetMemberIds,
        String sortOrder
) {}