package edu.internet2.middleware.grouper.ws.rest;

public interface CustomGrouperRestProvider {
    boolean supports(CustomGrouperRestRequest o);
    Object provide(CustomGrouperRestRequest o);
}
