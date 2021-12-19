package chess.controller;

import chess.model.MessageModel;
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

    @PostMapping(path="/command", consumes=MediaType.APPLICATION_JSON_VALUE,
    produces=MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody
    String receiveMessage (
            @RequestBody MessageModel command) {

        // implement support for various commands depending on the needs

        return command.getMsg()+" received";
    }

    @GetMapping(path="/hello")
    public @ResponseBody
    String checkConnection () {

        return "Hello";
    }
}
