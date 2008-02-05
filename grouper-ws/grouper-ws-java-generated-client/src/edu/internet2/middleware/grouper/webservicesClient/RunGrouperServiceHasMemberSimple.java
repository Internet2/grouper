/**
 *
 */
package edu.internet2.middleware.grouper.webservicesClient;

import edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.HasMemberSimple;
import edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.WsHasMemberResult;

import org.apache.axis2.client.Options;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.axis2.transport.http.HttpTransportProperties;

import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * @author mchyzer
 *
 */
public class RunGrouperServiceHasMemberSimple {
    /**
     * @param args
     */
    public static void main(String[] args) {
        hasMember();
    }

    /**
     *
     */
    public static void hasMember() {
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
            HasMemberSimple hasMemberSimple = HasMemberSimple.class.newInstance();

            // set the act as id
            hasMemberSimple.setActAsSubjectId("GrouperSystem");
            hasMemberSimple.setActAsSubjectIdentifier("");

            // check all
            hasMemberSimple.setMemberFilter("All");

            hasMemberSimple.setGroupName("aStem:aGroup");
            hasMemberSimple.setGroupUuid("");

            hasMemberSimple.setSubjectId("GrouperSystem");
            hasMemberSimple.setSubjectIdentifier("");

            WsHasMemberResult wsHasMemberResult = stub.hasMemberSimple(hasMemberSimple)
                                                      .get_return();

            System.out.println(ToStringBuilder.reflectionToString(
                    wsHasMemberResult));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
