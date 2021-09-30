package edu.internet2.middleware.grouper.ws.rest;

import java.util.List;
import java.util.Map;

public class CustomGrouperRestRequest {
    private List<String> urlStrings;
    private Map<String, String[]> parameterMap;
    private String body;

    public CustomGrouperRestRequest(List<String> urlStrings, Map<String, String[]> parameterMap, String body) {
        this.urlStrings = urlStrings;
        this.parameterMap = parameterMap;
        this.body = body;
    }

    public List<String> getUrlStrings() {
        return urlStrings;
    }

    public void setUrlStrings(List<String> urlStrings) {
        this.urlStrings = urlStrings;
    }

    public Map<String, String[]> getParameterMap() {
        return parameterMap;
    }

    public void setParameterMap(Map<String, String[]> parameterMap) {
        this.parameterMap = parameterMap;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }
}
