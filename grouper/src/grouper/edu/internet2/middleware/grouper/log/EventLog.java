/*
  Copyright (C) 2004-2007 University Corporation for Advanced Internet Development, Inc.
  Copyright (C) 2004-2007 The University Of Chicago

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

package edu.internet2.middleware.grouper.log;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.StopWatch;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.Composite;
import edu.internet2.middleware.grouper.Field;
import edu.internet2.middleware.grouper.FieldType;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Membership;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.exception.GroupNotFoundException;
import edu.internet2.middleware.grouper.exception.MemberNotFoundException;
import edu.internet2.middleware.grouper.exception.StemNotFoundException;
import edu.internet2.middleware.grouper.internal.util.Quote;
import edu.internet2.middleware.grouper.misc.DefaultMemberOf;
import edu.internet2.middleware.grouper.misc.E;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.misc.M;
import edu.internet2.middleware.grouper.misc.Owner;
import edu.internet2.middleware.grouper.privs.Privilege;
import edu.internet2.middleware.grouper.subj.SubjectHelper;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;

/** 
 * Grouper API logging.
 * <p/>
 * @author  blair christensen.
 * @version $Id: EventLog.java,v 1.6 2009-01-27 12:09:24 mchyzer Exp $
 */
public class EventLog {

  private               HashMap<String, Group>  groupCache        = new HashMap<String, Group>();
  private static final  Log                     LOG               = GrouperUtil.getLog(EventLog.class);
  private               boolean                 log_eff_group_add = false;
  private               boolean                 log_eff_group_del = false;
  private               boolean                 log_eff_stem_add  = false;
  private               boolean                 log_eff_stem_del  = false;
  private               HashMap<String, Stem>   stemCache         = new HashMap<String, Stem>();


  // CONSTRUCTORS //
  public EventLog() {
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
  protected void groupAddAndDelCompositeMembers(
    GrouperSession s, Composite c, Set saves, Set deletes, StopWatch sw
  )
  {
    //note, no need for GrouperSession inverse of control
    Object      obj;
    Membership  ms;
    String groupName = c.internal_getOwnerName();
    Set<Membership> membershipSaves = new LinkedHashSet<Membership>();
    Set<Membership> membershipDeletes = new LinkedHashSet<Membership>();

    Iterator    it  = saves.iterator();
    while (it.hasNext()) {
      obj = it.next();
      if (obj instanceof Membership) {
        ms = (Membership) obj;
        membershipSaves.add(ms);
      }
    }

    _addEffs(s, "group=" + Quote.single(groupName), null, null, membershipSaves, sw);

    it = deletes.iterator();
    while (it.hasNext()) {
      obj = it.next();
      if (obj instanceof Membership) {
        ms = (Membership) obj;
        membershipDeletes.add(ms);
      }
    }

    _delEffs(s, "group=" + Quote.single(groupName), null, null, membershipDeletes, sw);

  } // protected void groupAddAndDelCompositeMembers(s, c, mof, sw)

  // @since 1.0
  public void groupAddComposite(
    GrouperSession s, Composite c, DefaultMemberOf mof, StopWatch sw
  )
  {
    //note, no need for GrouperSession inverse of control
    EventLog.info(
      s
      , 
      M.COMP_ADD 
      + Quote.single( c.internal_getOwnerName() )
      + " type="  + Quote.single(c.getType().toString() )
      + " left="  + Quote.single( c.internal_getLeftName() )
      + " right=" + Quote.single( c.internal_getRightName() )
      ,
      sw
    );
    this.groupAddAndDelCompositeMembers(
      s, c, mof.getSaves(), mof.getDeletes(), sw
    );
  } // protected void groupAddComposite(s, c, mof, sw)

  // @since 1.0
  public void groupDelComposite(
    GrouperSession s, Composite c, DefaultMemberOf mof, StopWatch sw
  )
  {
    EventLog.info(
      s
      , 
      M.COMP_DEL 
      + Quote.single( c.internal_getOwnerName() )
      + " type="  + Quote.single(c.getType().toString()  )
      + " left="  + Quote.single( c.internal_getLeftName() )
      + " right=" + Quote.single( c.internal_getRightName() )
      ,
      sw
    );
    this.groupAddAndDelCompositeMembers(
      s, c, mof.getSaves(), mof.getDeletes(), sw
    );
  } // protected void groupDelComposite(s, c, mof, sw)

  // @since 1.0
  public static void info(String msg) {
    LOG.info(msg);
  } // protected static void info(msg)

  // @since 1.0
  public static void info(GrouperSession s, String msg) {
    LOG.info(LogHelper.internal_formatSession(s) + msg);
  } // protected static void info(s, msg)

  // @since 1.0
  public static void info(GrouperSession s, String msg, StopWatch sw) {
    EventLog.info(s.toString(), msg, sw);
  } // protected static void info(log, sessionToString, msg, sw)

  // @since 1.0
  public static void info(
    String sessionToString, String msg, StopWatch sw
  ) 
  {
    LOG.info(
      LogHelper.internal_formatSession(sessionToString) + msg + LogHelper.internal_formatStopWatch(sw)
    );
  } // protected static void info(log, sessionToString, msg, sw)


  // PROTECTED INSTANCE METHODS //

  // @since   1.2.0
  public void addEffMembers(GrouperSession s, Group g, Subject subj, Field f, Set effs) {
    if (this.log_eff_group_add) {
      this._addEffs(s, "group=" + Quote.single( g.getName() ), subj, f, effs, null);
    }
  } // protected void addEffMembers(s, g, subj, f, effs)

  // @since   1.2.0
  public void addEffMembers(GrouperSession s, Stem ns, Subject subj, Field f, Set effs) {
    if (this.log_eff_stem_add) {
      this._addEffs(s, "stem=" + Quote.single( ns.getName() ), subj, f, effs, null);
    }
  } // protected void addEffMembers(s, ns, subj, f, effs)

  public void delEffMembers(
    GrouperSession s, Owner o, Subject subj, Field f, Set effs
  )
  {
    if      (o instanceof Group) {
      if (this.log_eff_group_del == true) {
        this._delEffs(s, "group=" + Quote.single( ( (Group) o).getName() ), subj, f, effs, null);
      }
    }
    else if (o instanceof Stem) {
      if (this.log_eff_stem_del == true) {
        this._delEffs(s, "stem=" + Quote.single( ( (Stem) o).getName() ), subj, f, effs, null);
      }
    }
    else {
      LOG.error(E.EVENT_EFFDEL);
    }
  }

  public void groupAddMember(
    GrouperSession s, String group, Subject subj, Field f, StopWatch sw
  )
  {
    this._member(s, M.G_AM, group, subj, f, sw);
  } // protected void groupAddMember(s, group, subj, f, sw)

  public void groupDelAttr(
    GrouperSession s, String group, String attr, String val, StopWatch sw
  )
  {
    this._setAttr(s, M.G_DA, group, attr, val, sw);
  } // protected void groupDelAttr(s, group, attr, val, sw);

  public void groupDelMember(
    GrouperSession s, String group, Subject subj, Field f, StopWatch sw
  ) {
    this._member(s, M.G_DM, group, subj, f, sw);
  } // protected void groupDelMember(s, group, subj, f, sw)

  public void groupGrantPriv(
    GrouperSession s, String group, Subject subj, Privilege p, StopWatch sw
  ) {
    this._grantPriv(s, M.G_GP, group, subj, p, sw);
  } // protected void groupGrantPriv(s, group, subj, p, sw)

  public void groupRevokePriv(
    GrouperSession s, String group, Privilege p, StopWatch sw
  ) 
  {
    this._revokePriv(s, M.G_RP, group, p, sw);
  } // protected void groupRevokePriv(s, group, p, sw)

  public void groupRevokePriv(
    GrouperSession s, String group, Subject subj, Privilege p, StopWatch sw
  ) 
  {
    this._revokePriv(s, M.G_RP, group, subj, p, sw);
  } // protected void groupRevokePriv(s, group, subj, p, sw)

  public void groupSetAttr(
    GrouperSession s, String group, String attr, String val, StopWatch sw
  )
  {
    this._setAttr(s, M.G_SA, group, attr, val, sw);
  } // protected void groupSetAttr(s, group, attr, val, sw);

  public void stemGrantPriv(
    GrouperSession s, String stem, Subject subj, Privilege p, StopWatch sw
  )
  {
    this._grantPriv(s, M.S_GP, stem, subj, p, sw);
  } // protected void stemGrantPriv(s, stem, subj, p, sw)

  public void stemRevokePriv(
    GrouperSession s, String stem, Privilege p, StopWatch sw
  ) 
  {
    this._revokePriv(s, M.S_RP, stem, p, sw);
  } // protected void stemRevokePriv(s, stem, p, sw)

  public void stemRevokePriv(
    GrouperSession s, String stem, Subject subj, Privilege p, StopWatch sw
  ) 
  {
    this._revokePriv(s, M.S_RP, stem, subj, p, sw);
  } // protected void stemRevokePriv(s, stem, subj, p, sw)

  public void stemSetAttr(
    GrouperSession s, String stem, String attr, String val, StopWatch sw
  )
  {
    this._setAttr(s, M.S_SA, stem, attr, val, sw);
  } // protected void stemSetAttr(s, stem, attr, val, sw);


  // PRIVATE CLASS METHODS //

  // @since 1.0
  private static void _member(
    GrouperSession s, String msg, String where, Membership ms, StopWatch sw
  )
  {
    //note, no need for GrouperSession inverse of control
    String subject = GrouperConfig.EMPTY_STRING;
    try {
      subject = SubjectHelper.getPretty(ms.getMember().getSubject());
    }
    catch (Exception e) {
      subject = Quote.single(e.getMessage());
    }

    if (sw != null) {
      EventLog.info(
        s,
        msg           + Quote.single(where) 
        + " list="    + Quote.single( ms.getList().getName() )  
        + " subject=" + subject,
        sw
      );
    } else {
      EventLog.info(
        s,
        msg           + Quote.single(where) 
        + " list="    + Quote.single( ms.getList().getName() )  
        + " subject=" + subject
      );
    }
  } // private void _member(s, msg, where, subj, f, sw)


  // PRIVATE INSTANCE METHODS //

  private void _addEffs(
    GrouperSession s, String name, Subject subj, Field f, Set effs, StopWatch sw
  )
  {
    //note, no need for GrouperSession inverse of control
    Membership   eff;
    Iterator        iter  = effs.iterator();
    while (iter.hasNext()) {
      eff = (Membership) iter.next();
      if      ( eff.getListType().equals(FieldType.ACCESS.toString()) )  {
        this._eff(s, M.G_GP_E, name, subj, f, eff, "priv="); 
      }
      else if ( eff.getListType().equals(FieldType.LIST.toString()) )    {
        if (eff.isComposite()) {
          _member(s, M.COMP_MEMADD, this._getEffOwnerMsg(eff), eff, sw);
        } else {
          this._eff(s, M.G_AM_E, name, subj, f, eff, "list="); 
        }
      }
      else if ( eff.getListType().equals(FieldType.NAMING.toString()) )  {
        this._eff(s, M.S_GP_E, name, subj, f, eff, "priv="); 
      }
    }
  }

  private void _delEffs(
    GrouperSession s, String name, Subject subj, Field f, Set effs, StopWatch sw
  )
  {
    //note, no need for GrouperSession inverse of control
    Membership   eff;
    Iterator        iter  = effs.iterator();
    while (iter.hasNext()) {
      eff = (Membership) iter.next();
      if      ( eff.getListType().equals(FieldType.ACCESS.toString()) )  {
        this._eff(s, M.G_RP_E, name, subj, f, eff, "priv="); 
      }
      else if ( eff.getListType().equals(FieldType.LIST.toString()) )    {
        if (eff.isComposite()) {
          _member(s, M.COMP_MEMDEL, this._getEffOwnerMsg(eff), eff, sw);
        } else {
          this._eff(s, M.G_DM_E, name, subj, f, eff, "list="); 
        }
      }
      else if ( eff.getListType().equals(FieldType.NAMING.toString()) )  {
        this._eff(s, M.S_RP_E, name, subj, f, eff, "priv="); 
      }
    }
  } 

  private void _eff(
    GrouperSession s, String msg, String name, Subject subj, Field f, Membership eff, String field
  )
  {
    //note, no need for GrouperSession inverse of control
    msg += Quote.single(this._getEffOwnerMsg(eff));
    // Get eff field
    msg += " " + field + Quote.single( eff.getListName() );
    msg += this._getEffSubjectMsg(eff);
    if (subj != null) {
      // Get added or removed message that caused this effective membership change
      msg += " (" + name + " ";
      if      ( f.getType().equals(FieldType.ACCESS) )  {
        msg += "priv=" + Quote.single( f.getName() );
      }
      else if ( f.getType().equals(FieldType.LIST) )    {
        msg += "list=" + Quote.single( f.getName() );
      }
      else if ( f.getType().equals(FieldType.NAMING) )  {
        msg += "priv=" + Quote.single( f.getName() );
      }
      // Get added or removed subject that caused this effective
      // membership change
      msg += " subject=" + SubjectHelper.getPretty(subj) + ")";
    }
    // Now log it
    LOG.info( LogHelper.internal_formatMsg(s, msg) );
  } 

  // @since   1.2.0
  // TODO 20070531 i need to make this all go away
  private String _getEffOwnerMsg(Membership _eff) {
    String  msg   = GrouperConfig.EMPTY_STRING;
    String  groupId  = _eff.getOwnerGroupId();
    String  stemId  = _eff.getOwnerStemId();

    Group   g     = null;
    Stem    ns    = null;
    if      ( this.groupCache.containsKey(groupId) )   {
      g = this.groupCache.get(groupId);
    }
    else if ( this.stemCache.containsKey(stemId) )  {
      ns = this.stemCache.get(stemId);
    }
    else {
      try {
        if (!StringUtils.isBlank(groupId)) {
          g = _eff.getGroup();
          this.groupCache.put(groupId, g);
        } else if (!StringUtils.isBlank(stemId)) {
          ns = _eff.getStem();
          this.stemCache.put(stemId, ns);
        }
      } catch (Exception e) {
        LOG.error(E.EVENT_EFFOWNER + e.getMessage());
      }
    }
    if (g != null) {
      msg += g.getName();
    }
    else if (ns != null ) {
      msg += ns.getName();
    }
    else {
      msg += "???";
    }
    return msg;
  }

  // @since   1.2.0
  private String _getEffSubjectMsg(Membership _eff) {
    try {
      return " subject=" + SubjectHelper.getPretty( GrouperDAOFactory.getFactory().getMember().findByUuid( _eff.getMemberUuid() ) );
    }
    catch (MemberNotFoundException eMNF)    {
      // TODO 20070323 this can't help performance
      LOG.error( E.EVENT_EFFSUBJ + eMNF.getMessage() );
    }
    return " subject=???";
  } 

  private void _grantPriv(
    GrouperSession s, String msg, String name, Subject subj, Privilege p, StopWatch sw
  )
  {
    //note, no need for GrouperSession inverse of control
    EventLog.info(
      s,
      msg + Quote.single(name) + " priv=" + Quote.single( p.getName() ) + " subject=" 
      + SubjectHelper.getPretty(subj),
      sw
    );
  } // private void _grantPriv(s, msg, name, subj, p, sw)

  private void _member(
    GrouperSession s, String msg, String group, Subject subj, Field f, StopWatch sw
  )
  {
    //note, no need for GrouperSession inverse of control
    EventLog.info(
      s,
      msg + Quote.single(group) + " list=" + Quote.single( f.getName() ) + " subject=" 
      + SubjectHelper.getPretty(subj),
      sw
    );
  } // private void _member(s, msg, group, subj, f, sw)

  private void _revokePriv(
    GrouperSession s, String msg, String name, Privilege p, StopWatch sw
  )
  {
    //note, no need for GrouperSession inverse of control
    EventLog.info(
      s, msg + Quote.single(name) + " priv=" + Quote.single( p.getName() ), sw
    );
  } // private void _revokePriv(s, msg, name, p, sw)

  private void _revokePriv(
    GrouperSession s, String msg, String name, Subject subj, Privilege p, StopWatch sw
  )
  {
    //note, no need for GrouperSession inverse of control
    EventLog.info(
      s,
      msg + Quote.single(name) + " priv=" + Quote.single( p.getName() ) + " subject=" 
      + SubjectHelper.getPretty(subj),
      sw
    );
  } // private void _revokePriv(s, msg, name, subj, p, sw)

  private void _setAttr(
    GrouperSession s, String msg, String name, String attr, String val, StopWatch sw
  )
  {
    EventLog.info(s, msg + Quote.single(name) + " attr=" + Quote.single(attr) + " value=" + Quote.single(val), sw);
  } // private void _setAttr(s, msg, attr, val, sw)
}

