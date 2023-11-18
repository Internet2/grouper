package edu.internet2.middleware.grouper.webservicesClient;

import org.apache.axis2.client.Options;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.axis2.transport.http.HttpTransportProperties;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;

import edu.internet2.middleware.grouper.webservicesClient.util.GeneratedClientSettings;
import edu.internet2.middleware.grouper.ws.samples.types.WsSampleGenerated;
import edu.internet2.middleware.grouper.ws.samples.types.WsSampleGeneratedType;
import edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.GetAuditEntriesLite;
import edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.GetAuditEntriesLiteResponse;
import edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.WsGetAuditEntriesResults;

public class WsSampleGetAuditEntriesLite implements WsSampleGenerated {

  @Override
  public void executeSample(WsSampleGeneratedType wsSampleGeneratedType) {
    getAuditEntriesLite(WsSampleGeneratedType.soap);
  }

  
  /**
  *
  * @param wsSampleGeneratedType can run as soap or xml/http
  */
 public static void getAuditEntriesLite(WsSampleGeneratedType wsSampleGeneratedType) {
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

     GetAuditEntriesLite getAuditEntriesLite = null;
     GetAuditEntriesLiteResponse getAuditEntriesLiteResponse = null;
     WsGetAuditEntriesResults wsGetAuditEntriesResult = null;

     getAuditEntriesLite = GetAuditEntriesLite.class.newInstance();

     //version, e.g. v1_3_000
     getAuditEntriesLite.setClientVersion(GeneratedClientSettings.VERSION);

     getAuditEntriesLite.setAuditType("group");
     getAuditEntriesLite.setAuditActionId("addGroup");


     getAuditEntriesLiteResponse = stub.getAuditEntriesLite(getAuditEntriesLite);
     wsGetAuditEntriesResult = getAuditEntriesLiteResponse.get_return();
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
   getAuditEntriesLite(WsSampleGeneratedType.soap);
 } 
}
