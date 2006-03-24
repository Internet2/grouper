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
import  java.util.*;
import  org.apache.commons.lang.builder.*;
import  org.apache.commons.logging.*;


/** 
 * Grouper Transaction Queue <b>FACTOR ADD</b> command.
 * @author blair christensen.
 *     
*/
class TxFactorAdd extends TxQueue implements Serializable {

  // Private Class Constants
  private static final EventLog EL  = new EventLog();
  private static final Log      LOG = LogFactory.getLog(TxFactorAdd.class);

  
  // Constructors

  // For Hibernate
  public TxFactorAdd() { 
    super();
  } // TxFactorAdd()

  protected TxFactorAdd(GrouperSession s, Owner owner, Factor f) {
    super();
    this.setSessionId(  s.getSessionId()        );
    this.setActor(      s.getMember()           );
    this.setOwner(      owner                   );
    this.setFactor(     f                       );
    this.setField(      Group.getDefaultList()  );
  } // TxFactorAdd(s, owner, f)


  // Public Instance Methods
  public boolean apply(GrouperDaemon gd) {
    boolean rv = false;
    try {
      GrouperSession  root  = GrouperSessionFinder.getTransientRootSession();
      GrouperSession  fake  = new GrouperSession(this.getSessionId(), this.getActor());
      Set             ms    = new LinkedHashSet();

      // TODO REFACTOR
      if (this.getFactor().getKlass().equals(UnionFactor.KLASS)) {
        ms = this._calculateUnionFactor(root);
      }
      else {
        gd.getLog().error("factor type unknown: " + this.getFactor().getKlass());
      }
     
      if (ms.size() > 0) { 
        ( (Group) this.getOwner() ).setModified(this.getActor());
        Set saves = new LinkedHashSet();
        saves.addAll(ms);
        // TODO saves.addAll( this._findParentMemberships() );
        saves.addAll( FactorFinder.findIsFactor(root, this) );
        saves.add(this);
        HibernateHelper.save(saves);
        // FIXME LOG!
      }

      root.stop();
      rv = true;
    }
    catch (Exception e) {
      //  MembershipNotFoundException
      //  SessionException
      //  SubjectNotFoundException
      gd.getLog().failedToApplyTx(this, e.getMessage());
    }
    return rv;
  } // public boolean apply()


  // Private Instance Methods
  private Set _calculateUnionFactor(GrouperSession s) 
    throws  Exception
  {
    Set results = new LinkedHashSet();
    Set tmp     = new LinkedHashSet();
    Group left  = (Group) this.getFactor().getLeft();
    Group right = (Group) this.getFactor().getRight();
    left.setSession(s);
    right.setSession(s);
    tmp.addAll( left.getMembers() );
    tmp.addAll( right.getMembers()  );
    return this._createNewMembershipObjects(s, tmp);
  } // private Set _calculateUnionFactor(s)

  private Set _createNewMembershipObjects(GrouperSession s, Set tmp) 
    throws  Exception
  {
    Set       results = new LinkedHashSet();
    Iterator iter     = tmp.iterator();
    while (iter.hasNext()) {
      Member      m   = (Member) iter.next();
      Membership  imm = new Membership(
        s, this.getOwner(), m, this.getField()
      );
      results.add(imm);
    }
    return results;
  } // private Set _createNewMembershipObjects(s, tmp)

}

