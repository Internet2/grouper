package edu.internet2.middleware.directory.grouper;

/** 
 * {@link Grouper} Privilege Interface.
 *
 * @author  blair christensen.
 * @version $Id: GrouperAccess.java,v 1.1 2004-04-11 03:13:44 blair Exp $
 */
public interface GrouperPrivilege {
  public boolean assignPrivilege(GrouperGroup g, String priv);
  public boolean hasPrivilege(GrouperGroup g, String priv);
}

