/*
Copyright 2004-2006 University Corporation for Advanced Internet Development, Inc.
Copyright 2004-2006 The University Of Bristol

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

/**
 * A simple message Class which is created for display in the message area of
 * the UI. It is used in conjnction with <fmt:message tags
 * <p />
 * 
 * @author Gary Brown.
 * @version $Id: Message.java,v 1.3 2006-07-14 11:04:11 isgwb Exp $
 */
public class Message {
	private String message = "";

	private String[] args = null;

	private boolean isError = false;

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
		if(!isError) return "Message";
		return "ErrorMessage";
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
	}
}