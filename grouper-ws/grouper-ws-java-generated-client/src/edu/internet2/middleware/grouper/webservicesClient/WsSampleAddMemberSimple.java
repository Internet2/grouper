/**
 *
 */
package edu.internet2.middleware.grouper.webservicesClient;

import edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.AddMemberSimple;
import edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.WsAddMemberSimpleResult;
import edu.internet2.middleware.grouper.webservicesClient.util.GeneratedClientSettings;
import edu.internet2.middleware.grouper.ws.samples.WsSampleGenerated;
import edu.internet2.middleware.grouper.ws.samples.WsSampleGeneratedType;

import org.apache.axis2.Constants;
import org.apache.axis2.client.Options;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.axis2.transport.http.HttpTransportProperties;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;


/**
 *
 * @author mchyzer
 *
 */
public class WsSampleAddMemberSimple implements WsSampleGenerated {
    /**
     * @param wsSampleGeneratedType if SOAP or XML/HTTP
     */
    public static void addMemberSimple(
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

            AddMemberSimple addMemberSimple = AddMemberSimple.class.newInstance();

            //version, e.g. v1_3_000
            addMemberSimple.setClientVersion(GeneratedClientSettings.VERSION);

            addMemberSimple.setGroupName("aStem:aGroup");

            addMemberSimple.setGroupUuid("");

            addMemberSimple.setSubjectId("10021368");
            addMemberSimple.setSubjectSource("");
            addMemberSimple.setSubjectIdentifier("");

            // set the act as id
            addMemberSimple.setActAsSubjectId("GrouperSystem");

            addMemberSimple.setActAsSubjectSource("");
            addMemberSimple.setActAsSubjectIdentifier("");
            addMemberSimple.setFieldName("");
            addMemberSimple.setIncludeGroupDetail("");
            addMemberSimple.setIncludeSubjectDetail("");
            addMemberSimple.setSubjectAttributeNames("");
            addMemberSimple.setParamName0("");
            addMemberSimple.setParamValue0("");
            addMemberSimple.setParamName1("");
            addMemberSimple.setParamValue1("");

            WsAddMemberSimpleResult wsAddMemberSimpleResult = stub.addMemberSimple(addMemberSimple)
                                                                  .get_return();

            System.out.println(ToStringBuilder.reflectionToString(
                    wsAddMemberSimpleResult, ToStringStyle.MULTI_LINE_STYLE));
            System.out.println(ToStringBuilder.reflectionToString(
                    wsAddMemberSimpleResult.getResultMetadata(),
                    ToStringStyle.MULTI_LINE_STYLE));
            System.out.println(ToStringBuilder.reflectionToString(
                    wsAddMemberSimpleResult.getSubjectAttributeNames(),
                    ToStringStyle.MULTI_LINE_STYLE));
            System.out.println(ToStringBuilder.reflectionToString(
                    wsAddMemberSimpleResult.getWsGroupAssigned(),
                    ToStringStyle.MULTI_LINE_STYLE));
            System.out.println(ToStringBuilder.reflectionToString(
                    wsAddMemberSimpleResult.getWsSubject(),
                    ToStringStyle.MULTI_LINE_STYLE));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        addMemberSimple(WsSampleGeneratedType.SOAP);
    }

    /**
     * @see edu.internet2.middleware.grouper.ws.samples.WsSampleGenerated#executeSample(edu.internet2.middleware.grouper.ws.samples.WsSampleGeneratedType)
     */
    public void executeSample(WsSampleGeneratedType wsSampleGeneratedType) {
        addMemberSimple(wsSampleGeneratedType);
    }
}
