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
 * Group math factors.
 * @author blair christensen.
 *     
*/
class Factor implements Serializable {

  // Private Class Constants
  private static final Log LOG = LogFactory.getLog(Factor.class);

  
  // Hibernate Properties
  private String      id;
  private String      klass;
  private Member      creator_id;
  private long        create_time;
  private Membership  node_a;
  private Membership  node_b;
  private Status      status;
  private String      uuid;


  // Constructors

  // For Hibernate
  public Factor() { 
    super();
  }


  // Hibernate Accessors
  private String getId() {
    return this.getId();
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

  private Member getCreator_id() {
    return this.creator_id;
  }
  private void setCreator_id(Member m) {
    this.creator_id = m;
  }

  private long getCreate_time() {
    return this.create_time;
  }
  private void setCreate_time(long time) {
    this.create_time = time;
  }
 
  private Membership getNode_a() {
    return this.node_a;
  }
  private void setNode_a(Membership ms) {
    this.node_a = ms;
  }
 
  private Membership getNode_b() {
    return this.node_b;
  }
  private void setNode_b(Membership ms) {
    this.node_b = ms;
  }

  private Status getStatus() {
    return this.status;
  }
  private void setStatus(Status s) {
    this.status = s;
  }

  private String getUuid() {
    return this.uuid;
  }
  private void setUuid(String uuid) {
    this.uuid = uuid;
  }

}
