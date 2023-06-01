package com.drkwitht.types;

/**
 * ServerWorker.java
 * @author Derek Tan (DrkWithT at GitHub)
 * TODO: 1. create DocResource and ResourceCache classes.
 * TODO: 2. create RequestHandler class and a calling method in ServerWorker.
 */

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.net.ssl.SSLSocket;

import com.drkwitht.http.HttpReader;
import com.drkwitht.http.HttpRequest;

public class ServerWorker implements Runnable {
    private WorkerState state;
    private WorkerError error;

    private String serverString;
    private int serverMajorVersion;
    private int serverMinorVersion;

    private SSLSocket workerSocket;
    private DataInputStream fromClient;
    private DataOutputStream toClient;

    private HttpReader messageReader;
    private String requestURL;
    
    public ServerWorker(SSLSocket socket, String serverName, int versionCode) throws IOException {
        state = WorkerState.IDLE;
        error = WorkerError.NONE;
        
        serverString = serverName;
        serverMajorVersion = (versionCode & 0xff00) >> 8;
        serverMinorVersion = (versionCode & 0x00ff);
        
        workerSocket = socket;
        fromClient = new DataInputStream(socket.getInputStream());
        toClient = new DataOutputStream(socket.getOutputStream());

        messageReader = null;
        requestURL = null;
    }

    @Override
    public void run() {
        messageReader = new HttpReader(new BufferedReader(new InputStreamReader(fromClient)));

        HttpRequest tempRequest = null;

        while (state != WorkerState.STOP) {
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
                        break;
                    case ERROR_REQUEST:
                        break;
                    case ERROR_NOT_FOUND:
                        break;
                    case ERROR_GENERIC:
                        break;
                    case ERROR_UNSUPPORTED:
                        break;
                    case RESPOND:
                        // TODO: 3a. create stringifiable HttpResponse before RESPOND state!
                        break;
                    case STOP:
                    default:
                        break;
                }
            } catch (Exception generalErr) {
                // TODO: 4. a default handler for 500 responses goes here for now!
            }
        }
    }
}
