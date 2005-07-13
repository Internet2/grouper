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


import  edu.internet2.middleware.subject.*;
import  edu.internet2.middleware.subject.provider.*;
import  java.util.*;
import  net.sf.hibernate.*;
import  org.apache.commons.logging.Log;
import  org.apache.commons.logging.LogFactory;


/** 
 * TODO
 * <p />
 *
 * @author  blair christensen.
 * @version $Id: GrouperSourceAdapter.java,v 1.8 2005-07-13 18:33:38 blair Exp $
 */
public class GrouperSourceAdapter extends BaseSourceAdapter {

  /*
   * PRIVATE CLASS VARIABLES
   */
  private static Log log = LogFactory.getLog(GrouperSourceAdapter.class);


  /*
   * PRIVATE INSTANCE VARIABLES
   */
  private GrouperSession s = null;


  /*
   * CONSTRUCTORS
   */

  /**
   * Allocates new GrouperSourceAdapter.
   */
  public GrouperSourceAdapter() {
    super();
  }

  /**
   * Allocates new GrouperSourceAdapter.
   */
  public GrouperSourceAdapter(String id, String name) {
    super(id, name);
  }


  /*
   * PUBLIC INSTANCE METHODS
   */

  /**
   * {@inheritDoc}
   */
  public Subject getSubject(String id) throws SubjectNotFoundException {
    Subject subj = null;
    // TODO Optimize further based upon presence of '-' and ':'?
    GrouperGroup g = GrouperGroup.loadByID(this.getSession(), id);
    if (g == null) { // TODO GroupNotFoundException
      g = GrouperGroup.loadByName(s, id);
        if (g != null) {
          subj = new GrouperSubject(g, this);
        }
    } else {
      subj = new GrouperSubject(g, this);
    }        
    if (subj == null) {
      log.debug("Unable to find subject: " + id);
      throw new SubjectNotFoundException("Unable to find subject: " + id);
    }
    log.debug("Found subject: " + id + ": " + subj);
    return subj;
  }

  /**
   * This method is currently just an alias for 
   * {@link #getSubject(String) getSubject} method.
   */
  public Subject getSubjectByIdentifier(String id) 
    throws SubjectNotFoundException 
  {
    // FIXME Move _loadByName_ from _getSubject()_ to here?
    return this.getSubject(id);
  }

  /** 
   * {@inheritDoc}
   */
  public void init() throws SourceUnavailableException {
    // TODO What initialization should I be doing?
    log.info("Initializing GrouperSourceAdapter");
  }

  /**
   * {@inheritDoc}
   */
  // TODO Is this the right search?
  // TODO document search
  // TODO ideally this query could be moved to GrouperQuery
  // TODO There is overlap in code between this and sBID
  public Set search(String searchValue) {
    String  qry   = "Group.subject.search";
    Set     vals  = new HashSet();
    try {
      Query q = this.getSession().dbSess().session().getNamedQuery(qry);
      // TODO Move _%_ to _Grouper.hbm.xml_
      q.setString(0, "%" + searchValue + "%"); // _stem_
      // TODO Move _%_ to _Grouper.hbm.xml_
      q.setString(1, "%" + searchValue + "%"); // _extension_
      // TODO Move _%_ to _Grouper.hbm.xml_
      q.setString(2, "%" + searchValue + "%"); // _displayextension_
      // TODO Move _%_ to _Grouper.hbm.xml_
      q.setString(3, "%" + searchValue + "%"); // _name_
      // TODO Move _%_ to _Grouper.hbm.xml_
      q.setString(4, "%" + searchValue + "%"); // _displayname_
      try {
        Iterator iter = q.list().iterator();
        while (iter.hasNext()) {
          try {
            String key = (String) iter.next();
            GrouperGroup g = (GrouperGroup) GrouperGroup.loadByKey(this.getSession(), key);
            Subject subj = new GrouperSubject(g, this);
            vals.add(subj);
            log.debug("search found: " + g + "/" + subj);
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
    } catch (SubjectNotFoundException e) {
      throw new RuntimeException(
        "Unable to perform query " + qry + ": " + e.getMessage()
      );
    }
    log.debug("search results: " + vals.size());
    return vals;
  }


  /*
   * PRIVATE INSTANCE METHODS
   */

  /*
   * Return root GrouperSession.  Creates session if necessary.
   */
  private GrouperSession getSession() throws SubjectNotFoundException {
    // TODO Should I check to see that it is connected?
    if (this.s == null) {
      try {
        Subject root = SubjectFactory.getSubject(
          Grouper.config("member.system"), Grouper.DEF_SUBJ_TYPE
        );
        this.s = GrouperSession.start(root);
      } catch (SubjectNotFoundException e) {
        log.debug(
          "Unable to create root subject for querying: " + e.getMessage()
        );
        throw new SubjectNotFoundException(
          "Unable to create root subject for querying: " + e.getMessage()
        );
      }
      log.info("Created root session");
    } else {
      log.debug("Reusing existing root session");
    }
    return this.s;
  }

}

