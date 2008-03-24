/**
 *
 */
package edu.internet2.middleware.grouper.webservicesClient;

import edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.AddMember;
import edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.WsAddMemberResult;
import edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.WsAddMemberResults;
import edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.WsGroupLookup;
import edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.WsSubjectLookup;
import edu.internet2.middleware.grouper.webservicesClient.util.GeneratedClientSettings;
import edu.internet2.middleware.grouper.ws.samples.WsSampleGenerated;
import edu.internet2.middleware.grouper.ws.samples.WsSampleGeneratedType;

import org.apache.axis2.Constants;
import org.apache.axis2.client.Options;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.axis2.transport.http.HttpTransportProperties;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import java.lang.reflect.Array;


/**
 *
 * @author mchyzer
 *
 */
public class WsSampleAddMember implements WsSampleGenerated {
    /**
     * @param args
     */
    public static void main(String[] args) {
        addMember(WsSampleGeneratedType.SOAP);
    }

    /**
     * @param wsSampleGeneratedType can run as soap or xml/http
     */
    public static void addMember(WsSampleGeneratedType wsSampleGeneratedType) {
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

            AddMember addMember = AddMember.class.newInstance();

            // set the act as id
            WsSubjectLookup actAsSubject = WsSubjectLookup.class.newInstance();
            actAsSubject.setSubjectId("GrouperSystem");
            addMember.setActAsSubjectLookup(actAsSubject);

            // just add, dont replace
            addMember.setReplaceAllExisting("F");

            WsGroupLookup wsGroupLookup = WsGroupLookup.class.newInstance();
            wsGroupLookup.setGroupName("aStem:aGroup");
            addMember.setWsGroupLookup(wsGroupLookup);

            //version, e.g. v1_3_000
            addMember.setClientVersion(GeneratedClientSettings.VERSION);

            // add two subjects to the group
            WsSubjectLookup[] subjectLookups = (WsSubjectLookup[]) Array.newInstance(WsSubjectLookup.class,
                    2);
            subjectLookups[0] = WsSubjectLookup.class.newInstance();
            subjectLookups[0].setSubjectId("10021368");

            subjectLookups[1] = WsSubjectLookup.class.newInstance();
            subjectLookups[1].setSubjectId("10039438");

            addMember.setSubjectLookups(subjectLookups);

            WsAddMemberResults wsAddMemberResults = stub.addMember(addMember)
                                                        .get_return();

            System.out.println(ToStringBuilder.reflectionToString(
                    wsAddMemberResults, ToStringStyle.MULTI_LINE_STYLE));
            System.out.println(ToStringBuilder.reflectionToString(
                    wsAddMemberResults.getResultMetadata(),
                    ToStringStyle.MULTI_LINE_STYLE));

            if (wsAddMemberResults != null) {
                for (WsAddMemberResult wsAddMemberResult : wsAddMemberResults.getResults()) {
                    System.out.println(ToStringBuilder.reflectionToString(
                            wsAddMemberResult, ToStringStyle.MULTI_LINE_STYLE));
                    System.out.println(ToStringBuilder.reflectionToString(
                            wsAddMemberResult.getResultMetadata(),
                            ToStringStyle.MULTI_LINE_STYLE));
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @see edu.internet2.middleware.grouper.ws.samples.WsSampleGenerated#executeSample(edu.internet2.middleware.grouper.ws.samples.WsSampleGeneratedType)
     */
    public void executeSample(WsSampleGeneratedType wsSampleGeneratedType) {
        addMember(wsSampleGeneratedType);
    }
}
