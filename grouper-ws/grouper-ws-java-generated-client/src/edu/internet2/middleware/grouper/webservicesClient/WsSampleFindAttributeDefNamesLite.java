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
import edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.FindAttributeDefNamesLite;
import edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsFindAttributeDefNamesResults;
import edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsAttributeDefName;


/**
 *
 * @author mchyzer
 *
 */
public class WsSampleFindAttributeDefNamesLite implements WsSampleGenerated {
    /**
     * @see edu.internet2.middleware.grouper.ws.samples.types.WsSampleGenerated#executeSample(edu.internet2.middleware.grouper.ws.samples.types.WsSampleGeneratedType)
     */
    public void executeSample(WsSampleGeneratedType wsSampleGeneratedType) {
        findAttributeDefNamesLite(wsSampleGeneratedType);
    }

    /**
     * @param wsSampleGeneratedType can run as soap or xml/http
     */
    public static void findAttributeDefNamesLite(
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

            FindAttributeDefNamesLite findAttributeDefNamesLite = FindAttributeDefNamesLite.class.newInstance();

            //version, e.g. v1_3_000
            findAttributeDefNamesLite.setClientVersion(GeneratedClientSettings.VERSION);
            //this will find everything in the test stem and substems
            findAttributeDefNamesLite.setScope("test:");

            // set the act as id
            // findAttributeDefNamesLite.setActAsSubjectId("GrouperSystem");
            WsFindAttributeDefNamesResults wsAttributeDefNamesResults = stub.findAttributeDefNamesLite(findAttributeDefNamesLite)
                                                      .get_return();

            System.out.println(ToStringBuilder.reflectionToString(
                    wsAttributeDefNamesResults));

            WsAttributeDefName[] wsAttributeDefNames = wsAttributeDefNamesResults.getAttributeDefNameResults();

            if (wsAttributeDefNames != null) {
                for (WsAttributeDefName wsAttributeDefName : wsAttributeDefNames) {
                    System.out.println(ToStringBuilder.reflectionToString(
                            wsAttributeDefName));
                }
            }
            
            if (!StringUtils.equals("T", 
                wsAttributeDefNamesResults.getResultMetadata().getSuccess())) {
              throw new RuntimeException("didnt get success! ");
            }
            
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        findAttributeDefNamesLite(WsSampleGeneratedType.soap);
    }
}
