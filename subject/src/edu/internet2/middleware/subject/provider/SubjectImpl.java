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
/*--
$Id: SubjectImpl.java,v 1.1 2009-09-02 05:40:10 mchyzer Exp $
$Date: 2009-09-02 05:40:10 $
 
Copyright 2005 Internet2 and Stanford University.  All Rights Reserved.
See doc/license.txt in this distribution.
 */
package edu.internet2.middleware.subject.provider;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.internet2.middleware.subject.Source;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.SubjectCaseInsensitiveMap;
import edu.internet2.middleware.subject.SubjectCaseInsensitiveMapImpl;
import edu.internet2.middleware.subject.SubjectCaseInsensitiveSet;
import edu.internet2.middleware.subject.SubjectCaseInsensitiveSetImpl;
import edu.internet2.middleware.subject.SubjectType;
import edu.internet2.middleware.subject.SubjectUtils;
import edu.internet2.middleware.subject.util.ExpirableCache;

/**
 * Base Subject implementation.  Subclass this to change behavior
 */
@SuppressWarnings("serial")
public class SubjectImpl implements Subject {

  /**
   * turn some strings into a map, every other is a name or value of attribute
   * @param strings
   * @return the map (never null)
   */
  public static Map<String, Set<String>> toAttributeMap(String... strings) {
    Map<String, Set<String>> map = new SubjectCaseInsensitiveMapImpl<String, Set<String>>();
    if (strings != null) {
      if (strings.length % 2 != 0) {
        throw new RuntimeException("Must pass in an odd number of strings: " + strings.length);
      }
      for (int i=0;i<strings.length;i+=2) {
        Set<String> set = new LinkedHashSet<String>();
        set.add(strings[i+1]);
        map.put(strings[i], set);
      }
    }
    return map;
  }
  
  /**
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return toStringStatic(this);
  }


  /**
   * toString
   * @param subject
   * @return string
   */
  public static String toStringStatic(Subject subject) {
    String name = subject.getName();
    return "Subject id: " + subject.getId() + ", sourceId: " + subject.getSourceId() + 
      (StringUtils.isBlank(name) ? "" : (", name: " + name));
  }

  /** */
  private static Log log = LogFactory.getLog(SubjectImpl.class);

  /** */
  private String id;

  /** */
  private String name;

  /** */
  private String description;

  /** */
  private String typeName;

  
  /**
   * sourceId
   * @return the sourceId
   */
  public String getSourceId() {
    return this.sourceId;
  }

  
  /**
   * sourceId
   * @param sourceId1 the sourceId to set
   */
  public void setSourceId(String sourceId1) {
    this.sourceId = sourceId1;
  }

  /** sourceId */
  private String sourceId;
  
  /** */
  private Map<String, Set<String>> attributes = null;

  /**
   * Constructor called by SourceManager.  Will create an empty map for attributes
   * @param id1 
   * @param name1 
   * @param description1 
   * @param typeName1 
   * @param sourceId1 
   */
  public SubjectImpl(String id1, String name1, String description1, String typeName1,
      String sourceId1) {
    this(id1, name1, description1, typeName1, sourceId1, new SubjectCaseInsensitiveMapImpl<String, Set<String>>());
  }

  /**
   * Constructor called by SourceManager.
   * @param id1 
   * @param name1 
   * @param description1 
   * @param typeName1 
   * @param sourceId1 
   * @param attributes1 
   */
  public SubjectImpl(String id1, String name1, String description1, String typeName1,
      String sourceId1, Map<String, Set<String>> attributes1) {
    this.id = id1;
    this.name = name1;
    this.typeName = typeName1;
    this.description = description1;
    this.sourceId = sourceId1;
    this.setAttributes(attributes1);
  }

  /**
   * {@inheritDoc}
   */
  public String getId() {
    return this.id;
  }

  /**
   * {@inheritDoc}
   */
  public SubjectType getType() {
    return SubjectTypeEnum.valueOf(this.typeName);
  }

  /**
   * {@inheritDoc}
   */
  public String getName() {
    return this.name;
  }

  /**
   * {@inheritDoc}
   */
  public String getDescription() {
    return this.description;
  }

  /**
   * @see edu.internet2.middleware.subject.Subject#getAttributeValue(java.lang.String, boolean)
   */
  public String getAttributeValue(String name1) {
    return getAttributeValue(name1, true);
  }
  
  /**
   * @see edu.internet2.middleware.subject.Subject#getAttributeValue(java.lang.String, boolean)
   */
  public String getAttributeValue(String name1, boolean excludeInternalAttributes) {
    this.initAttributesIfNeeded();
    if (this.attributes == null) {
      return null;
    }
    
    if (excludeInternalAttributes && !StringUtils.isBlank(this.sourceId) && getSource().getInternalAttributes().contains(name1)) {
      return null;
    }
    
    Set<String> values = this.attributes.get(name1);
    if (values != null && values.size() > 0) {
      //return the first, no matter how many there are
      return values.iterator().next();
    }
    return null;
  }

  /**
   * @see edu.internet2.middleware.subject.Subject#getAttributeValues(java.lang.String)
   */
  public Set<String> getAttributeValues(String name1) {
    return getAttributeValues(name1, true);
  }
  
  /**
   * @see edu.internet2.middleware.subject.Subject#getAttributeValues(java.lang.String, boolean)
   */
  public Set<String> getAttributeValues(String name1, boolean excludeInternalAttributes) {
    this.initAttributesIfNeeded();
    if (this.attributes == null) {
      return new LinkedHashSet<String>();
    }
    
    if (excludeInternalAttributes && !StringUtils.isBlank(this.sourceId) && getSource().getInternalAttributes().contains(name1)) {
      return null;
    }
    
    return this.attributes.get(name1);
  }

  /** if we have initted the attributes */
  private boolean attributesInitted = false;

  /**
   * 
   */
  private void initAttributesIfNeeded() {
    
    //if no source, cant init
    //maybe we are just creating the subject or something...
    if (StringUtils.isBlank(this.sourceId)) {
      return;
    }
    
    if (!this.attributesInitted) {
      //NOTE, there could be race conditions here (marking initted before initted), 
      //but we get endless loops without this
      this.attributesInitted = true;
      initVirtualAttributes(this);
    }
  }

  /**
   * make sure the virtual attributes are setup for the subject
   * @param subject
   */
  public static void initVirtualAttributes(Subject subject) {
    
    Source source = subject.getSource();
    Map<String, String> virtualAttributes = virtualAttributesForSource(source);
    if (SubjectUtils.length(virtualAttributes) == 0) {
      return;
    }
    
    Map<String, Object> variableMap = new HashMap<String, Object>();
    variableMap.put("subject", subject);

    Map<String, String> virtualAttributeVariables = virtualAttributeVariablesForSource(source);
    if (SubjectUtils.length(virtualAttributeVariables) > 0) {
      for (String name : virtualAttributeVariables.keySet()) {
        
        String className = virtualAttributeVariables.get(name);
        Class<?> theClass = SubjectUtils.forName(className);
        Object instance = SubjectUtils.newInstance(theClass);
        variableMap.put(name, instance);
      }
    }
    
    //take each attribute and init it
    for (String attributeName : virtualAttributes.keySet()) {
      
      String el = virtualAttributes.get(attributeName);
      //TODO dont warn on null values
      String value =  SubjectUtils.substituteExpressionLanguage(el, variableMap, true);
      Set<String> valueSet = new HashSet<String>();
      valueSet.add(value);
      subject.getAttributes(false).put(attributeName, valueSet);
    }
    
    
  }

  
  /**
   * get the ordered list of virtual attributes for a source
   * @param source
   * @return the ordered list
   */
  @SuppressWarnings("unchecked")
  public static Map<String, String> virtualAttributeVariablesForSource(Source source) {

    Map<String, String> virtualAttributeVariables = virtualAttributeVariablesForSource.get(source.getId());
    if (virtualAttributeVariables!=null) {
      return virtualAttributeVariables;
    }
    
    virtualAttributeVariables = new LinkedHashMap<String, String>();
    Properties properties = source.getInitParams();
    
    //no virtuals
    if (properties != null && properties.size() > 0) {
      
      //these are the virtual names:
      Set<String> propertiesSet = new HashSet<String>((Set<String>)(Object)properties.keySet());
      
      Iterator<String> iterator = propertiesSet.iterator();
      
      Pattern pattern = Pattern.compile("^subjectVirtualAttributeVariable_(.*)$");

      while (iterator.hasNext()) {
        String property = iterator.next();
        Matcher matcher = pattern.matcher(property);
        if (matcher.matches()) {
          String name = matcher.group(1);
          if (!name.matches("[a-zA-Z0-9_]+")) {
            String message = "Virtual attribute variable name (from sources.xml?) must be alphanumeric, or underscore: '" 
              + name + "' for source: " + source.getId();
            log.error(message);
            throw new RuntimeException(message);
          }
          virtualAttributeVariables.put(name, properties.getProperty(property));
        }
      }
    }
    
    virtualAttributeVariablesForSource.put(source.getId(), virtualAttributeVariables);
    return virtualAttributeVariables;
  }
  
  /** expirable cache will not look at configs all the time, but will refresh */
  private static ExpirableCache<String, Map<String, String>> virtualAttributeVariablesForSource = 
    new ExpirableCache<String, Map<String, String>>(2);
  

  /**
   * get the ordered list of virtual attributes for a source
   * @param source
   * @return the ordered list
   */
  @SuppressWarnings("unchecked")
  public static Map<String, String> virtualAttributesForSource(Source source) {

    Map<String, String> virtualAttributes = virtualAttributeForSource.get(source.getId());
    if (virtualAttributes!=null) {
      return virtualAttributes;
    }
    
    virtualAttributes = new LinkedHashMap<String, String>();
    Properties properties = source.getInitParams();
    
    //no virtuals
    if (properties != null && properties.size() > 0) {
      
      //these are the virtual names:
      Set<String> virtualKeys = new HashSet<String>((Set<String>)(Object)properties.keySet());
      

      Iterator<String> iterator = virtualKeys.iterator();
      
      while (iterator.hasNext()) {
        String virtualKey = iterator.next();
        if (!virtualKey.startsWith("subjectVirtualAttribute_")) {
          iterator.remove();
        }
      }
      
      //look for virtuals, we need these in order since they might depend on each other
      for (int i=0;i<100;i++) {

        //maybe we are done
        if (virtualKeys.size() == 0) {
          break;
        }
        
        iterator = virtualKeys.iterator();
        
        Pattern pattern = Pattern.compile("^subjectVirtualAttribute_" + i + "_(.*)$");
        
        //subjectVirtualAttribute_0_someName (name alphanumeric underscore) JEXL expression
        while (iterator.hasNext()) {
          String key = iterator.next();
          Matcher matcher = pattern.matcher(key);
          if (matcher.matches()) {
            String name = matcher.group(1);
            if (!name.matches("[a-zA-Z0-9_]+")) {
              String message = "Virtual attribute name (from sources.xml?) must be alphanumeric, or underscore: '" 
                + name + "' for source: " + source.getId();
              log.error(message);
              throw new RuntimeException(message);
            }
            virtualAttributes.put(name, properties.getProperty(key));
            iterator.remove();
          }
        }
      }
      if (virtualKeys.size() > 0) {
        log.error("Invalid virtual attribute keys: " + SubjectUtils.toStringForLog(virtualKeys) + ", for source: " + source.getId());
      }
    }
    
    virtualAttributeForSource.put(source.getId(), virtualAttributes);
    return virtualAttributes;
  }
  
  /** expirable cache will not look at configs all the time, but will refresh */
  private static ExpirableCache<String, Map<String, String>> virtualAttributeForSource = 
    new ExpirableCache<String, Map<String, String>>(2);
  
  /**
   * @see edu.internet2.middleware.subject.Subject#getAttributes()
   */
  public Map<String, Set<String>> getAttributes() {
    Map<String, Set<String>> result = getAttributes(true);
    if (!(result instanceof SubjectCaseInsensitiveMap)) {
      log.error("Why is attribute map not case insensitive???? " + this, new RuntimeException());
    }
    return result;
  }
  
  /**
   * @see edu.internet2.middleware.subject.Subject#getAttributes(boolean)
   */
  public Map<String, Set<String>> getAttributes(boolean excludeInternalAttributes) {
    this.initAttributesIfNeeded();
    
    //note, it is assume that if not excluding that the actual
    //map will be returned so it can be modified
    if (!excludeInternalAttributes || StringUtils.isBlank(this.sourceId)) {
      return this.attributes;
    }
    
    Set<String> internalAttributes = getSource().getInternalAttributes();
    
    if (!(internalAttributes instanceof SubjectCaseInsensitiveSet)) {
      internalAttributes = new SubjectCaseInsensitiveSetImpl<String>(internalAttributes);
    }
    
    Map<String, Set<String>> nonInternalAttributes = new SubjectCaseInsensitiveMapImpl<String, Set<String>>();
    
    for (String attribute : attributes.keySet()) {
      if (!internalAttributes.contains(attribute)) {
        nonInternalAttributes.put(attribute, attributes.get(attribute));
      }
    }
    
    return nonInternalAttributes;
  }

  /**
   * {@inheritDoc}
   */
  public Source getSource() {
    return SourceManager.getInstance().getSource(this.sourceId);
  }

  /**
   * 
   * @param attributes1
   */
  public void setAttributes(Map<String, Set<String>> attributes1) {
    if (attributes1 == null || attributes1 instanceof SubjectCaseInsensitiveMap) {
      this.attributes = attributes1;
    } else {
      this.attributes = new SubjectCaseInsensitiveMapImpl<String, Set<String>>(attributes1);
    }
    this.attributesInitted = false;
  }


  /**
   * @see edu.internet2.middleware.subject.Subject#getTypeName()
   */
  public String getTypeName() {
    return this.typeName;
  }


  
  /**
   * @param id1 the id to set
   */
  public void setId(String id1) {
    this.id = id1;
  }


  
  /**
   * @param name1 the name to set
   */
  public void setName(String name1) {
    this.name = name1;
  }


  
  /**
   * @param description1 the description to set
   */
  public void setDescription(String description1) {
    this.description = description1;
  }


  
  /**
   * @param typeName1 the typeName to set
   */
  public void setTypeName(String typeName1) {
    this.typeName = typeName1;
  }


  /**
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    return equalsStatic(this, obj);
  }


  /**
   * @param subject 
   * @param obj
   * @return true if equal
   */
  public static boolean equalsStatic(Subject subject, Object obj) {
    if (subject == obj) {
      return true;
    }
    //this catches null too
    if (!(obj instanceof Subject)) {
      return false;
    }
    Subject otherObj = (Subject) obj;
    return StringUtils.equals(subject.getId(), otherObj.getId()) 
      && StringUtils.equals(subject.getSourceId(), otherObj.getSourceId());
  }


  /**
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    return hashcodeStatic(this);
  }


  /**
   * @param subject 
   * @return hash code
   */
  public static int hashcodeStatic(Subject subject) {
    return new HashCodeBuilder().append(subject.getId()).append(subject.getSourceId()).toHashCode();
  }

  /**
   * @see edu.internet2.middleware.subject.Subject#getAttributeValueOrCommaSeparated(java.lang.String)
   */
  public String getAttributeValueOrCommaSeparated(String attributeName) {
    return getAttributeValueOrCommaSeparated(attributeName, true);
  }
  
  /**
   * @see edu.internet2.middleware.subject.Subject#getAttributeValueOrCommaSeparated(java.lang.String, boolean)
   */
  public String getAttributeValueOrCommaSeparated(String attributeName, boolean excludeInternalAttributes) {
    this.initAttributesIfNeeded();
    return attributeValueOrCommaSeparated(this, attributeName, excludeInternalAttributes);
  }

  /**
   * @see Subject#getAttributeValueOrCommaSeparated(String)
   * @param subject shouldnt be null
   * @param attributeName
   * @return the string
   */
  public static String attributeValueOrCommaSeparated(Subject subject, String attributeName) {
    return attributeValueOrCommaSeparated(subject, attributeName, true);
  }

  /**
   * @see Subject#getAttributeValueOrCommaSeparated(String, boolean)
   * @param subject shouldnt be null
   * @param attributeName
   * @param excludeInternalAttributes 
   * @return the string
   */
  public static String attributeValueOrCommaSeparated(Subject subject, String attributeName, boolean excludeInternalAttributes) {
    if (excludeInternalAttributes && !StringUtils.isBlank(subject.getSourceId()) && subject.getSource().getInternalAttributes().contains(attributeName)) {
      return null;
    }
    
    Set<String> attributeValues = subject.getAttributeValues(attributeName);
    if (attributeValues == null || attributeValues.size() == 0) {
      return null;
    }
    return StringUtils.join(attributeValues.iterator(), ", ");
  }
  
  /**
   * @see edu.internet2.middleware.subject.Subject#getAttributeValueSingleValued(java.lang.String)
   */
  public String getAttributeValueSingleValued(String attributeName) {
    return getAttributeValueSingleValued(attributeName, true);
  }
  
  /**
   * @see edu.internet2.middleware.subject.Subject#getAttributeValueSingleValued(java.lang.String, boolean)
   */
  public String getAttributeValueSingleValued(String attributeName, boolean excludeInternalAttributes) {
    this.initAttributesIfNeeded();
    if (this.attributes == null) {
      return null;
    }
    
    if (excludeInternalAttributes && !StringUtils.isBlank(this.sourceId) && getSource().getInternalAttributes().contains(attributeName)) {
      return null;
    }
    
    Set<String> values = this.attributes.get(attributeName);
    if (values != null) {
      if (values.size() > 1) {
        throw new RuntimeException(
            "This is not a single valued attribute, it is multivalued: '" + attributeName + "', " + values.size());
      }
      return values.iterator().next();
    }
    return null;
  }

  
  
}
