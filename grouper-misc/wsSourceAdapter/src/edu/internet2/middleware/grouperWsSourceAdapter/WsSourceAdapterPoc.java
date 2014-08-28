/**
 * Copyright 2014 Internet2
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
 */
/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouperWsSourceAdapter;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.xml.sax.InputSource;

import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.provider.SourceManager;


/**
 * ws sample source adapter
 */
public class WsSourceAdapterPoc {

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {

    Subject subject = SourceManager.getInstance().getSource("ws").getSubject("urn:uuid:61c8922b-2d23-4fba-9bce-5f1dde6decf7", true);
    
    System.out.println("id: " + subject.getId());
    System.out.println("name: " + subject.getName());
    System.out.println("description: " + subject.getDescription());
    System.out.println("email: " + subject.getAttributeValue("email"));
    
  }

  /**
   * sample using libraries
   */
  public static void sampleMethods() {
    String xml = WsSourceAdapterHttp.urlGet("http://somehost.whatever.org/bsp/contacts/urn:uuid:61c8922b-2d23-4fba-9bce-5f1dde6decf7");
    Map<String, String> namespaces = new HashMap<String, String>();
    
    namespaces.put("contacts", "http://projectbamboo.org/bsp/services/core/contact");
    
    namespaces.put("dc", "http://purl.org/dc/elements/1.1/");
    namespaces.put("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
    namespaces.put("dcterms", "http://purl.org/dc/terms/");
    namespaces.put("foaf", "http://xmlns.com/foaf/0.1/");
    namespaces.put("bsp", "http://projectbamboo.org/bsp/resource");
    namespaces.put("xsi", "http://www.w3.org/2001/XMLSchema-instance");

    String result = WsSourceAdapterXpath.xpath(xml, namespaces, "contacts:bambooContact/contacts:contactNote/text()");
    System.out.println(result);

  }
  
  /**
   * sample with raw java
   */
  public static void sampleRaw() {
    
    HttpClient httpClient = new HttpClient();

    GetMethod getMethod = new GetMethod("http://somehost.whatever.org/bsp/contacts/urn:uuid:61c8922b-2d23-4fba-9bce-5f1dde6decf7");

    InputStream inputStream = null;
    
    try {
      httpClient.executeMethod(getMethod);

      String response = getMethod.getResponseBodyAsString();
      
      //      <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
      //      <contacts:bambooContact xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:dc="http://purl.org/dc/elements/1.1/" xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#" xmlns:dcterms="http://purl.org/dc/terms/" xmlns:foaf="http://xmlns.com/foaf/0.1/" xmlns:bsp="http://projectbamboo.org/bsp/resource" xmlns:contacts="http://projectbamboo.org/bsp/services/core/contact" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
      //          <dcterms:subject/>
      //          <dcterms:creator xsi:type="dcterms:URI">urn:uuid:99999999-9999-9999-9999-999999999999</dcterms:creator>
      //          <dcterms:created xsi:type="dcterms:W3CDTF">2014-03-17T21:25:36.487Z</dcterms:created>
      //          <bsp:modifier>urn:uuid:99999999-9999-9999-9999-999999999999</bsp:modifier>
      //          <dcterms:modified xsi:type="dcterms:W3CDTF">2014-03-17T21:25:36.488Z</dcterms:modified>
      //          <contacts:contactId>urn:uuid:61c8922b-2d23-4fba-9bce-5f1dde6decf7</contacts:contactId>
      //          <contacts:contactNote>Bamboo Person One Contact</contacts:contactNote>
      //          <contacts:emails>
      //              <email>PersonOne@example1.com</email>
      //              <contacts:email>PersonOne@example1.com</contacts:email>
      //          </contacts:emails>
      //          <contacts:displayName>Mr. John Doe</contacts:displayName>
      //          <contacts:partNames>
      //              <contacts:partNameType>HONORIFIC_PREFIX</contacts:partNameType>
      //              <contacts:partNameContent>Mr.</contacts:partNameContent>
      //              <contacts:partNameLang>eng</contacts:partNameLang>
      //          </contacts:partNames>
      //          <contacts:partNames>
      //              <contacts:partNameType>NAME_GIVEN</contacts:partNameType>
      //              <contacts:partNameContent>John</contacts:partNameContent>
      //              <contacts:partNameLang>spa</contacts:partNameLang>
      //          </contacts:partNames>
      //          <contacts:partNames>
      //              <contacts:partNameType>NAME_FAMILY_PATERNAL</contacts:partNameType>
      //              <contacts:partNameContent>Doe</contacts:partNameContent>
      //              <contacts:partNameLang>spa</contacts:partNameLang>
      //          </contacts:partNames>
      //          <contacts:telephones>
      //              <contacts:telephoneType>VOICE</contacts:telephoneType>
      //              <contacts:telephoneNumber>212-555-1212</contacts:telephoneNumber>
      //              <contacts:locationType>HOME</contacts:locationType>
      //          </contacts:telephones>
      //          <contacts:telephones>
      //              <contacts:telephoneType>SMS</contacts:telephoneType>
      //              <contacts:telephoneNumber>999-555-1212</contacts:telephoneNumber>
      //              <contacts:locationType>SABBATICAL</contacts:locationType>
      //          </contacts:telephones>
      //          <contacts:iMs>
      //              <contacts:instantMessagingType>SKYPE</contacts:instantMessagingType>
      //              <contacts:account>PersonOneSkype</contacts:account>
      //              <contacts:locationType>WORK</contacts:locationType>
      //          </contacts:iMs>
      //          <contacts:addresses>
      //              <contacts:streetAddress1>123 Main St.</contacts:streetAddress1>
      //              <contacts:streetAddress2>2nd Fl</contacts:streetAddress2>
      //              <contacts:locality>New York</contacts:locality>
      //              <contacts:region>NY</contacts:region>
      //              <contacts:postalCode>10001</contacts:postalCode>
      //              <contacts:country>USA</contacts:country>
      //              <contacts:locationType>WORK</contacts:locationType>
      //          </contacts:addresses>
      //      </contacts:bambooContact>


      
      System.out.println(response);
      
      inputStream = new ByteArrayInputStream(response.getBytes("utf-8"));
      
      //DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      //DocumentBuilder builder = factory.newDocumentBuilder();
      //Document doc = builder.parse(inputStream);
      XPathFactory xPathfactory = XPathFactory.newInstance();
      XPath xpath = xPathfactory.newXPath();
      
      // there's no default implementation for NamespaceContext...seems kind of silly, no?
      xpath.setNamespaceContext(new NamespaceContext() {
          public String getNamespaceURI(String prefix) {
              if (prefix == null) throw new NullPointerException("Null prefix");
              if ("contacts".equals(prefix)) return "http://projectbamboo.org/bsp/services/core/contact";
              else if ("dc".equals(prefix)) return "http://purl.org/dc/elements/1.1/";
              else if ("rdf".equals(prefix)) return "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
              else if ("dcterms".equals(prefix)) return "http://purl.org/dc/terms/";
              else if ("foaf".equals(prefix)) return "http://xmlns.com/foaf/0.1/";
              else if ("bsp".equals(prefix)) return "http://projectbamboo.org/bsp/resource";
              else if ("xsi".equals(prefix)) return "http://www.w3.org/2001/XMLSchema-instance";
              else if ("xml".equals(prefix)) return XMLConstants.XML_NS_URI;
              return XMLConstants.NULL_NS_URI;
          }

          // This method isn't necessary for XPath processing.
          public String getPrefix(String uri) {
              throw new UnsupportedOperationException();
          }

          // This method isn't necessary for XPath processing either.
          public Iterator getPrefixes(String uri) {
              throw new UnsupportedOperationException();
          }
      });
      
      XPathExpression expr = xpath.compile("contacts:bambooContact/contacts:contactNote/text()");
      String result = expr.evaluate(new InputSource(inputStream));
      System.out.println(result);
      
    } catch (Exception ioe) {
      throw new RuntimeException(ioe);
    } finally {
      try {
        inputStream.close();
      } catch (Exception e) {
        //ignore
      }
    }
  }
  
}
