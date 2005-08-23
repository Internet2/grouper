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

package uk.ac.bris.is.grouper.quickstart; 

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import edu.internet2.middleware.grouper.Grouper;
import edu.internet2.middleware.grouper.GrouperAccess;
import edu.internet2.middleware.grouper.GrouperGroup;
import edu.internet2.middleware.grouper.GrouperMember;
import edu.internet2.middleware.grouper.GrouperNaming;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.GrouperStem;
import edu.internet2.middleware.grouper.SubjectFactory;
import edu.internet2.middleware.subject.Subject;

/**
 * Utility class which reads an XML file representing all or part of a Grouper repository. 
 * Has a main method which takes a command line argument to specify the Xml file to be
 * processed. Alternatively, call public static void load(GrouperSession s,Document doc). The Grouper API libraries
 * and configuration files must be on the classpath.
 * <p />
 <p>The following XML illustrates the format understood by XmlLoader. Subjects 
  must be available through an adapter configured in sources.xml. the getSubjectByIdentifier 
  method is used to lookup subject ids.</p>
<pre>&lt;registry&gt;
  &lt;path extension=&quot;qsuob&quot; displayExtension=&quot;QS University of Bristol&quot;&gt;
    &lt;naming priv=&quot;CREATE&quot; group=&quot;qsuob:admins&quot;/&gt;
    &lt;naming priv=&quot;STEM&quot; group=&quot;.:admins&quot;/&gt;
    &lt;group extension=&quot;admins&quot; displayExtension=&quot;UoB Administrators&quot;&gt;
      &lt;access priv=&quot;ADMIN&quot; group=&quot;*SELF*&quot;/&gt;
      &lt;access priv=&quot;READ&quot; group=&quot;..:all_staff&quot;/&gt;<br>      &lt;subject id=&quot;kebe&quot; name=&quot;Keith Benson&quot; /&gt;<br>    &lt;/group&gt;
    &lt;group extension=&quot;all_staff&quot; displayExtension=&quot;All staff&quot;&gt;
      &lt;access priv=&quot;UPDATE&quot; group=&quot;*SELF*&quot;/&gt;
      &lt;access priv=&quot;READ&quot; group=&quot;*SELF*&quot;/&gt;
      &lt;access priv=&quot;ADMIN&quot; group=&quot;..:admins&quot;/&gt;
      &lt;access priv=&quot;ADMIN&quot; person=&quot;kebe&quot;/&gt;<br>      &lt;subject id=&quot;kebe&quot; name=&quot;Keith Porter&quot; /&gt;
      &lt;subject group=&quot;faculties:artf:staff&quot; location=&quot;relative&quot;/&gt;<br>    &lt;/group&gt;
    &lt;path extension=&quot;faulties&quot; displayExtension=&quot;Faculties&quot;&gt;
      &lt;path extension=&quot;artf&quot; displayExtension=&quot;Art faculty&quot;&gt;
        &lt;group extension=&quot;staff&quot; displayExtension=&quot;staff&quot; description=&quot;Art faculty staff&quot;&gt;
        &lt;/group&gt;
      &lt;/path&gt;
    &lt;/path&gt;
  &lt;/path&gt;
&lt;/registry&gt; </pre>
<p><em>path</em> elements represent GrouperStems and <em>group</em> elements represent 
  GrouperGroups. Both elements must have <em>extension</em> and <em>displayExtension</em> 
  attributes and may have <em>description</em> attributes.</p>
<p><em>path</em> elements may contain other<em> path </em>elements, <em>group</em> 
  elements and <em>naming</em> elements. <em>naming</em> elements designate which 
  privilege (<em>priv</em> attribute) is granted to which subject (<em>person</em> 
  or <em>group</em> attribute).</p>
<p><em>group</em> elements may contain <em>access</em> elements which may have 
  the same attributes that <em>naming</em> elements have. They may also contain 
  <em>subject</em> elements which identify members through their <em>id</em> attribute 
  (person) or <em>group</em> attribute.</p>
<p>XmlLoader checks if stems, groups, memberships exist or if access or naming 
  privileges have been granted before execising the Grouper API to create objects 
  / grant privileges. If objects exist XmlLoader does not modify attributes even 
  if they are different. Existing memberships and privileges are not revoked if 
  they are not specified, but new ones will be added.</p>
<p>When assigning access privileges <em>group=&quot;*SELF*&quot;</em> indicates 
  the privilege should be granted to the group which is the parent of the access 
  element. For both access and naming privileges, if the<em> group</em> attribute 
  starts .: or ..: a relative group name is assumed and resolved to an absolute 
  name much as you would expect in DOS or UNIX command lines when moving in the 
  file system - except : is a separator rather than \ or /..</p>
<p>When making one group a memer of another group adding an attribute<em> location=&quot;relative&quot;</em> 
  causes the group atribute value to be added to the stem of the parent group 
  to determine an absolute group name for the member.</p>
<p></p>
 * 
 * @author Gary Brown.
 * @version $Id: XmlLoader.java,v 1.1.1.1 2005-08-23 13:03:13 isgwb Exp $
 */
public class XmlLoader {
	private final static String sep=":";
	private static GrouperSession s;
	private static List memberships = new ArrayList();
	private static List accessPrivs = new ArrayList();
	private static List namingPrivs = new ArrayList();
	
	/**
	 * Process an Xml file as the 'root' user.
	 * @param args args[0] = name of Xml file to process
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception{
		Subject sys = SubjectFactory.getSubject(Grouper.config("member.system"));
		GrouperSession s = GrouperSession.start(sys);
		String dataFile=args[0];
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		Document doc = db.parse(new File(dataFile));
		XmlLoader.load(s,doc);
		s.stop();
	}
	
	/**
	 * Recurse through the XML document and create any stems / groups, accumulating 
	 * memberships and privilege assignments for later - this ensures that any groups
	 * which will become members or have privileges granted to them actually exist!
	 * Create any memberships
	 * Grant naming privileges
	 * Grant access privileges
	 * @param s
	 * @param doc
	 * @throws Exception
	 */
	public static void load(GrouperSession s,Document doc) throws Exception {
		XmlLoader.s = s;
		Element root = doc.getDocumentElement();
		System.out.println("Starting load...");
		process(root,Grouper.NS_ROOT);
		processMemberships();
		processNamingPrivs();
		processAccessPrivs();
		System.out.println("Ending load...");
	}
	
	/**
	 * For each stem list and process any child stems.
	 * List and process any child groups
	 * @param e
	 * @param stem
	 * @throws Exception
	 */
	private static void process(Element e,String stem) throws Exception {	
		Collection paths = getImmediateElements(e,"path");
		Iterator it = paths.iterator();
		while(it.hasNext()) {
			Element path = (Element)it.next(); 
			processPath(path,stem);
		}
		
		Collection groups = getImmediateElements(e,"group");
		it = groups.iterator();
		while(it.hasNext()) {
			Element group = (Element)it.next();
			processGroup(group,stem);
		}
		
	}
	
	private static void processNamingPrivs() throws Exception {
		Element naming;
		String stem;
		GrouperMember member = null;
		GrouperStem grouperStem;
		Map map;
		String group;
		String person;
		String priv;
		Subject subj;
		String absoluteGroup;
		GrouperGroup privGroup;
		GrouperNaming grouperNaming = s.naming();
		for(int i=0;i<namingPrivs.size();i++) {
			map = (Map)namingPrivs.get(i);
			naming = (Element)map.get("naming");
			stem = (String) map.get("stem");
			group=naming.getAttribute("group");
			person=naming.getAttribute("person");
			priv=naming.getAttribute("priv");
			if(!isEmpty(group)) {
				absoluteGroup = getAbsoluteName(group,stem);
				privGroup = GrouperGroup.loadByName(s,absoluteGroup);
				member = GrouperMember.load(s,privGroup.id(),"group");
				
				System.out.println("Assigning " + priv + " to " + absoluteGroup + " for " + stem);
			}else if(!isEmpty(person)) {
				subj = SubjectFactory.getSubjectByIdentifier(person,"person");
				member = GrouperMember.load(s,subj);
				System.out.println("Assigning " + priv + " to " + subj.getName() + " for " + stem);
			}
			
			grouperStem = GrouperStem.loadByName(s,stem);
			if(!grouperNaming.has(s,grouperStem,member,priv)) {
				grouperNaming.grant(s,grouperStem,member,priv);
			}
		}
	}
	
	private static void processAccessPrivs() throws Exception {
		Element access;
		String stem;
		GrouperMember member=null;
		GrouperGroup grouperGroup;
		Map map;
		String group;
		String person;
		String priv;
		String absoluteGroup;
		GrouperGroup privGroup;
		Subject subj;
		GrouperAccess grouperAccess = s.access();
		for(int i=0;i<accessPrivs.size();i++) {
			map = (Map)accessPrivs.get(i);
			access = (Element)map.get("access");
			stem = (String) map.get("stem");
			group=access.getAttribute("group");
			person = access.getAttribute("person");
			priv=access.getAttribute("priv");
			grouperGroup = GrouperGroup.loadByName(s,stem);
			if(!isEmpty(group)) {
				absoluteGroup = getAbsoluteName(group,stem);
				privGroup = GrouperGroup.loadByName(s,absoluteGroup);
				member = GrouperMember.load(s,privGroup.id(),"group");
				
				System.out.println("Assigning " + priv + " to " + absoluteGroup + " for " + stem);
			}else if(!isEmpty(person)) {
				subj = SubjectFactory.getSubjectByIdentifier(person,"person");
				member = GrouperMember.load(s,subj);
				System.out.println("Assigning " + priv + " to " + subj.getName() + " for " + stem);
			}
			if(!grouperAccess.has(s,grouperGroup,member,priv)) {
				grouperAccess.grant(s,grouperGroup,member,priv);
			}
		}
	}
	
	public static String getAbsoluteName(String name,String stem) {
		if("*SELF*".equals(name))return stem;
		if(name!=null && name.startsWith(".")) {
			if(name.startsWith("." + sep)) {
				name=stem + name.substring(1);
			}else {
				while(name.startsWith(".." + sep)) {
					name = name.substring(3);
					stem = stem.substring(0,stem.lastIndexOf(sep));
				}
				name = stem + sep + name;
			}
		}
		return name;
	}
	
	private static void processMemberships() throws Exception {
		Element subject;
		String stem;
		GrouperMember member;
		GrouperGroup group;
		Map map;
		for(int i=0;i<memberships.size();i++) {
			map = (Map)memberships.get(i);
			subject = (Element)map.get("subject");
			stem = (String) map.get("stem");
			String id = subject.getAttribute("id");
			group = GrouperGroup.loadByName(s,stem);
			if(id !=null && id.length()!=0) {
				member = GrouperMember.load(s,id,"person");
				
				if(group!=null && !group.hasMember(member)) group.listAddVal(member);
			}else{
				String groupName = subject.getAttribute("group");
				if(groupName!=null && groupName.length()!=0) {
					if("relative".equals(subject.getAttribute("location"))) {
						groupName = group.attribute("stem").value() + sep + groupName;	
					}
					GrouperGroup groupSubj = GrouperGroup.loadByName(s,groupName);
					member = GrouperMember.load(s,groupSubj.id(),"group");
					if(group!=null && !group.hasMember(member)) group.listAddVal(member);
				}
			}
		}
	}
	
	private static void processPath(Element e,String stem) throws Exception {
		String extension = e.getAttribute("extension");
		String displayExtension = e.getAttribute("displayExtension");
		String description = e.getAttribute("description");
		String newStem = joinStem(stem,extension);
		GrouperStem existingStem = GrouperStem.loadByName(s,newStem);
		if(existingStem==null) {
			GrouperStem gs = GrouperStem.create(s,stem,extension);
			gs.attribute("displayExtension",displayExtension);
			if(description!=null && description.length()!=0) gs.attribute("description",description);
		}
		processNaming(e,newStem.replaceAll(Grouper.NS_ROOT + sep,""));
		process(e,newStem.replaceAll(Grouper.NS_ROOT + sep,""));
	}
	
	private static void processGroup(Element e,String stem) throws Exception {
		String extension = e.getAttribute("extension");
		String displayExtension = e.getAttribute("displayExtension");
		String description = e.getAttribute("description");
		String newStem = joinStem(stem,extension);
		GrouperGroup existingGroup=GrouperGroup.load(s,stem,extension);
		if(existingGroup==null) {
			GrouperGroup gg = GrouperGroup.create(s,stem,extension);
			gg.attribute("displayExtension",displayExtension);
			if(description!=null && description.length()!=0) gg.attribute("description",description);
		}
		processSubjects(e,newStem);
		processAccess(e,newStem);
		
	}
	
	
	private static void processSubjects(Element e,String stem) throws Exception{
		Collection subjects = getImmediateElements(e,"subject");
		Iterator it = subjects.iterator();
		Element subject;
		Map map;
		while(it.hasNext()) {
			subject = (Element)it.next();
			map = new HashMap();
			map.put("stem",stem);
			map.put("subject",subject);
			memberships.add(map);
		}
		
	}
	
	private static void processAccess(Element e,String stem) throws Exception{
		Collection accesses = getImmediateElements(e,"access");
		Iterator it = accesses.iterator();
		Element access;
		Map map;
		while(it.hasNext()) {
			access = (Element)it.next();
			map = new HashMap();
			map.put("stem",stem);
			map.put("access",access);
			accessPrivs.add(map);
		}
	}
	
	private static void processNaming(Element e,String stem) throws Exception{
		Collection namings = getImmediateElements(e,"naming");
		Iterator it = namings.iterator();
		Element naming;
		Map map;
		while(it.hasNext()) {
			naming = (Element)it.next();
			map = new HashMap();
			map.put("stem",stem);
			map.put("naming",naming);
			namingPrivs.add(map);
		}
	}
	
	
	
	private static String joinStem(String stem,String extension) {
		if(stem.equals(Grouper.NS_ROOT)) return extension;
		return stem + sep + extension;
	}
	
	/**
    * Returns immediate child elements with given name
    */
    public static Collection getImmediateElements(Element element,String elementName) throws Exception {
		NodeList nl = element.getElementsByTagName(elementName);
		Collection elements = new Vector();
		if(nl.getLength()<1) {
			return elements;
		}			
		Element child;
		for(int i=0;i<nl.getLength();i++) {
			child = (Element) nl.item(i);
			if(child.getParentNode().equals(element)) {
				elements.add(child);	
			}	
		}
		return elements;	
	 }
    
    private static boolean isEmpty(Object obj) {
    	if(obj==null || "".equals(obj))return true;
    	return false;
    }
}
