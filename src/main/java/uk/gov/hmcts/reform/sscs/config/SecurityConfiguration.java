package uk.gov.hmcts.reform.sscs.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.oauth2.server.resource.web.BearerTokenAuthenticationFilter;
import uk.gov.hmcts.reform.authorisation.filters.ServiceAuthFilter;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;

@Configuration
@EnableWebSecurity
@ConfigurationProperties(prefix = "spring.security")
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

    private final ServiceAuthFilter serviceAuthFilter;

    private final List<String> anonymousPaths = new ArrayList<>();

    @Autowired
    public SecurityConfiguration(ServiceAuthFilter serviceAuthFilter) {
        super();
        this.serviceAuthFilter = serviceAuthFilter;
    }

    @Override
    public void configure(WebSecurity web) {
        web.ignoring().antMatchers(anonymousPaths.toArray(String[]::new));
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            .addFilterBefore(serviceAuthFilter, BearerTokenAuthenticationFilter.class)
            .sessionManagement()
            .sessionCreationPolicy(STATELESS)
            .and()
            .httpBasic()
            .disable()
            .formLogin()
            .disable()
            .logout()
            .disable()
            .csrf()
            .disable();
    }

    public List<String> getAnonymousPaths() {
        return anonymousPaths;
    }
}
