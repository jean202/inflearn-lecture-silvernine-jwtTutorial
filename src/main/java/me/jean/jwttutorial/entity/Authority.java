package me.jean.jwttutorial.entity;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// Database의 테이블과 1:1 매핑되는 객체임을 의미
@Entity
// table명을 authority로 지정
@Table(name = "authority")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Authority {

    // pk로 지정
    @Id
    @Column(name = "authority_name", length = 50)
    private String authorityName;
}
