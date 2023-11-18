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
import edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.FindGroups;
import edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.FindGroupsResponse;
import edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.WsFindGroupsResults;
import edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.WsQueryFilter;


/**
 * @author mchyzer
 *
 */
public class WsSampleFindGroups implements WsSampleGenerated {
    /**
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        findGroup(WsSampleGeneratedType.soap);
    }

    /**
     * @see edu.internet2.middleware.grouper.ws.samples.types.WsSampleGenerated#executeSample(edu.internet2.middleware.grouper.ws.samples.types.WsSampleGeneratedType)
     */
    public void executeSample(WsSampleGeneratedType wsSampleGeneratedType) {
        findGroup(wsSampleGeneratedType);
    }

    /**
     *
     * @param wsSampleGeneratedType can run as soap or xml/http
     */
    public static void findGroup(WsSampleGeneratedType wsSampleGeneratedType) {
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

            FindGroups findGroups = null;
            FindGroupsResponse findGroupsResponse = null;
            WsFindGroupsResults wsFindGroupsResults = null;

            findGroups = FindGroups.class.newInstance();

            //version, e.g. v1_3_000
            findGroups.setClientVersion(GeneratedClientSettings.VERSION);

            WsQueryFilter wsQueryFilter = new WsQueryFilter();
            wsQueryFilter.setGroupName("aGr");
            wsQueryFilter.setQueryFilterType("FIND_BY_GROUP_NAME_APPROXIMATE");
            wsQueryFilter.setStemName("aStem");

            findGroups.setWsQueryFilter(wsQueryFilter);

            findGroupsResponse = stub.findGroups(findGroups);
            wsFindGroupsResults = findGroupsResponse.get_return();
            System.out.println(ToStringBuilder.reflectionToString(
                    wsFindGroupsResults));
            System.out.println(ToStringBuilder.reflectionToString(
                    wsFindGroupsResults.getGroupResults()[0]));
            
            if (!StringUtils.equals("T", 
                wsFindGroupsResults.getResultMetadata().getSuccess())) {
              throw new RuntimeException("didnt get success! ");
            }
            
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
