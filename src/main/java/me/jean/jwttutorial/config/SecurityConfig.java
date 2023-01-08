package me.jean.jwttutorial.config;

import me.jean.jwttutorial.jwt.JwtAccessDeniedHandler;
import me.jean.jwttutorial.jwt.JwtAuthenticationEntryPoint;
import me.jean.jwttutorial.jwt.JwtSecurityConfig;
import me.jean.jwttutorial.jwt.TokenProvider;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.filter.CorsFilter;

/*
//@EnableWebSecurity
//// @PreAuthorize 어노테이션을 메서드 단위로 추가하기(사용하기) 위해 적용
//@EnableGlobalMethodSecurity(prePostEnabled = true)
 */
@EnableWebSecurity
@EnableMethodSecurity
@Configuration
public class SecurityConfig {

    private final TokenProvider tokenProvider;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;
    private final CorsFilter corsFilter;

    public SecurityConfig (
        TokenProvider tokenProvider,
        JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint,
        JwtAccessDeniedHandler jwtAccessDeniedHandler,
        CorsFilter corsFilter
    ){
        this.tokenProvider = tokenProvider;
        this.jwtAuthenticationEntryPoint = jwtAuthenticationEntryPoint;
        this.jwtAccessDeniedHandler = jwtAccessDeniedHandler;
        this.corsFilter = corsFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /*
    // h2 console 하위 모든 요청들과 파비콘 관련 요청은 Spring Security로직을 수행하지 않도록
    // public configure메서드를 오버라이드하여 설정
    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring()
            .antMatchers(
                "/h2-console/**",
                "/favicon.ico"
            );

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // token방식을 사용하기 때문에 csrf설정은 disable
        http.csrf().disable()
            // 예외처리
            .exceptionHandling()
            .authenticationEntryPoint(jwtAuthenticationEntryPoint)
            .accessDeniedHandler(jwtAccessDeniedHandler)
            // h2 콘솔을 위한 설정
            .and()
            .headers()
            .frameOptions()
            .sameOrigin()
            // session을 사용하지 않을 것이기 때문에 session설정을 STATELESS로
            .and()
            .sessionManagement()
            .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            // token을 받기 위한 로그인 api, 회원가입을 위한 api 모두 token이 없는 상태에서 요청되기 때문에
            // 인증 없이도 접근을 허
            .and()
            // HttpServletRequest를 사용하는 요청들에 대한 접근제한을 설정하겠다는 의미
            .authorizeRequests()
            // "api/hello"에 대한 접근은 인증 없이 허용하겠다
            .antMatchers("/api/hello").permitAll()
            .antMatchers("/api/authenticate").permitAll()
            .antMatchers("/api/signup").permitAll()
            // 나머지 요청들에 대해서는 모두 인증되어야 한다
            .anyRequest().authenticated()

            // JwtFilter를 addFilterBefore 메서드로 등록한 JwtSecurityConfig클래스도 적용시켜줌(?)
            .and()
            .apply(new JwtSecurityConfig(tokenProvider));
    }
     */

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity
            // token을 사용하는 방식이기 때문에 csrf를 disable합니다.
            .csrf().disable()

            .addFilterBefore(corsFilter, UsernamePasswordAuthenticationFilter.class)

            .exceptionHandling()
            .authenticationEntryPoint(jwtAuthenticationEntryPoint)
            .accessDeniedHandler(jwtAccessDeniedHandler)

            // enable h2-console
            .and()
            .headers()
            .frameOptions()
            .sameOrigin()

            // 세션을 사용하지 않기 때문에 STATELESS로 설정
            .and()
            .sessionManagement()
            .sessionCreationPolicy(SessionCreationPolicy.STATELESS)

            .and()
            .authorizeHttpRequests()
            .requestMatchers("/api/hello", "/api/authenticate", "/api/signup").permitAll()
            .requestMatchers(PathRequest.toH2Console()).permitAll()
            .anyRequest().authenticated()

            .and()
            .apply(new JwtSecurityConfig(tokenProvider));

        return httpSecurity.build();
    }
}
