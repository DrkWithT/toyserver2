package com.drkwitht.http;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class HttpReader {
    public final String[] methodStrings = {"HEAD", "GET", "POST", "UNKNOWN"};
    public final String[] mimeStrings = {"PLAIN_TXT", "HTML_TXT", "CSS_TXT", "APPLICATION_JSON", "UNKNOWN"};

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
    private boolean expectBody;
    private int expectedBodyLength;
    private HttpMimeType expectedContentType;

    private HashMap<String, HttpMethod> methodMap;
    private HashMap<String, HttpMimeType> mimeMap;
    private BufferedReader reader;

    private HttpHeading tempHeading;
    private ArrayList<HttpHeader> tempHeaders;
    private HttpContent tempContent;

    public HttpReader(BufferedReader rawReader) {
        state = ReaderState.READ_IDLE;
        expectBody = false;
        expectedBodyLength = 0;
        expectedContentType = HttpMimeType.UNKNOWN;

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
        expectedContentType = HttpMimeType.UNKNOWN;
        
        reader.reset();
        tempHeading = null;
        tempHeaders.clear();
        tempContent = null;
    }

    private ReaderState readHeading(String line) {
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
        if (line.isEmpty()) {
            return ReaderState.READ_BODY;
        }

        String[] lineHalves = line.split(":");

        if (lineHalves.length != 2) {
            return ReaderState.READ_ERROR;
        }

        String tempIdentifier = lineHalves[0].trim().toLowerCase();
        String tempValue = lineHalves[1].trim();
        int tempNumericValue = Integer.parseInt(tempValue);

        // NOTE: check for content length and type headers
        if (tempIdentifier.equalsIgnoreCase("content-length") && tempNumericValue > 0) {
            expectBody = true;
            expectedBodyLength = tempNumericValue;
        } else if (tempIdentifier.equalsIgnoreCase("content-type")) {
            expectBody = true;
            expectedContentType = mimeMap.get(tempValue);
        }

        tempHeaders.add(new HttpHeader(tempIdentifier, tempValue));

        return ReaderState.READ_HEADER;
    }

    private ReaderState readBody() throws IOException {
        if (!expectBody || expectedBodyLength < 1) {
            return ReaderState.READ_ERROR;
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
            tempLine = reader.readLine();

            switch (state) {
                case READ_IDLE:
                    state = ReaderState.READ_HEADING;
                    break;
                case READ_HEADING:
                    state = readHeading(tempLine);
                    break;
                case READ_HEADER:
                    state = readHeader(tempLine);
                    break;
                case READ_BODY:
                    state = readBody();
                    break;
                case READ_ERROR:
                default:
                    throw new IOException("Invalid HTTP message syntax.");
            }
        }

        return new HttpRequest(tempHeading, tempHeaders, tempContent);
    }
}
