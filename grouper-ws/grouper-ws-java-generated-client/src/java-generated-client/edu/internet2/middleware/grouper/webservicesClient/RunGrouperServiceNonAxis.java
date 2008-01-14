package edu.internet2.middleware.grouper.webservicesClient;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;


public class RunGrouperServiceNonAxis {
    /**
     * @param args
     */
    @SuppressWarnings("unchecked")
    public static void main(String[] args) throws Exception {
        HttpClient httpClient = new HttpClient();
        GetMethod getMethod = new GetMethod(
                "http://localhost:8090/grouper/services/GrouperService/addMemberSimple?groupExtension=someGroup&groupStemExtensions=stem1:stem2&subjectId=1234123&actAsSubjectId=abcd");
        httpClient.executeMethod(getMethod);

        int statusCode = getMethod.getStatusCode();

        // see if request worked or not
        if (statusCode != 200) {
            throw new RuntimeException("Bad response from web service: " +
                statusCode);
        }

        String response = getMethod.getResponseBodyAsString();

        if ("SUCCESS".equals(response)) {
            System.out.println("Success");
        } else {
            System.out.println("Error: " + response);
        }
    }
}
