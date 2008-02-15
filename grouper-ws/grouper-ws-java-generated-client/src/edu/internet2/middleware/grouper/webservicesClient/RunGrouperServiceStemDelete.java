/**
 *
 */
package edu.internet2.middleware.grouper.webservicesClient;

import edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.StemDelete;
import edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.WsStemDeleteResult;
import edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.WsStemDeleteResults;
import edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.WsStemLookup;
import edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.WsSubjectLookup;

import org.apache.axis2.Constants;
import org.apache.axis2.client.Options;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.axis2.transport.http.HttpTransportProperties;

import org.apache.commons.lang.builder.ToStringBuilder;


/**
 *
 * @author mchyzer
 *
 */
public class RunGrouperServiceStemDelete {
    /**
     * @param args
     */
    public static void main(String[] args) {
        stemDelete();
    }

    /**
     *
     */
    public static void stemDelete() {
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

            StemDelete stemDelete = StemDelete.class.newInstance();

            // set the act as id
            WsSubjectLookup actAsSubject = WsSubjectLookup.class.newInstance();
            actAsSubject.setSubjectId("GrouperSystem");
            stemDelete.setActAsSubjectLookup(actAsSubject);

            WsStemLookup wsStemLookup = WsStemLookup.class.newInstance();
            wsStemLookup.setStemName("aStem:stemNotExist");
            stemDelete.setWsStemLookups(new WsStemLookup[] { wsStemLookup });

            WsStemDeleteResults wsStemDeleteResults = stub.stemDelete(stemDelete)
                                                          .get_return();

            System.out.println(ToStringBuilder.reflectionToString(
                    wsStemDeleteResults));

            WsStemDeleteResult[] wsStemDeleteResultArray = wsStemDeleteResults.getResults();

            if (wsStemDeleteResultArray != null) {
                for (WsStemDeleteResult wsStemDeleteResult : wsStemDeleteResultArray) {
                    System.out.println(ToStringBuilder.reflectionToString(
                            wsStemDeleteResult));
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
