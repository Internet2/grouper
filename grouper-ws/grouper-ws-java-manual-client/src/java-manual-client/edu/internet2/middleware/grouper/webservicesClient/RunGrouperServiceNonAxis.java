package edu.internet2.middleware.grouper.webservicesClient;

import java.io.Reader;
import java.io.StringReader;

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;


public class RunGrouperServiceNonAxis {
	
	public static void addMemberRest() {
        //lets load this into jdom, since it is xml
		Reader xmlReader = null;

		try {
	        HttpClient httpClient = new HttpClient();
	        PostMethod method = new PostMethod(
	                "http://localhost:8091/grouper-ws/services/GrouperService");
	
	        method.setRequestHeader("Content-Type", "application/xml; charset=UTF-8");
	        httpClient.getParams().setAuthenticationPreemptive(true);
	        Credentials defaultcreds = new UsernamePasswordCredentials("GrouperSystem", "pass");
	        httpClient.getState().setCredentials(new AuthScope("localhost", 8091), defaultcreds);
	        String xml = "<ns1:addMember xmlns:ns1=\"http://webservices.grouper.middleware.internet2.edu/xsd\"><ns1:wsGroupLookup><ns1:groupName>aStem:aGroup</ns1:groupName></ns1:wsGroupLookup><ns1:subjectLookups><ns1:subjectId>10021368</ns1:subjectId></ns1:subjectLookups><ns1:subjectLookups><ns1:subjectId>10039438</ns1:subjectId></ns1:subjectLookups><ns1:replaceAllExisting>F</ns1:replaceAllExisting><ns1:actAsSubjectLookup><ns1:subjectId>GrouperSystem</ns1:subjectId></ns1:actAsSubjectLookup></ns1:addMember>";
	        RequestEntity requestEntity = new StringRequestEntity(xml);
	        method.setRequestEntity(requestEntity);
	        httpClient.executeMethod(method);
	
	        int statusCode = method.getStatusCode();
	
	        // see if request worked or not
	        if (statusCode != 200) {
	            throw new RuntimeException("Bad response from web service: " +
	                statusCode);
	        }
	
	        String response = method.getResponseBodyAsString();
	        
	        //lets load this into jdom, since it is xml
			xmlReader = new StringReader(response);

			// process xml
			Document document = new SAXBuilder().build(xmlReader);
			Element addMemberSimpleResponse = document.getRootElement();
			
			//parse: 
			
			
			
			
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
		}catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			try {
				if (xmlReader != null) {xmlReader.close();}
			} catch (Exception e) {
			}
		}

	}
	
	/**
	 * add member simple web service with REST
	 */
	public static void addMemberSimpleRest() {
		Reader xmlReader = null;
		try {
	        HttpClient httpClient = new HttpClient();
	        GetMethod method = new GetMethod(
	                "http://localhost:8090/grouper-ws/services/GrouperService/addMemberSimple?groupName=aStem:aGroup&subjectId=10021368&actAsSubjectId=GrouperSystem");
	
	        httpClient.getParams().setAuthenticationPreemptive(true);
	        Credentials defaultcreds = new UsernamePasswordCredentials("GrouperSystem", "pass");
	        httpClient.getState().setCredentials(new AuthScope("localhost", 8091), defaultcreds);
	        
	        httpClient.executeMethod(method);
	
	        int statusCode = method.getStatusCode();
	
	        // see if request worked or not
	        if (statusCode != 200) {
	            throw new RuntimeException("Bad response from web service: " +
	                statusCode);
	        }
	
	        String response = method.getResponseBodyAsString();
	        
	        //lets load this into jdom, since it is xml
			xmlReader = new StringReader(response);

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
			 
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			try {
				xmlReader.close();
			} catch (Exception e) {
			}
		}
		
	}
	
    /**
     * @param args
     */
    @SuppressWarnings("unchecked")
    public static void main(String[] args) {
        addMemberRest();
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
