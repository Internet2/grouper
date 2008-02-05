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
import edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.WsGetGroupsResult;
import edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.WsGetGroupsResults;
import edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.WsSubjectLookup;


/**
 * @author mchyzer
 *
 */
public class RunGrouperServiceGetGroups {
    /**
     * @param args
     */
    public static void main(String[] args) {
        getGroups();
    }

    /**
     *
     */
    public static void getGroups() {
        try {
            GrouperServiceStub stub = new GrouperServiceStub(
                    "http://localhost:8091/grouper-ws/services/GrouperService");
            Options options = stub._getServiceClient().getOptions();
            HttpTransportProperties.Authenticator auth = new HttpTransportProperties.Authenticator();
            auth.setUsername("GrouperSystem");
            auth.setPassword("pass");

            options.setProperty(HTTPConstants.AUTHENTICATE, auth);
            options.setProperty(HTTPConstants.SO_TIMEOUT, new Integer(3600000));
            options.setProperty(HTTPConstants.CONNECTION_TIMEOUT,
                new Integer(3600000));

                        options.setProperty(Constants.Configuration.ENABLE_REST,
                        		Constants.VALUE_TRUE);
            GetGroups getGroups = GetGroups.class.newInstance();

            // set the act as id
            WsSubjectLookup actAsSubject = WsSubjectLookup.class.newInstance();
            actAsSubject.setSubjectId("GrouperSystem");
            getGroups.setActAsSubjectLookup(actAsSubject);

            // check all
            getGroups.setMemberFilter("All");

            WsSubjectLookup wsSubjectLookup = WsSubjectLookup.class.newInstance();
            wsSubjectLookup.setSubjectId("GrouperSystem");
            getGroups.setSubjectLookup(wsSubjectLookup);

            WsGetGroupsResults wsGetGroupsResults = stub.getGroups(getGroups).get_return();

            System.out.println(ToStringBuilder.reflectionToString(
                    wsGetGroupsResults));

            WsGetGroupsResult[] results = wsGetGroupsResults.getResults();

            if (results != null) {
                for (WsGetGroupsResult wsGetGroupsResult : results) {
                    System.out.println(ToStringBuilder.reflectionToString(
                            wsGetGroupsResult));
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
