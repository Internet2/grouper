/*
 * Copyright (C) 2004-2005 University Corporation for Advanced Internet Development, Inc.
 * Copyright (C) 2004-2005 The University Of Chicago
 * All Rights Reserved. 
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 *  * Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *  * Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *  * Neither the name of the University of Chicago nor the names
 *    of its contributors nor the University Corporation for Advanced
 *   Internet Development, Inc. may be used to endorse or promote
 *   products derived from this software without explicit prior
 *   written permission.
 *
 * You are under no obligation whatsoever to provide any enhancements
 * to the University of Chicago, its contributors, or the University
 * Corporation for Advanced Internet Development, Inc.  If you choose
 * to provide your enhancements, or if you choose to otherwise publish
 * or distribute your enhancements, in source code form without
 * contemporaneously requiring end users to enter into a separate
 * written license agreement for such enhancements, then you thereby
 * grant the University of Chicago, its contributors, and the University
 * Corporation for Advanced Internet Development, Inc. a non-exclusive,
 * royalty-free, perpetual license to install, use, modify, prepare
 * derivative works, incorporate into the software or other computer
 * software, distribute, and sublicense your enhancements or derivative
 * works thereof, in binary and source code form.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND WITH ALL FAULTS.  ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING,
 * BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE, AND NON-INFRINGEMENT ARE DISCLAIMED AND the
 * entire risk of satisfactory quality, performance, accuracy, and effort
 * is with LICENSEE. IN NO EVENT SHALL THE COPYRIGHT OWNER, CONTRIBUTORS,
 * OR THE UNIVERSITY CORPORATION FOR ADVANCED INTERNET DEVELOPMENT, INC.
 * BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR
 * BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OR DISTRIBUTION OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package edu.internet2.middleware.grouper;

import  edu.internet2.middleware.grouper.*;
import  net.sf.hibernate.*;
import  org.apache.commons.lang.builder.ToStringBuilder;


/** 
 * Class modeling a {@link Grouper} via path element.
 * <p />
 *
 * @author  blair christensen.
 * @version $Id: MemberVia.java,v 1.3 2005-03-16 22:54:08 blair Exp $
 */
public class MemberVia  {

  /*
   * PRIVATE INSTANCE VARIABLES
   */
  private String  chainKey;
  private int     chainIdx;
  private String  listKey;


  /*
   * CONSTRUCTORS
   */

  public MemberVia() {
    this._init();
  }
  protected MemberVia(GrouperList lv) {
    this._init();
    // this.setPathKey( new GrouperUUID().toString() );
    this.setListKey( lv.group().key() );
  }
  protected void key(String key) {
    this.chainKey = key;
  }
  protected void idx(int idx) {
    this.chainIdx = idx;
  }
  protected void save(GrouperSession s) {
    // TODO I should validate our state first
    try {
      s.dbSess().session().save(this);
    } catch (HibernateException e) {
      throw new RuntimeException(
                  "Error saving chain element " + this + ": " + e
                );
    }   
  }


  /*
   * PUBLIC INSTANCE METHODS
   */

  /**
   * Return a string representation of this object.
   * <p />
   * @return String representation of this object.
   */
  public String toString() {
    return new ToStringBuilder(this)      .
      append("chain", this.getChainKey()) .
      append("index", this.getChainIdx()) .
      append("list",  this.getListKey())  .
      toString();
  }

  /*
   * PRIVATE INSTANCE METHODS
   */

  /*
   * Initialize instance variables
   */
  private void _init() {
    this.chainKey = null;
    this.chainIdx = -1;
    this.listKey  = null;
  }


  /*
   * PROTECTED INSTANCE METHODS
   */
  protected String key() {
    return this.getChainKey();
  }


  /*
   * HIBERNATE
   */

  private String getChainKey() {
    return this.chainKey;
  }

  private void setChainKey(String key) {
    this.chainKey = key;
  } 

  private int getChainIdx() {
    return this.chainIdx;
  }

  private void setChainIdx(int idx) {
    this.chainIdx = idx;
  } 

  private String getListKey() {
    return this.listKey;
  }

  private void setListKey(String key) {
    this.listKey = key;
  } 

}

