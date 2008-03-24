/**
 *
 */
package edu.internet2.middleware.grouper.webservicesClient;

import edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.GetMembersSimple;
import edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.WsGetMembersResults;
import edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.WsSubject;
import edu.internet2.middleware.grouper.webservicesClient.util.GeneratedClientSettings;
import edu.internet2.middleware.grouper.ws.samples.WsSampleGenerated;
import edu.internet2.middleware.grouper.ws.samples.WsSampleGeneratedType;

import org.apache.axis2.Constants;
import org.apache.axis2.client.Options;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.axis2.transport.http.HttpTransportProperties;

import org.apache.commons.lang.builder.ToStringBuilder;


/**
 *
 * @author mchyzer
 *
 */
public class WsSampleGetMembersSimple implements WsSampleGenerated {
    /**
     * @see edu.internet2.middleware.grouper.ws.samples.WsSampleGenerated#executeSample(edu.internet2.middleware.grouper.ws.samples.WsSampleGeneratedType)
     */
    public void executeSample(WsSampleGeneratedType wsSampleGeneratedType) {
        getMembersSimple(wsSampleGeneratedType);
    }

    /**
     * @param wsSampleGeneratedType can run as soap or xml/http
     */
    public static void getMembersSimple(
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

            if (WsSampleGeneratedType.XML_HTTP.equals(wsSampleGeneratedType)) {
                options.setProperty(Constants.Configuration.ENABLE_REST,
                    Constants.VALUE_TRUE);
            }

            GetMembersSimple getMembersSimple = GetMembersSimple.class.newInstance();

            //version, e.g. v1_3_000
            getMembersSimple.setClientVersion(GeneratedClientSettings.VERSION);

            // set the act as id
            getMembersSimple.setActAsSubjectId("GrouperSystem");

            getMembersSimple.setGroupName("aStem:aGroup");
            getMembersSimple.setGroupUuid("");
            getMembersSimple.setMemberFilter("All");
            getMembersSimple.setIncludeSubjectDetail("true");

            WsGetMembersResults wsGetMembersResults = stub.getMembersSimple(getMembersSimple)
                                                          .get_return();

            System.out.println(ToStringBuilder.reflectionToString(
                    wsGetMembersResults));

            WsSubject[] wsSubjectArray = wsGetMembersResults.getResults();

            for (WsSubject wsSubject : wsSubjectArray) {
                System.out.println(ToStringBuilder.reflectionToString(wsSubject));
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        getMembersSimple(WsSampleGeneratedType.SOAP);
    }
}
