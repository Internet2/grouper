/**
 *
 */
package edu.internet2.middleware.grouper.webservicesClient;

import org.apache.axis2.client.Options;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.axis2.transport.http.HttpTransportProperties;
import org.apache.commons.lang.builder.ToStringBuilder;

import edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.GetMembershipsSimple;
import edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.WsGetMembershipsResult;
import edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.WsGetMembershipsResults;


/**
 *
 * @author mchyzer
 *
 */
public class RunGrouperServiceGetMembershipsSimple {
    /**
     *
     */
    public static void getMembershipsSimple() {
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

            //options.setProperty(Constants.Configuration.ENABLE_REST,
            //		Constants.VALUE_TRUE);
            GetMembershipsSimple getMembershipsSimple = GetMembershipsSimple.class.newInstance();

            // set the act as id
            getMembershipsSimple.setActAsSubjectId("GrouperSystem");

            getMembershipsSimple.setGroupName("aStem:aGroup");
            getMembershipsSimple.setGroupUuid("");
            getMembershipsSimple.setMembershipFilter("All");
            getMembershipsSimple.setRetrieveExtendedSubjectData("true");

            WsGetMembershipsResults wsGetMembershipsResults = stub.getMembershipsSimple(getMembershipsSimple)
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

    /**
     * @param args
     */
    public static void main(String[] args) {
        getMembershipsSimple();
    }
}
