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
import edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.GetGroups;
import edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.WsGetGroupsResult;
import edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.WsGetGroupsResults;
import edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.WsGroup;
import edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.WsParam;
import edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.WsSubjectLookup;


/**
 * @author mchyzer
 *
 */
public class WsSampleGetGroupsAdmins implements WsSampleGenerated {
    /**
     * @param args
     */
    public static void main(String[] args) {
        getGroupsAdmins(WsSampleGeneratedType.soap);
    }

    /**
     * @see edu.internet2.middleware.grouper.ws.samples.types.WsSampleGenerated#executeSample(edu.internet2.middleware.grouper.ws.samples.types.WsSampleGeneratedType)
     */
    public void executeSample(WsSampleGeneratedType wsSampleGeneratedType) {
        getGroupsAdmins(wsSampleGeneratedType);
    }

    /**
     * @param wsSampleGeneratedType can run as soap or xml/http
     */
    public static void getGroupsAdmins(WsSampleGeneratedType wsSampleGeneratedType) {
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

            GetGroups getGroups = GetGroups.class.newInstance();

            //set the act as id
            WsSubjectLookup actAsSubject = WsSubjectLookup.class.newInstance();
            getGroups.setActAsSubjectLookup(actAsSubject);

            //version, e.g. v1_3_000
            getGroups.setClientVersion(GeneratedClientSettings.VERSION);

            //check all
            getGroups.setMemberFilter("All");

            WsSubjectLookup wsSubjectLookup = WsSubjectLookup.class.newInstance();
            wsSubjectLookup.setSubjectId("GrouperSystem");

            WsSubjectLookup wsSubjectLookup2 = WsSubjectLookup.class.newInstance();
            wsSubjectLookup2.setSubjectId("10021368");
            getGroups.setSubjectLookups(new WsSubjectLookup[] {
                    wsSubjectLookup, wsSubjectLookup2
                });

            WsParam param = new WsParam();

            param.setParamName("fieldName");
            param.setParamValue("admins");

            getGroups.setParams(new WsParam[] {param});
            
            getGroups.setIncludeGroupDetail("F");
            getGroups.setIncludeSubjectDetail("F");
            getGroups.setSubjectAttributeNames(new String[]{""});
            
            WsGetGroupsResults wsGetGroupsResults = stub.getGroups(getGroups)
                                                        .get_return();

            System.out.println(ToStringBuilder.reflectionToString(
                    wsGetGroupsResults));

            WsGetGroupsResult[] results = wsGetGroupsResults.getResults();

            if (results != null) {
                for (WsGetGroupsResult result : results) {
                    WsGroup[] wsGroups = result.getWsGroups();

                    if (wsGroups != null) {
                        for (WsGroup wsGroup : wsGroups) {
                            System.out.println(ToStringBuilder.reflectionToString(
                                    wsGroup));
                        }
                    }
                }
            }
            
            if (!StringUtils.equals("T", 
                wsGetGroupsResults.getResultMetadata().getSuccess())) {
              throw new RuntimeException("didnt get success! ");
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
