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
 * @version $Id: GrouperQuery.java,v 1.21 2005-03-23 22:29:50 blair Exp $
 */
public class GrouperQuery {

  /*
   * PRIVATE CONSTANTS
   */
  private static final String KEY_CA = "createdAfter";
  private static final String KEY_CB = "createdBefore";
  private static final String KEY_GT = "groupType";
  private static final String KEY_MT = "membershipType";
  private static final String KEY_MA = "modifiedAfter";
  private static final String KEY_MB = "modifiedBefore";


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
    return this._queryCreatedAfter(date);
  }

  /**
   * Set group <i>createTime</i> filter.
   * <p />
   *
   * @param   date  Query for groups created before this {@link Date}.
   * @return  True if one or more matches found.
   */
  public boolean createdBefore(Date date) throws GrouperException {
    return this._queryCreatedBefore(date);
  }

  /**
   * Set group <i>groupType</i> filter.
   * <p />
   * @param   type  Type of {@link GrouperGroup} to query on.
   * @return  True if one or more matches found.
   */
  public boolean groupType(String type) throws GrouperException {
    // TODO How do I unset?  `null'?
    return this._queryGroupType(type);
  }

  /**
   * Set <i>membershipType</i> query filter.
   * <p />
   *
   * @param   type  Type of membership to query on.  Valid options are
   *   <i>Grouper.MEM_ALL</i>, <i>Grouper.MEM_EFF</i>, and
  *    <i>Grouper.MEM_IMM</i>.
   * @return  True if one or more matches found.
   */
  public boolean membershipType(String type) throws GrouperException {
    return this._queryMembershipType(type);
  }

  /**
   * Set group <i>modifyTime</i> filter.
   * <p />
   *
   * @param   date  Query for groups modified after this {@link Date}.
   * @return  True if one or more matches found.
   */
  public boolean modifiedAfter(Date date) throws GrouperException {
    return this._queryModifiedAfter(date);
  }

  /**
   * Set group <i>modifyTime</i> filter.
   * <p />
   *
   * @param   date  Query for groups modifed before this {@link Date}.
   * @return  True if one or more matches found.
   */
  public boolean modifiedBefore(Date date) throws GrouperException {
    return this._queryModifiedBefore(date);
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
    // Filter candidates through privilege interfaces
    // FIXME vals  = this._candidateFilter(vals);
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

  /* (!javadoc)
   * Perform query candidate privilege selection.
   */
  private List _candidateFilter(List candidates) {
    // FIXME Just a passthrough for now
    return candidates;
  }

  /*
   * @return List of groups created after a specified date.
   */
  private List _groupCreatedAfter(java.util.Date d) {
    String  qry   = "GrouperGroup.by.created.after";
    List    vals  = new ArrayList();
    try {
      Query q = this.s.dbSess().session().getNamedQuery(qry);
      q.setString(0, Long.toString(d.getTime()));
      try {
        // TODO Is this necessary?  Or even accurate?
        Iterator iter = q.list().iterator();
        while (iter.hasNext()) {
          GrouperGroup g = (GrouperGroup) iter.next();
          g = GrouperGroup.loadByKey(this.s, g, g.key());
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
    String  qry   = "GrouperGroup.by.created.before";
    List    vals  = new ArrayList();
    try {
      Query q = this.s.dbSess().session().getNamedQuery(qry);
      q.setString(0, Long.toString(d.getTime()));
      try {
        // TODO Is this necessary?  Or even accurate?
        Iterator iter = q.list().iterator();
        while (iter.hasNext()) {
          GrouperGroup g = (GrouperGroup) iter.next();
          g = GrouperGroup.loadByKey(this.s, g, g.key());
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
    String  qry   = "GrouperGroup.by.modified.after";
    List    vals  = new ArrayList();
    try {
      Query q = this.s.dbSess().session().getNamedQuery(qry);
      q.setString(0, Long.toString(d.getTime()));
      try {
        // TODO Is this necessary?  Or even accurate?
        Iterator iter = q.list().iterator();
        while (iter.hasNext()) {
          GrouperGroup g = (GrouperGroup) iter.next();
          g = GrouperGroup.loadByKey(this.s, g, g.key());
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
    String  qry   = "GrouperGroup.by.modified.before";
    List    vals  = new ArrayList();
    try {
      Query q = s.dbSess().session().getNamedQuery(qry);
      q.setString(0, Long.toString(d.getTime()));
      try {
        // TODO Is this necessary?  Or even accurate?
        Iterator iter = q.list().iterator();
        while (iter.hasNext()) {
          GrouperGroup g = (GrouperGroup) iter.next();
          g = GrouperGroup.loadByKey(this.s, g, g.key());
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

  /* (!javadoc)
   * Perform createdAfter query.
   */
  private boolean _queryCreatedAfter(Date date) {
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

  /* (!javadoc)
   * Perform createdBefore query.
   */
  private boolean _queryCreatedBefore(Date date) {
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

  /* (!javadoc)
   * Perform groupType query.
   */
  private boolean _queryGroupType(String type) throws GrouperException {
    boolean rv    = false;
    List    vals  = new ArrayList();

    this.candidates.remove(KEY_GT);
    // Find all groups of matching type
    List      groups  = this._queryByGroupType(type);
    // Find all list values for matching groups
    Iterator  iter    = groups.iterator();
    while (iter.hasNext()) {
      GrouperGroup g = (GrouperGroup) iter.next();
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
          GrouperGroup g = GrouperGroup.loadByKey(this.s, gs.key());
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

  /* (!javadoc)
   * Perform membership type query.
   */
  private boolean _queryMembershipType(String type) throws GrouperException {
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
  
  /* (!javadoc)
   * Perform modifiedAfter query.
   */
  private boolean _queryModifiedAfter(Date date) {
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

  /* (!javadoc)
   * Perform modifiedBefore query.
   */
  private boolean _queryModifiedBefore(Date date) {
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

  /* (!javadoc)
   * Iterate through a list of groups, find list values for group, and
   * add list values to a List that will be returned.
   */
  private static List _iterGroup(GrouperSession s, List groups) {
    List vals = new ArrayList();

    if (groups != null) {
      Iterator iter = groups.iterator();
      while (iter.hasNext()) {
        GrouperGroup g = (GrouperGroup) iter.next();
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

