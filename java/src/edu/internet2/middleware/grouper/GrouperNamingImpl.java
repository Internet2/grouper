package edu.internet2.middleware.directory.grouper;

import  edu.internet2.middleware.directory.grouper.*;
import  java.util.*;

/** 
 * Default implementation of the {@link GrouperNaming} interface.
 *
 * @author  blair christensen.
 * @version $Id: GrouperNamingImpl.java,v 1.8 2004-08-03 01:31:21 blair Exp $
 */
public class InternalGrouperNaming implements GrouperNaming {

  private GrouperSession intSess = null;

  /**
   * Create a naming interface object.
   *
   * @param   s   Session context.
   */
  public InternalGrouperNaming(GrouperSession s) {
    // Internal reference to the session we are using.
    intSess = s;
  }

  /**
   * Grant a naming privilege on a {@link Grouper} stem.
   */
  public void grant(GrouperStem stem, GrouperMember m, String priv) {
    // Nothing -- Yet
  }

  /**
   * Revoke a naming privilege on a {@link Grouper} stem.
   */
  public void revoke(GrouperStem stem, GrouperMember m, String priv) {
    // Nothing -- Yet
  }

  /**
   * Return all naming privileges that the current session's subject has on 
   * a {@link Grouper} stem.
   */
  public List has(GrouperStem stem) {
    List privs = new ArrayList();
    return privs; 
  }

  /**
   * Return all naming privileges for a {@link GrouperMember} on a 
   * {@link Grouper} stem.
   */
  public List has(GrouperStem stem, GrouperMember m) {
    List privs = new ArrayList();
    return privs; 
  }

  /**
   * Verify whether the current session's subject has a specified 
   * naming privilege on a {@link Grouper} stem.
   */
  public boolean has(GrouperStem stem, String priv) {
    return false;
  }

  /**
   * Verify whether a {@link GrouperMember} has a specified naming privilege
   * on a {@link Grouper} stem.
   */
  public boolean has(GrouperStem stem, GrouperMember m, String priv) {
    return false;
  }

  /**
   * List stems where the current subject has the specified privilege.
   * <p>
   * TODO 
   *
   * @param   priv  Query for this privilege type.
   * @return  List of {@link GrouperStem} stems.
   */
  public List has(String priv) {
    List stems = new ArrayList();
    return stems;
  }

  /**
   * List stems where the specified member has the specified
   * privilege.
   * <p>
   * TODO 
   *
   * @param   m     Query for this {@link GrouperMember}.
   * @param   priv  Query for this privilege type.
   * @return  List of {@link GrouperStem} stems.
   */
  public List has(GrouperMember m, String priv) {
    List stems = new ArrayList();
    return stems;
  }

}

