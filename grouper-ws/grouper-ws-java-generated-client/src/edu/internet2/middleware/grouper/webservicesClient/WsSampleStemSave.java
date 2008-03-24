/**
 *
 */
package edu.internet2.middleware.grouper.webservicesClient;

import edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.StemSave;
import edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.WsStemSaveResult;
import edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.WsStemSaveResults;
import edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.WsStemToSave;
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
public class WsSampleStemSave implements WsSampleGenerated {
    /**
     * @param args
     */
    public static void main(String[] args) {
        stemSave(WsSampleGeneratedType.SOAP);
    }

    /**
     * @see edu.internet2.middleware.grouper.ws.samples.WsSampleGenerated#executeSample(edu.internet2.middleware.grouper.ws.samples.WsSampleGeneratedType)
     */
    public void executeSample(WsSampleGeneratedType wsSampleGeneratedType) {
        stemSave(wsSampleGeneratedType);
    }

    /**
     * @param wsSampleGeneratedType can run as soap or xml/http
     */
    public static void stemSave(WsSampleGeneratedType wsSampleGeneratedType) {
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

            StemSave stemSave = StemSave.class.newInstance();

            //version, e.g. v1_3_000
            stemSave.setClientVersion(GeneratedClientSettings.VERSION);

            // set the act as id
            WsSubjectLookup actAsSubject = WsSubjectLookup.class.newInstance();
            actAsSubject.setSubjectId("GrouperSystem");
            stemSave.setActAsSubjectLookup(actAsSubject);

            WsStemToSave wsStemToSave = WsStemToSave.class.newInstance();

            /*
               wsStemToSave.setCreateStemsIfNotExist("");
               wsStemToSave.setDescription("the test stem");
               wsStemToSave.setDisplayExtension("test stem");
               wsStemToSave.setStemName("aStem:testStem");
               wsStemToSave.setSaveMode("");
               wsStemToSave.setStemUuid("");
               stemSave.setWsStemsToSave(new WsStemToSave[] { wsStemToSave });
               WsStemSaveResults wsStemSaveResults = stub.stemSave(stemSave)
                                                         .get_return();
               System.out.println(ToStringBuilder.reflectionToString(
                       wsStemSaveResults));
               WsStemSaveResult[] wsStemSaveResultArray = wsStemSaveResults.getResults();
               if (wsStemSaveResultArray != null) {
                   for (WsStemSaveResult wsStemSaveResult : wsStemSaveResultArray) {
                       System.out.println(ToStringBuilder.reflectionToString(
                               wsStemSaveResult));
                   }
               }
             */
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
