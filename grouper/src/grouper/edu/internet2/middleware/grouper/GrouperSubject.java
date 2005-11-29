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


/** 
 * {@link Subject} returned by the {@link GrouperSourceAdapter}.
 * <p />
 * @author  blair christensen.
 * @version $Id: GrouperSubject.java,v 1.4 2005-11-29 17:25:39 blair Exp $
 */
public class GrouperSubject implements Subject {

  // Private Instance Methods
  private GrouperSourceAdapter  adapter = null;
  private Map                   attrs   = new HashMap(); 
  private String                id      = null;
  private String                name    = null;
  private SubjectType           type    = SubjectTypeEnum.valueOf("group");


  // Constructors
  protected GrouperSubject(Group g, GrouperSourceAdapter sa) {
    this.id       = g.getUuid();
    this.name     = g.getName();
    this.adapter  = sa;
    // Attach attributes
    this._addAttrs(g);
  } // protected GrouperSubject(g, sa)


  // Public Instance Methods
  public Map getAttributes() {
    return this.attrs;
  } // public Map getAttributes()

  public String getAttributeValue(String name) {
    if (this.attrs.containsKey(name)) {
      return (String) this.attrs.get(name);
    }
    return new String();
  } // public String getAttributevalue(name)

  public Set getAttributeValues(String name) {
    throw new RuntimeException("Not implemented");
  } // public Set getAttributeValues(name)

  // TODO Is this even part of the latest spec?
  public String getDescription() {
    throw new RuntimeException("Not implemented");
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


  // Private Instance Methods
  private void _addAttr(String attr, String value) {
    // TODO Should I be applying a regex to the attr name?  The current check
    //      is fairly naive - but I guess right now only *I* can add items so
    //      it is probably sufficient - for now.
    if ( 
      (attr   != null       ) 
      && (attr.length() > 0 )
      && (value  != null    ) 
      && (value.length() > 0)
    ) 
    {
      this.attrs.put(attr, value);
    }
  } // private void _addAttr(attr, value)

  private void _addAttrs(Group g) {
    // TODO Ideally I wouldn't just iterate through the appropriate items in
    //      the fields list but I think I need more logic than that
    try {
      // Don't bother with any of the create* attrs unless we can find
      // the creating subject
      Subject creator = g.getCreateSubject();
      this._addAttr("createSubjectId"   , creator.getId()               );
      this._addAttr("createSubjectType" , creator.getType().getName()   );
      this._addAttr("createTime"        , g.getCreateTime().toString()  ); 
    }
    catch (SubjectNotFoundException eSNF0) {
      // No creator?
    }
    this._addAttr("description"         , g.getDescription()            );
    this._addAttr("displayExtension"    , g.getDisplayExtension()       );
    this._addAttr("displayName"         , g.getDisplayName()            );
    this._addAttr("extension"           , g.getExtension()              );
    try {
      // Don't bother with any of the modify* attrs unless we can find
      // the modifying subject
      Subject modifier = g.getModifySubject();
      this._addAttr("modifySubjectId"   , modifier.getId()              );
      this._addAttr("modifySubjectType" , modifier.getType().getName()  );
      this._addAttr("modifyTime"        , g.getModifyTime().toString()  ); 
    }
    catch (SubjectNotFoundException eSNF1) {
      // No modifier?
    }
    this.attrs.put(
      "name"              , g.getName()
    );
    // TODO Attach custom attributes
    // TODO Attach lists.  "list name" => [ subjectids? ]
  } // private void _addAttrs(g)
}

