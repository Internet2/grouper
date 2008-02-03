/**
 *
 */
package edu.internet2.middleware.grouper.webservicesClient;

import org.apache.axis2.client.Options;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.axis2.transport.http.HttpTransportProperties;
import org.apache.commons.lang.builder.ToStringBuilder;

import edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.GetMembersSimple;
import edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.WsGetMembersResult;
import edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.WsGetMembersResults;

/**
 *
 * @author mchyzer
 *
 */
public class RunGrouperServiceGetMembersSimple {
    /**
     *
     */
    public static void deleteMemberSimple() {
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
            GetMembersSimple getMembersSimple = GetMembersSimple.class.newInstance();

            // set the act as id
            getMembersSimple.setActAsSubjectId("GrouperSystem");

            getMembersSimple.setGroupName("aStem:aGroup");
            getMembersSimple.setGroupUuid("");
            getMembersSimple.setMemberFilter("All");
            getMembersSimple.setRetrieveExtendedSubjectData("true");
            
            WsGetMembersResults wsGetMembersResults = stub.getMembersSimple(getMembersSimple)
                                                            .get_return();

            System.out.println(ToStringBuilder.reflectionToString(
                    wsGetMembersResults));

            WsGetMembersResult[] wsGetMembersResultArray = wsGetMembersResults.getResults();

            for (WsGetMembersResult wsGetMembersResult : wsGetMembersResultArray) {
                System.out.println(ToStringBuilder.reflectionToString(
                        wsGetMembersResult));
            }
                        
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        deleteMemberSimple();
    }
}
