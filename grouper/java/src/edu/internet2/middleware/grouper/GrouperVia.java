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

import  org.apache.commons.lang.builder.ToStringBuilder;


/** 
 * Class representing a via relationship.
 * <p />
 * This class is only used internally.
 * <ul>
 * <li>TODO Is this class really needed?</li>
 * </ul>
 *
 * @author  blair christensen.
 * @version $Id: GrouperVia.java,v 1.7 2004-12-01 04:23:10 blair Exp $
 */
public class GrouperVia {

  /*
   * PRIVATE INSTANCE VARIABLES
   */
  private GrouperMember member;   // I am a member
  private GrouperGroup  group;    // Of this group
  private GrouperGroup  via;      // Via this group


  /*
   * CONSTRUCTORS
   */

  /**
   * Construct a new {@link GrouperVia} object.
   * <p />
   * TODO Is this class <b>really</b> needed?
   * TODO This should <b>only</b> be used within Grouper and I'd
   *      prefer to not be relying upon <i>protected</i> for that...
   */
  protected GrouperVia(GrouperMember member, GrouperGroup group, GrouperGroup via) {
    this._init();
    this.member = member;
    this.group  = group;
    this.via    = via;
  }


  /*
   * PUBLIC INSTANCE METHODS
   */

  /**
   * Return a string representation of the {@link GrouperVia} object.
   * <p />
   *
   * @return  String representation of the object.
   */
  public String toString() {
    return new ToStringBuilder(this)    .
      append("member",  this.member() ) .
      append("group",   this.group()  ) .
      append("via",     this.via()    ) .
      toString();
  }

  /*
   * PROTECTED INSTANCE METHODS
   */

  /**
   * TODO 
   */
  protected GrouperGroup group() {
    return this.group;
  }

  /**
   * TODO 
   */
  protected GrouperMember member() {
    return this.member;
  }

  /**
   * TODO 
   */
  protected GrouperGroup via() {
    return this.via;
  }

  /*
   * PRIVATE INSTANCE METHODS
   */
  private void _init() {
    this.member  = member;
    this.group   = group;
    this.via     = via;
  }

}

