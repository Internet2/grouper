/**
 *
 */
package edu.internet2.middleware.grouper.webservicesClient;

import org.apache.axis2.client.Options;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.axis2.transport.http.HttpTransportProperties;
import org.apache.commons.lang.builder.ToStringBuilder;

import edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.GroupDeleteSimple;
import edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.WsGroupDeleteResult;


/**
 *
 * @author mchyzer
 *
 */
public class RunGrouperServiceGroupDeleteSimple {
    /**
     * @param args
     */
    public static void main(String[] args) {
        groupDelete();
    }

    /**
     *
     */
    public static void groupDelete() {
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
            GroupDeleteSimple groupDeleteSimple = GroupDeleteSimple.class.newInstance();

            groupDeleteSimple.setActAsSubjectId("GrouperSystem");
            groupDeleteSimple.setActAsSubjectIdentifier("");
            groupDeleteSimple.setGroupName("aStem:test");
            groupDeleteSimple.setGroupUuid("");
            
            WsGroupDeleteResult wsGroupDeleteResult = stub.groupDeleteSimple(groupDeleteSimple)
                                                        .get_return();

            System.out.println(ToStringBuilder.reflectionToString(
            		wsGroupDeleteResult));
            
            
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
