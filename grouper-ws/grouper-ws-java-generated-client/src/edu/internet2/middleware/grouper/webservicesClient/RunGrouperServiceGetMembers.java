/**
 *
 */
package edu.internet2.middleware.grouper.webservicesClient;

import org.apache.axis2.Constants;
import org.apache.axis2.client.Options;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.axis2.transport.http.HttpTransportProperties;
import org.apache.commons.lang.builder.ToStringBuilder;

import edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.GetMembers;
import edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.WsGetMembersResult;
import edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.WsGetMembersResults;
import edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.WsGroupLookup;
import edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.WsSubjectLookup;


/**
 * Use ant script to generate client, but if manual, do this:
 * Run this to run the generated axis client.
 *
 * Generate the code:
 *
 * C:\mchyzer\isc\dev\grouper\grouper-ws-java-generated-client>wsdl2java -p
 * edu.internet2.middleware.grouper.webservicesClient -t -uri GrouperService.wsdl
 *
 * @author mchyzer
 *
 */
public class RunGrouperServiceGetMembers {
    /**
     * @param args
     */
    public static void main(String[] args) {
        getMembers();
    }

    /**
     *
     */
    public static void getMembers() {
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
            GetMembers getMembers = GetMembers.class.newInstance();

            // set the act as id
            WsSubjectLookup actAsSubject = WsSubjectLookup.class.newInstance();
            actAsSubject.setSubjectId("GrouperSystem");
            getMembers.setActAsSubjectLookup(actAsSubject);

            WsGroupLookup wsGroupLookup = WsGroupLookup.class.newInstance();
            wsGroupLookup.setGroupName("aStem:aGroup");
            getMembers.setWsGroupLookup(wsGroupLookup);

            getMembers.setMemberFilter("All");
            getMembers.setRetrieveExtendedSubjectData("true");

            WsGetMembersResults wsGetMembersResults = stub.getMembers(getMembers)
                                                          .get_return();

            System.out.println(ToStringBuilder.reflectionToString(
                    wsGetMembersResults));

            WsGetMembersResult[] wsGetMembersResultArray = wsGetMembersResults.getResults();

            for (WsGetMembersResult wsGetMembersResult : wsGetMembersResultArray) {
                System.out.println(ToStringBuilder.reflectionToString(
                        wsGetMembersResult));
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
