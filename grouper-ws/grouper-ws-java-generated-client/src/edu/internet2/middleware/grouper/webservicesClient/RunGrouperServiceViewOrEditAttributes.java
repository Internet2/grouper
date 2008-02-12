/**
 *
 */
package edu.internet2.middleware.grouper.webservicesClient;

import org.apache.axis2.Constants;
import org.apache.axis2.client.Options;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.axis2.transport.http.HttpTransportProperties;
import org.apache.commons.lang.builder.ToStringBuilder;

import edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.ViewOrEditAttributes;
import edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.WsAttribute;
import edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.WsAttributeEdit;
import edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.WsGroupLookup;
import edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.WsSubjectLookup;
import edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.WsViewOrEditAttributesResult;
import edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.WsViewOrEditAttributesResults;


/**
 * Use ant script to generate client, but if manual, do this:
 *
 * Generate the code:
 *
 * C:\mchyzer\isc\dev\grouper\grouper-ws-java-generated-client>wsdl2java -p
 * edu.internet2.middleware.grouper.webservicesClient -t -uri GrouperService.wsdl
 *
 * @author mchyzer
 *
 */
public class RunGrouperServiceViewOrEditAttributes {
	
    /**
     * main method
     * @param args cmd line args
     */
    public static void main(String[] args) {
        viewOrEditAttributes();
    }

    /**
     * run the ws
     */
    public static void viewOrEditAttributes() {
        try {
            GrouperServiceStub stub = new GrouperServiceStub(
                    "http://localhost:8091/grouper-ws/services/GrouperService");
            Options options = stub._getServiceClient().getOptions();
            HttpTransportProperties.Authenticator auth = new HttpTransportProperties.Authenticator();
            auth.setUsername("GrouperSystem");
            auth.setPassword("pass");

            options.setProperty(HTTPConstants.AUTHENTICATE, auth);

            options.setProperty(Constants.Configuration.ENABLE_REST,
                Constants.VALUE_TRUE);

            ViewOrEditAttributes viewOrEditAttributes = ViewOrEditAttributes.class.newInstance();

            // set the act as id
            WsSubjectLookup actAsSubject = WsSubjectLookup.class.newInstance();
            actAsSubject.setSubjectId("GrouperSystem");
            viewOrEditAttributes.setActAsSubjectLookup(actAsSubject);

            WsGroupLookup wsGroupLookup = WsGroupLookup.class.newInstance();
            wsGroupLookup.setGroupName("aStem:aGroup");
            WsGroupLookup wsGroupLookup2 = WsGroupLookup.class.newInstance();
            wsGroupLookup2.setGroupName("aStem:aGroup2");
            viewOrEditAttributes.setWsGroupLookups(new WsGroupLookup[]{ wsGroupLookup, wsGroupLookup2});
            
            WsAttributeEdit wsAttributeEdit = WsAttributeEdit.class.newInstance();
            wsAttributeEdit.setName("description");
            wsAttributeEdit.setValue("some description");
            WsAttributeEdit wsAttributeEdit2 = WsAttributeEdit.class.newInstance();
            //note, this would be different in real life
            wsAttributeEdit2.setName("description");
            wsAttributeEdit2.setValue("some description");

            viewOrEditAttributes.setWsAttributeEdits(new WsAttributeEdit[]{wsAttributeEdit, wsAttributeEdit2});

            WsViewOrEditAttributesResults wsViewOrEditAttributesResults = stub
            	.viewOrEditAttributes(viewOrEditAttributes).get_return();

            System.out.println(ToStringBuilder.reflectionToString(
                    wsViewOrEditAttributesResults));

            for (WsViewOrEditAttributesResult result : wsViewOrEditAttributesResults.getResults()) {
                System.out.println(ToStringBuilder.reflectionToString(result));
            	for (WsAttribute wsAttribute : result.getAttributes()) {
            		System.out.println(ToStringBuilder.reflectionToString(wsAttribute));
            	}
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
