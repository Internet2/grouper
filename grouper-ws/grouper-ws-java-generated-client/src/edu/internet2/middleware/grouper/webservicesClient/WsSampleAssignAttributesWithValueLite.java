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
import edu.internet2.middleware.grouper.ws.soap_v2_3.xsd.AssignAttributesLite;
import edu.internet2.middleware.grouper.ws.soap_v2_3.xsd.WsAssignAttributeResult;
import edu.internet2.middleware.grouper.ws.soap_v2_3.xsd.WsAssignAttributesLiteResults;
import edu.internet2.middleware.grouper.ws.soap_v2_3.xsd.WsAttributeAssign;
import edu.internet2.middleware.grouper.ws.soap_v2_3.xsd.WsGroup;


/**
 *
 * @author mchyzer
 *
 */
public class WsSampleAssignAttributesWithValueLite implements WsSampleGenerated {
    /**
     * @see edu.internet2.middleware.grouper.ws.samples.types.WsSampleGenerated#executeSample(edu.internet2.middleware.grouper.ws.samples.types.WsSampleGeneratedType)
     */
    public void executeSample(WsSampleGeneratedType wsSampleGeneratedType) {
        assignAttributesWithValueLite(wsSampleGeneratedType);
    }

    /**
     * @param wsSampleGeneratedType can run as soap or xml/http
     */
    public static void assignAttributesWithValueLite(
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

          AssignAttributesLite assignAttributesLite = AssignAttributesLite.class.newInstance();

          // set the act as id
          assignAttributesLite.setActAsSubjectId("GrouperSystem");
          assignAttributesLite.setActAsSubjectIdentifier("");
          assignAttributesLite.setActAsSubjectSourceId("");
          
          assignAttributesLite.setAction("");
          
          assignAttributesLite.setAssignmentDisabledTime("");
          assignAttributesLite.setAssignmentEnabledTime("");
          assignAttributesLite.setAssignmentNotes("");
          assignAttributesLite.setAttributeAssignOperation("assign_attr");
          assignAttributesLite.setWsAttributeAssignId("");
          assignAttributesLite.setAttributeAssignType("group");
          assignAttributesLite.setAttributeAssignValueOperation("add_value");
          
          //version, e.g. v1_3_000
          assignAttributesLite.setClientVersion(GeneratedClientSettings.VERSION);
          assignAttributesLite.setDelegatable("");

          assignAttributesLite.setIncludeGroupDetail("");
          assignAttributesLite.setIncludeSubjectDetail("");
          assignAttributesLite.setParamName0("");
          assignAttributesLite.setParamValue0("");
          assignAttributesLite.setParamName1("");
          assignAttributesLite.setParamValue1("");

          assignAttributesLite.setSubjectAttributeNames("");
          assignAttributesLite.setValueId("");
          assignAttributesLite.setValueSystem("34");
          assignAttributesLite.setValueFormatted("");

          assignAttributesLite.setWsAttributeDefNameId("");
          assignAttributesLite.setWsAttributeDefNameName("test:testAttributeAssignDefName");
          assignAttributesLite.setWsOwnerAttributeAssignId("");
          assignAttributesLite.setWsOwnerAttributeDefId("");
          assignAttributesLite.setWsOwnerAttributeDefName("");
          assignAttributesLite.setWsOwnerGroupId("");
          assignAttributesLite.setWsOwnerGroupName("test:groupTestAttrAssign");
          assignAttributesLite.setWsOwnerMembershipAnyGroupId("");
          assignAttributesLite.setWsOwnerMembershipAnyGroupName("");
          assignAttributesLite.setWsOwnerMembershipAnySubjectId("");
          assignAttributesLite.setWsOwnerMembershipAnySubjectIdentifier("");
          assignAttributesLite.setWsOwnerMembershipAnySubjectSourceId("");
          assignAttributesLite.setWsOwnerMembershipId("");
          assignAttributesLite.setWsOwnerStemId("");
          assignAttributesLite.setWsOwnerStemName("");
          assignAttributesLite.setWsOwnerSubjectId("");
          assignAttributesLite.setWsOwnerSubjectIdentifier("");
          assignAttributesLite.setWsOwnerSubjectSourceId("");
          
          WsAssignAttributesLiteResults wsAssignAttributesLiteResults = stub.assignAttributesLite(assignAttributesLite)
                                                                .get_return();

          System.out.println(ToStringBuilder.reflectionToString(
              wsAssignAttributesLiteResults));

          WsAssignAttributeResult wsAttributeAssignResultArray = wsAssignAttributesLiteResults.getWsAttributeAssignResult();

          for (WsAttributeAssign wsAttributeAssign : wsAttributeAssignResultArray.getWsAttributeAssigns()) {
            System.out.println(ToStringBuilder.reflectionToString(
                wsAttributeAssign));
          }
          
          WsGroup wsGroup = wsAssignAttributesLiteResults.getWsGroup();

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
        assignAttributesWithValueLite(WsSampleGeneratedType.soap);
    }
}
