package com.langtools;

public class BuildResult {
    private final boolean success;
    private final String output;

    public BuildResult(boolean success, String output) {
        this.success = success;
        this.output = output;
    }

    /**
     * Creates a JSON string containing the build's status and output.
     *
     * @return a JSON string.
     */
    public String toJSONStr() {
        return "{\"success\":" + success + ",\"output\":" + output + "}";
    }
}
