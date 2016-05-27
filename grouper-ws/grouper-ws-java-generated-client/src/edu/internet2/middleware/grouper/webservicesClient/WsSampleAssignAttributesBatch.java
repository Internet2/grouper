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
import edu.internet2.middleware.grouper.ws.soap_v2_3.xsd.AssignAttributesBatch;
import edu.internet2.middleware.grouper.ws.soap_v2_3.xsd.WsAssignAttributeBatchEntry;
import edu.internet2.middleware.grouper.ws.soap_v2_3.xsd.WsAssignAttributeBatchResult;
import edu.internet2.middleware.grouper.ws.soap_v2_3.xsd.WsAssignAttributesBatchResults;
import edu.internet2.middleware.grouper.ws.soap_v2_3.xsd.WsAttributeAssignLookup;
import edu.internet2.middleware.grouper.ws.soap_v2_3.xsd.WsAttributeDefNameLookup;
import edu.internet2.middleware.grouper.ws.soap_v2_3.xsd.WsGroup;
import edu.internet2.middleware.grouper.ws.soap_v2_3.xsd.WsGroupLookup;
import edu.internet2.middleware.grouper.ws.soap_v2_3.xsd.WsSubjectLookup;

/**
 *
 * @author mchyzer
 *
 */
public class WsSampleAssignAttributesBatch implements WsSampleGenerated {

  /**
   * @param args
   */
  public static void main(String[] args) {
    assignAttributesBatch(WsSampleGeneratedType.soap);
  }

  /**
   * @see edu.internet2.middleware.grouper.ws.samples.types.WsSampleGenerated#executeSample(edu.internet2.middleware.grouper.ws.samples.types.WsSampleGeneratedType)
   */
  public void executeSample(WsSampleGeneratedType wsSampleGeneratedType) {
    assignAttributesBatch(wsSampleGeneratedType);
  }

  /**
   * @param wsSampleGeneratedType can run as soap or xml/http
   */
  public static void assignAttributesBatch(
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

      AssignAttributesBatch assignAttributesBatch = AssignAttributesBatch.class.newInstance();

      //version, e.g. v1_6_000
      assignAttributesBatch.setClientVersion(GeneratedClientSettings.VERSION);
      
      // set the act as id
      WsSubjectLookup actAsSubject = WsSubjectLookup.class.newInstance();
      actAsSubject.setSubjectId("GrouperSystem");

      WsAssignAttributeBatchEntry wsAssignAttributeBatchEntry1 = WsAssignAttributeBatchEntry.class.newInstance();
      
      {
        wsAssignAttributeBatchEntry1.setAttributeAssignOperation("assign_attr");
        wsAssignAttributeBatchEntry1.setAttributeAssignType("group");
        
        WsAttributeDefNameLookup wsAttributeDefNameLookup1 = WsAttributeDefNameLookup.class.newInstance();
        wsAttributeDefNameLookup1.setName("test:testAttributeAssignDefName");
        wsAssignAttributeBatchEntry1.setWsAttributeDefNameLookup(wsAttributeDefNameLookup1);

        WsGroupLookup wsGroupLookup = new WsGroupLookup();
        wsGroupLookup.setGroupName("test:groupTestAttrAssign");

        wsAssignAttributeBatchEntry1.setWsOwnerGroupLookup(wsGroupLookup);

      }
      
      WsAssignAttributeBatchEntry wsAssignAttributeBatchEntry2 = WsAssignAttributeBatchEntry.class.newInstance();

      {
        wsAssignAttributeBatchEntry2.setAttributeAssignOperation("assign_attr");
        wsAssignAttributeBatchEntry2.setAttributeAssignType("group_asgn");
        
        WsAttributeDefNameLookup wsAttributeDefNameLookup2 = WsAttributeDefNameLookup.class.newInstance();
        wsAttributeDefNameLookup2.setName("test:testAttributeAssignAssignName");
        wsAssignAttributeBatchEntry2.setWsAttributeDefNameLookup(wsAttributeDefNameLookup2);
  
        WsAttributeAssignLookup wsAttributeAssignLookup = new WsAttributeAssignLookup();
        wsAttributeAssignLookup.setBatchIndex("0");
        wsAssignAttributeBatchEntry2.setWsOwnerAttributeAssignLookup(wsAttributeAssignLookup);
      }
      

      WsAssignAttributeBatchEntry[] wsAssignAttributeBatchEntries = new WsAssignAttributeBatchEntry[]{
          wsAssignAttributeBatchEntry1, wsAssignAttributeBatchEntry2};
      
      assignAttributesBatch.setWsAssignAttributeBatchEntries(wsAssignAttributeBatchEntries);
      
      WsAssignAttributesBatchResults wsAssignAttributesBatchResults = stub.assignAttributesBatch(assignAttributesBatch)
          .get_return();

      System.out.println(ToStringBuilder.reflectionToString(
          wsAssignAttributesBatchResults));

      WsAssignAttributeBatchResult[] wsAssignAttributeBatchResultsArray = wsAssignAttributesBatchResults
        .getWsAssignAttributeBatchResultArray();

      for (WsAssignAttributeBatchResult wsAssignAttributeBatchResult : wsAssignAttributeBatchResultsArray) {
        System.out.println(ToStringBuilder.reflectionToString(
            wsAssignAttributeBatchResult));
      }
      
      WsGroup[] wsGroupsResultArray = wsAssignAttributesBatchResults.getWsGroups();

      for (WsGroup wsGroup : wsGroupsResultArray) {
        System.out.println(ToStringBuilder.reflectionToString(
            wsGroup));
      }
      
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
