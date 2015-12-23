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
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;

import edu.internet2.middleware.grouper.webservicesClient.util.GeneratedClientSettings;
import edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.AssignAttributeDefNameInheritance;
import edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.AssignAttributeDefNameInheritanceResponse;
import edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.WsAssignAttributeDefNameInheritanceResults;
import edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.WsAttributeDefNameLookup;


/**
 * @author mchyzer
 *
 */
public class WsSampleAssignAttributeDefNameInheritance implements WsSampleGenerated {
    /**
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        assignAttributeDefNameInheritance(WsSampleGeneratedType.soap);
    }

    /**
     * @see edu.internet2.middleware.grouper.ws.samples.types.WsSampleGenerated#executeSample(edu.internet2.middleware.grouper.ws.samples.types.WsSampleGeneratedType)
     */
    public void executeSample(WsSampleGeneratedType wsSampleGeneratedType) {
      assignAttributeDefNameInheritance(wsSampleGeneratedType);
    }

    /**
     *
     * @param wsSampleGeneratedType can run as soap or xml/http
     */
    public static void assignAttributeDefNameInheritance(WsSampleGeneratedType wsSampleGeneratedType) {
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

            AssignAttributeDefNameInheritance assignAttributeDefNameInheritance = null;
            AssignAttributeDefNameInheritanceResponse assignAttributeDefNameInheritanceResponse = null;
            WsAssignAttributeDefNameInheritanceResults wsAssignAttributeDefNameInheritanceResults = null;

            assignAttributeDefNameInheritance = AssignAttributeDefNameInheritance.class.newInstance();

            //version, e.g. v1_3_000
            assignAttributeDefNameInheritance.setClientVersion(GeneratedClientSettings.VERSION);
            
            //this is the parent of the relation
            {
              WsAttributeDefNameLookup wsAttributeDefNameLookup = new WsAttributeDefNameLookup();
              wsAttributeDefNameLookup.setName("aStem:permissionDefName");
              assignAttributeDefNameInheritance.setWsAttributeDefNameLookup(wsAttributeDefNameLookup);
            }
            
            //we are doing an assignment
            assignAttributeDefNameInheritance.setAssign("T");
            
            {
              //these are the children of the relation
              WsAttributeDefNameLookup relatedAttributeDefNameLookup = new WsAttributeDefNameLookup();
              relatedAttributeDefNameLookup.setName("aStem:permissionDefName3");
              assignAttributeDefNameInheritance.addRelatedWsAttributeDefNameLookups(relatedAttributeDefNameLookup);
              relatedAttributeDefNameLookup = new WsAttributeDefNameLookup();
              relatedAttributeDefNameLookup.setName("aStem:permissionDefName4");
              assignAttributeDefNameInheritance.addRelatedWsAttributeDefNameLookups(relatedAttributeDefNameLookup);
            }            
            
            assignAttributeDefNameInheritanceResponse = stub.assignAttributeDefNameInheritance(assignAttributeDefNameInheritance);
            wsAssignAttributeDefNameInheritanceResults = assignAttributeDefNameInheritanceResponse.get_return();
            System.out.println(ToStringBuilder.reflectionToString(
                    wsAssignAttributeDefNameInheritanceResults));
            
            if (!StringUtils.equals("T", 
                wsAssignAttributeDefNameInheritanceResults.getResultMetadata().getSuccess())) {
              throw new RuntimeException("didnt get success! ");
            }
            
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
