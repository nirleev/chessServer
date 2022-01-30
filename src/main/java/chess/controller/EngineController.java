package chess.controller;

import chess.engine.EngineHandler;
import chess.model.EngineModel;
import chess.model.MessageModel;
import chess.server.ServerLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

/**
 * Handles all engine related routes
 */
@SuppressWarnings("unused")
@RestController
@RequestMapping("/engine")
public class EngineController {

    @Autowired
    private EngineHandler engineHandler;

    private final ServerLogger logger = new ServerLogger(this.getClass().getName(), true);

    /**
     * Starts engine with name
     * @param engine StartEngineModel with name of the engine that user wants to start.
     * @return information whether engine was started.
     */
    @PostMapping(value = "/start", consumes=MediaType.APPLICATION_JSON_VALUE,
            produces=MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody
    String start(@RequestBody EngineModel engine) {

        logger.log("info", "Start engine request");
        boolean info = engineHandler.startEngine(engine);

        if(info){
            logger.log("info", String.format("Engine %s started", engine.getName()));
            return String.format("Engine %s started", engine.getName());
        } else {
            logger.log("info", String.format("Can't start engine %s", engine.getName()));
            return String.format("Can't start engine %s", engine.getName());
        }
    }

    /**
     * This method stops currently running engine.
     * @return whether the engine was stopped.
     */
    @GetMapping(value = "/stop")
    public @ResponseBody String stop() {

        logger.log("info", "Stop engine request");
        engineHandler.stopEngine();
        logger.log("info", "Engine stopped");
        return "Engine stopped";
    }

    /**
     * This method send command to currently running engine.
     * @return whether the engine was stopped.
     */
    @PostMapping(value = "/send", consumes=MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody String send(@RequestBody MessageModel command) {

        if(engineHandler.isEngineRunning()){
            engineHandler.processRawCommand(command.getMsg());
            return "Command sent";
        } else {
            logger.log("error", "Engine is not running - command ignored");
            return "Engine is not running";
        }
    }
}


