package edu.internet2.middleware.directory.grouper;

import  edu.internet2.middleware.directory.grouper.*;
import  java.util.List;

/** 
 * Default implementation of the {@link GrouperNaming} interface.
 *
 * @author  blair christensen.
 * @version $Id: GrouperNamingImpl.java,v 1.2 2004-04-29 03:43:58 blair Exp $
 */
public class InternalGrouperNaming implements GrouperNaming {
  public InternalGrouperNaming(GrouperSession s) {
    // Nothing -- Yet
  }

  public List allowedStems() {
    return null;
  }

  public boolean allowedStems(String stem) {
    return false;
  }
}

