/**
 *
 */
package edu.internet2.middleware.grouper.webservicesClient;

import edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.GetMembershipsLite;
import edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.WsGetMembershipsResults;
import edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.WsMembership;
import edu.internet2.middleware.grouper.webservicesClient.util.GeneratedClientSettings;
import edu.internet2.middleware.grouper.ws.samples.types.WsSampleGenerated;
import edu.internet2.middleware.grouper.ws.samples.types.WsSampleGeneratedType;

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
public class WsSampleGetMembershipsLite implements WsSampleGenerated {
    /**
     * @see edu.internet2.middleware.grouper.ws.samples.types.WsSampleGenerated#executeSample(edu.internet2.middleware.grouper.ws.samples.types.WsSampleGeneratedType)
     */
    public void executeSample(WsSampleGeneratedType wsSampleGeneratedType) {
        getMembershipsLite(wsSampleGeneratedType);
    }

    /**
     * @param wsSampleGeneratedType can run as soap or xml/http
     */
    public static void getMembershipsLite(
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

            GetMembershipsLite getMembershipsLite = GetMembershipsLite.class.newInstance();

            //version, e.g. v1_3_000
            getMembershipsLite.setClientVersion(GeneratedClientSettings.VERSION);

            // set the act as id
            getMembershipsLite.setActAsSubjectId("GrouperSystem");

            getMembershipsLite.setGroupName("aStem:aGroup");
            getMembershipsLite.setGroupUuid("");
            getMembershipsLite.setMembershipFilter("All");
            getMembershipsLite.setIncludeSubjectDetail("true");

            WsGetMembershipsResults wsGetMembershipsResults = stub.getMembershipsLite(getMembershipsLite)
                                                                  .get_return();

            System.out.println(ToStringBuilder.reflectionToString(
                    wsGetMembershipsResults));

            WsMembership[] wsMembershipArray = wsGetMembershipsResults.getResults();

            for (WsMembership wsMemberships : wsMembershipArray) {
                System.out.println(ToStringBuilder.reflectionToString(
                        wsMemberships));
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        getMembershipsLite(WsSampleGeneratedType.soap);
    }
}
