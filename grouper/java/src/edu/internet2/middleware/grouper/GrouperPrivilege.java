/* 
 * Copyright (C) 2004 Internet2
 * Copyright (C) 2004 The University Of Chicago
 * All Rights Reserved. 
 *
 * See the LICENSE file in the top-level directory of the 
 * distribution for licensing information.
 */

package edu.internet2.middleware.grouper;

import  edu.internet2.middleware.grouper.*;
import  java.lang.reflect.*;
import  java.util.*;
import  org.apache.commons.lang.builder.ToStringBuilder;


/** 
 * Class providing read/write access to Grouper privileges.
 *
 * @author  blair christensen.
 * @version $Id: GrouperPrivilege.java,v 1.1 2004-11-20 18:46:36 blair Exp $
 */
public class GrouperPrivilege {

  /*
   * PRIVATE CLASS VARIABLES
   */
  private static GrouperAccess access; // Access priv interface
  private static GrouperNaming naming; // Naming priv interface
  private static boolean       initialized = false;


  /*
   * PUBLIC CLASS METHODS
   */

  /**
   * Grant an access privilege on a {@link GrouperGroup}.
   * <p>
   *
   * @param   s     Act within this {@link GrouperSession}.
   * @param   g     Grant privileges on this {@link GrouperGroup}.
   * @param   m     Grant privileges for this {@link GrouperMember}.
   * @param   priv  Privilege to grant.
   */
  public static void grant(
                           GrouperSession s, GrouperGroup g, 
                           GrouperMember m, String priv
                          ) 
  {
    GrouperPrivilege._init();
    access.grant(s, g, m, priv);
  }

  /**
   * Grant a naming privilege on a {@link GrouperStem}.
   * <p>
   *
   * @param   s     Act within this {@link GrouperSession}.
   * @param   stem  Grant privileges on this {@link Grouper} stem.
   * @param   m     Grant privileges for this {@link GrouperMember}.
   * @param   priv  Privilege to grant.
   */
  public static void grant(
                           GrouperSession s, GrouperStem stem, 
                           GrouperMember m, String priv
                          ) 
  {
    GrouperPrivilege._init();
    naming.grant(s, stem, m, priv);
  }

  /**
   * List access privileges for current subject on the specified group.
   * <p>
   *
   * @param   s   Act within this {@link GrouperSession}.
   * @param   g   List privileges on this group.
   * @return  List of privileges.
   */
  public static List has(GrouperSession s, GrouperGroup g) {
    GrouperPrivilege._init();
    return access.has(s, g);
  }

  /**
   * List naming privileges for current subject on the specified stem.
   * <p>
   *
   * @param   s     Act within this {@link GrouperSession}.
   * @param   stem  List privileges on this stem.
   * @return  List of privileges.
   */
  public static List has(GrouperSession s, GrouperStem stem) {
    GrouperPrivilege._init();
    return naming.has(s, stem);
  }

  /**
   * List groups or stems, depending upon privilege type, where the
   * current subject has the specified privilege.
   * <p>
   *
   * @param   s     Act within this {@link GrouperSession}.
   * @param   priv  Query for this privilege type.
   * @return  List of {@link GrouperStem} stems.
   */
  public static List has(GrouperSession s, String priv) {
    GrouperPrivilege._init();
    // TODO Dispatch based upon priv type
    // return access.has(s, priv);
    // return naming.has(s, priv);
    return null;
  }

  /**
   * Verify whether current subject has the specified privilege on the
   * specified group.
   * <p>
   *
   * @param   s     Act within this {@link GrouperSession}.
   * @param   g     Verify privilege for this group.
   * @param   priv  Verify this privilege.
   * @return  True if subject has this privilege on the group.
   */
  public static boolean has(GrouperSession s, GrouperGroup g, String priv) {
    GrouperPrivilege._init();
    return access.has(s, g, priv);
  }

  /**
   * Verify whether current subject has the specified privilege on the
   * specified stem.
   * <p>
   *
   * @param   s     Act within this {@link GrouperSession}.
   * @param   stem  Verify privilege for this stem.
   * @param   priv  Verify this privilege.
   * @return  True if subject has this privilege on the stem.
   */
  public static boolean has(GrouperSession s, GrouperStem stem, String priv) {
    GrouperPrivilege._init();
    return naming.has(s, stem, priv);
  }

  /**
   * List access privileges for specified member on the specified group.
   * <p>
   *
   * @param   s     Act within this {@link GrouperSession}.
   * @param   g     Return privileges for this {@link GrouperGroup}.
   * @param   m     List privileges for this {@link GrouperMember}.
   * @return  List of privileges.
   */
  public static List has(GrouperSession s, GrouperGroup g, GrouperMember m) {
    GrouperPrivilege._init();
    return access.has(s, g, m);
  }

  /**
   * List groups or stems, depending upon the privilege type, where
   * the specified member has the specified privilege.
   * <p>
   *
   * @param   s     Act within this {@link GrouperSession}.
   * @param   m     Query for this {@link GrouperMember}.
   * @param   priv  Query for this privilege type.
   * @return  List of {@link GrouperGroup} groups.
   */
  public static List has(GrouperSession s, GrouperMember m, String priv) {
    GrouperPrivilege._init();
    // TODO Dispatch based upon priv type
    // return access.has(s, m, priv);
    // return naming.has(s, stem, m);
    return null;
  }



  /**
   * Verify whether the specified member has the specified privilege
   * on the specified group.
   * <p>
   *
   * @param   s     Act within this {@link GrouperSession}.
   * @param   g     Verify privilege for this group.
   * @param   m     Verify privilege for this member.
   * @param   priv  Verify this privilege.
   * @return  True if subject has this privilege on the group.
   */
  public static boolean has(
                            GrouperSession s, GrouperGroup g, 
                            GrouperMember m, String priv
                           )
  {
    GrouperPrivilege._init();
    return access.has(s, g, m, priv);
  }

  /**
   * Verify whether the specified member has the specified privilege
   * on the specified stem.
   * <p>
   *
   * @param   s     Act within this {@link GrouperSession}.
   * @param   stem  Verify privilege for this stem.
   * @param   m     Verify privilege for this member.
   * @param   priv  Verify this privilege.
   * @return  True if subject has this privilege on the stem.
   */
  public static boolean has(
                            GrouperSession s, GrouperStem stem, 
                            GrouperMember m, String priv
                           ) 
  {
    GrouperPrivilege._init();
    return naming.has(s, stem, m, priv);
  }

  /**
   * Revoke an access privilege.
   * <p>
   *
   * @param   s     Act within this {@link GrouperSession}.
   * @param   g     Revoke privilege on this {@link GrouperGroup}.
   * @param   m     Revoke privilege for this{@link GrouperMember}.
   * @param   priv  Privilege to revoke.
   */
  public static void revoke(
                            GrouperSession s, GrouperGroup g, 
                            GrouperMember m, String priv
                           ) 
  {
    GrouperPrivilege._init();
    access.revoke(s, g, m, priv);
  }

  /**
   * Revoke a naming privilege on a {@link Grouper} stem.
   * <p>
   *
   * @param   s     Act within this {@link GrouperSession}.
   * @param   stem  Revoke privilege on this {@link Grouper} stem.
   * @param   m     Revoke privilege for this{@link GrouperMember}.
   * @param   priv  Privilege to revoke.
   */
  public static void revoke(
                            GrouperSession s, GrouperStem stem, 
                            GrouperMember m, String priv
                           )  
  {
    GrouperPrivilege._init();
    naming.revoke(s, stem, m, priv);
  }


  /*
   * PRIVATE STATIC METHODS
   */

  /*
   * Initialize static interfaces
   */
  private static void _init() {
    if (initialized == false) {
      access = (GrouperAccess) GrouperPrivilege._interfaceCreate( 
                Grouper.config("interface.access" ) 
               );
      naming = (GrouperNaming) GrouperPrivilege._interfaceCreate( 
                Grouper.config("interface.naming" ) 
               );
      initialized = true;
    }
  }

  /*
   * Instantiate an interface reflectively
   */
  private static Object _interfaceCreate(String name) {
    try {
      Class classType     = Class.forName(name);
      Class[] paramsClass = new Class[] { };
      try {
        Constructor con     = classType.getDeclaredConstructor(paramsClass);
        Object[] params     = new Object[] { };
        try {
          return con.newInstance(params);
        } catch (Exception e) {
          System.err.println("Unable to instantiate class: " + name);
          System.exit(1);
        }
      } catch (NoSuchMethodException e) {
        System.err.println("Unable to find constructor for class: " + name);
        System.exit(1);
      }
    } catch (ClassNotFoundException e) {
      System.err.println("Unable to find class: " + name);
      System.exit(1);
    }
    return null;
  }

}

