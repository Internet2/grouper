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
 * @version $Id: GrouperBackend.java,v 1.194 2005-03-22 21:08:34 blair Exp $
 */
public class GrouperBackend {

  protected static boolean listDelVal(GrouperSession s, GrouperList gl) {
    GrouperSession.validate(s);
    gl.load(s);
    boolean rv = false;
      // TODO Confirm gl exists before calculating mof?
      // The GrouperList objects that we will need to delete
      MemberOf mof = new MemberOf(s);
      List listVals = mof.memberOf(gl);

      // Now delete the list values
      // TODO Refactor out to _listDelVal(List vals)
      Iterator listValIter = listVals.iterator();
      while (listValIter.hasNext()) {
        GrouperList lv = (GrouperList) listValIter.next();
        _listDelVal(s, lv);
      }
      rv = true;
    return rv;
  }

  /* !javadoc
   * Delete a GrouperList object
   */
  // TODO Refactor into smaller components
  protected static void _listDelVal(GrouperSession s, GrouperList gl) {
    Query   q;
    // TODO Refactor out
    Grouper.log().backend("_listDelVal() (g) " + gl.group().name());
    Grouper.log().backend("_listDelVal() (m) " + gl.member().subjectID());
    Grouper.log().backend("_listDelVal() (t) " + gl.groupField());
    if (gl.via() != null) {
      try {
        // TODO Why can't I use delete() with a parameterized query?
        q = s.dbSess().session().createQuery(
              "FROM GrouperList AS gl WHERE " +
              "gl.groupKey    = ? AND "       +
              "gl.memberKey   = ? AND "       +
              "gl.groupField  = ? AND "       +
              "gl.viaKey      = ?"
            );
        q.setString(0, gl.group().key());
        q.setString(1, gl.member().key());
        q.setString(2, gl.groupField());
        q.setString(3, gl.via().key());
      } catch (HibernateException e) {
        throw new RuntimeException("Unable to create query: " + e);
      }
      Grouper.log().backend("_listDelVal() (v) " + gl.via().name());
    } else {
      try {
        // TODO Why can't I use delete() with a parameterized query?
        q = s.dbSess().session().createQuery(
              "FROM GrouperList AS gl WHERE " +
              "gl.groupKey    = ? AND "       +
              "gl.memberKey   = ? AND "       +
              "gl.groupField  = ? AND "       +
              "gl.viaKey      IS NULL"
            );
        q.setString(0, gl.group().key());
        q.setString(1, gl.member().key());
        q.setString(2, gl.groupField());
      } catch (HibernateException e) {
        throw new RuntimeException("Unable to create query: " + e);
      }
      Grouper.log().backend("_listDelVal() (v) null");
    }

    try {
      List vals = q.list();
      if (vals.size() == 1) {
        GrouperList del = (GrouperList) vals.get(0);
        try {
          s.dbSess().session().delete(del);
          Grouper.log().backend("_listDelVal() deleted");
        } catch (HibernateException e) {
          throw new RuntimeException(
                      "Error deleting list value: " + e
                    );
        }
      } else {
/* TODO Later, later...
        throw new RuntimeException(
                    "Wrong number of values to delete: " + vals.size()
                  );
*/
      }
    } catch (HibernateException e) {
      throw new RuntimeException(
                  "Error finding values: " + e
                );
    }

  }

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
   * Valid {@link GrouperField} items.
   *
   * @return List of group fields.
   */
  protected static List groupFields(DbSess dbSess) {
    String  qry   = "GrouperField.all";
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

  /**
   * Valid {@link GrouperTypeDef} items.
   *
   * @return List of group type definitions.
   */
  protected static List groupTypeDefs(DbSess dbSess) {
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

  /**
   * Query for all groups of the specified type.
   * <p />
   *
   * @param   s     Perform query within this session.
   * @param   type  Query on this {@link GrouperGroup} type.
   * @return  List of {@link GrouperGroup} objects.
   */
  protected static List groupType(GrouperSession s, String type) {
    String  qry   = "GrouperSchema.by.type";
    List    vals  = new ArrayList();
    try {
      Query q = s.dbSess().session().getNamedQuery(qry);
      q.setString(0, type);
      try {
        Iterator iter = q.list().iterator();
        while (iter.hasNext()) {
          GrouperSchema gs = (GrouperSchema) iter.next();
          // TODO What a hack
          GrouperGroup g = GrouperGroup.loadByKey(s, gs.key());
          if (g != null) {
            vals.add(g);
          }
        }
      } catch (HibernateException e) {
        throw new RuntimeException(
                    "Error getting results for " + qry + ": " + e
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
   * Retrieve all valid {@link GrouperGroup} types.
   * <p />
   *
   * @return List of {@link GrouperType} objects.
   */
  protected static List groupTypes(DbSess dbSess) {
    String  qry   = "GrouperType.all";
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

  /**
   * Query for a single {@link Subject} of type "group".
   *
   * @return  {@link Subject} object or null.
   */
  protected static Subject subjectLookupTypeGroup(
                             String id, String typeID
                           ) 
  {
    DbSess  dbSess  = new DbSess(); // FIXME CACHE!
    String  qry     = "GrouperGroup.by.id";
    Subject subj    = null;
    try {
      Query q = dbSess.session().getNamedQuery(qry);
      q.setString(0, id);
      try {
        List vals = q.list();
        if (vals.size() == 1) {
          // FIXME Properly load the group
          GrouperGroup g = (GrouperGroup) vals.get(0);
          if (g != null) {
            // ... And convert it to a subject object
            subj = new SubjectImpl(id, typeID);
          } else {
            Grouper.log().backend(
              "subjectLookupTypeGroup() Returned group is null"
            );
          }
        } else {
          Grouper.log().backend(
            "subjectLookupTypeGroup() Found " + vals.size() + 
            " matching groups"
          );
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
    dbSess.stop();
    return subj;
  }

  /**
   * Query for a single {@link Subject} of the type DEF_SUBJ_TYPE using 
   * the internal subject store.
   *
   * @return  {@link Subject} object or null.
   */
  protected static Subject subjectLookupTypePerson(
                             String id, String typeID
                           ) 
  {
    DbSess  dbSess  = new DbSess(); // FIXME CACHE!
    String  qry     = "SubjectImpl.by.subjectid.and.typeid";
    Subject subj    = null;
    try {
      Query q = dbSess.session().getNamedQuery(qry);
      q.setString(0, id);
      q.setString(1, typeID);
      try {
        List vals = q.list();
        if (vals.size() == 1) {
          subj = (Subject) vals.get(0);
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
    dbSess.stop();
    return subj;
  }

  /**
   * Valid {@link SubjectType} items.
   *
   * @return List of subject types.
   */
  protected static List subjectTypes(DbSess dbSess) {
    String  qry   = "SubjectTypeImpl.all";
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
   * PRIVATE CLASS METHODS
   */

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
            Grouper.log().backend("Unable to delete attribute " + field);
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

  // FIXME Refactor.  Mercilesssly.
  protected static GrouperGroup groupLoad(
                                GrouperSession s, String stem, 
                                String extn, String type
                              )
  {
    GrouperGroup g = null;
    if (GrouperStem.exists(s, stem)) {
      String name = GrouperGroup.groupName(stem, extn);
      g = GrouperBackend.groupLoadByName(s, name, type);
      // FIXME WTF? Should I do *something* here?
    }
    return g;
  }

  // FIXME Refactor.  Mercilesssly.
  protected static GrouperGroup groupLoadByID(GrouperSession s, String id) {
    GrouperGroup  g     = null;
    String        key   = null;
    String        qry   = "GrouperGroup.by.id";
    try {
      Query q = s.dbSess().session().getNamedQuery(qry);
      q.setString(0, id);
      try {
        List vals = q.list();
        if (vals.size() == 1) {
          g = (GrouperGroup) vals.get(0);
          if ( (g != null) && (g.key() != null) ) {
            key = g.key();
            g = GrouperGroup.loadByKey(s, g, key);
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
    return g;
  }

  /* (!javadoc)
   * Load a group by name.
   */
  // FIXME Now *this* is ugly
  protected static GrouperGroup groupLoadByName(
                   GrouperSession s, String name, String type
                 ) 
  {
    GrouperGroup  g           = null;
    String        qryGG       = "GrouperAttribute.by.name";
    String        qryGS       = "GrouperSchema.by.key.and.type";
    boolean       initialized = false;
    try {
      Query qGG = s.dbSess().session().getNamedQuery(qryGG);
      qGG.setString(0, name);
      try {
        List names = qGG.list();
        if (names.size() > 0) {
          Iterator iter = names.iterator();
          while (iter.hasNext()) {
            GrouperAttribute attr = (GrouperAttribute) iter.next();
            try {
              Query qGS = s.dbSess().session().getNamedQuery(qryGS); 
              qGS.setString(0, attr.key());
              qGS.setString(1, type);
              try {
                List gs = qGS.list();
                if (gs.size() == 1) {
                  GrouperSchema schema = (GrouperSchema) gs.get(0);
                  if (schema.type().equals(type)) {
                    g = GrouperGroup.loadByKey(s, g, attr.key());
                    if (g != null) {
                      if (g.type().equals(type)) {
                        initialized = true;
                      }
                    }
                  }
                }
              } catch (HibernateException e) {
                throw new RuntimeException(
                            "Error retrieving results for " + 
                            qryGS + ": " + e
                          );
              }
            } catch (HibernateException e) {
              throw new RuntimeException(
                          "Unable to get query " + qryGS + ": " + e
                        );
            }
          }
        }
      } catch (HibernateException e) {
        throw new RuntimeException(
                    "Error retrieving results for " + qryGG + ": " + e
                  );
      }
    } catch (HibernateException e) {
      throw new RuntimeException(
                  "Unable to get query " + qryGG + ": " + e
                );
    }
    if (!initialized) {
      // We failed to load a group.  Null out the object.
      g = null;
    }
    return g;
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
 
