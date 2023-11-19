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
/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.util;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouperClient.collections.MultiKey;
import edu.internet2.middleware.grouperClient.util.ExpirableCache;
import edu.internet2.middleware.subject.Source;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.provider.SourceManager;



/**
 * utils about emails
 */
public class GrouperEmailUtils {

  /**
   * keep a cache of group name to comma separated list
   */
  private static ExpirableCache<String, String> groupNameToEmailList = new ExpirableCache<String, String>(5);

  /**
   * 
   * @param groupName
   * @return comma separated email addresses
   */
  public static String retrieveEmailAddresses(final String groupName) {
    return (String)GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
      
      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
        
        StringBuilder result = new StringBuilder();
        
        Group group = GroupFinder.findByName(grouperSession, groupName, true);
        
        Set<Member> members = group.getMembers();
        
        Map<MultiKey, Subject> sourceIdSubjectIdToSubject = SubjectFinder.findByMembers(members);
        
        for (Subject subject : GrouperUtil.nonNull(sourceIdSubjectIdToSubject).values()) {
          String emailAddress = getEmail(subject);
          if (!StringUtils.isBlank(emailAddress)) {
            if (result.length() > 0) {
              result.append(", ");
            }
            result.append(emailAddress);
          }
        }
        
        return null;
      }
    });
  }

  /**
   * 
   * @param groupName
   * @return the email addresses
   */
  public static String retrieveEmailAddressesOrFromCache(final String groupName) {
    String emailAddresses = groupNameToEmailList.get(groupName);
    if (emailAddresses == null) {
      emailAddresses = retrieveEmailAddresses(groupName);
      emailAddresses = GrouperUtil.defaultString(emailAddresses);
      groupNameToEmailList.put(groupName, emailAddresses);
    }
    return emailAddresses;
  }
  
  /**
   * get the subject attribute name for a source id
   * @param sourceId
   * @return the attribute name
   */
  public static String emailAttributeNameForSource(String sourceId) {
    Source source = SourceManager.getInstance().getSource(sourceId);
    return source.getInitParam("emailAttributeName");
  }
  
  /**
   * get email address given a subject.
   * @param subject
   * @return emailAddress if it's there or null otherwise
   */
  public static String getEmail(Subject subject) {
    String emailAttributeName = emailAttributeNameForSource(subject.getSourceId());
    if (!StringUtils.isBlank(emailAttributeName)) {
      String emailAddress = subject.getAttributeValue(emailAttributeName);
      if (!StringUtils.isBlank(emailAddress)) {
        return emailAddress;
      }
    }
    return null;
  }
  
  /**
   * get email addresses for subjects
   * @param subjects
   * @return a unique set of emails
   */
  public static Set<String> getEmails(Set<Subject> subjects) {
    Set<String> emails = new HashSet<String>();
    for (Subject subject: subjects) {
      String email = getEmail(subject);
      if (StringUtils.isNotBlank(email)) {
        emails.add(email);
      }
    }
    return emails;
  }
}
