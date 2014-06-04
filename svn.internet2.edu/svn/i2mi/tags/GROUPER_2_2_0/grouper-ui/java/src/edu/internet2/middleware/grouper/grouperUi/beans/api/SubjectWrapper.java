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
/**
 * @author Kate
 * $Id: SubjectWrapper.java,v 1.1 2009-09-09 15:10:04 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.grouperUi.beans.api;

import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.subject.provider.SubjectImpl;


/**
 * subject implementation around a member, if the subject is not found, handle gracefully
 */
public class SubjectWrapper extends SubjectImpl {

  /**
   * construct from member
   * @param theMember
   */
  public SubjectWrapper(Member theMember) {
    super(theMember.getSubjectId(), "Cant find subject: " + theMember.getSubjectSourceId() + ": " + theMember.getSubjectId(),
        "Cant find subject: " + theMember.getSubjectSourceId() + ": " + theMember.getSubjectId(),
        theMember.getSubjectSourceId(), null);
  }

}
