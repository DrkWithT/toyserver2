package com.drkwitht.http;

/**
 * HttpRequest.java
 * @author Derek Tan (DrkWithT at GitHub)
 */

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

public class HttpRequest {
    private HttpHeading heading;
    private HashMap<String, HttpHeader> headers;
    private HttpContent body;

    public HttpRequest(HttpHeading headingObj, ArrayList<HttpHeader> headerList, HttpContent bodyObj) {
        heading = headingObj;
        headers = new HashMap<String, HttpHeader>();
        body = bodyObj;

        for (HttpHeader httpHeader : headerList) {
            headers.put(httpHeader.getName(), httpHeader);
        }
    }

    public HttpMethod getMethod() { return heading.getMethodCode(); }

    public String getPath() { return heading.getPath(); }

    public String getScheme() { return heading.getScheme(); }
    
    public HttpHeader getHeader(String name) {
        return headers.get(name.toLowerCase(Locale.ENGLISH));
    }

    public String getBodyAsText() {
        return body.asText();
    }

    public byte[] getBodyBytes() {
        return body.asBytes();
    }
}
