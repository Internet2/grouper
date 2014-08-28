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
 * @author mchyzer $Id$
 */
package edu.internet2.middleware.grouperWsSourceAdapter;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.xml.sax.InputSource;

/**
 *
 */
public class WsSourceAdapterXpath {

  /**
   * do an xpath expression
   * @param xml 
   * @param namespaces
   * @param xpathExpression
   * @return the map of labels
   */
  public static String xpath(String xml, final Map<String, String> namespaces,
      String xpathExpression) {
    
    Map<String, String> map = new HashMap<String, String>();
    map.put("result", xpathExpression);
    
    map = xpath(xml, namespaces, map);
    
    return map.get("result");
  }

  /**
   * do some xpath expressions
   * @param xml 
   * @param namespaces
   * @param xpathExpressions are map of label to xpath expression
   * @return the map of labels
   */
  public static Map<String, String> xpath(String xml, final Map<String, String> namespaces,
      Map<String, String> xpathExpressions) {

    InputStream inputStream = null;

    try {

      XPathFactory xPathfactory = XPathFactory.newInstance();
      XPath xpath = xPathfactory.newXPath();

      // there's no default implementation for NamespaceContext...seems kind of silly, no?
      xpath.setNamespaceContext(new NamespaceContext() {

        public String getNamespaceURI(String prefix) {
          if (prefix == null) {
            throw new NullPointerException("Null prefix");
          }
          
          //if ("contacts".equals(prefix)) return "http://projectbamboo.org/bsp/services/core/contact";
          //if ("dc".equals(prefix)) return "http://purl.org/dc/elements/1.1/";
          //if ("rdf".equals(prefix)) return "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
          //if ("dcterms".equals(prefix)) return "http://purl.org/dc/terms/";
          //if ("foaf".equals(prefix)) return "http://xmlns.com/foaf/0.1/";
          //if ("bsp".equals(prefix)) return "http://projectbamboo.org/bsp/resource";

          if (namespaces.containsKey(prefix)) {
            return namespaces.get(prefix);
          }
          if ("xsi".equals(prefix)) { 
            return "http://www.w3.org/2001/XMLSchema-instance";
          }
          
          if ("xml".equals(prefix)) {
            return XMLConstants.XML_NS_URI;
          }
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

      //XPathExpression expr = xpath.compile("contacts:bambooContact/contacts:contactNote/text()");
      //String result = expr.evaluate(new InputSource(inputStream));
      //System.out.println(result);
      
      Map<String, String> result = new LinkedHashMap<String, String>();

      for (String label : xpathExpressions.keySet()) {
        try {
          inputStream = new ByteArrayInputStream(xml.getBytes("utf-8"));
          InputSource inputSource = new InputSource(inputStream);
  
  
          String xpathExpressionString = xpathExpressions.get(label);
          
          if (xpathExpressionString == null || "".equals(xpathExpressionString.trim())) {
            throw new RuntimeException("Null xpath expression for: " + label);
          }
          XPathExpression xPathExpression = xpath.compile(xpathExpressionString);
          String resultString = xPathExpression.evaluate(inputSource);
          result.put(label, resultString);
        } finally {
          try {
            inputStream.close();
          } catch (Exception e) {
            //ignore
          }
        }
      }
      
      return result;
      
    } catch (Exception ioe) {
      throw new RuntimeException(ioe);
    }

  }
}
