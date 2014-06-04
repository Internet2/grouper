/*******************************************************************************
 * Copyright 2012 Internet2
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
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
import edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.AssignPermissionsLite;
import edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.WsAssignPermissionResult;
import edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.WsAssignPermissionsLiteResults;
import edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.WsAttributeAssign;
import edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.WsGroup;


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
