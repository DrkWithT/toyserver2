package com.drkwitht.types;

/**
 * ServerWorker.java
 * @author Derek Tan (DrkWithT at GitHub)
 * @apiNote This may be moved into the main server class to easily use a synchronized ResourceCache to save memory.
 */

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.drkwitht.handlers.HandlerStorage;
import com.drkwitht.handlers.IRequestHandler;
import com.drkwitht.handlers.MyHttpResponse;
import com.drkwitht.handlers.ResourceCache;
import com.drkwitht.http.HttpReader;
import com.drkwitht.http.HttpRequest;
import com.drkwitht.http.HttpStatus;

public class ServerWorker implements Runnable {
    public static final int MAX_STRIKES = 2;

    private WorkerState state;
    private int strikes;

    private String hostString;

    private Socket workerSocket;
    private DataInputStream fromClient;
    private DataOutputStream toClient;

    private HttpReader messageReader;
    private MyHttpResponse messageSender;
    private ResourceCache resCacheReference;  // NOTE: stores a reference value of main class's ResourceCache!
    private HandlerStorage handlerTableRef;   // NOTE: stores a reference to server handler collection!
    
    private Logger debugLogger;

    public ServerWorker(Socket socket, String hostIdentifier, ResourceCache cacheReference, HandlerStorage handlers) throws IOException {
        state = WorkerState.IDLE;
        strikes = 0;
        
        hostString = hostIdentifier;
        
        workerSocket = socket;
        fromClient = new DataInputStream(socket.getInputStream());
        toClient = new DataOutputStream(socket.getOutputStream());

        messageReader = new HttpReader(new BufferedReader(new InputStreamReader(fromClient)), hostString);
        messageSender = new MyHttpResponse(toClient);
        resCacheReference = cacheReference;
        handlerTableRef = handlers;

        debugLogger = Logger.getLogger("ServerWorker");
        debugLogger.setLevel(Level.INFO);
        debugLogger.info("Created instance.");
    }

    private WorkerState stateRespond(HttpRequest request, HttpStatus optional) throws IOException {
        IRequestHandler currentHandler = null;
        
        if (request != null)
            currentHandler = handlerTableRef.getHandler(request.getPath());

        debugLogger.info("request.path = " + request.getPath());  // DEBUG
        
        currentHandler.handle(resCacheReference, request, messageSender, optional);

        return WorkerState.START;
    }

    @Override
    public void run() {
        HttpRequest tempRequest = null;

        while (state != WorkerState.STOP) {
            if (strikes > MAX_STRIKES) {
                state = WorkerState.STOP;
            }

            debugLogger.info("ServerWorker.state = " + state);  // DEBUG!

            try {
                switch (state) {
                    case IDLE:
                        state = WorkerState.START;
                        break;
                    case START:
                        state = WorkerState.REQUEST;
                        break;
                    case REQUEST:
                        tempRequest = messageReader.run();
                        messageReader.reset();
                        if (!messageReader.isHostValid()) {
                            state = WorkerState.ERROR_REQUEST;  // check host header or reject request!
                        } else {
                            state = WorkerState.RESPOND;
                        }
                        break;
                    case ERROR_REQUEST:
                        state = stateRespond(tempRequest, HttpStatus.BAD_REQUEST);
                        break;
                    case ERROR_NOT_FOUND:
                        state = stateRespond(tempRequest, HttpStatus.NOT_FOUND);
                        break;
                    case ERROR_GENERIC:
                        state = stateRespond(tempRequest, HttpStatus.SERVER_ERROR);
                        break;
                    case ERROR_UNSUPPORTED:
                        state = stateRespond(tempRequest, HttpStatus.NOT_IMPLEMENTED);
                        break;
                    case RESPOND:
                        state = stateRespond(tempRequest, HttpStatus.NOT_FOUND);
                        break;
                    case STOP:
                        break;
                    default:
                        throw new Exception("Invalid worker state.");
                }
            } catch (IOException ioError) {
                debugLogger.warning(ioError.toString());
                strikes++;
                state = WorkerState.ERROR_REQUEST;
            } catch (Exception generalError) {
                debugLogger.warning(generalError.toString());
                strikes++;
                state = WorkerState.ERROR_GENERIC;
            }
        }

        try {
            workerSocket.close();
        } catch (IOException ioError) {
            debugLogger.warning(ioError.getMessage());
        }
    }
}
