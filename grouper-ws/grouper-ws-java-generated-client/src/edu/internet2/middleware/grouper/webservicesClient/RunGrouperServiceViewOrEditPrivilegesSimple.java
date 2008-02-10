/**
 *
 */
package edu.internet2.middleware.grouper.webservicesClient;

import org.apache.axis2.client.Options;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.axis2.transport.http.HttpTransportProperties;
import org.apache.commons.lang.builder.ToStringBuilder;

import edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.ViewOrEditPrivilegesSimple;
import edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.WsViewOrEditPrivilegesResult;


/**
 *
 * @author mchyzer
 *
 */
public class RunGrouperServiceViewOrEditPrivilegesSimple {
    /**
     *
     */
    public static void viewOrEditPrivilegesSimple() {
        try {
            GrouperServiceStub stub = new GrouperServiceStub(
                    "http://localhost:8091/grouper-ws/services/GrouperService");
            Options options = stub._getServiceClient().getOptions();
            HttpTransportProperties.Authenticator auth = new HttpTransportProperties.Authenticator();
            auth.setUsername("GrouperSystem");
            auth.setPassword("pass");

            options.setProperty(HTTPConstants.AUTHENTICATE, auth);

            //options.setProperty(Constants.Configuration.ENABLE_REST,
            //		Constants.VALUE_TRUE);
            ViewOrEditPrivilegesSimple viewOrEditPrivilegesSimple = ViewOrEditPrivilegesSimple.class.newInstance();

            // set the act as id
            viewOrEditPrivilegesSimple.setActAsSubjectId("GrouperSystem");
            viewOrEditPrivilegesSimple.setActAsSubjectIdentifier("");
            viewOrEditPrivilegesSimple.setAdminAllowed("");
            viewOrEditPrivilegesSimple.setGroupName("aStem:aGroup");
            viewOrEditPrivilegesSimple.setGroupUuid("");
            viewOrEditPrivilegesSimple.setOptinAllowed("T");
            viewOrEditPrivilegesSimple.setOptoutAllowed("F");
            viewOrEditPrivilegesSimple.setReadAllowed("");
            viewOrEditPrivilegesSimple.setSubjectId("10021368");
            viewOrEditPrivilegesSimple.setSubjectIdentifier("");
            viewOrEditPrivilegesSimple.setUpdateAllowed("");
            viewOrEditPrivilegesSimple.setViewAllowed("");
            
            WsViewOrEditPrivilegesResult wsViewOrEditPrivilegesResult = 
            	stub.viewOrEditPrivilegesSimple(viewOrEditPrivilegesSimple).get_return();

            System.out.println(ToStringBuilder.reflectionToString(
            		wsViewOrEditPrivilegesResult));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        viewOrEditPrivilegesSimple();
    }
}
