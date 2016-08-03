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
import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.params.HttpClientParams;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;

import edu.internet2.middleware.grouper.webservicesClient.util.GeneratedClientSettings;
import edu.internet2.middleware.grouper.ws.samples.types.WsSampleGenerated;
import edu.internet2.middleware.grouper.ws.samples.types.WsSampleGeneratedType;
import edu.internet2.middleware.grouper.ws.soap_v2_3.xsd.ExternalSubjectSave;
import edu.internet2.middleware.grouper.ws.soap_v2_3.xsd.WsExternalSubject;
import edu.internet2.middleware.grouper.ws.soap_v2_3.xsd.WsExternalSubjectAttribute;
import edu.internet2.middleware.grouper.ws.soap_v2_3.xsd.WsExternalSubjectLookup;
import edu.internet2.middleware.grouper.ws.soap_v2_3.xsd.WsExternalSubjectSaveResult;
import edu.internet2.middleware.grouper.ws.soap_v2_3.xsd.WsExternalSubjectSaveResults;
import edu.internet2.middleware.grouper.ws.soap_v2_3.xsd.WsExternalSubjectToSave;
import edu.internet2.middleware.grouper.ws.soap_v2_3.xsd.WsSubjectLookup;


/**
 *
 * @author mchyzer
 *
 */
public class WsSampleExternalSubjectSave implements WsSampleGenerated {
    /**
     * @param args
     */
    public static void main(String[] args) {
        externalSubjectSave(WsSampleGeneratedType.soap);
    }

    /**
     * @see edu.internet2.middleware.grouper.ws.samples.types.WsSampleGenerated#executeSample(edu.internet2.middleware.grouper.ws.samples.types.WsSampleGeneratedType)
     */
    public void executeSample(WsSampleGeneratedType wsSampleGeneratedType) {
        externalSubjectSave(wsSampleGeneratedType);
    }

    /**
     * @param wsSampleGeneratedType can run as soap or xml/http
     */
    public static void externalSubjectSave(WsSampleGeneratedType wsSampleGeneratedType) {
        try {
            //URL, e.g. http://localhost:8091/grouper-ws/services/GrouperService
            GrouperServiceStub stub = new GrouperServiceStub(GeneratedClientSettings.URL);
            Options options = stub._getServiceClient().getOptions();
            HttpTransportProperties.Authenticator auth = new HttpTransportProperties.Authenticator();
            auth.setUsername(GeneratedClientSettings.USER);
            auth.setPassword(GeneratedClientSettings.PASS);
            auth.setPreemptiveAuthentication(true);

            HttpClientParams.getDefaultParams().setParameter(
                HttpClientParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler(0, false));
            
            options.setProperty(HTTPConstants.AUTHENTICATE, auth);
            options.setProperty(HTTPConstants.SO_TIMEOUT, new Integer(3600000));
            options.setProperty(HTTPConstants.CONNECTION_TIMEOUT,
                new Integer(3600000));

            ExternalSubjectSave externalSubjectSave = ExternalSubjectSave.class.newInstance();

            //version, e.g. v1_3_000
            externalSubjectSave.setClientVersion(GeneratedClientSettings.VERSION);

            // set the act as id
            WsSubjectLookup actAsSubject = WsSubjectLookup.class.newInstance();
            actAsSubject.setSubjectId("GrouperSystem");
            externalSubjectSave.setActAsSubjectLookup(actAsSubject);

            WsExternalSubjectToSave wsExternalSubjectToSave = WsExternalSubjectToSave.class.newInstance();
            WsExternalSubject wsExternalSubject = WsExternalSubject.class.newInstance();
            
            wsExternalSubject.setIdentifier("b_ident@c.d");
            wsExternalSubject.setName("Another Name");
            wsExternalSubject.setEmail("b@c.d");
            WsExternalSubjectAttribute wsExternalSubjectAttribute = new WsExternalSubjectAttribute();
            wsExternalSubjectAttribute.setAttributeSystemName("jabber");
            wsExternalSubjectAttribute.setAttributeValue("b_jabber@c.d");
            wsExternalSubject.setWsExternalSubjectAttributes(new WsExternalSubjectAttribute[]{wsExternalSubjectAttribute});
            wsExternalSubjectToSave.setWsExternalSubject(wsExternalSubject);

            WsExternalSubjectLookup wsExternalSubjectLookup = new WsExternalSubjectLookup();
            wsExternalSubjectLookup.setIdentifier("b_ident@c.d");
            wsExternalSubjectToSave.setWsExternalSubjectLookup(wsExternalSubjectLookup);

            externalSubjectSave.setWsExternalSubjectToSaves(new WsExternalSubjectToSave[] {wsExternalSubjectToSave});
            
            WsExternalSubjectSaveResults wsExternalSubjectSaveResults = stub.externalSubjectSave(externalSubjectSave)
                                                        .get_return();
            System.out.println(ToStringBuilder.reflectionToString(
                    wsExternalSubjectSaveResults));

            WsExternalSubjectSaveResult[] wsExternalSubjectSaveResultArray = wsExternalSubjectSaveResults.getResults();

            for (WsExternalSubjectSaveResult wsExternalSubjectSaveResult : GeneratedClientSettings.nonNull(
                    wsExternalSubjectSaveResultArray)) {
                System.out.println(ToStringBuilder.reflectionToString(
                        wsExternalSubjectSaveResult));
            }
            if (!StringUtils.equals("T", 
                wsExternalSubjectSaveResults.getResultMetadata().getSuccess())) {
              throw new RuntimeException("didnt get success! ");
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
