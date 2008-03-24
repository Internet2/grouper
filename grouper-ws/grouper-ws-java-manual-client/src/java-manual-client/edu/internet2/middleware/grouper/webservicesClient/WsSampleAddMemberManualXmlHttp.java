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

import edu.internet2.middleware.grouper.webservicesClient.util.ManualClientSettings;
import edu.internet2.middleware.grouper.ws.samples.WsSampleManualXmlHttp;

/**
 * 
 */
public class WsSampleAddMemberManualXmlHttp implements WsSampleManualXmlHttp {

  /**
   * 
   */
  @SuppressWarnings("unchecked")
  public static void addMemberXmlHttp() {
    //lets load this into jdom, since it is xml
    Reader xmlReader = null;

    try {
      HttpClient httpClient = new HttpClient();
      
      //e.g. http://localhost:8093/grouper-ws/services/GrouperService
      PostMethod method = new PostMethod(
          ManualClientSettings.URL);

      method.setRequestHeader("Content-Type", "application/xml; charset=UTF-8");
      httpClient.getParams().setAuthenticationPreemptive(true);
      Credentials defaultcreds = new UsernamePasswordCredentials(ManualClientSettings.USER, 
          ManualClientSettings.PASS);
      //e.g. localhost and 8093
      httpClient.getState()
          .setCredentials(new AuthScope(ManualClientSettings.HOST, ManualClientSettings.PORT), defaultcreds);
      String xml = "<ns1:addMember xmlns:ns1=\"http://soap.ws.grouper.middleware.internet2.edu/xsd\"><ns1:clientVersion>" +
      		ManualClientSettings.VERSION + "</ns1:clientVersion><ns1:wsGroupLookup><ns1:groupName>aStem:aGroup</ns1:groupName></ns1:wsGroupLookup><ns1:subjectLookups><ns1:subjectId>10021368</ns1:subjectId></ns1:subjectLookups><ns1:subjectLookups><ns1:subjectId>10039438</ns1:subjectId></ns1:subjectLookups><ns1:replaceAllExisting>F</ns1:replaceAllExisting><ns1:actAsSubjectLookup><ns1:subjectId>GrouperSystem</ns1:subjectId></ns1:actAsSubjectLookup></ns1:addMember>";
      
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
      Element addMemberResponse = document.getRootElement();

      //parse: 

      
      //  <ns:addMemberResponse xmlns:ns="http://soap.ws.grouper.middleware.internet2.edu/xsd">
      
      RunGrouperServiceNonAxisUtils.assertTrue("addMemberResponse"
          .equals(addMemberResponse.getName()), "root not addMemberResponse: "
          + addMemberResponse.getName());


      
      Namespace namespace = addMemberResponse.getNamespace();

      //  <ns:return type="edu.internet2.middleware.grouper.ws.soap.WsAddMemberResults">
      Element returnElement = addMemberResponse.getChild("return", namespace);
      String theType = returnElement.getAttributeValue("type");
      RunGrouperServiceNonAxisUtils.assertTrue(
          "edu.internet2.middleware.grouper.ws.soap.WsAddMemberResults"
              .equals(theType),
          "type not edu.internet2.middleware.grouper.ws.soap.WsAddMemberResults: "
              + theType);

      
      //      <ns:resultCode>SUCCESS</ns:resultCode>
      //      <ns:resultMessage>Success for: clientVersion: v1_3_000, wsGroupLookup: edu.internet2.middleware.grouper.ws.soap.WsGroupLookup@14f5021[group=&lt;null>,uuid=&lt;null>,groupName=aStem:aGroup,groupFindResult=&lt;null>], subjectLookups: Array size: 2: [0]: edu.internet2.middleware.grouper.ws.soap.WsSubjectLookup@12cd8d4[subject=&lt;nul...
      //, replaceAllExisting: false, actAsSubject: edu.internet2.middleware.grouper.ws.soap.WsSubjectLookup@bb6255[subject=&lt;null>,cause=&lt;null>,subjectFindResult=&lt;null>,subjectId=GrouperSystem,subjectIdentifier=&lt;null>,subjectType=&lt;null>,subjectSource=&lt;null>], fieldName: null, txType: NONE, includeGroupDetail: false, includeSubjectDetail: false, subjectAttributeNames: Empty array
      //, paramNames: Empty array, paramValues: Empty array</ns:resultMessage>
      //    <ns:resultMetadata type="edu.internet2.middleware.grouper.ws.soap.WsResultMeta">
      Element resultMetadata = returnElement.getChild("resultMetadata", namespace);
      String resultCode = resultMetadata.getChildText("resultCode", namespace);
      String resultMessage = resultMetadata.getChildText("resultMessage", namespace);
      //    <ns:success>T</ns:success>
      String success = returnElement.getChildText("success", namespace);
      //      <ns:resultWarnings></ns:resultWarnings>
      //      <ns:serverVersion>v1_3_000</ns:serverVersion>
      //      <ns:success>T</ns:success>
      //    </ns:resultMetadata>

      List<Element> resultsList = returnElement.getChildren("results", namespace);

      if (resultsList != null) {
        int i = 0;
        for (Element resultsElement : resultsList) {
          //    <ns:results type="edu.internet2.middleware.grouper.ws.soap.WsAddMemberResult">
          String resultsType = resultsElement.getAttributeValue("type");
          RunGrouperServiceNonAxisUtils.assertTrue(
              "edu.internet2.middleware.grouper.ws.soap.WsAddMemberResult"
                  .equals(resultsType),
              "type not edu.internet2.middleware.grouper.ws.soap.WsAddMemberResult: "
                  + theType);

          //      <ns:resultMetadata type="edu.internet2.middleware.grouper.ws.soap.WsResultMeta">
          //        <ns:resultCode>SUCCESS</ns:resultCode>
          //        <ns:resultMessage></ns:resultMessage>
          //        <ns:resultWarnings></ns:resultWarnings>
          //        <ns:serverVersion>v1_3_000</ns:serverVersion>
          //        <ns:success>T</ns:success>
          //      </ns:resultMetadata>
          Element innerResultMetadata = resultsElement.getChild("resultMetadata", namespace);
          String resultResultCode = innerResultMetadata.getChildText("resultCode", namespace);
          String resultResultMessage = innerResultMetadata.getChildText("resultMessage",
              namespace);
          String resultSuccess = innerResultMetadata.getChildText("success", namespace);



          //      <ns:wsSubject type="edu.internet2.middleware.grouper.ws.soap.WsSubject">
          //        <ns:attributeValues xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:nil="true" />
          //        <ns:id>10021368</ns:id>
          //        <ns:name xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:nil="true" />
          //        <ns:resultCode>SUCCESS</ns:resultCode>
          //        <ns:source>QSUOB JDBC Source Adapter</ns:source>
          //        <ns:success>T</ns:success>
          //      </ns:wsSubject>
          
          Element innerSubject = resultsElement.getChild("wsSubject", namespace);

          String resultSubjectId = innerSubject.getChildText("id", namespace);

          System.out.println("Row: " + i++ + ": success: " + resultSuccess + ", code: "
              + resultResultCode + ", id: " + resultSubjectId + ", message: " + resultResultMessage);

          
          //    </ns:results>
    
          //    <ns:subjectAttributeNames xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:nil="true" />
          //    <ns:wsGroupAssigned type="edu.internet2.middleware.grouper.ws.soap.WsGroup">
          //      <ns:description>somedescription</ns:description>
          //      <ns:detail xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:nil="true" />
          //      <ns:displayExtension>test group</ns:displayExtension>
          //      <ns:displayName>a stem:test group</ns:displayName>
          //      <ns:extension>aGroup</ns:extension>
          //      <ns:name>aStem:aGroup</ns:name>
          //      <ns:uuid>cd89f7c5-913e-4788-9a67-c78a5fee1fba</ns:uuid>
          //    </ns:wsGroupAssigned>
          //  </ns:return>
          //</ns:addMemberResponse>
    
        }
        //		</ns:results>
      }

      //	</ns:return>
      //</ns:addMemberResponse>

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
    addMemberXmlHttp();
  }

  /**
   * @see edu.internet2.middleware.grouper.ws.samples.WsSampleManualXmlHttp#executeSample()
   */
  public void executeSample() {
    addMemberXmlHttp();
  }
}
