package chess.engine;

import chess.model.EngineModel;
import org.springframework.stereotype.Component;

import java.util.Observable;

/**
 * Provides high level interface for managing chess engine.
 */
@Component
public class EngineHandler {

    private EngineThread engineThread = null;

    public final EngineObservable observable = new EngineObservable();

    public EngineHandler() {
    }

    /**
     * Starts new engine thread. If there is any running engine then it will be stopped.
     * @param engine engine to start.
     */
    public boolean startEngine(EngineModel engine) {
        stopEngine();
        engineThread = new EngineThread(engine.getPath());
        engineThread.addListeners(observable);
        engineThread.setDebug(true);
        engineThread.start();

        return waitForEngine();
    }

    /**
     * Stops currently running engine thread.
     */
    public void stopEngine() {
        if (!isEngineRunning()) return;

        engineThread.setShouldStop(true);
        try {
            engineThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        engineThread = null;
    }

    public boolean isEngineRunning() {
        return engineThread != null && engineThread.isRunning();
    }

    public boolean waitForEngine(){
        if(engineThread != null){
            debugLog("info", "waiting for engine status info");
            while(engineThread.getInfo() == null || engineThread.getInfo().equals("stopped")) {}
            debugLog("info", "sending engine status info");
            return engineThread.isRunning();
        }
        return false;
    }

    /**
     * Pass {@code command} to engine.
     * @param command command to pass.
     */
    public void processRawCommand(String command) {
        if (engineThread == null) {
            System.err.println("Cant process command, engine is null");
            return;
        }

        engineThread.processRawCommand(command);
    }

    /**
     * Observable for handling {@link chess.engine.EngineThread.EngineOutputListener EngineOutputListener} notifications
     */
    public class EngineObservable extends Observable implements EngineThread.EngineOutputListener {

        /**
         * Called every time there is some output form engine.
         * This method will notify all observers.
         * @param output engine output line.
         */
        @Override
        public void onEngineOutput(String output) {
            setChanged();
            notifyObservers(output);
        }
    }

    private void debugLog(String tag, String msg) {
        System.out.println(String.format("[EngineHandler] %s : %s", tag, msg));
    }
}

