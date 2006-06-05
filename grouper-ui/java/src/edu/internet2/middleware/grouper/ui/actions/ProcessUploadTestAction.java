/*
Copyright 2004-2005 University Corporation for Advanced Internet Development, Inc.
Copyright 2004-2005 The University Of Bristol

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

package edu.internet2.middleware.grouper.ui.actions;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.DynaActionForm;
import org.apache.struts.upload.FormFile;
import org.w3c.dom.Attr;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.ui.util.DOMHelper;


/**
 * Uploads output file from JUnit / HtmlUnit tests and generates 'slide show'
 * 
 * @author Gary Brown.
 * @version $Id: ProcessUploadTestAction.java,v 1.3 2006-06-05 14:58:49 isgwb Exp $
 */
public class ProcessUploadTestAction extends GrouperCapableAction {
	

	//------------------------------------------------------------ Local
	// Forwards
	static final private String FORWARD_Frameset = "frameset";
	static final private String FORWARD_FramesetControl = "framesetControl";

	//------------------------------------------------------------ Action
	// Methods

	public ActionForward grouperExecute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response,
			HttpSession session, GrouperSession grouperSession)
			throws Exception {
		if("slides".equals(mapping.getParameter())) {
			return mapping.findForward(FORWARD_FramesetControl);
		}
		DynaActionForm uploadForm = (DynaActionForm) form;
		FormFile data = (FormFile)uploadForm.get("testData");
		Document doc = DOMHelper.newDocument(new String(data.getFileData()).replaceAll("&","&amp;"));
		String testRoot = session.getServletContext().getRealPath("/") + "_test\\";
		File root = new File(testRoot);
		if(!root.exists()) {
			root.mkdirs();
		}else{
			File[] files = root.listFiles();
			if(files!=null) {
				for(int i=0;i<files.length;i++) {
					files[i].delete();
				}
			}
		}
		Element userE = uk.ac.bris.is.xml.DOMHelper.getImmediateElement(doc.getDocumentElement(),"user");
		if(userE!=null) {
			NamedNodeMap user = userE.getAttributes();
			Map userMap = new LinkedHashMap();
			for(int i=0;i<user.getLength();i++) {
				Attr attr = (Attr)user.item(i);
				userMap.put(attr.getName(),attr.getValue());
			}
			session.setAttribute("uploadUser",userMap);
			
		}
		NodeList nodes = doc.getElementsByTagName("test");
		Element el;
		Element dataEl;
		CDATASection cd;
		String id = null;
		File outFile;
		int pageCount=0;
		List pages;
		List tests = new ArrayList();
		Map attr = null;
		for(int i=0;i<nodes.getLength();i++) {
			attr=new HashMap();
			tests.add(attr);
			el = (Element)nodes.item(i);
			id=el.getAttribute("id");
			attr.put("id",id);
			pages=getPages(el,testRoot);
			attr.put("pages",pages);
			pageCount+=pages.size();	
		}
		session.setAttribute("_tests",tests);
		session.setAttribute("_testPagesCount",new Integer(pageCount));
		return mapping.findForward(FORWARD_Frameset);
	}
	
	private List getPages(Element test,String testRoot) throws Exception{
		NodeList nodes = test.getElementsByTagName("page");
		Element el;
		Element dataEl;
		CDATASection cd;
		String no = null;
		String page;
		File outFile;
		PrintWriter pw;
		
		List pages = new ArrayList();
		Map attr = null;
		String data;
		int pos;
		for(int i=0;i<nodes.getLength();i++) {
			attr=new HashMap();
			pages.add(attr);
			el = (Element)nodes.item(i);
			no=el.getAttribute("no");
			dataEl = (Element)el.getFirstChild();
			cd = (CDATASection)dataEl.getFirstChild();
			page = "page"+no+".html";
			attr.put("page",page);
			attr.put("no",no);
			attr.put("url",el.getAttribute("url"));
			attr.put("messages",getMessages(el));
			outFile=new File(testRoot + page);
			pw = new PrintWriter(new FileWriter(outFile));
			data=cd.getData();
			if(data.indexOf("<base")==-1) {
				pos=data.indexOf("</title>") + 8;
				data = data.substring(0,pos) 
					+ "<base href=\"" + el.getAttribute("url") + "\"/>" + data.substring(pos);
			}
			pw.print(data);
			pw.close();
		}
		return pages;
	}
	
	
	private List getMessages(Element el) {
		List messages = new ArrayList();
		NodeList messageEls = el.getElementsByTagName("message");
		Map message;
		Element msgEl;
		for(int i=0;i<messageEls.getLength();i++) {
			msgEl = (Element)messageEls.item(i);
			message=new HashMap();
			message.put("isError", new Boolean("error".equals(msgEl.getAttribute("type"))));
			message.put("message",((Text)msgEl.getFirstChild()).getData());
			messages.add(message);
		}
		return messages;
	}
}