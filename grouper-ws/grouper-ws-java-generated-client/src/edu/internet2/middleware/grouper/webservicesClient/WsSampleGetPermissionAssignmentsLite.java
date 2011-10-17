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
import edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GetPermissionAssignmentsLite;
import edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsGetPermissionAssignmentsResults;
import edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsGroup;
import edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsPermissionAssign;


/**
 *
 * @author mchyzer
 *
 */
public class WsSampleGetPermissionAssignmentsLite implements WsSampleGenerated {
    /**
     * @see edu.internet2.middleware.grouper.ws.samples.types.WsSampleGenerated#executeSample(edu.internet2.middleware.grouper.ws.samples.types.WsSampleGeneratedType)
     */
    public void executeSample(WsSampleGeneratedType wsSampleGeneratedType) {
        getPermissionAssignmentsLite(wsSampleGeneratedType);
    }

    /**
     * @param wsSampleGeneratedType can run as soap or xml/http
     */
    public static void getPermissionAssignmentsLite(
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

          GetPermissionAssignmentsLite getPermissionAssignmentsLite = GetPermissionAssignmentsLite.class.newInstance();

          // set the act as id
          getPermissionAssignmentsLite.setActAsSubjectId("GrouperSystem");
          getPermissionAssignmentsLite.setActAsSubjectIdentifier("");
          getPermissionAssignmentsLite.setActAsSubjectSourceId("");
          
          getPermissionAssignmentsLite.setAction("");
          

          //version, e.g. v1_3_000
          getPermissionAssignmentsLite.setClientVersion(GeneratedClientSettings.VERSION);
          
          getPermissionAssignmentsLite.setEnabled("");
          
          getPermissionAssignmentsLite.setIncludeAssignmentsOnAssignments("");

          getPermissionAssignmentsLite.setIncludeAttributeAssignments("");
          getPermissionAssignmentsLite.setIncludeAttributeDefNames("");
          
          getPermissionAssignmentsLite.setIncludePermissionAssignDetail("");

          getPermissionAssignmentsLite.setIncludeGroupDetail("");
          getPermissionAssignmentsLite.setIncludeSubjectDetail("");
          getPermissionAssignmentsLite.setParamName0("");
          getPermissionAssignmentsLite.setParamValue0("");
          getPermissionAssignmentsLite.setParamName1("");
          getPermissionAssignmentsLite.setParamValue1("");

          getPermissionAssignmentsLite.setSubjectAttributeNames("");
          getPermissionAssignmentsLite.setWsAttributeDefId("");
          getPermissionAssignmentsLite.setWsAttributeDefName("aStem:permissionDef");
          getPermissionAssignmentsLite.setWsAttributeDefNameId("");
          getPermissionAssignmentsLite.setWsAttributeDefNameName("");

          getPermissionAssignmentsLite.setRoleId("");
          getPermissionAssignmentsLite.setRoleName("");
          getPermissionAssignmentsLite.setWsSubjectId("");
          getPermissionAssignmentsLite.setWsSubjectIdentifier("");
          getPermissionAssignmentsLite.setWsSubjectSourceId("");
          
          WsGetPermissionAssignmentsResults wsGetPermissionAssignmentsResults = stub.getPermissionAssignmentsLite(getPermissionAssignmentsLite)
                                                                .get_return();

          System.out.println(ToStringBuilder.reflectionToString(
                  wsGetPermissionAssignmentsResults));

          WsPermissionAssign[] wsPermissionAssignResultArray = wsGetPermissionAssignmentsResults.getWsPermissionAssigns();

          for (WsPermissionAssign wsPermissionAssign : wsPermissionAssignResultArray) {
            System.out.println(ToStringBuilder.reflectionToString(
                wsPermissionAssign));
          }
          
          WsGroup[] wsGroupsResultArray = wsGetPermissionAssignmentsResults.getWsGroups();

          for (WsGroup wsGroup : wsGroupsResultArray) {
            System.out.println(ToStringBuilder.reflectionToString(
                wsGroup));
          }

      } catch (Exception e) {
          throw new RuntimeException(e);
      }
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        getPermissionAssignmentsLite(WsSampleGeneratedType.soap);
    }
}
