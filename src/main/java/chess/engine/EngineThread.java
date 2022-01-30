package chess.engine;

import chess.server.ServerLogger;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * This class handles running engine process. It also allows for receiving and sending messages from and to
 * chess engine.
 */
class EngineThread extends Thread {
    private static final long DEFAULT_UPDATE_DELAY = 20;

    private String enginePath = "engines/stockfish";

    /**
     * Engine process.
     */
    private Process process;

    /**
     * reads values from engine output.
     */
    private BufferedReader reader;

    /**
     * writes values to engine process input.
     */
    private BufferedWriter writer;

    /**
     * for storing values which will be passed to engine
     */
    private ConcurrentLinkedQueue<String> output;

    /**
     * listeners which will be notified when engine outputs new line.
     */
    private List<EngineOutputListener> listeners = new ArrayList<>();

    private final ServerLogger logger = new ServerLogger(this.getClass().getName(), true);

    /**
     * whether this thread should stop
     */
    private boolean shouldStop = false;

    /**
     * is this thread running
     */
    private boolean running = false;

    private String info = null;

    /**
     * time in ms between two engine read/write operations
     */
    private long updateDelay = DEFAULT_UPDATE_DELAY;

    public EngineThread() {
        this.output = new ConcurrentLinkedQueue<>();
    }

    public EngineThread(String enginePath) {
        this();
        this.enginePath = "engines/" + enginePath;
    }

    /**
     * This method will be called after {@link Thread#start()} call. This method starts new chess engine process
     * using path from {@link #enginePath} field. Then it reads/writes from/to engine standard i/o using {@link #reader}
     * /{@link #writer} in loop. After each loop cycle thread sleeps for {@link #updateDelay} ms.
     * <p>
     * Write to engine input is only performed when {@link #output} contains any message.
     */
    @Override
    public void run() {
        try {
            process = new ProcessBuilder(enginePath).start();

            running = true;
            info = "started";
            logger.log("status", "Engine started");

            InputStream in = process.getInputStream();
            InputStreamReader inr = new InputStreamReader(in);
            reader = new BufferedReader(inr);

            OutputStream out = process.getOutputStream();
            OutputStreamWriter outw = new OutputStreamWriter(out);
            writer = new BufferedWriter(outw);

            while (!shouldStop) {
                try {
                    Thread.sleep(updateDelay);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                String line;
                if (reader.ready()) {
                    if ((line = reader.readLine()) != null) {
                        logger.log("in", line);
                        if (listeners != null) {
                            for (EngineOutputListener l :
                                    listeners) {
                                l.onEngineOutput(line);
                            }
                        }
                    }
                }

                if (output.size() > 0 && (line = output.remove()) != null) {
                    logger.log("out", line);
                    writer.append(line);
                    writer.flush();
                }
            }
            writer.close();
            reader.close();
            process.destroy();
        } catch (IOException e) {
            running = false;
            info = "not found";
            logger.log("status", "Engine " + info);
            return;
        }

        running = false;
        info = "stopped";
        logger.log("status", "Engine " + info);
    }

    /**
     * This method passes {@code command} argument to engine standard input.
     *
     * @param command command to pass to engine.
     */
    public void processRawCommand(String command) {
        output.add(String.format("%s\n", command));
    }

    /**
     * Sets {@link #shouldStop} value. If {@code shouldStop} is true, then engine process would be stopped as soon as
     * possible.
     *
     * @param shouldStop whether the engine should stop.
     */
    public void setShouldStop(boolean shouldStop) {
        this.shouldStop = shouldStop;
    }

    /**
     * @return {@code true} if engine is running, {@code false} otherwise.
     */
    public boolean isRunning() {
        return running;
    }

    public String getInfo(){
        return info;
    }

    /**
     * Adds listener which will be notified after every engine output.
     *
     * @param listener
     */
    public void addListeners(EngineOutputListener listener) {
        this.listeners.add(listener);
    }

    public interface EngineOutputListener {

        /**
         * Will be called every time engine outputs new line.
         *
         * @param output engine output line.
         */
        void onEngineOutput(String output);
    }

    @Override
    protected void finalize() throws Throwable {
        reader.close();
        writer.close();
        if (process.isAlive()) {
            process.destroy();
        }
        super.finalize();
    }
}