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
import  java.util.*;
import  net.sf.hibernate.*;


/** 
 * Internal class for performing Hibernate queries.
 * <p />
 * All methods are class methods and most are restricted to use within
 * {@link Grouper}.
 *
 * @author  blair christensen.
 * @version $Id: BackendQuery.java,v 1.7 2005-03-09 05:02:18 blair Exp $
 */
public class BackendQuery {

  /*
   * PROTECTED CLASS METHODS
   */

  /*
   * Return all items in a Hibernate-mapped table.
   */
  protected static List all(Session sess, String klass) {
    List vals = new ArrayList();
    try { 
      Query q = sess.createQuery("FROM " + klass);
      Grouper.log().query("all", q);
      vals    = q.list();
    } catch (HibernateException e) {
      throw new RuntimeException(e);
    }
    return vals;
  }

  protected static List grouperAttr(
                          Session sess, String key, String field
                        )
  {
    List vals = new ArrayList();
    try {
      Query q = sess.createQuery(
        "FROM GrouperAttribute AS ga"   +
        " WHERE "                       +
        "ga.groupKey='"   + key   + "'" +
        " AND "                         +
        "ga.groupField='" + field + "'"
      );          
      Grouper.log().query("grouperAttr ", q);
      vals = q.list();
    } catch (HibernateException e) {
      throw new RuntimeException(e);
    }
    return vals;
  }

  /*
   * Return matching items from {@link GrouperList}-mapped table.
   */
  protected static List grouperList(
                          Session sess, String gkey,
                          String  mkey,    String gfield,
                          String  via
                        )
  {
    List    vals        = new ArrayList();
    String  gfield_txt  = nullOrVal(gfield);
    String  gkey_txt    = nullOrVal(gkey);
    String  mkey_txt    = nullOrVal(mkey);
    String  via_txt     = "";
    if (via != null) {
      if        (via.equals(Grouper.MEM_ALL)) {        
        // Already set via_txt is fine
      } else if (via.equals(Grouper.MEM_EFF)) {
        // We want a value
        via_txt = " AND gl.via IS NOT NULL";
      } else if (via.equals(Grouper.MEM_IMM)) {
        // We don't want a value
        via_txt = " AND gl.via IS NULL";
      } else {
        via_txt = " AND gl.via" + nullOrVal(via);
      }
    }
    try {
      Query q = sess.createQuery(
        "FROM GrouperList AS gl"      +
        " WHERE "                     +
        "gl.groupKey"   + gkey_txt    +
        " AND "                       +
        "gl.memberKey"  + mkey_txt    +
        " AND "                       +
        "gl.groupField" + gfield_txt  +
        via_txt
      );
      Grouper.log().query("grouperList", q);
      vals = q.list();
    } catch (HibernateException e) {
      throw new RuntimeException(e);
    }
    return vals;
  }

  /*
   * Return all items matching key=value specification.
   */
  protected static List kv(
                           Session sess, String klass, 
                           String key, String value
                         ) 
  {
    List vals = new ArrayList();
    try {
      Query q = sess.createQuery(
        "FROM " + klass + " WHERE " + key + "='" + value + "'"
      );
      Grouper.log().query("kv", q);
      vals = q.list();
    } catch (HibernateException e) {
      throw new RuntimeException(e);
    }
    return vals;
  }

  /* (!javadoc)
   * Query for values greater than the specified value.
   */
  protected static List kvgt(
                          Session sess, String klass, 
                          String  key,     String time
                        )
  {
    List vals = new ArrayList();
    try {
      Query q = sess.createQuery(
        "FROM " + klass + " WHERE "           +
        key     + " > " + time
      );
      Grouper.log().query("kvgt", q);
      vals = q.list();
    } catch (HibernateException e) {
      throw new RuntimeException(e);
    }
    return vals;
  }

  /*
   * Return all items matching two key=value specifications.
   */
  protected static List kvkv(
                          Session sess, String klass, 
                          String key0, String value0,
                          String key1, String value1
                        ) 
  {
    List vals = new ArrayList();
    try {
      Query q = sess.createQuery(
        "FROM " + klass + " WHERE "           +
        key0    + "='"  + value0  + "' AND "  +
        key1    + "='"  + value1  + "'"
      );
      Grouper.log().query("kvkv", q);
      vals = q.list();
    } catch (HibernateException e) {
      throw new RuntimeException(e);
    }
    return vals;
  }

  /* (!javadoc)
   * Query for values less than the specified value.
   */
  protected static List kvlt(
                          Session sess, String klass, 
                          String  key,     String time
                        )
  {
    List vals = new ArrayList();
    try {
      Query q = sess.createQuery(
        "FROM " + klass + " WHERE "           +
        key     + " < " + time
      );
      Grouper.log().query("kvlt", q);
      vals = q.list();
    } catch (HibernateException e) {
      throw new RuntimeException(e);
    }
    return vals;
  }


  /*
   * PRIVATE CLASS METHODS
   */

  /*
   * Return string value suitable for insertion into a query.
   */
  private static String nullOrVal(String val) {
    if        (val == null)                             {
      // FIXME Should I allow this?
      val = " IS NOT NULL";
    } else if (val.equals(GrouperBackend.VAL_NULL))     {
      val = " IS NULL";
    } else if (val.equals(GrouperBackend.VAL_NOTNULL))  {
      val = " IS NOT NULL";
    } else {
      val = "='" + val + "'";
    }
    return val;
  }

}
 
