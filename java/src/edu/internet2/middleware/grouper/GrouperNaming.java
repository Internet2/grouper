package edu.internet2.middleware.directory.grouper;

import java.util.List;

/** 
 * {@link Grouper} Naming Interface.
 *
 * @author  blair christensen.
 * @version $Id: GrouperNaming.java,v 1.2 2004-04-29 16:14:08 blair Exp $
 */
public interface GrouperNaming {
  /**
   * List of all stems that can be created by a <i>subjectID</i>.
   * 
   * @return  List of all stems that can be created. 
   */
  public List allowedStems();

  /**
   * Verifies whether the given stem can be created by a
   * <i>subjectID</i>.
   *
   * @param   stem  The stem to verify.
   * @return  True if allowed to create <i>stem</i>.
   */
  public boolean allowedStems(String stem);
}

