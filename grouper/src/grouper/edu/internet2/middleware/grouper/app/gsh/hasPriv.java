/*******************************************************************************
 * Copyright 2012 Internet2
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
/*
 * Copyright (C) 2006-2007 blair christensen.
 * All Rights Reserved.
 *
 * You may use and distribute under the same terms as Grouper itself.
 */

package edu.internet2.middleware.grouper.app.gsh;
import bsh.CallStack;
import bsh.Interpreter;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefFinder;
import edu.internet2.middleware.grouper.exception.GroupNotFoundException;
import edu.internet2.middleware.grouper.exception.StemNotFoundException;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.privs.AttributeDefPrivilege;
import edu.internet2.middleware.grouper.privs.NamingPrivilege;
import edu.internet2.middleware.grouper.privs.Privilege;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.SubjectNotFoundException;
import edu.internet2.middleware.subject.SubjectNotUniqueException;

/**
 * Check if subject has privilege.
 * <p/>
 * @author  blair christensen.
 * @version $Id: hasPriv.java,v 1.4 2009-11-02 03:50:51 mchyzer Exp $
 * @since   0.0.1
 */
public class hasPriv {

  // PUBLIC CLASS METHODS //

  /**
   * Check if subject has privilege.
   * <p/>
   * @param   i           BeanShell interpreter.
   * @param   stack       BeanShell call stack.
   * @param   name        Check for privilege on this {@link Group} or {@link Stem}.
   * @param   subjId      Check if this {@link Subject} has privilege.
   * @param   priv        Check this {@link AccessPrivilege}.
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
      Subject         subj  = SubjectFinder.findByIdOrIdentifier(subjId, true);
      if (Privilege.isAccess(priv)) {
        Group g = GroupFinder.findByName(s, name, true);
        if      (priv.equals( AccessPrivilege.ADMIN  )) {
          return g.hasAdmin(subj);
        }
        else if (priv.equals( AccessPrivilege.OPTIN  )) {
          return g.hasOptin(subj);
        }
        else if (priv.equals( AccessPrivilege.OPTOUT )) {
          return g.hasOptout(subj);
        }
        else if (priv.equals( AccessPrivilege.READ   )) {
          return g.hasRead(subj);
        }
        else if (priv.equals( AccessPrivilege.UPDATE )) {
          return g.hasUpdate(subj);
        }
        else if (priv.equals( AccessPrivilege.VIEW   )) {
          return g.hasView(subj);
        } else {
          throw new RuntimeException("Not expecting privilege: " + priv);
        }
      } else if (Privilege.isNaming(priv)) {
        Stem ns = StemFinder.findByName(s, name, true);
        if      (priv.equals( NamingPrivilege.CREATE )) {
          return ns.hasCreate(subj);
        }
        else if (priv.equals( NamingPrivilege.STEM   )) {
          return ns.hasStem(subj);
        } else {
          throw new RuntimeException("Not expecting privilege: " + priv);
        }
      } else if (Privilege.isAttributeDef(priv)) {
        AttributeDef attributeDef = AttributeDefFinder.findByName(name, true);
        if      (priv.equals( AttributeDefPrivilege.ATTR_ADMIN )) {
          return attributeDef.getPrivilegeDelegate().hasAttrAdmin(subj);
        } else if (priv.equals( AttributeDefPrivilege.ATTR_OPTIN )) {
          return attributeDef.getPrivilegeDelegate().hasAttrOptin(subj);
        } else if (priv.equals( AttributeDefPrivilege.ATTR_OPTOUT )) {
          return attributeDef.getPrivilegeDelegate().hasAttrOptout(subj);
        } else if (priv.equals( AttributeDefPrivilege.ATTR_READ )) {
          return attributeDef.getPrivilegeDelegate().hasAttrRead(subj);
        } else if (priv.equals( AttributeDefPrivilege.ATTR_UPDATE )) {
          return attributeDef.getPrivilegeDelegate().hasAttrUpdate(subj);
        } else if (priv.equals( AttributeDefPrivilege.ATTR_VIEW )) {
          return attributeDef.getPrivilegeDelegate().hasAttrView(subj);
        } else {
          throw new RuntimeException("Not expecting privilege: " + priv);
        }
        
      } else {
        throw new RuntimeException("Invalid privilege type: " + priv);
      }
    }
    catch (GroupNotFoundException eGNF)         {
      GrouperShell.error(i, eGNF);
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

} // public class hasPriv

