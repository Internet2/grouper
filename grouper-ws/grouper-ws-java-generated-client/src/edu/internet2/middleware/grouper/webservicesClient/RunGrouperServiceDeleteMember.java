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

import edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.DeleteMember;
import edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.WsDeleteMemberResults;
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
public class RunGrouperServiceDeleteMember {
    /**
     * @param args
     */
    public static void main(String[] args) {
        deleteMember();
    }

    /**
     * 
     */
    public static void deleteMember() {
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
            DeleteMember deleteMember = DeleteMember.class.newInstance();

            // set the act as id
            WsSubjectLookup actAsSubject = WsSubjectLookup.class.newInstance();
            actAsSubject.setSubjectId("GrouperSystem");
            deleteMember.setActAsSubjectLookup(actAsSubject);

            WsGroupLookup wsGroupLookup = WsGroupLookup.class.newInstance();
            wsGroupLookup.setGroupName("aStem:aGroup");
            deleteMember.setWsGroupLookup(wsGroupLookup);

            // delete two subjects from the group
            WsSubjectLookup[] subjectLookups = (WsSubjectLookup[]) Array.newInstance(WsSubjectLookup.class,
                    2);
            subjectLookups[0] = WsSubjectLookup.class.newInstance();
            subjectLookups[0].setSubjectId("10021368");

            subjectLookups[1] = WsSubjectLookup.class.newInstance();
            subjectLookups[1].setSubjectId("10039438");

            deleteMember.setSubjectLookups(subjectLookups);

            WsDeleteMemberResults wsDeleteMemberResults = stub.deleteMember(deleteMember)
                                                        .get_return();

            System.out.println(ToStringBuilder.reflectionToString(
                    wsDeleteMemberResults));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
