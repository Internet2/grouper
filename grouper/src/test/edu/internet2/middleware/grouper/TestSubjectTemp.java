/*
  Copyright (C) 2004-2007 University Corporation for Advanced Internet Development, Inc.
  Copyright (C) 2004-2007 The University Of Chicago

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

import java.util.Set;

import edu.internet2.middleware.grouper.subj.SubjectHelper;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;

/**
 * @author  blair christensen.
 * @version $Id: TestSubjectTemp.java,v 1.1.2.1 2009-05-18 16:56:38 mchyzer Exp $
 */
public class TestSubjectTemp {

  /**
   * 
   * @param args
   */
  public static void main(String[] args) throws Exception {
    Set<Subject> subjects = SubjectFinder.findAll("beck", "pennperson");
    for (Subject subject : subjects) {
      System.out.println(SubjectHelper.getPretty(subject) + ", " + subject.getName() + ", " + subject.getDescription());
    }
  }
}

