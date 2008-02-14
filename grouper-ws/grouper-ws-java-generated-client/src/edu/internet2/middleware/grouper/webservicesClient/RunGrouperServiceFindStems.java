/**
 *
 */
package edu.internet2.middleware.grouper.webservicesClient;


import org.apache.axis2.Constants;
import org.apache.axis2.client.Options;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.axis2.transport.http.HttpTransportProperties;
import org.apache.commons.lang.builder.ToStringBuilder;

import edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.FindStems;
import edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.FindStemsResponse;
import edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.WsFindStemsResults;
import edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.WsStemResult;


/**
 * Run this to run the generated axis client.
 *
 * Generate the code:
 *
 * C:\mchyzer\isc\dev\grouper\grouper-ws-java-generated-client>wsdl2java -p
 * edu.internet2.middleware.grouper.webservicesClient -t -uri GrouperService.wsdl
 *
 * @author mchyzer
 *
 */
public class RunGrouperServiceFindStems {
    /**
     * @param args
     */
    public static void main(String[] args) throws Exception {
        findStems();
    }

    public static void findStems() {
        try {
            GrouperServiceStub stub = new GrouperServiceStub(
                    "http://localhost:8091/grouper-ws/services/GrouperService");
            Options options = stub._getServiceClient().getOptions();
            HttpTransportProperties.Authenticator auth = new HttpTransportProperties.Authenticator();
            auth.setUsername("GrouperSystem");
            auth.setPassword("pass");

            options.setProperty(HTTPConstants.AUTHENTICATE, auth);
            options.setProperty(HTTPConstants.SO_TIMEOUT, new Integer(3600000));
            options.setProperty(HTTPConstants.CONNECTION_TIMEOUT,
                new Integer(3600000));

            FindStems findStems = null;
            FindStemsResponse findStemsResponse = null;
            WsFindStemsResults wsFindStemsResults = null;
//            options.setProperty(Constants.Configuration.ENABLE_REST,
//                Constants.VALUE_TRUE);
            findStems = FindStems.class.newInstance();
            
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
            if (wsFindStemsResults.getStemResults()!= null) {
	            for (WsStemResult wsStemResult : wsFindStemsResults.getStemResults()) {
	                System.out.println(wsStemResult == null ? null : ToStringBuilder.reflectionToString(wsStemResult));
	            }
            }
            
            //try by uuid
            findStems.setStemName("");
            System.out.println("\n\nQUERY BY UUID: ");
            //            String groupName, String stemName, 
            //    		String stemNameScope,
            //    		String groupUuid, String queryTerm, String querySearchFromStemName
            findStems.setStemUuid("19284537-6118-44b2-bbbc-d5757c709cb7");

            findStemsResponse = stub.findStems(findStems);

            wsFindStemsResults = findStemsResponse.get_return();
            System.out.println(ToStringBuilder.reflectionToString(
                    wsFindStemsResults));
            if (wsFindStemsResults.getStemResults()!= null) {
	            for (WsStemResult wsStemResult : wsFindStemsResults.getStemResults()) {
	                System.out.println(wsStemResult == null ? null : ToStringBuilder.reflectionToString(wsStemResult));
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
            if (wsFindStemsResults.getStemResults()!= null) {
	            for (WsStemResult wsStemResult : wsFindStemsResults.getStemResults()) {
	                System.out.println(wsStemResult == null ? null : ToStringBuilder.reflectionToString(wsStemResult));
	            }
            }

            WsStemResult[] wsStemResults = wsFindStemsResults.getStemResults();

            if (wsStemResults != null) {
                for (WsStemResult wsStemResult : wsFindStemsResults.getStemResults()) {
	                System.out.println(wsStemResult == null ? null : ToStringBuilder.reflectionToString(wsStemResult));
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
	                System.out.println(wsStemResult == null ? null : ToStringBuilder.reflectionToString(wsStemResult));
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
