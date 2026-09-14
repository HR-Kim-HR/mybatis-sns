package net.likelion.bebc25.sns.service;

import net.likelion.bebc25.sns.dto.PostCreateRequest;
import net.likelion.bebc25.sns.dto.PostDetailResponse;
import net.likelion.bebc25.sns.dto.PostResponse;
import net.likelion.bebc25.sns.dto.PostSearchRequest;
import net.likelion.bebc25.sns.dto.PostUpdateRequest;

import java.util.List;

public interface PostService {

    // 게시글 신규 등록 (생성된 PK 반환)
    PostResponse createPost(PostCreateRequest dto);

    // 게시글 단건 기본 조회
    PostResponse getPostById(Long id);

    // 게시글 복합 상세 조회 (게시글 + 작성자 + 댓글 목록)
    PostDetailResponse getPostDetailById(Long id);

    // 다중 조건 동적 검색 및 정렬 목록 조회
    List<PostResponse> searchPosts(PostSearchRequest condition);

    // 게시글 본문 및 이미지 수정
    void updatePost(Long id, PostUpdateRequest dto);

    // 게시글 단건 삭제
    void deletePost(Long id);

    // 다중 ID 일괄 삭제
    void deletePosts(List<Long> idList);
}