/**
 *
 */
package edu.internet2.middleware.grouper.webservicesClient;

import org.apache.axis2.client.Options;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.axis2.transport.http.HttpTransportProperties;
import org.apache.commons.lang.builder.ToStringBuilder;

import edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.AddMemberSimple;
import edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.WsAddMemberResult;


/**
 *
 * @author mchyzer
 *
 */
public class RunGrouperServiceAddMemberSimple {
	
	/**
	 * 
	 */
    public static void addMemberSimple() {
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
            AddMemberSimple addMemberSimple = AddMemberSimple.class.newInstance();

            // set the act as id
            addMemberSimple.setActAsSubjectId("GrouperSystem");

            addMemberSimple.setGroupName("aStem:aGroup");

            addMemberSimple.setSubjectId("10021368");

            WsAddMemberResult wsAddMemberResult = stub.addMemberSimple(addMemberSimple)
                                                      .get_return();

            System.out.println(ToStringBuilder.reflectionToString(
                    wsAddMemberResult));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        addMemberSimple();
    }
}
