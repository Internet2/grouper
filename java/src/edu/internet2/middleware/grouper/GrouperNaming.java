package edu.internet2.middleware.directory.grouper;

import java.util.List;

/** 
 * {@link Grouper} Naming Interface.
 *
 * @author  blair christensen.
 * @version $Id: GrouperNaming.java,v 1.1 2004-04-11 03:13:44 blair Exp $
 */
public interface GrouperNaming {
  public List allowedStems();
  public boolean allowedStems(String stem);
}

