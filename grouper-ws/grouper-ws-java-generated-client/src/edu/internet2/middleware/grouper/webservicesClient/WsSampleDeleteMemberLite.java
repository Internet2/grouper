/**
 * 
 */
package edu.internet2.middleware.grouper.webservicesClient;

import edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.DeleteMemberLite;
import edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.WsDeleteMemberLiteResult;
import edu.internet2.middleware.grouper.webservicesClient.util.GeneratedClientSettings;
import edu.internet2.middleware.grouper.ws.samples.types.WsSampleGenerated;
import edu.internet2.middleware.grouper.ws.samples.types.WsSampleGeneratedType;

import org.apache.axis2.client.Options;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.axis2.transport.http.HttpTransportProperties;

import org.apache.commons.lang.builder.ToStringBuilder;

/**
 *
 * @author mchyzer
 *
 */
public class WsSampleDeleteMemberLite implements WsSampleGenerated {

  /**
   * @see edu.internet2.middleware.grouper.ws.samples.types.WsSampleGenerated#executeSample(edu.internet2.middleware.grouper.ws.samples.types.WsSampleGeneratedType)
   */
  public void executeSample(WsSampleGeneratedType wsSampleGeneratedType) {
    deleteMemberLite(wsSampleGeneratedType);
  }

  /**
   * @param wsSampleGeneratedType can run as soap or xml/http
   */
  public static void deleteMemberLite(WsSampleGeneratedType wsSampleGeneratedType) {
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
      options.setProperty(HTTPConstants.CONNECTION_TIMEOUT, new Integer(3600000));

      DeleteMemberLite deleteMemberLite = DeleteMemberLite.class.newInstance();

      //version, e.g. v1_3_000
      deleteMemberLite.setClientVersion(GeneratedClientSettings.VERSION);

      // set the act as id
      deleteMemberLite.setActAsSubjectId("GrouperSystem");

      deleteMemberLite.setGroupName("aStem:aGroup");

      deleteMemberLite.setSubjectId("10021368");

      WsDeleteMemberLiteResult wsDeleteMemberLiteResult = stub.deleteMemberLite(
          deleteMemberLite).get_return();

      System.out.println(ToStringBuilder.reflectionToString(wsDeleteMemberLiteResult));
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    deleteMemberLite(WsSampleGeneratedType.soap);
  }
}
