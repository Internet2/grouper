/*
 * Copyright (C) 2006 blair christensen.
 * All Rights Reserved.
 *
 * You may use and distribute under the same terms as Grouper itself.
 */

package com.devclue.grouper.shell;
import  bsh.*;
import  edu.internet2.middleware.grouper.*;
import  edu.internet2.middleware.subject.*;
import  edu.internet2.middleware.subject.provider.*;
import  java.util.*;

/**
 * Revoke a privilege.
 * <p/>
 * @author  blair christensen.
 * @version $Id: revokePriv.java,v 1.1 2006-06-29 15:44:12 blair Exp $
 * @since   0.0.1
 */
public class revokePriv {

  // PUBLIC CLASS METHODS //

  /**
   * Revoke a privilege.
   * <p/>
   * @param   i           BeanShell interpreter.
   * @param   stack       BeanShell call stack.
   * @param   name        Revoke privilege on this {@link Group} or {@link Stem}.
   * @param   subjId      Revoke privilege from this {@link Subject}.
   * @param   priv        Revoke this {@link Privilege}.
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
        g.revokePriv(subj, priv);
        return true;
      } 
      else {
        Stem    ns    = StemFinder.findByName(s, name);
        ns.revokePriv(subj, priv);
        return true;
      }
    }
    catch (GroupNotFoundException eGNF)         {
      GrouperShell.error(i, eGNF);
    }
    catch (InsufficientPrivilegeException eIP)  {
      GrouperShell.error(i, eIP);
    }
    catch (RevokePrivilegeException eRP)        {
      GrouperShell.error(i, eRP);  
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

} // public class revokePriv

