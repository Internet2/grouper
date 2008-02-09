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


/**
 *
 * @author mchyzer
 *
 */
public class RunGrouperServiceGroupSave {
    /**
     * @param args
     */
    public static void main(String[] args) {
        groupSave();
    }

    /**
     *
     */
    public static void groupSave() {
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

                        options.setProperty(Constants.Configuration.ENABLE_REST,
                        		Constants.VALUE_TRUE);
            GroupSave groupSave = GroupSave.class.newInstance();

            // set the act as id
            WsSubjectLookup actAsSubject = WsSubjectLookup.class.newInstance();
            actAsSubject.setSubjectId("GrouperSystem");
            groupSave.setActAsSubjectLookup(actAsSubject);

            WsGroupToSave wsGroupToSave = WsGroupToSave.class.newInstance();

            wsGroupToSave.setCreateGroupIfNotExist("");
            wsGroupToSave.setCreateStemsIfNotExist("");
            wsGroupToSave.setDescription("the test group");
            wsGroupToSave.setDisplayExtension("test group");
            wsGroupToSave.setGroupName("aStem:test");
            wsGroupToSave.setRetrieveViaNameIfNoUuid("");
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
