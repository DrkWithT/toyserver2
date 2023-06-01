package com.drkwitht.http;

import java.util.ArrayList;

public class HttpHeader {
    private String name;  // http header name
    private ArrayList<String> values;  // list of http header values delimited by ';'
    private boolean hasSimpleValue;

    public HttpHeader(String headerName, String valueLiteral) {
        this.name = headerName;
        this.values = new ArrayList<String>();

        for (String value : valueLiteral.split(";")) {
            this.values.add(value);
        };

        this.hasSimpleValue = this.values.size() == 1;
    }

    public boolean isSimple() {
        return this.hasSimpleValue;
    }

    public String getName() {
        return this.name;
    }

    public String getValueAt(int index) {
        if (index < 0 || index > this.values.size())
            return null;

        return this.values.get(index);
    }
}
