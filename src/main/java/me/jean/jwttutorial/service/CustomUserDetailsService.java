package me.jean.jwttutorial.service;

import java.util.List;
import java.util.stream.Collectors;
import me.jean.jwttutorial.entity.User;
import me.jean.jwttutorial.repository.UserRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component("userDetailsService")
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // login시에 DB에서 사용자 정보와 권한 정보를 가져온다.
    @Override
    @Transactional
    public UserDetails loadUserByUsername(final String username) {
        return userRepository.findOneWithAuthoritiesByUsername(username)
            .map(user -> createUser(username, user))
            .orElseThrow(() -> new UsernameNotFoundException(username + " -> 데이터베이스에서 레코드를 찾을 수 없습니다."));
    }

    // DB에서 가져온 사용자 정보와 권한 정보를 바탕으로
    private org.springframework.security.core.userdetails.User createUser(String username, User user) {
        // 해당 사용자가 활성화 상태라면
        if (!user.isActivated()) {
            throw new RuntimeException(username + "해당 사용자가 활성화 되어 있지 않습니다.");
        }
        // 사용자의 권한 정보들과
        List<GrantedAuthority> grantedAuthorities = user.getAuthorities().stream()
            .map(authority -> new SimpleGrantedAuthority(authority.getAuthorityName()))
            .collect(Collectors.toList());

        // username, password를 가지고 userDetails.User객체를 생성해서 반환한다.
        return new org.springframework.security.core.userdetails.User(user.getUsername(),
            user.getPassword(),
            grantedAuthorities);
    }
}
