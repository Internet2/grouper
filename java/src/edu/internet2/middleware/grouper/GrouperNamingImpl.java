package edu.internet2.middleware.directory.grouper;

import java.util.List;

/** 
 * Default implementation of the {@link GrouperNaming} interface.
 *
 * @author  blair christensen.
 * @version $Id: GrouperNamingImpl.java,v 1.1 2004-04-11 22:55:35 blair Exp $
 */
public class InternalGrouperNaming implements GrouperNaming {
  public List allowedStems() {
    return null;
  }

  public boolean allowedStems(String stem) {
    return false;
  }
}

