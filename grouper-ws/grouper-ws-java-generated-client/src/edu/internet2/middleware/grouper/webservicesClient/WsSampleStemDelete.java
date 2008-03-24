/**
 *
 */
package edu.internet2.middleware.grouper.webservicesClient;

import edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.StemDelete;
import edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.WsStemDeleteResult;
import edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.WsStemDeleteResults;
import edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.WsStemLookup;
import edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.WsSubjectLookup;
import edu.internet2.middleware.grouper.webservicesClient.util.GeneratedClientSettings;
import edu.internet2.middleware.grouper.ws.samples.WsSampleGenerated;
import edu.internet2.middleware.grouper.ws.samples.WsSampleGeneratedType;

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
public class WsSampleStemDelete implements WsSampleGenerated {
    /**
     * @param args
     */
    public static void main(String[] args) {
        stemDelete(WsSampleGeneratedType.SOAP);
    }

    /**
     * @see edu.internet2.middleware.grouper.ws.samples.WsSampleGenerated#executeSample(edu.internet2.middleware.grouper.ws.samples.WsSampleGeneratedType)
     */
    public void executeSample(WsSampleGeneratedType wsSampleGeneratedType) {
        stemDelete(wsSampleGeneratedType);
    }

    /**
     * @param wsSampleGeneratedType can run as soap or xml/http
     */
    public static void stemDelete(WsSampleGeneratedType wsSampleGeneratedType) {
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

            if (WsSampleGeneratedType.XML_HTTP.equals(wsSampleGeneratedType)) {
                options.setProperty(Constants.Configuration.ENABLE_REST,
                    Constants.VALUE_TRUE);
            }

            StemDelete stemDelete = StemDelete.class.newInstance();

            //version, e.g. v1_3_000
            stemDelete.setClientVersion(GeneratedClientSettings.VERSION);

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
