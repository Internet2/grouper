/**
 *
 */
package edu.internet2.middleware.grouper.webservicesClient;

import org.apache.axis2.client.Options;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.axis2.transport.http.HttpTransportProperties;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;

import edu.internet2.middleware.grouper.webservicesClient.util.GeneratedClientSettings;
import edu.internet2.middleware.grouper.ws.samples.types.WsSampleGenerated;
import edu.internet2.middleware.grouper.ws.samples.types.WsSampleGeneratedType;
import edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.FindGroupsLite;
import edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsFindGroupsResults;
import edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsGroup;


/**
 *
 * @author mchyzer
 *
 */
public class WsSampleFindGroupsLite implements WsSampleGenerated {
    /**
     * @see edu.internet2.middleware.grouper.ws.samples.types.WsSampleGenerated#executeSample(edu.internet2.middleware.grouper.ws.samples.types.WsSampleGeneratedType)
     */
    public void executeSample(WsSampleGeneratedType wsSampleGeneratedType) {
        findGroupsLite(wsSampleGeneratedType);
    }

    /**
     * @param wsSampleGeneratedType can run as soap or xml/http
     */
    public static void findGroupsLite(
        WsSampleGeneratedType wsSampleGeneratedType) {
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

            FindGroupsLite findGroupsLite = FindGroupsLite.class.newInstance();

            findGroupsLite.setActAsSubjectId("");
            findGroupsLite.setActAsSubjectIdentifier("");
            findGroupsLite.setActAsSubjectSourceId("");
            //version, e.g. v1_3_000
            findGroupsLite.setClientVersion(GeneratedClientSettings.VERSION);
            findGroupsLite.setGroupAttributeName("");
            findGroupsLite.setGroupAttributeValue("");
            findGroupsLite.setGroupName("");
            findGroupsLite.setGroupTypeName("");
            findGroupsLite.setGroupUuid("");
            findGroupsLite.setStemName("aStem");
            findGroupsLite.setStemNameScope("ALL_IN_SUBTREE");
            findGroupsLite.setIncludeGroupDetail("T");
            findGroupsLite.setQueryFilterType("FIND_BY_STEM_NAME");

            // set the act as id
            // findGroupsLite.setActAsSubjectId("GrouperSystem");
            WsFindGroupsResults wsGroupsResults = stub.findGroupsLite(findGroupsLite)
                                                      .get_return();

            System.out.println(ToStringBuilder.reflectionToString(
                    wsGroupsResults));

            WsGroup[] wsGroups = wsGroupsResults.getGroupResults();

            if (wsGroups != null) {
                for (WsGroup wsGroup : wsGroups) {
                    System.out.println(ToStringBuilder.reflectionToString(
                            wsGroup));
                }
            }
            
            if (!StringUtils.equals("T", 
                wsGroupsResults.getResultMetadata().getSuccess())) {
              throw new RuntimeException("didnt get success! ");
            }
            
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        findGroupsLite(WsSampleGeneratedType.soap);
    }
}
