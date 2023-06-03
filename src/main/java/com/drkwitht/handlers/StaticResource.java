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

    public StaticResource(String filePath) throws FileNotFoundException, IOException {
        File fileObject = new File(filePath);
        String fileExtension = fileObject.getName().split(".")[0].toLowerCase();
        FileInputStream fileStream = new FileInputStream(fileObject);

        if (fileExtension.equalsIgnoreCase("txt"))
            type = HttpMimeType.PLAIN_TXT;
        else if (fileExtension.equalsIgnoreCase("html"))
            type = HttpMimeType.HTML_TXT;
        else if (fileExtension.equalsIgnoreCase("css"))
            type = HttpMimeType.CSS_TXT;
        else if (fileExtension.equalsIgnoreCase("json"))
            type = HttpMimeType.APPLICATION_JSON;
        else
            type = HttpMimeType.UNKNOWN;

        contentBytes = fileStream.readAllBytes();

        fileStream.close();

        contentSize = contentBytes.length;
    }

    @Override
    public HttpContent toWebContent() {
        if (type == HttpMimeType.UNKNOWN)
            return null;

        return new HttpContent(type, (int)contentSize, contentBytes);
    }
}
