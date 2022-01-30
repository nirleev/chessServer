package chess.controller;

import chess.model.MessageModel;
import chess.server.ServerLogger;
import chess.server.ServerStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

/**
 * Handles all receiver related routes
 */
@SuppressWarnings("unused")
@RestController
@RequestMapping("/receiver")
public class ReceiverController {

    private final ServerLogger logger = new ServerLogger(this.getClass().getName(), true);

    @GetMapping(path="/hello")
    public @ResponseBody
    String checkConnection () {

        logger.log("info", "Check connection request");
        return "Hello";
    }
}