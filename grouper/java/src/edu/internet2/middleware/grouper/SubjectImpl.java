/*
 * Copyright (C) 2004 University Corporation for Advanced Internet Development, Inc.
 * Copyright (C) 2004 The University Of Chicago
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
import  edu.internet2.middleware.subject.*;
import  java.io.Serializable;
import  org.apache.commons.lang.builder.EqualsBuilder;
import  org.apache.commons.lang.builder.HashCodeBuilder;


/** 
 * Implementation of the I2MI {@link Subject} interface.
 *
 * @author  blair christensen.
 * @version $Id: SubjectImpl.java,v 1.11 2004-11-30 01:36:26 blair Exp $
 */
public class SubjectImpl 
  implements Serializable,Subject
{

  /*
   * PRIVATE INSTANCE VARIABLES
   */
  private String id;
  private String typeID;


  /* 
   * CONSTRUCTORS
   */

  /*
   * Null-argument constructor for Hibernate.
   */
  public SubjectImpl() {
    this._init();
  }

  /**
   * Construct a new {@link SubjectImpl} object.
   * <p />
   * TODO This should <b>only</b> be used within Grouper and I'd
   *      prefer to not be relying upon <i>protected</i> for that...
   */
  protected SubjectImpl(String id, String typeID) {
    this._init();
    this.id     = id;
    this.typeID = typeID;
  }


  /*
   * PUBLIC INSTANCE METHODS
   */

  public void addAttribute(String name, String value) {
    // XXX Nothing -- Yet
  }

  public String[] getAttributeArray(String name) {
    return null; 
  }

  public String getDescription() {
    return null;
  }

  public String getDisplayId() {
    return null; 
  }

  public String getId() {
    return this.getSubjectID();
  }

  public String getName() {
    return null; 
  }

  public SubjectType getSubjectType()  {
    return Grouper.subjectType(this.typeID);
  }

  public String toString() {
    return this.getClass().getName()  + ":" +
           this.getSubjectType()      + ":" +
           this.getId();
  }


  /*
   * PRIVATE INSTANCE METHODS
   */
  private void _init() {
    this.id     = id;
    this.typeID = typeID;
  }


  /*
   * HIBERNATE
   */

  private String getSubjectID() {
    return this.id;
  }

  private void setSubjectID(String id) {
    this.id = id;
  }

  private String getSubjectTypeID() {
    return this.typeID;
  }

  private void setSubjectTypeID(String typeID) {
    this.typeID = typeID;
  }

}
