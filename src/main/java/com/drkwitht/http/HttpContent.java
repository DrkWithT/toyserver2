package com.drkwitht.http;

import java.util.Arrays;

/**
 * HttpContent.java
 * @author Derek Tan (DrkWithT at GitHub)
 * TODO: 3b. Also, make a method to convert DocResource objects into HttpContent objects.
 */

/**
 * @apiNote Encapsulates an HTTP/1.1 message's body data and its important properties such as MIME type and length.
 */
public class HttpContent {
    private HttpMimeType type;
    private int contentLength;
    private byte[] content;

    public HttpContent(HttpMimeType contentType, int byteCount, byte[] data) {
        type = contentType;
        contentLength = byteCount;
        content = Arrays.copyOf(data, contentLength);
    }

    public HttpMimeType getMimeType() { return type; }

    public int getLength() { return contentLength; }

    public String asText() { return new String(content, 0, contentLength); }

    public byte[] asBytes() { return content; }
}
