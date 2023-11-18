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
/*
Copyright 2004-2007 University Corporation for Advanced Internet Development, Inc.
Copyright 2004-2007 The University Of Bristol

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

package edu.internet2.middleware.grouper.ui;

import java.io.Serializable;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.hooks.logic.HookVeto;
import edu.internet2.middleware.grouper.ui.util.MapBundleWrapper;

/**
 * A simple message Class which is created for display in the message area of
 * the UI. It is used in conjnction with <fmt:message tags
 * <p />
 * 
 * @author Gary Brown.
 * @version $Id: Message.java,v 1.8 2009-08-12 04:52:14 mchyzer Exp $
 */
public class Message implements Serializable {
	private String message = "";

	private String[] args = null;

	private boolean isError = false;
	private boolean isWarning = false;

	private static String[] nullArgs = null;

	/**
	 * Constructor to ceate a simple message
	 * 
	 * @param message
	 *            key in ResourceBundle that returns text
	 */
	public Message(String message) {
		this(message, nullArgs, false);
	}

	/**
	 * Constructor to create a simple message with argument substitution
	 * 
	 * @param message
	 *            key in ResourceBundle that returns text
	 * @param args
	 *            values to substitute in format text keyed by message
	 */
	public Message(String message, String[] args) {
		this(message, args, false);
	}

	/**
	 * Constructor to create a simple message with a single argument
	 * substitution
	 * 
	 * @param message
	 *            key in ResourceBundle that returns text
	 * @param arg
	 *            value to substitute in format text keyed by message
	 */
	public Message(String message, String arg) {
		this(message, new String[] { arg }, false);
	}

	/**
	 * Constructor to create a message which can be an error message
	 * 
	 * @param message
	 *            key in ResourceBundle that returns text
	 * @param isError
	 *            boolean
	 */
	public Message(String message, boolean isError) {
		this(message, nullArgs, isError);
	}

	/**
	 * Constructor to create a message with single argument substitution which
	 * can be an error message
	 * 
	 * @param message
	 *            key in ResourceBundle that returns text
	 * @param arg
	 *            value to substitute in message
	 * @param isError
	 *            boolean
	 */
	public Message(String message, String arg, boolean isError) {
		this(message, new String[] { arg }, isError);
	}

	/**
	 * Constructor to create a message with argument substitution which can be
	 * an error message
	 * 
	 * @param message
	 * @param args
	 *            values to substitute in message
	 * @param isError
	 *            boolean
	 */
	public Message(String message, String[] args, boolean isError) {
		this.message = message;
		this.args = args;
		this.isError = isError;

	}

	/**
	 * @return Returns the args.
	 */
	public String[] getArgs() {
		return args;
	}

	/**
	 * @param args
	 *            the args to set.
	 */
	public void setArgs(String[] args) {
		this.args = args;
	}
	/**
	 * @return Returns the containerId.
	 */
	public String getContainerId() {
		if(!isError && !isWarning) return "Message";
		if(isError) return "ErrorMessage";
		return "WarningMessage";
	}

	/**
	 * @return Returns the message.
	 */
	public String getText() {
		return message;
	}

	/**
	 * @param message
	 *            the message to set.
	 */
	public void setText(String message) {
		this.message = message;
	}

	/**
	 * @return boolean isError.
	 */
	public boolean isError() {
		return isError;
	}

	/**
	 * @param isError
	 *            boolean.
	 */
	public void setError(boolean isError) {
		this.isError = isError;
		if(isError) isWarning=false;
	}
	
	/**
	 * @return boolean isWarning.
	 */
	public boolean isWarning() {
		return isWarning;
	}

	/**
	 * @param isWarning
	 *            boolean.
	 */
	public void setWarning(boolean isWarning) {
		this.isWarning = isWarning;
		if(isWarning) isError=false;
	}
	
	/**
	 * add a veto message to screen.  Use the key in the veto if available, else
	 * use the error message
	 * @param request
	 * @param hookVeto
	 */
	public static void addVetoMessageToScreen(HttpServletRequest request, HookVeto hookVeto) {
    MapBundleWrapper mapBundleWrapper = (MapBundleWrapper)request.getSession().getAttribute("navNullMap");
    
    String hookReasonKeyValue = (String)mapBundleWrapper.get(hookVeto.getReasonKey());

    //make sure the key is in there
    if (!StringUtils.isEmpty(hookReasonKeyValue)) {
      
      request.setAttribute("message", new Message(
          hookVeto.getReasonKey(),true));
    } else {
    
      request.setAttribute("message", new Message("error.hook.veto",
          new String[] {hookVeto.getReason()}, true));
    }

	}
	
}
