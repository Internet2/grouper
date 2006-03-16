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


import  java.io.Serializable;
import  org.apache.commons.lang.builder.*;
import  org.apache.commons.logging.*;


/** 
 * Grouper Transaction Queue <b>MEMBER DEL</b> command.
 * @author blair christensen.
 *     
*/
class TxMemberDel extends TxQueue implements Serializable {

  // Private Class Constants
  private static final EventLog EL  = new EventLog();
  private static final Log      LOG = LogFactory.getLog(TxMemberDel.class);

  
  // Constructors

  // For Hibernate
  public TxMemberDel() { 
    super();
  } // TxMemberDel()

  protected TxMemberDel(GrouperSession s, Owner owner, Member m, Field f) {
    super();
    this.setSessionId(s.getSessionId());
    this.setActor(s.getMember());
    this.setOwner(owner);
    this.setField(f);
    this.setMember(m); 
  } // TxMemberDel()


  // Public Instance Methods
  public boolean apply(GrouperDaemon gd) {
    boolean rv = false;
    try {
      GrouperSession  root  = GrouperSessionFinder.getTransientRootSession();
      GrouperSession  fake  = new GrouperSession(this.getSessionId(), this.getActor());
      Member          m     = this.getMember();
      m.setSession(root);
      Membership      imm   = new Membership(root, this.getOwner(), m, this.getField());
      Membership.delEffectiveMemberships(fake, imm);
      root.stop();
      rv = true;
    }
    catch (Exception e) {
      gd.getLog().failedToApplyTx(this, e.getMessage());
    }
    return rv;
  } // public boolean apply()

}

