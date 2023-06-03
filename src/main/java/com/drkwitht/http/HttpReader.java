package com.drkwitht.http;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * HttpReader.java
 * @author Derek Tan
 * TODO: fix bad read of heading and headers in run()'s helpers!
 */

public class HttpReader {
    public final String[] methodStrings = {"HEAD", "GET", "POST", "UNKNOWN"};
    public final String[] mimeStrings = {"PLAIN_TXT", "HTML_TXT", "CSS_TXT", "APPLICATION_JSON", "ANY"};

    private enum ReaderState {
        READ_IDLE,
        READ_HEADING,
        READ_HEADER,
        // READ_CHUNK_LENGTH,
        READ_BODY,
        READ_END,
        READ_ERROR
    }

    private ReaderState state;
    private String hostIdent;
    private boolean hostOK;
    private boolean expectBody;
    private int expectedBodyLength;
    private HttpMimeType expectedContentType;

    private HashMap<String, HttpMethod> methodMap;
    private HashMap<String, HttpMimeType> mimeMap;
    private BufferedReader reader;

    private HttpHeading tempHeading;
    private ArrayList<HttpHeader> tempHeaders;
    private HttpContent tempContent;

    public HttpReader(BufferedReader rawReader, String hostString) {
        state = ReaderState.READ_IDLE;
        hostIdent = hostString;
        hostOK = true;
        expectBody = false;
        expectedBodyLength = 0;
        expectedContentType = HttpMimeType.ANY;

        methodMap = new HashMap<>();
        for (String string : methodStrings) {
            methodMap.put(string, HttpMethod.valueOf(string));
        }

        mimeMap = new HashMap<>();
        for (String string : mimeStrings) {
            mimeMap.put(string, HttpMimeType.valueOf(string));
        }

        reader = rawReader;
        tempHeaders = new ArrayList<HttpHeader>();
    }

    public void reset() throws IOException {
        state = ReaderState.READ_IDLE;
        expectBody = false;
        expectedBodyLength = 0;
        expectedContentType = HttpMimeType.ANY;
        tempHeading = null;
        tempHeaders.clear();
        tempContent = null;
    }

    public boolean isHostValid() {
        return hostOK;
    }

    private ReaderState readHeading(String line) {
        if (line == null) {
            return ReaderState.READ_ERROR;
        }

        String[] tokens = line.split(" ");

        if (tokens.length != 3) {
            return ReaderState.READ_ERROR;
        }

        HttpMethod tempMethod = methodMap.get(tokens[0]);
        String tempPath = tokens[1];
        String tempScheme = tokens[2];

        tempHeading = new HttpHeading(tempMethod, tempPath, tempScheme);

        if (tempMethod == null || tempPath.isEmpty() || tempScheme.isEmpty()) {
            return ReaderState.READ_ERROR;
        }

        return ReaderState.READ_HEADER;
    }

    private ReaderState readHeader(String line) throws NumberFormatException {
        if (line == null) {
            return ReaderState.READ_ERROR;
        }

        if (line.isEmpty()) {
            return ReaderState.READ_BODY;
        }

        int colonIndex = line.indexOf(":");
        String headerName = null;
        String headerValue = null;

        if (colonIndex >= 1) {
            headerName = line.substring(0, colonIndex).trim().toLowerCase();
            headerValue = line.substring(colonIndex + 1).trim();
        }

        if (headerName == null || headerValue == null) {
            return ReaderState.READ_ERROR;
        }

        int tempNumericValue = -1;

        // NOTE: check for content length and type headers
        if (headerName.equalsIgnoreCase("content-length") && tempNumericValue < 0) {
            expectBody = true;
            tempNumericValue = Integer.parseInt(headerValue);

            expectedBodyLength = tempNumericValue;
        } else if (headerName.equalsIgnoreCase("content-type")) {
            expectBody = true;
            expectedContentType = mimeMap.get(headerValue);
        } else if (headerName.equalsIgnoreCase("host")) {
            hostOK = (headerValue.equalsIgnoreCase(hostIdent));
        }

        tempHeaders.add(new HttpHeader(headerName, headerValue));

        return ReaderState.READ_HEADER;
    }

    private ReaderState readBody() throws IOException {
        // NOTE: Stop reader early when no body clues were found: no content-length, content-type... (Add transfer-encoding support later!) 
        if (!expectBody || expectedBodyLength < 1) {
            return ReaderState.READ_END;
        }

        byte[] rawBodyBuffer = new byte[expectedBodyLength];

        for (int i = 0; i < rawBodyBuffer.length; i++) {
            int byteVal = reader.read();

            // NOTE: set error on premature body ending versus its content-length, as I want to ensure correct message reads. 
            if (byteVal == -1) {
                return ReaderState.READ_ERROR;
            }

            rawBodyBuffer[i] = ((byte)byteVal);
        }

        tempContent = new HttpContent(expectedContentType, expectedBodyLength, rawBodyBuffer);

        return ReaderState.READ_END;
    }

    public HttpRequest run() throws IOException {
        String tempLine = null;

        while (state != ReaderState.READ_END) {
            System.out.println("HttpReader.state = " + state);  // DEBUG!

            switch (state) {
                case READ_IDLE:
                    state = ReaderState.READ_HEADING;
                    break;
                case READ_HEADING:
                    tempLine = reader.readLine();
                    state = readHeading(tempLine);
                    break;
                case READ_HEADER:
                    tempLine = reader.readLine();
                    state = readHeader(tempLine);
                    break;
                case READ_BODY:
                    state = readBody();
                    break;
                case READ_END:
                    break;
                case READ_ERROR:
                default:
                    throw new IOException("Invalid HTTP message syntax.");
            }
        }

        return new HttpRequest(tempHeading, tempHeaders, tempContent);
    }
}
