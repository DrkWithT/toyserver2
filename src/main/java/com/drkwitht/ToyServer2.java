package com.drkwitht;

import javax.net.ssl.SSLSocket;

import java.io.FileInputStream;
import java.security.KeyStore;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;

/**
 * ToyServer.java
 * @author Derek Tan (DrkWithT)
 * @summary This file contains driver code for my toy HTTP/1.1 server. 
 * @version 0.1.0
 */
public final class ToyServer2 {
    public static String SERVER_NAME = "ToyServer2";
    public static int SERVER_VERSION = 0x0001;

    private static final int DEFAULT_PORT = 5000;
    private static final String PKCS_STRING = "PKCS12";
    private static final String PKCS_FILE_PATH = "secrets/localhost.p12"; // NOTE: change as you wish
    private static final String PKCS_FILE_PWORD = "Derk23!";  // NOTE: change as you wish

    private boolean setupOK;
    private boolean isListening;
    private String hostName;
    private int portNumber;
    private int backlog; 
    private SSLServerSocket gatewayServerSocket;

    public ToyServer2(String host, int port, int backlog) {
        this.isListening = false;
        this.hostName = "localhost";
        this.portNumber = DEFAULT_PORT;
        this.backlog = backlog;
        this.gatewayServerSocket = null;

        this.setupOK = setupCredentials(PKCS_FILE_PATH);
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
            this.gatewayServerSocket = (SSLServerSocket) socketFactory.createServerSocket(this.portNumber, this.backlog);
        } catch (Exception e) {
            System.err.println("Setup Error: " + e);
            statusOK = false;
        }

        return statusOK;
    }

    public boolean startService() {
        if (!this.setupOK)
            return false;
        
        // Thread gatewayThread = new Thread(new GatewayWorker());
        // gatewayThread.start(); // todo: implement!!

        return true;
    }

    public void closeService() {
        this.isListening = false;
        // todo: add calls to ServerWorker.stop() for each cached ServerWorker object?
    }

    public static void main( String[] args ) {
        System.out.println( "Hello World!" );
    }
}
