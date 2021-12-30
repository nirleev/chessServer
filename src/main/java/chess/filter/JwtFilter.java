package chess.filter;

import chess.Constants;
import chess.server.ServerStatus;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureException;
import org.springframework.http.HttpStatus;
import org.springframework.web.filter.GenericFilterBean;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * Filter for user authorization with JWT
 * @see <a href="https://jwt.io/">Json Web Token (JWT)</a>
 */
public class JwtFilter extends GenericFilterBean {
    public static final String ERROR_MISSING_OR_INVALID_HEADER = "Missing or invalid Authorization header";
    public static final String ERROR_SERVER_IS_BUSY = "Server is busy";
    public static final String ERROR_INVALID_TOKEN = "Invalid token";
    public static final String OK = "OK";

    private final ServerStatus serverStatus;

    private final Constants constantsProperties;

    private boolean debug = false;

    public JwtFilter(ServerStatus serverStatus,
                     Constants constantsProperties) {
        this.serverStatus = serverStatus;
        this.constantsProperties = constantsProperties;
    }

    /**
     * Checks whether {@code req} contains {@code Authorization} header and whether this header contains JWT token
     * with required claims. It also checks if server is occupied by another user.
     */
    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
        final HttpServletRequest request = (HttpServletRequest) req;
        serverStatus.updateServerStatus(HttpStatus.OK.value());


        final String authHeader = request.getHeader("authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {

            debugLog("Filter", ERROR_MISSING_OR_INVALID_HEADER);
            serverStatus.updateServerStatus(HttpStatus.UNAUTHORIZED.value());
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED, ERROR_MISSING_OR_INVALID_HEADER);
        }

        final String token = authHeader.substring(7); // The part after "Bearer "

        if (!serverStatus.isServerAvailable(token)) {
            debugLog("Filter", ERROR_SERVER_IS_BUSY);
            serverStatus.updateServerStatus(HttpStatus.SERVICE_UNAVAILABLE.value());
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE, ERROR_SERVER_IS_BUSY);
        }

        try {
            String jwtSecretKey = constantsProperties.getJWT_SECRET_KEY();
            final Claims claims = Jwts.parser()
                    .setSigningKey(jwtSecretKey)
                    .parseClaimsJws(token)
                    .getBody();

            serverStatus.updateUser(token);
            request.setAttribute("claims", claims);
        } catch (Exception e) {
            debugLog("Filter", ERROR_INVALID_TOKEN);
            serverStatus.updateServerStatus(HttpStatus.UNAUTHORIZED.value());
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED, ERROR_INVALID_TOKEN);
        }

        chain.doFilter(req, res);
    }

    /**
     * @param debug if this is true, then this filter will log all exceptions.
     */
    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    private void debugLog(String tag, String msg) {
        if (debug) {
            System.out.printf("[ServerStatus] %s : %s%n", tag, msg);
        }
    }
}