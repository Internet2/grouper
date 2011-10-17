/**
 *
 */
package edu.internet2.middleware.grouper.webservicesClient;

import org.apache.axis2.client.Options;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.axis2.transport.http.HttpTransportProperties;
import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.params.HttpClientParams;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;

import edu.internet2.middleware.grouper.webservicesClient.util.GeneratedClientSettings;
import edu.internet2.middleware.grouper.ws.samples.types.WsSampleGenerated;
import edu.internet2.middleware.grouper.ws.samples.types.WsSampleGeneratedType;
import edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GetGrouperPrivilegesLite;
import edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsGetGrouperPrivilegesLiteResult;


/**
 *
 * @author mchyzer
 *
 */
public class WsSampleGetGrouperPrivilegesLite implements WsSampleGenerated {
    /**
     * @see edu.internet2.middleware.grouper.ws.samples.types.WsSampleGenerated#executeSample(edu.internet2.middleware.grouper.ws.samples.types.WsSampleGeneratedType)
     */
    public void executeSample(WsSampleGeneratedType wsSampleGeneratedType) {
        wsGetGrouperPrivilegesLite(wsSampleGeneratedType);
    }

    /**
     * @param wsSampleGeneratedType can run as soap or xml/http
     */
    @SuppressWarnings("deprecation")
    public static void wsGetGrouperPrivilegesLite(
        WsSampleGeneratedType wsSampleGeneratedType) {
        try {
            //URL, e.g. http://localhost:8091/grouper-ws/services/GrouperService
            GrouperServiceStub stub = new GrouperServiceStub(GeneratedClientSettings.URL);
            
            HttpClientParams.getDefaultParams().setParameter(
                HttpClientParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler(0, false));
            
            Options options = stub._getServiceClient().getOptions();
            HttpTransportProperties.Authenticator auth = new HttpTransportProperties.Authenticator();
            auth.setUsername(GeneratedClientSettings.USER);
            auth.setPassword(GeneratedClientSettings.PASS);
            auth.setPreemptiveAuthentication(true);

            options.setProperty(HTTPConstants.AUTHENTICATE, auth);
            options.setProperty(HTTPConstants.SO_TIMEOUT, new Integer(3600000));
            options.setProperty(HTTPConstants.CONNECTION_TIMEOUT,
                new Integer(3600000));

            GetGrouperPrivilegesLite getGrouperPrivilegesLite = GetGrouperPrivilegesLite.class.newInstance();

            //version, e.g. v1_3_000
            getGrouperPrivilegesLite.setClientVersion(GeneratedClientSettings.VERSION);

            // set the act as id
            getGrouperPrivilegesLite.setActAsSubjectId("GrouperSystem");
            getGrouperPrivilegesLite.setActAsSubjectSourceId("");
            getGrouperPrivilegesLite.setActAsSubjectIdentifier("");

            getGrouperPrivilegesLite.setSubjectId("test.subject.0");
            getGrouperPrivilegesLite.setSubjectSourceId("");
            getGrouperPrivilegesLite.setSubjectIdentifier("");
            
            getGrouperPrivilegesLite.setGroupName("aStem:aGroup");
            getGrouperPrivilegesLite.setGroupUuid("");
            
            getGrouperPrivilegesLite.setStemName("");
            getGrouperPrivilegesLite.setStemUuid("");
            
            getGrouperPrivilegesLite.setPrivilegeType("access");
            getGrouperPrivilegesLite.setPrivilegeName("admin");
            
            getGrouperPrivilegesLite.setIncludeSubjectDetail("T");
            getGrouperPrivilegesLite.setSubjectAttributeNames("loginid,description");
            getGrouperPrivilegesLite.setIncludeGroupDetail("F");
            
            WsGetGrouperPrivilegesLiteResult wsGetGrouperPrivilegesLiteResult = 
              stub.getGrouperPrivilegesLite(getGrouperPrivilegesLite).get_return();

            System.out.println(ToStringBuilder.reflectionToString(
                wsGetGrouperPrivilegesLiteResult));

            if (!StringUtils.equals("T", wsGetGrouperPrivilegesLiteResult.getResultMetadata().getSuccess())) {
              throw new RuntimeException("Didnt work");
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        wsGetGrouperPrivilegesLite(WsSampleGeneratedType.soap);
    }
}
