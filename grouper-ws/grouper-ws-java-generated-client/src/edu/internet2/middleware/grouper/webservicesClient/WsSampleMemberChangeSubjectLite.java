/**
 *
 */
package edu.internet2.middleware.grouper.webservicesClient;

import org.apache.axis2.client.Options;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.axis2.transport.http.HttpTransportProperties;
import org.apache.commons.httpclient.params.HttpClientParams;
import org.apache.commons.lang.builder.ToStringBuilder;

import edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.MemberChangeSubjectLite;
import edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.WsMemberChangeSubjectLiteResult;
import edu.internet2.middleware.grouper.webservicesClient.util.GeneratedClientSettings;
import edu.internet2.middleware.grouper.ws.samples.types.WsSampleGenerated;
import edu.internet2.middleware.grouper.ws.samples.types.WsSampleGeneratedType;


/**
 *
 * @author mchyzer
 *
 */
public class WsSampleMemberChangeSubjectLite implements WsSampleGenerated {
    /**
     * @see edu.internet2.middleware.grouper.ws.samples.types.WsSampleGenerated#executeSample(edu.internet2.middleware.grouper.ws.samples.types.WsSampleGeneratedType)
     */
    public void executeSample(WsSampleGeneratedType wsSampleGeneratedType) {
        memberChangeSubjectLite(wsSampleGeneratedType);
    }

    /**
     * @param wsSampleGeneratedType can run as soap or xml/http
     */
    @SuppressWarnings("deprecation")
    public static void memberChangeSubjectLite(
        WsSampleGeneratedType wsSampleGeneratedType) {
        try {
            //URL, e.g. http://localhost:8091/grouper-ws/services/GrouperService
            GrouperServiceStub stub = new GrouperServiceStub(GeneratedClientSettings.URL);
            
            org.apache.commons.httpclient.DefaultMethodRetryHandler retryhandler = 
              new org.apache.commons.httpclient.DefaultMethodRetryHandler();
            retryhandler.setRequestSentRetryEnabled(false);
            HttpClientParams.getDefaultParams().setParameter(HttpClientParams.RETRY_HANDLER, retryhandler);
            
            Options options = stub._getServiceClient().getOptions();
            HttpTransportProperties.Authenticator auth = new HttpTransportProperties.Authenticator();
            auth.setUsername(GeneratedClientSettings.USER);
            auth.setPassword(GeneratedClientSettings.PASS);
            auth.setPreemptiveAuthentication(true);

            options.setProperty(HTTPConstants.AUTHENTICATE, auth);
            options.setProperty(HTTPConstants.SO_TIMEOUT, new Integer(3600000));
            options.setProperty(HTTPConstants.CONNECTION_TIMEOUT,
                new Integer(3600000));

            MemberChangeSubjectLite memberChangeSubjectLite = MemberChangeSubjectLite.class.newInstance();

            //version, e.g. v1_3_000
            memberChangeSubjectLite.setClientVersion(GeneratedClientSettings.VERSION);

            // set the act as id
            memberChangeSubjectLite.setActAsSubjectId("GrouperSystem");

            memberChangeSubjectLite.setOldSubjectId("test.subject.0");
            memberChangeSubjectLite.setNewSubjectId("test.subject.1");

            memberChangeSubjectLite.setIncludeSubjectDetail("T");
            memberChangeSubjectLite.setSubjectAttributeNames("loginid,description");
            
            WsMemberChangeSubjectLiteResult wsMemberChangeSubjectLiteResult = 
              stub.memberChangeSubjectLite(memberChangeSubjectLite).get_return();

            System.out.println(ToStringBuilder.reflectionToString(
                wsMemberChangeSubjectLiteResult));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        memberChangeSubjectLite(WsSampleGeneratedType.soap);
    }
}
