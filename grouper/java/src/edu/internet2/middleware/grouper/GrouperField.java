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


import  java.util.*;
import  net.sf.hibernate.*;
import  org.apache.commons.logging.Log;
import  org.apache.commons.logging.LogFactory;


/** 
 * Class modeling a {@link GrouperGroup} field.
 * <p />
 *
 * @author  blair christensen.
 * @version $Id: GrouperField.java,v 1.25 2005-07-18 19:09:05 blair Exp $
 */
public class GrouperField implements Comparable {

  /*
   * PRIVATE CLASS VARIABLES
   */
  private static boolean  initialized = false;
  private static Log      log         = LogFactory.getLog(GrouperField.class);
  private static List     valL        = new ArrayList();
  private static Map      valM        = new HashMap();



  /*
   * PRIVATE INSTANCE VARIABLES
   */
  private String groupField;
  private String readPriv;
  private String writePriv;
  private String isList;


  /*
   * CONSTRUCTORS
   */

  /**
   * Null-argument constructor for Hibernate.
   */
  public GrouperField() {
    // Nothing
  }


  /*
   * PUBLIC INSTANCE METHODS
   */

  /**
   * Sort {@link GrouperField} objects by field name.
   * <p />
   */
  public int compareTo(Object anotherField) throws ClassCastException {
    if (!(anotherField instanceof GrouperField))  {
      throw new ClassCastException("GrouperField object expected.");
    }
    String fieldA = this.getGroupField();
    String fieldB = ( (GrouperField) anotherField ).getGroupField();
    return ( (String) fieldA ).compareTo( (String) fieldB  );
  }

  /** 
   * Return the name of the group field.
   * <p />
   * @return Field name.
   */
  public String field() {
    return this.getGroupField();
  }

  /**
   * Return whether this is a list field or not.
   * <p />
   * @return Boolean true if this is a list field.
   */
  public boolean isList() {
    boolean rv = false;
    if (this.getIsList().equals("TRUE")) {
      rv = true;
    }
    return rv;
  }

  /**
   * Return read privilege for this field.
   * <p />
   * @return Read privilege.
   */
  public String readPriv() {
    return this.getReadPriv();
  }

  /**
   * Return a string representation of this object.
   * <p />
   * @return String representation of this object.
   */
  public String toString() {
    return  this.getGroupField()  + ":" + 
            this.getReadPriv()    + ":" +
            this.getWritePriv()   + ":" +
            this.getIsList();
  }

  /**
   * Return write privilege for this field.
   * <p />
   * @return Write privilege.
   */
  public String writePriv() {
    return this.getWritePriv();
  }


  /*
   * PROTECTED CLASS METHODS
   */

  /*
   * Don't even get me started on the discrepancy between _all()_ and
   * _field()_.
   * @return List of all group fields
   */
  protected static List all(DbSess dbSess) {
    GrouperField.getFields(dbSess);
    log.debug("Returning all fields");
    return valL;
  }

  /*
   * Don't even get me started on the discrepancy between _all()_ and
   * _field()_.
   * @return {@link GrouperField} object
   * TODO Make public?
   */
  protected static GrouperField field(String field) {
    if (initialized == false) {
      GrouperField.getFields(GrouperSession.getRootSession().dbSess());
    }
    if (valM.containsKey(field)) {
      log.debug("Returning field " + field); 
      return (GrouperField) valM.get(field); 
    }
    log.debug("Unknown field: " + field);
    throw new RuntimeException("Field '" + field + "' is unknown");
  }


  /*
   * PRIVATE CLASS METHODS
   */

  /*
   * Cache all group fields in List and Map stashes
   */
  private static void getFields(DbSess dbSess) {
    if (initialized == false) {
      log.info("Building cached field list");
      String  qry   = "GrouperField.all";
      try {
        Query q = dbSess.session().getNamedQuery(qry);
        try {
          Iterator iter = q.list().iterator();
          while (iter.hasNext()) {
            GrouperField f = (GrouperField) iter.next();
            valL.add(f);  // So I don't have to bother casting later
            valM.put( f.field(), f );
            log.info("Cached field " + f);
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
      initialized = true;
    }
  }


  /*
   * HIBERNATE
   */

  private String getGroupField() {
    return this.groupField;
  }

  private void setGroupField(String groupField) {
    this.groupField = groupField;
  }

  private String getReadPriv() {
    return this.readPriv;
  }

  private void setReadPriv(String readPriv) {
    this.readPriv = readPriv;
  }

  private String getWritePriv() {
    return this.writePriv;
  }

  private void setWritePriv(String writePriv) {
    this.writePriv = writePriv;
  }

  private String getIsList() {
    return this.isList;
  }

  private void setIsList(String isList) {
    this.isList = isList;
  }

}

