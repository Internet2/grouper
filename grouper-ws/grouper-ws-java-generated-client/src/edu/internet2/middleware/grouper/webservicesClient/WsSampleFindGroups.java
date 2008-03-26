/**
 *
 */
package edu.internet2.middleware.grouper.webservicesClient;

import edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.FindGroups;
import edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.FindGroupsResponse;
import edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.WsFindGroupsResults;
import edu.internet2.middleware.grouper.webservicesClient.util.GeneratedClientSettings;
import edu.internet2.middleware.grouper.ws.samples.types.WsSampleGenerated;
import edu.internet2.middleware.grouper.ws.samples.types.WsSampleGeneratedType;

import org.apache.axis2.Constants;
import org.apache.axis2.client.Options;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.axis2.transport.http.HttpTransportProperties;


/**
 * @author mchyzer
 *
 */
public class WsSampleFindGroups implements WsSampleGenerated {
    /**
     * @param args
     */
    public static void main(String[] args) throws Exception {
        findGroup(WsSampleGeneratedType.soap);
    }

    /**
     * @see edu.internet2.middleware.grouper.ws.samples.types.WsSampleGenerated#executeSample(edu.internet2.middleware.grouper.ws.samples.types.WsSampleGeneratedType)
     */
    public void executeSample(WsSampleGeneratedType wsSampleGeneratedType) {
        findGroup(wsSampleGeneratedType);
    }

    /**
     *
     * @param wsSampleGeneratedType can run as soap or xml/http
     */
    public static void findGroup(WsSampleGeneratedType wsSampleGeneratedType) {
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

            FindGroups findGroups = null;
            FindGroupsResponse findGroupsResponse = null;
            WsFindGroupsResults wsFindGroupsResults = null;
            options.setProperty(Constants.Configuration.ENABLE_REST,
                Constants.VALUE_TRUE);
            findGroups = FindGroups.class.newInstance();

            //version, e.g. v1_3_000
            findGroups.setClientVersion(GeneratedClientSettings.VERSION);

            /*
               //temporary init (TODO remove)
               findGroups.setGroupName("");
               findGroups.setStemName("");
               findGroups.setStemNameScope("");
               findGroups.setGroupUuid("");
               findGroups.setQueryScope("");
               findGroups.setQuerySearchFromStemName("");
               findGroups.setQueryTerm("");
               findGroups.setGroupName("aStem:aGroup");
               System.out.println("\n\nQUERY BY GROUP NAME: ");
               findGroupsResponse = stub.findGroups(findGroups);
               wsFindGroupsResults = findGroupsResponse.get_return();
               System.out.println(ToStringBuilder.reflectionToString(
                       wsFindGroupsResults));
               System.out.println(ToStringBuilder.reflectionToString(
                       wsFindGroupsResults.getGroupResults()[0]));
               //try by uuid
               findGroups.setGroupName("s");
               System.out.println("\n\nQUERY BY UUID: ");
               //            String groupName, String stemName,
               //                    String stemNameScope,
               //                    String groupUuid, String queryTerm, String querySearchFromStemName
               findGroups.setGroupUuid("19284537-6118-44b2-bbbc-d5757c709cb7");
               findGroupsResponse = stub.findGroups(findGroups);
               wsFindGroupsResults = findGroupsResponse.get_return();
               System.out.println(ToStringBuilder.reflectionToString(
                       wsFindGroupsResults));
               System.out.println(ToStringBuilder.reflectionToString(
                       wsFindGroupsResults.getGroupResults()[0]));
               //search by stem
               findGroups.setGroupUuid("");
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
               findGroups.setStemName("");
               findGroups.setStemNameScope("");
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
             */
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
