package chess.filter;

import chess.server.ServerLogger;
import chess.server.ServerStatus;
import org.springframework.http.HttpStatus;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class ExceptionHandlerFilter extends OncePerRequestFilter {

    private final ServerStatus serverStatus;
    private final ServerLogger logger;

    public ExceptionHandlerFilter(ServerStatus serverStatus) {
        this.serverStatus = serverStatus;
        this.logger = new ServerLogger(this.getClass().getName(), serverStatus.getLogStatus());
    }

    @Override
    public void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {
            filterChain.doFilter(request, response);
        } catch (RuntimeException e) {
            logger.log("error", HttpStatus.valueOf(serverStatus.getServerStatus()).toString());
            response.sendError(serverStatus.getServerStatus());
        }
    }
}
