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

import  org.apache.log4j.*;


/** 
 * Class used within {@link Grouper} for logging.
 * <p />
 * This class is only used internally and isn't very useful at the
 * moment.  A more coherent, useful and comprehensive logging policy
 * will be implemented in a later release.
 *
 * @author  blair christensen.
 * @version $Id: GrouperLog.java,v 1.17 2005-05-23 13:09:20 blair Exp $
 */
public class GrouperLog {

  /*
   * PROTECTED CONSTANTS
   */
  private static final Logger LOG     = 
    Logger.getLogger(Grouper.class.getName());
  private static final Logger LOG_EVT = 
    Logger.getLogger(Grouper.class.getName() + ".event");


  /*
   * CONSTRUCTORS
   */
  protected GrouperLog() {
    super();
  }


  /*
   * PROTECTED INSTANCE METHODS
   */

  // General events
  protected void event(String msg) {
    LOG_EVT.info(msg);
  }

  // Privilege: Grant
  protected void grant(
                   boolean rv, GrouperSession s, GrouperStem ns,
                   GrouperMember m, String priv
                 ) 
  {
    Subject tgt = null;
    try {
      tgt = SubjectFactory.getSubject(
                                m.subjectID(), m.typeID()
                              );
    } catch (SubjectNotFoundException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    String pre  = "'" + s.subject().getId() + "' ";
    String post = " '" + priv + "' to memberID='" + m.memberID() + 
                  "' subjectID='" + tgt.getId() + "' on '" +
                  ns.name() + "'";
    if (rv == true) {
      LOG_EVT.info(pre + "granted" + post);
    } else {
      LOG_EVT.info(pre + "failed to grant" + post);
    }
  }

  // Privilege: Grant
  protected void grant(
                   boolean rv, GrouperSession s, Group g, 
                   GrouperMember m, String priv
                 ) 
  {
    Subject tgt = null;
    try {
      tgt = SubjectFactory.getSubject(
                                m.subjectID(), m.typeID()
                              );
    } catch (SubjectNotFoundException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    String pre  = "'" + s.subject().getId() + "' ";
    String post = " '" + priv + "' to memberID='" + m.memberID() + 
                  "' subjectID='" + tgt.getId() + "' on '" +
                  g.name() + "'";
    if (rv == true) {
      LOG_EVT.info(pre + "granted" + post);
    } else {
      LOG_EVT.info(pre + "failed to grant" + post);
    }
  }

  // Stem: Add
  protected void stemAdd(
                   GrouperSession s, GrouperStem ns, String name, 
                   String type
                 ) 
  {
    String pre  = "'" + s.subject().getId() + "' ";
    String post = " '" + name + "' (" + type + ")";
    if (ns != null) {
      LOG_EVT.info(pre + "created" + post);
    } else {
      LOG_EVT.info(pre + "failed to create" + post);
    }
  }

  // Group: Add
  protected void groupAdd(
                   GrouperSession s, Group g, String name, 
                   String type
                 ) 
  {
    String pre  = "'" + s.subject().getId() + "' ";
    String post = " '" + name + "' (" + type + ")";
    if (g != null) {
      LOG_EVT.info(pre + "created" + post);
    } else {
      LOG_EVT.info(pre + "failed to create" + post);
    }
  }

  // Grouper: Add Cannot
  protected void groupAddCannot(
                   GrouperSession s, String name, String type
                 ) 
  {
    LOG_EVT.info(
      "'" + s.subject().getId() + "' cannot create '" + name + 
      "' (" + type + ") as it already exists"
    );
  }

  // Group: Attribute Add
  protected void groupAttrAdd(
                   boolean rv, GrouperSession s, Group g,
                   String attr, String value
                 )
  {
    String pre  = "'" + s.subject().getId() + "' ";
    String post = " attribute '" + attr + "'='" + value +
                  "' to '" + g.name() + "'";
    if (rv == true) {
      LOG_EVT.info(pre + "added" + post);
    } else {
      LOG_EVT.info(pre + "failed to add" + post);
    }
  }

  // Group: Attribute Delete
  protected void groupAttrDel(
                   boolean rv, GrouperSession s, Group g, 
                   String attr
                 ) 
  {
    String pre  = "'" + s.subject().getId() + "' ";
    String post = " attribute '" + attr + "' from '" + g.name() + "'";
    if (rv == true) {
      LOG_EVT.info(pre + "deleted" + post);
    } else {
      LOG_EVT.info(pre + "failed to delete" + post);
    }
  }

  // Group: Attribute No Modification
  protected void groupAttrNoMod(String attribute) {
    LOG_EVT.info(
      "'" + attribute + "' modification is not currently supported"
    );
  }

  // Group: Attribute Update
  protected void groupAttrUpdate(
                   boolean rv, GrouperSession s, Group g,
                   String attr, String value
                 )
  {
    String pre  = "'" + s.subject().getId() + "' ";
    String post = " attribute '" + attr + "'='" + value +
                  "' to '" + g.name() + "'";
    if (rv == true) {
      LOG_EVT.info(pre + "updated" + post);
    } else {
      LOG_EVT.info(pre + "failed to update" + post);
    }
  }

  // Group: Delete
  protected void groupDel(
                   boolean rv, GrouperSession s, Group g
                 ) 
  {
    String pre  = "'" + s.subject().getId() + "' ";
    String post = " '" + g.name() + "' (" + g.type() + ")";
    if (rv == true) {
      LOG_EVT.info(pre + "deleted" + post);
    } else {
      LOG_EVT.info(pre + "failed to delete" + post);
    }
  }

  // Group: List Value Add
  protected void groupListAdd(
                   GrouperSession s, Group g, GrouperMember m
                 )
  {
    String pre  = "'" + s.subject().getId() + "' ";
    String post = " memberID='" + m.memberID() + "' subjectID='" +
                  m.subjectID() + "' to '" + g.name() + "' (" +
                  Grouper.DEF_LIST_TYPE + ")";
    LOG_EVT.info(pre + "added" + post);
  }

  // Group: List Value Delete
  protected void groupListDel(
                   GrouperSession s, Group g, GrouperMember m
                 )
  {
    String pre  = "'" + s.subject().getId() + "' ";
    String post = " memberID='" + m.memberID() + "' subjectID='" +
                  m.subjectID() + "' from '" + g.name() + "' (" +
                  Grouper.DEF_LIST_TYPE + ")";
    LOG_EVT.info(pre + "removed" + post);
  }

  // Member: Add
  protected void memberAdd(GrouperMember m, Subject subj) {
    String post = " to member table";
    if (m != null) { 
      LOG_EVT.info(
        "Added memberID='" + m.memberID() + "' " +
        "subjectID='" + m.subjectID() + "'" + post
      );
    } else {
      LOG_EVT.info(
        "Failed to add subjectID='" + subj.getId() + "' (" +
        subj.getType().getName() + ")" + post
      );
    }
  }

  // Not Implemented
  protected void notimpl(String method) {
    LOG.warn("Not Implemented: '" + method + "'");
  }

  // Privilege: Revoke
  protected void revoke(
                   boolean rv, GrouperSession s, GrouperStem ns,
                   GrouperMember m, String priv
                 ) 
  {
    Subject tgt = null;
    try {
      tgt = SubjectFactory.getSubject(
                                m.subjectID(), m.typeID()
                              );
    } catch (SubjectNotFoundException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    String pre  = "'" + s.subject().getId() + "' ";
    String post = " '" + priv + "' from memberID='" + m.memberID() + 
                  "' subjectID='" + tgt.getId() + "' on '" +
                  ns.name() + "'";
    if (rv == true) {
      LOG_EVT.info(pre + "revoked" + post);
    } else {
      LOG_EVT.info(pre + "failed to revoke" + post);
    }
  }
  protected void revoke(
                   boolean rv, GrouperSession s, Group g, 
                   GrouperMember m, String priv
                 ) 
  {
    Subject tgt = null;
    try {
      tgt = SubjectFactory.getSubject(
                                m.subjectID(), m.typeID()
                              );
    } catch (SubjectNotFoundException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    String pre  = "'" + s.subject().getId() + "' ";
    String post = " '" + priv + "' from memberID='" + m.memberID() + 
                  "' subjectID='" + tgt.getId() + "' on '" +
                  g.name() + "'";
    if (rv == true) {
      LOG_EVT.info(pre + "revoked" + post);
    } else {
      LOG_EVT.info(pre + "failed to revoke" + post);
    }
  }

  // Session: Start
  protected void sessionStart(GrouperSession s) {
    // TODO Should include type.  Just toString() it?
    LOG_EVT.info("Started session for '" + s.subject().getId() + "'");
  }

  // Session: Stop
  protected void sessionStop(GrouperSession s) {
    // TODO Should include type.  Just toString() it?
    LOG_EVT.info("Stopped session for '" + s.subject().getId() + "'");
  }

  // General warnings
  protected void warn(String msg) {
    LOG.warn(msg);
  }

}

