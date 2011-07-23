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
import edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.GroupDeleteLite;
import edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.WsGroupDeleteLiteResult;


/**
 *
 * @author mchyzer
 *
 */
public class WsSampleGroupDeleteLite implements WsSampleGenerated {
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

            GroupDeleteLite groupDeleteLite = GroupDeleteLite.class.newInstance();

            //version, e.g. v1_3_000
            groupDeleteLite.setClientVersion(GeneratedClientSettings.VERSION);

            groupDeleteLite.setActAsSubjectId("GrouperSystem");
            groupDeleteLite.setActAsSubjectIdentifier("");
            groupDeleteLite.setActAsSubjectSourceId("");
            groupDeleteLite.setIncludeGroupDetail("T");
            groupDeleteLite.setGroupName("aStem:test");
            groupDeleteLite.setGroupUuid("");

            WsGroupDeleteLiteResult wsGroupDeleteLiteResult = stub.groupDeleteLite(groupDeleteLite)
                                                                  .get_return();

            System.out.println(ToStringBuilder.reflectionToString(
                    wsGroupDeleteLiteResult));
            if (!StringUtils.equals("T", 
                wsGroupDeleteLiteResult.getResultMetadata().getSuccess())) {
              throw new RuntimeException("didnt get success! ");
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
