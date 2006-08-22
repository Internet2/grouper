/*
  Copyright 2004-2006 University Corporation for Advanced Internet Development, Inc.
  Copyright 2004-2006 The University Of Chicago

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
import  java.util.*;
import  org.apache.commons.lang.time.*;
import  org.apache.commons.logging.*;

/** 
 * Grouper API logging.
 * <p/>
 * @author  blair christensen.
 * @version $Id: EventLog.java,v 1.23 2006-08-22 19:48:22 blair Exp $
 */
class EventLog {

  // PRIVATE CLASS CONSTANTS //
  private static final Log LOG = LogFactory.getLog(EventLog.class);
  // FIXME Relocate to M
  private static final String G_AM        = "add member: group=";
  private static final String G_AM_E      = "add effective member: group=";
  private static final String G_DA        = "delete group attr: group=";
  private static final String G_DM        = "delete member: group=";
  private static final String G_DM_E      = "delete effective member: group=";
  private static final String G_GP        = "grant access priv: group=";
  private static final String G_GP_E      = "grant effective access priv: group=";
  private static final String G_RP        = "revoke access priv: group=";
  private static final String G_RP_E      = "revoke effective access priv: group=";
  private static final String G_SA        = "set group attr: group=";
  private static final String S_GP        = "grant naming priv: stem=";
  private static final String S_GP_E      = "grant effective naming priv: stem=";
  private static final String S_RP        = "revoke naming priv: stem=";
  private static final String S_RP_E      = "revoke effective naming priv: stem=";
  private static final String S_SA        = "set stem attr: stem=";


  // PRIVATE INSTANCE VARIALBES //
  private boolean       log_eff_group_add = false;
  private boolean       log_eff_group_del = false;
  private boolean       log_eff_stem_add  = false;
  private boolean       log_eff_stem_del  = false;


  // CONSTRUCTORS //
  protected EventLog() {
    super();
    if (GrouperConfig.getProperty(GrouperConfig.MSLGEA).equals(GrouperConfig.BT)) {
      log_eff_group_add = true;
    } 
    if (GrouperConfig.getProperty(GrouperConfig.MSLGED).equals(GrouperConfig.BT)) {
      log_eff_group_del = true;
    }
    if (GrouperConfig.getProperty(GrouperConfig.MSLSEA).equals(GrouperConfig.BT)) {
      log_eff_stem_add = true;
    }
    if (GrouperConfig.getProperty(GrouperConfig.MSLSED).equals(GrouperConfig.BT)) {
      log_eff_stem_del = true;
    }
  } // protected EventLog()


  // PROTECTED CLASS METHODS //

  // @since 1.0
  protected static void compositeUpdate(
    Composite c, Set saves, Set deletes, StopWatch sw
  )
  {
    EventLog.groupAddAndDelCompositeMembers(
      c.getSession(), c, saves, deletes, sw
    );
  } // protected static void compositeUpdate(c, saves, deletes)

  // @since 1.0
  protected static void groupAddAndDelCompositeMembers(
    GrouperSession s, Composite c, Set saves, Set deletes, StopWatch sw
  )
  {
    Object    obj;
    Iterator  iter  = saves.iterator();
    while (iter.hasNext()) {
      obj = iter.next();
      if (obj instanceof Membership) {
        Membership ms = (Membership) obj;
        _member(s, M.COMP_MEMADD, c.getOwnerName(), ms, sw);
      }
    }
    iter = deletes.iterator();
    while (iter.hasNext()) {
      obj = iter.next();
      if (obj instanceof Membership) {
        Membership ms = (Membership) obj;
        _member(s, M.COMP_MEMDEL, c.getOwnerName(), ms, sw);
      }
    }
  } // protected static void groupAddAndDelCompositeMembers(s, c, mof, sw)

  // @since 1.0
  protected static void groupAddComposite(
    GrouperSession s, Composite c, MemberOf mof, StopWatch sw
  )
  {
    EventLog.info(
      s, 
      M.COMP_ADD 
      + U.q(c.getOwnerName()                    )
      + " type="  + U.q(c.getType().toString()  )
      + " left="  + U.q(c.getLeftName()         )
      + " right=" + U.q(c.getRightName()        ),
      sw
    );
    EventLog.groupAddAndDelCompositeMembers(
      s, c, mof.getSaves(), mof.getDeletes(), sw
    );
  } // protected static void groupAddComposite(s, c, mof, sw)

  // @since 1.0
  protected static void groupDelComposite(
    GrouperSession s, Composite c, MemberOf mof, StopWatch sw
  )
  {
    EventLog.info(
      s, 
      M.COMP_DEL 
      + U.q(c.getOwnerName()                    )
      + " type="  + U.q(c.getType().toString()  )
      + " left="  + U.q(c.getLeftName()         )
      + " right=" + U.q(c.getRightName()        ),
      sw
    );
    EventLog.groupAddAndDelCompositeMembers(
      s, c, mof.getSaves(), mof.getDeletes(), sw
    );
  } // protected static void groupDelComposite(s, c, mof, sw)

  // @since 1.0
  protected static void info(String msg) {
    LOG.info(msg);
  } // protected static void info(msg)

  // @since 1.0
  protected static void info(GrouperSession s, String msg) {
    LOG.info(LogHelper.formatSession(s) + msg);
  } // protected static void info(s, msg)

  // @since 1.0
  protected static void info(GrouperSession s, String msg, StopWatch sw) {
    EventLog.info(s.toString(), msg, sw);
  } // protected static void info(log, sessionToString, msg, sw)

  // @since 1.0
  protected static void info(
    String sessionToString, String msg, StopWatch sw
  ) 
  {
    LOG.info(
      LogHelper.formatSession(sessionToString) + msg + LogHelper.formatStopWatch(sw)
    );
  } // protected static void info(log, sessionToString, msg, sw)


  // PROTECTED INSTANCE METHODS //

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
    GrouperSession s, Owner o, Subject subj, Field f, Set effs
  )
  {
    if      (o instanceof Group) {
      if (this.log_eff_group_del == true) {
        this._delEffs(s, "group=" + U.q( ( (Group) o).getName() ), subj, f, effs);
      }
    }
    else if (o instanceof Stem) {
      if (this.log_eff_stem_del == true) {
        this._delEffs(s, "stem=" + U.q( ( (Stem) o).getName() ), subj, f, effs);
      }
    }
    else {
      // FIXME Better message
      ErrorLog.error(EventLog.class, E.EVENT_EFFDEL);
    }
  }

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


  // PRIVATE STATIC METHODS //

  // @since 1.0
  private static void _member(
    GrouperSession s, String msg, String where, Membership ms, StopWatch sw
  )
  {
    String subject = GrouperConfig.EMPTY_STRING;
    try {
      subject = SubjectHelper.getPretty(ms.getMember().getSubject());
    }
    catch (Exception e) {
      subject = U.q(e.getMessage());
    }
    EventLog.info(
      s,
      msg           + U.q(where) 
      + " list="    + U.q(ms.getList().getName())  
      + " subject=" + subject,
      sw
    );
  } // private void _member(s, msg, where, subj, f, sw)


  // PRIVATE INSTANCE METHODS //

  private void _addEffs(
    GrouperSession s, String name, Subject subj, Field f, Set effs
  )
  {
    try {
      GrouperSession  root  = GrouperSession.startTransient();
      Membership      eff;
      Iterator        iter  = effs.iterator();
      while (iter.hasNext()) {
        eff = (Membership) iter.next();
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
      ErrorLog.error(EventLog.class, E.EVENT_EFFADD + eS.getMessage());
    }
  } // private void _addEffs(s, name, subj, f, effs)

  private void _delEffs(
    GrouperSession s, String name, Subject subj, Field f, Set effs
  )
  {
    try {
      GrouperSession  root  = GrouperSession.startTransient();
      Membership      eff;
      Iterator        iter  = effs.iterator();
      while (iter.hasNext()) {
        eff = (Membership) iter.next();
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
      ErrorLog.error(EventLog.class, E.EVENT_EFFADD + eS.getMessage());
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
      msg += "group=" + U.q(g.getName());
    }
    catch (GroupNotFoundException eGNF) {
      try {
        Stem ns = eff.getStem();
        msg += "stem=" + U.q(ns.getName());
      }
      catch (StemNotFoundException eSNF) {
        ErrorLog.error(EventLog.class, E.EVENT_EFFOWNER + eSNF.getMessage());
        msg += "owner=???";
      }
    }   
    // Get eff field
    msg += " " + field + U.q(eff.getList().getName());
    // Get eff subject
    try {
      Subject subject = eff.getMember().getSubject();
      msg += " subject=" + SubjectHelper.getPretty(subject);
    }
    catch (Exception e) {
      ErrorLog.error(EventLog.class, E.EVENT_EFFSUBJ + e.getMessage());
      msg += "subject=???";
    }
    // Get added or removed message that caused this effective membership change
    msg += " (" + name + " ";
    if      (f.getType().equals(FieldType.ACCESS)) {
      msg += "priv=" + U.q(f.getName());
    }
    else if (f.getType().equals(FieldType.LIST)) {
      msg += "list=" + U.q(f.getName());
    }
    else if (f.getType().equals(FieldType.NAMING)) {
      msg += "priv=" + U.q(f.getName());
    }
    // Get added or removed subject that caused this effective
    // membership change
    msg += " subject=" + SubjectHelper.getPretty(subj) + ")";
    // Now log it
    LOG.info( LogHelper.formatMsg(s, msg) );
    // Reset to the original session
    eff.setSession(s);
  } // private void _eff(root, s, msg, name, subj, f, eff, field)

  private void _grantPriv(
    GrouperSession s, String msg, String name, Subject subj, Privilege p, StopWatch sw
  )
  {
    EventLog.info(
      s,
      msg + U.q(name) + " priv=" + U.q(p.getName()) + " subject=" 
      + SubjectHelper.getPretty(subj),
      sw
    );
  } // private void _grantPriv(s, msg, name, subj, p, sw)

  // TODO Deprecate
  private void _member(
    GrouperSession s, String msg, String group, Subject subj, Field f, StopWatch sw
  )
  {
    EventLog.info(
      s,
      msg + U.q(group) + " list=" + U.q(f.getName()) + " subject=" 
      + SubjectHelper.getPretty(subj),
      sw
    );
  } // private void _member(s, msg, group, subj, f, sw)

  private void _revokePriv(
    GrouperSession s, String msg, String name, Privilege p, StopWatch sw
  )
  {
    EventLog.info(
      s, msg + U.q(name) + " priv=" + U.q(p.getName()), sw
    );
  } // private void _revokePriv(s, msg, name, p, sw)

  private void _revokePriv(
    GrouperSession s, String msg, String name, Subject subj, Privilege p, StopWatch sw
  )
  {
    EventLog.info(
      s,
      msg + U.q(name) + " priv=" + U.q(p.getName()) + " subject=" 
      + SubjectHelper.getPretty(subj),
      sw
    );
  } // private void _revokePriv(s, msg, name, subj, p, sw)

  private void _setAttr(
    GrouperSession s, String msg, String name, String attr, String val, StopWatch sw
  )
  {
    EventLog.info(s, msg + U.q(name) + " attr=" + U.q(attr) + " value=" + U.q(val), sw);
  } // private void _setAttr(s, msg, attr, val, sw)
}

