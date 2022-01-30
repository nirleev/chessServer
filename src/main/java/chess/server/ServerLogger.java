package chess.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServerLogger {

    private boolean log;

    private final Logger logger;

    public ServerLogger(String className, boolean log) {
        this.logger = LoggerFactory.getLogger(className);
        this.log = log;
    }

    public void log(String tag, String msg) {
        if(log){
            logger.info(String.format("[%s] %s", tag, msg));
        }
    }
}
