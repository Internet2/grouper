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
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;

import edu.internet2.middleware.grouper.webservicesClient.util.GeneratedClientSettings;
import edu.internet2.middleware.grouper.ws.samples.types.WsSampleGenerated;
import edu.internet2.middleware.grouper.ws.samples.types.WsSampleGeneratedType;
import edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.AttributeDefNameSave;
import edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.AttributeDefNameSaveResponse;
import edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsAttributeDefName;
import edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsAttributeDefNameSaveResult;
import edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsAttributeDefNameSaveResults;
import edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsAttributeDefNameToSave;


/**
 * @author mchyzer
 *
 */
public class WsSampleAttributeDefNameSave implements WsSampleGenerated {
    /**
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        attributeDefNameSave(WsSampleGeneratedType.soap);
    }

    /**
     * @see edu.internet2.middleware.grouper.ws.samples.types.WsSampleGenerated#executeSample(edu.internet2.middleware.grouper.ws.samples.types.WsSampleGeneratedType)
     */
    public void executeSample(WsSampleGeneratedType wsSampleGeneratedType) {
      attributeDefNameSave(wsSampleGeneratedType);
    }

    /**
     *
     * @param wsSampleGeneratedType can run as soap or xml/http
     */
    public static void attributeDefNameSave(WsSampleGeneratedType wsSampleGeneratedType) {
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

            AttributeDefNameSave attributeDefNameSave = null;
            AttributeDefNameSaveResponse attributeDefNameSaveResponse = null;
            WsAttributeDefNameSaveResults wsAttributeDefNameSaveResults = null;

            attributeDefNameSave = AttributeDefNameSave.class.newInstance();

            //version, e.g. v1_3_000
            attributeDefNameSave.setClientVersion(GeneratedClientSettings.VERSION);
            
            {
              WsAttributeDefNameToSave wsAttributeDefNameToSave = new WsAttributeDefNameToSave();

              //The attribute def name to save
              WsAttributeDefName wsAttributeDefName = new WsAttributeDefName();
              
              wsAttributeDefName.setAttributeDefName("test:testAttributeAssignDefNameDef");
              wsAttributeDefName.setName("test:testAttributeAssignDefNameToSave1_" + wsSampleGeneratedType);
              wsAttributeDefName.setDisplayExtension("My new attribute def name to save 1 " + wsSampleGeneratedType);
              wsAttributeDefName.setDescription("This is a description 1 " + wsSampleGeneratedType);
              
              wsAttributeDefNameToSave.setWsAttributeDefName(wsAttributeDefName);
              
              attributeDefNameSave.addWsAttributeDefNameToSaves(wsAttributeDefNameToSave);
            }
            
            {
              WsAttributeDefNameToSave wsAttributeDefNameToSave = new WsAttributeDefNameToSave();

              //The attribute def name to save
              WsAttributeDefName wsAttributeDefName = new WsAttributeDefName();
              
              wsAttributeDefName.setAttributeDefName("test:testAttributeAssignDefNameDef");
              wsAttributeDefName.setName("test:testAttributeAssignDefNameToSave2_" + wsSampleGeneratedType);
              wsAttributeDefName.setDisplayExtension("My new attribute def name to save 2 " + wsSampleGeneratedType);
              wsAttributeDefName.setDescription("This is a description 2 " + wsSampleGeneratedType);
              
              wsAttributeDefNameToSave.setWsAttributeDefName(wsAttributeDefName);
              
              attributeDefNameSave.addWsAttributeDefNameToSaves(wsAttributeDefNameToSave);
            }
            
            attributeDefNameSaveResponse = stub.attributeDefNameSave(attributeDefNameSave);
            wsAttributeDefNameSaveResults = attributeDefNameSaveResponse.get_return();
            System.out.println(ToStringBuilder.reflectionToString(
                    wsAttributeDefNameSaveResults));
            
            if (!StringUtils.equals("T", 
                wsAttributeDefNameSaveResults.getResultMetadata().getSuccess())) {
              throw new RuntimeException("didnt get success! ");
            }
            
            WsAttributeDefNameSaveResult[] wsAttributeDefNameSaveResultArray = wsAttributeDefNameSaveResults.getResults();

            for (WsAttributeDefNameSaveResult wsAttributeDefNameSaveResult : GeneratedClientSettings.nonNull(
                wsAttributeDefNameSaveResultArray)) {
                System.out.println(ToStringBuilder.reflectionToString(
                    wsAttributeDefNameSaveResult));
            }

            
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
