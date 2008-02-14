/**
 *
 */
package edu.internet2.middleware.grouper.webservicesClient;

import org.apache.axis2.Constants;
import org.apache.axis2.client.Options;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.axis2.transport.http.HttpTransportProperties;
import org.apache.commons.lang.builder.ToStringBuilder;

import edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.StemDeleteSimple;
import edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.WsStemDeleteResult;

/**
 *
 * @author mchyzer
 *
 */
public class RunGrouperServiceStemDeleteSimple {
    /**
     * @param args
     */
    public static void main(String[] args) {
        stemDeleteSimple();
    }

    /**
     *
     */
    public static void stemDeleteSimple() {
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
            StemDeleteSimple stemDeleteSimple = StemDeleteSimple.class.newInstance();

            stemDeleteSimple.setActAsSubjectId("GrouperSystem");
            stemDeleteSimple.setActAsSubjectIdentifier("");
            stemDeleteSimple.setStemName("aStem:stemNotExist");
            stemDeleteSimple.setStemUuid("");
            
            WsStemDeleteResult wsStemDeleteResult = stub.stemDeleteSimple(stemDeleteSimple)
                                                            .get_return();

            System.out.println(ToStringBuilder.reflectionToString(
                    wsStemDeleteResult));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
