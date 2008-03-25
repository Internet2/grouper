/**
 *
 */
package edu.internet2.middleware.grouper.webservicesClient;

import org.apache.axis2.Constants;
import org.apache.axis2.client.Options;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.axis2.transport.http.HttpTransportProperties;
import org.apache.commons.lang.builder.ToStringBuilder;

import edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.GetGroups;
import edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.WsGetGroupsResults;
import edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.WsGroup;
import edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.WsSubjectLookup;
import edu.internet2.middleware.grouper.webservicesClient.util.GeneratedClientSettings;
import edu.internet2.middleware.grouper.ws.samples.types.WsSampleGenerated;
import edu.internet2.middleware.grouper.ws.samples.types.WsSampleGeneratedType;


/**
 * @author mchyzer
 *
 */
public class WsSampleGetGroups implements WsSampleGenerated {
    /**
     * @param args
     */
    public static void main(String[] args) {
        getGroups(WsSampleGeneratedType.soap);
    }

    /**
     * @see edu.internet2.middleware.grouper.ws.samples.types.WsSampleGenerated#executeSample(edu.internet2.middleware.grouper.ws.samples.types.WsSampleGeneratedType)
     */
    public void executeSample(WsSampleGeneratedType wsSampleGeneratedType) {
        getGroups(wsSampleGeneratedType);
    }

    /**
     * @param wsSampleGeneratedType can run as soap or xml/http
     */
    public static void getGroups(WsSampleGeneratedType wsSampleGeneratedType) {
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
            actAsSubject.setSubjectId("GrouperSystem");
            getGroups.setActAsSubjectLookup(actAsSubject);

            //version, e.g. v1_3_000
            getGroups.setClientVersion(GeneratedClientSettings.VERSION);

            //check all
            getGroups.setMemberFilter("All");

            WsSubjectLookup wsSubjectLookup = WsSubjectLookup.class.newInstance();
            wsSubjectLookup.setSubjectId("GrouperSystem");
            getGroups.setSubjectLookup(wsSubjectLookup);

            WsGetGroupsResults wsGetGroupsResults = stub.getGroups(getGroups)
                                                        .get_return();

            System.out.println(ToStringBuilder.reflectionToString(
                    wsGetGroupsResults));

            WsGroup[] results = wsGetGroupsResults.getResults();

            if (results != null) {
                for (WsGroup wsGroup : results) {
                    System.out.println(ToStringBuilder.reflectionToString(
                            wsGroup));
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
