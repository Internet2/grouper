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
import  edu.internet2.middleware.grouper.database.*;
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
 * @version $Id: GrouperBackend.java,v 1.168 2005-03-16 22:54:08 blair Exp $
 */
public class GrouperBackend {

  /*
   * FIXME This entire class needs to be gutted and refactored.  I
   *       won't complain if all of this code were to disappear.
   *       In fact, I'd probably celebrate.  
   *       
   *       Unfortunately, I don't have time to refactor this before the
   *       release of 0.5.  Alas.
   */

  /*
   * PROTECTED CONSTANTS
   */
  protected static final String VAL_NOTNULL = "**NOTNULL**";  // FIXME
  protected static final String VAL_NULL    = "**NULL**";     // FIXME



  /*
   * PROTECTED CLASS METHODS
   */

  /**
   * Delete a {@link GrouperGroup} from the repository.
   * <p />
   * @param   s   {@link GrouperSession}
   * @param   g   {@link GrouperGroup} to add
   * @return  True if the group was added.
   */
  protected static boolean groupAdd(GrouperSession s, GrouperGroup g) {
    boolean rv      = false;
    if (_validateGroupAdd(s, g)) {
      try {
        // The group object
        s.dbSess().session().save(g);
      } catch (HibernateException e) {
        throw new RuntimeException("Error saving group: " + g);
      }
      // Add schema
      if (_schemaAdd(s, g)) {
        // Add attributes
        if (_attributesAdd(s, g)) {
          if (_privGrantUponCreate(s, g)) {
            rv = true;
          }
        }
      }
    } else { 
      Grouper.log().event("Unable to add group " + g.name());
    }
    return rv;
  }

  /**
   * Delete a {@link GrouperGroup} from the registry.
   * <p />
   *
   * @param   s   Session to delete group within.
   * @param   g   Group to delete.
   * @return  True if the group was deleted.
   */
  protected static boolean groupDelete(GrouperSession s, GrouperGroup g) {
    boolean rv = false;
    /*
     * TODO I envision problems when people start creating group types
     *      with other custom fields...
     */
    /*
     * TODO Do I need to validate the privs and then delete with a root
     *      session?
     */
    if (_validateGroupDel(s, g)) {
      try {
        // Revoke access privileges
        if (_privAccessRevokeAll(s, g)) {
          // Revoke naming privileges
          if (_privNamingRevokeAll(s, g)) {
            // Delete attributes
            if (_attributesDel(s, g))  {
              // Delete schema
              if (_schemaDel(s, g)) {
                // Delete group
                s.dbSess().session().delete(g);
                // Commit
                rv = true;
              }
            }
          }
        }
      } catch (HibernateException e) {
        // TODO We probably need a rollback in here in case of failure
        //      above.
        throw new RuntimeException(e);
      }
    }
    return rv;
  }

  /**
   * Add list values to the backend store.
   *
   * @param s     Add member within this session context.
   * @param gl    Add this {@link GrouperList} object.
   */
  protected static boolean listAddVal(GrouperSession s, GrouperList gl) {
    boolean rv = false; 
    if (_validateListVal(s, gl)) {
      // TODO Remove existence validation from _lAV?
      if (_listValExist(s, gl) == false) {
        // The GrouperList objects that we will need to add
        MemberOf mof = new MemberOf(s);
        List listVals = mof.memberOf(gl);
          
        // Now add the list values
        // TODO Refactor out to _listAddVal(List vals)
        Iterator iter = listVals.iterator();
        while (iter.hasNext()) {
          GrouperList lv = (GrouperList) iter.next();
          Iterator i = lv.chain().iterator();
          while (i.hasNext()) {
            MemberVia el = (MemberVia) i.next();
          }
          _listAddVal(s, lv);
        }
        rv = true; // TODO This seems naive
      }
    }
    return rv;
  }

  /**
   * Remove list values from the backend store.
   *
   * @param s     Delete list value within this session context.
   * @param gl    Delete this {@link GrouperList} object.
   */
  protected static boolean listDelVal(GrouperSession s, GrouperList gl) {
    boolean rv = false;
    if (_validateListVal(s, gl)) {
      try {
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
        try {
          s.dbSess().session().flush();
        } catch (HibernateException e) {
          throw new RuntimeException("Error flushing session: " + e);
        }

        // Update modify information
        s.dbSess().session().update(gl.group());
        rv = true;
      } catch (HibernateException e) {
        throw new RuntimeException(e);
      }
    }
    return rv;
  }


  /*
   * PRIVATE CLASS METHODS
   */

  /* !javadoc
   * Attach attributes to a group
   */
  private static boolean _attributesAdd(GrouperSession s, GrouperGroup g) {
    boolean rv = false;
    Iterator iter = g.attributes().keySet().iterator();
    while (iter.hasNext()) {
      GrouperAttribute attr = (GrouperAttribute) g.attribute(
                                                   (String) iter.next() 
                                                 );
      // TODO Error checking, anyone? 
      GrouperBackend._attrAdd(
                              s, g.key(), attr.field(), attr.value()
                             );
      rv = true; // FIXME This *cannot* be correct
    }
    return rv;
  }

  /* !javadoc
   * Delete all attributes attached to a group
   */
  private static boolean _attributesDel(GrouperSession s, GrouperGroup g) {
    boolean rv = false;
    Iterator iter = attributes(s, g).iterator();
    while (iter.hasNext()) {
      GrouperAttribute ga = (GrouperAttribute) iter.next();
      try {
        s.dbSess().session().delete(ga);
        rv = true;
      } catch (HibernateException e) {
        // FIXME LOG
      }
    }
    return rv;
  } 

  /* !javadoc
   * Add a GrouperList object
   */
  private static void _listAddVal(GrouperSession s, GrouperList gl) {
    // TODO Refactor out
    Grouper.log().backend("_listAddVal() (g) " + gl.group().name());
    Grouper.log().backend("_listAddVal() (m) " + gl.member().subjectID());
    Grouper.log().backend("_listAddVal() (t) " + gl.groupField());
    if (gl.via() != null) {
      Grouper.log().backend("_listAddVal() (v) " + gl.via().name());
    } else {
      Grouper.log().backend("_listAddVal() (v) null");
    }

    /*
     * If a via chain is present, either save it or find out what
     * its key is
     */
    if (gl.chain().size() > 0) {
      int     idx   = 0;
      String  uuid = new GrouperUUID().toString();
      gl.listKey(uuid);
      Iterator iter = gl.chain().iterator();
      while (iter.hasNext()) {
        MemberVia mv = (MemberVia) iter.next();
        mv.key(uuid);
        mv.idx(idx);
        mv.save(s);
        idx++;
      }
    }
    try {
      if (!s.dbSess().session().contains(gl)) {
        s.dbSess().session().save(gl);
        Grouper.log().backend("_listAddVal() added");
      } else {
      }
    } catch (HibernateException e) {
      throw new RuntimeException("Error adding list value: " + e);
    }
  }

  /* !javadoc
   * Delete a GrouperList object
   */
  // TODO Refactor into smaller components
  private static void _listDelVal(GrouperSession s, GrouperList gl) {
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

  /* !javadoc
   * Check whether a list value exists.
   */
  private static boolean _listValExist(GrouperSession s, GrouperList gl) {
    Query   q;
    String  qry;
    String  qryEff  = "GrouperList.by.group.and.member.and.list.and.is.eff"; 
    String  qryImm  = "GrouperList.by.group.and.member.and.list.and.is.imm"; 
    boolean rv      = false;
    if (gl.via() == null) { 
      try {
        q = s.dbSess().session().getNamedQuery(qryImm);
        qry = qryImm;
      } catch (HibernateException e) {
        throw new RuntimeException(
                    "Unable to get query " + qryImm + ": " + e
                  );
      }
    } else {
      try {
        q = s.dbSess().session().getNamedQuery(qryEff);
        qry = qryEff;
      } catch (HibernateException e) {
        throw new RuntimeException(
                    "Unable to get query " + qryEff + ": " + e
                  );
      }
    } 
    q.setString(0, gl.group().key());
    q.setString(1, gl.member().key());
    q.setString(2, gl.groupField());
    try {
      List vals = q.list();
      if (vals.size() == 1) {
        GrouperList lv = (GrouperList) vals.get(0);
        rv = true;
      }
    } catch (HibernateException e) {
      throw new RuntimeException(
                  "Error retrieving results for " + qry + ": " + e
                );
    }
    return rv;
  }

  /* !javadoc
   * Revoke all access privs attached to a group
   */
  private static boolean _privAccessRevokeAll(
                           GrouperSession s, GrouperGroup g
                         ) 
  {
    boolean rv = false;
    /* 
     * TODO This could be prettier, especially if/when there are custom
     *      privs
     */
    if (
        s.access().revoke(s, g, Grouper.PRIV_OPTIN)   &&
        s.access().revoke(s, g, Grouper.PRIV_OPTOUT)  &&
        s.access().revoke(s, g, Grouper.PRIV_VIEW)    &&
        s.access().revoke(s, g, Grouper.PRIV_READ)    &&
        s.access().revoke(s, g, Grouper.PRIV_UPDATE)  &&
        s.access().revoke(s, g, Grouper.PRIV_ADMIN)
       )
    {
      rv = true;
    }
    return rv;
  }

  /* !javadoc
   * Grant PRIV_ADMIN to group creator upon creation
   */
  private static boolean _privGrantAdminUponCreate(
                           GrouperSession s, GrouperGroup g,
                           GrouperMember m
                         )
  {
    boolean rv = false;
    if (s.access().grant(s, g, m, Grouper.PRIV_ADMIN)) {
      Grouper.log().backend("Granted " + Grouper.PRIV_ADMIN + " to " + m);
      rv = true;
    } else {
      Grouper.log().backend(
                            "Unable to grant " + Grouper.PRIV_ADMIN + 
                            " to " + m
                           );
    }
    return rv;
  }

  /* !javadoc
   * Grant PRIV_STEM to stem creator upon creation
   */
  private static boolean _privGrantStemUponCreate(
                           GrouperSession s, GrouperGroup g,
                           GrouperMember m
                         )
  {
    boolean rv = false;
    if (s.naming().grant(s, g, m, Grouper.PRIV_STEM)) {
      Grouper.log().backend("Granted " + Grouper.PRIV_STEM + " to " + m);
      rv      = true;
    } else {
      Grouper.log().backend(
        "Unable to grant " + Grouper.PRIV_STEM + " to " + m
      );
    }
    return rv;
  }

  /* !javadoc
   * Grant appropriate privilege to group|stem creator upon creation
   */
  private static boolean _privGrantUponCreate(
                           GrouperSession s, GrouperGroup g
                         )
  {
    boolean rv = false;
    // We need a root session for for bootstrap privilege granting
    Subject root = GrouperSubject.load(
                     Grouper.config("member.system"), Grouper.DEF_SUBJ_TYPE
                   );
    GrouperSession rs = GrouperSession.start(root);
    if (rs != null) {
      // Now grant privileges to the group creator
      Grouper.log().backend("Converting subject " + s);
      GrouperMember m = GrouperMember.load(s.subject() );
      if (m != null) { // FIXME Bah
        Grouper.log().backend("Converted to member " + m);
        if (g.type().equals(Grouper.NS_TYPE)) {
          if (_privGrantStemUponCreate(rs, g, m)) {
            // NS_TYPE groups get PRIV_STEM
            rv = true;
          } 
        } else if (_privGrantAdminUponCreate(rs, g, m)) {
          // All other group types get PRIV_ADMIN
          rv = true;
        }
      } else {
        Grouper.log().backend("Unable to convert to member");
      }
      // Close root session
      rs.stop();
    }
    return rv;
  }

  /* !javadoc
   * Revoke all naming privs attached to a group
   */
  private static boolean _privNamingRevokeAll(
                           GrouperSession s, GrouperGroup g
                         ) 
  {
    boolean rv = false;
    // Revoke all privileges
    // FIXME This is ugly 
    if (
        s.naming().revoke(s, g, Grouper.PRIV_STEM)    &&
        s.naming().revoke(s, g, Grouper.PRIV_CREATE) 
       )
    {       
      rv = true;
    }
    return rv;
  }

  /* !javadoc
   * Attach schema to a group
   */
  private static boolean _schemaAdd(GrouperSession s, GrouperGroup g) {
    boolean rv = false;
    try {
      GrouperSchema schema = new GrouperSchema( g.key(), g.type() );
      if (schema != null) {
        s.dbSess().session().save(schema);
        rv = true;
      }
    } catch (HibernateException e) {
      // FIXME LOG.  Hell, anything.
    }
    return rv;
  }

  /* !javadoc
   * Delete all schema attached to a group
   */
  private static boolean _schemaDel(GrouperSession s, GrouperGroup g) {
    boolean rv  = false;
    String  qry = "GrouperSchema.by.key";
    try {
      Query q = s.dbSess().session().getNamedQuery(qry);
      q.setString(0, g.key());
      try {
        Iterator iter = q.list().iterator();
        while (iter.hasNext()) {
          GrouperSchema gs = (GrouperSchema) iter.next();
          try {
            s.dbSess().session().delete(gs);
            rv = true;
          } catch (HibernateException e) {
            throw new RuntimeException(
                        "Error deleting schema: " + e
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
 
  /* !javadoc
   * Validate that a group is eligible to be created
   */
  private static boolean _validateGroupAdd(
                           GrouperSession s, GrouperGroup g
                         )
  {
    boolean rv = false;
    GrouperAttribute stem = (GrouperAttribute) g.attribute("stem");
    if (_stemLookup(s, (String) stem.value())) {
      rv = true;
    }
    return rv;
  }

  /* !javadoc
   * Validate that a group is eligible to be deleted
   */
  private static boolean _validateGroupDel(
                           GrouperSession s, GrouperGroup g
                         ) 
  {
    boolean rv = false;
    // Convert the group to member to see if it has any mships
    GrouperMember asMem = GrouperMember.load(s, g.id(), "group");
    List valsG = listVals(s, g, Grouper.DEF_LIST_TYPE);
    List valsM = listVals(s, asMem, Grouper.DEF_LIST_TYPE);
    if ( (valsG.size() != 0) || (valsM.size() != 0) ) {
      if (valsG.size() != 0) {
        Grouper.log().event(
          "ERROR: Unable to delete group as it still has members"
        );
      }
      if (valsM.size() != 0) {
        Grouper.log().event(
          "ERROR: Unable to delete group as it is a member of other groups"
        );
      }
    } else {
      rv = true;
    }
    return rv;
  }

  /* !javadoc
   * Return true if the list value appears remotely legitimate.
   */
  private static boolean _validateListVal(
                            GrouperSession s, GrouperList gl
                          )
  {
    // TODO Better validation would be appreciated
    if (s == null) {
      throw new RuntimeException("List validation: null session");
    }
    if (gl == null) {
      throw new RuntimeException("List validation: null list");
    }
    if (!s.getClass().getName().equals(Grouper.KLASS_GS)) {
      throw new RuntimeException("List validation: invalid session");
    }
    if (gl.group() == null) {
      throw new RuntimeException("List validation: null group");
    }
    if (!gl.group().getClass().getName().equals(Grouper.KLASS_GG)) {
      throw new RuntimeException("List validation: invalid group");
    }
    if (gl.member() == null) {
      throw new RuntimeException("List validation: null member");
    }
    if (gl.member() == null) {
      throw new RuntimeException("List validation: null member");
    }
    if (!gl.member().getClass().getName().equals(Grouper.KLASS_GM)) {
      throw new RuntimeException("List validation: invalid member");
    }
    if (!Grouper.groupField(gl.group().type(), gl.groupField())) {
      throw new RuntimeException("List validation: invalid field");
    }
    if (!gl.member().getClass().getName().equals(Grouper.KLASS_GM)) {
      throw new RuntimeException("List validation: invalid member");
    }
    if (gl.groupField() == null) {
      throw new RuntimeException("List validation: null field");
    }
    if (!Grouper.groupField(gl.group().type(), gl.groupField())) {
      throw new RuntimeException("List validation: invalid field");
    }
    return true;
  }


  /*
   * METHODS ABOVE HAVE HAD AT LEAST ONE REFACTORING PASS APPLIED TO
   * THEM; THOSE  BELOW HAVE NOT.
   */


  /*
   * PROTECTED CLASS METHODS 
   */

  /**
   * Store a {@link GrouperAttribute} in the registry.
   * <p />
   *
   * @param   key     {@link GrouperGroup} key.
   * @param   field   {@link GrouperField} field.
   * @param   value   {@link GrouperField} value.
   * @return  The added {@link GrouperAttribute} object.
   */
  protected static GrouperAttribute attrAdd(
                                            GrouperSession s,
                                            String key, String field, 
                                            String value
                                           ) 
  {
    GrouperAttribute attr = GrouperBackend._attrAdd(
                              s, key, field, value
                            ); 
    return attr;
  }

  /**
   * Delete a {@link GrouperAttribute} from the registry.
   * <p />
   *
   * @param   key     {@link GrouperGroup} key.
   * @param   field   {@link GrouperField} field.
   * @return  The added {@link GrouperAttribute} object.
   */
  protected static boolean attrDel(
                             GrouperSession s, String key, String field
                           ) 
  {
    boolean rv = false;
    rv = GrouperBackend._attrDel(s, key, field);
    return rv;
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

  // TODO
  /**
   * Query for attributes with a field of <i>extension</i> and 
   * the specified value.
   * <p />
   * 
   * @param   s     Perform query within this {@link
   *   GrouperSession}.
   * @param   extn  Query for extensions of this type.
   * @return  List of {@link GrouperAttribute} objects.
   */
  protected static List extensions(GrouperSession s, String extn) {
    List    extensions  = GrouperBackend._extensions(s, extn);
    return extensions;
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

  // FIXME Refactor.  Mercilesssly.
  protected static GrouperGroup groupLoadByKey(GrouperSession s, GrouperGroup g, String key) {
    if (key != null) {
      try {
        // Attempt to load a stored group into the current object
        g = (GrouperGroup) s.dbSess().session().get(GrouperGroup.class, key);
        if (g != null) {
          // Its schema
          GrouperSchema schema = GrouperBackend._groupSchema(s, g);
          if (schema != null) {
            if (GrouperBackend._groupAttachAttrs(s, g)) {
              g.type( schema.type() );
            } else {
              g = null;
            }
          } else {
            g = null;
          }
        }
      } catch (HibernateException e) {
        // TODO Rollback if load fails?  Unset this.exists?
        throw new RuntimeException("Error loading group: " + e);
      }
    }
    return g;
  }

  /**
   * TODO Does this actually work?
   */
  protected static boolean groupUpdate(GrouperSession s, GrouperGroup g) {
    boolean rv      = false;
    try {
      s.dbSess().session().update(g);
      rv = true;
    } catch (HibernateException e) {
      rv = false; 
      Grouper.log().backend("Unable to update group " + g);
    }
    return rv;
  }

  // TODO Why not just listValExist directly?
  protected static boolean listVal(GrouperSession s, GrouperList gl) {
    // TODO Basic input data validation
    boolean rv      = false;
    rv = GrouperBackend._listValExist(s, gl);
    return rv;
  }

  /**
   * Query for memberships in the specified list.
   * <p />
   *
   * @param   s     Perform query within this session.
   * @param   list  Query on this list type.
   * @return  List of {@link GrouperList} objects.
   */
  protected static List listVals(GrouperSession s, String list) {
    String  qry   = "GrouperList.by.list";
    List    vals  = new ArrayList();
    try {
      Query q = s.dbSess().session().getNamedQuery(qry);
      q.setString(0, list);
      try {
        // TODO Argh!
        Iterator iter = q.list().iterator();
        while (iter.hasNext()) {
          // Make the returned items into proper objects
          GrouperList gl = (GrouperList) iter.next();
          vals.add(gl);
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
    return vals;
  }

  /**
   * Query for memberships of the specified list in the specified
   * group.
   * <p />
   *
   * @param   s     Perform query within this session.
   * @param   g     Query on this {@link GrouperGroup}.
   * @param   list  Query on this list type.
   * @return  List of {@link GrouperList} objects.
   */
  protected static List listVals(GrouperSession s, GrouperGroup g, String list) {
    String  qry   = "GrouperList.by.group.and.list";
    List    vals  = new ArrayList();
    try {
      Query q = s.dbSess().session().getNamedQuery(qry);
      q.setString(0, g.key());
      q.setString(1, list);
      try {
        Iterator iter = q.list().iterator();
        while (iter.hasNext()) {
          // Make the returned items into proper objects
          GrouperList gl = (GrouperList) iter.next();
          gl.load(s);
          vals.add(gl);
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
    return vals;
  }

  /**
   * Query for memberships of the specified list for the specified
   * member.
   * <p />
   *
   * @param   s     Perform query within this session.
   * @param   m     Query on this {@link GrouperMember}.
   * @param   list  Query on this list type.
   * @return  List of {@link GrouperList} objects.
   */
  protected static List listVals(GrouperSession s, GrouperMember m, String list) {
    String  qry   = "GrouperList.by.member.and.list";
    List    vals  = new ArrayList();
    try {
      if (s == null) {
        throw new RuntimeException("s == null");
      }
      if (s.dbSess() == null) {
        throw new RuntimeException("s.dbSess() == null");
      }
      if (s.dbSess().session() == null) {
        throw new RuntimeException("s.dbSess().session() == null");
      }
      Query q = s.dbSess().session().getNamedQuery(qry);
      q.setString(0, m.key());
      q.setString(1, list);
      try {
        // TODO Argh!
        Iterator iter = q.list().iterator();
        while (iter.hasNext()) {
          // Make the returned items into proper objects
          GrouperList gl = (GrouperList) iter.next();
          gl.load(s);
          vals.add(gl);
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
    return vals;
  }

  /**
   * Query for effective memberships in the specified list.
   * <p />
   *
   * @param   s     Perform query within this session.
   * @param   list  Query on this list type.
   * @return  List of {@link GrouperList} objects.
   */
  protected static List listEffVals(GrouperSession s, String list) {
    String  qry   = "GrouperList.by.list.and.is.eff";
    List    vals  = new ArrayList();
    try {
      Query q = s.dbSess().session().getNamedQuery(qry);
      q.setString(0, list);
      try {
        // TODO Argh!
        Iterator iter = q.list().iterator();
        while (iter.hasNext()) {
          // Make the returned items into proper objects
          GrouperList gl = (GrouperList) iter.next();
          gl.load(s);
          vals.add(gl);
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
    return vals;
  }

  /**
   * Query for effective memberships of the specified list in the
   * specified group.
   * <p />
   *
   * @param   s     Perform query within this session.
   * @param   g     Query on this group.
   * @param   list  Query on this list type.
   * @return  List of {@link GrouperList} objects.
   */
  protected static List listEffVals(GrouperSession s, GrouperGroup g, String list) {
    String  qry   = "GrouperList.by.group.and.list.and.is.eff";
    List    vals  = new ArrayList();
    try {
      Query q = s.dbSess().session().getNamedQuery(qry);
      q.setString(0, g.key());
      q.setString(1, list);
      try {
        // TODO Argh!
        Iterator iter = q.list().iterator();
        while (iter.hasNext()) {
          // Make the returned items into proper objects
          GrouperList gl = (GrouperList) iter.next();
          gl.load(s);
          vals.add(gl);
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
    return vals;
  }

  /**
   * Query for effective memberships for the specified member in the
   * specified list.
   * <p />
   *
   * @param   s     Perform query within this session.
   * @param   m     Query on this {@link GrouperMember}.
   * @param   list  Query on this list type.
   * @return  List of {@link GrouperList} objects.
   */
  protected static List listEffVals(GrouperSession s, GrouperMember m, String list) {
    String  qry   = "GrouperList.by.member.and.list.and.is.eff";
    List    vals  = new ArrayList();
    try {
      Query q = s.dbSess().session().getNamedQuery(qry);
      q.setString(0, m.key());
      q.setString(1, list);
      try {
        // TODO Argh!
        Iterator iter = q.list().iterator();
        while (iter.hasNext()) {
          // Make the returned items into proper objects
          GrouperList gl = (GrouperList) iter.next();
          gl.load(s);
          vals.add(gl);
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
    return vals;
  }

  /**
   * Query for immediate memberships in the specified list.
   * <p />
   *
   * @param   s     Perform query within this session.
   * @param   list  Query on this list type.
   * @return  List of {@link GrouperList} objects.
   */
  protected static List listImmVals(GrouperSession s, String list) {
    String  qry   = "GrouperList.by.list.and.is.imm";
    List    vals  = new ArrayList();
    try {
      Query q = s.dbSess().session().getNamedQuery(qry);  
      q.setString(0, list);
      try {
        // TODO Argh!
        Iterator iter = q.list().iterator();
        while (iter.hasNext()) {
          // Make the returned items into proper objects
          GrouperList gl = (GrouperList) iter.next();
          gl.load(s);
          vals.add(gl);
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
    return vals;
  }
  
  /**
   * Query for immediate memberships in the specified list.
   * <p />
   *
   * @param   s     Perform query within this session.
   * @param   g     Query on this {@link GrouperGroup}.
   * @param   list  Query on this list type.
   * @return  List of {@link GrouperList} objects.
   */
  protected static List listImmVals(GrouperSession s, GrouperGroup g, String list) {
    String  qry   = "GrouperList.by.group.and.list.and.is.imm";
    List    vals  = new ArrayList();
    try {
      Query q = s.dbSess().session().getNamedQuery(qry);
      q.setString(0, g.key());
      q.setString(1, list);
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
   * Query for immediate memberships for the specified member in the
   * specified list.
   * <p />
   *
   * @param   s     Perform query within this session.
   * @param   m     Query on this member.
   * @param   list  Query on this list type.
   * @return  List of {@link GrouperList} objects.
   */
  protected static List listImmVals(GrouperSession s, GrouperMember m, String list) {
    String  qry   = "GrouperList.by.member.and.list.and.is.imm";
    List    vals  = new ArrayList();
    try {
      Query q = s.dbSess().session().getNamedQuery(qry);
      q.setString(0, m.key());
      q.setString(1, list);
      try {
        // TODO Argh!
        Iterator iter = q.list().iterator();
        while (iter.hasNext()) {
          // Make the returned items into proper objects
          GrouperList gl = (GrouperList) iter.next();
          gl.load(s);
          vals.add(gl);
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
   * Query for groups created after the specified time.
   * <p />
   *
   * @param   d   A {@link java.util.Date} object.
   * @return  List of {@link GrouperGroup} objects.
   */
  protected static List groupCreatedAfter(GrouperSession s, java.util.Date d) {
    String  qry   = "GrouperGroup.by.created.after";
    List    vals  = new ArrayList();
    try {
      Query q = s.dbSess().session().getNamedQuery(qry);
      q.setString(0, Long.toString(d.getTime()));
      try {
        Iterator iter = q.list().iterator();
        vals = GrouperBackend._iterGroup(s, iter);
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
   * Query for groups created before the specified time.
   * <p />
   *
   * @param   d   A {@link java.util.Date} object.
   * @return  List of {@link GrouperGroup} objects.
   */
  protected static List groupCreatedBefore(GrouperSession s, java.util.Date d) {
    String  qry   = "GrouperGroup.by.created.before";
    List    vals  = new ArrayList();
    try {
      Query q = s.dbSess().session().getNamedQuery(qry);
      q.setString(0, Long.toString(d.getTime()));
      try {
        Iterator iter = q.list().iterator();
        vals = GrouperBackend._iterGroup(s, iter);
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
   * Query for groups modified after the specified time.
   * <p />
   *
   * @param   d   A {@link java.util.Date} object.
   * @return  List of {@link GrouperGroup} objects.
   */
  protected static List groupModifiedAfter(GrouperSession s, java.util.Date d) {
    String  qry   = "GrouperGroup.by.modified.after";
    List    vals  = new ArrayList();
    try {
      Query q = s.dbSess().session().getNamedQuery(qry);
      q.setString(0, Long.toString(d.getTime()));
      try {
        Iterator iter = q.list().iterator();
        vals = GrouperBackend._iterGroup(s, iter);
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
   * Query for groups modified before the specified time.
   * <p />
   *
   * @param   d   A {@link java.util.Date} object.
   * @return  List of {@link GrouperGroup} objects.
   */
  protected static List groupModifiedBefore(GrouperSession s, java.util.Date d) {
    String  qry   = "GrouperGroup.by.modified.before";
    List    vals  = new ArrayList();
    try {
      Query q = s.dbSess().session().getNamedQuery(qry);
      q.setString(0, Long.toString(d.getTime()));
      try {
        Iterator iter = q.list().iterator();
        vals = GrouperBackend._iterGroup(s, iter);
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

  /*
   * Retrieve GrouperMember by memberID
   */
  protected static GrouperMember memberByID(GrouperSession s, String id) {
    String        qry = "GrouperMember.by.id";
    GrouperMember m   = new GrouperMember();
    try {
      Query q = s.dbSess().session().getNamedQuery(qry);
      q.setString(0, id);
      try {
        List vals = q.list();
        if (vals.size() == 1) {
          m = (GrouperMember) vals.get(0);
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
    return m;
  }

  /**
   * Query for a single {@link GrouperMember} by key.
   *
   * @return  {@link GrouperMember} object or null.
   */
  protected static GrouperMember member(GrouperSession s, String key) {
    GrouperMember m = new GrouperMember();
    if (key != null) {
      try {
        m = (GrouperMember) s.dbSess().session().get(
                              GrouperMember.class, key
                            );
      } catch (HibernateException e) {
        throw new RuntimeException("Error loading member: " + e);
      }
    } else {
      m = null;
    }
    return m;
  }

  /**
   * Query for a single {@link GrouperMember} by subject id and type.
   *
   * @return  {@link GrouperMember} object or null.
   */
  protected static GrouperMember member(
                                   DbSess dbSess, String subjectID, 
                                   String subjectTypeID
                                 ) 
  {
    GrouperMember m     = null;
    String        qry   = "GrouperMember.by.subjectid.and.typeid";
    List          vals  = new ArrayList();
    try {
      Query q = dbSess.session().getNamedQuery(qry);
      q.setString(0, subjectID);
      q.setString(1, subjectTypeID);
      try {
        vals = q.list();
        if (vals.size() == 1) {
          m = (GrouperMember) vals.get(0);
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
    return m;
  }

  /**
   * Add a {@link GrouperMember} to backend store.
   *
   * @param   member  {@link GrouperMember} object to store.
   * @return  {@link GrouperMember} object.
   */
  protected static GrouperMember memberAdd(DbSess dbSess, GrouperMember member) {
    // TODO Should I have session/security restrictions in place?
    if ( 
        ( member.memberID()   != null) &&
        ( member.subjectID()  != null) &&
        ( member.typeID()     != null)
       ) 
    {
      try {
        // Save it
        dbSess.session().save(member);
      } catch (Exception e) {
        // TODO We probably need a rollback in here in case of failure
        //      above.
        throw new RuntimeException(e);
      }
      return GrouperBackend.member(dbSess, member.subjectID(), member.typeID());
    }
    return null;
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
   * Add a new {@link GrouperSession}.
   *
   * @param s Session to add.
   */
  protected static void sessionAdd(GrouperSession s) {
    try {
      s.dbSess().session().save(s);
    } catch (Exception e) {
      throw new RuntimeException("Error adding session: " + e);
    }
  }

  /**
   * Delete a {@link GrouperSession}.
   * <p />
   *
   * @param   s   Session to delete.
   * @return  True is fhe session was deleted.
   */
  protected static boolean sessionDel(GrouperSession s) {
    try {
      s.dbSess().session().delete(s);
    } catch (Exception e) {
      throw new RuntimeException("Error deleting session: " + e);
    }
    return true;
  }

  /**
   * Is the {@link GrouperSession} still valid?
   * <p />
   *
   * @param   s   Session to validate. 
   * @return  True if the session is still valid.
   */
  protected static boolean sessionValid(GrouperSession s) {
    boolean rv  = false;
    String  qry = "GrouperSession.by.id";
    try {
      Query q = s.dbSess().session().getNamedQuery(qry);
      q.setString(0, s.id());
      try {
        List vals = q.list();
        if (vals.size() == 1) {
          rv = true;
        } else {
          Grouper.log().event("Attempt to use an invalid session");
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

  /**
   * Cull old sessions.
   * <p />
   * TODO Go away. 
   */
  protected static void sessionsCull(GrouperSession s) {
    /* XXX Until I find the time to identify a better way of managing
     *     sessions -- which I *know* exists -- be crude about it. */
    java.util.Date  now     = new java.util.Date();
    long nowTime = now.getTime();
    long tooOld  = nowTime - 360000;

    try {
      s.dbSess().session().delete(
        "FROM GrouperSession AS gs" +
        " WHERE "                   +
        "gs.startTime > " + nowTime
      );
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  // TODO
  protected static List stems(GrouperSession s, String stem) {
    List    stems   = GrouperBackend._stems(s, stem);
    return stems;
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

  /*
   * Hibernate an attribute
   */
  private static GrouperAttribute _attrAdd(
                                    GrouperSession s, String key,
                                    String  field,   String value
                                  )
  {
    GrouperAttribute  attr  = null;
    String            qry   = "GrouperAttribute.by.key.and.value";
    List              vals  = new ArrayList();
    try {
      Query q = s.dbSess().session().getNamedQuery(qry);
      q.setString(0, key);
      q.setString(1, field);
      try {
        vals = q.list();
        if (vals.size() == 0) {
          // We've got a new one.  Store it.
          try {
            attr = new GrouperAttribute(key, field, value);
            s.dbSess().session().save(attr);   
          } catch (HibernateException e) {
            attr = null;
            Grouper.log().backend(
              "Unable to store attribute " + field + "=" + value
            );
          }
        } else if (vals.size() == 1) {
          // Attribute already exists.  Check to see if the value has
          // changed.
          attr = (GrouperAttribute) vals.get(0); 
          if (!attr.value().equals(value)) {
            try {
              attr = new GrouperAttribute(key, field, value);
              s.dbSess().session().update(attr);
            } catch (HibernateException e) {
              attr = null;
              Grouper.log().backend(
                "Unable to update attribute " + field + "=" + value
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
    return attr;
  }

  private static boolean _attrDel(
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

  private static List _extensions(GrouperSession s, String extension) {
    String  qry   = "GrouperAttribute.by.extension";
    List    vals  = new ArrayList();
    try {
      Query q = s.dbSess().session().getNamedQuery(qry);
      q.setString(0, extension);
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

  /* (!javadoc)
   * Attach attributes to a group.
   * FIXME Won't calling g.attribute(...) eventually cause the group's
   *       modify attrs to be updated every time this group is loaded?
   *      
   *       But perhaps the `initialized' hack that I added for another
   *       reason will work?
   */
  private static boolean _groupAttachAttrs(GrouperSession s, GrouperGroup g) {
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
    if (GrouperBackend._stemLookup(s, stem)) {
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
            g = GrouperBackend.groupLoadByKey(s, g, key);
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
                    g = groupLoadByKey(s, g, attr.key());
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
  private static GrouperSchema _groupSchema(GrouperSession s, GrouperGroup g) {
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

  /* (!javadoc)
   * Iterate through a list and fully load {@link GrouperGroup}
   * objects.
   */
  private static List _iterGroup(GrouperSession s, Iterator iter) {
    List vals = new ArrayList();
    while (iter.hasNext()) {
      GrouperGroup g = (GrouperGroup) iter.next();
      if (g != null) {
        g = GrouperBackend.groupLoadByKey(s, g, g.key());
        if (g != null) {
          vals.add(g);
        }
      }  
    }
    return vals;
  }

  /* (!javadoc)
   * True if the stem exists.
   */
  private static boolean _stemLookup(GrouperSession s, String stem) {
    boolean rv = false;
    if (stem.equals(Grouper.NS_ROOT)) {
      rv = true;
    } else {
      GrouperGroup g = GrouperBackend.groupLoadByName(
                         s, stem, Grouper.NS_TYPE
                       );
      if (g != null) {
        rv = true;
      }
    }
    return rv;
  }

  private static List _stems(GrouperSession s, String stem) {
    String  qry   = "GrouperAttribute.by.stem";
    List    vals  = new ArrayList();
    try {
      Query q = s.dbSess().session().getNamedQuery(qry);
      q.setString(0, stem);
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
  
}
 
