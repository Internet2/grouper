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
import  org.apache.commons.lang.time.*;
import  org.apache.commons.logging.*;


/** 
 * Grouper API logging.
 * <p />
 * @author  blair christensen.
 * @version $Id: EventLog.java,v 1.3 2006-01-25 18:55:33 blair Exp $
 *     
*/
class EventLog implements Serializable {

  // Private Class Constants
  private static final Log LOG = LogFactory.getLog(EventLog.class);

  // event log errors
  private static final String ERR_EFF_LOG = "unable to log effective membership: ";
  private static final String ERR_EFF_ONF = "effective membership owner not found";
  private static final String ERR_EFF_SNF = "effective membership subject not found";

  // event log messages
  private static final String G_AM        = "add member: group=";
  private static final String G_AM_E      = "add effective member: group=";
  private static final String G_D         = "group delete: ";
  private static final String G_DA        = "delete group attr: group=";
  private static final String G_DM        = "delete member: group=";
  private static final String G_DM_E      = "delete effective member: group=";
  private static final String G_GP        = "grant access priv: group=";
  private static final String G_GP_E      = "grant effective access priv: group=";
  private static final String G_RP        = "revoke access priv: group=";
  private static final String G_RP_E      = "revoke effective access priv: group=";
  private static final String G_SA        = "set group attr: group=";
  private static final String GS_START    = "session started";
  private static final String GS_STOP     = "session stopped: duration=";
  private static final String GT_AT       = "add group type: ";
  private static final String S_ACG       = "add group: ";  
  private static final String S_ACS       = "add stem: ";  
  private static final String S_GP        = "grant naming priv: stem=";
  private static final String S_GP_E      = "grant effective naming priv: stem=";
  private static final String S_RP        = "revoke naming priv: stem=";
  private static final String S_RP_E      = "revoke effective naming priv: stem=";
  private static final String S_SA        = "set stem attr: stem=";


  // Private Instance Variables
  private GrouperConfig cfg;
  private boolean       log_eff_group_add = false;
  private boolean       log_eff_group_del = false;
  private boolean       log_eff_stem_add  = false;
  private boolean       log_eff_stem_del  = false;


  // Constructors

  protected EventLog() {
    super();
    this.cfg = GrouperConfig.getInstance();
    if (this.cfg.getProperty(GrouperConfig.MSLGEA).equals(GrouperConfig.BT)) {
      log_eff_group_add = true;
    } 
    if (this.cfg.getProperty(GrouperConfig.MSLGED).equals(GrouperConfig.BT)) {
      log_eff_group_del = true;
    }
    if (this.cfg.getProperty(GrouperConfig.MSLSEA).equals(GrouperConfig.BT)) {
      log_eff_stem_add = true;
    }
    if (this.cfg.getProperty(GrouperConfig.MSLSED).equals(GrouperConfig.BT)) {
      log_eff_stem_del = true;
    }
  } // protected EventLog()


  // Protected Instance Methods

  protected void addEffMembers(
    GrouperSession s, Object o, Subject subj, Field f, Set effs
  )
  {
    if 
    ( o.getClass().equals(Group.class) && this.log_eff_group_add == true ) 
    {
      Group g = (Group) o;
      this._addEffs(s, "group=" + g.getName(), subj, f, effs);
    }
    else if
    ( o.getClass().equals(Group.class) && this.log_eff_group_add == true ) 
    {
      Stem ns = (Stem) o;
      this._addEffs(s, "stem=" + ns.getName(), subj, f, effs);
    }
  } // protected void addEffMembers(s, o, subj, f, effs)

  protected void delEffMembers(
    GrouperSession s, Group g, Subject subj, Field f, Set effs
  ) 
  {
    if (this.log_eff_group_del == true) {
      this._delEffs(s, "group=" + g.getName(), subj, f, effs);
    }
  } // protected void delEffMembers(s, g, subj, f, effs)

  protected void delEffMembers(
    GrouperSession s, Stem ns, Subject subj, Field f, Set effs
  ) 
  {
    if (this.log_eff_stem_del == true) {
      this._delEffs(s, "stem=" + ns.getName(), subj, f, effs);
    }
  } // protected void delEffMembers(s, ns, subj, f, effs)

  protected void groupAddMember(
    GrouperSession s, String group, Subject subj, Field f, StopWatch sw
  )
  {
    this._member(s, G_AM, group, subj, f, sw);
  } // protected void groupAddMember(s, group, subj, f, sw)

  protected void groupDelAttr(
    GrouperSession s, String group, String attr, String val, StopWatch sw
  )
  {
    this._setAttr(s, G_DA, group, attr, val, sw);
  } // protected void groupDelAttr(s, group, attr, val, sw);

  protected void groupDelete(GrouperSession s, String group, StopWatch sw) {
    GrouperLog.info(LOG, s.toString(), G_D + group, sw);
  } // protected void groupDelete(s, group, sw)

  protected void groupDelMember(
    GrouperSession s, String group, Subject subj, Field f, StopWatch sw
  )
  {
    this._member(s, G_DM, group, subj, f, sw);
  } // protected void groupDelMember(s, group, subj, f, sw)

  protected void groupGrantPriv(
    GrouperSession s, String group, Subject subj, Privilege p, StopWatch sw
  )
  {
    this._grantPriv(s, G_GP, group, subj, p, sw);
  } // protected void groupGrantPriv(s, group, subj, p, sw)

  protected void groupRevokePriv(
    GrouperSession s, String group, Privilege p, StopWatch sw
  ) 
  {
    this._revokePriv(s, G_RP, group, p, sw);
  } // protected void groupRevokePriv(s, group, p, sw)

  protected void groupRevokePriv(
    GrouperSession s, String group, Subject subj, Privilege p, StopWatch sw
  ) 
  {
    this._revokePriv(s, G_RP, group, subj, p, sw);
  } // protected void groupRevokePriv(s, group, subj, p, sw)

  protected void groupSetAttr(
    GrouperSession s, String group, String attr, String val, StopWatch sw
  )
  {
    this._setAttr(s, G_SA, group, attr, val, sw);
  } // protected void groupSetAttr(s, group, attr, val, sw);

  protected void groupTypeAdd(
    GrouperSession s, String type, StopWatch sw
  )
  {
    GrouperLog.info(LOG, s.toString(), GT_AT + type, sw);
  } // protected void groupDelAttr(s, group, attr, val, sw);

  protected void sessionStart(String sessionToString, StopWatch sw) {
    GrouperLog.info(LOG, sessionToString, GS_START, sw);
  } // protected sessionStart(sessionToString, sw)

  protected void sessionStop(String sessionToString, long start, StopWatch sw) {
    Date now  = new Date();
    long dur  = now.getTime() - start;
    GrouperLog.info(LOG, sessionToString, GS_STOP + dur + "ms", sw);
  } // protected sessionStop(sessionToString, start, sw)

  protected void stemAddChildGroup(GrouperSession s, String name, StopWatch sw) {
    GrouperLog.info(LOG, s.toString(), S_ACG + name, sw);
  } // protected void stemAddChildGroup(s, name, sw)

  protected void stemAddChildStem(GrouperSession s, String name, StopWatch sw) {
    GrouperLog.info(LOG, s.toString(), S_ACS + name, sw);
  } // protected void stemAddChildGroup(s, name, sw)

  protected void stemGrantPriv(
    GrouperSession s, String stem, Subject subj, Privilege p, StopWatch sw
  )
  {
    this._grantPriv(s, S_GP, stem, subj, p, sw);
  } // protected void stemGrantPriv(s, stem, subj, p, sw)

  protected void stemRevokePriv(
    GrouperSession s, String stem, Privilege p, StopWatch sw
  ) 
  {
    this._revokePriv(s, S_RP, stem, p, sw);
  } // protected void stemRevokePriv(s, stem, p, sw)

  protected void stemRevokePriv(
    GrouperSession s, String stem, Subject subj, Privilege p, StopWatch sw
  ) 
  {
    this._revokePriv(s, S_RP, stem, subj, p, sw);
  } // protected void stemRevokePriv(s, stem, subj, p, sw)

  protected void stemSetAttr(
    GrouperSession s, String stem, String attr, String val, StopWatch sw
  )
  {
    this._setAttr(s, S_SA, stem, attr, val, sw);
  } // protected void stemSetAttr(s, stem, attr, val, sw);


  // Private Instance Methods

  private void _addEffs(
    GrouperSession s, String name, Subject subj, Field f, Set effs
  )
  {
    try {
      GrouperSession root = GrouperSessionFinder.getTransientRootSession();
      Iterator iter = effs.iterator();
      while (iter.hasNext()) {
        Membership eff = (Membership) iter.next();
        if      (eff.getList().getType().equals(FieldType.ACCESS)) {
          this._eff(root, s, G_GP_E, name, subj, f, eff, "priv="); 
        }
        else if (eff.getList().getType().equals(FieldType.LIST)) {
          this._eff(root, s, G_AM_E, name, subj, f, eff, "list="); 
        }
        else if (eff.getList().getType().equals(FieldType.NAMING)) {
          this._eff(root, s, S_GP_E, name, subj, f, eff, "priv="); 
        }
      }
      root.stop();
    }
    catch (SessionException eS) {
      GrouperLog.error(LOG, s, ERR_EFF_LOG + eS.getMessage());
    }
  } // private void _addEffs(s, name, subj, f, effs)

  private void _delEffs(
    GrouperSession s, String name, Subject subj, Field f, Set effs
  )
  {
    try {
      GrouperSession root = GrouperSessionFinder.getTransientRootSession();
      Iterator iter = effs.iterator();
      while (iter.hasNext()) {
        Membership eff = (Membership) iter.next();
        if      (eff.getList().getType().equals(FieldType.ACCESS)) {
          this._eff(root, s, G_RP_E, name, subj, f, eff, "priv="); 
        }
        else if (eff.getList().getType().equals(FieldType.LIST)) {
          this._eff(root, s, G_DM_E, name, subj, f, eff, "list="); 
        }
        else if (eff.getList().getType().equals(FieldType.NAMING)) {
          this._eff(root, s, S_RP_E, name, subj, f, eff, "priv="); 
        }
      }
      root.stop();
    }
    catch (SessionException eS) {
      GrouperLog.error(LOG, s, ERR_EFF_LOG + eS.getMessage());
    }
  } // private void _delEffs(s, name, subj, f, effs)

  private void _eff(
    GrouperSession root, GrouperSession s, String msg, String name, 
    Subject subj, Field f,Membership eff, String field
  )
  {
    // Proxy as root so that we don't run into priv problems
    eff.setSession(root);
    // Get eff owner
    try {
      Group g = eff.getGroup();
      msg += "group=" + g.getName();
    }
    catch (GroupNotFoundException eGNF) {
      try {
        Stem ns = eff.getStem();
        msg += "stem=" + ns.getName();
      }
      catch (StemNotFoundException eSNF) {
        GrouperLog.error(LOG, s, ERR_EFF_ONF);
        msg += "owner=???";
      }
    }   
    // Get eff field
    msg += " " + field + eff.getList().getName();
    // Get eff subject
    try {
      Subject subject = eff.getMember().getSubject();
      msg += " subject=" + SubjectHelper.getPretty(subject);
    }
    catch (Exception e) {
      GrouperLog.error(LOG, s, ERR_EFF_SNF);
      msg += "subject=???";
    }
    // Get added or removed message that caused this effective membership change
    msg += " (" + name + " ";;
    if      (f.getType().equals(FieldType.ACCESS)) {
      msg += "priv=" + f.getName();
    }
    else if (f.getType().equals(FieldType.LIST)) {
      msg += "list=" + f.getName();
    }
    else if (f.getType().equals(FieldType.NAMING)) {
      msg += "priv=" + f.getName();
    }
    // Get added or removed subject that caused this effective
    // membership change
    msg += " subject=" + SubjectHelper.getPretty(subj) + ")";
    // Now log it
    GrouperLog.info(LOG, s.toString(), msg);
    // Reset to the original session
    eff.setSession(s);
  } // private void _eff(root, s, msg, name, subj, f, eff, field)

  private void _grantPriv(
    GrouperSession s, String msg, String name, Subject subj, Privilege p, StopWatch sw
  )
  {
    GrouperLog.info(
      LOG, s.toString(), 
      msg + name + " priv=" + p.getName() + " subject=" 
      + SubjectHelper.getPretty(subj),
      sw
    );
  } // private void _grantPriv(s, msg, name, subj, p, sw)

  private void _member(
    GrouperSession s, String msg, String group, Subject subj, Field f, StopWatch sw
  )
  {
    GrouperLog.info(
      LOG, s.toString(),
      msg + group + " list=" + f.getName() + " subject=" 
      + SubjectHelper.getPretty(subj),
      sw
    );
  } // private void _member(s, msg, group, subj, f, sw)

  private void _revokePriv(
    GrouperSession s, String msg, String name, Privilege p, StopWatch sw
  )
  {
    GrouperLog.info(
      LOG, s.toString(), msg + name + " priv=" + p.getName(), sw
    );
  } // private void _revokePriv(s, msg, name, p, sw)

  private void _revokePriv(
    GrouperSession s, String msg, String name, Subject subj, Privilege p, StopWatch sw
  )
  {
    GrouperLog.info(
      LOG, s.toString(), 
      msg + name + " priv=" + p.getName() + " subject=" 
      + SubjectHelper.getPretty(subj),
      sw
    );
  } // private void _revokePriv(s, msg, name, subj, p, sw)

  private void _setAttr(
    GrouperSession s, String msg, String name, String attr, String val, StopWatch sw
  )
  {
    GrouperLog.info(
      LOG, s.toString(), msg + name + " attr=" + attr + " value=" + val, sw
    );
  } // private void _setAttr(s, msg, attr, val, sw)
}

