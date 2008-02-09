/**
 *
 */
package edu.internet2.middleware.grouper.webservicesClient;

import edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.GroupSaveSimple;
import edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.WsGroupSaveResult;

import org.apache.axis2.client.Options;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.axis2.transport.http.HttpTransportProperties;

import org.apache.commons.lang.builder.ToStringBuilder;


/**
 *
 * @author mchyzer
 *
 */
public class RunGrouperServiceGroupSaveSimple {
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

            //            options.setProperty(Constants.Configuration.ENABLE_REST,
            //            		Constants.VALUE_TRUE);
            GroupSaveSimple groupSaveSimple = GroupSaveSimple.class.newInstance();

            groupSaveSimple.setActAsSubjectId("GrouperSystem");
            groupSaveSimple.setActAsSubjectIdentifier("");
            groupSaveSimple.setCreateGroupIfNotExist("");
            groupSaveSimple.setCreateStemsIfNotExist("");
            groupSaveSimple.setDescription("test group");
            groupSaveSimple.setDisplayExtension("the test group");
            groupSaveSimple.setGroupName("aStem:test");
            groupSaveSimple.setGroupUuid("");
            groupSaveSimple.setRetrieveViaNameIfNoUuid("");

            WsGroupSaveResult wsGroupSaveResult = stub.groupSaveSimple(groupSaveSimple)
                                                          .get_return();

            System.out.println(ToStringBuilder.reflectionToString(
                    wsGroupSaveResult));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
