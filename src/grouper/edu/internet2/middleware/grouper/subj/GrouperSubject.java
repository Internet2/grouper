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
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GrouperSourceAdapter;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.exception.GroupNotFoundException;
import edu.internet2.middleware.grouper.misc.E;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Source;
import edu.internet2.middleware.subject.SourceUnavailableException;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.SubjectNotFoundException;
import edu.internet2.middleware.subject.SubjectType;
import edu.internet2.middleware.subject.provider.SubjectTypeEnum;

/** 
 * {@link Subject} returned by the {@link GrouperSourceAdapter}.
 * <p/>
 * @author  blair christensen.
 * @version $Id: GrouperSubject.java,v 1.2 2008-09-29 03:38:31 mchyzer Exp $
 */
public class GrouperSubject implements Subject {

  // PRIVATE CLASS CONSTANTS //
  private static final Set  ATTR_VALUES = new LinkedHashSet();


  // PRIVATE INSTANCE VARIABLES //
  private GrouperSourceAdapter  adapter = null;
  private Map<String, String>   attrs   = new HashMap<String, String>();
  private String                id      = null;
  private String                name    = null;
  private SubjectType           type    = SubjectTypeEnum.valueOf("group");


  // CONSTRUCTORS //
  public GrouperSubject(Group g) 
    throws  SourceUnavailableException
  {
    this.id       = g.getUuid();
    this.name     = (String) g.getAttributes().get(GrouperConfig.ATTR_NAME);
    this.adapter  = (GrouperSourceAdapter) SubjectFinder.internal_getGSA();
  } // protected GrouperSubject(g, sa)


  // PUBLIC INSTANCE METHODS //

  /**
   * @since 1.0
   */
  public boolean equals(Object other) {
    return SubjectHelper.eq(this, other);
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

  /* (non-Javadoc)
 * @see edu.internet2.middleware.subject.Subject#getAttributeValues(java.lang.String)
 */
public Set getAttributeValues(String name) {
	//https://bugs.internet2.edu/jira/browse/GRP-40
	//2007-10-18: Gary Brown
	//Simply put value in a Set, however, would
	//need to revisit if Grouper had multi-value String attributes
	Set values = new LinkedHashSet();
	String value = this.getAttributeValue(name);
	if(!GrouperConfig.EMPTY_STRING.equals(value)) 
		values.add(value);
    return values;
  } // public Set getAttributeValues(name)

  public String getDescription() {
    return this.getAttributeValue(GrouperConfig.ATTR_DESCRIPTION);
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
      try {
        Group g = GrouperDAOFactory.getFactory().getGroup().findByUuid( this.getId() ) ;
        this._populateAttributes(g); // populate `this.attrs`
      }
      catch (GroupNotFoundException eGNF) {
        LOG.error("unable to retrieve group attributes: " + eGNF.getMessage() );
      }
    }
    return this.attrs;
  } // private Map _getAttributes()

  // @since   1.2.0
  private void _populateAttributes(Group g) {
    try {
      // Don't bother with any of the create* attrs unless we can find
      // the creating subject
      Subject creator = g.getCreateSubject();
      this.attrs.put( "createSubjectId",   creator.getId()                   );
      this.attrs.put( "createSubjectType", creator.getType().getName()       );
      this.attrs.put( "createTime",        g.getCreateTime().toString() ); 
    }
    catch (SubjectNotFoundException eSNF0) {
      LOG.error(E.GSUBJ_NOCREATOR + eSNF0.getMessage());
    }
    try {
      // Don't bother with any of the modify* attrs unless we can find
      // the modifying subject
      Subject modifier = g.getModifySubject();
      this.attrs.put( "modifySubjectId",   modifier.getId()                  );
      this.attrs.put( "modifySubjectType", modifier.getType().getName()      );
      this.attrs.put( "modifyTime",        g.getModifyTime().toString() ); 
    }
    catch (SubjectNotFoundException eSNF1) {
      // No modifier
    }
    Map.Entry kv;
    Iterator  it  = g.getAttributes().entrySet().iterator();
    while (it.hasNext()) {
      kv = (Map.Entry) it.next();
      this.attrs.put( (String) kv.getKey(), (String) kv.getValue() );
    }
    LOG.info("[" + this.name + "] attached attributes: " + this.attrs.size() );
  } // private void _populateAttributes(g)

  /** logger */
  private static final Log LOG = GrouperUtil.getLog(GrouperSubject.class);


} // public class GrouperSubject implements Subject

