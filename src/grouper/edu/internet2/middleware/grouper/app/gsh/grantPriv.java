/*
 * Copyright (C) 2006-2007 blair christensen.
 * All Rights Reserved.
 *
 * You may use and distribute under the same terms as Grouper itself.
 */

package edu.internet2.middleware.grouper.app.gsh;
import  bsh.*;
import  edu.internet2.middleware.grouper.*;
import edu.internet2.middleware.grouper.exception.GrantPrivilegeException;
import edu.internet2.middleware.grouper.exception.GroupNotFoundException;
import edu.internet2.middleware.grouper.exception.InsufficientPrivilegeException;
import edu.internet2.middleware.grouper.exception.SchemaException;
import edu.internet2.middleware.grouper.exception.StemNotFoundException;
import edu.internet2.middleware.grouper.privs.Privilege;
import  edu.internet2.middleware.subject.*;

/**
 * Grant a privilege.
 * <p/>
 * @author  blair christensen.
 * @version $Id: grantPriv.java,v 1.1 2008-07-21 21:01:59 mchyzer Exp $
 * @since   0.0.1
 */
public class grantPriv {

  // PUBLIC CLASS METHODS //

  /**
  /**
   * Grant a privilege.
   * <p/>
   * @param   i           BeanShell interpreter.
   * @param   stack       BeanShell call stack.
   * @param   name        Grant privilege on this {@link Group} or {@link Stem}.
   * @param   subjId      Grant privilege to this {@link Subject}.
   * @param   priv        Grant this {@link Privilege}.
   * @return  True if succeeds.
   * @throws  GrouperShellException
   * @since   0.0.1
   */
  public static boolean invoke(
    Interpreter i, CallStack stack, String name,  String subjId, Privilege priv
  ) 
    throws  GrouperShellException
  {
    GrouperShell.setOurCommand(i, true);
    try {
      GrouperSession  s     = GrouperShell.getSession(i);
      Subject         subj  = SubjectFinder.findById(subjId);
      if (Privilege.isAccess(priv)) {
        Group   g     = GroupFinder.findByName(s, name);
        g.grantPriv(subj, priv);
        return true;
      } 
      else {
        Stem    ns    = StemFinder.findByName(s, name);
        ns.grantPriv(subj, priv);
        return true;
      }
    }
    catch (GrantPrivilegeException eGP)         {
      GrouperShell.error(i, eGP);  
    }
    catch (GroupNotFoundException eGNF)         {
      GrouperShell.error(i, eGNF); 
    }
    catch (InsufficientPrivilegeException eIP)  {
      GrouperShell.error(i, eIP);
    }
    catch (SchemaException eS)                  {
      GrouperShell.error(i, eS);    
    }
    catch (StemNotFoundException eNSNF)         {
      GrouperShell.error(i, eNSNF); 
    }
    catch (SubjectNotFoundException eSNF)       {
      GrouperShell.error(i, eSNF); 
    }
    catch (SubjectNotUniqueException eSNU)      {
      GrouperShell.error(i, eSNU); 
    }
    return false;
  } // public static boolean invoke(i, stack, name, subjId, priv)

} // public class grantPriv

