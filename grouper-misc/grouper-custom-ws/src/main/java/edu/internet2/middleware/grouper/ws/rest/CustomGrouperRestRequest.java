package edu.internet2.middleware.grouper.ws.rest;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.util.List;
import java.util.Map;

public class CustomGrouperRestRequest {
    private List<String> urlStrings;
    private Map<String, String[]> parameterMap;
    private String body;
    private ServletRequest servletRequest;
    private ServletResponse servletResponse;

    public CustomGrouperRestRequest(List<String> urlStrings, Map<String, String[]> parameterMap, String body, ServletRequest servletRequest, ServletResponse servletResponse) {
        this.urlStrings = urlStrings;
        this.parameterMap = parameterMap;
        this.body = body;
        this.servletRequest = servletRequest;
        this.servletResponse = servletResponse;
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
