package com.drkwitht.handlers;

import java.util.ArrayList;
import java.util.HashMap;

public class HandlerStorage {
    private HashMap<String, Integer> indexMap;     // maps routes to handler references!
    private ArrayList<IRequestHandler> handlers;   // stores handlers in defined indexes for multiple paths each!
    private IRequestHandler fallbackHandler;
    
    public HandlerStorage() {
        indexMap = new HashMap<String, Integer>();
        handlers = new ArrayList<IRequestHandler>();
        fallbackHandler = null;
    }

    public void registerHandler(String[] paths, IRequestHandler handler) {
        handlers.add(handler);

        int recentIndex = handlers.size() - 1;

        for (String path : paths) {
            indexMap.put(path, recentIndex);
        }
    }

    public void setFallbackHandler(IRequestHandler fallback) {
        fallbackHandler = fallback;
    }

    public synchronized IRequestHandler getHandler(String path) {
        Integer targetIndex = indexMap.get(path);

        if (targetIndex == null) {
            return fallbackHandler;
        }

        return handlers.get(targetIndex.intValue());
    }
}
