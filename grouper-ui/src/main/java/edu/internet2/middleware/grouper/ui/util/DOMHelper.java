/*******************************************************************************
 * Copyright 2012 Internet2
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
/*
Copyright 2004-2005 University Corporation for Advanced Internet Development, Inc.
Copyright 2004-2005 The University Of Bristol

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package edu.internet2.middleware.grouper.ui.util;

import org.w3c.dom.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;
import javax.xml.parsers.*;
import java.io.*;
import org.apache.xml.serialize.*;
import java.util.*;
import java.util.jar.JarFile;

/**
 * Utility class to provide common functions useful when working with DOM.
 * 
 * @author Gary Brown.
 * @version $Id: DOMHelper.java,v 1.2 2008-01-31 16:16:35 mchyzer Exp $
 */
 
public class DOMHelper 
{

  /**
     * Somewhat old method to turn org.w3c.dom.Node into a String
    */
  public static String nodeToString(Node node) throws Exception 
  {
    TransformerFactory tf  = TransformerFactory.newInstance();
    Transformer t = tf.newTransformer();
    t.setOutputProperty(OutputKeys.METHOD,"xml");
    
    StringWriter w = new StringWriter();
    t.transform(new DOMSource(node),new StreamResult(w));
    return w.toString();
  }

  /**
    * Serializes a Document to a String - without an Xml declaration
    */
  public static String domToString(Document document) throws Exception 
  {
    return domToString(document,false);
  }

    /**
    * Serializes a Document to a String - with/without an Xml declaration
    */
  public static String domToString(Document document,boolean withDeclaration) throws Exception 
  {
    return elementToString(document.getDocumentElement(),withDeclaration);
  }
  
  /**
   * Serializes an Element to a String - without an Xml declaration
   */
  public static String elementToString(Element element) throws Exception 
  {
    return elementToString(element,false);
  }
  
  
   /**
    * Serializes an Element to a String - with/without an Xml declaration
    */
  public static String elementToString(Element element,boolean withDeclaration) throws Exception 
  { 
    OutputFormat format = new OutputFormat(element.getOwnerDocument());
    NodeList nl = element.getElementsByTagName("*");
    HashMap hm = new HashMap();
    for (int i=0;i<nl.getLength();i++) {
    	hm.put(((Element)nl.item(i)).getNodeName(),"");	
    }
    Iterator i = hm.keySet().iterator();
    String[] nonEscapingElements = new String[hm.size()];
    int j=0;
    while(i.hasNext()) {
    	nonEscapingElements[j++] = (String)i.next();	
    }
    format.setNonEscapingElements(nonEscapingElements);
    format.setPreserveSpace(true);
    format.setIndenting(true);
    StringWriter w = new StringWriter();
    XMLSerializer serializer = new XMLSerializer(w,format);
    
    serializer.asDOMSerializer();
    serializer.serialize(element);
    String result = w.toString();
    if(withDeclaration) return result;
    
    int endDec = result.indexOf("?>");
    int startElement = result.indexOf("<",endDec);
    return result.substring(startElement);
   
  }
  
  

  
  
	

  /**
    * looks for file on classpath and parses it into a Document
    */
    public static Document getDomFromResourceOnClassPath(String resource) throws Exception {
    	InputStream is=null;
    	if(!resource.startsWith("jar:")) {
    		is = DOMHelper.class.getClassLoader().getResourceAsStream(resource);
    	}else{
			String jar = resource.replaceAll("jar:file:(.*?)!.*","$1");
			String path = resource.replaceAll("jar:file:/.*?!/(.*)","$1");
			JarFile jarFile = new JarFile(jar);
			is = jarFile.getInputStream(jarFile.getEntry(path));		
    		
    	}
    	
    	if (is == null) {
    		throw new IllegalArgumentException("Resource on classpath not found: '" + resource + "'");
    	}
    	
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    	DocumentBuilder db = dbf.newDocumentBuilder();
    	Document doc = db.parse(is);
    	return doc;
    	
    }
    
  /**
    * Assumes tag only occurs once and contains only text / CDATA
    * If tag does not exist 'nullable' determines if an Exception is thrown
    * 
    */
    public static String getText(String elementName,Document doc, boolean nullable) throws Exception {
    		NodeList nl = doc.getElementsByTagName(elementName);
			if(nl.getLength()<1) {
				if(nullable) {
					return null;
				}else {
					throw(new Exception("Cannot find " + elementName + " tag"));	
				}
			}			
			Element stmt = (Element)nl.item(0);
			stmt.normalize();
			nl = stmt.getChildNodes();
			if(nl.getLength() !=1 ) {
				throw(new Exception("Cannot process " + elementName + " tag"));
			}
			Node n = nl.item(0);
			if(n.getNodeType()!=Node.TEXT_NODE && n.getNodeType()!=Node.CDATA_SECTION_NODE) {
				throw(new Exception("Cannot process " + elementName + " tag"));	
			}
			return ((CharacterData)n).getData().trim();	
    }
    
     /**
    * Assumes tag only occurs once and contains only text / CDATA
    * If tag does not exist 'nullable' determines if an Exception is thrown

    */
    public static String getText(Element element, String elementName, boolean nullable) throws Exception {
    		NodeList nl = element.getElementsByTagName(elementName);
			if(nl.getLength()<1) {
				if(nullable) {
					return null;
				}else {
					throw(new Exception("Cannot find " + elementName + " tag"));	
				}
			}			
			Element stmt = (Element)nl.item(0);
			stmt.normalize();
			nl = stmt.getChildNodes();
			if(nl.getLength() !=1 ) {
				throw(new Exception("Cannot process " + elementName + " tag"));
			}
			Node n = nl.item(0);
			if(n.getNodeType()!=Node.TEXT_NODE && n.getNodeType()!=Node.CDATA_SECTION_NODE) {
				throw(new Exception("Cannot process " + elementName + " tag"));	
			}
			return ((CharacterData)n).getData().trim();	
    }
    
    /**
    * Returns immediate child elements with given name
    */
    public static Collection getImmediateElements(Element element,String elementName) throws Exception {
    		NodeList nl = element.getElementsByTagName(elementName);
			Collection elements = new Vector();
			if(nl.getLength()<1) {
				return elements;
			}			
			Element child;
			for(int i=0;i<nl.getLength();i++) {
				child = (Element) nl.item(i);
				if(child.getParentNode().equals(element)) {
					elements.add(child);	
				}	
			}
			
			
			return elements;	
    }
   
  /**
    * Returns immediate child element with given name - first only if > 1
    */ 
    public static Element getImmediateElement(Element element,String elementName) throws Exception {
    		
			Collection elements = getImmediateElements(element,elementName);
			if(elements.size() < 1) return null;
			Iterator i = elements.iterator();			
			return (Element)i.next();
    }
    
    
   /**
    * Convenience method so you can forget about DocumentBuilderFactory etc
    */
    public static Document  newDocument() throws Exception{
    	DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    	DocumentBuilder db = dbf.newDocumentBuilder();
    	return db.newDocument();	
    }
    
     /**
    * Convenience method so you can forget about DocumentBuilderFactory etc
    */
    public static Document newDocument(String str) throws Exception{
    	DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    	DocumentBuilder db = dbf.newDocumentBuilder();
    	Document doc = db.parse(new org.xml.sax.InputSource(new StringReader(str)));
    	return doc;	
    }

 /**
    * Convenience method so you can forget about DocumentBuilderFactory etc
    */
    public static Document newDocument(File file) throws Exception{
    	DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    	DocumentBuilder db = dbf.newDocumentBuilder();
    	Document doc = db.parse(file);
    	return doc;	
    }
    
    /**
    * Convenience method for transformations
    */
    public static void transform(String data,File out,String xsl) throws Exception{
    	transform(data,out,xsl,null);
    	
    		
    }
    
	/**
		* Convenience method for transformations
		*/
		public static void transform(String data,File out,String xsl,Map parameters) throws Exception{
			TransformerFactory tf  = TransformerFactory.newInstance();
			Transformer t = tf.newTransformer(new StreamSource(xsl));
			if(parameters != null) {
							String key;
							Iterator it = parameters.keySet().iterator();
							while(it.hasNext()) {
								key = (String)it.next();
								t.setParameter(key,parameters.get(key));
							}
						}
			t.transform(new StreamSource(new StringReader(data)),new StreamResult(out));
    	
    		
		}
	public static Document transform(Document doc,String xsl) throws Exception{
			return transform(doc,xsl,null);
    		
		}
	public static Document transform(Document doc,String xsl,Map parameters) throws Exception{
			TransformerFactory tf  = TransformerFactory.newInstance();
			Transformer t = tf.newTransformer(new StreamSource(xsl));
			if(parameters != null) {
				String key;
				Iterator it = parameters.keySet().iterator();
				while(it.hasNext()) {
					key = (String)it.next();
					t.setParameter(key,parameters.get(key));
				}
			}
			DOMResult res = new DOMResult();
			t.transform(new DOMSource(doc),res);
			return (Document)res.getNode();
    		
		}
}
