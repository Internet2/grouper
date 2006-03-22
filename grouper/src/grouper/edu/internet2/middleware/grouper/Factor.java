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
 * Group math factors.
 * @author blair christensen.
 *     
*/
public class Factor extends Owner implements Serializable {

  // Private Class Constants
  private static final Log LOG = LogFactory.getLog(Factor.class);

  
  // Hibernate Properties
  private String      id;
  private String      klass;
  private Member      creator_id;
  private String      create_source;
  private long        create_time;
  private Owner       left;
  private Member      modifier_id;
  private String      modify_source;
  private long        modify_time;
  private String      owner_uuid;
  private Owner       right;


  // Constructors
  /** 
   * For Hibernate.
   */
  public Factor() { 
    super();
  }
  protected Factor(Group g, Factor f) {
    this.setCreator_id(   g.getSession().getMember()  );
    this.setLeft(         f.getLeft()                 );
    this.setRight(        f.getRight()                );
    this.setCreate_time(  new Date().getTime()        );
  } // protected Factor(g, f)


  // Public Instance Methods
  public String toString() {
    return new ToStringBuilder(this, ToStringStyle.SIMPLE_STYLE )
      .append("name"        , this.getClass().getName()         )
      .append("left"        , this.getLeft().toString()         )
      .append("right"       , this.getRight().toString()        )
      .toString();
  } // public String toString()

  
  // Hibernate Accessors
  private String getCreate_source() { 
    return this.create_source;
  }
  private long getCreate_time() {
    return this.create_time;
  }
  private Member getCreator_id() {
    return this.creator_id;
  }
  private String getId() {
    return this.id;
  }
  private String getKlass() {
    return this.klass;
  }
  protected Owner getLeft() {
    return this.left;
  }
  private Member getModifier_id() {
    return this.modifier_id;
  }
  private String getModify_source() {
    return this.modify_source;
  }
  private long getModify_time() {
    return this.modify_time;
  }
  protected Owner getRight() {
    return this.right;
  }

  private void setCreate_source(String source) {
    this.create_source = source;
  }
  private void setCreate_time(long time) {
    this.create_time = time;
  }
  private void setCreator_id(Member m) {
    this.creator_id = m;
  }
  private void setId(String id) {
    this.id = id;
  }
  private void setKlass(String klass) {
    this.klass = klass;
  }
  protected void setLeft(Owner left) {
    this.left = left;
  }
  private void setModifier_id(Member m) {
    this.modifier_id = m;
  }
  private void setModify_source(String source) {
    this.modify_source = source;
  }
  private void setModify_time(long time) {
    this.modify_time = time;
  }
  protected void setRight(Owner right) {
    this.right = right;
  }

}

