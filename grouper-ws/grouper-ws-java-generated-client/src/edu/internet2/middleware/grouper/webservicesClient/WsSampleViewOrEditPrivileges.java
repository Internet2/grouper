/**
 *
 */
package edu.internet2.middleware.grouper.webservicesClient;

import edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.ViewOrEditPrivileges;
import edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.WsGroupLookup;
import edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.WsSubjectLookup;
import edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.WsViewOrEditPrivilegesResult;
import edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.WsViewOrEditPrivilegesResults;
import edu.internet2.middleware.grouper.webservicesClient.util.GeneratedClientSettings;
import edu.internet2.middleware.grouper.ws.samples.WsSampleGenerated;
import edu.internet2.middleware.grouper.ws.samples.WsSampleGeneratedType;

import org.apache.axis2.Constants;
import org.apache.axis2.client.Options;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.axis2.transport.http.HttpTransportProperties;

import org.apache.commons.lang.builder.ToStringBuilder;

import java.lang.reflect.Array;


/**
 *
 * @author mchyzer
 *
 */
public class WsSampleViewOrEditPrivileges implements WsSampleGenerated {
    /**
     * @param args
     */
    public static void main(String[] args) {
        viewOrEditPrivileges(WsSampleGeneratedType.SOAP);
    }

    /**
     * @see edu.internet2.middleware.grouper.ws.samples.WsSampleGenerated#executeSample(edu.internet2.middleware.grouper.ws.samples.WsSampleGeneratedType)
     */
    public void executeSample(WsSampleGeneratedType wsSampleGeneratedType) {
        viewOrEditPrivileges(wsSampleGeneratedType);
    }

    /**
     * @param wsSampleGeneratedType can run as soap or xml/http
     */
    public static void viewOrEditPrivileges(
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

            ViewOrEditPrivileges viewOrEditPrivileges = ViewOrEditPrivileges.class.newInstance();

            //version, e.g. v1_3_000
            viewOrEditPrivileges.setClientVersion(GeneratedClientSettings.VERSION);

            // set the act as id
            WsSubjectLookup actAsSubject = WsSubjectLookup.class.newInstance();
            actAsSubject.setSubjectId("GrouperSystem");
            viewOrEditPrivileges.setActAsSubjectLookup(actAsSubject);

            WsGroupLookup wsGroupLookup = WsGroupLookup.class.newInstance();
            wsGroupLookup.setGroupName("aStem:aGroup");

            /*
               viewOrEditPrivileges.setWsGroupLookup(wsGroupLookup);
               // add two subjects to the group
               WsSubjectLookup[] subjectLookups = (WsSubjectLookup[]) Array.newInstance(WsSubjectLookup.class,
                       2);
               subjectLookups[0] = WsSubjectLookup.class.newInstance();
               subjectLookups[0].setSubjectId("10021368");
               subjectLookups[1] = WsSubjectLookup.class.newInstance();
               subjectLookups[1].setSubjectId("10039438");
               viewOrEditPrivileges.setSubjectLookups(subjectLookups);
               //change some privs
               viewOrEditPrivileges.setAdminAllowed("");
               viewOrEditPrivileges.setOptinAllowed("T");
               viewOrEditPrivileges.setOptoutAllowed("");
               viewOrEditPrivileges.setReadAllowed("");
               viewOrEditPrivileges.setUpdateAllowed("F");
               viewOrEditPrivileges.setViewAllowed("");
               WsViewOrEditPrivilegesResults wsViewOrEditPrivilegesResults = stub.viewOrEditPrivileges(viewOrEditPrivileges)
                                                                                 .get_return();
               System.out.println(ToStringBuilder.reflectionToString(
                       wsViewOrEditPrivilegesResults));
               for (WsViewOrEditPrivilegesResult result : wsViewOrEditPrivilegesResults.getResults()) {
                   System.out.println(ToStringBuilder.reflectionToString(result));
               }
             */
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
