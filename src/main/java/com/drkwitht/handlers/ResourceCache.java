package com.drkwitht.handlers;

import java.io.File;
import java.util.HashMap;

public class ResourceCache {
    private int size;
    private HashMap<String, StaticResource> resourceMap;  // NOTE: path to static resources... assume that the mapping is 1:1 !

    public ResourceCache(String rootFolderPath) throws Exception {
        resourceMap = new HashMap<String, StaticResource>();
        File rootEntry = new File(rootFolderPath).getAbsoluteFile();

        if (!rootEntry.exists())
            throw new Exception("ResourceCache: root path not valid.");

        if (!rootEntry.isDirectory())
            throw new Exception("ResourceCache: root path is not a folder.");

        File[] files = rootEntry.listFiles();

        for (File file : files) {
            resourceMap.put(file.getName(), new StaticResource(file));
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
