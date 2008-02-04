/**
 *
 */
package edu.internet2.middleware.grouper.webservicesClient;

import edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.GetMemberships;
import edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.WsGetMembershipsResult;
import edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.WsGetMembershipsResults;
import edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.WsGroupLookup;
import edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.WsSubjectLookup;

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
public class RunGrouperServiceGetMemberships {
    /**
     * @param args
     */
    public static void main(String[] args) {
        getMemberships();
    }

    /**
     *
     */
    public static void getMemberships() {
        try {
            GrouperServiceStub stub = new GrouperServiceStub(
                    "http://localhost:8091/grouper-ws/services/GrouperService");
            Options options = stub._getServiceClient().getOptions();
            HttpTransportProperties.Authenticator auth = new HttpTransportProperties.Authenticator();
            auth.setUsername("GrouperSystem");
            auth.setPassword("pass");

            options.setProperty(HTTPConstants.AUTHENTICATE, auth);

            options.setProperty(Constants.Configuration.ENABLE_REST,
                Constants.VALUE_TRUE);

            GetMemberships getMembers = GetMemberships.class.newInstance();

            // set the act as id
            WsSubjectLookup actAsSubject = WsSubjectLookup.class.newInstance();
            actAsSubject.setSubjectId("GrouperSystem");
            getMembers.setActAsSubjectLookup(actAsSubject);

            WsGroupLookup wsGroupLookup = WsGroupLookup.class.newInstance();
            wsGroupLookup.setGroupName("aStem:aGroup");
            getMembers.setWsGroupLookup(wsGroupLookup);

            getMembers.setMembershipFilter("All");
            getMembers.setRetrieveExtendedSubjectData("true");

            WsGetMembershipsResults wsGetMembershipsResults = stub.getMemberships(getMembers)
                                                                  .get_return();

            System.out.println(ToStringBuilder.reflectionToString(
                    wsGetMembershipsResults));

            WsGetMembershipsResult[] wsGetMembershipsResultArray = wsGetMembershipsResults.getResults();

            for (WsGetMembershipsResult wsGetMembershipsResult : wsGetMembershipsResultArray) {
                System.out.println(ToStringBuilder.reflectionToString(
                        wsGetMembershipsResult));
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
