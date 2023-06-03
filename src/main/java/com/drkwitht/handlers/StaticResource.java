package com.drkwitht.handlers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import com.drkwitht.http.HttpContent;
import com.drkwitht.http.HttpMimeType;

public class StaticResource implements IStaticResource {
    private HttpMimeType type;
    private byte[] contentBytes;
    private long contentSize;

    public StaticResource(File file) throws FileNotFoundException, IOException {
        String fileName = file.getName();
        int dotIndex = fileName.lastIndexOf(".");
        String fileExtension = "any";

        if (dotIndex >= 0) {
            fileExtension = fileName.substring(dotIndex + 1);
        }

        if (fileExtension.equalsIgnoreCase("txt")) {
            type = HttpMimeType.PLAIN_TXT;
        } else if (fileExtension.equalsIgnoreCase("html")) {
            type = HttpMimeType.HTML_TXT;
        } else if (fileExtension.equalsIgnoreCase("css")) {
            type = HttpMimeType.CSS_TXT;
        } else if (fileExtension.equalsIgnoreCase("json")) {
            type = HttpMimeType.APPLICATION_JSON;
        } else {
            type = HttpMimeType.ANY;
        }

        FileInputStream fileStream = new FileInputStream(file);
        contentBytes = fileStream.readAllBytes();

        fileStream.close();

        contentSize = contentBytes.length;
    }

    @Override
    public HttpContent toWebContent() {
        if (type == HttpMimeType.ANY)
            return null;

        return new HttpContent(type, (int)contentSize, contentBytes);
    }
}
