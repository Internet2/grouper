/**
 * 
 */
package edu.internet2.middleware.grouper.webservicesClient;

import edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.GetMembers;
import edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.WsGetMembersResult;
import edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.WsGetMembersResults;
import edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.WsGroupLookup;
import edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.WsSubject;
import edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.WsSubjectLookup;
import edu.internet2.middleware.grouper.webservicesClient.util.GeneratedClientSettings;
import edu.internet2.middleware.grouper.webservicesClient.util.GeneratedUtils;
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
public class WsSampleGetMembers implements WsSampleGenerated {

  /**
   * @param args
   */
  public static void main(String[] args) {
    getMembers(WsSampleGeneratedType.soap);
  }

  /**
   * @see edu.internet2.middleware.grouper.ws.samples.types.WsSampleGenerated#executeSample(edu.internet2.middleware.grouper.ws.samples.types.WsSampleGeneratedType)
   */
  public void executeSample(WsSampleGeneratedType wsSampleGeneratedType) {
    getMembers(wsSampleGeneratedType);
  }

  /**
   * @param wsSampleGeneratedType can run as soap or xml/http
   */
  public static void getMembers(WsSampleGeneratedType wsSampleGeneratedType) {
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

      GetMembers getMembers = GetMembers.class.newInstance();

      //version, e.g. v1_3_000
      getMembers.setClientVersion(GeneratedClientSettings.VERSION);

      // set the act as id
      WsSubjectLookup actAsSubject = WsSubjectLookup.class.newInstance();
      actAsSubject.setSubjectId("GrouperSystem");
      getMembers.setActAsSubjectLookup(actAsSubject);

      WsGroupLookup wsGroupLookup = WsGroupLookup.class.newInstance();
      wsGroupLookup.setGroupName("aStem:aGroup");

      WsGroupLookup[] wsGroupLookups = new WsGroupLookup[] { wsGroupLookup };
      getMembers.setWsGroupLookups(wsGroupLookups);

      getMembers.setFieldName("");
      getMembers.setMemberFilter("All");
      getMembers.setIncludeGroupDetail("F");
      getMembers.setIncludeSubjectDetail("T");

      WsGetMembersResults wsGetMembersResults = stub.getMembers(getMembers).get_return();

      System.out.println(ToStringBuilder.reflectionToString(wsGetMembersResults));

      WsGetMembersResult[] wsGetMemberResults = wsGetMembersResults.getResults();
      int i = 0;

      for (WsGetMembersResult wsGetMembersResult : GeneratedUtils
          .nonNull(wsGetMemberResults)) {
        System.out.println("Result: " + i++ + ": code: "
            + wsGetMembersResult.getResultMetadata().getResultCode());

        WsSubject[] wsSubjectArray = wsGetMembersResult.getWsSubjects();

        for (WsSubject wsSubject : GeneratedUtils.nonNull(wsSubjectArray)) {
          System.out.println(ToStringBuilder.reflectionToString(wsSubject));
        }
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
