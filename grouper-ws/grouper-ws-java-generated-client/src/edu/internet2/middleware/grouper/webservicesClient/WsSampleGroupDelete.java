/**
 *
 */
package edu.internet2.middleware.grouper.webservicesClient;

import org.apache.axis2.client.Options;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.axis2.transport.http.HttpTransportProperties;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;

import edu.internet2.middleware.grouper.webservicesClient.util.GeneratedClientSettings;
import edu.internet2.middleware.grouper.ws.samples.types.WsSampleGenerated;
import edu.internet2.middleware.grouper.ws.samples.types.WsSampleGeneratedType;
import edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GroupDelete;
import edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsGroupDeleteResult;
import edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsGroupDeleteResults;
import edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsGroupLookup;
import edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsSubjectLookup;


/**
 *
 * @author mchyzer
 *
 */
public class WsSampleGroupDelete implements WsSampleGenerated {
    /**
     * @param args
     */
    public static void main(String[] args) {
        groupDelete(WsSampleGeneratedType.soap);
    }

    /**
     * @see edu.internet2.middleware.grouper.ws.samples.types.WsSampleGenerated#executeSample(edu.internet2.middleware.grouper.ws.samples.types.WsSampleGeneratedType)
     */
    public void executeSample(WsSampleGeneratedType wsSampleGeneratedType) {
        groupDelete(wsSampleGeneratedType);
    }

    /**
     * @param wsSampleGeneratedType can run as soap or xml/http
     */
    public static void groupDelete(WsSampleGeneratedType wsSampleGeneratedType) {
        try {
            //URL, e.g. http://localhost:8091/grouper-ws/services/GrouperService
            GrouperServiceStub stub = new GrouperServiceStub(GeneratedClientSettings.URL);
            Options options = stub._getServiceClient().getOptions();
            HttpTransportProperties.Authenticator auth = new HttpTransportProperties.Authenticator();
            auth.setUsername(GeneratedClientSettings.USER);
            auth.setPassword(GeneratedClientSettings.PASS);
            auth.setPreemptiveAuthentication(true);

            options.setProperty(HTTPConstants.AUTHENTICATE, auth);
            options.setProperty(HTTPConstants.SO_TIMEOUT, new Integer(3600000));
            options.setProperty(HTTPConstants.CONNECTION_TIMEOUT,
                new Integer(3600000));

            GroupDelete groupDelete = GroupDelete.class.newInstance();

            //version, e.g. v1_3_000
            groupDelete.setClientVersion(GeneratedClientSettings.VERSION);

            // set the act as id
            WsSubjectLookup actAsSubject = WsSubjectLookup.class.newInstance();
            actAsSubject.setSubjectId("GrouperSystem");
            groupDelete.setActAsSubjectLookup(actAsSubject);

            WsGroupLookup wsGroupLookup = WsGroupLookup.class.newInstance();
            wsGroupLookup.setGroupName("aStem:test");
            groupDelete.setWsGroupLookups(new WsGroupLookup[] { wsGroupLookup });

            WsGroupDeleteResults wsGroupDeleteResults = stub.groupDelete(groupDelete)
                                                            .get_return();

            System.out.println(ToStringBuilder.reflectionToString(
                    wsGroupDeleteResults));

            WsGroupDeleteResult[] wsGroupDeleteResultArray = wsGroupDeleteResults.getResults();

            for (WsGroupDeleteResult wsGroupDeleteResult : GeneratedClientSettings.nonNull(
                    wsGroupDeleteResultArray)) {
                System.out.println(ToStringBuilder.reflectionToString(
                        wsGroupDeleteResult));
            }
            
            if (!StringUtils.equals("T", 
                wsGroupDeleteResults.getResultMetadata().getSuccess())) {
              throw new RuntimeException("didnt get success! ");
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
