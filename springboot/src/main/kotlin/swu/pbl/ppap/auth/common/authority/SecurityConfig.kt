package swu.pbl.ppap.auth.common.authority

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.factory.PasswordEncoderFactories
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.security.web.server.SecurityWebFilterChain


@Configuration
@EnableWebSecurity
class SecurityConfig (
    private val jwtTokenProvider: JwtTokenProvider,
    //private val entryPoint: AuthenticationEntryPoint,
) {
    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
            //jwt 인증방식을 사용할 것
        return http
            .httpBasic { it.disable() }
            .csrf {it.disable() }
            .sessionManagement {
                it.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            }
            .authorizeHttpRequests {
                //anonymous : 인증을 하지 않은 사용자
                // 회원가입, 로그인, 토큰 재발급 Api는 인증 없이 접근 가능하게끔 anonymous 설정
                it.requestMatchers("/api/users/signup", "/api/users/login", "/api/users/token/refresh").anonymous()
                    .requestMatchers("/api/users/**").hasRole("USER")
                    .anyRequest().permitAll()
            }
            .addFilterBefore(
                JwtAuthenticationFilter(jwtTokenProvider), //먼저 실행
                UsernamePasswordAuthenticationFilter::class.java
            )
            .build()
    }
    @Bean
    fun passwordEncoder(): PasswordEncoder =
        PasswordEncoderFactories.createDelegatingPasswordEncoder()
    //spring security5 이상부터는 createDelegatingPasswordEncoder로 passwordEncoder 생성 권장
}