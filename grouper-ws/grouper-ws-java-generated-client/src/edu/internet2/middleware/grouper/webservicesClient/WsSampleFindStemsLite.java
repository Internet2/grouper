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
import edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.FindStemsLite;
import edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.FindStemsLiteResponse;
import edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.WsFindStemsResults;
import edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.WsStem;


/**
 * find stems lite
 * @author mchyzer
 *
 */
public class WsSampleFindStemsLite implements WsSampleGenerated {
    /**
     * @param args
     */
    public static void main(String[] args) {
        findStems(WsSampleGeneratedType.soap);
    }

    /**
     * @see edu.internet2.middleware.grouper.ws.samples.types.WsSampleGenerated#executeSample(edu.internet2.middleware.grouper.ws.samples.types.WsSampleGeneratedType)
     */
    public void executeSample(WsSampleGeneratedType wsSampleGeneratedType) {
        findStems(wsSampleGeneratedType);
    }

    /**
     *
     * @param wsSampleGeneratedType can run as soap or xml/http
     */
    public static void findStems(WsSampleGeneratedType wsSampleGeneratedType) {
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

            FindStemsLite findStemsLite = null;
            FindStemsLiteResponse findStemsLiteResponse = null;
            WsFindStemsResults wsFindStemsResults = null;
            //            options.setProperty(Constants.Configuration.ENABLE_REST,
            //                Constants.VALUE_TRUE);
            findStemsLite = FindStemsLite.class.newInstance();

            findStemsLite.setActAsSubjectId("");
            findStemsLite.setActAsSubjectIdentifier("");
            findStemsLite.setActAsSubjectSourceId("");
            //version, e.g. v1_3_000
            findStemsLite.setClientVersion(GeneratedClientSettings.VERSION);
            findStemsLite.setStemAttributeName("");
            findStemsLite.setStemAttributeValue("");
            findStemsLite.setParentStemName("");
            findStemsLite.setStemUuid("");
            findStemsLite.setStemName("aSte");
            findStemsLite.setStemQueryFilterType(
                "FIND_BY_STEM_NAME_APPROXIMATE");

            System.out.println("\n\nQUERY BY STEM NAME: ");
            findStemsLiteResponse = stub.findStemsLite(findStemsLite);
            wsFindStemsResults = findStemsLiteResponse.get_return();
            System.out.println(ToStringBuilder.reflectionToString(
                    wsFindStemsResults));

            if (wsFindStemsResults.getStemResults() != null) {
                for (WsStem wsStemResult : wsFindStemsResults.getStemResults()) {
                    System.out.println((wsStemResult == null) ? null
                                                              : ToStringBuilder.reflectionToString(
                            wsStemResult));
                }
            }
            
            if (!StringUtils.equals("T", 
                wsFindStemsResults.getResultMetadata().getSuccess())) {
              throw new RuntimeException("didnt get success! ");
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
