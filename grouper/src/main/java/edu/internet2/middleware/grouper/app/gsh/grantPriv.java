/**
 * Copyright 2014 Internet2
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
 */
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
import edu.internet2.middleware.grouper.exception.GrantPrivilegeException;
import edu.internet2.middleware.grouper.exception.GroupNotFoundException;
import edu.internet2.middleware.grouper.exception.InsufficientPrivilegeException;
import edu.internet2.middleware.grouper.exception.SchemaException;
import edu.internet2.middleware.grouper.exception.StemNotFoundException;
import edu.internet2.middleware.grouper.privs.Privilege;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.SubjectNotFoundException;
import edu.internet2.middleware.subject.SubjectNotUniqueException;

/**
 * Grant a privilege.
 * <p/>
 * @author  blair christensen.
 * @version $Id: grantPriv.java,v 1.4 2009-11-02 03:50:51 mchyzer Exp $
 * @since   0.0.1
 */
public class grantPriv {

  // PUBLIC CLASS METHODS //

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
      return invoke(s, name, subjId, priv);
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
  }
  
  /**
   * Grant a privilege.
   * <p/>
   * @param   grouperSession
   * @param   name        Grant privilege on this {@link Group} or {@link Stem}.
   * @param   subjId      Grant privilege to this {@link Subject}.
   * @param   priv        Grant this {@link Privilege}.
   * @return  True if succeeds.
   */
  public static boolean invoke(GrouperSession grouperSession, String name,  String subjId, Privilege priv) {
    Subject         subj  = SubjectFinder.findByIdOrIdentifier(subjId, true);
    if (Privilege.isAccess(priv)) {
      Group   g     = GroupFinder.findByName(grouperSession, name, true);
      return g.grantPriv(subj, priv, false);

    } else if (Privilege.isNaming(priv)) {
      Stem    ns    = StemFinder.findByName(grouperSession, name, true);
      return ns.grantPriv(subj, priv, false);

    } else if (Privilege.isAttributeDef(priv)) {
      AttributeDef attributeDef = AttributeDefFinder.findByName(name, true);
      return attributeDef.getPrivilegeDelegate().grantPriv(subj, priv, false);
    } else {
      throw new RuntimeException("Invalid privilege type: " + priv);
    }
  }

} // public class grantPriv

