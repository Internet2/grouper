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
import edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.GetAttributeAssignmentsLite;
import edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.WsAttributeAssign;
import edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.WsGetAttributeAssignmentsResults;
import edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.WsGroup;


/**
 *
 * @author mchyzer
 *
 */
public class WsSampleGetAttributeAssignmentsLite implements WsSampleGenerated {
    /**
     * @see edu.internet2.middleware.grouper.ws.samples.types.WsSampleGenerated#executeSample(edu.internet2.middleware.grouper.ws.samples.types.WsSampleGeneratedType)
     */
    public void executeSample(WsSampleGeneratedType wsSampleGeneratedType) {
        getAttributeAssignmentsLite(wsSampleGeneratedType);
    }

    /**
     * @param wsSampleGeneratedType can run as soap or xml/http
     */
    public static void getAttributeAssignmentsLite(
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

          GetAttributeAssignmentsLite getAttributeAssignmentsLite = GetAttributeAssignmentsLite.class.newInstance();

          // set the act as id
          getAttributeAssignmentsLite.setActAsSubjectId("GrouperSystem");
          getAttributeAssignmentsLite.setActAsSubjectIdentifier("");
          getAttributeAssignmentsLite.setActAsSubjectSourceId("");
          
          getAttributeAssignmentsLite.setAction("");
          
          getAttributeAssignmentsLite.setAttributeAssignId("");
          
          getAttributeAssignmentsLite.setAttributeAssignType("group");

          //version, e.g. v1_3_000
          getAttributeAssignmentsLite.setClientVersion(GeneratedClientSettings.VERSION);
          
          getAttributeAssignmentsLite.setEnabled("");
          
          getAttributeAssignmentsLite.setIncludeAssignmentsOnAssignments("T");
          
          getAttributeAssignmentsLite.setIncludeGroupDetail("");
          getAttributeAssignmentsLite.setIncludeSubjectDetail("");
          getAttributeAssignmentsLite.setParamName0("");
          getAttributeAssignmentsLite.setParamValue0("");
          getAttributeAssignmentsLite.setParamName1("");
          getAttributeAssignmentsLite.setParamValue1("");

          getAttributeAssignmentsLite.setSubjectAttributeNames("");
          getAttributeAssignmentsLite.setWsAttributeDefId("");
          getAttributeAssignmentsLite.setWsAttributeDefName("");
          getAttributeAssignmentsLite.setWsAttributeDefNameId("");
          getAttributeAssignmentsLite.setWsAttributeDefNameName("test:testAttributeAssignDefName");
          
          getAttributeAssignmentsLite.setWsOwnerAttributeDefId("");
          getAttributeAssignmentsLite.setWsOwnerAttributeDefName("");
          getAttributeAssignmentsLite.setWsOwnerGroupId("");
          getAttributeAssignmentsLite.setWsOwnerGroupName("");
          getAttributeAssignmentsLite.setWsOwnerMembershipAnyGroupId("");
          getAttributeAssignmentsLite.setWsOwnerMembershipAnyGroupName("");
          getAttributeAssignmentsLite.setWsOwnerMembershipAnySubjectId("");
          getAttributeAssignmentsLite.setWsOwnerMembershipAnySubjectIdentifier("");
          getAttributeAssignmentsLite.setWsOwnerMembershipAnySubjectSourceId("");
          getAttributeAssignmentsLite.setWsOwnerMembershipId("");
          getAttributeAssignmentsLite.setWsOwnerStemId("");
          getAttributeAssignmentsLite.setWsOwnerStemName("");
          getAttributeAssignmentsLite.setWsOwnerSubjectId("");
          getAttributeAssignmentsLite.setWsOwnerSubjectIdentifier("");
          getAttributeAssignmentsLite.setWsOwnerSubjectSourceId("");
          
          WsGetAttributeAssignmentsResults wsGetAttributeAssignmentsResults = stub.getAttributeAssignmentsLite(getAttributeAssignmentsLite)
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

    /**
     * @param args
     */
    public static void main(String[] args) {
        getAttributeAssignmentsLite(WsSampleGeneratedType.soap);
    }
}
