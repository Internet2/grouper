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
 * A group or namespace attribute within the Groups Registry.
 * <p />
 *
 * @author  blair christensen.
 * @version $Id: GrouperAttribute.java,v 1.36 2005-09-07 18:58:15 blair Exp $
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
    // Nothing
  }

  /* 
   * Create a new and populated attribute object.
   */
  protected GrouperAttribute(String key, String field, String value) {
    this.groupKey         = key;
    this.groupField       = field; 
    this.groupFieldValue  = value;
  }


  /*
   * PROTECTED CLASS METHODS
   */

  /*
   * Return group's attributes as a map
   */
  protected static Map attributes(GrouperSession s, Group g) {
    String  qry         = "GrouperAttribute.by.key";
    Map     attributes  = new HashMap();
    try {
      Query q = s.dbSess().session().getNamedQuery(qry);
      q.setString(0, g.key());
      try {
        Iterator iter = q.list().iterator();
        while (iter.hasNext()) {
          GrouperAttribute attr = (GrouperAttribute) iter.next();
          try {
            s.canReadField(g, attr.field());
            attributes.put(attr.field(), attr);
          } catch (InsufficientPrivilegeException e) {
            // Ignore
          }
        }
      } catch (HibernateException e) {
        throw new RuntimeException(
           "Error retrieving results for " + qry + ": " + e.getMessage()
         );
      }
    } catch (HibernateException e) {
      throw new RuntimeException(
        "Unable to get query " + qry + ": " + e.getMessage()
      );
    }
    return attributes;
  }

  /*
   * Delete all of a group's attributes.
   */
  protected static void delete(GrouperSession s, Group g) {
    String qry = "GrouperAttribute.by.key";
    try {
      Query q = s.dbSess().session().getNamedQuery(qry);
      q.setString(0, g.key());
      try {
        Iterator iter = q.list().iterator();
        while (iter.hasNext()) {
          GrouperAttribute attr = (GrouperAttribute) iter.next();
          attr.delete(s);
          // BDC GrouperAttribute.delete(s, attr);
        }
      } catch (HibernateException e) {
        throw new RuntimeException(
                    "Error retrieving results for " + qry + ": " + e
                  );
      }
    } catch (HibernateException e) {
      throw new RuntimeException(
                  "Unable to get query " + qry + ": " + e);
    }
                
  }

  /*
   * Retrieve an attribute value by group key
   */
  protected static String getAttrValByKey(
    GrouperSession s, String key, String field
  ) 
  {
    String qry = "GrouperAttribute.string.value.by.group.key";
    try {
      Query q = s.dbSess().session().getNamedQuery(qry);
      q.setString(0, key);    // Search for this key
      q.setString(1, field);  // And this field value
      try {
        return (String) q.uniqueResult();
      } catch (HibernateException e) {
        throw new RuntimeException(
          "Error retrieving results for " + qry + ": " + e.getMessage()
        );
      }
    } catch (HibernateException e) {
      throw new RuntimeException(
        "Unable to get query " + qry + ": " + e.getMessage()
      );
    }
  }

  /*
   * Save an attribute in the Groups Registry.
   */
  protected static void save(GrouperSession s, GrouperAttribute attr) {
    String qry   = "GrouperAttribute.by.key.and.field";
    List   vals  = new ArrayList();
    try {
      Query q = s.dbSess().session().getNamedQuery(qry);
      q.setString(0, attr.key());
      q.setString(1, attr.field());
      try {
        vals = q.list();
        if (vals.size() == 0) {
          // We've got a new one: save it.
          try {
            s.dbSess().session().save(attr);   
          } catch (HibernateException e) {
            throw new RuntimeException(
              "Error saving attribute " + attr + ": " + e.getMessage()
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
                "Error updating attribute " + attr + ": " + 
                e.getMessage()
              );
            }
          } else {
            // FIXME ???
          }
        } 
      } catch (HibernateException e) {
        throw new RuntimeException(
          "Error retrieving results for " + qry + ": " + e.getMessage()
        );
      }
    } catch (HibernateException e) {
      throw new RuntimeException(e.getMessage());
    }
  }


  /*
   * PUBLIC INSTANCE METHODS
   */

  /**
   * Compare the specified object with this attribute for equality.
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
   * Return the hash code value for this attribute.
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

  /*
   * Delete this attribute.
   * <p />
   * TODO Should this become public and become the proper way of
   * deleting an attribute?  Rather than relying upon null or ""
   * values?
   */
  protected void delete(GrouperSession s) {
    try {
      s.dbSess().session().delete(this);
    } catch (HibernateException e) {
      throw new RuntimeException(
                  "Error deleting attribute " + this + ": " + e
                );
    }
  }

  /*
   * Return the group key for this attribute.
   */
  protected String key() {
    return this.getGroupKey();
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

  /*
   * Life is just easier this way.
   */
  protected void setGroupFieldValue(String groupFieldValue) {
    this.groupFieldValue = groupFieldValue;
  }

}

