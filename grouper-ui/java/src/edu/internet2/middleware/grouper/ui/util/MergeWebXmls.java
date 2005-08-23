/*
 * Copyright (C) 2004-2005 University Corporation for Advanced Internet Development, Inc.
 * Copyright (C) 2004-2005 The University Of Bristol
 * All Rights Reserved. 
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 *  * Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *  * Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *  * Neither the name of the University of Bristol nor the names
 *    of its contributors nor the University Corporation for Advanced
 *   Internet Development, Inc. may be used to endorse or promote
 *   products derived from this software without explicit prior
 *   written permission.
 *
 * You are under no obligation whatsoever to provide any enhancements
 * to the University of Bristol, its contributors, or the University
 * Corporation for Advanced Internet Development, Inc.  If you choose
 * to provide your enhancements, or if you choose to otherwise publish
 * or distribute your enhancements, in source code form without
 * contemporaneously requiring end users to enter into a separate
 * written license agreement for such enhancements, then you thereby
 * grant the University of Bristol, its contributors, and the University
 * Corporation for Advanced Internet Development, Inc. a non-exclusive,
 * royalty-free, perpetual license to install, use, modify, prepare
 * derivative works, incorporate into the software or other computer
 * software, distribute, and sublicense your enhancements or derivative
 * works thereof, in binary and source code form.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND WITH ALL FAULTS.  ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING,
 * BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE, AND NON-INFRINGEMENT ARE DISCLAIMED AND the
 * entire risk of satisfactory quality, performance, accuracy, and effort
 * is with LICENSEE. IN NO EVENT SHALL THE COPYRIGHT OWNER, CONTRIBUTORS,
 * OR THE UNIVERSITY CORPORATION FOR ADVANCED INTERNET DEVELOPMENT, INC.
 * BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR
 * BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OR DISTRIBUTION OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package edu.internet2.middleware.grouper.ui.util;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.xml.transform.*;
import javax.xml.transform.stream.*;

/**
 * Class which can merge a series of web.xml files Designed to allow
 * contributions + site specific changes to be added to the Grouper supplied
 * web.xml file
 * <p />
 * 
 * @author Gary Brown.
 * @version $Id: MergeWebXmls.java,v 1.1.1.1 2005-08-23 13:04:16 isgwb Exp $
 */

public class MergeWebXmls {
	private String tempDir; //Dir for creating intermediate web.xml files

	private String xmlFiles; //List of files to be merged with...

	private String mergeXsl; //xsl File which does merging

	private String mergeTagsXml;//meta data for xsl

	private String finalXml; //Name of final file at end of all merges

	/**
	 * @param args
	 *            tempDir,xmlFiles,mergeXsl,mergeTagsXml,finalXml
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		MergeWebXmls mwx = new MergeWebXmls(args[0], args[1], args[2], args[3],
				args[4]);
		mwx.process();
	}

	/**
	 * @param tempDir
	 *            working directory
	 * @param xmlFiles
	 *            list of xml files to process
	 * @param mergeXsl
	 *            path to XSL stylesheet to merge with
	 * @param mergeTagsXml
	 *            path to XML data file which indicates how to merge
	 * @param finalXml
	 *            name of final file at end of all merges
	 */
	public MergeWebXmls(String tempDir, String xmlFiles, String mergeXsl,
			String mergeTagsXml, String finalXml) {
		this.tempDir = tempDir;
		this.xmlFiles = xmlFiles;

		this.mergeXsl = mergeXsl;
		this.mergeTagsXml = mergeTagsXml;
		this.finalXml = finalXml;
	}

	/**
	 * Given constructor supplied values merge web.xml files
	 * 
	 * @throws Exception
	 */
	public void process() throws Exception {

		String[] files = xmlFiles.split(File.pathSeparator);
		Arrays.sort(files);
		Map params = new HashMap();
		params.put("mergeTagsXmlFile", mergeTagsXml);
		String mergeIn = files[0];
		String out = null;
		System.out.println("Transforming: "
				+ xmlFiles.replaceAll(File.pathSeparator, "\n") + "\n\n");
		for (int i = 1; i < files.length; i++) {
			out = tempDir + File.separatorChar + "web." + i + ".xml";
			if (i == files.length - 1)
				out = finalXml;
			params.put("mergeXmlFile", mergeIn);
			System.out.println(files[i] + "\n + " + mergeIn + "\n -> " + out
					+ "\n");
			transform(files[i], out, mergeXsl, params);
			mergeIn = out;
		}
	}

	private static void transform(String data, String out, String xsl,
			Map parameters) throws Exception {
		File dataFile = new File(data);
		File outFile = new File(out);
		File xslFile = new File(xsl);

		TransformerFactory tf = TransformerFactory.newInstance();
		Transformer t = tf.newTransformer(new StreamSource(xslFile));
		if (parameters != null) {
			String key;
			Iterator it = parameters.keySet().iterator();
			while (it.hasNext()) {
				key = (String) it.next();
				t.setParameter(key, parameters.get(key));
			}
		}
		t.transform(new StreamSource(dataFile), new StreamResult(outFile));
	}

}