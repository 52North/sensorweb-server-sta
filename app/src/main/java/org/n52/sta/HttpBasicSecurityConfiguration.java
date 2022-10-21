package org.n52.sta;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configurers.provisioning.InMemoryUserDetailsManagerConfigurer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.password.MessageDigestPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class HttpBasicSecurityConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpBasicSecurityConfiguration.class);

    @Value("${server.feature.authentication.httpbasic.credentials:}")
    private String[] credentials;

    @Value("${server.feature.authentication.httpbasic.enabled:false}")
    private boolean enabled;

    @Bean
    public PasswordEncoder sha256PasswordEncoder() {
        return new MessageDigestPasswordEncoder("SHA-256");
    }

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        if (!enabled) {
            return;
        }
        InMemoryUserDetailsManagerConfigurer<AuthenticationManagerBuilder> configurer = auth.inMemoryAuthentication();
        for (String user : credentials) {
            String[] split = user.split(":");
            if (split.length != 2) {
                LOGGER.error("Could not parse credentials. Invalid format!");
            }
            configurer.withUser(split[0])
                      .password(split[1])
                      .authorities("default");
        }
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        if (enabled) {
            // Only allow GET. Require Auth for all else
            http.authorizeRequests()
                .antMatchers(HttpMethod.GET)
                .permitAll()
                .anyRequest()
                .authenticated()
                .and()
                .httpBasic()
                .and().csrf().disable();
        } else {
            // Allow everything
            http.authorizeRequests()
                .anyRequest()
                .permitAll()
                .and().csrf().disable();
        }
        return http.build();
    }

}
