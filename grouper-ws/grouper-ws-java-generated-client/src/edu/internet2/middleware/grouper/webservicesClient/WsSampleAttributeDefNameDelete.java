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
import edu.internet2.middleware.grouper.ws.soap_v2_3.xsd.AttributeDefNameDelete;
import edu.internet2.middleware.grouper.ws.soap_v2_3.xsd.AttributeDefNameDeleteResponse;
import edu.internet2.middleware.grouper.ws.soap_v2_3.xsd.WsAttributeDefNameDeleteResult;
import edu.internet2.middleware.grouper.ws.soap_v2_3.xsd.WsAttributeDefNameDeleteResults;
import edu.internet2.middleware.grouper.ws.soap_v2_3.xsd.WsAttributeDefNameLookup;


/**
 * @author mchyzer
 *
 */
public class WsSampleAttributeDefNameDelete implements WsSampleGenerated {
    /**
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        attributeDefNameDelete(WsSampleGeneratedType.soap);
    }

    /**
     * @see edu.internet2.middleware.grouper.ws.samples.types.WsSampleGenerated#executeSample(edu.internet2.middleware.grouper.ws.samples.types.WsSampleGeneratedType)
     */
    public void executeSample(WsSampleGeneratedType wsSampleGeneratedType) {
      attributeDefNameDelete(wsSampleGeneratedType);
    }

    /**
     *
     * @param wsSampleGeneratedType can run as soap or xml/http
     */
    public static void attributeDefNameDelete(WsSampleGeneratedType wsSampleGeneratedType) {
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

            AttributeDefNameDelete attributeDefNameDelete = null;
            AttributeDefNameDeleteResponse attributeDefNameDeleteResponse = null;
            WsAttributeDefNameDeleteResults wsAttributeDefNameDeleteResults = null;

            attributeDefNameDelete = AttributeDefNameDelete.class.newInstance();

            //version, e.g. v1_3_000
            attributeDefNameDelete.setClientVersion(GeneratedClientSettings.VERSION);

            {
              WsAttributeDefNameLookup wsAttributeDefNameLookup = new WsAttributeDefNameLookup();
              wsAttributeDefNameLookup.setName("test:testAttributeAssignDefNameToDelete1_" + wsSampleGeneratedType);
              attributeDefNameDelete.addWsAttributeDefNameLookups(wsAttributeDefNameLookup);
            }
            
            {
              WsAttributeDefNameLookup wsAttributeDefNameLookup = new WsAttributeDefNameLookup();
              wsAttributeDefNameLookup.setName("test:testAttributeAssignDefNameToDelete2_" + wsSampleGeneratedType);
              attributeDefNameDelete.addWsAttributeDefNameLookups(wsAttributeDefNameLookup);
            }
            
            attributeDefNameDeleteResponse = stub.attributeDefNameDelete(attributeDefNameDelete);
            wsAttributeDefNameDeleteResults = attributeDefNameDeleteResponse.get_return();
            System.out.println(ToStringBuilder.reflectionToString(
                    wsAttributeDefNameDeleteResults));
            
            if (!StringUtils.equals("T", 
                wsAttributeDefNameDeleteResults.getResultMetadata().getSuccess())) {
              throw new RuntimeException("didnt get success! ");
            }

            WsAttributeDefNameDeleteResult[] wsAttributeDefNameDeleteResultArray = wsAttributeDefNameDeleteResults.getResults();

            for (WsAttributeDefNameDeleteResult wsAttributeDefNameDeleteResult : GeneratedClientSettings.nonNull(
                wsAttributeDefNameDeleteResultArray)) {
                System.out.println(ToStringBuilder.reflectionToString(
                    wsAttributeDefNameDeleteResult));
            }


        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
