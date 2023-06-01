package com.drkwitht.http;

/**
 * HttpHeading.java
 * @author Derek Tan (DrkWithT)
 */

 /**
  * @apiNote Encapsulates HTTP/1.1 heading data from a request's status line.
  * For example, <code>GET / HTTP/1.1</code> is the equivalent representation of <code>HttpHeading("GET", "/", "HTTP/1.1")</code>.
  */
public class HttpHeading {
    private HttpMethod method;
    private String path;
    private String scheme;

    /**
     * 
     * @param methodCode
     * @param relativePath
     * @param scheme
     */
    public HttpHeading(HttpMethod method, String relativePath, String scheme) {
        this.method = method;
        this.path = relativePath;
        this.scheme = scheme;
    }

    public HttpMethod getMethodCode() {
        return this.method;
    }

    public String getPath() {
        return this.path;
    }

    public String getScheme() {
        return this.scheme;
    }
}
