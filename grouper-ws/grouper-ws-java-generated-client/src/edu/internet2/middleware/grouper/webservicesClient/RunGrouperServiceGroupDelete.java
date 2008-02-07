/**
 *
 */
package edu.internet2.middleware.grouper.webservicesClient;

import org.apache.axis2.client.Options;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.axis2.transport.http.HttpTransportProperties;
import org.apache.commons.lang.builder.ToStringBuilder;

import edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.GroupDelete;
import edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.WsGroupDeleteResult;
import edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.WsGroupDeleteResults;
import edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.WsGroupLookup;
import edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.WsSubjectLookup;


/**
 *
 * @author mchyzer
 *
 */
public class RunGrouperServiceGroupDelete {
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
            GroupDelete groupDelete = GroupDelete.class.newInstance();

            // set the act as id
            WsSubjectLookup actAsSubject = WsSubjectLookup.class.newInstance();
            actAsSubject.setSubjectId("GrouperSystem");
            groupDelete.setActAsSubjectLookup(actAsSubject);

            WsGroupLookup wsGroupLookup = WsGroupLookup.class.newInstance();
            wsGroupLookup.setGroupName("aStem:test");
            groupDelete.setWsGroupLookups(new WsGroupLookup[]{wsGroupLookup});

            WsGroupDeleteResults wsGroupDeleteResults = stub.groupDelete(groupDelete)
                                                        .get_return();

            System.out.println(ToStringBuilder.reflectionToString(
            		wsGroupDeleteResults));
            
            WsGroupDeleteResult[] wsGroupDeleteResultArray = wsGroupDeleteResults.getResults();
            
            if (wsGroupDeleteResultArray != null) {
            	for (WsGroupDeleteResult wsGroupDeleteResult : wsGroupDeleteResultArray) {
                    System.out.println(ToStringBuilder.reflectionToString(
                    		wsGroupDeleteResult));
            	}
            }
            
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
