/**
 *
 */
package edu.internet2.middleware.grouper.webservicesClient;

import org.apache.axis2.Constants;
import org.apache.axis2.client.Options;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.axis2.transport.http.HttpTransportProperties;
import org.apache.commons.lang.builder.ToStringBuilder;

import edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.GroupSave;
import edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.WsGroupSaveResult;
import edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.WsGroupSaveResults;
import edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.WsGroupToSave;
import edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.WsSubjectLookup;
import edu.internet2.middleware.grouper.webservicesClient.util.GeneratedClientSettings;
import edu.internet2.middleware.grouper.ws.samples.types.WsSampleGenerated;
import edu.internet2.middleware.grouper.ws.samples.types.WsSampleGeneratedType;


/**
 *
 * @author mchyzer
 *
 */
public class WsSampleGroupSave implements WsSampleGenerated {
    /**
     * @param args
     */
    public static void main(String[] args) {
        groupSave(WsSampleGeneratedType.soap);
    }

    /**
     * @see edu.internet2.middleware.grouper.ws.samples.types.WsSampleGenerated#executeSample(edu.internet2.middleware.grouper.ws.samples.types.WsSampleGeneratedType)
     */
    public void executeSample(WsSampleGeneratedType wsSampleGeneratedType) {
        groupSave(wsSampleGeneratedType);
    }

    /**
     * @param wsSampleGeneratedType can run as soap or xml/http
     */
    public static void groupSave(WsSampleGeneratedType wsSampleGeneratedType) {
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

            GroupSave groupSave = GroupSave.class.newInstance();

            //version, e.g. v1_3_000
            groupSave.setClientVersion(GeneratedClientSettings.VERSION);

            // set the act as id
            WsSubjectLookup actAsSubject = WsSubjectLookup.class.newInstance();
            actAsSubject.setSubjectId("GrouperSystem");
            groupSave.setActAsSubjectLookup(actAsSubject);

            WsGroupToSave wsGroupToSave = WsGroupToSave.class.newInstance();

            wsGroupToSave.setCreateStemsIfNotExist("");
            wsGroupToSave.setDescription("the test group");
            wsGroupToSave.setDisplayExtension("test group");
            wsGroupToSave.setGroupName("aStem:test");
            wsGroupToSave.setSaveMode("");
            wsGroupToSave.setUuid("");
            groupSave.setWsGroupsToSave(new WsGroupToSave[] { wsGroupToSave });

            WsGroupSaveResults wsGroupSaveResults = stub.groupSave(groupSave)
                                                        .get_return();

            System.out.println(ToStringBuilder.reflectionToString(
                    wsGroupSaveResults));

            WsGroupSaveResult[] wsGroupSaveResultArray = wsGroupSaveResults.getResults();

            if (wsGroupSaveResultArray != null) {
                for (WsGroupSaveResult wsGroupSaveResult : wsGroupSaveResultArray) {
                    System.out.println(ToStringBuilder.reflectionToString(
                            wsGroupSaveResult));
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
