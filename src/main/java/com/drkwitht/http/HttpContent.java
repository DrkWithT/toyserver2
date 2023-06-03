package com.drkwitht.http;

import java.util.Arrays;

/**
 * HttpContent.java
 * @author Derek Tan (DrkWithT at GitHub)
 */

/**
 * @apiNote Encapsulates an HTTP/1.1 message's body data and its important properties such as MIME type and length.
 */
public class HttpContent {
    public static String[] MIME_NAMES = {"text/plain", "text/html", "text/css", "application/json", "*/*"};
    private HttpMimeType type;
    private int contentLength;
    private byte[] content;

    public HttpContent(HttpMimeType contentType, int byteCount, byte[] data) {
        type = contentType;
        contentLength = byteCount;
        content = Arrays.copyOf(data, contentLength);
    }

    public String getMime() { return MIME_NAMES[type.ordinal()]; }

    public int getLength() { return contentLength; }

    public String asText() { return new String(content, 0, contentLength); }

    public byte[] asBytes() { return content; }
}
