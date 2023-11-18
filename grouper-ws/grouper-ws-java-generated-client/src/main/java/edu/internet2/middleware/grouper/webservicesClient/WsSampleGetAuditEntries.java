package edu.internet2.middleware.grouper.webservicesClient;

import org.apache.axis2.client.Options;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.axis2.transport.http.HttpTransportProperties;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;

import edu.internet2.middleware.grouper.webservicesClient.util.GeneratedClientSettings;
import edu.internet2.middleware.grouper.ws.samples.types.WsSampleGenerated;
import edu.internet2.middleware.grouper.ws.samples.types.WsSampleGeneratedType;
import edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.GetAuditEntries;
import edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.GetAuditEntriesResponse;
import edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.WsGetAuditEntriesResults;
import edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.WsGroupLookup;

public class WsSampleGetAuditEntries implements WsSampleGenerated {

  @Override
  public void executeSample(WsSampleGeneratedType wsSampleGeneratedType) {
    getAuditEntries(WsSampleGeneratedType.soap);
  }

  
  /**
  *
  * @param wsSampleGeneratedType can run as soap or xml/http
  */
 public static void getAuditEntries(WsSampleGeneratedType wsSampleGeneratedType) {
   try {
     //URL, e.g. http://localhost:8091/grouper-ws/services/GrouperService
     GrouperServiceStub stub = new GrouperServiceStub(GeneratedClientSettings.URL);
     Options options = stub._getServiceClient().getOptions();
     HttpTransportProperties.Authenticator auth = new HttpTransportProperties.Authenticator();
     auth.setUsername(GeneratedClientSettings.USER);
     auth.setPassword(GeneratedClientSettings.PASS);
     auth.setPreemptiveAuthentication(true);

     options.setProperty(HTTPConstants.AUTHENTICATE, auth);
     options.setProperty(HTTPConstants.SO_TIMEOUT, new Integer(3600000));
     options.setProperty(HTTPConstants.CONNECTION_TIMEOUT,
         new Integer(3600000));

     GetAuditEntries getAuditEntries = null;
     GetAuditEntriesResponse getAuditEntriesResponse = null;
     WsGetAuditEntriesResults wsGetAuditEntriesResult = null;

     getAuditEntries = GetAuditEntries.class.newInstance();

     //version, e.g. v1_3_000
     getAuditEntries.setClientVersion(GeneratedClientSettings.VERSION);

     getAuditEntries.setAuditType("group");
     getAuditEntries.setAuditActionId("addGroup");
     
     getAuditEntries.setPageLastCursorField("219ae92ea6554b18bbb979e1af725a7c");
     getAuditEntries.setPageIsCursor("T");
     getAuditEntries.setPageSize("2");
     getAuditEntries.setPageLastCursorFieldType("string");
     getAuditEntries.setPageCursorFieldIncludesLastRetrieved("F");
     getAuditEntries.setSortString("id");
     
     
//     WsGroupLookup wsGroupLookup = new WsGroupLookup();
//     wsGroupLookup.setGroupName("");
//     
//     getAuditEntries.setWsOwnerGroupLookup(wsGroupLookup);
     
     getAuditEntriesResponse = stub.getAuditEntries(getAuditEntries);
     wsGetAuditEntriesResult = getAuditEntriesResponse.get_return();
     System.out.println(ToStringBuilder.reflectionToString(
         wsGetAuditEntriesResult));
     System.out.println(ToStringBuilder.reflectionToString(
         wsGetAuditEntriesResult.getWsAuditEntries()[0]));
     
     if (!StringUtils.equals("T",
         wsGetAuditEntriesResult.getResultMetadata().getSuccess())) {
       throw new RuntimeException("didnt get success! ");
     }

   } catch (Exception e) {
     throw new RuntimeException(e);
   }
 }
 
 /**
  * @param args
  */
 public static void main(String[] args) {
   getAuditEntries(WsSampleGeneratedType.soap);
 } 
}
