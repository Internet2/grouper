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
package edu.internet2.middleware.grouper.subj.decoratorExamples;

import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.cache.GrouperCache;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.hooks.beans.GrouperContextType;
import edu.internet2.middleware.grouper.hooks.beans.GrouperContextTypeBuiltIn;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.privs.PrivilegeHelper;
import edu.internet2.middleware.grouper.subj.SubjectCustomizerBase;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.collections.MultiKey;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.provider.SubjectImpl;

/**
 * decorate an attribute for some subjects
 */
public class SubjectCustomizerForDecoratorUiDisplay extends SubjectCustomizerBase {
  
  /**
   * cache who has access
   */
  private static GrouperCache<MultiKey, Boolean> subjectIsPrivileged = new GrouperCache(
      "subjectIsPrivileged", 10000, false, 120, 120, false);
  
  /**
   * @see SubjectCustomizer#filterSubjects(GrouperSession, Set, String)
   */
  @Override
  public Set<Subject> filterSubjects(GrouperSession grouperSession,
      Set<Subject> subjects, String findSubjectsInStemName) {

    //if not the UI then dont worry about it
    GrouperContextType grouperContextType = GrouperContextTypeBuiltIn.currentGrouperContext();
    if (grouperContextType == null || grouperContextType != GrouperContextTypeBuiltIn.GROUPER_UI) {
      return subjects;
    }
    
    final Subject subjectCallingGrouperSession = grouperSession.getSubject();

    MultiKey subjectMultiKey = new MultiKey(subjectCallingGrouperSession.getSourceId(), subjectCallingGrouperSession.getId());

    //see if decision in cache
    Boolean allowExtendedDisplay = subjectIsPrivileged.get(subjectMultiKey);
    
    if (allowExtendedDisplay == null) {
      
      //dont allow grouperSysAdmin or wheel group
      boolean grouperSystem = PrivilegeHelper.isSystemSubject(subjectCallingGrouperSession);
      boolean wheel = PrivilegeHelper.isWheel(grouperSession);
      
      //if grouper system then dont see the data, some thing happen as grouper system, dont allow
      if (grouperSystem) {
        allowExtendedDisplay = false;
      } else if (wheel) {
        
        //if someone is a wheel group, they can see everything
        allowExtendedDisplay = true;
        
      } else {

        //do this check as root so anyone can do it
        allowExtendedDisplay = (Boolean)GrouperSession.callbackGrouperSession(grouperSession.internal_getRootSession(), new GrouperSessionHandler() {
          
          @Override
          public Object callback(GrouperSession rootSession) throws GrouperSessionException {
            Group privilegedGroup = GroupFinder.findByName(rootSession, "etc:privilegedGroup", true);
            return privilegedGroup.hasMember(subjectCallingGrouperSession);
          }
        });
        
      }
      //put back in cache
      subjectIsPrivileged.put(subjectMultiKey, allowExtendedDisplay);
    }
    
    Set<Subject> newSubjectList = new LinkedHashSet<Subject>();
    
    for (Subject originalSubject : subjects) {

      if (StringUtils.equals(originalSubject.getSourceId(), "jdbc" )) {
        
        String uiLabel = originalSubject.getName();
        if (allowExtendedDisplay) {
          uiLabel += " - " + originalSubject.getId();
        }
        Subject newSubject = originalSubject;
        if (!(newSubject instanceof SubjectImpl)) {
          newSubject = new SubjectImpl(originalSubject.getId(), originalSubject.getName(), 
              originalSubject.getDescription(), originalSubject.getTypeName(), originalSubject.getSourceId(), 
              originalSubject.getAttributes(false));
        }

        //this should return a modifiable map of attributes for us to work with
        newSubject.getAttributes(false).put("uiLabel", GrouperUtil.toSet(uiLabel));
        newSubjectList.add(newSubject);
      } else {
        newSubjectList.add(originalSubject);
      }
    }
    
    return newSubjectList;
    
  }

}
