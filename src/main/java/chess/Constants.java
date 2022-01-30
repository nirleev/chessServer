package chess;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

/**
 * Contains basic constants for application.
 */
@Configuration("Constants")
public class Constants{

    /**
     * Secret key used for generating JSON Web Token
     */
    @Value("${config.JWT_SECRET_KEY}")
    String JWT_SECRET_KEY;

    /**
     * After this time without call to any API method logged user will be considered inactive.
     */
    @Value("${config.MAX_USER_INACTIVE}")
    long MAX_USER_INACTIVE;

    @Value("${config.LOG}")
    boolean LOG;

    public String getJWT_SECRET_KEY(){
        return JWT_SECRET_KEY;
    }

    public long getMAX_USER_INACTIVE() {
        return MAX_USER_INACTIVE;
    }

    public boolean getLOG() { return LOG; }
}