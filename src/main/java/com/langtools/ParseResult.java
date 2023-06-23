package com.langtools;

public class ParseResult {
    private final boolean success;
    private final String output;

    public ParseResult(boolean success, String output) {
        this.success = success;
        this.output = output;
    }

    public String toJsonStr() {
        return "{\"success\":" + success + ",\"output\":" + output + "}";
    }
}
