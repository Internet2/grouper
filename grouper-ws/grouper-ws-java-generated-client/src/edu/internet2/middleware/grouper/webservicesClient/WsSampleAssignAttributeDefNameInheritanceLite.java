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
import edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.AssignAttributeDefNameInheritanceLite;
import edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsAssignAttributeDefNameInheritanceResults;


/**
 *
 * @author mchyzer
 *
 */
public class WsSampleAssignAttributeDefNameInheritanceLite implements WsSampleGenerated {
    /**
     * @see edu.internet2.middleware.grouper.ws.samples.types.WsSampleGenerated#executeSample(edu.internet2.middleware.grouper.ws.samples.types.WsSampleGeneratedType)
     */
    public void executeSample(WsSampleGeneratedType wsSampleGeneratedType) {
        assignAttributeDefNameInheritanceLite(wsSampleGeneratedType);
    }

    /**
     * @param wsSampleGeneratedType can run as soap or xml/http
     */
    public static void assignAttributeDefNameInheritanceLite(
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

            AssignAttributeDefNameInheritanceLite assignAttributeDefNameInheritanceLite = AssignAttributeDefNameInheritanceLite.class.newInstance();

            //version, e.g. v1_3_000
            assignAttributeDefNameInheritanceLite.setClientVersion(GeneratedClientSettings.VERSION);
            
            //parent of the inheritance
            assignAttributeDefNameInheritanceLite.setAttributeDefNameName("aStem:permissionDefName");
            
            //we are doing an assignment
            assignAttributeDefNameInheritanceLite.setAssign("T");
            
            //this is the child of the relation
            assignAttributeDefNameInheritanceLite.setRelatedAttributeDefNameName("aStem:permissionDefName3");

            // set the act as id
            // findAttributeDefNamesLite.setActAsSubjectId("GrouperSystem");
            WsAssignAttributeDefNameInheritanceResults wsAssignAttributeDefNameInheritanceResults = stub.assignAttributeDefNameInheritanceLite(assignAttributeDefNameInheritanceLite)
                                                      .get_return();

            System.out.println(ToStringBuilder.reflectionToString(
                    wsAssignAttributeDefNameInheritanceResults));

            if (!StringUtils.equals("T", 
                wsAssignAttributeDefNameInheritanceResults.getResultMetadata().getSuccess())) {
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
        assignAttributeDefNameInheritanceLite(WsSampleGeneratedType.soap);
    }
}
