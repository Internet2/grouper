/**
 *
 */
package edu.internet2.middleware.grouper.webservicesClient;

import edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.ViewOrEditPrivilegesSimple;
import edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.WsViewOrEditPrivilegesResult;
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
public class WsSampleViewOrEditPrivilegesSimple implements WsSampleGenerated {
    /**
     * @see edu.internet2.middleware.grouper.ws.samples.WsSampleGenerated#executeSample(edu.internet2.middleware.grouper.ws.samples.WsSampleGeneratedType)
     */
    public void executeSample(WsSampleGeneratedType wsSampleGeneratedType) {
        viewOrEditPrivilegesSimple(wsSampleGeneratedType);
    }

    /**
     * @param wsSampleGeneratedType can run as soap or xml/http
     */
    public static void viewOrEditPrivilegesSimple(
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

            ViewOrEditPrivilegesSimple viewOrEditPrivilegesSimple = ViewOrEditPrivilegesSimple.class.newInstance();

            //version, e.g. v1_3_000
            viewOrEditPrivilegesSimple.setClientVersion(GeneratedClientSettings.VERSION);

            /*
               // set the act as id
               viewOrEditPrivilegesSimple.setActAsSubjectId("GrouperSystem");
               viewOrEditPrivilegesSimple.setActAsSubjectIdentifier("");
               viewOrEditPrivilegesSimple.setAdminAllowed("");
               viewOrEditPrivilegesSimple.setGroupName("aStem:aGroup");
               viewOrEditPrivilegesSimple.setGroupUuid("");
               viewOrEditPrivilegesSimple.setOptinAllowed("T");
               viewOrEditPrivilegesSimple.setOptoutAllowed("F");
               viewOrEditPrivilegesSimple.setReadAllowed("");
               viewOrEditPrivilegesSimple.setSubjectId("10021368");
               viewOrEditPrivilegesSimple.setSubjectIdentifier("");
               viewOrEditPrivilegesSimple.setUpdateAllowed("");
               viewOrEditPrivilegesSimple.setViewAllowed("");
               WsViewOrEditPrivilegesResult wsViewOrEditPrivilegesResult = stub.viewOrEditPrivilegesSimple(viewOrEditPrivilegesSimple)
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
        viewOrEditPrivilegesSimple(WsSampleGeneratedType.SOAP);
    }
}
