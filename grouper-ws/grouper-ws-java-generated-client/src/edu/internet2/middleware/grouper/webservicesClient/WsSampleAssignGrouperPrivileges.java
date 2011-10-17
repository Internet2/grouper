/**
 *
 */
package edu.internet2.middleware.grouper.webservicesClient;

import java.lang.reflect.Array;

import org.apache.axis2.client.Options;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.axis2.transport.http.HttpTransportProperties;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import edu.internet2.middleware.grouper.webservicesClient.util.GeneratedClientSettings;
import edu.internet2.middleware.grouper.ws.samples.types.WsSampleGenerated;
import edu.internet2.middleware.grouper.ws.samples.types.WsSampleGeneratedType;
import edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.AssignGrouperPrivileges;
import edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsAssignGrouperPrivilegesResult;
import edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsAssignGrouperPrivilegesResults;
import edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsGroupLookup;
import edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsParam;
import edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsStemLookup;
import edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsSubjectLookup;

/**
 *
 * @author mchyzer
 *
 */
public class WsSampleAssignGrouperPrivileges implements WsSampleGenerated {

  /**
   * @param args
   */
  public static void main(String[] args) {
    assignGrouperPrivileges(WsSampleGeneratedType.soap);
  }

  /**
   * @param wsSampleGeneratedType can run as soap or xml/http
   */
  public static void assignGrouperPrivileges(WsSampleGeneratedType wsSampleGeneratedType) {
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

      AssignGrouperPrivileges assignGrouperPrivileges = AssignGrouperPrivileges.class
          .newInstance();

      // set the act as id
      WsSubjectLookup actAsSubject = WsSubjectLookup.class.newInstance();
      actAsSubject.setSubjectId("GrouperSystem");
      assignGrouperPrivileges.setActAsSubjectLookup(actAsSubject);

      // just add, dont replace
      assignGrouperPrivileges.setReplaceAllExisting("F");

      WsGroupLookup wsGroupLookup = WsGroupLookup.class.newInstance();
      wsGroupLookup.setGroupName("aStem:aGroup");
      assignGrouperPrivileges.setWsGroupLookup(wsGroupLookup);

      assignGrouperPrivileges.setWsStemLookup(new WsStemLookup());

      //version, e.g. v1_3_000
      assignGrouperPrivileges.setClientVersion(GeneratedClientSettings.VERSION);

      // add two subjects to the group
      WsSubjectLookup[] subjectLookups = (WsSubjectLookup[]) Array.newInstance(
          WsSubjectLookup.class,
          2);
      subjectLookups[0] = WsSubjectLookup.class.newInstance();
      subjectLookups[0].setSubjectId("test.subject.0");
      subjectLookups[0].setSubjectSourceId("jdbc");
      subjectLookups[0].setSubjectIdentifier("");

      subjectLookups[1] = WsSubjectLookup.class.newInstance();
      subjectLookups[1].setSubjectId("");
      subjectLookups[1].setSubjectSourceId("");
      subjectLookups[1].setSubjectIdentifier("id.test.subject.1");

      assignGrouperPrivileges.setWsSubjectLookups(subjectLookups);

      assignGrouperPrivileges.setAllowed("T");

      assignGrouperPrivileges.setIncludeGroupDetail("F");
      assignGrouperPrivileges.setIncludeSubjectDetail("F");
      assignGrouperPrivileges.setParams(new WsParam[0]);

      assignGrouperPrivileges.setSubjectAttributeNames(new String[0]);

      assignGrouperPrivileges.setPrivilegeType("access");
      assignGrouperPrivileges.setPrivilegeNames(new String[] { "read", "update" });

      WsAssignGrouperPrivilegesResults wsAssignGrouperPrivilegesResults = stub
          .assignGrouperPrivileges(assignGrouperPrivileges)
          .get_return();

      System.out.println(ToStringBuilder.reflectionToString(
          wsAssignGrouperPrivilegesResults, ToStringStyle.MULTI_LINE_STYLE));
      System.out.println(ToStringBuilder.reflectionToString(
          wsAssignGrouperPrivilegesResults.getResultMetadata(),
          ToStringStyle.MULTI_LINE_STYLE));

      if (wsAssignGrouperPrivilegesResults != null) {
        for (WsAssignGrouperPrivilegesResult wsAssignGrouperPrivilegesResult : wsAssignGrouperPrivilegesResults
            .getResults()) {
          System.out.println(ToStringBuilder.reflectionToString(
              wsAssignGrouperPrivilegesResult, ToStringStyle.MULTI_LINE_STYLE));
          System.out.println(ToStringBuilder.reflectionToString(
              wsAssignGrouperPrivilegesResult.getResultMetadata(),
              ToStringStyle.MULTI_LINE_STYLE));
        }
      }

      if (!StringUtils.equals("T",
          wsAssignGrouperPrivilegesResults.getResultMetadata().getSuccess())) {
        throw new RuntimeException("didnt get success! ");
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * @see edu.internet2.middleware.grouper.ws.samples.types.WsSampleGenerated#executeSample(edu.internet2.middleware.grouper.ws.samples.types.WsSampleGeneratedType)
   */
  public void executeSample(WsSampleGeneratedType wsSampleGeneratedType) {
    assignGrouperPrivileges(wsSampleGeneratedType);
  }
}
