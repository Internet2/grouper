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
 * @version $Id: GrouperQuery.java,v 1.2 2004-12-02 02:33:49 blair Exp $
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
   * Set membership query filter.
   * <p />
   *
   * @param   type  Type of membership to query on.  Valid options are
   *   null, "effective", and "immediate".
   * @return  Boolean true if matching items were found, otherwise
   *   false.
   */
  public boolean membership(String type) throws GrouperException {
    return this._queryMembership(type);
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
        } else {
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
   * Perform membership query.
   */
  private boolean _queryMembership(String type) throws GrouperException {
    boolean rv    = false;
    List    vals  = new ArrayList();
    if (type == null) { // FIXME Null is ugly
      // Query for both effective + immediate memberships
      vals = GrouperBackend.listVals(this.gs, Grouper.DEF_LIST_TYPE);
    } else if (type.equals("effective")) {
      // Query for effective memberships
      vals = GrouperBackend.listEffVals(this.gs, Grouper.DEF_LIST_TYPE);
    } else if (type.equals("immediate")) {
      // Query for immediate memberships
      vals = GrouperBackend.listImmVals(this.gs, Grouper.DEF_LIST_TYPE);
    } else {
      throw new GrouperException("Unknown membership type: " + type);
    } 
    if ( (vals != null) && (vals.size() > 0) ) {
      this.candidates.put("membership", vals);
      rv = true;
    }
    return rv; 
  }

}

