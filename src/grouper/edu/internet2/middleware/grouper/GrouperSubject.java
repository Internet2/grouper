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
import  java.util.*;
import  net.sf.hibernate.*;

/** 
 * {@link Subject} returned by the {@link GrouperSourceAdapter}.
 * <p />
 * @author  blair christensen.
 * @version $Id: GrouperSubject.java,v 1.14 2006-06-15 00:07:02 blair Exp $
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
    this.adapter  = (GrouperSourceAdapter) SubjectFinder.getSource(GrouperSourceAdapter.ID);
  } // protected GrouperSubject(g, sa)


  // PUBLIC INSTANCE METHODS //
  public Map getAttributes() {
    this._addAttrs();
    return this.attrs;
  } // public Map getAttributes()

  public String getAttributeValue(String name) {
    this._addAttrs();
    if (this.attrs.containsKey(name)) {
      return (String) this.attrs.get(name);
    }
    return GrouperConfig.EMPTY_STRING;
  } // public String getAttributevalue(name)

  public Set getAttributeValues(String name) {
    return ATTR_VALUES;
  } // public Set getAttributeValues(name)

  public String getDescription() {
    return this.getAttributeValue("description");
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


  // PRIVATE INSTANCE METHODS
  private void _addAttr(String attr, String value) {
    // TODO Should I be applying a regex to the attr name?  The current check
    //      is fairly naive - but I guess right now only *I* can add items so
    //      it is probably sufficient - for now.
    //      Use Commons validating code?
    if ( 
      (attr   != null       ) 
      && (attr.length() > 0 )
      && (value  != null    ) 
      && (value.length() > 0)
    ) 
    {
      this.attrs.put(attr, value);
      DebugLog.info(
        GrouperSubject.class,
        "[" + this.name + "] attached attribute: '" + attr + "' = '" + value + "'"
      );
    }
  } // private void _addAttr(attr, value)

  private void _addAttrs() {
    // TODO Ideally I wouldn't just iterate through the appropriate items in
    //      the fields list but I think I need more logic than that
    // TODO Attach lists.  Maybe.  Aren't there security issues with
    //      that?  We're already ignoring them when it comes to attributes.
    try {
      Session hs = HibernateHelper.getSession();
      hs.refresh(this.g);
      hs.close();
    }
    catch (HibernateException eH) {
      String msg = "unable to refresh group subject (" + this.name + "): " + eH.getMessage();
      ErrorLog.error(GrouperSubject.class, msg);
    }
    try {
      // Don't bother with any of the create* attrs unless we can find
      // the creating subject
      Subject creator = this.g.getCreateSubject();
      this._addAttr("createSubjectId"   , creator.getId()                   );
      this._addAttr("createSubjectType" , creator.getType().getName()       );
      this._addAttr("createTime"        , this.g.getCreateTime().toString() ); 
    }
    catch (SubjectNotFoundException eSNF0) {
      ErrorLog.error(GrouperSubject.class, E.GSUBJ_NOCREATOR + eSNF0.getMessage());
    }
    try {
      // Don't bother with any of the modify* attrs unless we can find
      // the modifying subject
      Subject modifier = this.g.getModifySubject();
      this._addAttr("modifySubjectId"   , modifier.getId()                  );
      this._addAttr("modifySubjectType" , modifier.getType().getName()      );
      this._addAttr("modifyTime"        , this.g.getModifyTime().toString() ); 
    }
    catch (SubjectNotFoundException eSNF1) {
      // No modifier
    }
    Map       attrs = this.g.getAttributes();
    Iterator  iter  = attrs.keySet().iterator();
    while (iter.hasNext()) {
      String key = (String) iter.next();
      this._addAttr(key, (String) attrs.get(key));
    }
    DebugLog.info(
      GrouperSubject.class, 
      "[" + this.name + "] attached attributes: " + this.attrs.size()
    );
  } // private void _addAttrs(g)
}

