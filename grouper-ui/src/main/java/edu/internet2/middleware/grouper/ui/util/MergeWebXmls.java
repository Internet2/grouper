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
 * @version $Id: MergeWebXmls.java,v 1.6 2007-04-11 08:19:24 isgwb Exp $
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
		String mergeIn;
		String baseXml = files[0];
		String out = null;
		System.out.println("Transforming: "
				+ xmlFiles.replaceAll(File.pathSeparator, "\n") + "\n\n");
		for (int i = 1; i < files.length; i++) {
			mergeIn = files[i];
			
			out = tempDir + File.separatorChar + "web." + i + ".xml";
			if (i == files.length - 1)
				out = finalXml;
			params.put("mergeXmlFile", new File(mergeIn).toURI().toString());
			System.out.println("Base = " + baseXml + "\n + " + mergeIn + "\n -> " + out
					+ "\n");
			transform(baseXml, out, mergeXsl, params);
			baseXml = out;
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
		try {
			t.transform(new StreamSource(data), new StreamResult(out));
		}catch(Exception e) {
			System.err.println(e.getClass().getName() + ":" + e.getMessage());
		}
	}

}
