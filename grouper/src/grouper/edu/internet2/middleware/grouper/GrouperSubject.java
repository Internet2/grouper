/*
  Copyright 2004-2006 University Corporation for Advanced Internet Development, Inc.
  Copyright 2004-2006 The University Of Chicago

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
import  java.util.Iterator;
import  java.util.HashMap;
import  java.util.LinkedHashSet;
import  java.util.Map;
import  java.util.Set;
import  org.apache.commons.lang.builder.*;

/** 
 * {@link Subject} returned by the {@link GrouperSourceAdapter}.
 * <p/>
 * @author  blair christensen.
 * @version $Id: GrouperSubject.java,v 1.28 2006-12-27 18:22:21 blair Exp $
 */
public class GrouperSubject implements Subject {

  // PRIVATE CLASS CONSTANTS //
  private static final Set  ATTR_VALUES = new LinkedHashSet();


  // PRIVATE INSTANCE VARIABLES //
  private GrouperSourceAdapter  adapter = null;
  private Map                   attrs   = new HashMap();
  private Group                 g       = null;
  private String                id      = null;
  private String                name    = null;
  private SubjectType           type    = SubjectTypeEnum.valueOf("group");


  // CONSTRUCTORS //
  protected GrouperSubject(Group g) 
    throws  SourceUnavailableException
  {
    this.g        = g;
    this.id       = g.getUuid();
    this.name     = g.getName();
    this.adapter  = (GrouperSourceAdapter) SubjectFinder.getGSA();
  } // protected GrouperSubject(g, sa)


  // PUBLIC INSTANCE METHODS //

  /**
   * @since 1.0
   */
  public boolean equals(Object other) {
    // TODO 20061011 I should modify *SubjectHelper.eq(this, other)* so that I can just call that.
    if (this == other) {
      return true;
    }
    if (!(other instanceof Subject)) {
      return false;
    }
    Subject otherSubject = (Subject) other;
    return new EqualsBuilder()
      .append(  this.getId()              , otherSubject.getId()              )
      .append(  this.getSource().getId()  , otherSubject.getSource().getId()  )
      .append(  this.getType().getName()  , otherSubject.getType().getName()  )
      .isEquals();
  } // public boolean equals(other)

  public Map getAttributes() {
    return this._getAttributes();
  } // public Map getAttributes()

  public String getAttributeValue(String name) {
    if ( this._getAttributes().containsKey(name) ) {
      return (String) this.attrs.get(name);
    }
    return GrouperConfig.EMPTY_STRING;
  } // public String getAttributevalue(name)

  public Set getAttributeValues(String name) {
    return ATTR_VALUES;
  } // public Set getAttributeValues(name)

  public String getDescription() {
    return this.getAttributeValue(GrouperConfig.ATTR_D);
  } // public String getDescription()

  public String getId() {
    return this.id;
  } // public String getId()

  public String getName() {
    return this.name;
  } // public String getName()

  public Source getSource() {
    return this.adapter;
  } // public Source getSource()

  public SubjectType getType() {
    return this.type;
  } // public SubjectType getType()

  /**
   * @since 1.0
   */ 
  public int hashCode() {
    return new HashCodeBuilder()
      .append(  this.getId()              )
      .append(  this.getSource().getId()  )
      .append(  this.getType().getName()  )
      .toHashCode()
      ;
  } // public int hashCode()


  // PRIVATE INSTANCE METHODS //
 
  // @since   1.2.0 
  private Map _getAttributes() {
    if ( this.attrs.size() == 0 ) {
      this.g = HibernateGroupDAO.findByUuid( this.getId() );
      if (this.g == null) {
        ErrorLog.error(GrouperSubject.class, "unable to retrieve group attributes");
      }
      else {
        this._populateAttributes(); // populates `this.attrs`
      }
    }
    return this.attrs;
  } // private Map _getAttributes()

  // @since   1.2.0
  private void _populateAttributes() {
    try {
      // Don't bother with any of the create* attrs unless we can find
      // the creating subject
      Subject creator = this.g.getCreateSubject();
      this.attrs.put( "createSubjectId",   creator.getId()                   );
      this.attrs.put( "createSubjectType", creator.getType().getName()       );
      this.attrs.put( "createTime",        this.g.getCreateTime().toString() ); 
    }
    catch (SubjectNotFoundException eSNF0) {
      ErrorLog.error(GrouperSubject.class, E.GSUBJ_NOCREATOR + eSNF0.getMessage());
    }
    try {
      // Don't bother with any of the modify* attrs unless we can find
      // the modifying subject
      Subject modifier = this.g.getModifySubject();
      this.attrs.put( "modifySubjectId",   modifier.getId()                  );
      this.attrs.put( "modifySubjectType", modifier.getType().getName()      );
      this.attrs.put( "modifyTime",        this.g.getModifyTime().toString() ); 
    }
    catch (SubjectNotFoundException eSNF1) {
      // No modifier
    }
    Map.Entry e;
    Iterator  it  = this.g.internal_getAttributes().entrySet().iterator();
    while (it.hasNext()) {
      e = (Map.Entry) it.next();
      this.attrs.put( (String) e.getKey(), ( (Attribute) e.getValue() ).getValue() );
    }
    DebugLog.info( GrouperSubject.class, "[" + this.name + "] attached attributes: " + this.attrs.size() );
  } // private void _populateAttributes()

} // public class GrouperSubject implements Subject

