/*
 * Copyright (C) 2005 blair christensen.
 * All Rights Reserved.
 *
 * You may use and distribute under the same terms as Grouper itself.
 */

package com.devclue.grouper.subject;

import  edu.internet2.middleware.subject.*;
import  edu.internet2.middleware.subject.provider.*;
import  java.util.*;
import  org.apache.commons.lang.builder.ToStringBuilder;

/**
 * Mock <i>Subject</i> for a {@link MockSourceAdapter}.
 * <p />
 * @author  blair christensen.
 * @version $Id: MockSubject.java,v 1.1 2005-12-16 21:48:00 blair Exp $
 */
public class MockSubject implements Subject {

  /*
   * PRIVATE INSTANCE VARIABLES
   */

  private Source      adapter = new MockSourceAdapter();
  private Map         attrs   = new HashMap();
  private String      desc    = new String();
  private String      id      = new String();
  private String      name    = new String();
  private SubjectType type    = SubjectTypeEnum.valueOf("person");


  /*
   * CONSTRUCTORS
   */

  /**
   * Create a mock subject.
   * <pre class="eg">
   * Subject ms = new MockSubject();
   * </pre>
   */
  public MockSubject() {
    super();
  } // public MockSubject()

  /**
   * Create a mock subject.
   * <pre class="eg">
   * // Create a new subject.
   * Subject ms = new MockSubject(
   *   id, name, new MockSourceAdapter()
   * );
   * </pre>
   * @param id      Id of this subjecct.
   * @param name    Name of this subject.
   * @param adapter Source adapter that retrieved this subject.
   */
  public MockSubject(String id, String name, MockSourceAdapter adapter) {
    this.adapter  = adapter;
    this.desc     = name;
    this.id       = id;
    this.name     = name;
  } // public MockSubject(id, name, adapter)


  /*
   * PUBLIC INSTANCE METHODS
   */

  /**
   * Gets a map of attribute names and values.
   * <p>
   * <b>NOTE:</b> This only returns the "first" value of multi-valued
   * attributes.
   * </p>
   * <pre class="eg">
   * // Retrieve subject attributes for the subject <i>john</i>.
   * Map attributes = john.getAttributes();
   * </pre>
   * @return  Map of subject attributes.
   */
  public Map getAttributes() {
    Map       attrs = new HashMap();
    Iterator iter   = this.attrs.keySet().iterator();
    while (iter.hasNext()) {
      String name = (String) iter.next();
      attrs.put( name, this.getAttributeValue(name) );
    }
    return attrs;
  } // public Map getAttributes()

  /**
   * Returns the value of a single-valued attribute or the "first"
   * value of a multi-valued attribute.
   * <pre class="eg">
   * // Retrieve the <i>loginID</i> attribute for subject <i>john</i>.
   * String loginID = john.getAttributeValue("loginID");
   * </pre>
   * @param   name  Retrieve value of this attribute.
   * @return  String value of attribute.
   */
  public String getAttributeValue(String name) {
    if (this.attrs.containsKey(name)) {
      Map       attr  = (Map) this.attrs.get(name);
      Iterator  iter  = ( (Set) attr.get("v") ).iterator(); 
      // There may be more than one value but we don't care - return
      // the first one
      while (iter.hasNext()) {
        return (String) iter.next();
      }  
    }
    return new String();
  } // public String getAttributeValue(name)

  /**
   * Returns the search value of a single-valued attribute or the "first"
   * value of a multi-valued attribute.
   * <p>
   * <b>NOTE:</b> This is a {@link MockSubject} extension to the 
   * {@link Subject} interface.
   * </p>
   * <pre class="eg">
   * // Retrieve the search value of the <i>name</i> attribute for the
   * // subject <i>john</i>.
   * String name = john.getAttributeSearchValue("name");
   * </pre>
   * @param   name  Retrieve search value of this attribute.
   * @return  Search value of attribute.
   */
  public String getAttributeSearchValue(String name) {
    if (this.attrs.containsKey(name)) {
      Map       attr  = (Map) this.attrs.get(name);
      Iterator  iter  = ( (Set) attr.get("sv") ).iterator(); 
      // There may be more than one value but we don't care - return
      // the first one
      while (iter.hasNext()) {
        return (String) iter.next();
      }  
    }
    return new String();
  } // public String getAttributeSearchValue(name)

  /**
   * Returns the values of a multi-valued attribute.
   * <pre class="eg">
   * // Retrieve the values of the <i>isMember</i> attribute for the
   * // subject <i>john</i>.
   * Set isMember = john.getAttributeValues("isMember");
   * </pre>
   * @param   name  Retrieve values of this attribute.
   * @return  Set of attribute values.
   */
  public Set getAttributeValues(String name) {
    if (this.attrs.containsKey(name)) {
      Map attr = (Map) this.attrs.get(name);
      return (Set) attr.get("v");
    }
    return new HashSet();
  } // public Set getAttributeValues(name)

  /**
   * Returns the search values of a multi-valued attribute.
   * <b>NOTE:</b> This is a {@link MockSubject} extension to the 
   * {@link Subject} interface.
   * </p>
   * <pre class="eg">
   * // Retrieve the search values of the <i>isMember</i> attribute for 
   * // the subject <i>john</i>.
   * Set isMember = john.getAttributeSearchValues("isMember");
   * </pre>
   * @param   name  Retrieve search values of this attribute.
   * @return  Set of attribute search values.
   */
  public Set getAttributeSearchValues(String name) {
    if (this.attrs.containsKey(name)) {
      Map attr = (Map) this.attrs.get(name);
      return (Set) attr.get("sv");
    }
    return new HashSet();
  } // public Set getAttributeSearchValues(name)

  /**
   * Gets this Subject's description.
   * <pre class="eg">
   * // Retrieve subject <i>john</i>'s <i>description</i> attribute
   * // value.
   * String description = john.getDescription();
   * </pre>
   * @return  Description value.
   */
  public String getDescription() {
    return this.desc;
  } // public String getDescription()
  
  /**
   * Gets this Subject's ID.
   * <pre class="eg">
   * // Retrieve subject <i>john</i>'s subject id.
   * String id = john.getId();
   * </pre>
   * @return  Subject ID.
   */
  public String getId() {
    return this.id;
  } // public String getId()

  /**
   * Gets this Subject's name.
   * <pre class="eg">
   * // Retrieve subject <i>john</i>'s <i>name</i> value.
   * String name = john.getName();
   * </pre>
   * @return  Name value.
   */
  public String getName() {
    return this.name;
  } // public String getName()

  /**
   * Returns the {@link Source} of this {@link Subject}.
   * <pre class="eg">
   * // Retrieve subject <i>john</i>'s source.
   * Source sa = john.getSource();
   * </pre>
   * @return  Source adapter.
   */
  public Source getSource() {
    return this.adapter;
  } // public String getSource()

  /**
   * Gets this {@link Subject}'s type.
   * <pre class="eg">
   * // Retrieve subject <i>john</i>'s subject type.
   * SubjectType type = john.getType();
   * </pre>
   * @return  Subject type.
   */
  public SubjectType getType() {
    return this.type;
  } // public String getType()

  /**
   * Set attribute value.
   * <p>
   * <b>NOTE:</b> This is a {@link MockSubject} extension to the 
   * {@link Subject} interface.
   * </p>
   * <pre class="eg">
   * // Set <i>loginID</i> value for subject <i>john</i>.
   * ( (MockSubject) john).setAttributeValue("loginID", value);
   * </pre>
   * @param name  Set this attribute.
   * @param value Set attribute to this value.
   */
  public void setAttributeValue(String name, String value) {
    Map attr    = new HashMap();
    Set vals    = new HashSet();  // Values
    Set svals   = new HashSet();  // Search Values
    if (this.attrs.containsKey(name)) {
      attr  = (Map) this.attrs.get(name);
      vals  = (Set) attr.get("v");
      svals = (Set) attr.get("sv");
    } 
    vals.add(value);
    svals.add(value);
    attr.put("v", vals);
    attr.put("sv", svals);
    this.attrs.put(name, attr);
  } // public void setAttributeValue(name, value)

  /**
   * Set attribute value and search value.
   * <p>
   * <b>NOTE:</b> This is a {@link MockSubject} extension to the 
   * {@link Subject} interface.
   * </p>
   * <pre class="eg">
   * // Set <i>loginID</i> value and search value for subject
   * // <i>john</i>.
   * ( (MockSubject) john).setAttributeSearchValue(
   *   "loginID", value, searchValue
   * );
   * </pre>
   * @param name        Set this attribute.
   * @param value       Set attribute to this value.
   * @param searchValue Set attribute to this search value.
   */
  public void setAttributeSearchValue(
    String name, String value, String searchValue
  ) 
  {
    Map attr    = new HashMap();
    Set vals    = new HashSet();  // Values
    Set svals   = new HashSet();  // Search Values
    if (this.attrs.containsKey(name)) {
      attr  = (Map) this.attrs.get(name);
      vals  = (Set) attr.get("v");
      svals = (Set) attr.get("sv");
    } 
    vals.add(value);
    svals.add(searchValue);
    attr.put("v", vals);
    attr.put("sv", svals);
    this.attrs.put(name, attr);
  } // public void setAttributeSearchValue(name, value, searchValue)

  /**
   * Set <i>description</i> value.
   * <p>
   * <b>NOTE:</b> This is a {@link MockSubject} extension to the 
   * {@link Subject} interface.
   * </p>
   * <pre class="eg">
   * // Set <i>description</i> for subject <i>john</i>.
   * ( (MockSubject) john).setADescription(value);
   * </pre>
   * @param value Set <i>description</i> to this value.
   */
  public void setDescriptionValue(String value) {
    if (value == null || value.equals("")) {
      throw new RuntimeException("ERROR: No description value specified");
    }
    this.desc = value;
  } // public void setDescriptionValue(value)

  /**
   * Set <i>id</i> value.
   * <p>
   * <b>NOTE:</b> This is a {@link MockSubject} extension to the 
   * {@link Subject} interface.
   * </p>
   * <pre class="eg">
   * // Set <i>subject id</i> for subject <i>john</i>.
   * ( (MockSubject) john).setIdValue(value);
   * </pre>
   * @param value Set subject id to this value.
   */
  public void setIdValue(String value) {
    if (value == null || value.equals("")) {
      throw new RuntimeException("ERROR: No id value specified");
    }
    this.id = value;
  } // public void setIdValue(value)

  /**
   * Set <i>name</i> value.
   * <p>
   * <b>NOTE:</b> This is a {@link MockSubject} extension to the 
   * {@link Subject} interface.
   * </p>
   * <pre class="eg">
   * // Set <i>name</i> for subject <i>john</i>.
   * ( (MockSubject) john).setName(value);
   * </pre>
   * @param value Set <i>name</i> to this value.
   */
  public void setNameValue(String value) {
    if (value == null || value.equals("")) {
      throw new RuntimeException("ERROR: No name value specified");
    }
    this.name = value;
  } // public void setNameValue(value)

  /**
   * Set the {@link Source} of this {@link Subject}.
   * <p>
   * <b>NOTE:</b> This is a {@link MockSubject} extension to the 
   * {@link Subject} interface.
   * </p>
   * <pre class="eg">
   * // Set source for subject <i>john</i>.
   * ( (MockSubject) john).setSource( new MockSourceAdapter() );
   * </pre>
   * @param adapter Set source to this adapter.
   */
  public void setSource(Source adapter) {
    this.adapter = adapter;
  } // public void setSource(adapter)

  /**
   * Set the {@link SubjectType} of this {@link Subject}.
   * <p>
   * <b>NOTE:</b> This is a {@link MockSubject} extension to the 
   * {@link Subject} interface.
   * </p>
   * <pre class="eg">
   * // Set subject type for subject <i>john</i>.
   * ( (MockSubject) john).setSubjectType(type);
   * </pre>
   * @param type Set subject type to this value.
   */
  public void setType(String type) {
    if (type == null || type.equals("")) {
      throw new RuntimeException("ERROR: No type value specified");
    }
    this.type = SubjectTypeEnum.valueOf(type);
  } // public void setType(type)

  /**
   * Return a string representation of the object.
   */
  public String toString() {
    ToStringBuilder tsb = new ToStringBuilder(this)
      .append("subjectID"     , this.getId()              )
      .append("subjectTypeID" , this.getType().getName()  )
      .append("name"          , this.getName()            )
      .append("description"   , this.getDescription()     );
    // Now add all the attributes along with their values and search
    // values
    Iterator iter = this.getAttributes().keySet().iterator();
    while (iter.hasNext()) {
      String name = (String) iter.next();
      tsb.append(
        name,
        "v["    + this.getAttributeValue(name)        + "] " 
        + "sv[" + this.getAttributeSearchValue(name)  + "]"
      );
    }
    return tsb.toString();
  } // public String toString()

}

