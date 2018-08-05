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
import edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.ExternalSubjectDelete;
import edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.WsExternalSubjectDeleteResult;
import edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.WsExternalSubjectDeleteResults;
import edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.WsExternalSubjectLookup;
import edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.WsSubjectLookup;


/**
 *
 * @author mchyzer
 *
 */
public class WsSampleExternalSubjectDelete implements WsSampleGenerated {
    /**
     * @param args
     */
    public static void main(String[] args) {
        externalSubjectDelete(WsSampleGeneratedType.soap);
    }

    /**
     * @see edu.internet2.middleware.grouper.ws.samples.types.WsSampleGenerated#executeSample(edu.internet2.middleware.grouper.ws.samples.types.WsSampleGeneratedType)
     */
    public void executeSample(WsSampleGeneratedType wsSampleGeneratedType) {
        externalSubjectDelete(wsSampleGeneratedType);
    }

    /**
     * @param wsSampleGeneratedType can run as soap or xml/http
     */
    public static void externalSubjectDelete(WsSampleGeneratedType wsSampleGeneratedType) {
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

            ExternalSubjectDelete externalSubjectDelete = ExternalSubjectDelete.class.newInstance();

            //version, e.g. v1_3_000
            externalSubjectDelete.setClientVersion(GeneratedClientSettings.VERSION);

            // set the act as id
            WsSubjectLookup actAsSubject = WsSubjectLookup.class.newInstance();
            actAsSubject.setSubjectId("GrouperSystem");
            externalSubjectDelete.setActAsSubjectLookup(actAsSubject);

            WsExternalSubjectLookup wsExternalSubjectLookup = WsExternalSubjectLookup.class.newInstance();
            wsExternalSubjectLookup.setIdentifier("a_ident@b.c");
            externalSubjectDelete.setWsExternalSubjectLookups(new WsExternalSubjectLookup[] { wsExternalSubjectLookup });

            WsExternalSubjectDeleteResults wsExternalSubjectDeleteResults = stub.externalSubjectDelete(externalSubjectDelete)
                                                            .get_return();

            System.out.println(ToStringBuilder.reflectionToString(
                    wsExternalSubjectDeleteResults));

            WsExternalSubjectDeleteResult[] wsExternalSubjectDeleteResultArray = wsExternalSubjectDeleteResults.getResults();

            for (WsExternalSubjectDeleteResult wsExternalSubjectDeleteResult : GeneratedClientSettings.nonNull(
                    wsExternalSubjectDeleteResultArray)) {
                System.out.println(ToStringBuilder.reflectionToString(
                        wsExternalSubjectDeleteResult));
            }
            
            if (!StringUtils.equals("T", 
                wsExternalSubjectDeleteResults.getResultMetadata().getSuccess())) {
              throw new RuntimeException("didnt get success! ");
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
