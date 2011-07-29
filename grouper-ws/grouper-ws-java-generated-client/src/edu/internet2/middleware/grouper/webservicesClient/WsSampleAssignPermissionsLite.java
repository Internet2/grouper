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
import edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.AssignPermissionsLite;
import edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.WsAssignPermissionResult;
import edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.WsAssignPermissionsLiteResults;
import edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.WsAttributeAssign;
import edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.WsGroup;


/**
 *
 * @author mchyzer
 *
 */
public class WsSampleAssignPermissionsLite implements WsSampleGenerated {
    /**
     * @see edu.internet2.middleware.grouper.ws.samples.types.WsSampleGenerated#executeSample(edu.internet2.middleware.grouper.ws.samples.types.WsSampleGeneratedType)
     */
    public void executeSample(WsSampleGeneratedType wsSampleGeneratedType) {
        assignPermissionsLite(wsSampleGeneratedType);
    }

    /**
     * @param wsSampleGeneratedType can run as soap or xml/http
     */
    public static void assignPermissionsLite(
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

          AssignPermissionsLite assignPermissionsLite = AssignPermissionsLite.class.newInstance();

          // set the act as id
          assignPermissionsLite.setActAsSubjectId("GrouperSystem");
          assignPermissionsLite.setActAsSubjectIdentifier("");
          assignPermissionsLite.setActAsSubjectSourceId("");
          
          assignPermissionsLite.setAction("read");
          
          assignPermissionsLite.setAssignmentDisabledTime("");
          assignPermissionsLite.setAssignmentEnabledTime("");
          assignPermissionsLite.setAssignmentNotes("");
          assignPermissionsLite.setWsAttributeAssignId("");
          
          //version, e.g. v1_3_000
          assignPermissionsLite.setClientVersion(GeneratedClientSettings.VERSION);
          assignPermissionsLite.setDelegatable("");

          assignPermissionsLite.setIncludeGroupDetail("");
          assignPermissionsLite.setIncludeSubjectDetail("");
          assignPermissionsLite.setParamName0("");
          assignPermissionsLite.setParamValue0("");
          assignPermissionsLite.setParamName1("");
          assignPermissionsLite.setParamValue1("");

          assignPermissionsLite.setPermissionAssignOperation("assign_permission");
          assignPermissionsLite.setPermissionType("role_subject");

          assignPermissionsLite.setSubjectAttributeNames("");

          assignPermissionsLite.setPermissionDefNameId("");
          assignPermissionsLite.setPermissionDefNameName("aStem:permissionDefName");
          assignPermissionsLite.setRoleId("");
          assignPermissionsLite.setRoleName("");
          assignPermissionsLite.setSubjectRoleId("");
          assignPermissionsLite.setSubjectRoleName("aStem:role");
          assignPermissionsLite.setSubjectRoleSubjectId("test.subject.4");
          assignPermissionsLite.setSubjectRoleSubjectIdentifier("");
          assignPermissionsLite.setSubjectRoleSubjectSourceId("");
          
          WsAssignPermissionsLiteResults wsAssignPermissionsLiteResults = stub.assignPermissionsLite(assignPermissionsLite)
                                                                .get_return();

          System.out.println(ToStringBuilder.reflectionToString(
              wsAssignPermissionsLiteResults));

          WsAssignPermissionResult wsAttributeAssignResultArray = wsAssignPermissionsLiteResults.getWsPermissionAssignResult();

          for (WsAttributeAssign wsAttributeAssign : wsAttributeAssignResultArray.getWsAttributeAssigns()) {
            System.out.println(ToStringBuilder.reflectionToString(
                wsAttributeAssign));
          }
          
          WsGroup wsGroup = wsAssignPermissionsLiteResults.getWsGroup();

          System.out.println(ToStringBuilder.reflectionToString(
              wsGroup));

      } catch (Exception e) {
          throw new RuntimeException(e);
      }
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        assignPermissionsLite(WsSampleGeneratedType.soap);
    }
}
