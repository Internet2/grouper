/**
 *
 */
package edu.internet2.middleware.grouper.webservicesClient;

import java.lang.reflect.Array;

import org.apache.axis2.Constants;
import org.apache.axis2.client.Options;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.axis2.transport.http.HttpTransportProperties;
import org.apache.commons.lang.builder.ToStringBuilder;

import edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.ViewOrEditPrivileges;
import edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.WsGroupLookup;
import edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.WsSubjectLookup;
import edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.WsViewOrEditPrivilegesResult;
import edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.WsViewOrEditPrivilegesResults;


/**
 * Use ant script to generate client, but if manual, do this:
 *
 * Generate the code:
 *
 * C:\mchyzer\isc\dev\grouper\grouper-ws-java-generated-client>wsdl2java -p
 * edu.internet2.middleware.grouper.webservicesClient -t -uri GrouperService.wsdl
 *
 * @author mchyzer
 *
 */
public class RunGrouperServiceViewOrEditPrivileges {
    /**
     * @param args
     */
    public static void main(String[] args) {
        viewOrEditPrivileges();
    }

    /**
     *
     */
    public static void viewOrEditPrivileges() {
        try {
            GrouperServiceStub stub = new GrouperServiceStub(
                    "http://localhost:8091/grouper-ws/services/GrouperService");
            Options options = stub._getServiceClient().getOptions();
            HttpTransportProperties.Authenticator auth = new HttpTransportProperties.Authenticator();
            auth.setUsername("GrouperSystem");
            auth.setPassword("pass");

            options.setProperty(HTTPConstants.AUTHENTICATE, auth);

            options.setProperty(Constants.Configuration.ENABLE_REST,
            		Constants.VALUE_TRUE);
            ViewOrEditPrivileges viewOrEditPrivileges = ViewOrEditPrivileges.class.newInstance();

            // set the act as id
            WsSubjectLookup actAsSubject = WsSubjectLookup.class.newInstance();
            actAsSubject.setSubjectId("GrouperSystem");
            viewOrEditPrivileges.setActAsSubjectLookup(actAsSubject);

            WsGroupLookup wsGroupLookup = WsGroupLookup.class.newInstance();
            wsGroupLookup.setGroupName("aStem:aGroup");
            viewOrEditPrivileges.setWsGroupLookup(wsGroupLookup);

            // add two subjects to the group
            WsSubjectLookup[] subjectLookups = (WsSubjectLookup[]) Array.newInstance(WsSubjectLookup.class,
                    2);
            subjectLookups[0] = WsSubjectLookup.class.newInstance();
            subjectLookups[0].setSubjectId("10021368");

            subjectLookups[1] = WsSubjectLookup.class.newInstance();
            subjectLookups[1].setSubjectId("10039438");

            viewOrEditPrivileges.setSubjectLookups(subjectLookups);

            //change some privs
            viewOrEditPrivileges.setAdminAllowed("");
            viewOrEditPrivileges.setOptinAllowed("T");
            viewOrEditPrivileges.setOptoutAllowed("");
            viewOrEditPrivileges.setReadAllowed("");
            viewOrEditPrivileges.setUpdateAllowed("F");
            viewOrEditPrivileges.setViewAllowed("");
            
            WsViewOrEditPrivilegesResults wsViewOrEditPrivilegesResults = 
            	stub.viewOrEditPrivileges(viewOrEditPrivileges).get_return();

            System.out.println(ToStringBuilder.reflectionToString(
            		wsViewOrEditPrivilegesResults));
            for (WsViewOrEditPrivilegesResult result : wsViewOrEditPrivilegesResults.getResults()) {
            	System.out.println(ToStringBuilder.reflectionToString(result));
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
