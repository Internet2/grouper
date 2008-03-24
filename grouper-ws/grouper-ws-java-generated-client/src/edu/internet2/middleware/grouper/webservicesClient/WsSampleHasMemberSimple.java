/**
 *
 */
package edu.internet2.middleware.grouper.webservicesClient;

import edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.HasMemberSimple;
import edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.WsHasMemberResult;
import edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.WsHasMemberResults;
import edu.internet2.middleware.grouper.webservicesClient.util.GeneratedClientSettings;
import edu.internet2.middleware.grouper.ws.samples.WsSampleGenerated;
import edu.internet2.middleware.grouper.ws.samples.WsSampleGeneratedType;

import org.apache.axis2.Constants;
import org.apache.axis2.client.Options;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.axis2.transport.http.HttpTransportProperties;

import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * @author mchyzer
 *
 */
public class WsSampleHasMemberSimple implements WsSampleGenerated {
    /**
     * @param args
     */
    public static void main(String[] args) {
        hasMember(WsSampleGeneratedType.SOAP);
    }

    /**
     * @see edu.internet2.middleware.grouper.ws.samples.WsSampleGenerated#executeSample(edu.internet2.middleware.grouper.ws.samples.WsSampleGeneratedType)
     */
    public void executeSample(WsSampleGeneratedType wsSampleGeneratedType) {
        hasMember(wsSampleGeneratedType);
    }

    /**
     * @param wsSampleGeneratedType can run as soap or xml/http
     */
    public static void hasMember(WsSampleGeneratedType wsSampleGeneratedType) {
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

            HasMemberSimple hasMemberSimple = HasMemberSimple.class.newInstance();

            //version, e.g. v1_3_000
            hasMemberSimple.setClientVersion(GeneratedClientSettings.VERSION);

            // set the act as id
            hasMemberSimple.setActAsSubjectId("GrouperSystem");
            hasMemberSimple.setActAsSubjectIdentifier("");

            // check all
            hasMemberSimple.setMemberFilter("All");

            hasMemberSimple.setGroupName("aStem:aGroup");
            hasMemberSimple.setGroupUuid("");

            hasMemberSimple.setSubjectId("GrouperSystem");
            hasMemberSimple.setSubjectIdentifier("");

            WsHasMemberResults wsHasMemberResults = stub.hasMemberSimple(hasMemberSimple)
                                                        .get_return();

            System.out.println(ToStringBuilder.reflectionToString(
                    wsHasMemberResults));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
