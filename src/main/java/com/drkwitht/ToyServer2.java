package com.drkwitht;

import com.drkwitht.handlers.HandlerStorage;
import com.drkwitht.handlers.IRequestHandler;

/**
 * ToyServer2.java
 * @author Derek Tan (DrkWithT)
 * @summary This file contains driver code for my toy HTTP/1.1 server.
 * @version 0.1.0
 * TODO: possibly put flush calls for response streams in bound req handlers? 
 */

import com.drkwitht.handlers.ResourceCache;
import com.drkwitht.http.HttpHeader;
import com.drkwitht.http.HttpMethod;
import com.drkwitht.http.HttpStatus;
import com.drkwitht.types.ServerWorker;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

public final class ToyServer2 {
    public static String SERVER_NAME = "ToyServer2/0.1";

    private static final int DEFAULT_PORT = 8000;
    private static final String DEFAULT_CONTENT_ROOT = "public";

    private boolean setupOK;
    private boolean isListening;
    private String hostName;
    private int portNumber;
    private int sockBacklog;
    private ServerSocket gatewayServerSocket;

    private ResourceCache materialCache;
    private HandlerStorage handlerTable;

    private final class GatewayWorker implements Runnable {
        
        public GatewayWorker() {}

        @Override
        public void run() {
            while (isListening) {
                try {
                    Socket clientSocket = gatewayServerSocket.accept();
 
                    new Thread(new ServerWorker(
                        clientSocket,
                        hostName + ":" + portNumber,
                        materialCache,
                        handlerTable
                    )).start();

                } catch (IOException ioError) {
                    System.err.println("Connect error (closing): " + ioError);
                    isListening = false;
                }
            }

            try {
                gatewayServerSocket.close();
            } catch (IOException ioError) {
                System.err.println("Socket close error: " + ioError);
            }
        }
    }

    public ToyServer2(String host, int port, int backlog, String folderPath) {
        isListening = false;
        portNumber = port;
        hostName = host;
        sockBacklog = backlog;
        gatewayServerSocket = null;
        materialCache = null;
        handlerTable = new HandlerStorage();

        setupOK = setupCache(DEFAULT_CONTENT_ROOT) && setupSocket(portNumber, sockBacklog);
    }

    private boolean setupCache(String rootPath) {
        boolean statusOK = true;

        try {
            materialCache = new ResourceCache(rootPath);
        } catch (Exception genericError) {
            statusOK = false;
            System.err.println("Cache setup error:" + genericError);
        }

        return statusOK;
    }

    private boolean setupSocket(int port, int backlog) {
        boolean statusOK = true;

        try {
            gatewayServerSocket = new ServerSocket(port, backlog);
        } catch (Exception genericError) {
            statusOK = false;
            System.err.println("Socket setup error: " + genericError);            
        }

        return statusOK;
    }

    public void addHandler(String[] paths, IRequestHandler handler) {
        handlerTable.registerHandler(paths, handler);
    }

    public void addFallbackHandler(IRequestHandler fallback) {
        handlerTable.setFallbackHandler(fallback);
    }

    public void startService() {
        if (!setupOK || isListening)
            return;
        
        isListening = true;
        
        new Thread(new GatewayWorker()).start();
    }

    public void closeService() {
        isListening = false;
        // todo: add calls to ServerWorker.stop() for each cached ServerWorker object only if they are saved by reference.
    }

    public static void main( String[] args ) {
        /// Initialize server driver.
        ToyServer2 appServer = new ToyServer2("localhost", DEFAULT_PORT, 10, DEFAULT_CONTENT_ROOT);

        String[] homePaths = {"/", "/index.html"};

        /// Add handlers!
        appServer.addHandler(homePaths, (ctx, req, res, errstatus) -> {
            DateTimeFormatter dtFormat = DateTimeFormatter.RFC_1123_DATE_TIME;
            String dateString = Instant.now().atZone(ZoneOffset.UTC).format(dtFormat);

            if (req.getMethod() == HttpMethod.GET) {
                res.writeHeading(HttpStatus.OK);

                res.writeHeader(new HttpHeader("Connection", "Keep-Alive"));
                res.writeHeader(new HttpHeader("Date", dateString));
                res.writeHeader(new HttpHeader("Server", SERVER_NAME));

                res.writeBody(ctx.getResource("/index.html").toWebContent());
            } else {
                res.writeHeading(HttpStatus.NOT_IMPLEMENTED);

                res.writeHeader(new HttpHeader("Connection", "Keep-Alive"));
                res.writeHeader(new HttpHeader("Date", dateString));
                res.writeHeader(new HttpHeader("Server", SERVER_NAME));

                res.writeBody(null);
            }
        });

        appServer.addFallbackHandler((ctx, req, res, errstatus) -> {
            DateTimeFormatter dtFormat = DateTimeFormatter.RFC_1123_DATE_TIME;
            String dateString = Instant.now().atZone(ZoneOffset.UTC).format(dtFormat);

            res.writeHeading(errstatus);
            
            res.writeHeader(new HttpHeader("Connection", "Keep-Alive"));
            res.writeHeader(new HttpHeader("Date", dateString));
            res.writeHeader(new HttpHeader("Server", SERVER_NAME));
            
            res.writeBody(null);
        });

        /// Run and hope for the best!
        appServer.startService();
    }
}
