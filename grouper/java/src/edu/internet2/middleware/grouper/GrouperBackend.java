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
import  edu.internet2.middleware.subject.*;
import  java.io.*;
import  java.sql.*;
import  java.util.*;
import  net.sf.hibernate.*;


/** 
 * Internal class providing more direct access to the groups registry
 * for queries and updates.
 * <p />
 *
 * @author  blair christensen.
 * @version $Id: GrouperBackend.java,v 1.200 2005-03-23 22:29:50 blair Exp $
 */
public class GrouperBackend {

  /**
   * Query for all of a group's attributes.
   * <p />
   *
   * @param g Group object
   * @return List of a {@link GrouperAttribute} objects.
   */
  protected static List attributes(GrouperSession s, GrouperGroup g) {
    String  qry   = "GrouperAttribute.by.key";
    List    vals  = new ArrayList();
    try {
      Query q = s.dbSess().session().getNamedQuery(qry);
      q.setString(0, g.key());
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

  /**
   * Query a group's schema.
   *
   * @param g Group object
   * @return List of a group's schema
   */
  protected static List schemas(GrouperSession s, GrouperGroup g) {
    String  qry   = "GrouperSchema.by.key";
    List    vals  = new ArrayList();
    try {
      Query q = s.dbSess().session().getNamedQuery(qry);
      q.setString(0, g.key());
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

  protected static boolean attrDel(
                           GrouperSession s, String key, String field
                         ) 
  {
    String  qry   = "GrouperAttribute.by.key.and.value";
    boolean rv    = false;
    List    vals  = new ArrayList();
    try {
      Query q = s.dbSess().session().getNamedQuery(qry);
      q.setString(0, key);
      q.setString(1, field);
      try {
        vals = q.list();
        if (vals.size() == 1) {
          try {
            GrouperAttribute attr = (GrouperAttribute) vals.get(0);
            s.dbSess().session().delete(attr);
            rv = true;
          } catch (HibernateException e) {
            throw new RuntimeException(
                        "Error deleting attribute: " + e
                      );
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
    return rv;
  }

  /* (!javadoc)
   * Attach attributes to a group.
   * FIXME Won't calling g.attribute(...) eventually cause the group's
   *       modify attrs to be updated every time this group is loaded?
   *      
   *       But perhaps the `initialized' hack that I added for another
   *       reason will work?
   */
  protected static boolean _groupAttachAttrs(GrouperSession s, GrouperGroup g) {
    boolean rv = false;
    if (g != null) {
      Iterator iter = GrouperBackend.attributes(s, g).iterator();
      while (iter.hasNext()) {
        GrouperAttribute attr = (GrouperAttribute) iter.next();
        g.attribute( attr.field(), attr );
        rv = true;
      }
    }
    return rv;
  }

  /* (!javadoc)
   * Given a {@link GrouperGroup} object, return its matching 
   * {@link GrouperSchema} object.
   * TODO This will need poking when we support multiple types.
   */
  protected static GrouperSchema _groupSchema(GrouperSession s, GrouperGroup g) {
    String        qry     = "GrouperSchema.by.key";
    GrouperSchema schema  = null;
    try {
      Query q = s.dbSess().session().getNamedQuery(qry);
      q.setString(0, g.key());
      try {
        List vals = q.list();
        if (vals.size() == 1) {
          schema = (GrouperSchema) vals.get(0);
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
    return schema;
  }

}
 
