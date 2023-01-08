package me.jean.jwttutorial.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;

@Component
public class TokenProvider implements InitializingBean {

    private final Logger logger = LoggerFactory.getLogger(TokenProvider.class);

    private static final String AUTHORITIES_KEY = "auth";

    private final String secret;
    private final long tokenValidityInMilliseconds;

    private Key key;

    public TokenProvider(
        @Value("${jwt.secret}") String secret,
        @Value("${jwt.token-validity-in-seconds}") long tokenValidityInSeconds
    ) {
        this.secret = secret;
        this.tokenValidityInMilliseconds = tokenValidityInSeconds * 1000;
    }

    // @Component 어노테이션으로 Bean이 생성되고
    // 생성자로 의존성 주입을 받은 후에
    // 주입받은 secret값을 Base64 Decode 해서 key변수에 할당하기 위해
    @Override
    public void afterPropertiesSet() throws Exception {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    // Authentication 객체에 포함되어 있는 권한정보들을 담은 토큰을 생성
    // (Authentication 객체의 권한정보를 이용해서 토큰을 생성)
    public String createToken(Authentication authentication) {
        // Authentication 파라미터를 받아서 권한들을 뽑아냄
        String authorities = authentication.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .collect(Collectors.joining(","));

        // application.yml에 작성한 시간 정보를 받아와서 토큰 만료 시간으로 설정
        long now = (new Date()).getTime();
        Date validity = new Date(now + this.tokenValidityInMilliseconds);

        // jwt 토큰 생성
        return Jwts.builder()
            .setSubject(authentication.getName())
            .claim(AUTHORITIES_KEY, authorities)
            .signWith(key, SignatureAlgorithm.HS512)
            .setExpiration(validity)
            .compact();
    }

    // token을 파라미터로 받아서 token에 담겨 있는 권한 정보들을 이용해서 authentication객체를 반환
    public Authentication getAuthentication(String token) {
        // token을 이용해 Calim을 만든
        Claims claims = Jwts
            .parserBuilder()
            .setSigningKey(key)
            .build()
            .parseClaimsJws(token)
            .getBody();

        // Claim에서 권한정보들 얻기
        Collection<? extends GrantedAuthority> authorities =
            Arrays.stream(claims.get(AUTHORITIES_KEY).toString().split(","))
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());

        // 얻은 권한정보들로 User객체를 만들고
        User principal = new User(claims.getSubject(), "", authorities);
        // User객체, token, 권한정보를 이용해 최종적으로 Authentication객체를 반환한다
        return new UsernamePasswordAuthenticationToken(principal, token, authorities);
    }

    // token을 파라미터로 받아서 token의 유효성을 검사
    public boolean validateToken(String token) {
        try {
            // 파라미터로 받은 token을 파싱해 보고
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
            // 나오는 예외들을 catch하 문제가 있으면 false, 없으면 true를 반환
        } catch (io.jsonwebtoken.security.SignatureException | MalformedJwtException e) {
            logger.info("잘못된 JWT 서명입니다");
        } catch (ExpiredJwtException e) {
            logger.info("만료된 JWT 토큰입니다");
        } catch (UnsupportedJwtException e) {
            logger.info("지원되지 않는 JWT 토큰입니다");
        } catch (IllegalArgumentException e) {
            logger.info("JWT 토큰이 잘못되었습니");
        }
        return false;
    }

}
