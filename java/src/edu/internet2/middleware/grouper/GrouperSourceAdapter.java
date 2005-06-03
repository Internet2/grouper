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
import  org.apache.commons.logging.Log;
import  org.apache.commons.logging.LogFactory;


/** 
 * TODO
 * <p />
 *
 * @author  blair christensen.
 * @version $Id: GrouperSourceAdapter.java,v 1.3 2005-06-03 15:45:33 blair Exp $
 */
public class GrouperSourceAdapter extends BaseSourceAdapter {

  private static Log log = LogFactory.getLog(GrouperSourceAdapter.class);

  /*
   * CONSTRUCTORS
   */

  public GrouperSourceAdapter() {
    super();
  }

  public GrouperSourceAdapter(String id, String name) {
    super(id, name);
  }


  /*
   * PUBLIC INSTANCE METHODS
   */

  public void destroy() {
    // TODO What destruction should I be doing?
    log.info("Destroying GrouperSourceAdapter");
  }

  /**
   * {@inheritDoc}
   */
  public Subject getSubject(String id) throws SubjectNotFoundException {
    // FIXME Now *this* is inefficient
    Subject subj = null;
    try {
      Subject root = SubjectFactory.getSubject(
        Grouper.config("member.system"), Grouper.DEF_SUBJ_TYPE
      );
      GrouperSession s = GrouperSession.start(root); 
      // TODO Optimize further based upon presence of '-' and ':'?
      GrouperGroup g = GrouperGroup.loadByID(s, id);
      if (g == null) { // TODO GroupNotFoundException
        g = GrouperGroup.loadByName(s, id);
          if (g != null) {
            subj = new GrouperSubject(g, this);
          }
      } else {
        subj = new GrouperSubject(g, this);
      }        
      s.stop();
    } catch (SubjectNotFoundException e) {
      log.debug("Unable to find subject: " + id + ": " + e.getMessage());
      throw new SubjectNotFoundException(
        "Unable to find subject: " + id + ": " + e.getMessage()
      );
    }
    if (subj == null) {
      log.debug("Unable to find subject: " + id);
      throw new SubjectNotFoundException("Unable to find subject: " + id);
    }
    log.debug("Found subject: " + id + ": " + subj);
    return subj;
  }

  /** 
   * {@inheritDoc}
   */
  public void init() throws SourceUnavailableException {
    // TODO What initialization should I be doing?
    log.info("Initializing GrouperSourceAdapter");
  }

  public Set search(String searchValue) {
    log.warn("GrouperSourceAdapter.search() not implemented");
    throw new RuntimeException(
      "GrouperSourceAdapter.search() not implemented"
    );
  }

  public Set searchByIdentifier(String id) {
    log.warn("GrouperSourceAdapter.searchByIdentifier() not implemented");
    throw new RuntimeException(
      "GrouperSourceAdapter.searchByIdentifier() not implemented"
    );
  }

  public Set searchByIdentifier(String id, SubjectType type) {
    log.warn("GrouperSourceAdapter.searchByIdentifier() not implemented");
    throw new RuntimeException(
      "GrouperSourceAdapter.searchByIdentifier() not implemented"
    );
  }

}

