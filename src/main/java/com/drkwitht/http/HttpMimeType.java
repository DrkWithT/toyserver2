package com.drkwitht.http;

/**
 * HttpMimeType.java
 * @author Derek Tan (DrkWithT at GitHub)
 */

 /**
  * @apiNote Defines constants for Content-Type of byte bodies in HTTP/1.1.
  */
public enum HttpMimeType {
    PLAIN_TXT,
    HTML_TXT,
    CSS_TXT,
    APPLICATION_JSON,  // NOTE: unused for now!
    ANY
}
