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
package edu.internet2.middleware.grouper.ui;

/**
 * Thrown when something is seriously amiss. Could be the API is missing something
 * or could be that there is a missing parameter or an object is no longer available
 * In general the code which throws such an exception should take care of logging. Code
 * catching this exception should not log it.
 * @author Gary Brown.
 * @version $Id: UnrecoverableErrorException.java,v 1.1 2008-04-09 14:23:08 isgwb Exp $
 */
public class UnrecoverableErrorException extends RuntimeException {
	private String message = null;
	public UnrecoverableErrorException(String message) {
		super(message);
		this.message=message;
	}
	
	public UnrecoverableErrorException(String message,Throwable t) {
		super(message,t);
		this.message=message;
	}
	
	public UnrecoverableErrorException(Throwable t) {
		super(t);
	}

	@Override
	public String getMessage() {
		return message;
	}
	
	
}
