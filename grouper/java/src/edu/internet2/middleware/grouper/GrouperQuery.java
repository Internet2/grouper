/*
 * Copyright (C) 2004-2005 University Corporation for Advanced Internet Development, Inc.
 * Copyright (C) 2004-2005 The University Of Chicago
 * All Rights Reserved. 
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 *  * Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *  * Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *  * Neither the name of the University of Chicago nor the names
 *    of its contributors nor the University Corporation for Advanced
 *   Internet Development, Inc. may be used to endorse or promote
 *   products derived from this software without explicit prior
 *   written permission.
 *
 * You are under no obligation whatsoever to provide any enhancements
 * to the University of Chicago, its contributors, or the University
 * Corporation for Advanced Internet Development, Inc.  If you choose
 * to provide your enhancements, or if you choose to otherwise publish
 * or distribute your enhancements, in source code form without
 * contemporaneously requiring end users to enter into a separate
 * written license agreement for such enhancements, then you thereby
 * grant the University of Chicago, its contributors, and the University
 * Corporation for Advanced Internet Development, Inc. a non-exclusive,
 * royalty-free, perpetual license to install, use, modify, prepare
 * derivative works, incorporate into the software or other computer
 * software, distribute, and sublicense your enhancements or derivative
 * works thereof, in binary and source code form.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND WITH ALL FAULTS.  ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING,
 * BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE, AND NON-INFRINGEMENT ARE DISCLAIMED AND the
 * entire risk of satisfactory quality, performance, accuracy, and effort
 * is with LICENSEE. IN NO EVENT SHALL THE COPYRIGHT OWNER, CONTRIBUTORS,
 * OR THE UNIVERSITY CORPORATION FOR ADVANCED INTERNET DEVELOPMENT, INC.
 * BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR
 * BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OR DISTRIBUTION OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package edu.internet2.middleware.grouper;


import  java.util.*;
import  net.sf.hibernate.*;
import  org.apache.commons.lang.builder.ToStringBuilder;


/** 
 * Class for querying the groups registry.
 * <p />
 *
 * @author  blair christensen.
 * @version $Id: GrouperQuery.java,v 1.29 2005-07-18 17:48:49 blair Exp $
 */
public class GrouperQuery {

  /*
   * PRIVATE CONSTANTS
   */
  private static final String KEY_BNS = "base";
  private static final String KEY_CA  = "createdAfter";
  private static final String KEY_CB  = "createdBefore";
  private static final String KEY_GA  = "groupAttr";
  private static final String KEY_GN  = "group";
  private static final String KEY_GT  = "groupType";
  private static final String KEY_MT  = "membershipType";
  private static final String KEY_MA  = "modifiedAfter";
  private static final String KEY_MB  = "modifiedBefore";
  private static final String KEY_SA  = "stemAttr";
  private static final String KEY_SN  = "stem";


  /*
   * PRIVATE INSTANCE VARIABLES
   */
  private GrouperSession  s;
  private Map             candidates = new HashMap();


  /*
   * CONSTRUCTORS
   */

  /**
   * Construct a new {@link GrouperQuery} object.
   */
  public GrouperQuery(GrouperSession s) {
    this.s = s;
  }


  /*
   * PUBLIC INSTANCE METHODS
   */

  /**
   * Set "base" scope filter.
   * <p />
   * Restricts results to children subordinate to the given namespace.
   * <pre>
   * GrouperQuery q = new GrouperQuery(s);
   * if (q.base(namespace)) {
   *   List results = q.getMembers();
   * }
   * </pre>
   * @param   namespace Filter results by presence within this
   *   namespace.
   * @return  True if one or more matches found.
   */
  public boolean base(String namespace) {
    boolean rv    = false;
    List    vals  = this._queryBase(namespace);
    this.candidates.put(KEY_BNS, vals);
    if (vals.size() > 0) { rv = true; }
    return rv;
  }

  /**
   * Clear all query filters.
   */
  public void clear() {
    this.candidates.clear();
  }

  /**
   * Clear the specified query filter.
   * <p />
   * @param   filter  The name of the filter to clear.
   * @return  True if filter results were cleared.
   */
  public boolean clear(String filter) {
    boolean rv = false;
    if (this.candidates.containsKey(filter)) {
      this.candidates.remove(filter);
      rv = true;
    }
    return rv;
  }

  /**
   * Set "createdAfter" group and stem creation filter.
   * <p />
   * <pre>
   * GrouperQuery q = new GrouperQuery(s);
   * if (q.createdAfter(date)) {
   *   List results = q.getMembers();
   * }
   * </pre>
   * @param   date  Filter results by groups and stems created after
   *   this date.
   * @return  True if one or more matches found.
   */
  public boolean createdAfter(Date date) {
    boolean rv    = false;
    // TODO Genericize date queries
    List    vals  = this._queryGroupCreatedAfter(date);
    this.candidates.put(KEY_CA, vals);
    if (vals.size() > 0) { rv = true; }
    return rv; 
  }

  /**
   * Set "createdBefore" group and stem creation filter.
   * <p />
   * <pre>
   * GrouperQuery q = new GrouperQuery(s);
   * if (q.createdBefore(date)) {
   *   List results = q.getMembers();
   * }
   * </pre>
   * @param   date  Filter results by groups and stems created after
   *   this date.
   * @return  True if one or more matches found.
   */
  public boolean createdBefore(Date date) {
    boolean rv    = false;
    // TODO Genericize date queries
    List    vals  = this._queryGroupCreatedBefore(date);
    this.candidates.put(KEY_CB, vals);
    if (vals.size() > 0) { rv = true; }
    return rv;
  }

  /**
   * Return group query results.
   * <p />
   * <pre>
   * GrouperQuery q = new GrouperQUery(s);
   * if (q.base(namespace) and q.createdAfter(yesterday)) {
   *   List results = q.getGroups();
   * }
   * </pre>
   * @return  List of {@link GrouperGroup} objects.
   */
  public List getGroups() {
    List      vals  = new ArrayList();
    Iterator  iter  = this.candidates.keySet().iterator();
    while (iter.hasNext()) {
      List cands = (List) this.candidates.get( iter.next() );
      cands = this.convertToGroups(cands);
      vals = this._candidateSelect(vals, cands);
      if (vals.size() == 0) { break; }
    }
    return vals;
  }

  /**
   * Return list value query results.
   * <p />
   * <pre>
   * GrouperQuery q = new GrouperQUery(s);
   * if (q.base(namespace) and q.createdAfter(yesterday)) {
   *   List results = q.getListValues();
   * }
   * </pre>
   * @return  List of {@link GrouperList} objects.
   */
  public List getListValues() {
    List      vals    = new ArrayList();
    Iterator  iter    = this.candidates.keySet().iterator();
    while (iter.hasNext()) {
      List cands = (List) this.candidates.get( iter.next() );
      cands = this.convertToListValues(cands);
      vals = this._candidateSelect(vals, cands);
      if (vals.size() == 0) { break; }
    }
    return vals;
  }

  /**
   * Return member query results.
   * <p />
   * <pre>
   * GrouperQuery q = new GrouperQUery(s);
   * if (q.base(namespace) and q.createdAfter(yesterday)) {
   *   List results = q.getMembers();
   * }
   * </pre>
   * @return  List of {@link GrouperMember} objects.
   */
  public List getMembers() {
    return this.convertListValuesToMembers( this.getListValues() );
  }

  /**
   * Return stem query results.
   * <p />
   * <pre>
   * GrouperQuery q = new GrouperQUery(s);
   * if (q.base(namespace) and q.createdAfter(yesterday)) {
   *   List results = q.getStems();
   * }
   * </pre>
   * @return  List of {@link GrouperStem} objects.
   */
  public List getStems() {
    List      vals  = new ArrayList();
    Iterator  iter  = this.candidates.keySet().iterator();
    while (iter.hasNext()) {
      List cands = (List) this.candidates.get( iter.next() );
      cands = this.convertToStems(cands);
      vals = this._candidateSelect(vals, cands);
      if (vals.size() == 0) { break; }
    }
    return vals;
  }

  /**
   * Set "group" filter.
   * <p />
   * <pre>
   * GrouperQuery q = new GrouperQuery(s);
   * if (q.group(name)) {
   *   List results = q.getMembers();
   * }
   * </pre>
   * @param   namespace Filter results by groups with matching name,
   *   displayName or displayExtension attributes.
   * @return  True if one or more matches found.
   */
  public boolean group(String name) {
    boolean rv    = false;
    List    vals  = this._queryName(name, GrouperGroup.class);
    this.candidates.put(KEY_GN, vals);
    if (vals.size() > 0) { rv = true; }
    return rv;
  }

  /**
   * Set "groupAttr" filter.
   * <p/>
   * <pre>
   * GrouperQuery q = new GrouperQuery(s);
   * if (g.groupAttr("description", "this is a generic description")) {
   *   List results = q.getGroups();
   * }
   * </pre>
   * @param   attribute Name of attribute to query on.
   * @param   value     Value of attribute to query on.
   * @return  True if one or more matches found.
   */
  public boolean groupAttr(String attribute, String value) {
    boolean rv    = false;
    List    vals  = this._queryAttribute(attribute, value, GrouperGroup.class);
    this.candidates.put(KEY_GA, vals);
    if (vals.size() > 0) { rv = true; }
    return rv;
  }

  /**
   * Set "groupType" filter.
   * <p />
   * <pre>
   * GrouperQuery q = new GrouperQuery(s);
   * if (q.groupType(type)) {
   *   List results = q.getMembers();
   * }
   * </pre>
   * @param   type  Filter results by groups of this type.
   * @return  True if one or more matches found.
   */
  public boolean groupType(String type) {
    // TODO How do I unset?  `null'?
    boolean rv    = false;
    List    vals  = this._queryGroupType(type);
    this.candidates.put(KEY_GT, vals);
    if (vals.size() > 0) { rv = true; }
    return rv;
  }

  /**
   * Set "membershipType" filter.
   * <p/>
   * <pre>
   * GrouperQuery q = new GrouperQuery(s);
   * if (q.membershipType(Grouper.MEM_ALL)) {
   *   List results = q.getMembers();
   * }
   * </pre>
   * @param   type  Type of membership to query on.  Valid options are
   *   <i>Grouper.MEM_ALL</i>, <i>Grouper.MEM_EFF</i>, and
   *    <i>Grouper.MEM_IMM</i>.
   * @return  True if one or more matches found.
   */
  public boolean membershipType(String type) {
    boolean rv    = false;
    List    vals  = new ArrayList();
    if        (type.equals(Grouper.MEM_ALL)) {
      // Query for both effective + immediate memberships
      vals = this._queryMembershipType(
        "GrouperList.by.list"
      );
    } else if (type.equals(Grouper.MEM_EFF)) {
      // Query for effective memberships
      vals = this._queryMembershipType(
        "GrouperList.by.list.and.is.eff"
      );
    } else if (type.equals(Grouper.MEM_IMM)) {
      // Query for immediate memberships
      vals = this._queryMembershipType(
        "GrouperList.by.list.and.is.imm"
      );
    } 
    this.candidates.put(KEY_MT, vals);
    if (vals.size() > 0) { rv = true; }
    return rv; 
  }

  /**
   * Set "modifiedAfter" group and stem creation filter.
   * <p />
   * <pre>
   * GrouperQuery q = new GrouperQuery(s);
   * if (q.modifiedAfter(date)) {
   *   List results = q.getMembers();
   * }
   * </pre>
   * @param   date  Filter results by groups and stems modified after
   *   this date.
   * @return  True if one or more matches found.
   */
  public boolean modifiedAfter(Date date) {
    boolean rv    = false;
    // TODO Genericize date queries
    List    vals  = this._queryGroupModifiedAfter(date);
    this.candidates.put(KEY_MA, vals);
    if (vals.size() > 0) { rv = true; }
    return rv;
  }

  /**
   * Set "modifiedBefore" group and stem creation filter.
   * <p />
   * <pre>
   * GrouperQuery q = new GrouperQuery(s);
   * if (q.modifiedBefore(date)) {
   *   List results = q.getMembers();
   * }
   * </pre>
   * @param   date  Filter results by groups and stems modified before
   *   this date.
   * @return  True if one or more matches found.
   */
  public boolean modifiedBefore(Date date) {
    boolean rv    = false;
    // TODO Genericize date queries
    List    vals  = this._queryGroupModifiedBefore(date);
    this.candidates.put(KEY_MB, vals);
    if (vals.size() > 0) { rv = true; }
    return rv; 
  }

  /**
   * Set "stem" filter.
   * <p />
   * <pre>
   * GrouperQuery q = new GrouperQuery(s);
   * if (q.stem(name)) {
   *   List results = q.getMembers();
   * }
   * </pre>
   * @param   name  Filter results by stems with matching name,
   *   displayName or displayExtension attributes.
   * @return  True if one or more matches found.
   */
  public boolean stem(String name) {
    boolean rv    = false;
    List    vals  = this._queryName(name, GrouperStem.class);
    this.candidates.put(KEY_SN, vals);
    if (vals.size() > 0) { rv = true; }
    return rv;
  }

  /**
   * Set "stemAttr" filter.
   * <p/>
   * <pre>
   * GrouperQuery q = new GrouperQuery(s);
   * if (g.stemAttr("description", "this is a generic description")) {
   *   List results = q.getStems();
   * }
   * </pre>
   * @param   attribute Name of attribute to query on.
   * @param   value     Value of attribute to query on.
   * @return  True if one or more matches found.
   */
  public boolean stemAttr(String attribute, String value) {
    boolean rv    = false;
    List    vals  = this._queryAttribute(attribute, value, GrouperStem.class);
    this.candidates.put(KEY_SA, vals);
    if (vals.size() > 0) { rv = true; }
    return rv;
  }

  /**
   * Return a string representation of this object.
   * <p />
   * @return String representation of this object.
   */
  public String toString() {
    return new ToStringBuilder(this).toString();
  }


  /*
   * PRIVATE INSTANCE METHODS
   */ 
 
  private List convertToGroups(List candidates) {
    List      vals  = new ArrayList();
    Iterator  iter  = candidates.iterator();
    while (iter.hasNext()) {
      Object obj = (Object) iter.next();
      if (obj instanceof GrouperGroup) {
        vals.add( (GrouperGroup) obj );
      } else if (obj instanceof GrouperList) {
        vals.addAll(
          this.convertListValueToGroup( (GrouperList) obj) 
        );
      } else if (obj instanceof GrouperStem) {
        // Skip; no conversion
      } else {
        throw new RuntimeException(
          "Unknown conversion: " + obj.getClass().getName() + 
          " to " + GrouperGroup.class.getName()
        );
      }
    }
    return vals;
  }

  private List convertToListValues(List candidates) {
    List      vals  = new ArrayList();
    Iterator  iter  = candidates.iterator();
    while (iter.hasNext()) {
      Object obj = (Object) iter.next();
      if (obj instanceof GrouperList) {
        vals.add( (GrouperList) obj );
      } else if (obj instanceof Group) {
        vals.addAll( 
          this.convertGroupToListValues( (Group) obj )
        );
      } else {
        throw new RuntimeException(
          "Unknown conversion: " + obj.getClass().getName() + 
          " to " + GrouperList.class.getName()
        );
      }
    }
    return vals;
  }

  private List convertToStems(List candidates) {
    List      vals  = new ArrayList();
    Iterator  iter  = candidates.iterator();
    while (iter.hasNext()) {
      Object obj = (Object) iter.next();
      if (obj instanceof GrouperStem) {
        vals.add( (GrouperStem) obj );
      } else if (obj instanceof GrouperGroup) {
        // Skip; no conversion
      } else if (obj instanceof GrouperList) {
        vals.addAll(
          this.convertListValueToStem( (GrouperList) obj) 
        );
      } else {
        throw new RuntimeException(
          "Unknown conversion: " + obj.getClass().getName() + 
          " to " + GrouperStem.class.getName()
        );
      }
    }
    return vals;
  }

  private List convertGroupToListValues(Group g) {
    String  qry   = "GrouperList.by.group";
    List    vals  = new ArrayList();
    try {
      Query q = s.dbSess().session().getNamedQuery(qry);
      q.setString(0, g.key());
      try {
        Iterator iter = q.list().iterator();
        while (iter.hasNext()) {
          // Make the returned items into proper objects
          GrouperList gl = (GrouperList) iter.next();
          gl.setSession(s);
          vals.add(gl);
        }
      } catch (HibernateException e) {
        throw new RuntimeException(
          "Error retrieving results for " + qry + ": " + e.getMessage()
        );
      }
    } catch (HibernateException e) {
      throw new RuntimeException(
        "Unable to get query " + qry + ": " + e.getMessage()
      );
    }
    return vals;
  }

  private List convertListValueToGroup(GrouperList lv) {
    List vals = new ArrayList();
    if (!lv.group().type().equals(Grouper.NS_TYPE)) {
      vals.add( (GrouperGroup) lv.group() );
    }
    return vals;
  }

  private List convertListValueToStem(GrouperList lv) {
    List vals = new ArrayList();
    if (lv.group().type().equals(Grouper.NS_TYPE)) {
      vals.add( (GrouperStem) lv.group() );
    }
    return vals;
  }

  private List convertListValuesToMembers(List listVals) {
    Set       vals  = new HashSet();
    Iterator  iter  = listVals.iterator();
    while (iter.hasNext()) {
      GrouperList lv = (GrouperList) iter.next();
      if (lv.groupField().equals(Grouper.DEF_LIST_TYPE)) {
        vals.add( lv.member() );
      }
    }
    return new ArrayList(vals);
  }

  private List _queryAttribute(String name, String value, Class klass) {
    String  qry   = "Group.as.key.by.attribute.fuzzy";
    List    vals  = new ArrayList();
    try {
      Query q = s.dbSess().session().getNamedQuery(qry);
      q.setString(0, name); // attribute
      // TODO Move _%_ to _Grouper.hbm.xml_
      q.setString(1, "%" + value + "%"); // value
      try {
        Iterator iter = q.list().iterator();
        while (iter.hasNext()) {
          String key = (String) iter.next();
          try {
            Group g = Group.loadByKey(this.s, key);
            // TODO Why can't I query on the _classType_ field above?
            if (g.getClass().equals(klass)) {
              vals.add(g);
            }
          } catch (InsufficientPrivilegeException e) {
            // Ignore
          }
        }
      } catch (HibernateException e) {
        throw new RuntimeException(
          "Error retrieving results for " + qry + ": " + e.getMessage()
        );
      }
    } catch (HibernateException e) {
      throw new RuntimeException(
        "Unable to get query " + qry + ": " + e.getMessage()
      );
    }
    return vals;
  }

  private List _queryBase(String namespace) {
    String  qry   = "Group.key.all.children";
    List    vals  = new ArrayList();
    try {
      Query q = this.s.dbSess().session().getNamedQuery(qry);
      q.setString(0, namespace);
      // TODO Move _%_ to _Grouper.hbm.xml_
      q.setString(1, namespace + ":%");
      try {
        Iterator iter = q.list().iterator();
        while (iter.hasNext()) {
          try {
            String key = (String) iter.next();
            Group g = Group.loadByKey(this.s, key);
            vals.add(g);
          } catch (InsufficientPrivilegeException e) {
            // Ignore
          }
        }
      } catch (HibernateException e) {
        throw new RuntimeException(
          "Error retrieving results for " + qry + ": " + e.getMessage()
        );
      }
    } catch (HibernateException e) {
      throw new RuntimeException(
        "Unable to get query " + qry + ": " + e.getMessage()
      );
    }
    return vals;
  }

  private List _queryGroupCreatedAfter(java.util.Date d) {
    String  qry   = "Group.by.created.after";
    List    vals  = new ArrayList();
    try {
      Query q = this.s.dbSess().session().getNamedQuery(qry);
      q.setString(0, Long.toString(d.getTime()));
      try {
        // TODO Is this necessary?  Or even accurate?
        Iterator iter = q.list().iterator();
        while (iter.hasNext()) {
          try {
            Group g = (Group) iter.next();
            g = Group.loadByKey(this.s, g.key());
            vals.add(g);
          } catch (InsufficientPrivilegeException e) {
            // Ignore
          }
        }
      } catch (HibernateException e) {
        throw new RuntimeException(
          "Error retrieving results for " + qry + ": " + e.getMessage()
        );
      }
    } catch (HibernateException e) {
      throw new RuntimeException(
        "Unable to get query " + qry + ": " + e.getMessage()
      );
    }
    return vals;
  }

  private List _queryGroupCreatedBefore(java.util.Date d) {
    String  qry   = "Group.by.created.before";
    List    vals  = new ArrayList();
    try {
      Query q = this.s.dbSess().session().getNamedQuery(qry);
      q.setString(0, Long.toString(d.getTime()));
      try {
        // TODO Is this necessary?  Or even accurate?
        Iterator iter = q.list().iterator();
        while (iter.hasNext()) {
          try {
            Group g = (Group) iter.next();
            g = Group.loadByKey(this.s, g.key());
            vals.add(g);
          } catch (InsufficientPrivilegeException e) {
            // Ignore
          }
        }
      } catch (HibernateException e) {
        throw new RuntimeException(
          "Error retrieving results for " + qry + ": " + e.getMessage()
        );
      }
    } catch (HibernateException e) {
      throw new RuntimeException(
        "Unable to get query " + qry + ": " + e.getMessage()
      );
    }
    return vals;
  }

  private List _queryGroupModifiedAfter(java.util.Date d) {
    String  qry   = "Group.by.modified.after";
    List    vals  = new ArrayList();
    try {
      Query q = this.s.dbSess().session().getNamedQuery(qry);
      q.setString(0, Long.toString(d.getTime()));
      try {
        // TODO Is this necessary?  Or even accurate?
        Iterator iter = q.list().iterator();
        while (iter.hasNext()) {
          try {
            Group g = (Group) iter.next();
            g = Group.loadByKey(this.s, g.key());
            vals.add(g);
          } catch (InsufficientPrivilegeException e) {
            // Ignore
          }
        }
      } catch (HibernateException e) {
        throw new RuntimeException(
          "Error retrieving results for " + qry + ": " + e.getMessage()
        );
      }
    } catch (HibernateException e) {
      throw new RuntimeException(
        "Unable to get query " + qry + ": " + e.getMessage()
      );
    }
    return vals;
  }

  private List _queryGroupModifiedBefore(java.util.Date d) {
    String  qry   = "Group.by.modified.before";
    List    vals  = new ArrayList();
    try {
      Query q = s.dbSess().session().getNamedQuery(qry);
      q.setString(0, Long.toString(d.getTime()));
      try {
        // TODO Is this necessary?  Or even accurate?
        Iterator iter = q.list().iterator();
        while (iter.hasNext()) {
          try {
            Group g = (Group) iter.next();
            g = Group.loadByKey(this.s, g.key());
            vals.add(g);
          } catch (InsufficientPrivilegeException e) {
            // Ignore
          }
        }
      } catch (HibernateException e) {
        throw new RuntimeException(
          "Error retrieving results for " + qry + ": " + e.getMessage()
        );
      }
    } catch (HibernateException e) {
      throw new RuntimeException(
        "Unable to get query " + qry + ": " + e.getMessage()
      );
    }
    return vals;
  }

  private List _queryGroupType(String type) {
    String  qry   = "GrouperSchema.by.type";
    List    vals  = new ArrayList();
    try {
      Query q = this.s.dbSess().session().getNamedQuery(qry);
      q.setString(0, type);
      try {
        Iterator iter = q.list().iterator();
        while (iter.hasNext()) {
          GrouperSchema gs = (GrouperSchema) iter.next();
          try {
            Group g = Group.loadByKey(this.s, gs.key());
            vals.add(g);
          } catch (InsufficientPrivilegeException e) {
            // Ignore
          }
        }
      } catch (HibernateException e) {
        throw new RuntimeException(
          "Error getting results for " + qry + ": " + e.getMessage()
        );
      }
    } catch (HibernateException e) {
      throw new RuntimeException(
        "Unable to get query " + qry + ": " + e.getMessage()
      );
    }
    return vals;
  }

  private List _queryMembershipType(String qry) {
    List vals  = new ArrayList();
    try {
      Query q = this.s.dbSess().session().getNamedQuery(qry);
      q.setString(0, Grouper.DEF_LIST_TYPE);
      try {
        // TODO Argh!
        Iterator iter = q.list().iterator();
        while (iter.hasNext()) {
          // Make the returned items into proper objects
          GrouperList gl = (GrouperList) iter.next();
          gl.setSession(this.s);
          vals.add(gl);
        }
      } catch (HibernateException e) {
        throw new RuntimeException(
          "Error retrieving results for " + qry + ": " + e.getMessage()
        );
      }
    } catch (HibernateException e) {
      throw new RuntimeException(
        "Unable to get query " + qry + ": " + e.getMessage()
      );
    }
    return vals;
  }

  private List _queryMembershipTypeAll(String list) {
    String  qry   = "GrouperList.by.list";
    List    vals  = new ArrayList();
    try {
      Query q = this.s.dbSess().session().getNamedQuery(qry);
      q.setString(0, list);
      try {
        // TODO Argh!
        Iterator iter = q.list().iterator();
        while (iter.hasNext()) {
          // Make the returned items into proper objects
          GrouperList gl = (GrouperList) iter.next();
          gl.setSession(this.s);
          vals.add(gl);
        }
      } catch (HibernateException e) {
        throw new RuntimeException(
          "Error retrieving results for " + qry + ": " + e.getMessage()
        );
      }
    } catch (HibernateException e) {
      throw new RuntimeException(
        "Unable to get query " + qry + ": " + e.getMessage()
      );
    }
    return vals;
  }

  private List _queryMembershipTypeEff(String list) {
    String  qry   = "GrouperList.by.list.and.is.eff";
    List    vals  = new ArrayList();
    try {
      Query q = this.s.dbSess().session().getNamedQuery(qry);
      q.setString(0, list);
      try {
        // TODO Argh!
        Iterator iter = q.list().iterator();
        while (iter.hasNext()) {
          // Make the returned items into proper objects
          GrouperList gl = (GrouperList) iter.next();
          gl.setSession(this.s);
          vals.add(gl);
        }
      } catch (HibernateException e) {
        throw new RuntimeException(
          "Error retrieving results for " + qry + ": " + e.getMessage()
        );
      }
    } catch (HibernateException e) {
      throw new RuntimeException(
        "Unable to get query " + qry + ": " + e.getMessage()
      );
    }
    return vals;
  }

  private List _queryMembershipTypeImm(String list) {
    String  qry   = "GrouperList.by.list.and.is.imm";
    List    vals  = new ArrayList();
    try {
      Query q = this.s.dbSess().session().getNamedQuery(qry);  
      q.setString(0, list);
      try {
        // TODO Argh!
        Iterator iter = q.list().iterator();
        while (iter.hasNext()) {
          // Make the returned items into proper objects
          GrouperList gl = (GrouperList) iter.next();
          gl.setSession(this.s);
          vals.add(gl);
        }
      } catch (HibernateException e) {
        throw new RuntimeException(
          "Error retrieving results for " + qry + ": " + e.getMessage()
        );
      }
    } catch (HibernateException e) {
      throw new RuntimeException(
        "Unable to get query " + qry + ": " + e.getMessage()
      );
    }
    return vals;
  }

  private List _queryName(String name, Class klass) {
    String  qry   = "Group.by.name.fuzzy";
    List    vals  = new ArrayList();
    try {
      Query q = s.dbSess().session().getNamedQuery(qry);
      // TODO Move _%_ to _Grouper.hbm.xml_
      q.setString(0, "%" + name + "%"); // name
      // TODO Move _%_ to _Grouper.hbm.xml_
      q.setString(1, "%" + name + "%"); // displayName
      // TODO Move _%_ to _Grouper.hbm.xml_
      q.setString(2, "%" + name + "%"); // displayExtension
      try {
        Iterator iter = q.list().iterator();
        while (iter.hasNext()) {
          String key = (String) iter.next();
          try {
            Group g = Group.loadByKey(this.s, key);
            // TODO Why can't I query on the _classType_ field above?
            if (g.getClass().equals(klass)) {
              vals.add(g);
            }
          } catch (InsufficientPrivilegeException e) {
            // Ignore
          }
        }
      } catch (HibernateException e) {
        throw new RuntimeException(
          "Error retrieving results for " + qry + ": " + e.getMessage()
        );
      }
    } catch (HibernateException e) {
      throw new RuntimeException(
        "Unable to get query " + qry + ": " + e.getMessage()
      );
    }
    return vals;
  }

  private List _candidateSelect(List vals, List candidates) {
    List      selected  = new ArrayList();
    Iterator  iter      = candidates.iterator();
    if (candidates.size() > 0) {
      while (iter.hasNext()) {
        // TODO Will this cause casting problems?
        Object obj = iter.next();
        if        (vals.size() == 0)  {
          selected.add(obj);
        } else if (vals.size() > 0)   {
          if (vals.contains(obj)) {
            selected.add(obj);
          }
        }
      }
    }
    return selected;
  }
}

