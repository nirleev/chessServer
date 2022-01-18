package chess.ws;


import chess.engine.EngineHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.LinkedList;
import java.util.Observable;
import java.util.Observer;
import java.util.Queue;

/**
 * Handles communication with client via web socket.
 */
public class WebSocketHandler extends TextWebSocketHandler implements Observer {

    @Autowired
    private EngineHandler engineHandler;

    /**
     * Current session
     */
    private WebSocketSession webSocketSession;

    private CloseEngineDelayedThread closeEngine;

    private Queue<String> messagesToSend = new LinkedList<>();

    /**
     * This method is called every time client sends message to server. If the message was sent by currently logged user
     * then it will be passed to engine handler.
     *
     * @param session client session
     * @param message message sent from client
     * @throws Exception
     */
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        if (session.getId().equals(webSocketSession.getId())) {
            synchronized (webSocketSession) {
                engineHandler.processRawCommand(message.getPayload());
            }
        }
    }

    /**
     * This method is called after connection with client was established. If this handler has another session open it
     * will be closed immediately.
     *
     * @param session client session
     * @throws Exception
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        if (webSocketSession != null && !session.equals(webSocketSession)) {
            webSocketSession.close();
        }
        engineHandler.observable.addObserver(this);
        this.webSocketSession = session;
        if (closeEngine != null) {
            closeEngine.interrupt();
        }
        if (messagesToSend.size() > 0) {
            String message;
            while ((message = messagesToSend.poll()) != null) {
                sendMessage(message);
            }
        }
    }

    /**
     * Closes active engine when connection is lost
     *
     * @param session
     * @param status
     * @throws Exception
     */
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        super.afterConnectionClosed(session, status);
        if (closeEngine != null) {
            closeEngine.interrupt();
        }
        closeEngine = new CloseEngineDelayedThread();
        closeEngine.start();
    }

    /**
     * This method sends message to client if {@link #webSocketSession} is not null. If {@code message} is null it will
     * be replaced with an empty string.
     *
     * @param message message to send
     */
    private void sendMessage(String message) {
        if (message == null || message.equals("")) {
            message = " ";
        }
        if (webSocketSession == null) {
            System.err.print("No session to handle message: " + message);
            return;
        }

        try {
            if (webSocketSession.isOpen()) {
                webSocketSession.sendMessage(new TextMessage(message));
            } else {
                messagesToSend.add(message);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * This method is called by {@link chess.engine.EngineHandler.EngineObservable EngineObservable} every time
     * chess engine outputs new line. Received line will be send to client via {@link #sendMessage(String)}.
     *
     * @param o   the observable object.
     * @param arg argument passed to {@link Observable#notifyObservers(Object)} method.
     */
    @Override
    public void update(Observable o, Object arg) {
        if (o instanceof EngineHandler.EngineObservable && arg instanceof String) {
            String message = (String) arg;
            sendMessage(message);
        }
    }

    private class CloseEngineDelayedThread extends Thread {

        static final long DELAY = 5 * 60 * 1000;

        @Override
        public void run() {
            try {
                Thread.sleep(DELAY);
            } catch (InterruptedException e) {
                return;
            }

            //engineHandler.stopEngine();
        }
    }
}

