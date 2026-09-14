package net.likelion.bebc25.sns.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import net.likelion.bebc25.sns.dto.ApiErrorResponse;
import net.likelion.bebc25.sns.dto.PostCreateRequest;
import net.likelion.bebc25.sns.dto.PostResponse;
import net.likelion.bebc25.sns.dto.PostSearchRequest;
import net.likelion.bebc25.sns.dto.PostUpdateRequest;
import net.likelion.bebc25.sns.service.PostService;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@Tag(name = "SNS 게시글 API", description = "피드 게시글 등록, 조회, 수정, 삭제를 담당하는 REST 컨트롤러")
@ApiResponse(
        responseCode = "500",
        description = "내부 서버 오류",
        content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
)
//@RestController
@RequestMapping("/api/v1/posts")
public class PostRestControllerSwagger {

    private final PostService postService;

    public PostRestControllerSwagger(PostService postService) {
        this.postService = postService;
    }

    @Operation(summary = "게시글 목록 조회 및 검색", description = "검색 키워드 및 정렬 조건에 부합하는 게시글 목록을 반환합니다.")
    @ApiResponse(responseCode = "200", description = "목록 조회 성공")
    @GetMapping
    public ResponseEntity<List<PostResponse>> getPostList(
            @ParameterObject @ModelAttribute PostSearchRequest searchRequest) {
        return ResponseEntity.ok(postService.searchPosts(searchRequest));
    }

    @Operation(summary = "게시글 단건 상세 조회", description = "기본 키 ID에 해당하는 게시글의 상세 정보를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(
                    responseCode = "404",
                    description = "게시글이 존재하지 않음",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            )
    })
    @GetMapping("/{id}")
    public ResponseEntity<PostResponse> getPostDetail(
            @Parameter(description = "조회할 게시글 ID", example = "1")
            @PathVariable("id") Long id) {
        return ResponseEntity.ok(postService.getPostById(id));
    }

    @Operation(summary = "신규 게시글 등록", description = "회원 ID와 게시글 본문, 이미지 URL을 전달받아 피드에 등록합니다.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "게시글 생성 성공",
                    headers = @Header(name = "Location", description = "생성된 게시글의 상세 조회 URI 경로", schema = @Schema(type = "string"))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "입력값 유효성 검증 실패",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            )
    })
    @PostMapping
    public ResponseEntity<PostResponse> createPost(
            @Parameter(description = "작성자 회원 ID", example = "1")
            @RequestHeader("X-Member-Id") Long memberId,
            @Valid @RequestBody PostCreateRequest request) {
        request.setMemberId(memberId); // 임시 헤더의 회원 식별자를 모델(DTO)에 주입
        PostResponse createdPost = postService.createPost(request);
        URI location = URI.create("/api/v1/posts/" + createdPost.id());
        return ResponseEntity.created(location).body(createdPost);
    }

    @Operation(summary = "게시글 수정", description = "게시글 ID와 수정할 본문 내용을 전달받아 데이터를 갱신합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "게시글 수정 성공"),
            @ApiResponse(
                    responseCode = "400",
                    description = "입력값 유효성 검증 실패",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "본인 작성 게시글이 아니므로 수정 권한 없음",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "수정할 대상 게시글이 존재하지 않음",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            )
    })
    @PutMapping("/{id}")
    public ResponseEntity<PostResponse> updatePost(
            @Parameter(description = "작성자 회원 ID", example = "1")
            @RequestHeader("X-Member-Id") Long memberId,
            @Parameter(description = "수정할 게시글 ID", example = "1")
            @PathVariable("id") Long id,
            @Valid @RequestBody PostUpdateRequest request) {
        PostResponse post = postService.getPostById(id);
        if (!post.memberId().equals(memberId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        postService.updatePost(id, request);
        PostResponse updatedPost = postService.getPostById(id);
        return ResponseEntity.ok(updatedPost);
    }

    @Operation(summary = "게시글 삭제", description = "게시글 ID를 전달받아 해당 자원을 삭제합니다.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "게시글 삭제 완료",
                    content = @Content(schema = @Schema(hidden = true))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "본인 작성 게시글이 아니므로 삭제 권한 없음",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "삭제할 대상 게시글이 존재하지 않음",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            )
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePost(
            @Parameter(description = "작성자 회원 ID", example = "1")
            @RequestHeader("X-Member-Id") Long memberId,
            @Parameter(description = "삭제할 게시글 ID", example = "1")
            @PathVariable("id") Long id) {
        PostResponse post = postService.getPostById(id);
        if (!post.memberId().equals(memberId)) {
            throw new IllegalStateException("본인의 게시글 삭제만 가능합니다.");
        }
        postService.deletePost(id);
        return ResponseEntity.noContent().build();
    }
}



//package net.likelion.bebc25.sns.controller;
//
//import io.swagger.v3.oas.annotations.Operation;
//import io.swagger.v3.oas.annotations.Parameter;
//import io.swagger.v3.oas.annotations.media.Content;
//import io.swagger.v3.oas.annotations.media.Schema;
//import io.swagger.v3.oas.annotations.responses.ApiResponse;
//import io.swagger.v3.oas.annotations.responses.ApiResponses;
//import io.swagger.v3.oas.annotations.tags.Tag;
//import jakarta.validation.Valid;
//import net.likelion.bebc25.sns.dto.*;
//import net.likelion.bebc25.sns.service.PostService;
//import org.springdoc.core.annotations.ParameterObject;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//import java.net.URI;
//import java.util.List;
//
//@Tag(name = "게시글 API", description = "게시글 CRUD 기능")
//@RestController
//@RequestMapping("/api/v1/posts")
//public class PostRestControllerSwagger {
//
//    private final PostService postService;
//
//    public PostRestControllerSwagger(PostService postService) {
//        this.postService = postService;
//    }
//
//    // 게시글 목록 조회, 검색
//    @Operation(
//            summary = "게시글 목록 조회, 검색",
//            description = "게시글의 목록을 조회하거나 검색을 수행합니다."
//    )
//    @GetMapping
//    public ResponseEntity<List<PostResponse>> getPostList(
//            @ParameterObject
//            @ModelAttribute PostSearchRequest searchRequest){
//        // 검색어에 해당하는 게시글 목록 조회
//        List<PostResponse> posts = postService.searchPosts(searchRequest);
//        return ResponseEntity.ok(posts); // 200
//    }
//
//    // 게시글 등록
//    @PostMapping
//    public ResponseEntity<PostResponse> createPost(
//            @Parameter(description = "회원 id", example = "1")
//            @RequestHeader("X-Member-Id") Long memberId, // 임시로 헤더에서 추출
//            @Valid @RequestBody PostCreateRequest request // JSON 요청 바디를 객체로 자동 매핑
//    ){
//        request.setMemberId(memberId);
//        PostResponse createdPost = postService.createPost(request);
//        URI location = URI.create("/api/v1/posts/" + createdPost.id());
//        return ResponseEntity.created(location).body(createdPost); // 201
//    }
//
//    // 게시글 한건 조회
//    @Operation(
//            summary = "게시글 상세 조회",
//            description = "id 값으로 게시글의 상세 정보를 조회합니다.<br>대상이 없을 경우 404에러를 반환합니다."
//    )
//    @ApiResponse(
//            responseCode = "200",
//            description = "성공"
//    )
//    @ApiResponse(
//            responseCode = "404",
//            description = "게시글이 존재하지 않음.",
//            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
//    )
//    @GetMapping("/{id}")
//    public ResponseEntity<PostResponse> getPost(
//            @Parameter(description = "게시글 id", example = "1", required = true)
//            @PathVariable("id") Long id){
//        // 전달받은 id를 이용해서 서비스 레이어의 게시글 한건 조회 메서드를 호출
//        PostResponse post = postService.getPostById(id);
//        // 응받 받은 게시글 DTO를 ResponseEntity를 이용해 응답 상태 코드 200으로 응답
//        return ResponseEntity.ok(post);
//    }
//
//    // 게시글 수정
//    @Operation(
//            summary = "게시글 수정",
//            description = "게시글을 수정합니다."
//    )
//    @ApiResponses({
//            @ApiResponse(
//                    responseCode = "200",
//                    description = "성공"
//            ),
//            @ApiResponse(
//                    responseCode = "400",
//                    description = "입력값 유효성 검사 실패.",
//                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
//            ),
//            @ApiResponse(
//                    responseCode = "403",
//                    description = "권한 없음.",
//                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
//            ),
//            @ApiResponse(
//                    responseCode = "404",
//                    description = "게시글이 존재하지 않음.",
//                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
//            )
//    })
//    @PutMapping("/{id}")
//    public ResponseEntity<PostResponse> updatePost(
//            @PathVariable("id") Long id,
//            @RequestHeader("X-Member-Id") Long memberId, // 임시로 헤더에서 추출
//            @Valid @RequestBody PostUpdateRequest request){
//
//        // 수정 전에 게시글 정보 조회
//        PostResponse post = postService.getPostById(id);
//
//        // 본인의 게시글인지 확인
//        if(!post.memberId().equals(memberId)){
//            throw new IllegalStateException("본인의 게시글만 수정이 가능합니다.");
//        }
//
//        // 수정 작업
//        postService.updatePost(id, request);
//
//        // 수정된 게시글 조회
//        PostResponse updatedPost = postService.getPostById(id);
//
//        // 200 응답 상태 코드와 수정된 게시글 정보로 응답
//        return ResponseEntity.ok(updatedPost);
//    }
//
//    // 게시글 삭제
//    @DeleteMapping("/{id}")
//    public ResponseEntity<Void> deletePost(
//            @PathVariable("id") Long id,
//            @RequestHeader("X-Member-Id") Long memberId // 임시로 헤더에서 추출
//    ){
//        // 삭제 전에 게시글 정보 조회
//        PostResponse post = postService.getPostById(id);
//
//        // 본인의 게시글인지 확인
//        if(!post.memberId().equals(memberId)){
//            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
//        }
//
//        // 삭제 작업
//        postService.deletePost(id);
//
//        // 200 응답 상태 코드와 수정된 게시글 정보로 응답
//        return ResponseEntity.noContent().build();
//    }
//
//}