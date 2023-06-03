package com.drkwitht.http;

public class HttpHeader {
    private String name;   // http header name
    private String value;  // http header literal
    private boolean hasSimpleValue;

    public HttpHeader(String headerName, String valueLiteral) {
        name = headerName;
        value = valueLiteral;

        hasSimpleValue = valueLiteral.indexOf(";") == -1;
    }

    public boolean isSimple() {
        return hasSimpleValue;
    }

    public String getName() {
        return name;
    }

    public String getLiteralValue() {
        return value;
    }
}
