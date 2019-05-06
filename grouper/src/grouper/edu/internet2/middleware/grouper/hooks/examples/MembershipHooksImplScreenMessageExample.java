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
 */
package edu.internet2.middleware.grouper.hooks.examples;
 
import edu.internet2.middleware.grouper.hooks.MembershipHooks;
import edu.internet2.middleware.grouper.hooks.beans.GrouperContextType;
import edu.internet2.middleware.grouper.hooks.beans.GrouperContextTypeBuiltIn;
import edu.internet2.middleware.grouper.hooks.beans.HooksContext;
import edu.internet2.middleware.grouper.hooks.beans.HooksMembershipChangeBean;
import edu.internet2.middleware.grouper.util.GrouperUtil;
 
 
/**
 * group hook to put message on screen
 */
public class MembershipHooksImplScreenMessageExample extends MembershipHooks {
 
  /**
   *
   * @see edu.internet2.middleware.grouper.hooks.MembershipHooks#membershipPreAddMember(edu.internet2.middleware.grouper.hooks.beans.HooksContext, edu.internet2.middleware.grouper.hooks.beans.HooksMembershipChangeBean)
   */
  @Override
  public void membershipPreAddMember(HooksContext hooksContext, HooksMembershipChangeBean preAddMemberBean) {
 
    GrouperContextType grouperContextType = hooksContext.getGrouperContextType();
     
    //only care about this if not grouper loader
    if (GrouperContextTypeBuiltIn.GROUPER_UI.equals(grouperContextType)) {
       
      if (preAddMemberBean.getGroup().getName().startsWith("test:")) {
        addMessageToScreen("Here is <a href=\"http://www.yahoo.com\">link</a>");
      }
    }
  }
 
  /**
   * @param message
   */
  public static void addMessageToScreen(String message) {
 
    Class<?> grouperUiHookShimClass = null;
     
    try {
      Class.forName("edu.internet2.middleware.grouper.grouperUi.beans.json.GuiResponseJs");
      grouperUiHookShimClass = Class.forName("edu.internet2.middleware.grouper.ui.hooks.GrouperUiHookShim");
       
      // in ui
    } catch (Exception e) {
      // not ui
      return;
    }
    // call class that is compiled with UI stuff
    GrouperUtil.callMethod(grouperUiHookShimClass, "addMessageToScreen",
        GrouperUtil.toSet(String.class), GrouperUtil.toSet(message));
     
  }
}