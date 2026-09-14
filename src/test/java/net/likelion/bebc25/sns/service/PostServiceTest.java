package net.likelion.bebc25.sns.service;

import net.likelion.bebc25.sns.dto.PostCreateRequest;
import net.likelion.bebc25.sns.dto.PostDetailResponse;
import net.likelion.bebc25.sns.dto.PostResponse;
import net.likelion.bebc25.sns.dto.PostUpdateRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class PostServiceTest {

    @Autowired
    private PostService postService;

    @Test
    @DisplayName("신규 게시글 등록 및 단건 조회 테스트")
    void createAndFindPostTest() {
        // given
        PostCreateRequest createDto = new PostCreateRequest(1L, "서비스 계층 등록 테스트 본문", "https://image.com/service.jpg");

        // when
        PostResponse createdPost = postService.createPost(createDto);
        PostResponse foundPost = postService.getPostById(createdPost.id());

        // then
        assertThat(createdPost.id()).isNotNull();
        assertThat(foundPost.content()).isEqualTo("서비스 계층 등록 테스트 본문");
        assertThat(foundPost.memberId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("게시글 상세 복합 조인 조회 테스트")
    void getPostDetailByIdTest() {
        // given: data.sql의 1번 게시글 (작성자: 1번 회원, 댓글: 3건 등록)
        Long postId = 1L;

        // when
        PostDetailResponse detail = postService.getPostDetailById(postId);

        // then
        assertThat(detail).isNotNull();
        assertThat(detail.getId()).isEqualTo(postId);
        assertThat(detail.getAuthor().nickname()).isNotNull();
        assertThat(detail.getAuthor().nickname()).isEqualTo("스프링러버");
        assertThat(detail.getComments()).hasSize(3);
    }

    @Test
    @DisplayName("게시글 수정 테스트")
    void updatePostTest() {
        // given
        Long postId = 1L;
        PostUpdateRequest updateDto = new PostUpdateRequest("수정된 비즈니스 본문 내용", "https://image.com/updated.jpg");

        // when
        postService.updatePost(postId, updateDto);
        PostResponse updatedPost = postService.getPostById(postId);

        // then
        assertThat(updatedPost.content()).isEqualTo("수정된 비즈니스 본문 내용");
        assertThat(updatedPost.imageUrl()).isEqualTo("https://image.com/updated.jpg");
    }

    @Test
    @DisplayName("게시글 단건 삭제 테스트")
    void deletePostTest() {
        // given
        Long postId = 2L;

        // when
        postService.deletePost(postId);

        // then: 삭제된 게시글 조회 시 IllegalArgumentException 발생 검증
        assertThatThrownBy(() -> postService.getPostById(postId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("존재하지 않는 게시글입니다.");
    }

    @Test
    @DisplayName("다중 ID 일괄 삭제 테스트")
    void deletePostsTest() {
        // given: 4번, 5번 게시글 일괄 삭제
        List<Long> deleteIds = List.of(4L, 5L);

        // when
        postService.deletePosts(deleteIds);

        // then
        assertThatThrownBy(() -> postService.getPostById(4L))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> postService.getPostById(5L))
                .isInstanceOf(IllegalArgumentException.class);
    }
}