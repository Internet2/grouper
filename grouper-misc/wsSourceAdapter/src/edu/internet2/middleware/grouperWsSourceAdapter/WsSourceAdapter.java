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

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import edu.internet2.middleware.subject.SourceUnavailableException;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.SubjectNotFoundException;
import edu.internet2.middleware.subject.SubjectNotUniqueException;
import edu.internet2.middleware.subject.provider.BaseSourceAdapter;
import edu.internet2.middleware.subject.provider.SubjectImpl;


/**
 * ws sample source adapter
 */
public class WsSourceAdapter extends BaseSourceAdapter {
  
  /**
   * 
   */
  public WsSourceAdapter() {
  }

  /**
   * @see edu.internet2.middleware.subject.Source#checkConfig()
   */
  public void checkConfig() {
  }

  /**
   * @see edu.internet2.middleware.subject.Source#printConfig()
   */
  public String printConfig() {
    return null;
  }

  /**
   * convert xml to subject
   * @param xml
   * @return the subject
   */
  private Subject convertXmlToSubject(String xml) {
    
    String subjectIdXpath = this.getInitParam("subjectIdXpath");
    String nameXpath = this.getInitParam("nameXpath");
    String descriptionXpath = this.getInitParam("descriptionXpath");
    
    Map<String, String> xpathAttributes = new LinkedHashMap<String, String>();
    
    xpathAttributes.put("subjectIdXpath", subjectIdXpath);
    xpathAttributes.put("nameXpath", nameXpath);
    xpathAttributes.put("descriptionXpath", descriptionXpath);
    
    for (String attributeName : this.attributeXpaths.keySet()) {
      xpathAttributes.put(attributeName, this.attributeXpaths.get(attributeName));
    }
    
    xpathAttributes = WsSourceAdapterXpath.xpath(xml, this.namespaces, xpathAttributes);

    Map<String, Set<String>> subjectAttributes = null;
    
    if (this.attributeXpaths.size() > 0) {
      subjectAttributes = new LinkedHashMap<String, Set<String>>();
      for (String attributeName : this.attributeXpaths.keySet()) {
        Set<String> values = new HashSet<String>();
        values.add(xpathAttributes.get(attributeName));
        values = Collections.unmodifiableSet(values);
        subjectAttributes.put(attributeName, values);
      }
      
    }
    
    String subjectId = xpathAttributes.get("subjectIdXpath");
    String subjectName = xpathAttributes.get("nameXpath");
    String description = xpathAttributes.get("descriptionXpath");
    
    SubjectImpl subjectImpl = new SubjectImpl(subjectId, subjectName, description, this.getSubjectType().getName(), this.getId(), subjectAttributes);
    
    return subjectImpl;
    
  }
  
  /**
   * @see edu.internet2.middleware.subject.provider.BaseSourceAdapter#getSubject(java.lang.String)
   * @deprecated
   */
  @Deprecated
  @Override
  public Subject getSubject(String id1) throws SubjectNotFoundException, SubjectNotUniqueException {
    
    //<!--  you can substitute $baseUrl$ for the base url, and $id$ for the id to search for -->
    //<init-param>
    //  <param-name>searchByIdUrl</param-name>
    //  <param-value>$baseUrl$/contacts/$id$</param-value>
    //</init-param>
    
    
    String searchByIdUrl = this.getInitParam("searchByIdUrl");
    searchByIdUrl = searchByIdUrl.replace("$baseUrl$", this.baseUrl);
    searchByIdUrl = searchByIdUrl.replace("$id$", id1);

    try {
      String xml = WsSourceAdapterHttp.urlGet(searchByIdUrl);

      Subject subject = convertXmlToSubject(xml);
      
      return subject;

    } catch (WsSourceAdapterNotFound wsanf) {
      throw new SubjectNotFoundException("Cant find subject: " + id1);
    } catch (Exception e) {
      throw new SourceUnavailableException("Cant find subject: " + id1, e);
    }
    
  }

  /**
   * @see edu.internet2.middleware.subject.provider.BaseSourceAdapter#getSubjectByIdentifier(java.lang.String)
   */
  @Deprecated
  @Override
  public Subject getSubjectByIdentifier(String id1) throws SubjectNotFoundException,
      SubjectNotUniqueException {
    throw new RuntimeException("Not implemented yet");
  }

  /**
   * @see edu.internet2.middleware.subject.provider.BaseSourceAdapter#search(java.lang.String)
   */
  @Override
  public Set<Subject> search(String searchValue) {
    throw new RuntimeException("Not implemented yet");
  }

  /**
   * base url
   */
  private String baseUrl;
  
  /**
   * namespaces for xpath
   */
  private Map<String, String> namespaces = null;
  
  /**
   * attribute names and xpaths
   */
  private Map<String, String> attributeXpaths = null;
  
  /**
   * @see edu.internet2.middleware.subject.provider.BaseSourceAdapter#init()
   */
  @Override
  public void init() throws SourceUnavailableException {
    Properties properties = initParams();
    
    this.baseUrl = properties.getProperty("baseUrl");
    if (this.baseUrl == null || "".equals(this.baseUrl.trim())) {
      throw new RuntimeException("You must set a baseUrl in the sources.xml for this source: " + this.getId());
    }
    
    if (this.baseUrl.endsWith("/")) {
      this.baseUrl = this.baseUrl.substring(0, this.baseUrl.length()-1);
    }

    this.namespaces = new HashMap<String, String>();
    
    for (int i=0;i<100;i++) {
      //<init-param>
      //  <param-name>namespace_0_name</param-name>
      //  <param-value>contacts</param-value>
      //</init-param>
      //<init-param>
      //  <param-name>namespace_0_value</param-name>
      //  <param-value>http://projectbamboo.org/bsp/services/core/contact</param-value>
      //</init-param>
      String namespaceName = properties.getProperty("namespace_" + i + "_name");
      if (namespaceName == null || "".equals(namespaceName.trim())) {
        break;
      }
      String namespaceValue = properties.getProperty("namespace_" + i + "_value");
      this.namespaces.put(namespaceName, namespaceValue);
    }

    this.attributeXpaths = new LinkedHashMap<String, String>();
    
    for (int i=0;i<100;i++) {
      //<!-- add subject attributes in pairs with index from 0 to 99 sequential -->    
      //<init-param>
      //  <param-name>attribute_0_name</param-name>
      //  <param-value>email</param-value>
      //</init-param>
      //
      //<init-param>
      //  <param-name>attribute_0_xpath</param-name>
      //  <param-value>contacts:bambooContact/contacts:emails[1]/email/text()</param-value>
      //</init-param>
      String attributeName = properties.getProperty("attribute_" + i + "_name");
      if (attributeName == null || "".equals(attributeName.trim())) {
        break;
      }
      String namespaceValue = properties.getProperty("attribute_" + i + "_xpath");
      this.attributeXpaths.put(attributeName, namespaceValue);
      
    }
    
  }

}
