/*
 * Copyright (C) 2004 University Corporation for Advanced Internet Development, Inc.
 * Copyright (C) 2004 The University Of Chicago
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
import  org.apache.commons.lang.builder.ToStringBuilder;


/** 
 * Class representing a {@link Grouper} registry query.
 * <p />
 *
 * @author  blair christensen.
 * @version $Id: GrouperQuery.java,v 1.5 2004-12-06 19:33:10 blair Exp $
 */
public class GrouperQuery {

  /*
   * PRIVATE INSTANCE VARIABLES
   */
  private GrouperSession  gs;
  private Map             candidates;


  /*
   * CONSTRUCTORS
   */

  /**
   * Construct a new {@link GrouperQuery} object.
   */
  public GrouperQuery(GrouperSession s) {
    this._init();
    this.gs = s;
  }


  /*
   * PUBLIC INSTANCE METHODS
   */

  /**
   * Set {@link GrouperGroup} <i>createTime</i> filter.
   * <p />
   *
   * @param   date  Query for items created after this {@link Date}.
   * @return  Boolean true if matching items were found, otherwise
   *   false.
   */
  public boolean createdAfter(Date date) throws GrouperException {
    return this._queryCreatedAfter(date);
  }

  /**
   * Set {@link GrouperGroup} <i>createTime</i> filter.
   * <p />
   *
   * @param   date  Query for items created before this {@link Date}.
   * @return  Boolean true if matching items were found, otherwise
   *   false.
   */
  public boolean createdBefore(Date date) throws GrouperException {
    return this._queryCreatedBefore(date);
  }

  /**
   * Set {@link GrouperGroup} type filter.
   * <p />
   * TODO How do I unset?
   *
   * @param   type  Type of {@link GrouperGroup} to query on.
   * @return  Boolean true if matching items were found, otherwise
   *   false.
   */
  public boolean groupType(String type) throws GrouperException {
    return this._queryGroupType(type);
  }

  /**
   * Set membership query filter.
   * <p />
   *
   * @param   type  Type of membership to query on.  Valid options are
   *   <i>Grouper.MEM_ALL</i>, <i>Grouper.MEM_EFF</i>, and
  *    <i>Grouper.MEM_IMM</i>.
   * @return  Boolean true if matching items were found, otherwise
   *   false.
   */
  public boolean membership(String type) throws GrouperException {
    return this._queryMembership(type);
  }

  /**
   * Set {@link GrouperGroup} <i>modifyTime</i> filter.
   * <p />
   *
   * @param   date  Query for items modified after this {@link Date}.
   * @return  Boolean true if matching items were found, otherwise
   *   false.
   */
  public boolean modifiedAfter(Date date) throws GrouperException {
    return this._queryModifiedAfter(date);
  }

  /**
   * Set {@link GrouperGroup} <i>modifyTime</i> filter.
   * <p />
   *
   * @param   date  Query for items modifed before this {@link Date}.
   * @return  Boolean true if matching items were found, otherwise
   *   false.
   */
  public boolean modifiedBefore(Date date) throws GrouperException {
    return this._queryModifiedBefore(date);
  }

  /**
   * Query the group registry used the already specified filters.
   * <p />
   * TODO There will be <b>nothing</b> optimal about the first
   * implementation of this method -- or class, for that matter.
   *
   * @return  List of {@link GrouperList} objects.
   */
  public List query() {
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
   * Return a string representation of the {@link GrouperQuery} object.
   * <p />
   *
   * @return  String representation of the object.
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

  /* (!javadoc)
   * Initialize instance variables.
   */
  private void _init() {
    this.candidates = new HashMap();
    this.gs         = null;
  }

  /* (!javadoc)
   * Perform createdAfter query.
   */
  private boolean _queryCreatedAfter(Date date) {
    boolean rv    = false;
    List    vals  = new ArrayList();
    // Find all groups created after this date
    vals = GrouperQuery._iterGroup(
             this.gs, GrouperBackend.groupCreatedAfter(date)
           );
    if ( (vals != null) && (vals.size() > 0) ) {
      rv = true;
    }
    this.candidates.put("createdafter", vals);
    return rv; 
  }

  /* (!javadoc)
   * Perform createdBefore query.
   */
  private boolean _queryCreatedBefore(Date date) {
    boolean rv    = false;
    List    vals  = new ArrayList();
    // Find all groups created before this date
    vals = GrouperQuery._iterGroup(
             this.gs, GrouperBackend.groupCreatedBefore(date)
           );
    if ( (vals != null) && (vals.size() > 0) ) {
      rv = true;
    }
    this.candidates.put("createdbefore", vals);
    return rv; 
  }

  /* (!javadoc)
   * Perform groupType query.
   */
  private boolean _queryGroupType(String type) throws GrouperException {
    boolean rv    = false;
    List    vals  = new ArrayList();
    // Find all groups of matching type
    List      groups  = GrouperBackend.groupType(this.gs, type);
    // Find all list values for matching groups
    Iterator  iter    = groups.iterator();
    while (iter.hasNext()) {
      GrouperGroup g = (GrouperGroup) iter.next();
      Iterator lvIter =  GrouperBackend.listVals(
                           this.gs, g, Grouper.DEF_LIST_TYPE
                         ).iterator();
      while (lvIter.hasNext()) {
        GrouperList gl = (GrouperList) lvIter.next();
        vals.add(gl);
      }
    }
    if ( (vals != null) && (vals.size() > 0) ) {
      rv = true;
    }
    this.candidates.put("grouptype", vals);
    return rv; 
  }

  /* (!javadoc)
   * Perform membership query.
   */
  private boolean _queryMembership(String type) throws GrouperException {
    boolean rv    = false;
    List    vals  = new ArrayList();
    if        (type.equals(Grouper.MEM_ALL)) {
      // Query for both effective + immediate memberships
      vals = GrouperBackend.listVals(this.gs, Grouper.DEF_LIST_TYPE);
    } else if (type.equals(Grouper.MEM_EFF)) {
      // Query for effective memberships
      vals = GrouperBackend.listEffVals(this.gs, Grouper.DEF_LIST_TYPE);
    } else if (type.equals(Grouper.MEM_IMM)) {
      // Query for immediate memberships
      vals = GrouperBackend.listImmVals(this.gs, Grouper.DEF_LIST_TYPE);
    } else {
      throw new GrouperException("Unknown membership type: " + type);
    } 
    if ( (vals != null) && (vals.size() > 0) ) {
      rv = true;
    }
    this.candidates.put("membership", vals);
    return rv; 
  }

  /* (!javadoc)
   * Perform modifiedAfter query.
   */
  private boolean _queryModifiedAfter(Date date) {
    boolean rv    = false;
    List    vals  = new ArrayList();
    // Find all groups modified after this date
    vals = GrouperQuery._iterGroup(
             this.gs, GrouperBackend.groupModifiedAfter(date)
           );
    if ( (vals != null) && (vals.size() > 0) ) {
      rv = true;
    }
    this.candidates.put("modifiedafter", vals);
    return rv; 
  }

  /* (!javadoc)
   * Perform modifiedBefore query.
   */
  private boolean _queryModifiedBefore(Date date) {
    boolean rv    = false;
    List    vals  = new ArrayList();
    // Find all groups modified before this date
    vals = GrouperQuery._iterGroup(
             this.gs, GrouperBackend.groupModifiedBefore(date)
           );
    if ( (vals != null) && (vals.size() > 0) ) {
      rv = true;
    }
    this.candidates.put("modifiedbefore", vals);
    return rv; 
  }

  /* (!javadoc)
   * Iterate through a list of groups, find list values for group, and
   * add list values to a List that will be returned.
   */
  private static List _iterGroup(GrouperSession gs, List groups) {
    List vals = new ArrayList();
    if (groups != null) {
      Iterator iter = groups.iterator();
      while (iter.hasNext()) {
        GrouperGroup g = (GrouperGroup) iter.next();
        Iterator lvIter =  GrouperBackend.listVals(
                             gs, g, Grouper.DEF_LIST_TYPE
                           ).iterator();
        while (lvIter.hasNext()) {
          GrouperList gl = (GrouperList) lvIter.next();
          if (gl != null) {
            vals.add(gl);
          }
        }
      }
    }
    return vals;
  }

}

