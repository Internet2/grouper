/**
 *
 */
package edu.internet2.middleware.grouper.webservicesClient;

import edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.ViewOrEditAttributesLite;
import edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.WsViewOrEditAttributesResults;
import edu.internet2.middleware.grouper.webservicesClient.util.GeneratedClientSettings;
import edu.internet2.middleware.grouper.ws.samples.types.WsSampleGenerated;
import edu.internet2.middleware.grouper.ws.samples.types.WsSampleGeneratedType;

import org.apache.axis2.client.Options;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.axis2.transport.http.HttpTransportProperties;

import org.apache.commons.lang.builder.ToStringBuilder;


/**
 *
 * @author mchyzer
 *
 */
public class WsSampleViewOrEditAttributesLite implements WsSampleGenerated {
    /**
     * @see edu.internet2.middleware.grouper.ws.samples.types.WsSampleGenerated#executeSample(edu.internet2.middleware.grouper.ws.samples.types.WsSampleGeneratedType)
     */
    public void executeSample(WsSampleGeneratedType wsSampleGeneratedType) {
        viewOrEditAttributesLite(wsSampleGeneratedType);
    }

    /**
     * @param wsSampleGeneratedType can run as soap or xml/http
     */
    public static void viewOrEditAttributesLite(
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

            ViewOrEditAttributesLite viewOrEditAttributesLite = ViewOrEditAttributesLite.class.newInstance();

            //version, e.g. v1_3_000
            viewOrEditAttributesLite.setClientVersion(GeneratedClientSettings.VERSION);

            // set the act as id
            viewOrEditAttributesLite.setActAsSubjectId("GrouperSystem");
            viewOrEditAttributesLite.setActAsSubjectIdentifier("");
            viewOrEditAttributesLite.setGroupName("aStem:aGroup");
            viewOrEditAttributesLite.setGroupUuid("");
            viewOrEditAttributesLite.setAttributeName0("description");
            viewOrEditAttributesLite.setAttributeValue0("some description");
            viewOrEditAttributesLite.setAttributeDelete0("");
            viewOrEditAttributesLite.setAttributeName1("description");
            viewOrEditAttributesLite.setAttributeValue1("some description");
            viewOrEditAttributesLite.setAttributeDelete1("");
            viewOrEditAttributesLite.setAttributeName2("");
            viewOrEditAttributesLite.setAttributeValue2("");
            viewOrEditAttributesLite.setAttributeDelete2("");

            WsViewOrEditAttributesResults wsViewOrEditAttributesResults = stub.viewOrEditAttributesLite(viewOrEditAttributesLite)
                                                                              .get_return();

            System.out.println(ToStringBuilder.reflectionToString(
                    wsViewOrEditAttributesResults));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        viewOrEditAttributesLite(WsSampleGeneratedType.soap);
    }
}
