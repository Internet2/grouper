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


import  java.io.Serializable;
import  java.util.*;
import  net.sf.hibernate.*;
import  org.apache.commons.lang.builder.EqualsBuilder;
import  org.apache.commons.lang.builder.HashCodeBuilder;


/** 
 * Class modeling a {@link GrouperGroup} type definition relationship.
 * <p />
 *
 * @author  blair christensen.
 * @version $Id: GrouperTypeDef.java,v 1.21 2005-03-29 17:32:02 blair Exp $
 */
public class GrouperTypeDef implements Serializable {

  /*
   * PRIVATE INSTANCE VARIABLES
   */
  private String groupType;
  private String groupField;


  /*
   * CONSTRUCTORS
   */

  /**
   * Null-argument constructor for Hibernate.
   */
  public GrouperTypeDef() {
    // Nothing
  }


  /*
   * PUBLIC INSTANCE METHODS
   */

  /**
   * Compares the specified object with this type definition for equality.
   * <p />
   * @param o Object to be compared for equality with this type
   *   definition.
   * @return  True if the specified object is equal to this type
   *   definition.
   */
  public boolean equals(Object o) {
     return EqualsBuilder.reflectionEquals(this, o);
   }

  /**
   * Retrieve the {@link Group} field.
   * <p />
   * @return  Name of group field.
   */
  public String field() {
    return this.getGroupField();
  }

  /**
   * Return the hash code value for this type definition.
   * <p />
   * @return  The hash code value for this type definition.
   */
  public int hashCode() {
     return HashCodeBuilder.reflectionHashCode(this);
   }

  /**
   * Return a string representation of this object.
   * <p />
   * @return String representation of this object.
   */
  public String toString() {
    return this.getGroupType()  + ":" + this.getGroupField();
  }

  /**
   * Retrieve the {@link Group} type.
   * <p />
   * @return  Type of group.
   */
  public String type() {
    return this.getGroupType();
  }


  /*
   * PROTECTED CLASS METHODS
   */

  /*
   * @return List of all type definitions
   */
  protected static List all(DbSess dbSess) {
    String  qry   = "GrouperTypeDef.all";
    List    vals  = new ArrayList();
    try {
      Query q = dbSess.session().getNamedQuery(qry);
      try {
        vals = q.list();
      } catch (HibernateException e) {
        throw new RuntimeException(
                    "Error retrieving results for " + qry + ": " + e
                  );
      }
    } catch (HibernateException e) {
      throw new RuntimeException(
                  "Unable to get query " + qry + ": " + e
                );
    }
    return vals;
  }


  /*
   * HIBERNATE
   */

  private String getGroupType() {
    return this.groupType;
  }

  private void setGroupType(String groupType) {
    this.groupType = groupType;
  }

  private String getGroupField() {
    return this.groupField;
  }

  private void setGroupField(String groupField) {
    this.groupField = groupField;
  }

}

