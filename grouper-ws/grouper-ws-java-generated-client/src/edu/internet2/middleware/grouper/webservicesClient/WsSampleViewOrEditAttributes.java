/**
 *
 */
package edu.internet2.middleware.grouper.webservicesClient;

import edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.ViewOrEditAttributes;
import edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.WsAttribute;
import edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.WsAttributeEdit;
import edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.WsGroupLookup;
import edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.WsSubjectLookup;
import edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.WsViewOrEditAttributesResult;
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
public class WsSampleViewOrEditAttributes implements WsSampleGenerated {
    /**
     * main method
     * @param args cmd line args
     */
    public static void main(String[] args) {
        viewOrEditAttributes(WsSampleGeneratedType.soap);
    }

    /**
     * @see edu.internet2.middleware.grouper.ws.samples.types.WsSampleGenerated#executeSample(edu.internet2.middleware.grouper.ws.samples.types.WsSampleGeneratedType)
     */
    public void executeSample(WsSampleGeneratedType wsSampleGeneratedType) {
        viewOrEditAttributes(wsSampleGeneratedType);
    }

    /**
     * run the ws
     * @param wsSampleGeneratedType can run as soap or xml/http
     */
    public static void viewOrEditAttributes(
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

            ViewOrEditAttributes viewOrEditAttributes = ViewOrEditAttributes.class.newInstance();

            //version, e.g. v1_3_000
            viewOrEditAttributes.setClientVersion(GeneratedClientSettings.VERSION);

            // set the act as id
            WsSubjectLookup actAsSubject = WsSubjectLookup.class.newInstance();
            actAsSubject.setSubjectId("GrouperSystem");
            viewOrEditAttributes.setActAsSubjectLookup(actAsSubject);

            WsGroupLookup wsGroupLookup = WsGroupLookup.class.newInstance();
            wsGroupLookup.setGroupName("aStem:aGroup");

            WsGroupLookup wsGroupLookup2 = WsGroupLookup.class.newInstance();
            wsGroupLookup2.setGroupName("aStem:aGroup2");
            viewOrEditAttributes.setWsGroupLookups(new WsGroupLookup[] {
                    wsGroupLookup, wsGroupLookup2
                });

            WsAttributeEdit wsAttributeEdit = WsAttributeEdit.class.newInstance();
            wsAttributeEdit.setName("description");
            wsAttributeEdit.setValue("some description");

            WsAttributeEdit wsAttributeEdit2 = WsAttributeEdit.class.newInstance();
            //note, this would be different in real life
            wsAttributeEdit2.setName("description");
            wsAttributeEdit2.setValue("some description");

            viewOrEditAttributes.setWsAttributeEdits(new WsAttributeEdit[] {
                    wsAttributeEdit, wsAttributeEdit2
                });

            WsViewOrEditAttributesResults wsViewOrEditAttributesResults = stub.viewOrEditAttributes(viewOrEditAttributes)
                                                                              .get_return();

            System.out.println(ToStringBuilder.reflectionToString(
                    wsViewOrEditAttributesResults));

            for (WsViewOrEditAttributesResult result : wsViewOrEditAttributesResults.getResults()) {
                System.out.println(ToStringBuilder.reflectionToString(result));

                for (WsAttribute wsAttribute : result.getAttributes()) {
                    System.out.println(ToStringBuilder.reflectionToString(
                            wsAttribute));
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
