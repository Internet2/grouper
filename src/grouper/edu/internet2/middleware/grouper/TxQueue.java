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
 * Grouper Transaction Queue.
 * @author blair christensen.
 *     
*/
class TxQueue implements Serializable {

  // Private Class Constants
  private static final Log LOG = LogFactory.getLog(TxQueue.class);

  
  // Hibernate Properties
  private List        args        = new ArrayList();
  private List        dirty       = new ArrayList();
  private String      id;
  private String      klass;
  private Member      member;
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
  public String toString() {
    String k = this.getKlass();
    return new ToStringBuilder(this)
      .append("time"        , this.getQueueTime()         )
      .append("uuid"        , this.getUuid()              )
      .append("status"      , this.getStatus().getName()  )
      .append("class"       , this.getClass().getName()   )
      .toString()
      ;
  } // public String toString() 

  // Hibernate Accessors
  private List getArgs() {
    return this.args;
  }
  
  private void setArgs(List args) {
    this.args = args;
  }

  private List getDirty() {
    return this.dirty;
  }
  
  private void setDirty(List dirty) {
    this.dirty = dirty;
  }

  private String getId() {
    return this.id;
  }
  private void setId(String id) {
    this.id = id;
  }

  private String getKlass() {
    return this.klass;
  }
  private void setKlass(String klass) {
    this.klass = klass;
  }

  private Member getMember() {
    return this.member;
  }
  private void setMember(Member m) {
    this.member = m;
  }

  private long getQueueTime() {
    return this.queueTime;
  }
  private void setQueueTime(long time) {
    this.queueTime = time;
  }
 
  private String getSessionId() {
    return this.sessionId;
  }

  private void setSessionId(String id) {
    this.sessionId = id;
  }

  private QueueStatus getStatus() {
    return this.status;
  }
  private void setStatus(QueueStatus s) {
    this.status = s;
  }

  private String getUuid() {
    return this.uuid;
  }
  private void setUuid(String uuid) {
    this.uuid = uuid;
  }

}

