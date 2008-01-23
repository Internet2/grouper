/**
 *
 */
package edu.internet2.middleware.grouper.webservicesClient;

import org.apache.axis2.client.Options;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.axis2.transport.http.HttpTransportProperties;
import org.apache.commons.lang.builder.ToStringBuilder;

import edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.FindGroupsSimple;
import edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.WsFindGroupsResults;
import edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.WsGroupResult;


/**
 * Run this to run the generated axis client for find groups simple
 *
 * @author mchyzer
 *
 */
public class RunGrouperServiceFindGroupsSimple {
    /**
     *
     */
    public static void findGroupsSimple() {
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
            FindGroupsSimple findGroupsSimple = FindGroupsSimple.class.newInstance();
            
            findGroupsSimple.setGroupName("");
            findGroupsSimple.setStemName("aStem");
            findGroupsSimple.setStemNameScope("ALL_IN_SUBTREE");
            
            // set the act as id
            // findGroupsSimple.setActAsSubjectId("GrouperSystem");

            WsFindGroupsResults wsGroupsResults = stub.findGroupsSimple(findGroupsSimple)
                                                      .get_return();

            System.out.println(ToStringBuilder.reflectionToString(
            		wsGroupsResults));
            
            WsGroupResult[] wsGroupResults = wsGroupsResults.getGroupResults();
            
            if (wsGroupResults != null) {
                for (WsGroupResult wsGroupResult : wsGroupResults) {
                    System.out.println(ToStringBuilder.reflectionToString(wsGroupResult));
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @param args
     */
    public static void main(String[] args) throws Exception {
        //addMember();
        findGroupsSimple();
    }
}
