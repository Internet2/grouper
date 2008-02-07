/**
 *
 */
package edu.internet2.middleware.grouper.webservicesClient;

import edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.GetGroupsSimple;
import edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.WsGetGroupsResult;
import edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.WsGetGroupsResults;

import org.apache.axis2.client.Options;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.axis2.transport.http.HttpTransportProperties;

import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * @author mchyzer
 *
 */
public class RunGrouperServiceGetGroupsSimple {
    /**
     * @param args
     */
    public static void main(String[] args) {
        getGroupsSimple();
    }

    /**
     *
     */
    public static void getGroupsSimple() {
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

            //                        options.setProperty(Constants.Configuration.ENABLE_REST,
            //                        		Constants.VALUE_TRUE);
            GetGroupsSimple getGroupsSimple = GetGroupsSimple.class.newInstance();

            getGroupsSimple.setActAsSubjectId("GrouperSystem");

            // check all
            getGroupsSimple.setMemberFilter("All");

            getGroupsSimple.setActAsSubjectIdentifier("");
            getGroupsSimple.setSubjectId("GrouperSystem");
            getGroupsSimple.setSubjectIdentifier("");

            WsGetGroupsResults wsGetGroupsResults = stub.getGroupsSimple(getGroupsSimple)
                                                        .get_return();

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
