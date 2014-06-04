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
Copyright 2004-2008 University Corporation for Advanced Internet Development, Inc.
Copyright 2004-2008 The University Of Bristol

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
package edu.internet2.middleware.grouper.ui.util;

import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;
import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import javax.servlet.jsp.jstl.fmt.LocalizationContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.internet2.middleware.grouper.ui.GrouperUiFilter;
import edu.internet2.middleware.grouper.ui.UnrecoverableErrorException;

/**
 * Helper class centralise some Exception handling
 * <p />
 * 
 * @author Gary Brown.
 * @version $Id: NavExceptionHelper.java,v 1.3 2009-08-12 04:52:14 mchyzer Exp $
 */
public class NavExceptionHelper implements Serializable {
	protected static final Log LOG = LogFactory.getLog(NavExceptionHelper.class);

	public NavExceptionHelper() {
	}
	
	/**
	 * Takes class short name for Throwable and builds a key based on that.
	 * If the key is not present a default catch all is used. 
	 * @param t
	 * @return key
	 */
	public String key(Throwable t) {
		String key="error." + t.getClass().getSimpleName();
		try {
		  key = GrouperUiFilter.retrieveSessionNavResourceBundle().getString(key);
		}catch(MissingResourceException mre) {
			key = "error.unknown.exception";
		}
		return key;
	}
	
	/**
	 * UnrecoverableErrorException can have its own message key and one
	 * associated with any 'cause'. This method takes care of building 
	 * a message based on the available keys.
	 * @param cause
	 * @return message
	 */
	public String getMessage(UnrecoverableErrorException cause) {
		String messageKey=cause.getMessage();
		String exceptionKey=null;
		if(cause.getCause()!=null) {
			
			exceptionKey = this.key(cause.getCause());
		}
		String message = "";
		String exceptionText = "";
		if(messageKey!=null) {
			try {
				message = GrouperUiFilter.retrieveSessionNavResourceBundle().getString(messageKey);
				String[] args=cause.getMessageArgs();
				if(args != null) message=MessageFormat.format(message,(Object[])args);
			}catch(MissingResourceException e) {
				LOG.error("Missing nav key: " + messageKey);
			}
		}
		if(exceptionKey!=null) {
			try {
				exceptionText = GrouperUiFilter.retrieveSessionNavResourceBundle().getString(exceptionKey);
			}catch(MissingResourceException e) {
				LOG.error("Missing nav key: " + exceptionKey);
				//https://bugs.internet2.edu/jira/browse/GRP-443 - use the API message for now
				if(exceptionKey !=null && exceptionKey.indexOf(" ") > -1) {
					exceptionText=exceptionKey;
				}
			}
		}
		return message + " " +exceptionText;
	}
	
	/**
	 * Helper method takes alternate parameter / parameter name pairs and
	 * constructs a message, if any are empty, indicating what is missing
	 * @param params
	 * @return missing parameters
	 */
	public  String missingParameters(String... params) {
		StringBuffer msg = null;
		int missingCount=0;
		for(int i=0;i<params.length;i+=2) {
			if(isEmpty(params[i])) {
				if(missingCount==0) {
					msg=new StringBuffer("Missing parameter(s) - [");
				}else{
					msg.append(", ");
				}
				msg.append(params[i+1]);
				missingCount++;
			}
		}
		if(msg!=null) msg.append("]");
		else return null;
		return msg.toString();
	}
	
	/**
	 * Helper method takes alternate parameter / parameter name pairs and
	 * constructs a message, if any are empty, indicating what is missing
	 * @param params
	 * @return missing parameters
	 */
	public  String missingAlternativeParameters(String... params) {
		StringBuffer msg = null;
		int missingCount=0;
		for(int i=0;i<params.length;i+=2) {
			if(isEmpty(params[i])) {
				if(missingCount==0) {
					msg=new StringBuffer("Missing alternative parameter(s) - [");
				}else{
					msg.append(", ");
				}
				msg.append(params[i+1]);
				missingCount++;
			}
		}
		if(msg!=null) msg.append("]");
		else return null;
		return msg.toString();
	}
	
	private boolean isEmpty(Object obj) {
		return obj==null || "".equals(obj);
	}
	
	/**
	 * Allows code which constructs an Exception to add a stack trace
	 * @param e
	 * @return the input Exception
	 */
	public static Exception fillInStacktrace(Exception e) {
		if(e.getStackTrace()==null) {
			Thread c = Thread.currentThread();
			StackTraceElement[] stack = c.getStackTrace();
			StackTraceElement[] newStack = new StackTraceElement[stack.length-3];
			System.arraycopy(stack, 3, newStack, 0, newStack.length);
			e.setStackTrace(newStack);
		}
		return e;
	}
	
	/**
	 * Captures the stack trace for an Exception so it can be logged
	 * @param t
	 * @return the stack trace as a String
	 */
	public static String toLog(Throwable t) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		t.printStackTrace(pw);
		return sw.toString();
	}
}
