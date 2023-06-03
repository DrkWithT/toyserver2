package com.drkwitht.handlers;

import com.drkwitht.http.HttpContent;

/**
 * @apiNote Defines common methods for any StaticResource. Methods are used to help make responses.
 * @author Derek Tan (DrkWithT at GitHub)
 */
public interface IStaticResource {
    /**
     * @apiNote Converts an item from a <code>ResourceCache</code> to a HttpContent object.
     * @return A <code>HttpContent</code> object to put in a <code>HttpRepsonse</code>.
     */
    public HttpContent toWebContent();
}
