/**
 *
 */
package edu.internet2.middleware.grouper.webservicesClient;

import edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.AddMember;
import edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.WsAddMemberResults;
import edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.WsGroupLookup;
import edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.WsSubjectLookup;

import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.axis2.transport.http.HttpTransportProperties;

import org.apache.commons.lang.builder.ToStringBuilder;

import java.lang.reflect.Array;


/**
 * @author mchyzer
 *
 */
public class RunGrouperService {
    /**
     * @param args
     */
    public static void main(String[] args) throws Exception {
        GrouperServiceStub stub = new GrouperServiceStub(
                "http://localhost:8090/grouper-ws/services/GrouperService");
        AddMember addMember = AddMember.class.newInstance();

        //set the act as id
        WsSubjectLookup actAsSubject = WsSubjectLookup.class.newInstance();
        actAsSubject.setSubjectId("GrouperSystem");
        addMember.setActAsSubjectLookup(actAsSubject);

        //just add, dont replace
        addMember.setReplaceAllExisting("F");

        WsGroupLookup wsGroupLookup = WsGroupLookup.class.newInstance();
        wsGroupLookup.setGroupName("aStem:aGroup");
        addMember.setWsGroupLookup(wsGroupLookup);

        //add two subjects to the group
        WsSubjectLookup[] subjectLookups = (WsSubjectLookup[]) Array.newInstance(WsSubjectLookup.class,
                2);
        subjectLookups[0] = WsSubjectLookup.class.newInstance();
        subjectLookups[0].setSubjectId("10021368");

        subjectLookups[1] = WsSubjectLookup.class.newInstance();
        subjectLookups[1].setSubjectId("10039438");

        addMember.setSubjectLookups(subjectLookups);

        HttpTransportProperties.Authenticator auth = new HttpTransportProperties.Authenticator();
        auth.setUsername("GrouperSystem");
        auth.setPassword("123");
        // set if realm or domain is known
        stub._getServiceClient().getOptions()
            .setProperty(HTTPConstants.AUTHENTICATE, auth);
        stub._getServiceClient().getOptions()
            .setProperty(HTTPConstants.SO_TIMEOUT, new Integer(3600000));
        stub._getServiceClient().getOptions()
            .setProperty(HTTPConstants.CONNECTION_TIMEOUT, new Integer(3600000));

        WsAddMemberResults wsAddMemberResults = stub.addMember(addMember)
                                                    .get_return();

        System.out.println(ToStringBuilder.reflectionToString(
                wsAddMemberResults));
    }
}
