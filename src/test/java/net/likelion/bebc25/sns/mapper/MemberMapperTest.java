package net.likelion.bebc25.sns.mapper;

import net.likelion.bebc25.sns.domain.Member;
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
class MemberMapperTest {

    @Autowired
    private MemberMapper memberMapper;

    @Test
    @DisplayName("이메일로 회원 정보 조회 테스트")
    void findByEmailTest() {
        // given
        String email = "user1@example.com";

        // when
        Member member = memberMapper.findByEmail(email);

        // then
        assertThat(member).isNotNull();
        assertThat(member.getEmail()).isEqualTo(email);
    }

}