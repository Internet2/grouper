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
class TxFactorAdd extends TxQueue implements Serializable, Tx {

  // Private Class Constants
  private static final EventLog EL  = new EventLog();
  private static final Log      LOG = LogFactory.getLog(TxFactorAdd.class);


  // Transient Variables
  private transient GrouperDaemon gd = null;

  
  // Constructors

  // For Hibernate
  public TxFactorAdd() { 
    super();
  } // TxFactorAdd()

  protected TxFactorAdd(GrouperSession s, Owner owner, Factor f) {
    this(s.getSessionId(), owner, s.getMember(), f, owner);
  } // protected TxFactorAdd(s, owner, f)

  protected TxFactorAdd(String sid, Owner owner, Member m, Factor f, Owner origin) {
    super();
    this.setSessionId(  sid                     );
    this.setActor(      m                       );
    this.setOwner(      owner                   );
    this.setOrigin(     origin                  );
    this.setFactor(     f                       );
    this.setField(      Group.getDefaultList()  );
  } // protected TxFactorAdd(sid, owner, m, f, origin)


  // Public Instance Methods
  public void apply(GrouperDaemon gd) 
    throws  TxException
  {
    this.gd = gd;
    try {
      GrouperSession  root  = GrouperSessionFinder.getTransientRootSession();
      GrouperSession  fake  = new GrouperSession(this.getSessionId(), this.getActor());
      Set             ms    = new LinkedHashSet();

      // TODO Necessary? 
      root.flushCache("edu.internet2.middleware.grouper.MembershipFinder.FindMembershipsOwner");

      // TODO REFACTOR
      if (this.getFactor().getKlass().equals(UnionFactor.KLASS)) {
        ms = this._calculateUnionFactor(root);
      }
      else {
        this.gd.getLog().error("factor type unknown: " + this.getFactor().getKlass());
      }
    
      if ( (ms.size() > 0) && (this._hasMembershipChanged(root, ms) ) ) {
        ( (Group) this.getOwner() ).setModified(this.getActor());
        Set saves = new LinkedHashSet();
        saves.addAll(ms);
        // TODO saves.addAll( this._findParentMemberships() );
        saves.addAll( this._findFactorsToUpdate(root) );
        saves.add(this);
        HibernateHelper.save(saves);
        // FIXME LOG!
      }

      root.stop();
    }
    catch (Exception e) {
      //  MembershipNotFoundException
      //  SessionException
      //  SubjectNotFoundException
      String msg = "unable to apply factor add: " + e.getMessage();
      this.gd.getLog().failedToApplyTx(this, msg);
      throw new TxException(msg);
    }
  } // public void apply()


  // Private Instance Methods
  private Set _calculateUnionFactor(GrouperSession root) 
    throws  Exception
  {
    Set results = new LinkedHashSet();
    Set tmp     = new LinkedHashSet();
    Group left  = (Group) this.getFactor().getLeft();
    Group right = (Group) this.getFactor().getRight();
    left.setSession(root);
    right.setSession(root);
    tmp.addAll( left.getMembers() );
    tmp.addAll( right.getMembers()  );
    return this._createNewMembershipObjects(root, tmp);
  } // private Set _calculateUnionFactor(root)

  private Set _createNewMembershipObjects(GrouperSession root, Set tmp) 
    throws  Exception
  {
    Set       results = new LinkedHashSet();
    Iterator iter     = tmp.iterator();
    while (iter.hasNext()) {
      Member      m   = (Member) iter.next();
      Membership  imm = new Membership(
        root, this.getOwner(), m, this.getField()
      );
      imm.setVia_id( this.getFactor() );
      imm.setSession(root);
      results.add(imm);
    }
    return results;
  } // private Set _createNewMembershipObjects(root, tmp)

  private Set _findFactorsToUpdate(GrouperSession root) 
    throws  Exception
  {
    Set   results = new LinkedHashSet();
    Group origin  = (Group) this.getOrigin();
    origin.setSession(root);
    Iterator  iter    = FactorFinder.findIsFactor(root, this).iterator();
    while (iter.hasNext()) {
      Factor  f     = (Factor) iter.next();
      Group   owner = (Group) f.getFactor_owner(); 
      owner.setSession(root);
      if (origin.equals(f.getFactor_owner())) {
        continue; // We are back to where we started.  Ignore to prevent recursion.
      }
      TxQueue tx = new TxFactorAdd(
        this.getSessionId(), owner, this.getActor(), f, origin
      );
      results.add(tx);
    }
    return results;
  } // private Set _findFactorsToUpdate(root)

  private boolean _hasMembershipChanged(GrouperSession root, Set exp) {
    boolean rv    = false;
    Group   owner = (Group) this.getOwner();
    owner.setSession(root);

    Set got = owner.getMemberships();
    if (got.size() != exp.size()) {
      rv = true;
    }
    else {
      // FIXME Equality comparison on collections?
      Map       m       = new HashMap();
      Iterator  iterGot = got.iterator();
      Iterator  iterExp = exp.iterator();
      while (iterGot.hasNext()) {
        Membership ms = (Membership) iterGot.next();
        ms.setSession(root);
        //m.put(ms.getUuid(), ms);
        m.put(ms, ms);
      }
      while (iterExp.hasNext()) {
        Membership ms = (Membership) iterExp.next();
        ms.setSession(root);
        //if (m.containsKey(ms.getUuid())) {
        if (m.containsKey(ms)) {
          //m.remove(ms.getUuid());
          m.remove(ms);
        }
        else {
          break;  // As this signals at least one discrepancy no need 
                  //to check for addition differences
        }
      }
      if (m.size() > 0) {
        rv = true; // The membership has changed
      }
    }
    return rv;
  } // private boolean _hasMembershipChanged(root, exp)

}

