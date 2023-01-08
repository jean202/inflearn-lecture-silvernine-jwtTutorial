package me.jean.jwttutorial.controller;

import jakarta.validation.Valid;
import me.jean.jwttutorial.dto.LoginDto;
import me.jean.jwttutorial.dto.TokenDto;
import me.jean.jwttutorial.jwt.JwtFilter;
import me.jean.jwttutorial.jwt.TokenProvider;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class AuthController {

    private final TokenProvider tokenProvider;
    private final AuthenticationManagerBuilder authenticationManagerBuilder;

    public AuthController(TokenProvider tokenProvider, AuthenticationManagerBuilder authenticationManagerBuilder) {
        this.tokenProvider = tokenProvider;
        this.authenticationManagerBuilder = authenticationManagerBuilder;
    }

    @PostMapping("/authenticate")
    public ResponseEntity<TokenDto> authorize(@Valid @RequestBody LoginDto loginDto) {
        UsernamePasswordAuthenticationToken authenticationToken =
            new UsernamePasswordAuthenticationToken(loginDto.getUsername(), loginDto.getPassword());

        // Authentication객체를 생성해 SecurityContext에 저장
        // CustomUserDetailsService객체의 loadUserByUserName을 실행하고,
                                        //  AuthenticationManager 의 구현체인 ProviderManager 반환
        Authentication authentication = authenticationManagerBuilder.getObject().
            // ProviderManager의 authenticate메서드 실행
            authenticate(authenticationToken);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Authentication정보로 TokenProvider의 도움을 받아 JWT token을 생성
        String jwt = tokenProvider.createToken(authentication);

        // JWT token을 response header에도 넣어 주고
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add(JwtFilter.AUTHORIZATION_HEADER, "Bearer " + jwt);
        // TokenDto를 사용해서 ResponseBody에도 넣어서 반
        return new ResponseEntity<>(new TokenDto(jwt), httpHeaders, HttpStatus.OK);

    }

}
