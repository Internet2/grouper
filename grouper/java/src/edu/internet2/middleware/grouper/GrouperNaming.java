/*
 * Copyright (C) 2004-2005 University Corporation for Advanced Internet Development, Inc.
 * Copyright (C) 2004-2005 The University Of Chicago
 * All Rights Reserved. 
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 *  * Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *  * Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *  * Neither the name of the University of Chicago nor the names
 *    of its contributors nor the University Corporation for Advanced
 *   Internet Development, Inc. may be used to endorse or promote
 *   products derived from this software without explicit prior
 *   written permission.
 *
 * You are under no obligation whatsoever to provide any enhancements
 * to the University of Chicago, its contributors, or the University
 * Corporation for Advanced Internet Development, Inc.  If you choose
 * to provide your enhancements, or if you choose to otherwise publish
 * or distribute your enhancements, in source code form without
 * contemporaneously requiring end users to enter into a separate
 * written license agreement for such enhancements, then you thereby
 * grant the University of Chicago, its contributors, and the University
 * Corporation for Advanced Internet Development, Inc. a non-exclusive,
 * royalty-free, perpetual license to install, use, modify, prepare
 * derivative works, incorporate into the software or other computer
 * software, distribute, and sublicense your enhancements or derivative
 * works thereof, in binary and source code form.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND WITH ALL FAULTS.  ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING,
 * BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE, AND NON-INFRINGEMENT ARE DISCLAIMED AND the
 * entire risk of satisfactory quality, performance, accuracy, and effort
 * is with LICENSEE. IN NO EVENT SHALL THE COPYRIGHT OWNER, CONTRIBUTORS,
 * OR THE UNIVERSITY CORPORATION FOR ADVANCED INTERNET DEVELOPMENT, INC.
 * BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR
 * BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OR DISTRIBUTION OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package edu.internet2.middleware.grouper;


import  java.util.*;


/** 
 * {@link Grouper} Naming Privilege Interface.
 * <p />
 *
 * @author  blair christensen.
 * @version $Id: GrouperNaming.java,v 1.30 2005-03-29 17:41:24 blair Exp $
 */
public interface GrouperNaming {

  /**
   * Verify whether this implementation of the {@link GrouperNaming}
   * interface can handle this privilege.
   * <p />
   *
   * @param   priv  The privilege to verify.
   * @return  True if this implementation handles the specified
   *   privilege.
   */
  public boolean can(String priv);

  /**
   * Grant an naming privilege on a <i>naming</i> {@link GrouperStem}.
   * <p />
   * See implementations for more information.
   *
   * @param   s     Act within this {@link GrouperSession}.
   * @param   ns    Grant privileges on this {@link GrouperStem}.
   * @param   m     Grant privileges for this {@link GrouperMember}.
   * @param   priv  Privilege to grant.
   */
  public boolean grant(GrouperSession s, GrouperStem ns, GrouperMember m, String priv);

  /**
   * List naming privileges for current subject on the specified naming namespace.
   * <p />
   * See implementations for more information.
   *
   * @param   s   Act within this {@link GrouperSession}.
   * @param   ns  List privileges on this namespace.
   * @return  List of privileges.
   */
  public List has(GrouperSession s, GrouperStem ns);

  /**
   * List namespaces where the current subject has the specified privilege.
   * <p />
   * See implementations for more information.
   *
   * @param   s     Act within this {@link GrouperSession}.
   * @param   priv  Query for this privilege type.
   * @return  List of {@link GrouperStem} namespaces.
   */
  public List has(GrouperSession s, String priv);

  /**
   * List naming privileges for specified member on the specified naming namespace.
   * <p />
   * See implementations for more information.
   *
   * @param   s     Act within this {@link GrouperSession}.
   * @param   ns    Return privileges for this {@link GrouperStem}.
   * @param   m     List privileges for this {@link GrouperMember}.
   * @return  List of privileges.
   */
  public List has(GrouperSession s, GrouperStem ns, GrouperMember m);

  /**
   * Verify whether current subject has the specified privilege on the
   * specified namespace.
   * <p />
   * See implementations for more information.
   *
   * @param   s     Act within this {@link GrouperSession}.
   * @param   ns    Verify privilege for this namespace.
   * @param   priv  Verify this privilege.
   * @return  True if subject has this privilege on the namespace.
   */
  public boolean has(GrouperSession s, GrouperStem ns, String priv);

  /**
   * List namespaces where the specified member has the specified
   * privilege.
   * <p />
   * See implementations for more information.
   *
   * @param   s     Act within this {@link GrouperSession}.
   * @param   m     Query for this {@link GrouperMember}.
   * @param   priv  Query for this privilege type.
   * @return  List of {@link GrouperStem} namespaces.
   */
  public List has(GrouperSession s, GrouperMember m, String priv);

  /**
   * Verify whether the specified member has the specified privilege
   * on the specified naming namespace.
   * <p />
   * See implementations for more information.
   *
   * @param   s     Act within this {@link GrouperSession}.
   * @param   ns    Verify privilege for this namespace.
   * @param   m     Verify privilege for this member.
   * @param   priv  Verify this privilege.
   * @return  True if subject has this privilege on the namespace.
   */
  public boolean has(GrouperSession s, GrouperStem ns, GrouperMember m, String priv);

  /**
   * Revoke all privileges of the specified type on the specified
   * namespace.
   * <p />
   * See implementations for more information.
   *
   * @param   s     Act within this {@link GrouperSession}.
   * @param   ns    Revoke privilege on this {@link GrouperStem}.
   * @param   priv  Privilege to revoke.
   */
  public boolean revoke(GrouperSession s, GrouperStem ns, String priv);

  /**
   * Revoke an naming privilege.
   * <p />
   * See implementations for more information.
   *
   * @param   s     Act within this {@link GrouperSession}.
   * @param   ns    Revoke privilege on this {@link GrouperStem}.
   * @param   m     Revoke privilege for this{@link GrouperMember}.
   * @param   priv  Privilege to revoke.
   */
  public boolean revoke(GrouperSession s, GrouperStem ns, GrouperMember m, String priv);

  /**
   * List members who have the specified privilege on the 
   * specified namespace.
   * <p />
   * See implementations for more information.
   *
   * @param   s     Act within this {@link GrouperSession}.
   * @param   ns    Query for this {@link GrouperStem}.
   * @param   priv  Query for this privilege type.
   * @return  List of {@link GrouperMember} members.
   */
  public List whoHas(GrouperSession s, GrouperStem ns, String priv);

}

