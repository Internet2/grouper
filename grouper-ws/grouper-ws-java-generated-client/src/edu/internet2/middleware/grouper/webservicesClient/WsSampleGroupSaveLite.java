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
import edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GroupSaveLite;
import edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsGroupSaveLiteResult;


/**
 *
 * @author mchyzer
 *
 */
public class WsSampleGroupSaveLite implements WsSampleGenerated {
    /**
     * @param args
     */
    public static void main(String[] args) {
        groupSaveLite(WsSampleGeneratedType.soap);
    }

    /**
     * @see edu.internet2.middleware.grouper.ws.samples.types.WsSampleGenerated#executeSample(edu.internet2.middleware.grouper.ws.samples.types.WsSampleGeneratedType)
     */
    public void executeSample(WsSampleGeneratedType wsSampleGeneratedType) {
        groupSaveLite(wsSampleGeneratedType);
    }

    /**
     * @param wsSampleGeneratedType can run as soap or xml/http
     */
    public static void groupSaveLite(
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

            GroupSaveLite groupSaveLite = GroupSaveLite.class.newInstance();

            //version, e.g. v1_3_000
            groupSaveLite.setClientVersion(GeneratedClientSettings.VERSION);

            groupSaveLite.setActAsSubjectId("GrouperSystem");
            groupSaveLite.setActAsSubjectIdentifier("");
            groupSaveLite.setActAsSubjectSourceId("");
            groupSaveLite.setDescription("test group");
            groupSaveLite.setDisplayExtension("the test group");
            groupSaveLite.setGroupName("aStem:test");
            groupSaveLite.setGroupUuid("");
            groupSaveLite.setIncludeGroupDetail("F");
            groupSaveLite.setSaveMode("");

            groupSaveLite.setGroupLookupName("aGroup:test");
            groupSaveLite.setGroupLookupUuid("");

            WsGroupSaveLiteResult wsGroupSaveLiteResults = stub.groupSaveLite(groupSaveLite)
                                                               .get_return();

            System.out.println(ToStringBuilder.reflectionToString(
                    wsGroupSaveLiteResults));
            
            if (!StringUtils.equals("T", 
                wsGroupSaveLiteResults.getResultMetadata().getSuccess())) {
              throw new RuntimeException("didnt get success! ");
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
