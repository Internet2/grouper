package edu.internet2.middleware.grouper.webservicesClient;

import java.io.Reader;

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;

/**
 * 
 * @author mchyzer
 *
 */
public class RunGrouperServiceNonAxisStemDeleteSimple {

  /**
   * delete member simple web service with REST
   */
  public static void stemDeleteSimpleRest() {
    Reader xmlReader = null;
    try {
      HttpClient httpClient = new HttpClient();

      //	        String groupName,
      //			String groupUuid, String attributeName0, String attributeValue0, String attributeDelete0,
      //			String attributeName1, String attributeValue1, String attributeDelete1, String attributeName2,
      //			String attributeValue2, String attributeDelete2, String actAsSubjectId,
      //			String actAsSubjectIdentifier

      GetMethod method = new GetMethod(
          "http://localhost:8091/grouper-ws/services/GrouperService/stemDeleteSimple?"
              + "stemName=aStem:stemNotExist&stemUuid=&actAsSubjectId=GrouperSystem&actAsSubjectIdentifier=");

      httpClient.getParams().setAuthenticationPreemptive(true);
      Credentials defaultcreds = new UsernamePasswordCredentials("GrouperSystem", "pass");
      httpClient.getState()
          .setCredentials(new AuthScope("localhost", 8091), defaultcreds);

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
    stemDeleteSimpleRest();
  }
}
