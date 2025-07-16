package org.employee.surverythymeleaf.Configuration;

import org.employee.surverythymeleaf.service.CustomUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.rememberme.JdbcTokenRepositoryImpl;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;

import javax.sql.DataSource;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;

    private final DataSource dataSource;
    private final CustomAuthenticationSuccessHandler successHandler;

    public SecurityConfig(CustomAuthenticationSuccessHandler successHandler, CustomUserDetailsService userDetailsService, DataSource dataSource) {
        this.successHandler = successHandler;
        this.userDetailsService = userDetailsService;
        this.dataSource = dataSource;
    }

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, PersistentTokenRepository persistentTokenRepository) throws Exception{
        http

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.POST,"/sale/survey/add").hasAnyAuthority("Sale","Admin")
                        .requestMatchers(HttpMethod.DELETE,"/admin/user/delete").hasAuthority("Admin")
                        .requestMatchers("/login","/register","/css/**","/js/**").permitAll()
                        .requestMatchers("/admin","/","/admin/**").hasAuthority("Admin")
                        .requestMatchers("/sale","/sale/**").hasAnyAuthority("Sale","Admin")
                        .requestMatchers("/technical","/technical/**").hasAnyAuthority("Technical","Admin")
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
//                        .defaultSuccessUrl("/",true)
                        .successHandler(successHandler)
                        .permitAll()
                )
                .rememberMe(remember -> remember
                .tokenRepository(persistentTokenRepository(dataSource))
                .tokenValiditySeconds(7 * 24 * 60 * 60)
                .userDetailsService(userDetailsService)
                .key("Remember-Me-Key-38384792983")
                )

                .logout(logout -> logout
                                .logoutUrl("/logout")
                                .deleteCookies("JSESSIONID", "remember-me")
                                .logoutSuccessUrl("/login?logout")
                                .permitAll()
                        );

        return http.build();
    }

    @Bean
    public DaoAuthenticationProvider authProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(bCryptPasswordEncoder());
        return provider;
    }

    @Bean
    public PersistentTokenRepository persistentTokenRepository(DataSource dataSource) {
        JdbcTokenRepositoryImpl tokenRepository = new JdbcTokenRepositoryImpl();
        tokenRepository.setDataSource(dataSource);
        tokenRepository.setCreateTableOnStartup(false);
        return tokenRepository;
    }
}
