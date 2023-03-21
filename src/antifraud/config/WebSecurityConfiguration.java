package antifraud.config;

import antifraud.auth.Role;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;

import javax.servlet.http.HttpServletResponse;

@Configuration
public class WebSecurityConfiguration {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.httpBasic()
                .authenticationEntryPoint(restAuthenticationEntryPoint()) // Handles auth error
                .and()
                .csrf().disable().headers().frameOptions().disable() // for Postman, the H2 console
                .and()
                .authorizeRequests() // manage access
                .antMatchers("/h2-console/**").permitAll()
                .antMatchers("/actuator/shutdown").permitAll() // needs to run test
                .antMatchers(HttpMethod.POST, "/api/auth/user").permitAll()
                .antMatchers(HttpMethod.DELETE, "/api/auth/user/**").hasRole(Role.ADMINISTRATOR.name())
                .antMatchers(HttpMethod.PUT, "/api/auth/access", "/api/auth/role").hasRole(Role.ADMINISTRATOR.name())
                .antMatchers(HttpMethod.GET, "/api/auth/list").hasAnyRole(Role.ADMINISTRATOR.name(), Role.SUPPORT.name())
                .antMatchers(HttpMethod.POST, "/api/antifraud/transaction").hasRole(Role.MERCHANT.name())
                .antMatchers(HttpMethod.POST, "/api/antifraud/suspicious-ip", "api/antifraud/stolencard").hasRole(Role.SUPPORT.name())
                .antMatchers(HttpMethod.GET, "/api/antifraud/suspicious-ip", "api/antifraud/stolencard").hasRole(Role.SUPPORT.name())
                .antMatchers(HttpMethod.DELETE, "/api/antifraud/suspicious-ip/**", "api/antifraud/stolencard/**").hasRole(Role.SUPPORT.name())
                .anyRequest().denyAll()
                .and()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS); // no session

        return http.build();
    }

    private AuthenticationEntryPoint restAuthenticationEntryPoint() {
        return (request, response, authException) -> response.sendError(HttpServletResponse.SC_UNAUTHORIZED, authException.getMessage());
    }


    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
