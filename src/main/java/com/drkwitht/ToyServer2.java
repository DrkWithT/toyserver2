package com.drkwitht;

import com.drkwitht.handlers.HandlerStorage;
import com.drkwitht.handlers.IRequestHandler;

/**
 * ToyServer2.java
 * @author Derek Tan (DrkWithT)
 * @summary This file contains driver code for my toy HTTP/1.1 server.
 * @version 0.1.0
 * TODO: possibly put flush calls for response stream? 
 */

import com.drkwitht.handlers.ResourceCache;
import com.drkwitht.http.HttpHeader;
import com.drkwitht.http.HttpMethod;
import com.drkwitht.http.HttpStatus;
import com.drkwitht.types.ServerWorker;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.Socket;
import java.security.KeyStore;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.logging.Logger;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;

public final class ToyServer2 {
    public static String SERVER_NAME = "ToyServer2/0.1";

    private static final int DEFAULT_PORT = 5000;
    private static final String DEFAULT_CONTENT_ROOT = "./public";
    private static final String PKCS_STRING = "PKCS12";
    private static final String PKCS_FILE_PATH = "secrets/localhost.p12"; // NOTE: change as you wish
    private static final String PKCS_FILE_PWORD = "Derk23!";  // NOTE: change as you wish

    private boolean setupOK;
    private boolean isListening;
    private String hostName;
    private int portNumber;
    private int sockBacklog; 
    private SSLServerSocket gatewayServerSocket;

    private ResourceCache materialCache;
    private HandlerStorage handlerTable;
    private Logger mainLogger;

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
                System.err.println(ioError);
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

        setupOK = setupCredentials(PKCS_FILE_PATH) && setupCache(DEFAULT_CONTENT_ROOT);
        mainLogger = Logger.getLogger("ToyServer2");
    }

    private boolean setupCredentials(String pkcsFilePath) {
        boolean statusOK = true;

        try {
            SSLContext serverSSLCtx = SSLContext.getInstance("TLSv1.3");
            KeyManagerFactory keyFactory = KeyManagerFactory.getInstance("SunX509");
            
            // load pkcs12 file contents for (self-signed) certificate and keys... this must also be trusted manually in the browser!
            KeyStore keyWrapper = KeyStore.getInstance(PKCS_STRING);
            keyWrapper.load(new FileInputStream(pkcsFilePath), PKCS_FILE_PWORD.toCharArray());

            // generate key list for key manager factory...
            keyFactory.init(keyWrapper, PKCS_FILE_PWORD.toCharArray());

            // initialize SSLContext for SSL server socket factory...
            serverSSLCtx.init(keyFactory.getKeyManagers(), null, null);
            
            // initialize SSL socket for server usage... 
            SSLServerSocketFactory socketFactory = serverSSLCtx.getServerSocketFactory();
            gatewayServerSocket = (SSLServerSocket) socketFactory.createServerSocket(portNumber, sockBacklog);
        } catch (Exception genericErr) {
            statusOK = false;
            mainLogger.warning("SSL socket error: " + genericErr);
        }

        return statusOK;
    }

    private boolean setupCache(String rootPath) {
        boolean statusOK = true;

        try {
            materialCache = new ResourceCache(rootPath);
        } catch (Exception genericErr) {
            statusOK = false;
            mainLogger.warning("Cache setup error:" + genericErr);
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
                // res.flushData();
            } else {
                res.writeHeading(HttpStatus.NO_CONTENT);

                res.writeHeader(new HttpHeader("Connection", "Keep-Alive"));
                res.writeHeader(new HttpHeader("Date", dateString));
                res.writeHeader(new HttpHeader("Server", SERVER_NAME));

                res.writeBody(null);
                // res.flushData();
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
            // res.flushData();
        });

        /// Run and hope for the best!
        appServer.startService();
    }
}
