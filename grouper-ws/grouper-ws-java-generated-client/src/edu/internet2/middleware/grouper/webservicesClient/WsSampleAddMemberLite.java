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
import org.apache.commons.lang.builder.ToStringStyle;

import edu.internet2.middleware.grouper.webservicesClient.util.GeneratedClientSettings;
import edu.internet2.middleware.grouper.ws.samples.types.WsSampleGenerated;
import edu.internet2.middleware.grouper.ws.samples.types.WsSampleGeneratedType;
import edu.internet2.middleware.grouper.ws.soap_v2_3.xsd.AddMemberLite;
import edu.internet2.middleware.grouper.ws.soap_v2_3.xsd.WsAddMemberLiteResult;


/**
 *
 * @author mchyzer
 *
 */
public class WsSampleAddMemberLite implements WsSampleGenerated {
    /**
     * @param wsSampleGeneratedType if SOAP or XML/HTTP
     */
    public static void addMemberLite(
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

            AddMemberLite addMemberLite = AddMemberLite.class.newInstance();

            //version, e.g. v1_3_000
            addMemberLite.setClientVersion(GeneratedClientSettings.VERSION);

            addMemberLite.setGroupName("aStem:aGroup");

//            addMemberLite.setGroupUuid("");
//
//            addMemberLite.setSubjectId("");
            addMemberLite.setSubjectSourceId("jdbc");
            addMemberLite.setSubjectIdentifier("id.test.subject.0");

            // set the act as id
            addMemberLite.setActAsSubjectId("GrouperSystem");

//            addMemberLite.setActAsSubjectSourceId("");
//            addMemberLite.setActAsSubjectIdentifier("");
//            addMemberLite.setFieldName("");
//            addMemberLite.setIncludeGroupDetail("");
//            addMemberLite.setIncludeSubjectDetail("");
//            addMemberLite.setSubjectAttributeNames("");
//            addMemberLite.setParamName0("");
//            addMemberLite.setParamValue0("");
//            addMemberLite.setParamName1("");
//            addMemberLite.setParamValue1("");

            WsAddMemberLiteResult wsAddMemberLiteResult = stub.addMemberLite(addMemberLite)
                                                              .get_return();

            System.out.println(ToStringBuilder.reflectionToString(
                    wsAddMemberLiteResult, ToStringStyle.MULTI_LINE_STYLE));
            System.out.println(ToStringBuilder.reflectionToString(
                    wsAddMemberLiteResult.getResultMetadata(),
                    ToStringStyle.MULTI_LINE_STYLE));
            System.out.println(ToStringBuilder.reflectionToString(
                    wsAddMemberLiteResult.getSubjectAttributeNames(),
                    ToStringStyle.MULTI_LINE_STYLE));
            System.out.println(ToStringBuilder.reflectionToString(
                    wsAddMemberLiteResult.getWsGroupAssigned(),
                    ToStringStyle.MULTI_LINE_STYLE));
            System.out.println(ToStringBuilder.reflectionToString(
                    wsAddMemberLiteResult.getWsSubject(),
                    ToStringStyle.MULTI_LINE_STYLE));

            if (!StringUtils.equals("T", 
                wsAddMemberLiteResult.getResultMetadata().getSuccess())) {
              throw new RuntimeException("didnt get success! ");
            }

            
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        addMemberLite(WsSampleGeneratedType.soap);
    }

    /**
     * @see edu.internet2.middleware.grouper.ws.samples.types.WsSampleGenerated#executeSample(edu.internet2.middleware.grouper.ws.samples.types.WsSampleGeneratedType)
     */
    public void executeSample(WsSampleGeneratedType wsSampleGeneratedType) {
        addMemberLite(wsSampleGeneratedType);
    }
}
