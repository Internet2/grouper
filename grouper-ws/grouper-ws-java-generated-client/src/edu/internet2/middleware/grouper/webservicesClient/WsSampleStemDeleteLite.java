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
import edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.StemDeleteLite;
import edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.WsStemDeleteLiteResult;


/**
 *
 * @author mchyzer
 *
 */
public class WsSampleStemDeleteLite implements WsSampleGenerated {
    /**
     * @param args
     */
    public static void main(String[] args) {
        stemDeleteLite(WsSampleGeneratedType.soap);
    }

    /**
     * @see edu.internet2.middleware.grouper.ws.samples.types.WsSampleGenerated#executeSample(edu.internet2.middleware.grouper.ws.samples.types.WsSampleGeneratedType)
     */
    public void executeSample(WsSampleGeneratedType wsSampleGeneratedType) {
        stemDeleteLite(wsSampleGeneratedType);
    }

    /**
     * @param wsSampleGeneratedType can run as soap or xml/http
     */
    public static void stemDeleteLite(
        WsSampleGeneratedType wsSampleGeneratedType) {
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

            StemDeleteLite stemDeleteLite = StemDeleteLite.class.newInstance();

            //version, e.g. v1_3_000
            stemDeleteLite.setClientVersion(GeneratedClientSettings.VERSION);

            stemDeleteLite.setActAsSubjectId("GrouperSystem");
            stemDeleteLite.setActAsSubjectIdentifier("");
            stemDeleteLite.setStemName("aStem:stemNotExist");
            stemDeleteLite.setStemUuid("");

            WsStemDeleteLiteResult wsStemDeleteLiteResult = stub.stemDeleteLite(stemDeleteLite)
                                                                .get_return();

            System.out.println(ToStringBuilder.reflectionToString(
                    wsStemDeleteLiteResult));
            if (!StringUtils.equals("T", 
                wsStemDeleteLiteResult.getResultMetadata().getSuccess())) {
              throw new RuntimeException("didnt get success! ");
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
