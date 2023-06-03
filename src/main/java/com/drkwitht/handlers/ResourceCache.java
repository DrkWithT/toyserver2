package com.drkwitht.handlers;

import java.io.File;
import java.util.HashMap;

public class ResourceCache {
    private int size;
    private HashMap<String, StaticResource> resourceMap;  // NOTE: path to static resources... assume that the mapping is 1:1 !

    public ResourceCache(String rootFolderPath) throws Exception {
        resourceMap = new HashMap<String, StaticResource>();
        File rootEntry = new File(rootFolderPath);

        if (!rootEntry.isDirectory())
            throw new Exception("ResourceCache: root path is not a folder.");
        
        File[] entries = rootEntry.listFiles();

        for (File file : entries) {
            resourceMap.put(file.getName(), new StaticResource(file.getAbsolutePath()));
        }

        size = resourceMap.size();
    }

    public boolean isEmpty() {
        return size > 0;
    }

    public synchronized StaticResource getResource(String name) {
        if (name.charAt(0) == '/')
            return resourceMap.get(name.substring(1));

        return resourceMap.get(name);
    }
}
