/*--
  $Id: BaseAction.java,v 1.2 2005-01-10 21:52:16 acohen Exp $
  $Date: 2005-01-10 21:52:16 $
  
  Copyright 2004 Internet2 and Stanford University.  All Rights Reserved.
  Licensed under the Signet License, Version 1,
  see doc/license.txt in this distribution.
*/
package edu.internet2.middleware.signet.ui;

import java.util.logging.Logger;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import javax.servlet.http.HttpServletRequest;

public class BaseAction extends Action {

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

}
