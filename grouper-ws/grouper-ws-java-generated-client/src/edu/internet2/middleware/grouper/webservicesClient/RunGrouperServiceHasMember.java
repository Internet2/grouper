/**
 *
 */
package edu.internet2.middleware.grouper.webservicesClient;

import java.lang.reflect.Array;

import org.apache.axis2.client.Options;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.axis2.transport.http.HttpTransportProperties;
import org.apache.commons.lang.builder.ToStringBuilder;

import edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.HasMember;
import edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.WsGroupLookup;
import edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.WsHasMemberResult;
import edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.WsHasMemberResults;
import edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.WsSubjectLookup;


/**
 * @author mchyzer
 *
 */
public class RunGrouperServiceHasMember {
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
            HasMember hasMember = HasMember.class.newInstance();

            // set the act as id
            WsSubjectLookup actAsSubject = WsSubjectLookup.class.newInstance();
            actAsSubject.setSubjectId("GrouperSystem");
            hasMember.setActAsSubjectLookup(actAsSubject);

            // check all
            hasMember.setMemberFilter("All");

            WsGroupLookup wsGroupLookup = WsGroupLookup.class.newInstance();
            wsGroupLookup.setGroupName("aStem:aGroup");
            hasMember.setWsGroupLookup(wsGroupLookup);

            // check two subjects from the group
            WsSubjectLookup[] subjectLookups = (WsSubjectLookup[]) Array.newInstance(WsSubjectLookup.class,
                    2);
            subjectLookups[0] = WsSubjectLookup.class.newInstance();
            subjectLookups[0].setSubjectId("GrouperSystem");

            subjectLookups[1] = WsSubjectLookup.class.newInstance();
            subjectLookups[1].setSubjectId("10039438");

            hasMember.setSubjectLookups(subjectLookups);

            WsHasMemberResults wsHasMemberResults = stub.hasMember(hasMember)
                                                        .get_return();

            System.out.println(ToStringBuilder.reflectionToString(
                    wsHasMemberResults));
            
            WsHasMemberResult[] results = wsHasMemberResults.getResults();
            
            if (results != null) {
            	for (WsHasMemberResult wsHasMemberResult : results) {
                    System.out.println(ToStringBuilder.reflectionToString(
                    		wsHasMemberResult));
            		
            	}
            }
            
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
