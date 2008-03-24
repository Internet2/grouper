package edu.internet2.middleware.grouper.webservicesClient;

import java.io.InputStream;
import java.io.Reader;

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.io.IOUtils;

/**
 * find stems
 * @author mchyzer
 *
 */
public class RunGrouperServiceNonAxisFindStems {

  /**
   * find stems rest
   */
  @SuppressWarnings("unchecked")
  public static void findStemsRest() {
    //lets load this into jdom, since it is xml
    Reader xmlReader = null;

    try {
      HttpClient httpClient = new HttpClient();
      PostMethod method = new PostMethod(
          "http://localhost:8091/grouper-ws/services/GrouperService");

      method.setRequestHeader("Content-Type", "application/xml; charset=UTF-8");
      httpClient.getParams().setAuthenticationPreemptive(true);
      Credentials defaultcreds = new UsernamePasswordCredentials("GrouperSystem", "pass");
      httpClient.getState()
          .setCredentials(new AuthScope("localhost", 8091), defaultcreds);

      String xml = "<ns1:findStems xmlns:ns1=\"http://webservices.grouper.middleware.internet2.edu/xsd\">"
          + "<ns1:stemName>aStem</ns1:stemName>"
          + "<ns1:parentStemName></ns1:parentStemName>"
          + "<ns1:parentStemNameScope></ns1:parentStemNameScope>"
          + "<ns1:stemUuid></ns1:stemUuid>"
          + "<ns1:queryTerm></ns1:queryTerm>"
          + "<ns1:querySearchFromStemName></ns1:querySearchFromStemName>"
          + "<ns1:queryScope></ns1:queryScope>" + "</ns1:findStems>";

      RequestEntity requestEntity = new StringRequestEntity(xml);
      method.setRequestEntity(requestEntity);
      httpClient.executeMethod(method);

      int statusCode = method.getStatusCode();

      // see if request worked or not
      if (statusCode != 200) {
        throw new RuntimeException("Bad response from web service: " + statusCode);
      }
      //there is a getResponseAsString, but it logs a warning each time...
      InputStream inputStream = method.getResponseBodyAsStream();
      String response = null;
      try {
        response = IOUtils.toString(inputStream);
      } finally {
        IOUtils.closeQuietly(inputStream);
      }

      System.out.println(response);
    } catch (Exception e) {
      throw new RuntimeException(e);
    } finally {
      try {
        if (xmlReader != null) {
          xmlReader.close();
        }
      } catch (Exception e) {
      }
    }

  }

  /**
     * @param args
     */
  @SuppressWarnings("unchecked")
  public static void main(String[] args) {
    findStemsRest();
  }
}
