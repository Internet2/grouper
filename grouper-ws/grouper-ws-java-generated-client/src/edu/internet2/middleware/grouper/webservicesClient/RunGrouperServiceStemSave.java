/**
 *
 */
package edu.internet2.middleware.grouper.webservicesClient;

import edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.StemSave;
import edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.WsStemSaveResult;
import edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.WsStemSaveResults;
import edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.WsStemToSave;
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
public class RunGrouperServiceStemSave {
    /**
     * @param args
     */
    public static void main(String[] args) {
        stemSave();
    }

    /**
     *
     */
    public static void stemSave() {
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
            //                Constants.VALUE_TRUE);
            StemSave stemSave = StemSave.class.newInstance();

            // set the act as id
            WsSubjectLookup actAsSubject = WsSubjectLookup.class.newInstance();
            actAsSubject.setSubjectId("GrouperSystem");
            stemSave.setActAsSubjectLookup(actAsSubject);

            WsStemToSave wsStemToSave = WsStemToSave.class.newInstance();

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
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
