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
import  org.apache.commons.lang.builder.ToStringBuilder;


/** 
 * Abstract Group class.
 * <p />
 *
 * @author  blair christensen.
 * @version $Id: Group.java,v 1.44 2005-08-24 19:19:54 blair Exp $
 */
abstract public class Group {

  /*
   * ABSTRACT METHODS 
   */

  abstract public     String            getDisplayExtension();
  abstract public     String            getDisplayName();
  abstract public     String            getExtension();
  abstract public     List              getMembers();
  abstract public     String            getName();
  abstract public     String            getStem();
  abstract public     String            id();
  abstract public     String            type();

  abstract protected  GrouperAttribute  attribute(String attribute);
  abstract protected  void              attribute(String attribute, String value);
  abstract protected  void              attributeAdd(GrouperAttribute attr);
  abstract protected  void              attributeDel(GrouperAttribute attr);
  abstract protected  String            createSource();
  abstract protected  Subject           createSubject();
  abstract protected  Date              createTime();
  abstract protected  String            getCreateSource();
  abstract protected  String            getCreateSubject();
  abstract protected  String            getCreateTime();
  abstract protected  String            getGroupComment();
  abstract protected  String            getGroupID();
  abstract protected  String            getGroupKey();
  abstract protected  String            getModifySource();
  abstract protected  String            getModifySubject();
  abstract protected  String            getModifyTime();
  abstract protected  boolean           initialized();
  abstract protected  String            key();
  abstract protected  void              listAddVal(GrouperMember m);
  abstract protected  void              listAddVal(GrouperMember m, String list);
  abstract protected  void              listDelVal(GrouperMember m);
  abstract protected  void              listDelVal(GrouperMember m, String list);
  abstract protected  List              listVals();
  abstract protected  List              listVals(String list);
  abstract protected  List              listEffVals();
  abstract protected  List              listEffVals(String list);
  abstract protected  List              listImmVals();
  abstract protected  List              listImmVals(String list);
  abstract protected  void              load(GrouperSession s);
  abstract protected  String            modifySource();
  abstract protected  Subject           modifySubject();
  abstract protected  Date              modifyTime();
  abstract protected  void              setCreateSource(String createSource);
  abstract protected  void              setCreateSubject(String createSubject);
  abstract protected  void              setCreateTime(String createTime);
  abstract protected  void              setGroupComment(String comment);
  abstract protected  void              setGroupID(String id);
  abstract protected  void              setGroupKey(String key);
  abstract protected  void              setModified();
  abstract protected  void              setModifySource(String modifySource);
  abstract protected  void              setModifySubject(String modifySubject);
  abstract protected  void              setModifyTime(String modifyTime);


  /*
   * PUBLIC CLASS METHODS
   */

  /**
   * Delete a group.
   * <p />
   * @param s   Delete group within this session.
   * @param g   Delete this group.
   */
  public static void delete(GrouperSession s, GrouperGroup g) {
    // TODO Merge common code with ns version?
    Group.subjectCanDelete(s, g);
    try {
      s.dbSess().txStart();
      ( (GrouperGroup) g ).revokeAllAccessPrivs(s);
      GrouperAttribute.delete(s, g);
      GrouperSchema.delete(s, g);
      s.dbSess().session().delete(g);
      s.dbSess().txCommit();
    } catch (HibernateException e) {
      s.dbSess().txRollback();
      throw new RuntimeException("Error deleting group: " + e);
    }
  }

  /**
   * Delete a stem.
   * TODO Should this be a supported operation?
   * <p />
   * @param s   Delete namespace within this session.
   * @param ns  Delete this namespace.
   */
  public static void delete(GrouperSession s, GrouperStem ns) {
    // TODO Merge common code with g version?
    Group.subjectCanDelete(s, ns);
    try {
      s.dbSess().txStart();
      ( (GrouperStem) ns ).revokeAllNamingPrivs(s);
      GrouperAttribute.delete(s, ns);
      GrouperSchema.delete(s, ns);
      s.dbSess().session().delete(ns);
      s.dbSess().txCommit();
    } catch (HibernateException e) {
      s.dbSess().txRollback();
      throw new RuntimeException("Error deleting group: " + e);
    }
  }

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
   * PUBLIC INSTANCE METHODS
   */

  /**
   * Retrieve the value of the <i>name</i> attribute.
   * <p>
   * This is a convenience method.  The value can also be retrieved
   * using the <i>attribute()</i> method.
   *
   * @return  Name of group.
   */
  public String name() {
    return this.getName();
  }

  /**
   * Return a string representation of this object.
   * <p />
   * @return String representation of this object.
   */
  public String toString() {
    return new ToStringBuilder(this)            .
      append("type"     , this.type()         ) .
      append("id"       , this.getGroupID()   ) .
      append("stem"     , this.getStem()      ) .
      append("extension", this.getExtension() ) .
      toString();
  }

  /*
   * PROTECTED CLASS METHODS
   */

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
          "Error retrieving result for " + qry + ": " + e.getMessage()
        );
      }
    } catch (HibernateException e) {
      throw new RuntimeException(
        "Unable to get query " + qry + ": " + e.getMessage()
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
          "Error retrieving result for " + qry + ": " + e.getMessage()
        );
      }
    } catch (HibernateException e) {
      throw new RuntimeException(
        "Unable to get query " + qry + ": " + e.getMessage()
      );
    }
    return key;
  }

  /*
   * Find and return the group key for (stem, extn, type).
   */
  protected static String findKeyByStemExtnType(
    GrouperSession s, String stem, String extn, String type
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
          "Error retrieving result for " + qry + ": " + e.getMessage()
        );
      }
    } catch (HibernateException e) {
      throw new RuntimeException(
        "Unable to get query " + qry + ": " + e.getMessage()
      );
    }
    return key;
  }

  /*
   * No nulls, no blanks
   */
  protected static void invalidStemOrExtn(String stem, String extn) {
    if        (stem == null) {
      throw new RuntimeException("null stem");
    } else if (stem.equals("")) {
      throw new RuntimeException("blank stem");
    }
    if        (extn == null) {
      throw new RuntimeException("null extension");
    } else if (extn.equals("")) {
      throw new RuntimeException("blank extension");
    }
  }

  /*
   * Load {@link Group} by id.
   * @throws {@link InsufficientPrivilegeException}
   */
  protected static Group _loadByID(GrouperSession s, String id) 
    throws InsufficientPrivilegeException
  {
    return Group.loadByKey( s, Group.findKeyByID(s, id) );
  }

  /*
   * Return appropriate list keys for a given (group) key and list
   */
  protected static List listValsKeys(GrouperSession s, String key, String list) {
    String  qry  = "GrouperList.as.key.by.group.and.list";
    return Group.queryListValsKeys(s, qry, key, list);
  }

  /*
   * Load {@link Group} by key.
   * @throws {@link InsufficientPrivilegeException}
   */
  protected static Group loadByKey(GrouperSession s, String key) 
    throws InsufficientPrivilegeException
  {
    Group g = null;
    if (key != null) {
      try {
        g = (Group) s.dbSess().session().get(Group.class, key);
        g.load(s); 
        // TODO Can a group be loaded anywhere else?
        Group.validate(g);
        s.canVIEW(g);
      } catch (GrouperException e) {
        return null; // TODO *sigh*
      } catch (HibernateException e) {
        throw new RuntimeException(
          "Error loading group: " + e.getMessage()
        );
      }
    }
    return g; // FIXME Don't return null!
  }

  /*
   * Load {@link Group} by name and type.
   * @throws {@link InsufficientPrivilegeException}
   */
  protected static Group loadByNameAndType(
    GrouperSession s, String name, String type
  )
    throws InsufficientPrivilegeException
  {
    return Group.loadByKey(s, Group.findKeyByNameAndType(s, name, type));
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
   * @throws {@link InsufficientPrivilegeException}
   */
  protected static void subjectCanCreateGroup(
    GrouperSession s, String stem
  )
    throws InsufficientPrivilegeException
  {
    // Load stem for priv checking
    String key = Group.findKeyByNameAndType(s, stem, Grouper.NS_TYPE);
    if (key != null) {
      GrouperStem ns = (GrouperStem) Group.loadByKey(s, key);
      if (ns != null) { // TODO Flail if null?
        try {
          s.canCREATE(ns);
        } catch (InsufficientPrivilegeException e) {
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
   * @throws {@link InsufficientPrivilegeException}
   */
  protected static void subjectCanCreateStem(
    GrouperSession s, String stem
  )
    throws InsufficientPrivilegeException
  {
    // Load stem for priv checking
    String key = Group.findKeyByNameAndType(s, stem, Grouper.NS_TYPE);
    if (key != null) {
      GrouperStem ns = (GrouperStem) Group.loadByKey(s, key);
      if (ns != null) { // TODO Flail if null?
        // If a naming group, does subject have STEM on stem?
        try {
          s.canSTEM(ns);
        } catch (InsufficientPrivilegeException e) {
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
  protected static void subjectCanDelete(GrouperSession s, GrouperGroup g) {
    // Right priv required
    if (!s.access().has(s, g, Grouper.PRIV_ADMIN)) {
      throw new RuntimeException("Deletion requires ADMIN priv");
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
   * Does the current subject have privs to delete the current stem?
   */
  protected static void subjectCanDelete(GrouperSession s, GrouperStem ns) {
    // Right priv required
    try {
      s.canSTEM(ns);
    } catch (InsufficientPrivilegeException e) {
      throw new RuntimeException("Deletion requires STEM priv");
    }
    // Are there child stems?
    if (ns.stems().size() > 0) {
      throw new RuntimeException(
        "Cannot delete stem with child stems: " + ns.stems().size()
      );
    }
    // Are there child groups?
    if (ns.groups().size() > 0) {
      throw new RuntimeException(
        "Cannot delete stem with child groups: " + ns.groups().size()
      );
    }
  }

  /*
   * Simple object validation.
   */
  protected static void validate(Group g) 
    throws GrouperException
  {
    // TODO GrouperException isn't right
    if (g == null) {
      throw new GrouperException("group is null");
    }
    if ( (g.getGroupID() == null) || (g.getGroupID().equals("")) ) {
      // By pass access checks by using the get* version
      throw new GrouperException("group has no id");
    }
    if ( (g.type() == null) || g.type().equals("") ) {
      throw new GrouperException("group has no type");
    }
  }


  /*
   * PROTECTED INSTANCE METHODS
   */

  /*
   * Set an attribute value
   */ 
  protected void attribute(
    GrouperSession s, Group g, String attribute, String value
  )
  {
    if (!Grouper.groupField(g.type(), attribute)) {
      throw new RuntimeException(
        "Attribute is not valid for this group type"
      );
    }
    this.subjectCanModAttr(s, g, attribute);
    try {
      s.dbSess().txStart();
      if ( (value == null) || (value.equals("")) ) {
        // Delete 
        if (!attribute.equals("displayExtension")) { 
          g.attributeDel( g.attribute(attribute) );
        } else {
          // Reset - _displayExtension_ should always have a value
          value = g.getExtension();
          GrouperAttribute attr = g.attribute(attribute);
          attr.setGroupFieldValue(value);
          g.attributeAdd(attr);
        }
      } else {
        // Add-Or-Update
        GrouperAttribute attr = g.attribute(attribute);
        // TODO Isn't this sort of defeating the purpose?
        if (attr instanceof NullGrouperAttribute) {
          attr = new GrouperAttribute(
            g.key(), attribute, value
          );
        } else {
          attr.setGroupFieldValue(value);
        }
        g.attributeAdd(attr);
      }
      updateDisplayedAttributes(s, g, attribute, value);
      g.setModified();
      s.dbSess().txCommit();
    } catch (RuntimeException e) {
      s.dbSess().txRollback();
      throw new RuntimeException(
        "Error modifying attribute: " + e.getMessage()
      );
    }
  }

  // Get a group or stem's _displayName_
  protected static String getDisplayName(String stem, String extn) {
    return Group.getDisplayName( GrouperSession.getRootSession(), stem, extn );
  }

  // Get a group or stem's _displayName_
  // TODO Is there a reason I can't just use the root session?
  protected static String getDisplayName(
    GrouperSession s, String stem, String extn
  )
  {
    String  displayName       = new String();
    String  qry               = "GrouperAttribute.string.value.by.key";
    String  parentName        = stem;
    String  parentDisplayName = stem;
    if (stem.equals(Grouper.NS_ROOT)) {
      return extn;
    }
    try {
      Query q = s.dbSess().session().getNamedQuery(qry);
      q.setString(0, parentName);     // name we are searching for
      q.setString(1, "displayName");  // attr we want
      try {
        if (q.list().size() == 1) {
          parentDisplayName =(String) q.list().get(0);
        }
        displayName = parentDisplayName + Grouper.HIER_DELIM + extn;
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
    return displayName;
  }

  /*
   * Check for a list membership.
   */
  protected boolean hasMember(
    GrouperSession s, GrouperMember m, String list
  ) 
  {
    String  qry = "GrouperList.as.key.by.group.and.member.and.list";
    boolean rv  = false;
    try {
      Query q = s.dbSess().session().getNamedQuery(qry);
      q.setString(0, this.key());
      q.setString(1, m.key());
      q.setString(2, list);
      try {
        // Account for both immediate and effective mships
        if (q.list().size() >= 1) {
          rv = true;
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
    return rv;
  }

  /*
   * Add a list value
   */
  protected void listAddVal(
    GrouperSession s, Group g, GrouperMember m, String list
  )
  {
    GrouperSession.validate(s);
    GrouperMember.validate(m);
    try {
      if (m.memberID().equals(s.getMember().memberID())) {
        s.canOPTIN(g);  
      } else {
        s.canWriteField(g, list);
      }
      GrouperList gl = new GrouperList(s, g, m, list);
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
    } catch (InsufficientPrivilegeException e) {
      throw new RuntimeException(
        s.subject() + " does not have privilege to add " + m + 
        " to " + g + "/" + list
      );
    }
  }

  /*
   * Delete a list value.
   */
  protected void listDelVal(
    GrouperSession s, Group g, GrouperMember m, String list
  )
  {
    GrouperSession.validate(s);
    GrouperMember.validate(m);
    try {
      if (m.equals(s.getMember())) {
        s.canOPTOUT(g);  
      } else {
        s.canWriteField(g, list);
      }
      GrouperList gl = new GrouperList(s, g, m, list);
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
    } catch (InsufficientPrivilegeException e) {
      throw new RuntimeException(
        s.subject() + " does not have privileges to delete " + m + 
        " to " + g + "/" + list
      );
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
   * Number of seconds since the epoch.
   */
  protected String now() {
    java.util.Date now = new java.util.Date();
    return Long.toString(now.getTime());
  }

  /* 
   * Revoke all access privs attached to a group
   */
  protected void revokeAllAccessPrivs(GrouperSession s) {
    /* 
     * TODO This could be prettier, especially if/when there are custom
     *      privs
     */
    if (!(
          s.access().revoke(s, (GrouperGroup) this, Grouper.PRIV_OPTIN)   &&
          s.access().revoke(s, (GrouperGroup) this, Grouper.PRIV_OPTOUT)  &&
          s.access().revoke(s, (GrouperGroup) this, Grouper.PRIV_VIEW)    &&
          s.access().revoke(s, (GrouperGroup) this, Grouper.PRIV_READ)    &&
          s.access().revoke(s, (GrouperGroup) this, Grouper.PRIV_UPDATE)  &&
          s.access().revoke(s, (GrouperGroup) this, Grouper.PRIV_ADMIN)
       ))
    {
      throw new RuntimeException("Error revoking access privileges");
    }
  }

  /* 
   * Revoke all naming privs attached to a group
   */
  protected void revokeAllNamingPrivs(GrouperSession s) {
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

  /*
   * Convert a string to a date object.
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
   * PRIVATE CLASS METHODS
   */

  /*
   * Run a named list value query and return the relevant list keys
   */
  private static List queryListValsKeys(
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
          String lk = (String) iter.next();
          vals.add(lk);
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
    return vals;
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
      GrouperList.delete(s, lv);
    }
  }

  /*
   * Return list values for specified query.
   * TODO Start using _Group.queryListValsKeys_?
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
          GrouperList gl = (GrouperList) iter.next();
          gl.setSession(s); // TODO Remove elsewhere?
          vals.add(gl);
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
    return vals;
  }

  /*
   * Can the current subject modify attributes?
   */
  private void subjectCanModAttr(
    GrouperSession s, Group g, String attribute
  )
  {
    if ((
          // TODO No longer needed?
          // (attribute.equals("displayname")) ||
          (attribute.equals("name"))        ||
          (attribute.equals("stem"))        ||
          (attribute.equals("extension"))
       ))
    {
      throw new RuntimeException(
        "Modification of " + attribute + " is not currently allowed"
      );
    }
    if (this.type().equals(Grouper.NS_TYPE)) {
      // Stems require STEM...
      try {
        s.canSTEM( (GrouperStem) g );
      } catch (InsufficientPrivilegeException eCS) {
        try {
          // Or maybe something else?
          // TODO We now delve into deep and not necessarly accurate
          // hackery
          s.canWriteField(g, attribute);
        } catch (InsufficientPrivilegeException eCWF) {
          throw new RuntimeException("Modification not permitted");
        }
      }
    } else {
      try {
        s.canWriteField(g, attribute);
      } catch (InsufficientPrivilegeException e) {
        throw new RuntimeException("Modification not permitted");
      }
    }
  }

  /*
   * Update _displayExtension_ and _displayName_ as appropriate,
   * including recursively if necessary.
   */
  private void updateDisplayedAttributes(
    GrouperSession s, Group g, String attr, String extn
  ) 
  {
    GrouperAttribute dn = g.attribute("displayName");
    // Update this group
    if (attr.equals("displayExtension")) {
      dn.setGroupFieldValue( Group.getDisplayName(s, g.getStem(), extn) );
      g.attributeAdd(dn);
    }
    // And update children if a stem
    if (
        (g.type().equals(Grouper.NS_TYPE)) &&
        (
          (attr.equals("displayExtension")) ||
          (attr.equals("displayName"))
        )
       )
    {
      Iterator groups = ( (GrouperStem) g ).groups().iterator();
      while (groups.hasNext()) {
        GrouperGroup      cg = (GrouperGroup) groups.next();
        // Attempt to evade access controls
        String            an = "displayName";
        GrouperAttribute  ao = cg.attribute(an);
        ao.setGroupFieldValue(
          Group.getDisplayName( dn.value(), cg.getDisplayExtension() )
        );
        cg.attributeAdd(ao);
      }
      Iterator stems = ( (GrouperStem) g ).stems().iterator();
      while (stems.hasNext()) {
        GrouperStem cs = (GrouperStem) stems.next();
        cs.attribute(
          "displayName", 
          Group.getDisplayName( dn.value(), cs.getDisplayExtension() )
        );
      }
      
    }
  }

}

