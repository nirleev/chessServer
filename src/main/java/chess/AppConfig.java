package chess;

import chess.engine.EngineHandler;
import chess.filter.JwtFilter;
import chess.server.ServerStatus;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;


/**
 * This class contains base configuration for application.
 */
@EnableAutoConfiguration
@ComponentScan
@Configuration
@SuppressWarnings("unused")
public class AppConfig {

    @Bean
    public ServerStatus serverStatus() {
        return new ServerStatus();
    }

    @Bean
    public EngineHandler engineHandler() {
        return new EngineHandler();
    }

    @Bean
    public Constants constantsProperties(){ return new Constants(); }

    /**
     * This method defines which routes should be filtered by {@link JwtFilter}
     * @return configured FilterRegistrationBean
     */
    @Bean
    public FilterRegistrationBean jwtFilter() {
        FilterRegistrationBean registrationBean = new FilterRegistrationBean();
        registrationBean.setFilter(new JwtFilter(serverStatus(), constantsProperties()));
        registrationBean.setUrlPatterns(Arrays.asList(
                "/engine/*",
                "/receiver/*",
                "/ws_engine"
        ));
        return registrationBean;
    }

}

