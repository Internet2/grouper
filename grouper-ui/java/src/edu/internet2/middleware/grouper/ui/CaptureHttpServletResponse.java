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

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;

/**
 * Captures response data so that it is not immediately sent to end user.
 * Typically used in Filters to post process output. Use the toString() method
 * to obtain captured output.
 * 
 * 
 * @author Gary Brown.
 * @version $Id: CaptureHttpServletResponse.java,v 1.4 2007-04-11 08:19:24 isgwb Exp $
 */
public class CaptureHttpServletResponse extends HttpServletResponseWrapper {
	protected CharArrayWriter charWriter;

	protected ServletOutputStream out;

	protected PrintWriter writer;

	protected boolean getOutputStreamCalled;

	protected boolean getWriterCalled;

	/**
	 * Constructor used to wrap exising HttpServletResponse
	 * 
	 * @param response
	 *            to be wrapped
	 */
	public CaptureHttpServletResponse(HttpServletResponse response) {
		super(response);
		// Create the writer
		charWriter = new CharArrayWriter();
		out = new CaptureServletOutputStream();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.ServletResponse#getOutputStream()
	 */
	public ServletOutputStream getOutputStream() throws IOException {
		// Can't call getOutputStream if getWriter
		// has already been called
		if (getWriterCalled) {
			throw new IllegalStateException("getWriter already called");
		}
		getOutputStreamCalled = true;
		return out;
	}

	public PrintWriter getWriter() throws IOException {
		if (writer != null) {
			return writer;
		}
		// Can't call getWriter if getOutputStream
		// has already been called
		if (getOutputStreamCalled) {
			throw new IllegalStateException("getOutputStream already called");
		}
		getWriterCalled = true;
		writer = new PrintWriter(charWriter);
		return writer;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		String s = null;
		// Only return a String if the writer was
		// used.
		if (writer != null) {
			s = charWriter.toString();
		} else if (getOutputStreamCalled) {
			s = out.toString();
		}
		return s;
	}
}
