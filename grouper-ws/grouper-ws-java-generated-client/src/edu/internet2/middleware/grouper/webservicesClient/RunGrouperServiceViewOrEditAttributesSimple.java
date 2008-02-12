/**
 *
 */
package edu.internet2.middleware.grouper.webservicesClient;

import org.apache.axis2.client.Options;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.axis2.transport.http.HttpTransportProperties;
import org.apache.commons.lang.builder.ToStringBuilder;

import edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.ViewOrEditAttributesSimple;
import edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.WsViewOrEditAttributesResult;


/**
 *
 * @author mchyzer
 *
 */
public class RunGrouperServiceViewOrEditAttributesSimple {
    /**
     *
     */
    public static void viewOrEditAttributesSimple() {
        try {
            GrouperServiceStub stub = new GrouperServiceStub(
                    "http://localhost:8091/grouper-ws/services/GrouperService");
            Options options = stub._getServiceClient().getOptions();
            HttpTransportProperties.Authenticator auth = new HttpTransportProperties.Authenticator();
            auth.setUsername("GrouperSystem");
            auth.setPassword("pass");

            options.setProperty(HTTPConstants.AUTHENTICATE, auth);

            //options.setProperty(Constants.Configuration.ENABLE_REST,
            //		Constants.VALUE_TRUE);
            ViewOrEditAttributesSimple viewOrEditAttributesSimple = ViewOrEditAttributesSimple.class.newInstance();

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

            WsViewOrEditAttributesResult wsViewOrEditAttributesResult = stub.viewOrEditAttributesSimple(viewOrEditAttributesSimple)
                                                                            .get_return();

            System.out.println(ToStringBuilder.reflectionToString(
            		wsViewOrEditAttributesResult));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        viewOrEditAttributesSimple();
    }
}
