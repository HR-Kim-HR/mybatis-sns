package net.likelion.bebc25.sns.controller;

import jakarta.validation.Valid;
import net.likelion.bebc25.sns.dto.*;
import net.likelion.bebc25.sns.security.principal.CustomUserDetails;
import net.likelion.bebc25.sns.service.PostService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/posts")
public class PostRestController {

    private final PostService postService;

    public PostRestController(PostService postService) {
        this.postService = postService;
    }

    // 게시글 목록 조회, 검색
    @GetMapping
    public ResponseEntity<List<PostResponse>> getPostList(
            @ModelAttribute PostSearchRequest searchRequest) {
        // 검색어에 해당하는 게시글 목록 조회
        List<PostResponse> posts = postService.searchPosts(searchRequest);
        return ResponseEntity.ok(posts); // 200
    }

    // 게시글 등록
    @PostMapping
    public ResponseEntity<PostResponse> createPost(
//            @RequestHeader("X-Member-Id") Long memberId, // 임시로 헤더에서 추출
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody PostCreateRequest request // JSON 요청 바디를 객체로 자동 매핑
    ) {

        System.out.println(userDetails.getMember());
        request.setMemberId(userDetails.getId());
        PostResponse createdPost = postService.createPost(request);
        URI location = URI.create("/api/v1/posts/" + createdPost.id());
        return ResponseEntity.created(location).body(createdPost); // 201
    }

    // 게시글 한건 조회
    @GetMapping("/{id}")
    public ResponseEntity<PostResponse> getPost(
            @PathVariable("id") Long id) {
        // 전달받은 id를 이용해서 서비스 레이어의 게시글 한건 조회 메서드를 호출
        PostResponse post = postService.getPostById(id);
        // 응답받은 게시글 DTO를 ResponseEntity를 이용하여 응답 상태 코드 200으로 응답
        return ResponseEntity.ok(post);
    }

    // 게시글 수정
    @PutMapping("/{id}")
    public ResponseEntity<PostResponse> updatePost(
            @PathVariable("id") Long id,
//            @RequestHeader("X-Member-Id") Long memberId, // 임시로 헤더에서 추출
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody PostUpdateRequest request) {

//        // 수정 전에 게시글 정보 조회
//        PostResponse post = postService.getPostById(id);
//
//        // 본인의 게시글인지 확인
//        if (!post.memberId().equals(userDetails.getId())) {
//            throw new IllegalStateException("본인의 게시글만 수정이 가능합니다.");
//        }

        // 수정 작업
        postService.updatePost(id, request);

        // 수정된 게시글 조회
        PostResponse updatedPost = postService.getPostById(id);

        // 200 응답 상태 코드와 수정된 게시글 정보로 응답
        return ResponseEntity.ok(updatedPost);

    }

    // 게시글 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePost(
            @PathVariable("id") Long id,
//            @RequestHeader("X-Member-Id") Long memberId // 임시로 헤더에서 추출
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {

//        // 삭제 전에 게시글 정보 조회
//        PostResponse post = postService.getPostById(id);
//
//        // 본인의 게시글인지 확인
//        if (!post.memberId().equals(userDetails.getId())) {
//            throw new IllegalStateException("본인의 게시글만 삭제가 가능합니다.");
//        }

        // 삭제 작업
        postService.deletePost(id);

        // 200 응답 상태 코드와 수정된 게시글 정보로 응답
        return ResponseEntity.noContent().build();

    }
}

    // 주말에 과제로 작성해본 코드
//    @PutMapping("/update/{id}")
//    public ResponseEntity<Void> updatePost (
//            @PathVariable Long id,
//            @RequestBody PostUpdateRequest request
//    ) {
//       postService.updatePost(id, request);
//
//       return ResponseEntity.ok().build(); // 204
//    }
//
//    @DeleteMapping("/delete/{id}")
//    public ResponseEntity<Void> deletePost(
//            @PathVariable Long id
//    ) {
//        postService.deletePost(id);
//
//        return ResponseEntity.ok().build(); // 204
//    }



