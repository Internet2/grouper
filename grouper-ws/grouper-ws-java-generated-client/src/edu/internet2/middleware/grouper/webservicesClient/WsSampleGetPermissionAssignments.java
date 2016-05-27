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

import edu.internet2.middleware.grouper.ws.samples.types.WsSampleGenerated;
import edu.internet2.middleware.grouper.ws.samples.types.WsSampleGeneratedType;
import org.apache.axis2.client.Options;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.axis2.transport.http.HttpTransportProperties;
import org.apache.commons.lang.builder.ToStringBuilder;

import edu.internet2.middleware.grouper.webservicesClient.util.GeneratedClientSettings;
import edu.internet2.middleware.grouper.ws.soap_v2_3.xsd.GetPermissionAssignments;
import edu.internet2.middleware.grouper.ws.soap_v2_3.xsd.WsAttributeDefLookup;
import edu.internet2.middleware.grouper.ws.soap_v2_3.xsd.WsAttributeDefNameLookup;
import edu.internet2.middleware.grouper.ws.soap_v2_3.xsd.WsGetPermissionAssignmentsResults;
import edu.internet2.middleware.grouper.ws.soap_v2_3.xsd.WsGroup;
import edu.internet2.middleware.grouper.ws.soap_v2_3.xsd.WsGroupLookup;
import edu.internet2.middleware.grouper.ws.soap_v2_3.xsd.WsParam;
import edu.internet2.middleware.grouper.ws.soap_v2_3.xsd.WsPermissionAssign;
import edu.internet2.middleware.grouper.ws.soap_v2_3.xsd.WsSubjectLookup;

/**
 *
 * @author mchyzer
 *
 */
public class WsSampleGetPermissionAssignments implements WsSampleGenerated {

  /**
   * @param args
   */
  public static void main(String[] args) {
    getPermissionAssignments(WsSampleGeneratedType.soap);
  }

  /**
   * @see edu.internet2.middleware.grouper.ws.samples.types.WsSampleGenerated#executeSample(edu.internet2.middleware.grouper.ws.samples.types.WsSampleGeneratedType)
   */
  public void executeSample(WsSampleGeneratedType wsSampleGeneratedType) {
    getPermissionAssignments(wsSampleGeneratedType);
  }

  /**
   * @param wsSampleGeneratedType can run as soap or xml/http
   */
  public static void getPermissionAssignments(
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

      GetPermissionAssignments getPermissionAssignments = GetPermissionAssignments.class.newInstance();

      // set the act as id
      WsSubjectLookup actAsSubject = WsSubjectLookup.class.newInstance();
      actAsSubject.setSubjectId("GrouperSystem");
      
      getPermissionAssignments.setActions(new String[]{null});
      getPermissionAssignments.setActAsSubjectLookup(actAsSubject);

      //version, e.g. v1_6_000
      getPermissionAssignments.setClientVersion(GeneratedClientSettings.VERSION);
      
      getPermissionAssignments.setEnabled("T");
      
      getPermissionAssignments.setIncludeAssignmentsOnAssignments("");
      getPermissionAssignments.setIncludeAttributeAssignments("");
      getPermissionAssignments.setIncludeAttributeDefNames("");
      
      getPermissionAssignments.setIncludeGroupDetail("");
      getPermissionAssignments.setIncludePermissionAssignDetail("");
      getPermissionAssignments.setIncludeSubjectDetail("T");
      
      getPermissionAssignments.setParams(new WsParam[]{null});
      
      getPermissionAssignments.setRoleLookups(new WsGroupLookup[]{null});
      getPermissionAssignments.setSubjectAttributeNames(new String[]{null});
      
      WsAttributeDefLookup wsAttributeDefLookup = WsAttributeDefLookup.class.newInstance();
      wsAttributeDefLookup.setUuid("");
      wsAttributeDefLookup.setName("aStem:permissionDef");

      getPermissionAssignments.setWsAttributeDefLookups(new WsAttributeDefLookup[]{wsAttributeDefLookup});
      
      getPermissionAssignments.setWsAttributeDefNameLookups(new WsAttributeDefNameLookup[]{null});

      getPermissionAssignments.setWsSubjectLookups(new WsSubjectLookup[]{null});
      
      WsGetPermissionAssignmentsResults wsGetPermissionAssignmentsResults = stub.getPermissionAssignments(getPermissionAssignments)
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
}
