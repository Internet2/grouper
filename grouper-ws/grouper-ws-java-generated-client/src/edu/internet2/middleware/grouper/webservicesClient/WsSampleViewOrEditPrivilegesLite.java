/**
 *
 */
package edu.internet2.middleware.grouper.webservicesClient;

import edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.ViewOrEditPrivilegesLite;
import edu.internet2.middleware.grouper.webservicesClient.util.GeneratedClientSettings;
import edu.internet2.middleware.grouper.ws.samples.types.WsSampleGenerated;
import edu.internet2.middleware.grouper.ws.samples.types.WsSampleGeneratedType;

import org.apache.axis2.client.Options;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.axis2.transport.http.HttpTransportProperties;


/**
 *
 * @author mchyzer
 *
 */
public class WsSampleViewOrEditPrivilegesLite implements WsSampleGenerated {
    /**
     * @see edu.internet2.middleware.grouper.ws.samples.types.WsSampleGenerated#executeSample(edu.internet2.middleware.grouper.ws.samples.types.WsSampleGeneratedType)
     */
    public void executeSample(WsSampleGeneratedType wsSampleGeneratedType) {
        viewOrEditPrivilegesLite(wsSampleGeneratedType);
    }

    /**
     * @param wsSampleGeneratedType can run as soap or xml/http
     */
    public static void viewOrEditPrivilegesLite(
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

            ViewOrEditPrivilegesLite viewOrEditPrivilegesLite = ViewOrEditPrivilegesLite.class.newInstance();

            //version, e.g. v1_3_000
            viewOrEditPrivilegesLite.setClientVersion(GeneratedClientSettings.VERSION);

            /*
               // set the act as id
               viewOrEditPrivilegesLite.setActAsSubjectId("GrouperSystem");
               viewOrEditPrivilegesLite.setActAsSubjectIdentifier("");
               viewOrEditPrivilegesLite.setAdminAllowed("");
               viewOrEditPrivilegesLite.setGroupName("aStem:aGroup");
               viewOrEditPrivilegesLite.setGroupUuid("");
               viewOrEditPrivilegesLite.setOptinAllowed("T");
               viewOrEditPrivilegesLite.setOptoutAllowed("F");
               viewOrEditPrivilegesLite.setReadAllowed("");
               viewOrEditPrivilegesLite.setSubjectId("10021368");
               viewOrEditPrivilegesLite.setSubjectIdentifier("");
               viewOrEditPrivilegesLite.setUpdateAllowed("");
               viewOrEditPrivilegesLite.setViewAllowed("");
               WsViewOrEditPrivilegesResult wsViewOrEditPrivilegesResult = stub.viewOrEditPrivilegesLite(viewOrEditPrivilegesLite)
                                                                               .get_return();
               System.out.println(ToStringBuilder.reflectionToString(
                       wsViewOrEditPrivilegesResult));
             */
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        viewOrEditPrivilegesLite(WsSampleGeneratedType.soap);
    }
}
