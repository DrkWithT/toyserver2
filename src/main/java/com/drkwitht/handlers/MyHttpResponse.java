package com.drkwitht.handlers;

import java.io.DataOutputStream;
import java.io.IOException;

import com.drkwitht.http.HttpContent;
import com.drkwitht.http.HttpHeader;
import com.drkwitht.http.HttpStatus;

/**
 * @apiNote Abstracts a writable stream back to the client. It is like a PrintWriter for HTTP/1.1 responses.
 */
public class MyHttpResponse {
    public static final String RES_SCHEMA = "HTTP/1.1";
    public static final String[] STATUS_STRINGS = {"200", "204", "400", "404", "500", "501"};
    public static final String[] STATUS_MSGS = {"OK", "No Content", "Bad Request", "Not Found", "Server Error", "Not Implemented"};
    public static final String HTTP_1_1_DELIM = "\r\n";

    private DataOutputStream toClientStreamRef;  // NOTE: only 1 DataOutputStream will be passed by reference value here from a ServerWorker. No need for sync since each worker is sequential in handler dispatch.
    
    public MyHttpResponse(DataOutputStream toClientStream) {
        toClientStreamRef = toClientStream;
    }

    public void writeHeading(HttpStatus status) throws IOException {
        int lookupIndex = status.ordinal();

        toClientStreamRef.writeBytes(RES_SCHEMA);
        toClientStreamRef.writeBytes(" ");
        toClientStreamRef.writeBytes(STATUS_STRINGS[lookupIndex]);
        toClientStreamRef.writeBytes(" ");
        toClientStreamRef.writeBytes(STATUS_MSGS[lookupIndex]);
        toClientStreamRef.writeBytes(HTTP_1_1_DELIM);
    }

    public void writeHeader(HttpHeader header) throws IOException {
        toClientStreamRef.writeBytes(header.getName());
        toClientStreamRef.writeBytes(": ");
        toClientStreamRef.writeBytes(header.getLiteralValue());
        toClientStreamRef.writeBytes(HTTP_1_1_DELIM);
    }

    public void writeBody(HttpContent bodyContent) throws IOException {
        toClientStreamRef.writeBytes(HTTP_1_1_DELIM);

        if (bodyContent != null)
            toClientStreamRef.write(bodyContent.asBytes());
    }

    public void flushData() throws IOException {
        toClientStreamRef.flush();
    }
}
