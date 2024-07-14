import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.valves.ValveBase;

public class SomeValve extends ValveBase {

  @Override
  public void invoke(Request request, Response response) throws IOException, ServletException {
    HttpServletRequest httpRequest = (HttpServletRequest) request;
    final String authorization = httpRequest.getHeader("Authorization");
    if (authorization != null && authorization.toLowerCase().startsWith("basic")) {
      // Authorization: Basic base64credentials
      String base64Credentials = authorization.substring("Basic".length()).trim();
      byte[] credDecoded = Base64.getDecoder().decode(base64Credentials);
      String credentials = new String(credDecoded, StandardCharsets.UTF_8).trim();
      
      String allowColonsInPasswordsString = System.getenv().get("GROUPER_BASIC_AUTH_ALLOW_COLONS_IN_PASSWORDS");
      if (allowColonsInPasswordsString == null) {
        allowColonsInPasswordsString = "false";
      }
      
      allowColonsInPasswordsString = allowColonsInPasswordsString.toLowerCase();
      
      boolean allowColonsInPasswords = "true".equals(allowColonsInPasswordsString) || "yes".equals(allowColonsInPasswordsString) 
          || "t".equals(allowColonsInPasswordsString) || "y".equals(allowColonsInPasswordsString);
      
      String password = null;
      if (!credentials.contains(":")) {
        password = "";
      } else if (allowColonsInPasswords) {
        password = credentials.substring(credentials.indexOf(':')+1, credentials.length());
      } else {
        password = credentials.substring(credentials.lastIndexOf(':')+1, credentials.length());
      }
      
      
      // credentials = username:password
      if (password == null || password.trim().equals("")) {
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        httpResponse.setStatus(401);
        httpResponse.getWriter().append("Invalid username and password handled by patch valve.");
        httpResponse.getWriter().flush();
        return;
      }
      if ("mF6uEZ:wNn2DA".equals(credentials)) {
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        httpResponse.setStatus(401);
        httpResponse.getWriter().append("Invalid username and password handled by valve, patch valve is working.");
        httpResponse.getWriter().flush();
        return;
      }
    }
    getNext().invoke(request, response);
  }



}
