/**
 *
 */
package edu.internet2.middleware.grouper.webservicesClient;

import edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.FindGroups;
import edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.FindGroupsResponse;
import edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.WsFindGroupsResults;
import edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.WsGroupResult;

import org.apache.axis2.Constants;
import org.apache.axis2.client.Options;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.axis2.transport.http.HttpTransportProperties;

import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * Run this to run the generated axis client.
 *
 * Generate the code:
 *
 * C:\mchyzer\isc\dev\grouper\grouper-ws-java-generated-client>wsdl2java -p
 * edu.internet2.middleware.grouper.webservicesClient -t -uri GrouperService.wsdl
 *
 * @author mchyzer
 *
 */
public class RunGrouperServiceFindGroup {
    /**
     * @param args
     */
    public static void main(String[] args) throws Exception {
        findGroup();
    }

    public static void findGroup() {
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

            FindGroups findGroups = null;
            FindGroupsResponse findGroupsResponse = null;
            WsFindGroupsResults wsFindGroupsResults = null;
            options.setProperty(Constants.Configuration.ENABLE_REST,
                Constants.VALUE_TRUE);
            findGroups = FindGroups.class.newInstance();

            //temporary init (TODO remove)
            findGroups.setGroupName(" ");
            findGroups.setStemName(" ");
            findGroups.setStemNameScope(" ");
            findGroups.setGroupUuid(" ");
            findGroups.setQueryScope(" ");
            findGroups.setQuerySearchFromStemName(" ");
            findGroups.setQueryTerm(" ");

            findGroups.setGroupName("aStem:aGroup");
            System.out.println("\n\nQUERY BY GROUP NAME: ");

            findGroupsResponse = stub.findGroups(findGroups);

            wsFindGroupsResults = findGroupsResponse.get_return();
            System.out.println(ToStringBuilder.reflectionToString(
                    wsFindGroupsResults));
            System.out.println(ToStringBuilder.reflectionToString(
                    wsFindGroupsResults.getGroupResults()[0]));

            //try by uuid
            findGroups.setGroupName(" ");
            System.out.println("\n\nQUERY BY UUID: ");
            //            String groupName, String stemName, 
            //    		String stemNameScope,
            //    		String groupUuid, String queryTerm, String querySearchFromStemName
            findGroups.setGroupUuid("19284537-6118-44b2-bbbc-d5757c709cb7");

            findGroupsResponse = stub.findGroups(findGroups);

            wsFindGroupsResults = findGroupsResponse.get_return();
            System.out.println(ToStringBuilder.reflectionToString(
                    wsFindGroupsResults));
            System.out.println(ToStringBuilder.reflectionToString(
                    wsFindGroupsResults.getGroupResults()[0]));

            //search by stem
            findGroups.setGroupUuid(" ");
            System.out.println("\n\nQUERY BY STEM: ");
            findGroups.setStemName("aStem");
            findGroups.setStemNameScope("ONE_LEVEL");
            findGroupsResponse = stub.findGroups(findGroups);

            wsFindGroupsResults = findGroupsResponse.get_return();
            System.out.println(ToStringBuilder.reflectionToString(
                    wsFindGroupsResults));

            WsGroupResult[] wsGroupResults = wsFindGroupsResults.getGroupResults();

            if (wsGroupResults != null) {
                for (WsGroupResult wsGroupResult : wsFindGroupsResults.getGroupResults()) {
                    System.out.println(ToStringBuilder.reflectionToString(
                            wsGroupResult));
                }
            }

            //search by query
            findGroups.setStemName(" ");
            findGroups.setStemNameScope(" ");
            System.out.println("\n\nQUERY BY QUERY: ");
            findGroups.setQueryTerm("group");
            findGroups.setQueryScope("NAME");
            findGroupsResponse = stub.findGroups(findGroups);

            wsFindGroupsResults = findGroupsResponse.get_return();
            System.out.println(ToStringBuilder.reflectionToString(
                    wsFindGroupsResults));

            wsGroupResults = wsFindGroupsResults.getGroupResults();

            if (wsGroupResults != null) {
                for (WsGroupResult wsGroupResult : wsFindGroupsResults.getGroupResults()) {
                    System.out.println(ToStringBuilder.reflectionToString(
                            wsGroupResult));
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
