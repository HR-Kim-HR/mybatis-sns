package net.likelion.bebc25.sns.security.config;

import net.likelion.bebc25.sns.security.filter.RequestAuditFilter;
import net.likelion.bebc25.sns.security.handler.CustomAccessDeniedHandler;
import net.likelion.bebc25.sns.security.handler.CustomAuthenticationEntryPoint;
import net.likelion.bebc25.sns.security.handler.OAuth2SuccessHandler;
import net.likelion.bebc25.sns.security.jwt.JwtAuthenticationFilter;
import net.likelion.bebc25.sns.security.jwt.JwtProvider;
import net.likelion.bebc25.sns.security.oauth.CustomOAuth2UserService;
import net.likelion.bebc25.sns.security.service.CustomUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.context.SecurityContextHolderFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity   // 컨트롤러나 서비스 계층 메서드 단위의 보안 검증 작업 활성화
public class SecurityConfig
{
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    private final JwtProvider jwtProvider;
    private final CustomUserDetailsService userDetailsService;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2SuccessHandler oAuth2SuccessHandler;

    public SecurityConfig(JwtProvider jwtProvider,
                          CustomUserDetailsService userDetailsService,
                          CustomOAuth2UserService customOAuth2UserService,
                          OAuth2SuccessHandler oAuth2SuccessHandler)
    {
        this.jwtProvider = jwtProvider;
        this.userDetailsService = userDetailsService;
        this.customOAuth2UserService = customOAuth2UserService;
        this.oAuth2SuccessHandler = oAuth2SuccessHandler;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            CustomAuthenticationEntryPoint customAuthenticationEntryPoint,
            CustomAccessDeniedHandler customAccessDeniedHandler) throws Exception {
        http

                // 보안 감사 필터 등록 (요청 유입 및 처리 소요 시간 계측)
                .addFilterBefore(
                        new RequestAuditFilter(),
                        SecurityContextHolderFilter.class
                )

                // CSRF 공격 방어 기능 비활성화
                // 쿠키를 사용할 경우에 하는 공격
                .csrf(AbstractHttpConfigurer::disable)

                // HTTP 기본 인증 활성화
//                .httpBasic(Customizer.withDefaults())
                // HTTP 기본 인증 비활성화
                .httpBasic(AbstractHttpConfigurer::disable)

                // 기본 폼 로그인 비활성화
                .formLogin(AbstractHttpConfigurer::disable)

                // 세션 생성 및 보관 비활성화
                // RESR API할 때에는 세션을 안할 것
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // Filter에서 발생하는 예외 처리 핸들러
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(customAuthenticationEntryPoint)
                        .accessDeniedHandler(customAccessDeniedHandler)
                )

                // 커스텀 JWT 인증 필터를 UsernamePasswordAuthenticationFilter 바로 앞에 배치
                .addFilterBefore(
                        new JwtAuthenticationFilter(jwtProvider, userDetailsService),
                        UsernamePasswordAuthenticationFilter.class
                )

                .authorizeHttpRequests(auth -> auth

                        // 소셜 로그인 테스트용
                        .requestMatchers("/login.html", "/favicon.ico", "/oauth/**").permitAll()

                        // 게시글 목록 및 상세 조회(GET)는 비로그인 사용자에게도 공개 허용
                        .requestMatchers(HttpMethod.GET, "/api/v1/posts/**").permitAll()

                        // 공지사항 조회(GET)는 비로그인 사용자에게도 공개 허용
                        .requestMatchers(HttpMethod.GET, "/api/v1/notices/**").permitAll()

                        // 공지사항 등록, 수정, 삭제(POST, PUT, DELETE 등)는 관리자 또는 매니저 권한 필수
                        .requestMatchers("/api/v1/notices/**").hasAnyRole("ADMIN", "MANAGER")

                        // 로그인, 회원가입 등 인증 진입 엔드포인트 접근 허용
                        .requestMatchers("/api/v1/auth/**").permitAll()

                        // H2 인메모리 데이터베이스 웹 콘솔 접근 허용 (개발 환경 전용)
                        .requestMatchers("/h2-console/**").permitAll()

                        // Swagger UI 및 OpenAPI API 사양 문서 화면 접근 허용
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()

                        // 소셜 로그인 테스트용
                        .requestMatchers("/oauth/login.html", "/favicon.ico", "/oauth/**").permitAll()

                        // 관리자 전용 엔드포인트 (ROLE_ADMIN 권한 필수)
                        .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")

                        // 그 외 모든 요청(게시글 작성, 수정, 삭제 등)은 로그인 인증을 거쳐야 함
                        .anyRequest().authenticated()
                )

                .oauth2Login(oauth2 -> oauth2
                        // 1. 소셜 사용자 프로필 조회 및 DB 저장 커스텀 서비스 등록
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(customOAuth2UserService)
                        )
                        // 2. 소셜 인증 성공 후 자체 JWT 발급 및 프론트엔드 리다이렉트 핸들러 등록
                        .successHandler(oAuth2SuccessHandler)
                );

        return http.build();
    }
}