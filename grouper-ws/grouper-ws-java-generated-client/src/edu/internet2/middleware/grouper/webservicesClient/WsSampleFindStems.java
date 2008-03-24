/**
 *
 */
package edu.internet2.middleware.grouper.webservicesClient;

import edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.FindStems;
import edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.FindStemsResponse;
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
 *
 * @author mchyzer
 *
 */
public class WsSampleFindStems implements WsSampleGenerated {
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

            FindStems findStems = null;
            FindStemsResponse findStemsResponse = null;
            WsFindStemsResults wsFindStemsResults = null;
            //            options.setProperty(Constants.Configuration.ENABLE_REST,
            //                Constants.VALUE_TRUE);
            findStems = FindStems.class.newInstance();
            //version, e.g. v1_3_000
            findStems.setClientVersion(GeneratedClientSettings.VERSION);

            /*
               findStems.setParentStemName("");
               findStems.setParentStemNameScope("");
               findStems.setStemName("");
               findStems.setStemUuid("");
               findStems.setQueryScope("");
               findStems.setQuerySearchFromStemName("");
               findStems.setQueryTerm("");
               findStems.setStemName("aStem");
               System.out.println("\n\nQUERY BY STEM NAME: ");
               findStemsResponse = stub.findStems(findStems);
               wsFindStemsResults = findStemsResponse.get_return();
               System.out.println(ToStringBuilder.reflectionToString(
                       wsFindStemsResults));
               if (wsFindStemsResults.getStemResults() != null) {
                   for (WsStemResult wsStemResult : wsFindStemsResults.getStemResults()) {
                       System.out.println((wsStemResult == null) ? null
                                                                 : ToStringBuilder.reflectionToString(
                               wsStemResult));
                   }
               }
               //try by uuid
               findStems.setStemName("");
               System.out.println("\n\nQUERY BY UUID: ");
               //            String groupName, String stemName,
               //                    String stemNameScope,
               //                    String groupUuid, String queryTerm, String querySearchFromStemName
               findStems.setStemUuid("19284537-6118-44b2-bbbc-d5757c709cb7");
               findStemsResponse = stub.findStems(findStems);
               wsFindStemsResults = findStemsResponse.get_return();
               System.out.println(ToStringBuilder.reflectionToString(
                       wsFindStemsResults));
               if (wsFindStemsResults.getStemResults() != null) {
                   for (WsStemResult wsStemResult : wsFindStemsResults.getStemResults()) {
                       System.out.println((wsStemResult == null) ? null
                                                                 : ToStringBuilder.reflectionToString(
                               wsStemResult));
                   }
               }
               //search by stem
               findStems.setStemUuid("");
               System.out.println("\n\nQUERY BY STEM: ");
               findStems.setStemName("aStem");
               findStems.setParentStemNameScope("ONE_LEVEL");
               findStemsResponse = stub.findStems(findStems);
               wsFindStemsResults = findStemsResponse.get_return();
               System.out.println(ToStringBuilder.reflectionToString(
                       wsFindStemsResults));
               if (wsFindStemsResults.getStemResults() != null) {
                   for (WsStemResult wsStemResult : wsFindStemsResults.getStemResults()) {
                       System.out.println((wsStemResult == null) ? null
                                                                 : ToStringBuilder.reflectionToString(
                               wsStemResult));
                   }
               }
               WsStemResult[] wsStemResults = wsFindStemsResults.getStemResults();
               if (wsStemResults != null) {
                   for (WsStemResult wsStemResult : wsFindStemsResults.getStemResults()) {
                       System.out.println((wsStemResult == null) ? null
                                                                 : ToStringBuilder.reflectionToString(
                               wsStemResult));
                   }
               }
               //search by query
               findStems.setStemName("");
               findStems.setParentStemNameScope("");
               System.out.println("\n\nQUERY BY QUERY: ");
               findStems.setQueryTerm("st");
               findStems.setQueryScope("NAME");
               findStemsResponse = stub.findStems(findStems);
               wsFindStemsResults = findStemsResponse.get_return();
               System.out.println(ToStringBuilder.reflectionToString(
                       wsFindStemsResults));
               wsStemResults = wsFindStemsResults.getStemResults();
               if (wsStemResults != null) {
                   for (WsStemResult wsStemResult : wsFindStemsResults.getStemResults()) {
                       System.out.println((wsStemResult == null) ? null
                                                                 : ToStringBuilder.reflectionToString(
                               wsStemResult));
                   }
               }
             */
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
