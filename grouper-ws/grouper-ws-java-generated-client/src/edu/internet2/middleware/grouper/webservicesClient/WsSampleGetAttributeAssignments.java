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
import edu.internet2.middleware.grouper.ws.soap_v2_3.xsd.GetAttributeAssignments;
import edu.internet2.middleware.grouper.ws.soap_v2_3.xsd.WsAttributeAssign;
import edu.internet2.middleware.grouper.ws.soap_v2_3.xsd.WsAttributeAssignLookup;
import edu.internet2.middleware.grouper.ws.soap_v2_3.xsd.WsAttributeDefLookup;
import edu.internet2.middleware.grouper.ws.soap_v2_3.xsd.WsAttributeDefNameLookup;
import edu.internet2.middleware.grouper.ws.soap_v2_3.xsd.WsGetAttributeAssignmentsResults;
import edu.internet2.middleware.grouper.ws.soap_v2_3.xsd.WsGroup;
import edu.internet2.middleware.grouper.ws.soap_v2_3.xsd.WsGroupLookup;
import edu.internet2.middleware.grouper.ws.soap_v2_3.xsd.WsMembershipAnyLookup;
import edu.internet2.middleware.grouper.ws.soap_v2_3.xsd.WsMembershipLookup;
import edu.internet2.middleware.grouper.ws.soap_v2_3.xsd.WsParam;
import edu.internet2.middleware.grouper.ws.soap_v2_3.xsd.WsStemLookup;
import edu.internet2.middleware.grouper.ws.soap_v2_3.xsd.WsSubjectLookup;

/**
 *
 * @author mchyzer
 *
 */
public class WsSampleGetAttributeAssignments implements WsSampleGenerated {

  /**
   * @param args
   */
  public static void main(String[] args) {
    getAttributeAssignments(WsSampleGeneratedType.soap);
  }

  /**
   * @see edu.internet2.middleware.grouper.ws.samples.types.WsSampleGenerated#executeSample(edu.internet2.middleware.grouper.ws.samples.types.WsSampleGeneratedType)
   */
  public void executeSample(WsSampleGeneratedType wsSampleGeneratedType) {
    getAttributeAssignments(wsSampleGeneratedType);
  }

  /**
   * @param wsSampleGeneratedType can run as soap or xml/http
   */
  public static void getAttributeAssignments(
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

      GetAttributeAssignments getAttributeAssignments = GetAttributeAssignments.class.newInstance();

      // set the act as id
      WsSubjectLookup actAsSubject = WsSubjectLookup.class.newInstance();
      actAsSubject.setSubjectId("GrouperSystem");
      
      getAttributeAssignments.setActions(new String[]{null});
      getAttributeAssignments.setActAsSubjectLookup(actAsSubject);
      getAttributeAssignments.setAttributeAssignType("group");

      //version, e.g. v1_6_000
      getAttributeAssignments.setClientVersion(GeneratedClientSettings.VERSION);
      
      getAttributeAssignments.setEnabled("T");
      
      getAttributeAssignments.setIncludeAssignmentsOnAssignments("T");
      
      getAttributeAssignments.setIncludeGroupDetail("");
      getAttributeAssignments.setIncludeSubjectDetail("T");
      
      getAttributeAssignments.setParams(new WsParam[]{null});
      
      getAttributeAssignments.setSubjectAttributeNames(new String[]{null});
      
      getAttributeAssignments.setWsAttributeAssignLookups(new WsAttributeAssignLookup[]{null});
      getAttributeAssignments.setWsAttributeDefLookups(new WsAttributeDefLookup[]{null});
      
      WsAttributeDefNameLookup wsAttributeDefNameLookup = WsAttributeDefNameLookup.class.newInstance();
      wsAttributeDefNameLookup.setUuid("");
      wsAttributeDefNameLookup.setName("test:testAttributeAssignDefName");
      getAttributeAssignments.setWsAttributeDefNameLookups(new WsAttributeDefNameLookup[]{wsAttributeDefNameLookup});

      getAttributeAssignments.setWsOwnerAttributeDefLookups(new WsAttributeDefLookup[]{null});
      getAttributeAssignments.setWsOwnerGroupLookups(new WsGroupLookup[]{null});
      getAttributeAssignments.setWsOwnerMembershipAnyLookups(new WsMembershipAnyLookup[]{null});
      getAttributeAssignments.setWsOwnerMembershipLookups(new WsMembershipLookup[]{null});
      getAttributeAssignments.setWsOwnerStemLookups(new WsStemLookup[]{null});
      getAttributeAssignments.setWsOwnerSubjectLookups(new WsSubjectLookup[]{null});
      
      WsGetAttributeAssignmentsResults wsGetAttributeAssignmentsResults = stub.getAttributeAssignments(getAttributeAssignments)
          .get_return();

      System.out.println(ToStringBuilder.reflectionToString(
          wsGetAttributeAssignmentsResults));

      WsAttributeAssign[] wsAttributeAssignResultArray = wsGetAttributeAssignmentsResults.getWsAttributeAssigns();

      for (WsAttributeAssign wsAttributeAssign : wsAttributeAssignResultArray) {
        System.out.println(ToStringBuilder.reflectionToString(
            wsAttributeAssign));
      }
      
      WsGroup[] wsGroupsResultArray = wsGetAttributeAssignmentsResults.getWsGroups();

      for (WsGroup wsGroup : wsGroupsResultArray) {
        System.out.println(ToStringBuilder.reflectionToString(
            wsGroup));
      }
      
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
