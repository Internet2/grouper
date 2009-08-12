/*
  Copyright (C) 2004-2007 University Corporation for Advanced Internet Development, Inc.
  Copyright (C) 2004-2007 The University Of Chicago

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

package edu.internet2.middleware.grouper.subj;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.builder.ToStringBuilder;

import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Source;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.SubjectType;
import edu.internet2.middleware.subject.provider.SubjectTypeEnum;

/**
 * Internal <i>Subject</i> returned by an {@link InternalSourceAdapter}.
 * <p/>
 * @author  blair christensen.
 * @version $Id: InternalSubject.java,v 1.5 2009-08-12 04:52:21 mchyzer Exp $
 */
public class InternalSubject implements Subject {

  private Map<String, Set<String>>         attrs   = new HashMap<String, Set<String>>();
  private String      desc    = GrouperConfig.EMPTY_STRING;
  private String      id      = GrouperConfig.EMPTY_STRING;
  private String      name    = GrouperConfig.EMPTY_STRING;
  private SubjectType type    = SubjectTypeEnum.valueOf(GrouperConfig.IST);


  // Constructors

  // Create an internal subjecct
  // @param id      Id of this subjecct.
  // @param name    Name of this subject.
  // @param adapter Source adapter that retrieved this subject.
  protected InternalSubject(String id, String name, InternalSourceAdapter adapter) {
    this.desc     = name;
    this.id       = id;
    this.name     = name;
    attrs.put("name", GrouperUtil.toSet(name));
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
  public Map<String, Set<String>> getAttributes() {
    return this.attrs;
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
      Set<String> set = this.attrs.get(name);
      return set.iterator().next();
    }
    return GrouperConfig.EMPTY_STRING;
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
  public Set<String> getAttributeValues(String name) {
    if (this.attrs.containsKey(name)) {
      this.attrs.get(name);
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
    return InternalSourceAdapter.instance();
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
   * @return string
   */
  public String toString() {
    return new ToStringBuilder(this)
      .append("subjectID"     , this.getId()              )
      .append("subjectTypeID" , this.getType().getName()  )
      .append("name"          , this.getName()            )
      .toString();
  } // public String toString()

}

