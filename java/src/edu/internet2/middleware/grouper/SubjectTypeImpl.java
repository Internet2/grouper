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
import  java.lang.reflect.*;


/** 
 * Default implementation of the I2MI {@link SubjectType} interface.
 * <p />
 *
 * @author  blair christensen.
 * @version $Id: SubjectTypeImpl.java,v 1.12 2004-12-09 01:28:38 blair Exp $
 */
public class SubjectTypeImpl implements SubjectType {

  /*
   * PRIVATE INSTANCE VARIABLES
   */
  private String adapterClass;
  private String name;
  private String typeID;


  /*
   * CONSTRUCTORS
   */

  /**
   * Create a {@link SubjectTypeImpl} object.
   */
  public SubjectTypeImpl() {
    // Nothing -- Yet
  }


  /*
   * PUBLIC INSTANCE METHODS 
   */

  /**
   * Return an instance of the {@link SubjectTypeAdapter} class for
   * this {@link SubjectType}.
   *
   * @return {@link SubjectTypeAdapter} object.
   */
  public SubjectTypeAdapter getAdapter() {
    if (this.getAdapterClass() != null) {
      // Attempt to reflectively create an instance of the
      // appropriate subject type adapter
      try {
        Class classType     = Class.forName( this.getAdapterClass() );
        Class[] paramsClass = new Class[] { };
        Constructor con     = classType.getDeclaredConstructor(paramsClass);
        Object[] params     = new Object[] { };
        return (SubjectTypeAdapter) con.newInstance(params);
      } catch (Exception e) {
        // TODO Well, this is blatantly the wrong thing to do
        System.err.println(e);
        System.exit(1);
      }
    }
    return null;
  }

  /**
   * Return subject type ID.
   *
   * @return ID of type.
   */

  public String getId() {
    return this.getSubjectTypeID();
  }

  /**
   * Return subject type name.
   *
   * @return  Name of type.
   */
  public String getName() {
    return this.name;
  }

  /**
   * Return a string representation of this object.
   * <p />
   * @return String representation of this object.
   */
  public String toString() {
    return this.getClass().getName()  + ":" +
           this.getId()               + ":" +
           this.getName()             + ":" +
           this.getAdapter();
  }


  /*
   * HIBERNATE
   */

  private String getAdapterClass() {
    return this.adapterClass;
  }

  private void setAdapterClass(String adapterClass) {
    this.adapterClass = adapterClass;
  }

  private String getSubjectTypeID() {
    return this.typeID;
  }

  private void setSubjectTypeID(String id) {
    this.typeID = id;
  }

  /*
   * Public due to `getName' being specified in the `SubjectType'
   * interface.
   *
   * private String getName() { ... }
   */

  private void setName(String name) {
    this.name = name;
  }

}

