package edu.internet2.middleware.grouper.j2ee;

import java.util.Set;

public abstract class MockServiceHandler {

  public abstract void handleRequest(MockServiceRequest mockServiceRequest, MockServiceResponse mockServiceResponse);
  
  public Set<String> doNotLogParameters() {
    return null;
  }

  public Set<String> doNotLogHeaders() {
    return null;
  }
}
