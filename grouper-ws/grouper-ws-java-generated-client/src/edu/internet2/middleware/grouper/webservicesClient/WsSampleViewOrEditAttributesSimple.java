/**
 *
 */
package edu.internet2.middleware.grouper.webservicesClient;

import edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.ViewOrEditAttributesSimple;
import edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.WsViewOrEditAttributesResults;
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
public class WsSampleViewOrEditAttributesSimple implements WsSampleGenerated {
    /**
     * @see edu.internet2.middleware.grouper.ws.samples.WsSampleGenerated#executeSample(edu.internet2.middleware.grouper.ws.samples.WsSampleGeneratedType)
     */
    public void executeSample(WsSampleGeneratedType wsSampleGeneratedType) {
        viewOrEditAttributesSimple(wsSampleGeneratedType);
    }

    /**
     * @param wsSampleGeneratedType can run as soap or xml/http
     */
    public static void viewOrEditAttributesSimple(
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

            if (WsSampleGeneratedType.XML_HTTP.equals(wsSampleGeneratedType)) {
                options.setProperty(Constants.Configuration.ENABLE_REST,
                    Constants.VALUE_TRUE);
            }

            ViewOrEditAttributesSimple viewOrEditAttributesSimple = ViewOrEditAttributesSimple.class.newInstance();

            //version, e.g. v1_3_000
            viewOrEditAttributesSimple.setClientVersion(GeneratedClientSettings.VERSION);

            // set the act as id
            viewOrEditAttributesSimple.setActAsSubjectId("GrouperSystem");
            viewOrEditAttributesSimple.setActAsSubjectIdentifier("");
            viewOrEditAttributesSimple.setGroupName("aStem:aGroup");
            viewOrEditAttributesSimple.setGroupUuid("");
            viewOrEditAttributesSimple.setAttributeName0("description");
            viewOrEditAttributesSimple.setAttributeValue0("some description");
            viewOrEditAttributesSimple.setAttributeDelete0("");
            viewOrEditAttributesSimple.setAttributeName1("description");
            viewOrEditAttributesSimple.setAttributeValue1("some description");
            viewOrEditAttributesSimple.setAttributeDelete1("");
            viewOrEditAttributesSimple.setAttributeName2("");
            viewOrEditAttributesSimple.setAttributeValue2("");
            viewOrEditAttributesSimple.setAttributeDelete2("");

            WsViewOrEditAttributesResults wsViewOrEditAttributesResults = stub.viewOrEditAttributesSimple(viewOrEditAttributesSimple)
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
        viewOrEditAttributesSimple(WsSampleGeneratedType.SOAP);
    }
}
