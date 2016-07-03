/**
 * Copyright 2014 Internet2
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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

import org.apache.commons.lang.time.StopWatch;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.Composite;
import edu.internet2.middleware.grouper.Field;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.internal.util.Quote;
import edu.internet2.middleware.grouper.misc.M;
import edu.internet2.middleware.grouper.privs.Privilege;
import edu.internet2.middleware.grouper.subj.SubjectHelper;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;

/** 
 * Grouper API logging.
 * 
 * @author  blair christensen.
 * @version $Id: EventLog.java,v 1.10 2009-08-18 23:11:38 shilen Exp $
 */
public class EventLog {

  private static final  Log                     LOG               = GrouperUtil.getLog(EventLog.class);


  /**
   * 
   */
  public EventLog() {
    super();
  } // protected EventLog()


  // PROTECTED CLASS METHODS //

  /**
   * @param s
   * @param c
   * @param sw
   * @since 1.0
   */
  public void groupAddComposite(
    GrouperSession s, Composite c, StopWatch sw
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
  } // protected void groupAddComposite(s, c, sw)

  /**
   * @param s
   * @param c
   * @param sw
   * @since 1.0
   */
  public void groupDelComposite(
    GrouperSession s, Composite c, StopWatch sw
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
    /*this.groupAddAndDelCompositeMembers(
      s, c, mof.getSaves(), mof.getDeletes(), sw
    );*/
  } // protected void groupDelComposite(s, c, mof, sw)

  /**
   * @since 1.0
   * @param msg
   */
  public static void info(String msg) {
    LOG.info(msg);
  } // protected static void info(msg)

  /**
   * @since 1.0
   * @param s
   * @param msg
   */
  public static void info(GrouperSession s, String msg) {
    LOG.info(LogHelper.internal_formatSession(s) + msg);
  } // protected static void info(s, msg)

  /**
   * @since 1.0
   * @param s
   * @param msg
   * @param sw
   */
  public static void info(GrouperSession s, String msg, StopWatch sw) {
    EventLog.info(s.toString(), msg, sw);
  } // protected static void info(log, sessionToString, msg, sw)

  /**
   * @since 1.0
   * @param sessionToString
   * @param msg
   * @param sw
   */
  public static void info(
    String sessionToString, String msg, StopWatch sw
  ) 
  {
    LOG.info(
      LogHelper.internal_formatSession(sessionToString) + msg + LogHelper.internal_formatStopWatch(sw)
    );
  } // protected static void info(log, sessionToString, msg, sw)


  // PROTECTED INSTANCE METHODS //

  /**
   * @param s 
   * @param group 
   * @param subj 
   * @param f 
   * @param sw 
   * 
   */
  public void groupAddMember(
    GrouperSession s, String group, Subject subj, Field f, StopWatch sw
  )
  {
    this._member(s, M.G_AM, group, subj, f, sw);
  } // protected void groupAddMember(s, group, subj, f, sw)

  /**
   * @param s
   * @param group
   * @param attr
   * @param val
   * @param sw
   */
  public void groupDelAttr(
    GrouperSession s, String group, String attr, String val, StopWatch sw
  )
  {
    this._setAttr(s, M.G_DA, group, attr, val, sw);
  } // protected void groupDelAttr(s, group, attr, val, sw);

  /**
   * @param s
   * @param group
   * @param subj
   * @param f
   * @param sw
   */
  public void groupDelMember(
    GrouperSession s, String group, Subject subj, Field f, StopWatch sw
  ) {
    this._member(s, M.G_DM, group, subj, f, sw);
  } // protected void groupDelMember(s, group, subj, f, sw)

  /**
   * @param s
   * @param group
   * @param subj
   * @param p
   * @param sw
   */
  public void groupGrantPriv(
    GrouperSession s, String group, Subject subj, Privilege p, StopWatch sw
  ) {
    this._grantPriv(s, M.G_GP, group, subj, p, sw);
  } // protected void groupGrantPriv(s, group, subj, p, sw)

  /**
   * @param s
   * @param group
   * @param p
   * @param sw
   */
  public void groupRevokePriv(
    GrouperSession s, String group, Privilege p, StopWatch sw
  ) 
  {
    this._revokePriv(s, M.G_RP, group, p, sw);
  } // protected void groupRevokePriv(s, group, p, sw)

  /**
   * @param s
   * @param group
   * @param subj
   * @param p
   * @param sw
   */
  public void groupRevokePriv(
    GrouperSession s, String group, Subject subj, Privilege p, StopWatch sw
  ) 
  {
    this._revokePriv(s, M.G_RP, group, subj, p, sw);
  } // protected void groupRevokePriv(s, group, subj, p, sw)

  /**
   * @param s
   * @param group
   * @param attr
   * @param val
   * @param sw
   */
  public void groupSetAttr(
    GrouperSession s, String group, String attr, String val, StopWatch sw
  )
  {
    this._setAttr(s, M.G_SA, group, attr, val, sw);
  } // protected void groupSetAttr(s, group, attr, val, sw);

  /**
   * @param s
   * @param stem
   * @param subj
   * @param p
   * @param sw
   */
  public void stemGrantPriv(
    GrouperSession s, String stem, Subject subj, Privilege p, StopWatch sw
  )
  {
    this._grantPriv(s, M.S_GP, stem, subj, p, sw);
  } // protected void stemGrantPriv(s, stem, subj, p, sw)

  /**
   * @param s
   * @param stem
   * @param p
   * @param sw
   */
  public void stemRevokePriv(
    GrouperSession s, String stem, Privilege p, StopWatch sw
  ) 
  {
    this._revokePriv(s, M.S_RP, stem, p, sw);
  } // protected void stemRevokePriv(s, stem, p, sw)

  /**
   * @param s
   * @param stem
   * @param subj
   * @param p
   * @param sw
   */
  public void stemRevokePriv(
    GrouperSession s, String stem, Subject subj, Privilege p, StopWatch sw
  ) 
  {
    this._revokePriv(s, M.S_RP, stem, subj, p, sw);
  } // protected void stemRevokePriv(s, stem, subj, p, sw)

  /**
   * @param s
   * @param stem
   * @param attr
   * @param val
   * @param sw
   */
  public void stemSetAttr(
    GrouperSession s, String stem, String attr, String val, StopWatch sw
  )
  {
    this._setAttr(s, M.S_SA, stem, attr, val, sw);
  } // protected void stemSetAttr(s, stem, attr, val, sw);


  // PRIVATE INSTANCE METHODS //

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

