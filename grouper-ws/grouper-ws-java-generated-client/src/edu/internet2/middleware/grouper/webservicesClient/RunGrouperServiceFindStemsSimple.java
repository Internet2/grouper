/**
 *
 */
package edu.internet2.middleware.grouper.webservicesClient;


import org.apache.axis2.client.Options;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.axis2.transport.http.HttpTransportProperties;
import org.apache.commons.lang.builder.ToStringBuilder;

import edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.FindStemsSimple;
import edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.FindStemsSimpleResponse;
import edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.WsFindStemsResults;
import edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.WsStemResult;


/**
 * find stems simple
 * @author mchyzer
 *
 */
public class RunGrouperServiceFindStemsSimple {
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

            FindStemsSimple findStemsSimple = null;
            FindStemsSimpleResponse findStemsSimpleResponse = null;
            WsFindStemsResults wsFindStemsResults = null;
//            options.setProperty(Constants.Configuration.ENABLE_REST,
//                Constants.VALUE_TRUE);
            findStemsSimple = FindStemsSimple.class.newInstance();
            
            findStemsSimple.setParentStemName("");
            findStemsSimple.setParentStemNameScope("");
            findStemsSimple.setStemName("");
            findStemsSimple.setStemUuid("");
            findStemsSimple.setQueryScope("");
            findStemsSimple.setQuerySearchFromStemName("");
            findStemsSimple.setQueryTerm("");

            findStemsSimple.setStemName("aStem");
            System.out.println("\n\nQUERY BY STEM NAME: ");

            findStemsSimpleResponse = stub.findStemsSimple(findStemsSimple);

            wsFindStemsResults = findStemsSimpleResponse.get_return();
            System.out.println(ToStringBuilder.reflectionToString(
                    wsFindStemsResults));
            if (wsFindStemsResults.getStemResults()!= null) {
	            for (WsStemResult wsStemResult : wsFindStemsResults.getStemResults()) {
	                System.out.println(wsStemResult == null ? null : ToStringBuilder.reflectionToString(wsStemResult));
	            }
            }
            
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
