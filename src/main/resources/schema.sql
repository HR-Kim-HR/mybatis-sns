-- 0. 기존 테이블 삭제 (외래 키 참조 역순으로 삭제)
DROP TABLE IF EXISTS bookmark;
DROP TABLE IF EXISTS post_like;
DROP TABLE IF EXISTS comment;
DROP TABLE IF EXISTS post_hashtag;
DROP TABLE IF EXISTS post;
DROP TABLE IF EXISTS member_detail;
DROP TABLE IF EXISTS member;

-- 1. 회원 테이블 (기본 개체)
CREATE TABLE member (
                        id BIGINT AUTO_INCREMENT PRIMARY KEY,
                        email VARCHAR(100) NOT NULL UNIQUE,
                        password VARCHAR(255) NOT NULL,
                        nickname VARCHAR(50) NOT NULL,
                        profile_image VARCHAR(255),
                        role VARCHAR(20) NOT NULL DEFAULT 'ROLE_USER',
                        created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- 2. 회원 상세 테이블 (1:1 수직 분할 개체)
CREATE TABLE member_detail (
                               member_id BIGINT PRIMARY KEY,
                               introduction TEXT,
                               address VARCHAR(255),
                               marketing_agreed VARCHAR(1) DEFAULT 'N',
                               FOREIGN KEY (member_id) REFERENCES member(id) ON DELETE CASCADE
);

-- 3. 게시글 테이블 (중심 개체)
CREATE TABLE post (
                      id BIGINT AUTO_INCREMENT PRIMARY KEY,
                      member_id BIGINT NOT NULL,
                      content TEXT NOT NULL,
                      image_url VARCHAR(255),
                      like_count INT DEFAULT 0,
                      created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                      updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                      FOREIGN KEY (member_id) REFERENCES member(id) ON DELETE CASCADE
);

-- 4. 게시글 해시태그 테이블 (1NF 원자값 보장 개체)
CREATE TABLE post_hashtag (
                              id BIGINT AUTO_INCREMENT PRIMARY KEY,
                              post_id BIGINT NOT NULL,
                              tag_name VARCHAR(50) NOT NULL,
                              FOREIGN KEY (post_id) REFERENCES post(id) ON DELETE CASCADE
);

-- 5. 댓글 테이블 (행위 개체)
CREATE TABLE comment (
                         id BIGINT AUTO_INCREMENT PRIMARY KEY,
                         post_id BIGINT NOT NULL,
                         member_id BIGINT NOT NULL,
                         content TEXT NOT NULL,
                         created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                         FOREIGN KEY (post_id) REFERENCES post(id) ON DELETE CASCADE,
                         FOREIGN KEY (member_id) REFERENCES member(id) ON DELETE CASCADE
);

-- 6. 게시글 좋아요 테이블 (N:M 비식별 매핑 개체)
CREATE TABLE post_like (
                           id BIGINT AUTO_INCREMENT PRIMARY KEY,
                           member_id BIGINT NOT NULL,
                           post_id BIGINT NOT NULL,
                           created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                           UNIQUE KEY uk_member_post_like (member_id, post_id),
                           FOREIGN KEY (member_id) REFERENCES member(id) ON DELETE CASCADE,
                           FOREIGN KEY (post_id) REFERENCES post(id) ON DELETE CASCADE
);

-- 7. 북마크 테이블 (N:M 비식별 매핑 개체)
CREATE TABLE bookmark (
                          id BIGINT AUTO_INCREMENT PRIMARY KEY,
                          member_id BIGINT NOT NULL,
                          post_id BIGINT NOT NULL,
                          created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                          UNIQUE KEY uk_member_post_bookmark (member_id, post_id),
                          FOREIGN KEY (member_id) REFERENCES member(id) ON DELETE CASCADE,
                          FOREIGN KEY (post_id) REFERENCES post(id) ON DELETE CASCADE
);