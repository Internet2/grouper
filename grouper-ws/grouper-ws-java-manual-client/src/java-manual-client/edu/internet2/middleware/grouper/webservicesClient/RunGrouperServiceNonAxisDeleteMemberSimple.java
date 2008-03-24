package edu.internet2.middleware.grouper.webservicesClient;

import java.io.Reader;
import java.io.StringReader;

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;

/**
 * 
 * @author mchyzer
 *
 */
public class RunGrouperServiceNonAxisDeleteMemberSimple {

  /**
   * delete member simple web service with REST
   */
  public static void deleteMemberSimpleRest() {
    Reader xmlReader = null;
    try {
      HttpClient httpClient = new HttpClient();
      GetMethod method = new GetMethod(
          "http://localhost:8091/grouper-ws/services/GrouperService/deleteMemberSimple?groupName=aStem:aGroup&subjectId=10021368&actAsSubjectId=GrouperSystem");

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

      //lets load this into jdom, since it is xml
      xmlReader = new StringReader(response);

      // process xml
      Document document = new SAXBuilder().build(xmlReader);
      Element deleteMemberSimpleResponse = document.getRootElement();

      //parse: <ns:deleteMemberSimpleResponse xmlns:ns="http://webservices.grouper.middleware.internet2.edu/xsd">
      RunGrouperServiceNonAxisUtils.assertTrue("deleteMemberSimpleResponse"
          .equals(deleteMemberSimpleResponse.getName()),
          "root not deleteMemberSimpleResponse: " + deleteMemberSimpleResponse.getName());

      Namespace namespace = deleteMemberSimpleResponse.getNamespace();

      //parse: <ns:return type="edu.internet2.middleware.grouper.webservices.WsDeleteMemberResult">
      Element returnElement = deleteMemberSimpleResponse.getChild("return", namespace);
      String theType = returnElement.getAttributeValue("type");
      RunGrouperServiceNonAxisUtils.assertTrue(
          "edu.internet2.middleware.grouper.webservices.WsDeleteMemberResult"
              .equals(theType),
          "type not edu.internet2.middleware.grouper.webservices.WsDeleteMemberResult: "
              + theType);

      //<ns:errorMessage xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:nil="true" />
      String errorMessage = returnElement.getChildText("errorMessage", namespace);

      //<ns:resultCode xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:nil="true" />
      String resultCode = returnElement.getChildText("resultCode", namespace);

      //<ns:subjectId>GrouperSystem</ns:subjectId>
      String subjectId = returnElement.getChildText("subjectId", namespace);

      //<ns:subjectIdentifier xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:nil="true" />
      String subjectIdentifier = returnElement.getChildText("subjectIdentifier",
          namespace);

      //<ns:success>T</ns:success>
      String success = returnElement.getChildText("success", namespace);

      System.out.println("Success: " + success + ", resultCode: " + resultCode
          + ", subjectId: " + subjectId + ", subjectIdentifier: " + subjectIdentifier
          + ", errorMessage: " + errorMessage);

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
    deleteMemberSimpleRest();
  }
}
