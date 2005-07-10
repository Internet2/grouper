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
 * @version $Id: GrouperQuery.java,v 1.26 2005-07-10 20:19:28 blair Exp $
 */
public class GrouperQuery {

  /*
   * PRIVATE CONSTANTS
   */
  private static final String KEY_BNS = "base";
  private static final String KEY_CA  = "createdAfter";
  private static final String KEY_CB  = "createdBefore";
  private static final String KEY_GN  = "group";
  private static final String KEY_GT  = "groupType";
  private static final String KEY_MT  = "membershipType";
  private static final String KEY_MA  = "modifiedAfter";
  private static final String KEY_MB  = "modifiedBefore";
  private static final String KEY_NSN = "namespace";


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
   * if (
   *     q.createdAfter(yesterday) &&
   *     q.base(namespace)
   *    )
   * {
   *   List results = q.query();
   * }
   * </pre>
   * @param   namespace Filter results by presence within this
   *   namespace.
   * @return  True if one or more matches found.
   * @throws  {@link GrouperException} - but why?
   */
  public boolean base(String namespace) throws GrouperException {
    boolean rv    = false;
    List    vals  = new ArrayList();
    this.candidates.remove(KEY_BNS);
    // Find all groups fuzzily matching this name
    vals = GrouperQuery._iterGroup(
      this.s, this._queryBase(namespace)
    );
    if ( (vals != null) && (vals.size() > 0) ) {
      rv = true;
    }
    this.candidates.put(KEY_BNS, vals);
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
   * Set group <i>createTime</i> filter.
   * <p />
   *
   * @param   date  Query for groups created after this {@link Date}.
   * @return  True if one or more matches found.
   */
  public boolean createdAfter(Date date) throws GrouperException {
    boolean rv    = false;
    List    vals  = new ArrayList();
    this.candidates.remove(KEY_CA);
    // Find all groups created after this date
    vals = GrouperQuery._iterGroup(this.s, this._groupCreatedAfter(date));
    if ( (vals != null) && (vals.size() > 0) ) {
      rv = true;
    }
    this.candidates.put(KEY_CA, vals);
    return rv; 
  }

  /**
   * Set group <i>createTime</i> filter.
   * <p />
   *
   * @param   date  Query for groups created before this {@link Date}.
   * @return  True if one or more matches found.
   */
  public boolean createdBefore(Date date) throws GrouperException {
    boolean rv    = false;
    List    vals  = new ArrayList();
    this.candidates.remove(KEY_CB);
    // Find all groups created before this date
    vals = GrouperQuery._iterGroup(this.s, this._groupCreatedBefore(date));
    if ( (vals != null) && (vals.size() > 0) ) {
      rv = true;
    }
    this.candidates.put(KEY_CB, vals);
    return rv; 
  }

  /**
   * Set "group" name filter.
   * <p />
   * <pre>
   * GrouperQuery q = new GrouperQuery(s);
   * if (q.group(name)) {
   *   List results = q.query();
   * }
   * </pre>
   * @param   name  String to match against <i>name</i>,
   *   <i>extension</i> and <i>displayName</i>.
   * @return  True if one or more matches found.
   * @throws  {@link GrouperException} - but why?
   */
  public boolean group(String name) throws GrouperException {
    boolean rv    = false;
    List    vals  = new ArrayList();
    this.candidates.remove(KEY_GN);
    // Find all groups fuzzily matching this name
    vals = GrouperQuery._iterGroup(
      this.s, this._queryName(name, GrouperGroup.class)
    );
    if ( (vals != null) && (vals.size() > 0) ) {
      rv = true;
    }
    this.candidates.put(KEY_GN, vals);
    return rv;
  }

  /**
   * Set group <i>groupType</i> filter.
   * <p />
   * @param   type  Type of {@link Group} to query on.
   * @return  True if one or more matches found.
   */
  public boolean groupType(String type) throws GrouperException {
    // TODO How do I unset?  `null'?
    boolean rv    = false;
    List    vals  = new ArrayList();

    this.candidates.remove(KEY_GT);
    // Find all groups of matching type
    List      groups  = this._queryByGroupType(type);
    // Find all list values for matching groups
    Iterator  iter    = groups.iterator();
    while (iter.hasNext()) {
      Group g = (Group) iter.next();
      // FIXME Wlll g.s be defined?
      Iterator lvIter = g.listVals(Grouper.DEF_LIST_TYPE).iterator();
      while (lvIter.hasNext()) {
        GrouperList gl = (GrouperList) lvIter.next();
        gl.load(this.s);
        vals.add(gl);
      }
    }
    if ( (vals != null) && (vals.size() > 0) ) {
      rv = true;
    }
    this.candidates.put(KEY_GT, vals);

    return rv; 
  }

  /**
   * Set <i>membershipType</i> query filter.
   * <p />
   * @param   type  Type of membership to query on.  Valid options are
   *   <i>Grouper.MEM_ALL</i>, <i>Grouper.MEM_EFF</i>, and
  *    <i>Grouper.MEM_IMM</i>.
   * @return  True if one or more matches found.
   */
  public boolean membershipType(String type) throws GrouperException {
    boolean rv    = false;
    List    vals  = new ArrayList();
    this.candidates.remove(KEY_MT);
    if        (type.equals(Grouper.MEM_ALL)) {
      // Query for both effective + immediate memberships
      vals = this._queryMembershipTypeAll(Grouper.DEF_LIST_TYPE);
    } else if (type.equals(Grouper.MEM_EFF)) {
      // Query for effective memberships
      vals = this._queryMembershipTypeEff(Grouper.DEF_LIST_TYPE);
    } else if (type.equals(Grouper.MEM_IMM)) {
      // Query for immediate memberships
      vals = this._queryMembershipTypeImm(Grouper.DEF_LIST_TYPE);
    } else {
      throw new GrouperException("Unknown membership type: " + type);
    } 
    if ( (vals != null) && (vals.size() > 0) ) {
      rv = true;
    }
    this.candidates.put(KEY_MT, vals);
    return rv; 
  }

  /**
   * Set group <i>modifyTime</i> filter.
   * <p />
   *
   * @param   date  Query for groups modified after this {@link Date}.
   * @return  True if one or more matches found.
   */
  public boolean modifiedAfter(Date date) throws GrouperException {
    boolean rv    = false;
    List    vals  = new ArrayList();
    this.candidates.remove(KEY_MA);
    // Find all groups modified after this date
    vals = GrouperQuery._iterGroup(this.s, this._groupModifiedAfter(date));
    if ( (vals != null) && (vals.size() > 0) ) {
      rv = true;
    }
    this.candidates.put(KEY_MA, vals);
    return rv; 
  }

  /**
   * Set group <i>modifyTime</i> filter.
   * <p />
   *
   * @param   date  Query for groups modifed before this {@link Date}.
   * @return  True if one or more matches found.
   */
  public boolean modifiedBefore(Date date) throws GrouperException {
    boolean rv    = false;
    List    vals  = new ArrayList();
    this.candidates.remove(KEY_MB);
    // Find all groups modified before this date
    vals = GrouperQuery._iterGroup(this.s, this._groupModifiedBefore(date));
    if ( (vals != null) && (vals.size() > 0) ) {
      rv = true;
    }
    this.candidates.put(KEY_MB, vals);
    return rv; 
  }

  /**
   * Set "namespace" name filter.
   * <p />
   * <pre>
   * GrouperQuery q = new GrouperQuery(s);
   * if (q.stem(name)) {
   *   List results = q.query();
   * }
   * </pre>
   * @param   name  String to match against <i>name</i>,
   *   <i>extension</i> and <i>displayName</i>.
   * @return  True if one or more matches found.
   * @throws  {@link GrouperException} - but why?
   */
  public boolean namespace(String name) throws GrouperException {
    boolean rv    = false;
    List    vals  = new ArrayList();
    this.candidates.remove(KEY_NSN);
    // Find all groups fuzzily matching this name
    vals = GrouperQuery._iterGroup(
      this.s, this._queryName(name, GrouperStem.class)
    );
    if ( (vals != null) && (vals.size() > 0) ) {
      rv = true;
    }
    this.candidates.put(KEY_NSN, vals);
    return rv;
  }

  /**
   * Retrieve query filter results.
   * <p />
   * @return  List of {@link GrouperList} objects.
   */
  public List query() {
    // TODO I suspect this approach may need optimizing
    List    vals  = new ArrayList();
    /*
     * TODO Ideally I would sort the candidate lists by size in an
     *      attempt to optimize the candidate selection.  Or I would
     *      just replace this with something entirely better, no?
     */
    Iterator  iter = this.candidates.keySet().iterator();
    while (iter.hasNext()) {
      List cands = (List) this.candidates.get( iter.next() );
      vals = this._candidateSelect(vals, cands);       
      // We have already failed to find anything
      if (vals.size() == 0) {
        break;
      }
    }
    // TODO Filter candidates through privilege interfaces
    return vals;
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

  /* (!javadoc)
   * Perform query candidate selection.
   */
  private List _candidateSelect(List vals, List candidates) {
    List      selected  = new ArrayList();
    Iterator  iter      = candidates.iterator();
    if (candidates.size() > 0) {
      while (iter.hasNext()) {
        /*
         * TODO This assumes I'll only be dealing with GrouperList
         *      objects.  Is that a safe assumption?
         */
        GrouperList gl = (GrouperList) iter.next();
        if        (vals.size() == 0)  {
          selected.add(gl);
        } else if (vals.size() > 0)   {
          if (vals.contains(gl)) {
            selected.add(gl);
          }
        }
      }
    }
    return selected;
  }

  /*
   * @return List of groups created after a specified date.
   */
  private List _groupCreatedAfter(java.util.Date d) {
    String  qry   = "Group.by.created.after";
    List    vals  = new ArrayList();
    try {
      Query q = this.s.dbSess().session().getNamedQuery(qry);
      q.setString(0, Long.toString(d.getTime()));
      try {
        // TODO Is this necessary?  Or even accurate?
        Iterator iter = q.list().iterator();
        while (iter.hasNext()) {
          Group g = (Group) iter.next();
          g = Group.loadByKey(this.s, g.key());
          vals.add(g);
        }
      } catch (HibernateException e) {
        throw new RuntimeException(
                    "Error retrieving results for " + qry + ": " + e
                  );
      }
    } catch (HibernateException e) {
      throw new RuntimeException(
                  "Unable to get query " + qry + ": " + e
                );
    }
    return vals;
  }

  /*
   * @return List of groups created before a specified date.
   */
  private List _groupCreatedBefore(java.util.Date d) {
    String  qry   = "Group.by.created.before";
    List    vals  = new ArrayList();
    try {
      Query q = this.s.dbSess().session().getNamedQuery(qry);
      q.setString(0, Long.toString(d.getTime()));
      try {
        // TODO Is this necessary?  Or even accurate?
        Iterator iter = q.list().iterator();
        while (iter.hasNext()) {
          Group g = (Group) iter.next();
          g = Group.loadByKey(this.s, g.key());
          vals.add(g);
        }
      } catch (HibernateException e) {
        throw new RuntimeException(
                    "Error retrieving results for " + qry + ": " + e
                  );
      }
    } catch (HibernateException e) {
      throw new RuntimeException(
                  "Unable to get query " + qry + ": " + e
                );
    }
    return vals;
  }

  /*
   * @return List of groups modified after a specified date.
   */
  private List _groupModifiedAfter(java.util.Date d) {
    String  qry   = "Group.by.modified.after";
    List    vals  = new ArrayList();
    try {
      Query q = this.s.dbSess().session().getNamedQuery(qry);
      q.setString(0, Long.toString(d.getTime()));
      try {
        // TODO Is this necessary?  Or even accurate?
        Iterator iter = q.list().iterator();
        while (iter.hasNext()) {
          Group g = (Group) iter.next();
          g = Group.loadByKey(this.s, g.key());
          vals.add(g);
        }
      } catch (HibernateException e) {
        throw new RuntimeException(
                    "Error retrieving results for " + qry + ": " + e
                  );
      }
    } catch (HibernateException e) {
      throw new RuntimeException(
                  "Unable to get query " + qry + ": " + e
                );
    }
    return vals;
  }

  /*
   * @return List of groups modified before a specified date.
   */
  private List _groupModifiedBefore(java.util.Date d) {
    String  qry   = "Group.by.modified.before";
    List    vals  = new ArrayList();
    try {
      Query q = s.dbSess().session().getNamedQuery(qry);
      q.setString(0, Long.toString(d.getTime()));
      try {
        // TODO Is this necessary?  Or even accurate?
        Iterator iter = q.list().iterator();
        while (iter.hasNext()) {
          Group g = (Group) iter.next();
          g = Group.loadByKey(this.s, g.key());
          vals.add(g);
        }
      } catch (HibernateException e) {
        throw new RuntimeException(
                    "Error retrieving results for " + qry + ": " + e
                  );
      }
    } catch (HibernateException e) {
      throw new RuntimeException(
                  "Unable to get query " + qry + ": " + e
                );
    }
    return vals;
  }

  // TODO Bleh
  private List _queryByGroupType(String type) {
    String  qry   = "GrouperSchema.by.type";
    List    vals  = new ArrayList();
    try {
      Query q = this.s.dbSess().session().getNamedQuery(qry);
      q.setString(0, type);
      try {
        Iterator iter = q.list().iterator();
        while (iter.hasNext()) {
          GrouperSchema gs = (GrouperSchema) iter.next();
          // TODO What a hack
          Group g = Group.loadByKey(this.s, gs.key());
          if (g != null) {
            vals.add(g);
          }
        }
      } catch (HibernateException e) {
        throw new RuntimeException(
                    "Error getting results for " + qry + ": " + e
                  );
      }
    } catch (HibernateException e) {
      throw new RuntimeException(
                  "Unable to get query " + qry + ": " + e
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
          gl.load(this.s);
          vals.add(gl);
        }
      } catch (HibernateException e) {
        throw new RuntimeException(
                    "Error retrieving results for " + qry + ": " + e
                  );
      }
    } catch (HibernateException e) {
      throw new RuntimeException(
                  "Unable to get query " + qry + ": " + e
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
          gl.load(this.s);
          vals.add(gl);
        }
      } catch (HibernateException e) {
        throw new RuntimeException(
                    "Error retrieving results for " + qry + ": " + e
                  );
      }
    } catch (HibernateException e) {
      throw new RuntimeException(
                  "Unable to get query " + qry + ": " + e
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
          gl.load(this.s);
          vals.add(gl);
        }
      } catch (HibernateException e) {
        throw new RuntimeException(
                    "Error retrieving results for " + qry + ": " + e
                  );
      }
    } catch (HibernateException e) {
      throw new RuntimeException(
                  "Unable to get query " + qry + ": " + e
                );
    }
    return vals;
  }
  
  /*
   * @return List of children of a given namespace
   */
  private List _queryBase(String namespace) {
    String  qry   = "Group.key.all.children";
    List    vals  = new ArrayList();
    try {
      Query q = s.dbSess().session().getNamedQuery(qry);
      q.setString(0, namespace);
      // TODO Move _%_ to _Grouper.hbm.xml_
      q.setString(1, namespace + ":%");
      try {
        Iterator iter = q.list().iterator();
        while (iter.hasNext()) {
          String key = (String) iter.next();
          Group g = Group.loadByKey(this.s, key);
          vals.add(g);
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

  /*
   * @return List of groups or stems fuzzily matching the given name.
   */
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
          Group g = Group.loadByKey(this.s, key);
          // TODO Why can't I query on the _classType_ field above?
          if (g.getClass().equals(klass)) {
            vals.add(g);
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

  /* (!javadoc)
   * Iterate through a list of groups, find list values for group, and
   * add list values to a List that will be returned.
   */
  private static List _iterGroup(GrouperSession s, List groups) {
    List vals = new ArrayList();

    if (groups != null) {
      Iterator iter = groups.iterator();
      while (iter.hasNext()) {
        Group g = (Group) iter.next();
        // FIXME Wlll g.s be defined?
        Iterator lvIter = g.listVals(Grouper.DEF_LIST_TYPE).iterator();
        while (lvIter.hasNext()) {
          GrouperList gl = (GrouperList) lvIter.next();
          if (gl != null) {
            gl.load(s);
            vals.add(gl);
          }
        }
      }
    }
    return vals;
  }

}

