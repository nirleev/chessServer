package chess.server;

import chess.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

/**
 * Contains information about server status i.e. last user activity time and currently logged user token.
 */
@Service
public class ServerStatus {

    /**
     * currently logged user token.
     */
    private String currentUserToken = null;

    /**
     * last logged user activity.
     */
    private long lastUserActivity = 0;

    private int HTTPStatus = HttpStatus.OK.value();

    @Autowired
    private final Constants constantsProperties;

    public ServerStatus(Constants constantsProperties) {
        this.constantsProperties = constantsProperties;
    }

    /**
     * Checks if server is available for user with token {@code token} i.e. {@link #currentUserToken} is null, or
     * {@link #currentUserToken} equals {#code token} and {@link #lastUserActivity} was more then
     * {@link Constants#getMAX_USER_INACTIVE()} ms ago.
     *
     * @param token token of user for which you want to check server availability
     * @return whether server is available for user, or not
     */
    public boolean isServerAvailable(String token) {
        if (currentUserToken == null) return true;

        if (token.equals(currentUserToken)) {
            return true;
        }

        long maxUserInactive = constantsProperties.getMAX_USER_INACTIVE();

        if ((System.currentTimeMillis() - lastUserActivity) > maxUserInactive) {
            currentUserToken = null;
            return true;
        }

        return false;
    }

    public void updateServerStatus(int status){
        this.HTTPStatus = status;
    }

    public int getServerStatus(){
        return HTTPStatus;
    }

    /**
     * Should be called every time user makes some authorized action (calls route).
     * @param token user token
     */
    public void updateUser(String token) {
        if (isServerAvailable(token)) {
            this.currentUserToken = token;
            lastUserActivity = System.currentTimeMillis();
        }
    }

    /**
     * Should be called when user logs out.
     */
    public String userLoggedOut() {

        currentUserToken = null;
        return "Logged out";
    }

    public String getCurrentUserToken(){
        return currentUserToken;
    }
}
