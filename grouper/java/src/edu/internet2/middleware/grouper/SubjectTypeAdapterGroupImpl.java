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


import  edu.internet2.middleware.grouper.*;
import  edu.internet2.middleware.subject.*;
import  java.util.*;
import  net.sf.hibernate.*;


/** 
 * Default implementation of the I2MI {@link SubjectTypeAdapter} interface
 * for subjects of type "group".
 * <p />
 *
 * @author  blair christensen.
 * @version $Id: SubjectTypeAdapterGroupImpl.java,v 1.14 2005-03-23 21:52:49 blair Exp $
 */
public class  SubjectTypeAdapterGroupImpl
	extends     AbstractSubjectTypeAdapter
	implements  SubjectTypeAdapter
{

  /*
   * CONSTRUCTORS
   */
  public SubjectTypeAdapterGroupImpl() {
    // Nothing -- Yet
  }
 
 
  /*
   * PUBLIC INSTANCE METHODS
   */

  /**
   * Not Implemented.
   */
  public void destroy() { 
    // XXX Nothing -- Yet
    Grouper.log().notimpl("SubjectTypeAdapterGroupImpldestroy");
  }

  /**
   * Retrieve a <i>group</i> subject from the groups registry.
   * <p />
   * @return  A {@link Subject} object.
   */
  public Subject getSubject(SubjectType type, String id) {
    DbSess  dbSess  = new DbSess(); // FIXME CACHE!
    String  qry     = "GrouperGroup.by.id";
    Subject subj    = null;
    try {
      Query q = dbSess.session().getNamedQuery(qry);
      q.setString(0, id);
      try {
        List vals = q.list();
        if (vals.size() == 1) {
          // FIXME Properly load the group
          GrouperGroup g = (GrouperGroup) vals.get(0);
          if (g != null) {
            // ... And convert it to a subject object
            subj = new SubjectImpl(id, type.getId());
          }
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
    dbSess.stop();
    return subj;
  }

  /**
   * Not Implemented.
   */
  public Subject getSubjectByDisplayId(SubjectType type, String displayId) {
    Grouper.log().notimpl("SubjectTypeAdapterGroupImplgetSubjectByDisplayId");
    return null;
  }
 
  /**
   * Not Implemented.
   */
  public Subject[] getSubjects(SubjectType type) {
    Grouper.log().notimpl("SubjectTypeAdapterGroupImplgetSubjects");
    return null;
  }

  /**
   * Not Implemented.
   */
  public void init() {
    // XXX Nothing -- Yet
    Grouper.log().notimpl("SubjectTypeAdapterGroupImplinit");
  }

  /**
   * Not Implemented.
   */
  public boolean isModifiable() {
    Grouper.log().notimpl("SubjectTypeAdapterGroupImplisModifiable");
    return false;
  }

  /**
   * Not Implemented.
   */
  public Subject newSubject(SubjectType type, 
                            String      id, 
                            String      name, 
                            String      description, 
                            String      displayId) 
  {
    Grouper.log().notimpl("SubjectTypeAdapterGroupImplnewSubject");
    return null;
  }

  /**
   * Not Implemented.
   */
  public Subject quickSearch(String searchValue) {
    Grouper.log().notimpl("SubjectTypeAdapterGroupImplquickSearch");
    return null;
  }

  /**
   * Not Implemented.
   */
  public Subject[] searchByIdentifier(SubjectType type, String id) {
    Grouper.log().notimpl("SubjectTypeAdapterGroupImplsearchByIdentifier");
    return null;
  }

}
