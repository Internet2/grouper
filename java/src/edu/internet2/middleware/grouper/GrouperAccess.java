package edu.internet2.middleware.directory.grouper;

/** 
 * {@link Grouper} Privilege Interface.
 *
 * @author  blair christensen.
 * @version $Id: GrouperAccess.java,v 1.2 2004-04-11 03:15:18 blair Exp $
 */
public interface GrouperPrivilege {
  public boolean assign(GrouperGroup g, String priv);
  public boolean has(GrouperGroup g, String priv);
}

