package com.drkwitht.handlers;

import java.io.IOException;

import com.drkwitht.handlers.ResourceCache;
import com.drkwitht.http.HttpRequest;
import com.drkwitht.http.HttpStatus;

/**
 * IRequestHandler.java
 * @author Derek Tan
 */

/**
 * @apiNote Defines common methods shared by all <code>RequestHandlers</code>. For lambda expressions to be added.
 */
public interface IRequestHandler {
    public void handle(ResourceCache contextCache, HttpRequest request, MyHttpResponse response, HttpStatus statusOnError) throws IOException;
}
