/*
  Copyright 2004-2005 University Corporation for Advanced Internet Development, Inc.
  Copyright 2004-2005 The University Of Chicago

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

package edu.internet2.middleware.grouper;

import  edu.internet2.middleware.subject.*;
import  edu.internet2.middleware.subject.provider.*;
import  java.util.*;
import  org.apache.commons.lang.builder.ToStringBuilder;

/**
 * Internal <i>Subject</i> returned by an {@link InternalSourceAdapter}.
 * <p />
 * @author  blair christensen.
 * @version $Id: InternalSubject.java,v 1.5 2005-12-15 16:22:42 blair Exp $
 */
public class InternalSubject implements Subject {

  // Private Instance Variables

  private Source      adapter = new InternalSourceAdapter();
  private Map         attrs   = new HashMap();
  private String      desc    = new String();
  private String      id      = new String();
  private String      name    = new String();
  private SubjectType type    = SubjectTypeEnum.valueOf(GrouperConfig.IST);


  // Constructors

  // Create an internal subjecct
  // @param id      Id of this subjecct.
  // @param name    Name of this subject.
  // @param adapter Source adapter that retrieved this subject.
  protected InternalSubject(String id, String name, InternalSourceAdapter adapter) {
    this.adapter  = adapter;
    this.desc     = name;
    this.id       = id;
    this.name     = name;
  } // protected InternalSubject(id, name, adapter)


  // Public Instance Methods
  
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
    return new LinkedHashSet();
  } // public Set getAttributeValues(name)

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
   * Return a string representation of the object.
   */
  public String toString() {
    return new ToStringBuilder(this)
      .append("subjectID"     , this.getId()              )
      .append("subjectTypeID" , this.getType().getName()  )
      .append("name"          , this.getName()            )
      .toString();
  } // public String toString()

}

