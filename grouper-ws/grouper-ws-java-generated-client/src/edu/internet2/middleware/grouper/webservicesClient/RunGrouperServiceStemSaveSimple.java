/**
 *
 */
package edu.internet2.middleware.grouper.webservicesClient;

import edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.StemSaveSimple;
import edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.WsStemSaveResult;

import org.apache.axis2.client.Options;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.axis2.transport.http.HttpTransportProperties;

import org.apache.commons.lang.builder.ToStringBuilder;


/**
 *
 * @author mchyzer
 *
 */
public class RunGrouperServiceStemSaveSimple {
    /**
     * @param args
     */
    public static void main(String[] args) {
        stemSaveSimple();
    }

    /**
     *
     */
    public static void stemSaveSimple() {
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
            StemSaveSimple stemSaveSimple = StemSaveSimple.class.newInstance();

            stemSaveSimple.setActAsSubjectId("GrouperSystem");
            stemSaveSimple.setActAsSubjectIdentifier("");
            stemSaveSimple.setCreateStemsIfNotExist("");
            stemSaveSimple.setDescription("test stem");
            stemSaveSimple.setDisplayExtension("the test stem");
            stemSaveSimple.setStemName("aStem:test");
            stemSaveSimple.setStemUuid("");
            stemSaveSimple.setSaveMode("");

            WsStemSaveResult wsStemSaveResult = stub.stemSaveSimple(stemSaveSimple)
                                                    .get_return();

            System.out.println(ToStringBuilder.reflectionToString(
                    wsStemSaveResult));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
