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
import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.params.HttpClientParams;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;

import edu.internet2.middleware.grouper.webservicesClient.util.GeneratedClientSettings;
import edu.internet2.middleware.grouper.ws.soap_v2_3.xsd.MemberChangeSubject;
import edu.internet2.middleware.grouper.ws.soap_v2_3.xsd.WsMemberChangeSubject;
import edu.internet2.middleware.grouper.ws.soap_v2_3.xsd.WsMemberChangeSubjectResults;
import edu.internet2.middleware.grouper.ws.soap_v2_3.xsd.WsSubjectLookup;


/**
 *
 * @author mchyzer
 *
 */
public class WsSampleMemberChangeSubject implements WsSampleGenerated {
    /**
     * @see edu.internet2.middleware.grouper.ws.samples.types.WsSampleGenerated#executeSample(edu.internet2.middleware.grouper.ws.samples.types.WsSampleGeneratedType)
     */
    public void executeSample(WsSampleGeneratedType wsSampleGeneratedType) {
        memberChangeSubject(wsSampleGeneratedType);
    }

    /**
     * @param wsSampleGeneratedType can run as soap or xml/http
     */
    @SuppressWarnings("deprecation")
    public static void memberChangeSubject(
        WsSampleGeneratedType wsSampleGeneratedType) {
        try {
            //URL, e.g. http://localhost:8091/grouper-ws/services/GrouperService
            GrouperServiceStub stub = new GrouperServiceStub(GeneratedClientSettings.URL);
            
            HttpClientParams.getDefaultParams().setParameter(
                HttpClientParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler(0, false));
            
            Options options = stub._getServiceClient().getOptions();
            HttpTransportProperties.Authenticator auth = new HttpTransportProperties.Authenticator();
            auth.setUsername(GeneratedClientSettings.USER);
            auth.setPassword(GeneratedClientSettings.PASS);
            auth.setPreemptiveAuthentication(true);

            options.setProperty(HTTPConstants.AUTHENTICATE, auth);
            options.setProperty(HTTPConstants.SO_TIMEOUT, new Integer(3600000));
            options.setProperty(HTTPConstants.CONNECTION_TIMEOUT,
                new Integer(3600000));

            MemberChangeSubject memberChangeSubject = new MemberChangeSubject();

            //version, e.g. v1_3_000
            memberChangeSubject.setClientVersion(GeneratedClientSettings.VERSION);

            WsMemberChangeSubject[] wsMemberChangeSubjects = new WsMemberChangeSubject[2];
            
            memberChangeSubject.setWsMemberChangeSubjects(wsMemberChangeSubjects);
            
            // set the act as id
            wsMemberChangeSubjects[0] = new WsMemberChangeSubject();

            WsSubjectLookup oldSubject0 = new WsSubjectLookup();
            oldSubject0.setSubjectIdentifier("id.test.subject.0");
            wsMemberChangeSubjects[0].setOldSubjectLookup(oldSubject0);

            WsSubjectLookup newSubject0 = new WsSubjectLookup();
            newSubject0.setSubjectId("test.subject.1");
            wsMemberChangeSubjects[0].setNewSubjectLookup(newSubject0);

            wsMemberChangeSubjects[1] = new WsMemberChangeSubject();

            WsSubjectLookup oldSubject1 = new WsSubjectLookup();
            oldSubject1.setSubjectIdentifier("id.test.subject.2");
            wsMemberChangeSubjects[1].setOldSubjectLookup(oldSubject1);

            WsSubjectLookup newSubject1 = new WsSubjectLookup();
            newSubject1.setSubjectId("test.subject.3");
            wsMemberChangeSubjects[1].setNewSubjectLookup(newSubject1);
            
            wsMemberChangeSubjects[1].setDeleteOldMember("F");

            WsSubjectLookup actAsSubject = new WsSubjectLookup();
            newSubject1.setSubjectId("GrouperSystem");
            memberChangeSubject.setActAsSubjectLookup(actAsSubject);

            memberChangeSubject.setTxType("");
            memberChangeSubject.setIncludeSubjectDetail("T");
            memberChangeSubject.setSubjectAttributeNames(new String[]{"loginid,description"});
            
            WsMemberChangeSubjectResults wsMemberChangeSubjectResults = 
              stub.memberChangeSubject(memberChangeSubject).get_return();

            String resultString = ToStringBuilder.reflectionToString(
                wsMemberChangeSubjectResults);
            
            if (!"T".equals(wsMemberChangeSubjectResults.getResultMetadata().getSuccess())) {
              throw new RuntimeException("Didnt get success: " + resultString);
            }
            
            if (!StringUtils.equals("T", 
                wsMemberChangeSubjectResults.getResultMetadata().getSuccess())) {
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
        memberChangeSubject(WsSampleGeneratedType.soap);
    }
}
