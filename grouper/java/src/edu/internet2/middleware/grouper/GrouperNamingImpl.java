package edu.internet2.middleware.directory.grouper;

import  edu.internet2.middleware.directory.grouper.*;
import  java.util.List;

/** 
 * Default implementation of the {@link GrouperNaming} interface.
 *
 * @author  blair christensen.
 * @version $Id: GrouperNamingImpl.java,v 1.3 2004-04-29 16:14:44 blair Exp $
 */
public class InternalGrouperNaming implements GrouperNaming {

  private GrouperSession intSess = null;

  public InternalGrouperNaming(GrouperSession s) {
    // Internal reference to the session we are using.
    intSess = s;
  }

  /**
   * List of all stems that can be created by a <i>subjectID</i>.
   * <p>
   * <ul>
   *  <li>XXX Fetch and return <i>GrouperCreator:*</i> memberships
   *      from the <i>grouper_membership</i> table?</li>
   *  <li>XXX Add to docs/examples/.</li>
   * </ul>
   * 
   */
  public List allowedStems() {
    return null;
  }

  /**
   * Verifies whether the given stem can be created by a
   * <i>subjectID</i>.
   * <p>
   * <ul>
   *  <li>XXX Fetch <i>GrouperCreator:*</i> memberships from the
   *      <i>grouper_membership</i> table?</li>
   *  <li>Determine whether <i>subjectID</i> is allowed to create
   *      <i>stem</i>.</li>
   *  <li>XXX Add to docs/examples/.</li>
   * </ul>
   *
   */
  public boolean allowedStems(String stem) {
    return false;
  }
}

