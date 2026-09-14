package net.likelion.bebc25.sns.service;

import net.likelion.bebc25.sns.dto.PostCreateRequest;
import net.likelion.bebc25.sns.dto.PostDetailResponse;
import net.likelion.bebc25.sns.dto.PostResponse;
import net.likelion.bebc25.sns.dto.PostSearchRequest;
import net.likelion.bebc25.sns.dto.PostUpdateRequest;
import net.likelion.bebc25.sns.mapper.PostMapper;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@Transactional(readOnly = true)
public class PostServiceImpl implements PostService {

    private final PostMapper postMapper;

    public PostServiceImpl(PostMapper postMapper) {
        this.postMapper = postMapper;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PostResponse createPost(PostCreateRequest dto) {
        postMapper.save(dto);
        return PostResponse.from(dto);
    }

    @Override
    public PostResponse getPostById(Long id) {
        PostResponse post = postMapper.findById(id);
        if (post == null) {
            throw new NoSuchElementException("존재하지 않는 게시글입니다. ID: " + id);
        }
        return post;
    }

    @Override
    public PostDetailResponse getPostDetailById(Long id) {
        PostDetailResponse detail = postMapper.findPostDetailById(id);
        if (detail == null) {
            throw new NoSuchElementException("존재하지 않는 게시글입니다. ID: " + id);
        }
        return detail;
    }

    @Override
    public List<PostResponse> searchPosts(PostSearchRequest condition) {
        return postMapper.searchPosts(condition);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @PreAuthorize("hasRole('ADMIN') or @postServiceImpl.isAuthor(#id, authentication.principal.id)")
    public void updatePost(Long id, PostUpdateRequest dto) {
//        // 수정 대상 게시글 존재 여부 사전 검증
//        if (postMapper.findById(id) == null) {
//            throw new NoSuchElementException("수정할 게시글이 존재하지 않습니다. ID: " + id);
//        }
        postMapper.update(id, dto.content(), dto.imageUrl());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @PreAuthorize("hasRole('ADMIN') or @postServiceImpl.isAuthor(#id, authentication.principal.id)")
    public void deletePost(Long id) {
//        // 삭제 대상 게시글 존재 여부 사전 검증
//        if (postMapper.findById(id) == null) {
//            throw new NoSuchElementException("삭제할 게시글이 존재하지 않습니다. ID: " + id);
//        }
        postMapper.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deletePosts(List<Long> idList) {
        if (idList == null || idList.isEmpty()) {
            throw new IllegalArgumentException("삭제할 게시글 ID 목록이 비어있습니다.");
        }
        postMapper.deleteByIds(idList);
    }

    // 게시글 작성자 본인 여부를 검증하는 헬퍼 메서드
    public boolean isAuthor(Long postId, Long memberId) {
        PostResponse post = postMapper.findById(postId);
        return post != null && post.memberId().equals(memberId);
    }

}