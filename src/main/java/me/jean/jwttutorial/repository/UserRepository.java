package me.jean.jwttutorial.repository;

import java.util.Optional;
import me.jean.jwttutorial.entity.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

    // username을 기준으로 User정보를 가져오며 그때 권한 정보도 함께 가져온다
    // 해당 쿼리가 수행 될 때 Lazy조회가 아닌 Eager조회로 authorities정보를 같이 가져오게 된
    @EntityGraph(attributePaths = "authorities")
    Optional<User> findOneWithAuthoritiesByUsername(String username);
}
