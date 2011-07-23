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
import edu.internet2.middleware.grouper.webservicesClient.util.GeneratedUtils;
import edu.internet2.middleware.grouper.ws.samples.types.WsSampleGenerated;
import edu.internet2.middleware.grouper.ws.samples.types.WsSampleGeneratedType;
import edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.GetSubjectsLite;
import edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.WsGetSubjectsResults;
import edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.WsSubject;


/**
 *
 * @author mchyzer
 *
 */
public class WsSampleGetSubjectsLite implements WsSampleGenerated {
    /**
     * @see edu.internet2.middleware.grouper.ws.samples.types.WsSampleGenerated#executeSample(edu.internet2.middleware.grouper.ws.samples.types.WsSampleGeneratedType)
     */
    public void executeSample(WsSampleGeneratedType wsSampleGeneratedType) {
        getSubjectsLite(wsSampleGeneratedType);
    }

    /**
     * @param wsSampleGeneratedType can run as soap or xml/http
     */
    public static void getSubjectsLite(
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

            GetSubjectsLite getSubjectsLite = GetSubjectsLite.class.newInstance();

            //version, e.g. v1_3_000
            getSubjectsLite.setClientVersion(GeneratedClientSettings.VERSION);

            // set the act as id
            getSubjectsLite.setActAsSubjectId("GrouperSystem");
            getSubjectsLite.setActAsSubjectIdentifier("");
            getSubjectsLite.setActAsSubjectSourceId("");
            getSubjectsLite.setFieldName("");
            getSubjectsLite.setGroupName("aStem:aGroup");
            getSubjectsLite.setGroupUuid("");
            getSubjectsLite.setIncludeGroupDetail("");
            getSubjectsLite.setIncludeSubjectDetail("F");
            getSubjectsLite.setParamName0("");
            getSubjectsLite.setParamValue0("");
            getSubjectsLite.setParamName1("");
            getSubjectsLite.setParamValue1("");
            getSubjectsLite.setSearchString("test");
            getSubjectsLite.setSourceId("");
            getSubjectsLite.setSourceIds("");
            getSubjectsLite.setSubjectAttributeNames("");
            getSubjectsLite.setSubjectId("");
            getSubjectsLite.setSubjectIdentifier("");
            getSubjectsLite.setWsMemberFilter("");
            
            WsGetSubjectsResults wsGetSubjectsResult = stub.getSubjectsLite(getSubjectsLite)
                                                                .get_return();

            System.out.println(ToStringBuilder.reflectionToString(
                    wsGetSubjectsResult));

            WsSubject[] wsSubjectArray = wsGetSubjectsResult.getWsSubjects();

            for (WsSubject wsSubject : GeneratedUtils.nonNull(wsSubjectArray)) {
                System.out.println(ToStringBuilder.reflectionToString(wsSubject));
            }

            if (!StringUtils.equals("T", 
                wsGetSubjectsResult.getResultMetadata().getSuccess())) {
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
        getSubjectsLite(WsSampleGeneratedType.soap);
    }
}
