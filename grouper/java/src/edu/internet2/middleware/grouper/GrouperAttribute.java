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
import  org.apache.commons.lang.builder.ToStringBuilder;


/** 
 * Class modeling a {@link GrouperGroup} attribute.
 * <p />
 *
 * @author  blair christensen.
 * @version $Id: GrouperAttribute.java,v 1.24 2005-03-22 20:56:15 blair Exp $
 */
public class GrouperAttribute implements Serializable {

  /*
   * PRIVATE INSTANCE VARIABLES
   */
  private String  groupKey;
  private String  groupField;
  private String  groupFieldValue;


  /*
   * CONSTRUCTORS
   */

  /**
   * Null-argument constructor for Hibernate.
   */
  public GrouperAttribute() {
    this._init();
  }

  /* (!javadoc)
   * TODO This should <b>only</b> be used within Grouper and I'd
   *      prefer to not be relying upon <i>protected</i> for that...
   */
  protected GrouperAttribute(String key, String field, String value) {
    this._init();
    this.groupKey         = key;
    this.groupField       = field;
    this.groupFieldValue  = value;
  }


  /*
   * PROTECTED CLASS METHODS
   */

  protected static void delete(GrouperSession s, GrouperAttribute attr) {
    try {
      s.dbSess().session().delete(attr);
    } catch (HibernateException e) {
      throw new RuntimeException(
                  "Error deleting attribute " + attr + ": " + e
                );
    }
  }

  /*
   * Save an attribute in the groups registry.
   */
  protected static void save(GrouperSession s, GrouperAttribute attr) {
    String            qry   = "GrouperAttribute.by.key.and.value";
    List              vals  = new ArrayList();
    try {
      Query q = s.dbSess().session().getNamedQuery(qry);
      q.setString(0, attr.key());
      q.setString(1, attr.value());
      try {
        vals = q.list();
        if (vals.size() == 0) {
          // We've got a new one: save it.
          try {
            s.dbSess().session().save(attr);   
          } catch (HibernateException e) {
            throw new RuntimeException(
                        "Error saving attribute " + attr + ": " + e
                      );
          }
        } else if (vals.size() == 1) {
          // Attribute already exists.  Check to see if the value has
          // changed.
          GrouperAttribute cur = (GrouperAttribute) vals.get(0);
          if (!attr.value().equals(cur.value())) {
            try {
              s.dbSess().session().update(attr);
            } catch (HibernateException e) {
              throw new RuntimeException(
                          "Error updating attribute " + attr + ": " + e
                        );
            }
          }
        } 
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
  }


  /*
   * PUBLIC INSTANCE METHODS
   */

  /**
   * Compares the specified object with this attribute for equality.
   * <p />
   * @param o Object to be compared for equality with this attribute.
   * @return  True if the specified object is equal to this attribute.
   */
  public boolean equals(Object o) {
     return EqualsBuilder.reflectionEquals(this, o);
   }

  /**
   * Retrieve this attribute's group field.
   * <p />
   * @return  Attribute's group field.
   */
  public String field() {
    return this.getGroupField();
  }

  /**
   * Returns the hash code value for this attribute.
   * <p />
   * @return  The hash code value for this attribute.
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
    return new ToStringBuilder(this).
      append("field", this.getGroupField()).
      append("value", this.getGroupFieldValue()).
      toString();
  }

  /**
   * Retrieve this attribute's value.
   * <p />
   * @return  Attribute's value.
   */
  public String value() {
    return this.getGroupFieldValue();
  }


  /*
   * PROTECTED INSTANCE METHODS
   */
  protected String key() {
    // TODO This does expose the group key.  Do we want that?
    return this.getGroupKey();
  }


  /*
   * PRIVATE INSTANCE METHODS
   */

  /*
   * Initialize instance variables
   */
  private void _init() {
    this.groupKey        = null;
    this.groupField      = null;
    this.groupFieldValue = null;
  }


  /*
   * HIBERNATE
   */

  private String getGroupKey() {
    return this.groupKey;
  }

  private void setGroupKey(String groupKey) {
    this.groupKey = groupKey;
  }

  private String getGroupField() {
    return this.groupField;
  }

  private void setGroupField(String groupField) {
    this.groupField = groupField;
  }

  private String getGroupFieldValue() {
    return this.groupFieldValue;
  }

  private void setGroupFieldValue(String groupFieldValue) {
    this.groupFieldValue = groupFieldValue;
  }

}

