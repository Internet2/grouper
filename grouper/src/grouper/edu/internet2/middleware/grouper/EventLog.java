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

package edu.internet2.middleware.grouper;
import  edu.internet2.middleware.grouper.internal.util.Quote;
import  edu.internet2.middleware.subject.*;
import  java.util.HashMap;
import  java.util.Iterator;
import  java.util.Set;
import  org.apache.commons.lang.time.*;
import  org.apache.commons.logging.*;

/** 
 * Grouper API logging.
 * <p/>
 * @author  blair christensen.
 * @version $Id: EventLog.java,v 1.54 2008-06-25 05:46:05 mchyzer Exp $
 */
class EventLog {

  private               HashMap<String, Group>  groupCache        = new HashMap<String, Group>();
  private static final  Log                     LOG               = LogFactory.getLog(EventLog.class);
  private               boolean                 log_eff_group_add = false;
  private               boolean                 log_eff_group_del = false;
  private               boolean                 log_eff_stem_add  = false;
  private               boolean                 log_eff_stem_del  = false;
  private               HashMap<String, Stem>   stemCache         = new HashMap<String, Stem>();


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
    EventLog.groupAddAndDelCompositeMembers( GrouperSession.staticGrouperSession(), c, saves, deletes, sw );
  } // protected static void compositeUpdate(c, saves, deletes)

  // @since 1.0
  protected static void groupAddAndDelCompositeMembers(
    GrouperSession s, Composite c, Set saves, Set deletes, StopWatch sw
  )
  {
    //note, no need for GrouperSession inverse of control
    Object      obj;
    Membership  ms;
    Iterator    it  = saves.iterator();
    while (it.hasNext()) {
      obj = it.next();
      if (obj instanceof Membership) {
        ms = (Membership) obj;
        _member( s, M.COMP_MEMADD, c.internal_getOwnerName(), ms, sw );
      }
    }
    it = deletes.iterator();
    while (it.hasNext()) {
      obj = it.next();
      if (obj instanceof Membership) {
        ms = (Membership) obj;
        _member( s, M.COMP_MEMDEL, c.internal_getOwnerName(), ms, sw );
      }
    }
  } // protected static void groupAddAndDelCompositeMembers(s, c, mof, sw)

  // @since 1.0
  protected static void groupAddComposite(
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
    EventLog.groupAddAndDelCompositeMembers(
      s, c, mof.getSaves(), mof.getDeletes(), sw
    );
  } // protected static void groupAddComposite(s, c, mof, sw)

  // @since 1.0
  protected static void groupDelComposite(
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
    LOG.info(LogHelper.internal_formatSession(s) + msg);
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
      LogHelper.internal_formatSession(sessionToString) + msg + LogHelper.internal_formatStopWatch(sw)
    );
  } // protected static void info(log, sessionToString, msg, sw)


  // PROTECTED INSTANCE METHODS //

  // @since   1.2.0
  protected void addEffMembers(GrouperSession s, Group g, Subject subj, Field f, Set effs) {
    if (this.log_eff_group_add) {
      this._addEffs(s, "group=" + Quote.single( g.getName() ), subj, f, effs);
    }
  } // protected void addEffMembers(s, g, subj, f, effs)

  // @since   1.2.0
  protected void addEffMembers(GrouperSession s, Stem ns, Subject subj, Field f, Set effs) {
    if (this.log_eff_stem_add) {
      this._addEffs(s, "stem=" + Quote.single( ns.getName() ), subj, f, effs);
    }
  } // protected void addEffMembers(s, ns, subj, f, effs)

  protected void delEffMembers(
    GrouperSession s, Owner o, Subject subj, Field f, Set effs
  )
  {
    if      (o instanceof Group) {
      if (this.log_eff_group_del == true) {
        this._delEffs(s, "group=" + Quote.single( ( (Group) o).getName() ), subj, f, effs);
      }
    }
    else if (o instanceof Stem) {
      if (this.log_eff_stem_del == true) {
        this._delEffs(s, "stem=" + Quote.single( ( (Stem) o).getName() ), subj, f, effs);
      }
    }
    else {
      ErrorLog.error(EventLog.class, E.EVENT_EFFDEL);
    }
  }

  protected void groupAddMember(
    GrouperSession s, String group, Subject subj, Field f, StopWatch sw
  )
  {
    this._member(s, M.G_AM, group, subj, f, sw);
  } // protected void groupAddMember(s, group, subj, f, sw)

  protected void groupDelAttr(
    GrouperSession s, String group, String attr, String val, StopWatch sw
  )
  {
    this._setAttr(s, M.G_DA, group, attr, val, sw);
  } // protected void groupDelAttr(s, group, attr, val, sw);

  protected void groupDelMember(
    GrouperSession s, String group, Subject subj, Field f, StopWatch sw
  )
  {
    this._member(s, M.G_DM, group, subj, f, sw);
  } // protected void groupDelMember(s, group, subj, f, sw)

  protected void groupGrantPriv(
    GrouperSession s, String group, Subject subj, Privilege p, StopWatch sw
  )
  {
    this._grantPriv(s, M.G_GP, group, subj, p, sw);
  } // protected void groupGrantPriv(s, group, subj, p, sw)

  protected void groupRevokePriv(
    GrouperSession s, String group, Privilege p, StopWatch sw
  ) 
  {
    this._revokePriv(s, M.G_RP, group, p, sw);
  } // protected void groupRevokePriv(s, group, p, sw)

  protected void groupRevokePriv(
    GrouperSession s, String group, Subject subj, Privilege p, StopWatch sw
  ) 
  {
    this._revokePriv(s, M.G_RP, group, subj, p, sw);
  } // protected void groupRevokePriv(s, group, subj, p, sw)

  protected void groupSetAttr(
    GrouperSession s, String group, String attr, String val, StopWatch sw
  )
  {
    this._setAttr(s, M.G_SA, group, attr, val, sw);
  } // protected void groupSetAttr(s, group, attr, val, sw);

  protected void stemGrantPriv(
    GrouperSession s, String stem, Subject subj, Privilege p, StopWatch sw
  )
  {
    this._grantPriv(s, M.S_GP, stem, subj, p, sw);
  } // protected void stemGrantPriv(s, stem, subj, p, sw)

  protected void stemRevokePriv(
    GrouperSession s, String stem, Privilege p, StopWatch sw
  ) 
  {
    this._revokePriv(s, M.S_RP, stem, p, sw);
  } // protected void stemRevokePriv(s, stem, p, sw)

  protected void stemRevokePriv(
    GrouperSession s, String stem, Subject subj, Privilege p, StopWatch sw
  ) 
  {
    this._revokePriv(s, M.S_RP, stem, subj, p, sw);
  } // protected void stemRevokePriv(s, stem, subj, p, sw)

  protected void stemSetAttr(
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
    EventLog.info(
      s,
      msg           + Quote.single(where) 
      + " list="    + Quote.single( ms.getList().getName() )  
      + " subject=" + subject,
      sw
    );
  } // private void _member(s, msg, where, subj, f, sw)


  // PRIVATE INSTANCE METHODS //

  private void _addEffs(
    GrouperSession s, String name, Subject subj, Field f, Set effs
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
        this._eff(s, M.G_AM_E, name, subj, f, eff, "list="); 
      }
      else if ( eff.getListType().equals(FieldType.NAMING.toString()) )  {
        this._eff(s, M.S_GP_E, name, subj, f, eff, "priv="); 
      }
    }
  }

  private void _delEffs(
    GrouperSession s, String name, Subject subj, Field f, Set effs
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
        this._eff(s, M.G_DM_E, name, subj, f, eff, "list="); 
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
    msg += this._getEffOwnerMsg(eff);
    // Get eff field
    msg += " " + field + Quote.single( eff.getListName() );
    msg += this._getEffSubjectMsg(eff);
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
    // Now log it
    LOG.info( LogHelper.internal_formatMsg(s, msg) );
  } 

  // @since   1.2.0
  // TODO 20070531 i need to make this all go away
  private String _getEffOwnerMsg(Membership _eff) {
    String  msg   = GrouperConfig.EMPTY_STRING;
    String  uuid  = _eff.getOwnerUuid();

    Group   g     = null;
    Stem    ns    = null;
    if      ( this.groupCache.containsKey(uuid) )   {
      g = this.groupCache.get(uuid);
    }
    else if ( this.stemCache.containsKey(uuid) )  {
      ns = this.stemCache.get(uuid);
    }
    else {
      try {
        Group _g = GrouperDAOFactory.getFactory().getGroup().findByUuid(uuid);
        this.groupCache.put(uuid, g);
      }
      catch (GroupNotFoundException eGNF) {
        try {
          Stem _ns = GrouperDAOFactory.getFactory().getStem().findByUuid(uuid);
          this.stemCache.put(uuid, ns);
        }
        catch (StemNotFoundException eSNF) {
          ErrorLog.error(EventLog.class, E.EVENT_EFFOWNER + eSNF.getMessage());
        }
      }   
    }
    if (g != null) {
      msg += "group=" + Quote.single( g.getName() );
    }
    else if (ns != null ) {
      msg += "stem=" + Quote.single( ns.getName() );
    }
    else {
      msg += "owner=???";
    }
    return msg;
  } // private String _getEffOwnerMsg(_eff)

  // @since   1.2.0
  private String _getEffSubjectMsg(Membership _eff) {
    try {
      return " subject=" + SubjectHelper.getPretty( GrouperDAOFactory.getFactory().getMember().findByUuid( _eff.getMemberUuid() ) );
    }
    catch (MemberNotFoundException eMNF)    {
      // TODO 20070323 this can't help performance
      ErrorLog.error( EventLog.class, E.EVENT_EFFSUBJ + eMNF.getMessage() );
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

