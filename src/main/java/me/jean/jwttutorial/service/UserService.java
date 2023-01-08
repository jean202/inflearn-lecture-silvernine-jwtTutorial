package me.jean.jwttutorial.service;

import java.util.Collections;
import me.jean.jwttutorial.dto.UserDto;
import me.jean.jwttutorial.entity.Authority;
import me.jean.jwttutorial.entity.User;
import me.jean.jwttutorial.exception.DuplicateMemberException;
import me.jean.jwttutorial.exception.NotFoundMemberException;
import me.jean.jwttutorial.repository.UserRepository;
import me.jean.jwttutorial.util.SecurityUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UserDto signup(UserDto userDto) {
        if (userRepository.findOneWithAuthoritiesByUsername(userDto.getUsername()).orElse(null) != null) {
            throw new DuplicateMemberException("이미 가입되어 있는 유저입니다.");
        }

        Authority authority = Authority.builder()
            .authorityName("ROLE_USER")
            .build();

        User user = User.builder()
            .username(userDto.getUsername())
            .password(passwordEncoder.encode(userDto.getPassword()))
            .nickname(userDto.getNickname())
            .authorities(Collections.singleton(authority))
            .activated(true)
            .build();

        return UserDto.from(userRepository.save(user));
    }

    // user와 권한정보를 가져오는 메서드 두개 : 두 가지 메서드의 허용 권한을 다르게 해서 권한검증에 대한 부분을 테스트 해 보자
    // username을 매개변수로 받아서 어떤 username이든 username에 해당하는 user객체와 권한정보를 가지고 올 수 있는 메서드
    @Transactional(readOnly = true)
    public UserDto getUserWithAuthorities(String username) {
        return UserDto.from(userRepository.findOneWithAuthoritiesByUsername(username).orElse(null));
    }

    // 현재 SecurityContext에 저장되어 있는 username에 해당되는 user정보와 권한정보만 받아올 수 있
    @Transactional(readOnly = true)
    public UserDto getMyUserWithAuthorities() {
        return UserDto.from(
            SecurityUtil.getCurrentUsername()
                .flatMap(userRepository::findOneWithAuthoritiesByUsername)
                .orElseThrow(() -> new NotFoundMemberException("Member not found"))
        );
    }
}