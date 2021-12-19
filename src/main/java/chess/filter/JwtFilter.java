package chess.filter;

import chess.Constants;
import chess.server.ServerStatus;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureException;
import org.springframework.web.filter.GenericFilterBean;

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
    public static final String ERROR_MISSING_OR_INVALID_HEADER = "Missing or invalid Authorization header.";
    public static final String ERROR_SERVER_IS_BUSY = "Server is busy";
    public static final String ERROR_INVALID_TOKEN = "Invalid token.";

    private ServerStatus serverStatus;

    private Constants constantsProperties;

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


        final String authHeader = request.getHeader("authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new ServletException(ERROR_MISSING_OR_INVALID_HEADER);
        }

        final String token = authHeader.substring(7); // The part after "Bearer "

        String jwtSecretKey = constantsProperties.getJWT_SECRET_KEY();

        try {
            final Claims claims = Jwts.parser()
                    .setSigningKey(jwtSecretKey)
                    .parseClaimsJws(token)
                    .getBody();

            if (!serverStatus.isServerAvailable(token)) {
                throw new ServletException(ERROR_SERVER_IS_BUSY);
            }

            serverStatus.updateUser(token);
            request.setAttribute("claims", claims);
        } catch (final SignatureException e) {
            throw new ServletException(ERROR_INVALID_TOKEN);
        }

        chain.doFilter(req, res);
    }

}