/*--
$Id: BaseAction.java,v 1.6 2006-02-09 10:29:53 lmcrae Exp $
$Date: 2006-02-09 10:29:53 $

Copyright 2006 Internet2, Stanford University

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
package edu.internet2.middleware.signet.ui;

import java.util.logging.Logger;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

public class BaseAction extends Action
{

  // ----------------------------------------------------- Instance Variables

  /**
   * The <code>Logger</code> instance for this application.
   */
  protected Logger logger = Logger.getLogger(Constants.PACKAGE);

  // ------------------------------------------------------ Protected Methods


  /**
   * Return the local or global forward named "failure"
   * or null if there is no such forward.
   * @param mapping Our ActionMapping
   * @return Return the mapping named "failure" or null if there is no such mapping.
   */
  protected ActionForward findFailure(ActionMapping mapping) {
      return (mapping.findForward(Constants.FAILURE));
  }


  /**
   * Return the mapping labeled "success"
   * or null if there is no such mapping.
   * @param mapping Our ActionMapping
   * @return Return the mapping named "success" or null if there is no such mapping.
   */
  protected ActionForward findSuccess(ActionMapping mapping) {
      return (mapping.findForward(Constants.SUCCESS));
  }


  /**
   * Return the mapping labeled "duplicateAssignments"
   * or null if there is no such mapping.
   * @param mapping Our ActionMapping
   * @return Return the mapping named "duplicateAssignments" or null if there is no such mapping.
   */
  protected ActionForward findDuplicateAssignments(ActionMapping mapping) {
      return (mapping.findForward(Constants.DUPLICATE_ASSIGNMENTS));
  }


  /**
   * Return the mapping labeled "dataEntryErrors"
   * or null if there is no such mapping.
   * @param mapping Our ActionMapping
   * @return Return the mapping named "dataEntryErrors" or null if there is no
   * such mapping.
   */
  protected ActionForward findDataEntryErrors(ActionMapping mapping)
  {
      return (mapping.findForward(Constants.DATA_ENTRY_ERRORS));
  }

  /**
   * This mapping exists only for Signet demo installations.
   * In the case of a normal production system, user authentication would
   * occur before any Signet page is accessed.
   * 
   * Return the mapping labeled "demoLogin" or null if there is no such mapping.
   * @param mapping Our ActionMapping
   * @return Return the mapping named "demoLogin" or null if there is no
   * such mapping.
   */
  protected ActionForward findDemoLogin(ActionMapping mapping)
  {
      return (mapping.findForward(Constants.DEMO_LOGIN));
  }

}
