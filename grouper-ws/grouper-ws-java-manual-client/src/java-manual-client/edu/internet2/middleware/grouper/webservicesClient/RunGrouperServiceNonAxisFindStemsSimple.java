package edu.internet2.middleware.grouper.webservicesClient;

import java.io.Reader;
import java.io.StringReader;
import java.util.List;

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;

/**
 * find stems
 * @author mchyzer
 */
public class RunGrouperServiceNonAxisFindStemsSimple {
	
	/**
	 * add member simple web service with REST
	 */
	@SuppressWarnings("unchecked")
	public static void findGroupsSimpleRest() {
		Reader xmlReader = null;
		try {
	        HttpClient httpClient = new HttpClient();
	        
	        GetMethod method = new GetMethod(
	                "http://localhost:8091/grouper-ws/services/GrouperService/findStemsSimple?stemName=aStem&parentStemName=" +
	                "&stemNameScope=&stemUuid=&queryTerm=&querySearchFromStemName=&queryScope=");
	        
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
	        
	        System.out.println(response);
	        
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
        findGroupsSimpleRest();
    }
}
