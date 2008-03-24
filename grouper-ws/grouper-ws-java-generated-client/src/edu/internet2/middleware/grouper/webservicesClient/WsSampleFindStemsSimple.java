/**
 *
 */
package edu.internet2.middleware.grouper.webservicesClient;

import edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.FindStemsSimple;
import edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.FindStemsSimpleResponse;
import edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.WsFindStemsResults;
import edu.internet2.middleware.grouper.webservicesClient.util.GeneratedClientSettings;
import edu.internet2.middleware.grouper.ws.samples.WsSampleGenerated;
import edu.internet2.middleware.grouper.ws.samples.WsSampleGeneratedType;

import org.apache.axis2.Constants;
import org.apache.axis2.client.Options;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.axis2.transport.http.HttpTransportProperties;

import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * find stems simple
 * @author mchyzer
 *
 */
public class WsSampleFindStemsSimple implements WsSampleGenerated {
    /**
     * @param args
     */
    public static void main(String[] args) {
        findStems(WsSampleGeneratedType.SOAP);
    }

    /**
     * @see edu.internet2.middleware.grouper.ws.samples.WsSampleGenerated#executeSample(edu.internet2.middleware.grouper.ws.samples.WsSampleGeneratedType)
     */
    public void executeSample(WsSampleGeneratedType wsSampleGeneratedType) {
        findStems(wsSampleGeneratedType);
    }

    /**
     *
     * @param wsSampleGeneratedType can run as soap or xml/http
     */
    public static void findStems(WsSampleGeneratedType wsSampleGeneratedType) {
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

            FindStemsSimple findStemsSimple = null;
            FindStemsSimpleResponse findStemsSimpleResponse = null;
            WsFindStemsResults wsFindStemsResults = null;
            //            options.setProperty(Constants.Configuration.ENABLE_REST,
            //                Constants.VALUE_TRUE);
            findStemsSimple = FindStemsSimple.class.newInstance();

            //version, e.g. v1_3_000
            findStemsSimple.setClientVersion(GeneratedClientSettings.VERSION);

            findStemsSimple.setParentStemName("");
            findStemsSimple.setParentStemNameScope("");
            findStemsSimple.setStemName("");
            findStemsSimple.setStemUuid("");

            /*
               findStemsSimple.setQueryScope("");
               findStemsSimple.setQuerySearchFromStemName("");
               findStemsSimple.setQueryTerm("");
               findStemsSimple.setStemName("aStem");
               System.out.println("\n\nQUERY BY STEM NAME: ");
               findStemsSimpleResponse = stub.findStemsSimple(findStemsSimple);
               wsFindStemsResults = findStemsSimpleResponse.get_return();
               System.out.println(ToStringBuilder.reflectionToString(
                       wsFindStemsResults));
               if (wsFindStemsResults.getStemResults() != null) {
                   for (WsStemResult wsStemResult : wsFindStemsResults.getStemResults()) {
                       System.out.println((wsStemResult == null) ? null
                                                                 : ToStringBuilder.reflectionToString(
                               wsStemResult));
                   }
               } */
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
