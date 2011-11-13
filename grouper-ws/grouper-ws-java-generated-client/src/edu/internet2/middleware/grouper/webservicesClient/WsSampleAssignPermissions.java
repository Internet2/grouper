/**
 *
 */
package edu.internet2.middleware.grouper.webservicesClient;

import org.apache.axis2.client.Options;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.axis2.transport.http.HttpTransportProperties;
import org.apache.commons.lang.builder.ToStringBuilder;

import edu.internet2.middleware.grouper.webservicesClient.util.GeneratedClientSettings;
import edu.internet2.middleware.grouper.ws.samples.types.WsSampleGenerated;
import edu.internet2.middleware.grouper.ws.samples.types.WsSampleGeneratedType;
import edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.AssignPermissions;
import edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsAssignPermissionResult;
import edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsAssignPermissionsResults;
import edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsAttributeAssignLookup;
import edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsAttributeDefNameLookup;
import edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsGroup;
import edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsGroupLookup;
import edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsMembershipAnyLookup;
import edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsParam;
import edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsSubjectLookup;

/**
 *
 * @author mchyzer
 *
 */
public class WsSampleAssignPermissions implements WsSampleGenerated {

  /**
   * @param args
   */
  public static void main(String[] args) {
    assignPermissions(WsSampleGeneratedType.soap);
  }

  /**
   * @see edu.internet2.middleware.grouper.ws.samples.types.WsSampleGenerated#executeSample(edu.internet2.middleware.grouper.ws.samples.types.WsSampleGeneratedType)
   */
  public void executeSample(WsSampleGeneratedType wsSampleGeneratedType) {
    assignPermissions(wsSampleGeneratedType);
  }

  /**
   * @param wsSampleGeneratedType can run as soap or xml/http
   */
  public static void assignPermissions(
      WsSampleGeneratedType wsSampleGeneratedType) {
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

      AssignPermissions assignPermissions = AssignPermissions.class.newInstance();

      // set the act as id
      WsSubjectLookup actAsSubject = WsSubjectLookup.class.newInstance();
      actAsSubject.setSubjectId("GrouperSystem");
      
      assignPermissions.setActAsSubjectLookup(actAsSubject);
      assignPermissions.setActions(new String[]{"read", "write"});
      assignPermissions.setAssignmentDisabledTime("");
      assignPermissions.setAssignmentEnabledTime("");
      assignPermissions.setAssignmentNotes("");
      
      //version, e.g. v1_6_000
      assignPermissions.setClientVersion(GeneratedClientSettings.VERSION);
      
      assignPermissions.setDelegatable("");
      
      assignPermissions.setIncludeGroupDetail("");
      assignPermissions.setIncludeSubjectDetail("");
      
      assignPermissions.setParams(new WsParam[]{null});
      assignPermissions.setPermissionAssignOperation("assign_permission");
      assignPermissions.setPermissionType("role");
      
      assignPermissions.setSubjectAttributeNames(new String[]{null});
      
      assignPermissions.setWsAttributeAssignLookups(new WsAttributeAssignLookup[]{null});
      
      WsAttributeDefNameLookup permissionDefNameLookup = WsAttributeDefNameLookup.class.newInstance();
      permissionDefNameLookup.setUuid("");
      permissionDefNameLookup.setName("aStem:permissionDefName");
      assignPermissions.setPermissionDefNameLookups(new WsAttributeDefNameLookup[]{permissionDefNameLookup});

      WsGroupLookup roleLookup = new WsGroupLookup();
      roleLookup.setGroupName("aStem:role");
      roleLookup.setUuid("");
      assignPermissions.setRoleLookups(new WsGroupLookup[]{roleLookup});
      
      assignPermissions.setSubjectRoleLookups(new WsMembershipAnyLookup[]{null});
      
      
      WsAssignPermissionsResults wsAssignPermissionsResults = stub.assignPermissions(assignPermissions)
          .get_return();

      System.out.println(ToStringBuilder.reflectionToString(
          wsAssignPermissionsResults));

      WsAssignPermissionResult[] wsAssignPermissionResultArray = wsAssignPermissionsResults.getWsAssignPermissionResults();

      for (WsAssignPermissionResult wsAssignPermissionResult : wsAssignPermissionResultArray) {
        System.out.println(ToStringBuilder.reflectionToString(
            wsAssignPermissionResult));
      }
      
      WsGroup[] wsGroupsResultArray = wsAssignPermissionsResults.getWsGroups();

      for (WsGroup wsGroup : wsGroupsResultArray) {
        System.out.println(ToStringBuilder.reflectionToString(
            wsGroup));
      }
      
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
