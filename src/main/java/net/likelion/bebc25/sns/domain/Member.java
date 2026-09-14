package net.likelion.bebc25.sns.domain;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class Member {
    private Long id;
    private String email;
    private String password;
    private String nickname;
    private String profileImage;
    @Builder.Default
    private String role = "ROLE_USER";
    private LocalDateTime createdAt;
}
