package me.jean.jwttutorial.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// Database의 테이블과 1:1 매핑되는 객체임을 의미
@Entity
// table명을 user로 지정
@Table(name = "user")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class User {

    // pk로 지정
    @Id
    @Column(name = "user_id")
    // pk를 자동 증가시킴
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    @Column(name = "username", length = 50, unique = true)
    private String username;

    @Column(name = "password", length = 100)
    private String password;

    @Column(name = "nickname", length = 50)
    private String nickname;

    @Column(name = "activated")
    private boolean activated;

    // User객체와 Authority객체의 다 대 다 관계를
    @ManyToMany
    // join 테이블로 정의
    @JoinTable(
        name = "user_authority",
        // 다 대 일 관계의
        joinColumns = {@JoinColumn(name = "user_id", referencedColumnName = "user_id")},
        // 일 대 다
        inverseJoinColumns = {@JoinColumn(name = "authority_name", referencedColumnName = "authority_name")}
    )
    // -> user_authority라는 user_id와 authority_name을 컬럼으로 갖는 테이블 생성
    private Set<Authority> authorities;
}
