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
import edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.GetAttributeAssignActions;
import edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.WsAttributeAssignActionTuple;
import edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.WsAttributeDefLookup;
import edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.WsGetAttributeAssignActionsResults;
import edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.WsParam;
import edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.WsSubjectLookup;

/**
 *
 * @author mchyzer
 *
 */
public class WsSampleGetAttributeAssignActions implements WsSampleGenerated {

  /**
   * @param args
   */
  public static void main(String[] args) {
    getAttributeAssignActions(WsSampleGeneratedType.soap);
  }

  /**
   * @see edu.internet2.middleware.grouper.ws.samples.types.WsSampleGenerated#executeSample(edu.internet2.middleware.grouper.ws.samples.types.WsSampleGeneratedType)
   */
  public void executeSample(WsSampleGeneratedType wsSampleGeneratedType) {
    getAttributeAssignActions(wsSampleGeneratedType);
  }

  /**
   * @param wsSampleGeneratedType can run as soap or xml/http
   */
  public static void getAttributeAssignActions(
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

      GetAttributeAssignActions getAttributeAssignActions = GetAttributeAssignActions.class.newInstance();

      // set the act as id
      WsSubjectLookup actAsSubject = WsSubjectLookup.class.newInstance();
      actAsSubject.setSubjectId("GrouperSystem");
      
      getAttributeAssignActions.setActions(new String[]{null});
      getAttributeAssignActions.setActAsSubjectLookup(actAsSubject);

      //version, e.g. v1_6_000
      getAttributeAssignActions.setClientVersion(GeneratedClientSettings.VERSION);
      
      WsAttributeDefLookup wsAttributeDefLookup = WsAttributeDefLookup.class.newInstance();
      wsAttributeDefLookup.setUuid("");
      wsAttributeDefLookup.setName("aStem:permissionDef");

      getAttributeAssignActions.setWsAttributeDefLookups(new WsAttributeDefLookup[]{wsAttributeDefLookup});

      getAttributeAssignActions.setParams(new WsParam[]{null});
      
      WsGetAttributeAssignActionsResults wsGetAttributeAssignmentsResults = stub.getAttributeAssignActions(getAttributeAssignActions)
          .get_return();

      System.out.println(ToStringBuilder.reflectionToString(
          wsGetAttributeAssignmentsResults));

      WsAttributeAssignActionTuple[] wsAttributeAssignResultArray = wsGetAttributeAssignmentsResults.getWsAttributeAssignActionTuples();

      for (WsAttributeAssignActionTuple wsAttributeAssignActionTuple : wsAttributeAssignResultArray) {
        System.out.println(ToStringBuilder.reflectionToString(
            wsAttributeAssignActionTuple));
      }
      
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
