package edu.internet2.middleware.grouper.webservicesClient;

import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.util.List;

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.io.IOUtils;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;

/**
 * @author mchyzer
 *
 */
public class RunGrouperServiceNonAxisDeleteMember {

  /**
   * 
   */
  @SuppressWarnings("unchecked")
  public static void deleteMemberRest() {
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
      String xml = "<ns1:deleteMember xmlns:ns1=\"http://webservices.grouper.middleware.internet2.edu/xsd\">"
          + "<ns1:wsGroupLookup><ns1:groupName>aStem:aGroup</ns1:groupName></ns1:wsGroupLookup>"
          + "<ns1:subjectLookups><ns1:subjectId>10021368</ns1:subjectId></ns1:subjectLookups>"
          + "<ns1:subjectLookups><ns1:subjectId>10039438</ns1:subjectId></ns1:subjectLookups>"
          + "<ns1:actAsSubjectLookup><ns1:subjectId>GrouperSystem</ns1:subjectId></ns1:actAsSubjectLookup>"
          + "</ns1:deleteMember>";
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

      //lets load this into jdom, since it is xml
      xmlReader = new StringReader(response);

      // process xml
      Document document = new SAXBuilder().build(xmlReader);
      Element deleteMemberResponse = document.getRootElement();

      //parse: 

      //	<ns:deleteMemberResponse
      //	xmlns:ns="http://webservices.grouper.middleware.internet2.edu/xsd">

      RunGrouperServiceNonAxisUtils.assertTrue("deleteMemberResponse"
          .equals(deleteMemberResponse.getName()), "root not deleteMemberResponse: "
          + deleteMemberResponse.getName());

      Namespace namespace = deleteMemberResponse.getNamespace();

      //	<ns:return
      //		type="edu.internet2.middleware.grouper.webservices.WsDeleteMemberResults">
      Element returnElement = deleteMemberResponse.getChild("return", namespace);

      String theType = returnElement.getAttributeValue("type");
      RunGrouperServiceNonAxisUtils.assertTrue(
          "edu.internet2.middleware.grouper.webservices.WsDeleteMemberResults"
              .equals(theType),
          "type not edu.internet2.middleware.grouper.webservices.WsDeleteMemberResults: "
              + theType);

      String resultCode = returnElement.getChildText("resultCode", namespace);
      String resultMessage = returnElement.getChildText("resultMessage", namespace);

      //		<ns:success>T</ns:success>
      String success = returnElement.getChildText("success", namespace);

      List<Element> resultsList = returnElement.getChildren("results", namespace);

      if (resultsList != null) {
        int i = 0;
        for (Element resultsElement : resultsList) {
          //		<ns:results
          //			type="edu.internet2.middleware.grouper.webservices.WsDeleteMemberResult">
          String resultsType = resultsElement.getAttributeValue("type");
          RunGrouperServiceNonAxisUtils.assertTrue(
              "edu.internet2.middleware.grouper.webservices.WsDeleteMemberResult"
                  .equals(resultsType),
              "type not edu.internet2.middleware.grouper.webservices.WsDeleteMemberResult: "
                  + theType);

          //			<ns:resultCode
          //				xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
          //				xsi:nil="true" />
          String resultResultCode = resultsElement.getChildText("resultCode", namespace);

          //			<ns:resultMessage
          //				xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
          //				xsi:nil="true" />
          String resultResultMessage = resultsElement.getChildText("resultMessage",
              namespace);

          //			<ns:subjectId>10021368</ns:subjectId>
          String resultSubjectId = resultsElement.getChildText("subjectId", namespace);

          //			<ns:subjectIdentifier
          //				xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
          //				xsi:nil="true" />
          String resultSubjectIdentifier = resultsElement.getChildText(
              "subjectIdentifier", namespace);

          //			<ns:success>T</ns:success>
          String resultSuccess = resultsElement.getChildText("success", namespace);

          System.out.println("Row: " + i++ + ": success: " + resultSuccess + ", code: "
              + resultResultCode + ", id: " + resultSubjectId + ", identifier: "
              + resultSubjectIdentifier + ", message: " + resultResultMessage);
        }
        //		</ns:results>
      }

      //	</ns:return>
      //</ns:deleteMemberResponse>

      System.out.println("Success: " + success + ", resultCode: " + resultCode
          + ", resultMessage: " + resultMessage);
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
    deleteMemberRest();
  }
}
