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
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouperClientExt.xmpp;

import java.util.List;


/**
 * implement this to handle events from xmpp
 */
public interface GrouperClientXmppHandler {

  /**
   * handle an incremental event from xmpp
   * @param grouperClientXmppJob 
   * @param groupName
   * @param groupExtension
   * @param subjectAttributeNames
   * @param newSubjectList
   * @param previousSubjectList
   * @param changeSubject
   * @param action
   */
  public void handleIncremental(GrouperClientXmppJob grouperClientXmppJob, String groupName, String groupExtension, 
      List<GrouperClientXmppSubject> newSubjectList, 
      List<GrouperClientXmppSubject> previousSubjectList, GrouperClientXmppSubject changeSubject, String action);

  
  /**
   * handle a full refresh event from xmpp
   * @param grouperClientXmppJob
   * @param groupName
   * @param groupExtension
   * @param subjectAttributeNames
   * @param newSubjectList
   */
  public void handleAll(GrouperClientXmppJob grouperClientXmppJob, String groupName, String groupExtension, 
      List<GrouperClientXmppSubject> newSubjectList);

}
