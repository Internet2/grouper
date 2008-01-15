package edu.internet2.middleware.grouper.webservicesClient;

import java.io.Reader;
import java.io.StringReader;

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;


public class RunGrouperServiceNonAxis {
    /**
     * @param args
     */
    @SuppressWarnings("unchecked")
    public static void main(String[] args) throws Exception {
        HttpClient httpClient = new HttpClient();
        GetMethod getMethod = new GetMethod(
                "http://localhost:8091/grouper-ws/services/GrouperService/addMemberSimple?groupName=aStem:aGroup&subjectId=10021368&actAsSubjectId=GrouperSystem");

        httpClient.getParams().setAuthenticationPreemptive(true);
        Credentials defaultcreds = new UsernamePasswordCredentials("GrouperSystem", "123");
        httpClient.getState().setCredentials(new AuthScope("localhost", 80), defaultcreds);
        
        httpClient.executeMethod(getMethod);

        int statusCode = getMethod.getStatusCode();

        // see if request worked or not
        if (statusCode != 200) {
            throw new RuntimeException("Bad response from web service: " +
                statusCode);
        }

        String response = getMethod.getResponseBodyAsString();
        
        //lets load this into jdom, since it is xml
		Reader xmlReader = new StringReader(response);
		try {
			// process xml
			Document document = new SAXBuilder().build(xmlReader);
			Element addMemberSimpleResponse = document.getRootElement();
			
			//parse: <ns:addMemberSimpleResponse xmlns:ns="http://webservices.grouper.middleware.internet2.edu/xsd">
			assertTrue("addMemberSimpleResponse".equals(addMemberSimpleResponse.getName()),
					"root not addMemberSimpleResponse: " + addMemberSimpleResponse.getName());

			Namespace namespace = addMemberSimpleResponse.getNamespace();
			
			//parse: <ns:return type="edu.internet2.middleware.grouper.webservices.WsAddMemberResult">
			Element returnElement = addMemberSimpleResponse.getChild("return", namespace);
			String theType = returnElement.getAttributeValue("type");
			assertTrue("edu.internet2.middleware.grouper.webservices.WsAddMemberResult"
					.equals(theType),
					"type not edu.internet2.middleware.grouper.webservices.WsAddMemberResult: " + theType);

			//<ns:errorMessage xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:nil="true" />
			String errorMessage = returnElement.getChildText("errorMessage", namespace);

			//<ns:resultCode xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:nil="true" />
			String resultCode = returnElement.getChildText("resultCode", namespace);

			//<ns:subjectId>GrouperSystem</ns:subjectId>
			String subjectId = returnElement.getChildText("subjectId", namespace);

			//<ns:subjectIdentifier xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:nil="true" />
			String subjectIdentifier = returnElement.getChildText("subjectIdentifier", namespace);

			//<ns:success>T</ns:success>
			String success = returnElement.getChildText("success", namespace);

			System.out.println("Success: " + success + ", resultCode: " + resultCode + ", subjectId: " + subjectId
					+ ", subjectIdentifier: " + subjectIdentifier + ", errorMessage: " + errorMessage);
			 
		} finally {
			try {
				xmlReader.close();
			} catch (Exception e) {
			}
		}
        
    }
	/**
	 * assert like java 1.4 assert
	 * 
	 * @param isTrue
	 * @param reason
	 */
	public static void assertTrue(boolean isTrue, String reason) {
		if (!isTrue) {
			throw new RuntimeException(reason);
		}
	}
}
