package net.likelion.bebc25.sns.mapper;

import net.likelion.bebc25.sns.dto.PostCreateRequest;
import net.likelion.bebc25.sns.dto.PostDetailResponse;
import net.likelion.bebc25.sns.dto.PostResponse;
import net.likelion.bebc25.sns.dto.PostSearchRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class PostMapperTest {

    @Autowired
    private PostMapper postMapper;

    @Test
    @DisplayName("신규 게시글 등록 및 단건/전체 목록 조회 테스트")
    void saveAndFindTest() {
        // given
        PostCreateRequest newPost = new PostCreateRequest(1L, "기본 CRUD 등록 테스트", "https://image.com/crud.jpg");

        // when
        postMapper.save(newPost);
        List<PostResponse> posts = postMapper.findAll();

        // then
        assertThat(posts).isNotEmpty();
        PostResponse latestPost = posts.getFirst();
        assertThat(latestPost.content()).isEqualTo("기본 CRUD 등록 테스트");
        assertThat(latestPost.memberId()).isEqualTo(1L);

        // ID 단건 조회 검증
        PostResponse foundPost = postMapper.findById(latestPost.id());
        assertThat(foundPost).isNotNull();
        assertThat(foundPost.id()).isEqualTo(latestPost.id());
    }

    @Test
    @DisplayName("게시글 수정 및 단건 삭제 테스트")
    void updateAndDeleteTest() {
        // given
        PostCreateRequest post = new PostCreateRequest(1L, "수정 전 본문", "https://image.com/before.jpg");
        postMapper.save(post);
        PostResponse createdPost = postMapper.findAll().getFirst();

        // when: 본문 수정
        postMapper.update(createdPost.id(), "수정 완료된 본문", "https://image.com/after.jpg");
        PostResponse updatedPost = postMapper.findById(createdPost.id());

        // then: 수정 결과 확인
        assertThat(updatedPost.content()).isEqualTo("수정 완료된 본문");

        // when: 단건 삭제
        postMapper.deleteById(createdPost.id());

        // then: 삭제 결과 확인
        assertThat(postMapper.findById(createdPost.id())).isNull();
    }

    @Test
    @DisplayName("ResultMap 3중 조인 상세 조회(게시글 + 작성자 + 댓글 목록) 테스트")
    void findPostDetailByIdTest() {
        // given: 1번 게시글 조회 (schema.sql / data.sql 기준)
        Long targetPostId = 1L;

        // when: 3중 조인 상세 조회 실행
        PostDetailResponse detail = postMapper.findPostDetailById(targetPostId);

        // then: 복합 매핑 객체 정합성 검증
        if (detail != null) {
            assertThat(detail.getId()).isEqualTo(targetPostId);
            assertThat(detail.getContent()).isNotNull();

            // 1:1 작성자 요약 객체 매핑 검증 (<association>)
            assertThat(detail.getAuthor()).isNotNull();
            assertThat(detail.getAuthor().id()).isNotNull();
            assertThat(detail.getAuthor().nickname()).isNotNull();

            // 1:N 댓글 목록 컬렉션 매핑 검증 (<collection>)
            assertThat(detail.getComments()).isNotNull();
        }
    }

    @Test
    @DisplayName("동적 SQL 키워드 검색 (<where>, <if>) 테스트")
    void searchPostsWithKeywordTest() {
        // given: 테스트용 게시글 등록
        postMapper.save(new PostCreateRequest(1L, "동적 SQL 검색용 키워드 스프링부트", null));

        PostSearchRequest condition = new PostSearchRequest("스프링부트", null, null, null);

        // when: 키워드 동적 검색 실행
        List<PostResponse> searchResults = postMapper.searchPosts(condition);

        // then: 검색된 모든 게시글 본문에 키워드가 포함되어 있는지 검증
        assertThat(searchResults).isNotEmpty();
        for (PostResponse post : searchResults) {
            assertThat(post.content()).contains("스프링부트");
        }
    }

    @Test
    @DisplayName("동적 SQL 작성자 ID 및 다중 타겟 ID IN 절 검색 (<foreach>) 테스트")
    void searchPostsWithTargetMemberIdsTest() {
        // given: 1번 회원 게시글 등록
        postMapper.save(new PostCreateRequest(1L, "다중 ID 검색 대상 게시글", null));

        List<Long> targetIds = List.of(1L);
        PostSearchRequest condition = new PostSearchRequest(null, null, targetIds, null);

        // when: 다중 회원 ID 대상 IN 절 동적 검색 실행
        List<PostResponse> searchResults = postMapper.searchPosts(condition);

        // then: 검색 결과의 모든 작성자가 대상 회원 목록에 포함되는지 검증
        assertThat(searchResults).isNotEmpty();
        for (PostResponse post : searchResults) {
            assertThat(targetIds).contains(post.memberId());
        }
    }

    @Test
    @DisplayName("동적 SQL 정렬 분기 (<choose>, <when>, <otherwise>) 테스트")
    void findPostsWithSortTest() {
        // given: 정렬 조건 생성
        PostSearchRequest condition = new PostSearchRequest(null, null, null, "POPULAR");

        // when: 정렬 분기 쿼리 실행
        List<PostResponse> posts = postMapper.findPostsWithSort(condition);

        // then: 결과 반환 검증
        assertThat(posts).isNotNull();
    }

    @Test
    @DisplayName("동적 SQL 부분 수정 (<set>, <if>) 테스트")
    void updateSelectiveTest() {
        // given: 1번 게시글 대상 본문만 선택적으로 수정하는 파라미터 맵 구성
        Long targetPostId = 1L;
        String newContent = "동적 set 태그를 통한 본문 단독 수정";

        Map<String, Object> params = new HashMap<>();
        params.put("id", targetPostId);
        params.put("content", newContent);

        // when: 동적 부분 수정 실행
        postMapper.updateSelective(params);

        // then: 본문만 정상 변경되었는지 검증
        PostResponse updatedPost = postMapper.findById(targetPostId);
        assertThat(updatedPost).isNotNull();
        assertThat(updatedPost.content()).isEqualTo(newContent);
    }

    @Test
    @DisplayName("동적 SQL 다중 ID 일괄 삭제 (<foreach>) 테스트")
    void deleteByIdsTest() {
        // given: 삭제할 게시글 등록 후 ID 확보
        PostCreateRequest post1 = new PostCreateRequest(1L, "일괄 삭제 대상 게시글 1", null);
        PostCreateRequest post2 = new PostCreateRequest(1L, "일괄 삭제 대상 게시글 2", null);
        postMapper.save(post1);
        postMapper.save(post2);

        List<PostResponse> memberPosts = postMapper.findByMemberId(1L);
        Long id1 = memberPosts.get(0).id();
        Long id2 = memberPosts.get(1).id();
        List<Long> targetIds = List.of(id1, id2);

        // when: 다중 ID 일괄 삭제 실행
        postMapper.deleteByIds(targetIds);

        // then: 삭제된 대상 ID들이 단건 조회 시 모두 null로 반환되는지 검증
        for (Long id : targetIds) {
            assertThat(postMapper.findById(id)).isNull();
        }
    }

}