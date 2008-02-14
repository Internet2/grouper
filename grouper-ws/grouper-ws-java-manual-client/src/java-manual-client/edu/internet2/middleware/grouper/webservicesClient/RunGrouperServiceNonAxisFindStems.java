package edu.internet2.middleware.grouper.webservicesClient;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.List;

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.io.IOUtils;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;


public class RunGrouperServiceNonAxisFindStems {
	
	@SuppressWarnings("unchecked")
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
	        
	        //String xml = "<ns1:findGroups xmlns:ns1=\"http://webservices.grouper.middleware.internet2.edu/xsd\"><ns1:groupName> </ns1:groupName><ns1:stemName> </ns1:stemName><ns1:stemNameScope> </ns1:stemNameScope><ns1:groupUuid> </ns1:groupUuid><ns1:queryTerm>group</ns1:queryTerm><ns1:querySearchFromStemName> </ns1:querySearchFromStemName><ns1:queryScope>NAME</ns1:queryScope></ns1:findGroups>";

	        Namespace namespace = Namespace.getNamespace("ns1", "http://webservices.grouper.middleware.internet2.edu/xsd");
	        Element findGroupsElement = new Element("findGroups", namespace);
	        Element groupNameElement = new Element("groupName", namespace);
	        findGroupsElement.addContent(groupNameElement);
	        groupNameElement.setText("aStem:aGroup");
	        
	        //no need to close either of these
	        XMLOutputter outputter = new XMLOutputter();
	        StringWriter xmlStringWriter = new StringWriter();
	        try {
	          outputter.output(findGroupsElement, xmlStringWriter);
	        } catch (IOException e) {
	          throw new RuntimeException(e);
	        }
	        
	        String xml = xmlStringWriter.toString();
	        
	        RequestEntity requestEntity = new StringRequestEntity(xml);
	        method.setRequestEntity(requestEntity);
	        httpClient.executeMethod(method);
	
	        int statusCode = method.getStatusCode();
	
	        // see if request worked or not
	        if (statusCode != 200) {
	            throw new RuntimeException("Bad response from web service: " +
	                statusCode);
	        }
	        //there is a getResponseAsString, but it logs a warning each time...
	        InputStream inputStream = method.getResponseBodyAsStream();
	        String response = null;
	        try {
	        	response = IOUtils.toString(inputStream);
	        } finally {
	        	IOUtils.closeQuietly(inputStream);
	        }
	        
	        //lets load this into jdom, since it is xml
			xmlReader = new StringReader(response);
			
			// process xml
			Document document = new SAXBuilder().build(xmlReader);
			Element findGroupsSimple = document.getRootElement();
			
			//parse:
			//<ns:findGroupsResponse xmlns:ns="http://webservices.grouper.middleware.internet2.edu/xsd">
			RunGrouperServiceNonAxisUtils.assertTrue("findGroupsResponse".equals(findGroupsSimple.getName()),
					"root not findGroupsResponse: " + findGroupsSimple.getName());

			namespace = findGroupsSimple.getNamespace();
			
			//<ns:return type="edu.internet2.middleware.grouper.webservices.WsFindGroupsResults">
			Element returnElement = findGroupsSimple.getChild("return", namespace);
			String theType = returnElement.getAttributeValue("type");
			RunGrouperServiceNonAxisUtils.assertTrue("edu.internet2.middleware.grouper.webservices.WsFindGroupsResults"
					.equals(theType),
					"type not edu.internet2.middleware.grouper.webservices.WsFindGroupsResults: " + theType);

			List<Element> groupResultsList = returnElement.getChildren("groupResults", namespace);
			
			if (groupResultsList != null) {
				
				for (Element groupResultsElement : groupResultsList) {
					
					//	<ns:groupResults
					//		type="edu.internet2.middleware.grouper.webservices.WsGroupResult">
					theType = groupResultsElement.getAttributeValue("type");
					RunGrouperServiceNonAxisUtils.assertTrue("edu.internet2.middleware.grouper.webservices.WsGroupResult"
							.equals(theType),
							"type not edu.internet2.middleware.grouper.webservices.WsGroupResult: " + theType);

					//		<ns:createSource></ns:createSource>
					String createSource = groupResultsElement.getChildText("createSource", namespace);
					
					//		<ns:createSubjectId>GrouperSystem</ns:createSubjectId>
					String createSubjectId = groupResultsElement.getChildText("createSubjectId", namespace);
					
					//		<ns:createTime>2008/01/18 00:29:06.165</ns:createTime>
					String createTime = groupResultsElement.getChildText("createTime", namespace);
					
					//		<ns:description>aGroup3</ns:description>
					String description = groupResultsElement.getChildText("description", namespace);
					
					//		<ns:displayExtension>aGroup3</ns:displayExtension>
					String displayExtension = groupResultsElement.getChildText("displayExtension", namespace);
					
					//		<ns:displayName>a stem:aGroup3</ns:displayName>
					String displayName = groupResultsElement.getChildText("displayName", namespace);
					
					//		<ns:extension>aGroup3</ns:extension>
					String extension = groupResultsElement.getChildText("extension", namespace);
					
					//		<ns:isComposite>F</ns:isComposite>
					String isComposite = groupResultsElement.getChildText("isComposite", namespace);
					
					//		<ns:modifySource></ns:modifySource>
					String modifySource = groupResultsElement.getChildText("modifySource", namespace);
					
					//		<ns:modifySubjectId>GrouperSystem</ns:modifySubjectId>
					String modifySubjectId = groupResultsElement.getChildText("modifySubjectId", namespace);
					
					//		<ns:modifyTime>2008/01/18 00:29:06.347</ns:modifyTime>
					String modifyTime = groupResultsElement.getChildText("modifyTime", namespace);
					
					//		<ns:name>aStem:aGroup3</ns:name>
					String name = groupResultsElement.getChildText("name", namespace);
					
					//		<ns:parentStemName>aStem</ns:parentStemName>
					String parentStemName = groupResultsElement.getChildText("parentStemName", namespace);
					
					//		<ns:parentStemUuid>c5cdc0c4-04a8-47ac-931e-951b4eb44502</ns:parentStemUuid>
					String parentStemUuid = groupResultsElement.getChildText("parentStemUuid", namespace);

					//		<ns:uuid>1f9c138c-e984-4468-8b3b-f975dbb3f5e1</ns:uuid>
					String uuid = groupResultsElement.getChildText("uuid", namespace);

					//	</ns:groupResults>
					System.out.println("Group is: createSource: " + createSource + ", createSubjectId: "
							+ createSubjectId + ", createTime: " + createTime + ", description: " + description
							+ ", displayExtension: " + displayExtension + ", displayName: " + displayName
							+ ", extension: " + extension + ", isComposite: " + isComposite
							+ ", modifySource: " + modifySource + ", modifySubjectId: " + modifySubjectId
							+ ", modifyTime: " + modifyTime + ", name:" + name + ", parentStemName: " + parentStemName
							+ ", parentStemUuid: " + parentStemUuid + ", uuid: " +uuid);
				}
				
			}
			//	</ns:return>
			//</ns:findGroupsResponse>

			
			
			//<ns:resultMessage xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:nil="true" />
			String resultMessage = returnElement.getChildText("resultMessage", namespace);

			//	<ns:resultCode>SUCCESS</ns:resultCode>
			String resultCode = returnElement.getChildText("resultCode", namespace);

			//<ns:success>T</ns:success>
			String success = returnElement.getChildText("success", namespace);

			System.out.println("Success: " + success + ", resultCode: " + resultCode 
					+ ", resultMessage: " + resultMessage);

		
		
		
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
     * @param args
     */
    @SuppressWarnings("unchecked")
    public static void main(String[] args) {
        addMemberRest();
    }
}
