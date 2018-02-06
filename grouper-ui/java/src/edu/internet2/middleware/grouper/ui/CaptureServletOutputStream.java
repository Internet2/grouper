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

import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import java.io.*;

/**
 * Used in conjunction with a HttpServletResponseWrapper so that data can be
 * captured
 * <p />
 * 
 * @author Gary Brown.
 * @version $Id: CaptureServletOutputStream.java,v 1.4 2007-04-11 08:19:24 isgwb Exp $
 */

public class CaptureServletOutputStream extends ServletOutputStream {
	private OutputStream out;

	/**
	 * Constructor - sets up internal ByteArrayOutputStream for capturing data
	 */
	public CaptureServletOutputStream() {
		out = new ByteArrayOutputStream();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.OutputStream#write(int)
	 */
	public void write(int b) throws IOException {
		out.write(b);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.OutputStream#write(byte[])
	 */
	public void write(byte[] b) throws IOException {
		out.write(b);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.OutputStream#write(byte[], int, int)
	 */
	public void write(byte[] b, int off, int len) throws IOException {
		out.write(b, off, len);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return out.toString();
	}

	/**
	 * (non-Javadoc)
	 *
	 * @see CaptureServletOutputStream#isReady()
	 */
	public boolean isReady() {
		return true;
	}

	/**
	 * (non-Javadoc)
	 *
	 * @see javax.servlet.ServletOutputStream#setWriteListener(javax.servlet.WriteListener)
 	 */
	public void setWriteListener(WriteListener writeListener) {
		// neither async nor HTTP upgrade in progress
		throw new IllegalStateException();
	}


}
