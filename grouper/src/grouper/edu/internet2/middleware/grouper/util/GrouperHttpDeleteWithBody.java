package edu.internet2.middleware.grouper.util;

import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import java.net.URI;

public class GrouperHttpDeleteWithBody extends HttpEntityEnclosingRequestBase {
  
  public final static String METHOD_NAME = "DELETE";

  @Override
  public String getMethod() {
    return METHOD_NAME;
  }
  
  public GrouperHttpDeleteWithBody(final String uri) {
    super();
    setURI(URI.create(uri));
}

  public GrouperHttpDeleteWithBody(final URI uri) {
      super();
      setURI(uri);
  }
  
  public GrouperHttpDeleteWithBody() {
      super();
  }

}
