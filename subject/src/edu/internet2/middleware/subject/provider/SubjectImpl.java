/*--
$Id: SubjectImpl.java,v 1.1 2009-09-02 05:40:10 mchyzer Exp $
$Date: 2009-09-02 05:40:10 $
 
Copyright 2005 Internet2 and Stanford University.  All Rights Reserved.
See doc/license.txt in this distribution.
 */
package edu.internet2.middleware.subject.provider;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.internet2.middleware.subject.Source;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.SubjectType;

/**
 * Base Subject implementation.  Subclass this to change behavior
 */
public class SubjectImpl implements Subject {

  /**
   * turn some strings into a map, every other is a name or value of attribute
   * @param strings
   * @return the map (never null)
   */
  public static Map<String, Set<String>> toAttributeMap(String... strings) {
    Map<String, Set<String>> map = new LinkedHashMap<String, Set<String>>();
    if (strings != null) {
      if (strings.length % 2 != 0) {
        throw new RuntimeException("Must pass in an odd number of strings: " + strings.length);
      }
      for (int i=0;i<strings.length;i+=2) {
        Set<String> set = new LinkedHashSet<String>();
        set.add(strings[i+2]);
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
  @SuppressWarnings("unused")
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
    this(id1, name1, description1, typeName1, sourceId1, new LinkedHashMap<String, Set<String>>());
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
    this.attributes = attributes1;
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
   * {@inheritDoc}
   */
  public String getAttributeValue(String name1) {
    if (this.attributes == null) {
      return null;
    }
    Set<String> values = this.attributes.get(name1);
    if (values != null) {
      //return the first, no matter how many there are
      return values.iterator().next();
    }
    return null;
  }

  /**
   * {@inheritDoc}
   */
  public Set<String> getAttributeValues(String name1) {
    if (this.attributes == null) {
      return new LinkedHashSet<String>();
    }
    return this.attributes.get(name1);
  }


  /**
   * {@inheritDoc}
   */
  public Map<String, Set<String>> getAttributes() {
    return this.attributes;
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
    this.attributes = attributes1;
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
    return attributeValueOrCommaSeparated(this, attributeName);
  }


  /**
   * @see Subject#getAttributeValueOrCommaSeparated(String)
   * @param subject shouldnt be null
   * @param attributeName
   * @return the string
   */
  public static String attributeValueOrCommaSeparated(Subject subject, String attributeName) {
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
    if (this.attributes == null) {
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
