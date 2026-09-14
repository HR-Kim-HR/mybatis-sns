package net.likelion.bebc25.sns.controller;

import net.likelion.bebc25.sns.domain.Member;
import net.likelion.bebc25.sns.dto.MemberProfileResponse;
import net.likelion.bebc25.sns.security.principal.CustomUserDetails;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/members")
public class MemberRestController {

    @GetMapping("/me")
    public ResponseEntity<MemberProfileResponse> getMyProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails // SecurityContext 에서 인증된 사용자 정보를 반환
    ) {
        Member member = userDetails.getMember();
        return ResponseEntity.ok(MemberProfileResponse.from(member));
    }
}
