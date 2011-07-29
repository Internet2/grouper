/**
 *
 */
package edu.internet2.middleware.grouper.webservicesClient;

import java.lang.reflect.Array;

import org.apache.axis2.client.Options;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.axis2.transport.http.HttpTransportProperties;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import edu.internet2.middleware.grouper.webservicesClient.util.GeneratedClientSettings;
import edu.internet2.middleware.grouper.ws.samples.types.WsSampleGenerated;
import edu.internet2.middleware.grouper.ws.samples.types.WsSampleGeneratedType;
import edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.AddMember;
import edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.WsAddMemberResult;
import edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.WsAddMemberResults;
import edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.WsGroupLookup;
import edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.WsSubjectLookup;


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
        addMember(WsSampleGeneratedType.soap);
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
            subjectLookups[0].setSubjectId("test.subject.0");
            subjectLookups[0].setSubjectSourceId("jdbc");
            subjectLookups[0].setSubjectIdentifier("");

            subjectLookups[1] = WsSubjectLookup.class.newInstance();
            subjectLookups[1].setSubjectId("");
            subjectLookups[1].setSubjectSourceId("");
            subjectLookups[1].setSubjectIdentifier("id.test.subject.1");

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

            if (!StringUtils.equals("T", 
                wsAddMemberResults.getResultMetadata().getSuccess())) {
              throw new RuntimeException("didnt get success! ");
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @see edu.internet2.middleware.grouper.ws.samples.types.WsSampleGenerated#executeSample(edu.internet2.middleware.grouper.ws.samples.types.WsSampleGeneratedType)
     */
    public void executeSample(WsSampleGeneratedType wsSampleGeneratedType) {
        addMember(wsSampleGeneratedType);
    }
}
