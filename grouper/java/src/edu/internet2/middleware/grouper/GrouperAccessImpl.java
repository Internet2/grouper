package edu.internet2.middleware.directory.grouper;

/** 
 * Default implementation of the {@link GrouperPrivilege} interface.
 *
 * @author  blair christensen.
 * @version $Id: GrouperAccessImpl.java,v 1.1 2004-04-11 22:55:35 blair Exp $
 */
public class InternalGrouperPrivilege implements GrouperPrivilege {
  public boolean assign(GrouperGroup g, String priv) {
    return false;
  }

  public boolean has(GrouperGroup g, String priv) {
    return false;
  }
}

