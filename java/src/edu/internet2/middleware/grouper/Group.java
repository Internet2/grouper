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


import  edu.internet2.middleware.subject.*;
import  java.util.*;
import  net.sf.hibernate.*;


/** 
 * Abstract Group class.
 * <p />
 *
 * @author  blair christensen.
 * @version $Id: Group.java,v 1.16 2005-03-29 14:52:10 blair Exp $
 */
abstract class Group {

  /*
   * ABSTRACT METHODS 
   */

  abstract protected GrouperAttribute attribute(String attribute);
  abstract protected void             attribute(String attribute, String value);
  abstract protected void             attributeAdd(GrouperAttribute attr);
  abstract protected void             attributeDel(GrouperAttribute attr);
  abstract protected String           createSource();
  abstract protected Subject          createSubject();
  abstract protected Date             createTime();
  abstract protected String           getCreateSource();
  abstract protected String           getCreateSubject();
  abstract protected String           getCreateTime();
  abstract protected String           getGroupComment();
  abstract protected String           getGroupID();
  abstract protected String           getGroupKey();
  abstract protected String           getModifySource();
  abstract protected String           getModifySubject();
  abstract protected String           getModifyTime();
  abstract protected boolean          initialized();
  abstract protected String           key();
  abstract protected void             listAddVal(GrouperMember m);
  abstract protected void             listAddVal(GrouperMember m, String list);
  abstract protected void             listDelVal(GrouperMember m);
  abstract protected void             listDelVal(GrouperMember m, String list);
  abstract protected List             listVals();
  abstract protected List             listVals(String list);
  abstract protected List             listEffVals();
  abstract protected List             listEffVals(String list);
  abstract protected List             listImmVals();
  abstract protected List             listImmVals(String list);
  abstract protected void             load(GrouperSession s);
  abstract protected String           modifySource();
  abstract protected Subject          modifySubject();
  abstract protected Date             modifyTime();
  abstract protected String           name();
  abstract protected void             setCreateSource(String createSource);
  abstract protected void             setCreateSubject(String createSubject);
  abstract protected void             setCreateTime(String createTime);
  abstract protected void             setGroupComment(String comment);
  abstract protected void             setGroupID(String id);
  abstract protected void             setGroupKey(String key);
  abstract protected void             setModified();
  abstract protected void             setModifySource(String modifySource);
  abstract protected void             setModifySubject(String modifySubject);
  abstract protected void             setModifyTime(String modifyTime);
  abstract protected GrouperMember    toMember();
  abstract protected String           type();


  /*
   * PUBLIC CLASS METHODS
   */

  /**
   * Format a group  name.
   * <p />
   * @param   stem  Group stem.
   * @param   extn  Group extension.
   * @return  String concatenation of the stem, delimiter and
   *  extension.
   */
  public static String groupName(String stem, String extn) {
    String name;
    if (stem.equals(Grouper.NS_ROOT)) {
      name = extn;
    } else {
      if (extn.indexOf(Grouper.HIER_DELIM) != -1) {
        throw new RuntimeException(
                    "Extension '" + extn + "' contains the " +
                    "hierarchy delimiter: " + Grouper.HIER_DELIM
                  );
      } else {
        name = stem + Grouper.HIER_DELIM + extn;
      }
    }
    return name;
  }


  /*
   * PROTECTED CLASS METHODS
   */

  /*
   * Set an attribute value
   */ 
  protected void attribute(
                   GrouperSession s, Group g, 
                   String attribute, String value
                 )
  {
    // Normalize the case
    attribute = attribute.toLowerCase();
    if (!Grouper.groupField(g.type(), attribute)) {
      throw new RuntimeException(
                  "Attribute is not valid for this group type"
                );
    }
    this.subjectCanModAttr(s, g, attribute);
    try {
      s.dbSess().txStart();
      if (value == null) {
        // Delete
        g.attributeDel( g.attribute(attribute) );
      } else {
        // Add-Or-Update
        GrouperAttribute attr = new GrouperAttribute(
                                      g.key(), attribute, value
                                    );
        g.attributeAdd(attr);
      }
      g.setModified();
      s.dbSess().txCommit();
    } catch (RuntimeException e) {
      s.dbSess().txRollback();
      throw new RuntimeException("Error modifying attribute: " + e);
    }
  }

  /**
   * Delete a group.
   * <p />
   * @param s   Delete group within this session.
   * @param ns  Delete this group.
   */
  public static void delete(GrouperSession s, GrouperGroup g) {
    Group.delete(s, (Group) g);
  }

  /**
   * Delete a stem.
   * <p />
   * @param s   Delete namespace within this session.
   * @param ns  Delete this namespace.
   */
  public static void delete(GrouperSession s, GrouperStem ns) {
    Group.delete(s, (Group) ns);
  }

  /*
   * Find and return the group key for (stem, extn, type).
   * TODO Rename => findKeyStemExtnType
   */
  protected static String findKey(
                            GrouperSession s, String stem, 
                            String extn, String type
                          ) 
  {
    String qry = "Group.key.by.stem.and.extn.and.type";
    String key = null;
    try {
      Query q = s.dbSess().session().getNamedQuery(qry);
      q.setString(0, stem);
      q.setString(1, extn);
      q.setString(2, type);
      try {
        key = (String) q.uniqueResult();
      } catch (HibernateException e) {
        throw new RuntimeException(
                    "Error retrieving result for " + qry + ": " + e
                  );
      }
    } catch (HibernateException e) {
      throw new RuntimeException(
                  "Unable to get query " + qry + ": " + e
                );
    }
    return key;
  }

  /*
   * Find and return the group key for (id).
   */
  protected static String findKeyByID(GrouperSession s, String id) {
    String qry = "Group.key.by.id";
    String key = null;
    try {
      Query q = s.dbSess().session().getNamedQuery(qry);
      q.setString(0, id);
      try {
        key = (String) q.uniqueResult();
      } catch (HibernateException e) {
        throw new RuntimeException(
                    "Error retrieving result for " + qry + ": " + e
                  );
      }
    } catch (HibernateException e) {
      throw new RuntimeException(
                  "Unable to get query " + qry + ": " + e
                );
    }
    return key;
  }

  /*
   * Find and return the group key for (name, type).
   */
  protected static String findKeyByNameAndType(
                            GrouperSession s, String name, String type
                          )
  {
    String qry = "Group.by.name.and.type";
    String key = null;
    try {
      Query q = s.dbSess().session().getNamedQuery(qry);
      q.setString(0, name);
      q.setString(1, type);
      try {
        key = (String) q.uniqueResult();
      } catch (HibernateException e) {
        throw new RuntimeException(
                    "Error retrieving result for " + qry + ": " + e
                  );
      }
    } catch (HibernateException e) {
      throw new RuntimeException(
                  "Unable to get query " + qry + ": " + e
                );
    }
    return key;
  }

  /*
   * Load {@link Group} by id.
   */
  protected static Group loadByID(GrouperSession s, String id) {
    return Group.loadByKey( s, Group.findKeyByID(s, id) );
  }

  /*
   * Load {@link Group} by key.
   */
  protected static Group loadByKey(GrouperSession s, String key) {
    Group g = null;
    if (key != null) {
      try {
        g = (Group) s.dbSess().session().get(Group.class, key);
        g.load(s); // TODO Remove explicit calls in GG and GS?
      } catch (HibernateException e) {
        throw new RuntimeException("Error loading group: " + e);
      }
    }
    return g;
  }

  /*
   * Load {@link Group} by name and type.
   */
  protected static Group loadByNameAndType(
                           GrouperSession s, String name, String type
                         )
  {
    return Group.loadByKey(s, Group.findKeyByNameAndType(s, name, type));
  }

  /*
   * Number of seconds since the epoch.
   */
  protected String now() {
    java.util.Date now = new java.util.Date();
    return Long.toString(now.getTime());
  }

  /*
   * Convert a string to a date object.
   * <p />
   * @return Date object.
   */
  protected Date string2date(String seconds) {
    Date d = null;
    if (seconds != null) {
      d = new Date(Long.parseLong(seconds));
    } 
    return d; 
  }

  /*
   * Top-level namespace & group creation is restricted.
   */
  protected static void subjectCanCreateAtRoot(
                          GrouperSession s, String stem
                        ) 
  {
    // Is this a top-level namespace?
    if (stem.equals(Grouper.NS_ROOT)) {
      // Only member.system can do so in this release
      if (!s.subject().getId().equals(Grouper.config("member.system"))) {
        throw new RuntimeException(
                    "This subject cannot create at root-level"
                  );
      }
    }
  }

  /*
   * Does the current subject have privs to create a group beneath this
   * stem?
   */
  protected static void subjectCanCreateGroup(
                          GrouperSession s, String stem
                        )
  {
    // Load stem for priv checking
    String key = Group.findKeyByNameAndType(s, stem, Grouper.NS_TYPE);
    if (key != null) {
      GrouperStem ns = (GrouperStem) Group.loadByKey(s, key);
      if (ns != null) { // TODO Flail if null?
        // If a naming group, does subject have STEM on stem?
        if (!s.naming().has(s, ns, Grouper.PRIV_CREATE)) {
          throw new RuntimeException(
                      "Subject does not have CREATE on " + stem
                    );
        }
      }
    }
  }

  /*
   * Does the current subject have privs to create a stem beneath this
   * stem?
   */
  protected static void subjectCanCreateStem(
                          GrouperSession s, String stem
                        )
  {
    // Load stem for priv checking
    String key = Group.findKeyByNameAndType(s, stem, Grouper.NS_TYPE);
    if (key != null) {
      GrouperStem ns = (GrouperStem) Group.loadByKey(s, key);
      if (ns != null) { // TODO Flail if null?
        // If a naming group, does subject have STEM on stem?
        if (!s.naming().has(s, ns, Grouper.PRIV_STEM)) {
          throw new RuntimeException(
                      "Subject does not have STEM on " + stem
                    );
        }
      }
    }
  }

  /*
   * Does the current subject have privs to delete the current group?
   */
  protected static void subjectCanDelete(GrouperSession s, Group g) {
    if (g.type().equals(Grouper.NS_TYPE)) {
      GrouperStem ns = (GrouperStem) g;
      // Right priv required
      if (!s.naming().has(s, ns, Grouper.PRIV_STEM)) {
        throw new RuntimeException("Deletion requires STEM priv");
      }
      // Are there child stems?
      if (ns.stems().size() > 0) {
        throw new RuntimeException(
                    "Cannot delete stem with child stems: " +
                    ns.stems().size()
                  );
      }
      // Are there child groups?
      if (ns.groups().size() > 0) {
        throw new RuntimeException(
                    "Cannot delete stem with child groups: " +
                    ns.groups().size()
                  );
      }
    } else {
      // Right priv required
      if (!s.access().has(s, g, Grouper.PRIV_ADMIN)) {
        throw new RuntimeException("Deletion requires ADMIN priv");
      }
    }
    // Are there any members?
    /*
     * TODO The problem with this is that we assume far too much
     *      about the importantance of "members"
     */
    if (g.listVals().size() > 0) {
      throw new RuntimeException(
                  "Cannot delete group with members: " + 
                  g.listVals().size() 
                );
    }
    // Is the group a member?
    // TODO Again, the problem with relying upon "members"
    GrouperMember m = g.toMember();
    if (m.listVals().size() > 0) {
      throw new RuntimeException(
                  "Cannot delete group that is member: " + 
                  m.listVals().size() 
                );
    }
  }

  /*
   * Can the current subject modify attributes?
   */
  private void subjectCanModAttr(
                 GrouperSession s, Group g, String attribute
               )
  {
    if ((
          (attribute.equals("displayname")) ||
          (attribute.equals("name"))        ||
          (attribute.equals("stem"))        ||
          (attribute.equals("extension"))
       ))
    {
      throw new RuntimeException(
                  "Modification of " + attribute + 
                  " is not currently allowed"
                );
    }
    if (!s.access().has(s, g, Grouper.PRIV_ADMIN)) {
      throw new RuntimeException(
                  "Modification requires ADMIN"
                );
    }
  }

  /*
   * Does the current subject have privs to modify the specified list?
   */
  protected static boolean subjectCanModListVal(
                             GrouperSession s, Group g, String list) {
    boolean rv = false;
    if (
        (s.access().has(s, g, Grouper.PRIV_UPDATE)) ||
        (s.access().has(s, g, Grouper.PRIV_ADMIN))
       )
    {
      rv = true;
    }
    return rv;
  }


  /*
   * PROTECTED INSTANCE METHODS
   */

  /*
   * Add a list value
   */
  protected void listAddVal(
                   GrouperSession s, Group g, 
                   GrouperMember m, String list
                 )
  {
    if (Group.subjectCanModListVal(s, g, list)) {
      GrouperList gl = new GrouperList(g, m, list);
      gl.load(s);
      GrouperList.validate(gl);
      if (GrouperList.exists(s, gl)) {
        throw new RuntimeException("List value already exists");
      }
      s.dbSess().txStart();
      try {
        this.listAddVal(s, gl); // Calculate mof and add vals
        if (this.initialized() == true) {
          // Only update modify attrs if group is fully loaded
          this.setModified();
        }
        s.dbSess().txCommit(); 
        Grouper.log().groupListAdd(s, g, m);
      } catch (RuntimeException e) {
        s.dbSess().txRollback();
        throw new RuntimeException("Error adding list value: " + e);
      }
    }
  }

  /*
   * Delete a list value.
   */
  protected void listDelVal(
                   GrouperSession s, Group g,
                   GrouperMember m, String list
                 )
  {
    if (Group.subjectCanModListVal(s, g, list)) {
      GrouperList gl = new GrouperList(g, m, list);
      gl.load(s);
      GrouperList.validate(gl);
      if (!GrouperList.exists(s, gl)) {
        throw new RuntimeException("List value does not exist");
      }
      s.dbSess().txStart();
      try {
        this.listDelVal(s, gl); // Calculate mof and delete vals
        this.setModified();
        s.dbSess().txCommit();
        Grouper.log().groupListDel(s, g, m);
      } catch (RuntimeException e) {
        s.dbSess().txRollback();
        throw new RuntimeException("Error deleting list value: " + e);
      }
    }
  }

  /*
   * Retrieve list values from specified list.
   */
  protected List listVals(GrouperSession s, Group g, String list) {
    String  qry  = "GrouperList.by.group.and.list";
    return this.queryListVals(s, qry, g.key(), list);
  }

  /*
   * Retrieve effective list values from specified list.
   */
  protected List listEffVals(GrouperSession s, Group g, String list) {
    String  qry   = "GrouperList.by.group.and.list.and.is.eff";
    return this.queryListVals(s, qry, g.key(), list);
  }

  /*
   * Retrieve immediate list values from specified list.
   */
  protected List listImmVals(GrouperSession s, Group g, String list) {
    String  qry   = "GrouperList.by.group.and.list.and.is.imm";
    return this.queryListVals(s, qry, g.key(), list);
  }


  /*
   * PRIVATE CLASS METHODS
   */

  /*
   * Delete a group.
   */
  private static void delete(GrouperSession s, Group g) {
    Group.subjectCanDelete(s, (Group) g);
    try {
      s.dbSess().txStart();
      g.revokeAllAccessPrivs(s);
      g.revokeAllNamingPrivs(s);
      GrouperAttribute.delete(s, g);
      GrouperSchema.delete(s, g);
      s.dbSess().session().delete(g);
      s.dbSess().txCommit();
    } catch (HibernateException e) {
      s.dbSess().txRollback();
      throw new RuntimeException("Error deleting group: " + e);
    }
  }


  /*
   * PRIVATE INSTANCE METHODS
   */

  /*
   * Add immediate and effective list values.
   */
  private void listAddVal(GrouperSession s, GrouperList gl) {
    // Find the list values that we will need to add
    MemberOf mof  = new MemberOf(s);
    Iterator iter = mof.memberOf(gl).iterator();
    // Now add the list values
    while (iter.hasNext()) {
      GrouperList lv = (GrouperList) iter.next();
      lv.load(s);
      GrouperList.save(s, lv);
    }
  }

  /*
   * Delete immediate and effective list values.
   */
  private void listDelVal(GrouperSession s, GrouperList gl) {
    // Find the list values that we will need to add
    MemberOf mof  = new MemberOf(s);
    Iterator iter = mof.memberOf(gl).iterator();
    // Now add the list values
    while (iter.hasNext()) {
      GrouperList lv = (GrouperList) iter.next();
      lv.load(s);
      GrouperList.delete(s, lv);
    }
  }

  /*
   * Return list values for specified query.
   */
  private List queryListVals(
                 GrouperSession s, String qry, String key, String list
               ) 
  {
    List vals = new ArrayList();
    try {
      Query q = s.dbSess().session().getNamedQuery(qry);
      q.setString(0, key);
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

  /* 
   * Revoke all access privs attached to a group
   */
  private void revokeAllAccessPrivs(GrouperSession s) {
    /* 
     * TODO This could be prettier, especially if/when there are custom
     *      privs
     */
    if (!(
          s.access().revoke(s, this, Grouper.PRIV_OPTIN)   &&
          s.access().revoke(s, this, Grouper.PRIV_OPTOUT)  &&
          s.access().revoke(s, this, Grouper.PRIV_VIEW)    &&
          s.access().revoke(s, this, Grouper.PRIV_READ)    &&
          s.access().revoke(s, this, Grouper.PRIV_UPDATE)  &&
          s.access().revoke(s, this, Grouper.PRIV_ADMIN)
       ))
    {
      throw new RuntimeException("Error revoking access privileges");
    }
  }

  /* 
   * Revoke all naming privs attached to a group
   */
  private void revokeAllNamingPrivs(GrouperSession s) {
    // FIXME This is ugly 
    if (this.type().equals(Grouper.NS_TYPE)) {
      if (!(
            s.naming().revoke(s, (GrouperStem) this, Grouper.PRIV_STEM)    &&
            s.naming().revoke(s, (GrouperStem) this, Grouper.PRIV_CREATE) 
         ))
      {       
        throw new RuntimeException("Error revoking naming privileges");
      }
    }
  }

}

