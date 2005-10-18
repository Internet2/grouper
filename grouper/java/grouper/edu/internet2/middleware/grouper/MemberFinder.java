/*
  Copyright 2004-2005 University Corporation for Advanced Internet Development, Inc.
  Copyright 2004-2005 The University Of Chicago

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
*/

package edu.internet2.middleware.grouper;

import  edu.internet2.middleware.subject.*;
import  java.io.Serializable;

/**
 * Find members within the Groups Registry.
 * <p />
 * @author  blair christensen.
 * @version $Id: MemberFinder.java,v 1.1.2.3 2005-10-18 20:06:36 blair Exp $
 */
public class MemberFinder implements Serializable {

  /**
   * Convert a {@link Group} to a {@link Member}.
   * <pre class="eg">
   * // Convert Group g to a Member object
   * try {
   *   Member m = MemberFinder.findByGroup(s, g);
   * }
   * catch (MemberNotFoundException e) {
   *   // Member not found
   * }
   * </pre>
   * @param   s   Find {@link Member} within this session context.
   * @param   g   {@link Group} to convert.
   * @return  A {@link Member} object.
   * @throws  MemberNotFoundException
   */
  public static Member findByGroup(GrouperSession s, Group g) 
    throws MemberNotFoundException
  {
    throw new RuntimeException("Not implemented");
  }

  /**
   * Convert a {@link Subject} to a {@link Member}.
   * <pre class="eg">
   * // Convert a subject to a Member object
   * try {
   *   Member m = MemberFinder.findBySubject(s, subj);
   * }
   * catch (MemberNotFoundException e) {
   *   // Member not found
   * }
   * </pre>
   * @param   s   Find {@link Member} within this session context.
   * @param   subj  {@link Subject} to convert.
   * @return  A {@link Member} object.
   * @throws  MemberNotFoundException
   */
  public static Member findBySubject(GrouperSession s, Subject subj)
    throws MemberNotFoundException
  {
    throw new RuntimeException("Not implemented");
  }

  /**
   * Find a member by UUID.
   * <pre class="eg">
   * // Find a member by uuid.
   * try {
   *   Member m = MemberFind.findByUuid(s, uuid);
   * }
   * catch (MemberNotFoundException e) {
   *   // Member not found
   * }
   * </pre>
   * @param   s   Find {@link Member} within this session context.
   * @param   uuid  Find {@link Member} with this UUID.
   * @return  A {@link Member} object.
   * @throws  MemberNotFoundException
   */
  public static Member findByUuid(GrouperSession s, String uuid)
    throws MemberNotFoundException
  {
    throw new RuntimeException("Not implemented");
  }

}

