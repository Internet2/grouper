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
import  net.sf.hibernate.*;
import  org.apache.commons.lang.builder.*;
import  org.apache.commons.logging.*;


/** 
 * Grouper Transaction Queue.
 * @author blair christensen.
 *     
*/
class TxQueue implements Serializable, Tx {

  // Private Class Constants
  private static final Log LOG = LogFactory.getLog(TxQueue.class);

  
  // Hibernate Properties
  private Member      actor;
  private List        dirty       = new ArrayList();
  private Factor      factor;
  private Field       field;
  private String      id;
  private String      klass;
  private Member      member;
  private Owner       owner;      
  private String      sessionId;
  private long        queueTime;
  private QueueStatus status;
  private String      uuid;


  // Constructors

  // For Hibernate
  public TxQueue() { 
    super();
    this.setQueueTime(  new Date().getTime()            );
    this.setStatus(     QueueStatus.getInstance("wait") );
    this.setUuid(       GrouperUuid.getUuid()           );
  }


  // Public Instance Methods
  public void apply(GrouperDaemon gd) 
    throws  TxException
  {
    throw new TxException("apply() not implemented in TxQueue");
  } // public void apply(gd)

  public String toString() {
    String k = this.getKlass();
    return new ToStringBuilder(this, ToStringStyle.SIMPLE_STYLE)
      .append("class"       , this.getClass().getName()                 )
      .append("time"        , new Date( this.getQueueTime()).toString() )
      .append("uuid"        , this.getUuid()                            )
      .append("status"      , this.getStatus().getName()                )
      .toString()
      ;
  } // public String toString() 


  // Protected Instance Methods

  // Delete a tx from the queue
  protected void delete(GrouperDaemon gd) {
    try {
      HibernateHelper.delete(this);
      gd.getLog().deleteTx(this);
    }
    catch (HibernateException eH) {
      String msg = eH.getMessage();
      gd.getLog().failToDeleteTx(this, msg);
    }
  } // protected void delete(gd)

  // Mark a tx as failed
  protected boolean setFailed(GrouperDaemon gd) {
    boolean rv = false;
    try {
      this.setStatus( QueueStatus.getInstance("fail") );
      HibernateHelper.save(this);
      rv = true;
    }
    catch (HibernateException eH) {
      gd.getLog().error(eH.getMessage());
    }
    return rv;
  } // protected boolean setFailed(gd)


  // Hibernate Accessors
  protected Member getActor() {
    return this.actor;
  }
  protected List getDirty() {
    return this.dirty;
  }
  protected Factor getFactor() {
    return this.factor;
  }
  protected Field getField() {
    return this.field;
  }
  protected String getId() {
    return this.id;
  }
  protected String getKlass() {
    return this.klass;
  }
  protected Member getMember() {
    return this.member;
  }
  protected Owner getOwner() {
    return this.owner;
  }
  protected long getQueueTime() {
    return this.queueTime;
  }
  protected String getSessionId() {
    return this.sessionId;
  }
  protected QueueStatus getStatus() {
    return this.status;
  }
  protected String getUuid() {
    return this.uuid;
  }

  protected void setActor(Member actor) {
    this.actor = actor;
  }
  protected void setDirty(List dirty) {
    this.dirty = dirty;
  }
  protected void setFactor(Factor f) {
    this.factor = f;
  }
  protected void setField(Field f) {
    this.field = f;
  }
  protected void setId(String id) {
    this.id = id;
  }
  protected void setKlass(String klass) {
    this.klass = klass;
  }
  protected void setMember(Member m) {
    this.member = m;
  }
  protected void setOwner(Owner owner) {
    this.owner = owner;
  }
  protected void setQueueTime(long time) {
    this.queueTime = time;
  }
  protected void setSessionId(String id) {
    this.sessionId = id;
  }
  protected void setStatus(QueueStatus s) {
    this.status = s;
  }
  protected void setUuid(String uuid) {
    this.uuid = uuid;
  }

}

