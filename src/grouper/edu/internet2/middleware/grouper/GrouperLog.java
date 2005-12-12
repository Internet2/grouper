/*
  Copyright 2004-2005 University Corporation for Advanced Internet Development, Inc.
  Copyright 2004-2005 The University Of Chicago

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

package edu.internet2.middleware.grouper;


import  edu.internet2.middleware.subject.*;
import  edu.internet2.middleware.subject.provider.*;
import  java.io.Serializable;
import  java.util.*;
import  org.apache.commons.logging.*;


/** 
 * Grouper API logging.
 * <p />
 * @author  blair christensen.
 * @version $Id: GrouperLog.java,v 1.8 2005-12-12 21:52:09 blair Exp $
 *     
*/
class GrouperLog implements Serializable {

  // Protected Class Constants
  protected static final String ERR_CMGR  = "unable to get cache manager: ";
  protected static final String ERR_GRS   = "unable to start root session: ";
  protected static final String ERR_LOG   = "error logging: ";
  protected static final String MSG_EC    = "emptied cache ";


  // Protected Class Methods

  protected static void addEffMS(Log log, GrouperSession s, Set effs) {
    Iterator iter = effs.iterator();
    while (iter.hasNext()) {
      try {
        Membership eff = (Membership) iter.next();
        String msg = "add effective member: " + _getMsMsg(s, eff);
        GrouperLog.info(log, s, msg);
      }
      catch (Exception e) {
        GrouperLog.error(log, s, ERR_LOG + "addition of effective membership");
      }
    }
  } // protected static void addEffMS(log, s, effs)

  protected static void addImmMS(Log log, GrouperSession s, Membership imm) {
    try {
      String msg = "add immediate member: " + _getMsMsg(s, imm);
      GrouperLog.info(log, s, msg);
    }
    catch (Exception e) {
      GrouperLog.error(log, s, ERR_LOG + "addition of of immediate membership");
    }
  } // protected static void addImmMS(log, s, imm)

  protected static void debug(Log log, GrouperSession s, String msg) {
    log.debug( _formatMsg(s, msg) );
  } // protected static void debug(log, s, msg)

  protected static void delEffMS(Log log, GrouperSession s, Set effs) {
    Iterator iter = effs.iterator();
    while (iter.hasNext()) {
      try {
        Membership  eff = (Membership) iter.next();
        String msg = "delete effective member: " + _getMsMsg(s, eff);
        GrouperLog.info(log, s, msg);
      }
      catch (Exception e) {
        GrouperLog.error(log, s, ERR_LOG + "deletion of effective membership");
      }
    }
  } // protected static void delEffMS(log, s, effs)

  protected static void delImmMS(Log log, GrouperSession s, Membership imm) {
    try {
      String msg = "delete immediate member: " + _getMsMsg(s, imm);
      GrouperLog.info(log, s, msg);
    }
    catch (Exception e) {
      GrouperLog.error(log, s, ERR_LOG + "deletion of of immediate membership");
    }
  } // protected static void delImmMS(log, s, imm)

  protected static void error(Log log, GrouperSession s, String msg) {
    log.error( _formatMsg(s, msg) );
  } // protected static void error(log, s, msg)

  protected static void fatal(Log log, GrouperSession s, String msg) {
    log.fatal( _formatMsg(s, msg) );
  } // protected static void fatal(log, s, msg)

  protected static void info(Log log, GrouperSession s, String msg) {
    log.info( _formatMsg(s, msg) );
  } // protected static void info(log, s, msg)

  protected static String q(String txt) {
    String fmtd = "'" + txt + "'";
    return fmtd;
    //return "'" + txt + "'";
  } // protected static String q(txt)

  protected static void warn(Log log, GrouperSession s, String msg) {
    log.warn( _formatMsg(s, msg) );
  } // protected static void warn(log, s, msg)


  // Private Class Methods
  private static String _formatMsg(GrouperSession s, String msg) {
    return "[" + s.toString() + "] " + msg;
  } // private static String _formatMsg(s, msg)

  // Format membership for logging purposes
  private static String _getMsMsg(GrouperSession s, Membership ms) 
    throws  Exception
  {
    GrouperSession root = GrouperSessionFinder.getTransientRootSession();
    ms.setSession(root);
    String msg;
    try {
      Group g = (Group) ms.getGroup();
      msg     = "group=" + q(g.getName());
    }
    catch (GroupNotFoundException eGNF) {
      Stem ns = (Stem) ms.getStem();
      msg     = "stem=" + q(ns.getName());
    }
    msg += " field=" + q(ms.getList().getName()) + " subject="
        + q(SubjectHelper.getPretty(ms.getMember().getSubject()))
        + " depth=" + ms.getDepth();
    if (ms.getDepth() > 0) {
      msg += " via=";
      try {
        Group via = ms.getViaGroup();
        msg += q(via.getName());
      }
      catch (GroupNotFoundException eGNF) {
        msg += "???";
      }
    }
    ms.setSession(s);
    root.stop();
    return msg;
  } // private static String _getMsMsg(ms)

} // protected class GrouperLog

