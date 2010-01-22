package edu.internet2.middleware.grouper.webservicesClient;

import java.io.Reader;

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.PutMethod;

import edu.internet2.middleware.grouper.webservicesClient.util.ManualClientSettings;
import edu.internet2.middleware.grouper.ws.samples.WsSampleManualXmlHttp;

/**
 * @author mchyzer
 */
public class WsSampleAddMemberLite implements WsSampleManualXmlHttp {

  /**
   * add member simple web service with REST
   */
  public static void addMemberSimpleLite() {
    Reader xmlReader = null;
    try {
      HttpClient httpClient = new HttpClient();
      
      //NOTE: aStem:aGroup urlencoded substitutes %3A for a colon
      PutMethod method = new PutMethod(
          ManualClientSettings.URL + "/servicesRest/v1_5_000/groups/aStem%3AaGroup/members/GrouperSystem");

      httpClient.getParams().setAuthenticationPreemptive(true);
      Credentials defaultcreds = new UsernamePasswordCredentials(ManualClientSettings.USER, 
          ManualClientSettings.PASS);
      //e.g. localhost and 8093
      httpClient.getState()
          .setCredentials(new AuthScope(ManualClientSettings.HOST, ManualClientSettings.PORT), defaultcreds);

      httpClient.executeMethod(method);

      int statusCode = method.getStatusCode();

      // see if request worked or not
      if (statusCode != 200) {
        throw new RuntimeException("Bad response from web service: " + statusCode);
      }

      String response = method.getResponseBodyAsString();

      System.out.println(response);
    } catch (Exception e) {
      throw new RuntimeException(e);
    } finally {
      try {
        xmlReader.close();
      } catch (Exception e) {
      }
    }

  }

  /**
   * @param args
   */
  @SuppressWarnings("unchecked")
  public static void main(String[] args) {
    addMemberSimpleLite();
  }

  /**
   * @see edu.internet2.middleware.grouper.ws.samples.WsSampleManualXmlHttp#executeSample()
   */
  public void executeSample() {
    addMemberSimpleLite();
  }
}
