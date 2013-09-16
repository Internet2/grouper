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

package edu.internet2.middleware.grouper;


import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.exception.CompositeNotFoundException;
import edu.internet2.middleware.grouper.exception.GrantPrivilegeException;
import edu.internet2.middleware.grouper.exception.GroupNotFoundException;
import edu.internet2.middleware.grouper.exception.InsufficientPrivilegeException;
import edu.internet2.middleware.grouper.exception.MemberAddException;
import edu.internet2.middleware.grouper.exception.MemberDeleteException;
import edu.internet2.middleware.grouper.exception.MemberNotFoundException;
import edu.internet2.middleware.grouper.exception.MembershipNotFoundException;
import edu.internet2.middleware.grouper.exception.QueryException;
import edu.internet2.middleware.grouper.exception.SchemaException;
import edu.internet2.middleware.grouper.exception.SessionException;
import edu.internet2.middleware.grouper.exception.StemNotFoundException;
import edu.internet2.middleware.grouper.filter.GroupAnyAttributeFilter;
import edu.internet2.middleware.grouper.filter.GroupAttributeFilter;
import edu.internet2.middleware.grouper.filter.GrouperQuery;
import edu.internet2.middleware.grouper.filter.StemNameAnyFilter;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.privs.NamingPrivilege;
import edu.internet2.middleware.grouper.privs.Privilege;
import edu.internet2.middleware.grouper.privs.PrivilegeHelper;
import edu.internet2.middleware.grouper.subj.LazySubject;
import edu.internet2.middleware.grouper.subj.SubjectHelper;
import edu.internet2.middleware.grouper.subj.UnresolvableSubject;
import edu.internet2.middleware.grouper.ui.GroupOrStem;
import edu.internet2.middleware.grouper.ui.GrouperUiFilter;
import edu.internet2.middleware.grouper.ui.PersonalStem;
import edu.internet2.middleware.grouper.ui.UIThreadLocal;
import edu.internet2.middleware.grouper.ui.util.GroupAsMap;
import edu.internet2.middleware.grouper.ui.util.MembershipAsMap;
import edu.internet2.middleware.grouper.ui.util.ObjectAsMap;
import edu.internet2.middleware.grouper.ui.util.SubjectAsMap;
import edu.internet2.middleware.subject.Source;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.SubjectNotFoundException;
import edu.internet2.middleware.subject.SubjectNotUniqueException;
import edu.internet2.middleware.subject.SubjectType;
import edu.internet2.middleware.subject.provider.SourceManager;

/**
 * High level wrapper methods for the Grouper API, including workarounds. 
 * Primarily developed for the UI.<p />
 * Occasionally takes advantage of protected access to the Grouper API, however,
 * this access should not be required once the API is fully implemented.
 * <p />
 * 
 * @author Gary Brown.
 * @version $Id: GrouperHelper.java,v 1.64 2009-11-07 12:20:32 isgwb Exp $
 */


public class GrouperHelper {
  
  /** logger */
	protected static final Log LOG = LogFactory.getLog(GrouperHelper.class);

	private static Map superPrivs = null; //Privs automatically granted to the
										  // system user

	private static List personSources = null; //Subject sources which source
											  // 'people'

	public static final String HIER_DELIM = ":"; //Currently :
																// (name
																// separator)
	public static final String NS_ROOT = "Grouper.NS_ROOT";

	//Initialise system user privs
	static {
		superPrivs = new HashMap();
		superPrivs.put("read", Boolean.TRUE);
		superPrivs.put("view", Boolean.TRUE);
		superPrivs.put("update", Boolean.TRUE);
    superPrivs.put("admin", Boolean.TRUE);
    superPrivs.put("groupAttrRead", Boolean.TRUE);
    superPrivs.put("groupAttrUpdate", Boolean.TRUE);
		superPrivs.put("create", Boolean.TRUE);
    superPrivs.put("stem", Boolean.TRUE);
    superPrivs.put("stemAttrRead", Boolean.TRUE);
    superPrivs.put("stemAttrUpdate", Boolean.TRUE);
		//superPrivs.put("OPTIN", Boolean.TRUE);
		//superPrivs.put("OPTOUT", Boolean.TRUE);
	}
	
	//Privs which relate to Groups - access privileges
	private static String[] groupPrivs = { "admin", "update","read","view","optin","optout","groupAttrRead","groupAttrUpdate" };
	
//	Privs which relate to Groups - access privileges + member
	private static String[] groupPrivsWithMember = { "member", "admin", "update","read","view","optin","optout","groupAttrRead","groupAttrUpdate"};
	
	//Privs which relate to Stems - naming privileges
	//CH 20080324 change for UI from:  "STEM", "CREATE" 
	//private static String[] stemPrivs = {"Create Group", "Create Folder"};
	//GB 20080415 changed back, but UI looks up display name now for select options 
	private static String[] stemPrivs = {"create", "stem", "stemAttrRead", "stemAttrUpdate"};
	public static void main(String args[]) throws Exception{
		Subject subj = SubjectFinder.findById("GrouperSystem", true);
		GrouperSession s = GrouperSession.start(subj);

		//GroupType type = GroupType.createType(s,"teaching");
		GroupType type = GroupTypeFinder.find("committee", true);
		/*type.addField(s,"enforcer",FieldType.ATTRIBUTE,Privilege.getInstance("read"),Privilege.getInstance("update"),true);
		type = GroupTypeFinder.find("mailingList");
		type.addField(s,"alias",FieldType.ATTRIBUTE,Privilege.getInstance("read"),Privilege.getInstance("update"),true);
		type = GroupTypeFinder.find("studentUnion");
		type.addField(s,"campus",FieldType.ATTRIBUTE,Privilege.getInstance("read"),Privilege.getInstance("update"),true);
		type = GroupTypeFinder.find("personal");
		type.addField(s,"proxy",FieldType.ATTRIBUTE,Privilege.getInstance("read"),Privilege.getInstance("update"),true);
		*/
		type = GroupTypeFinder.find("community", true);
		type.addList(s,"contributors",Privilege.getInstance("read"),Privilege.getInstance("update"));
		type.addAttribute(s,"scope",Privilege.getInstance("read"),Privilege.getInstance("update"),true);
		type = GroupTypeFinder.find("staff", true);
		type.addAttribute(s,"dept",Privilege.getInstance("read"),Privilege.getInstance("update"),true);
		/*type.addField(s,"staff",FieldType.LIST,Privilege.getInstance("read"),Privilege.getInstance("update"),false);
		type.addField(s,"clerical",FieldType.LIST,Privilege.getInstance("read"),Privilege.getInstance("update"),false);
		type.addField(s,"faculty_code",FieldType.ATTRIBUTE,Privilege.getInstance("read"),Privilege.getInstance("update"),true);
		type.addField(s,"org_code",FieldType.ATTRIBUTE,Privilege.getInstance("read"),Privilege.getInstance("update"),true);
		
		Group g = new Group(s,StemFinder.findRootStem(s),"tGroup","Teaching Group");
		g.addType(type);
		g.setAttribute("faculty_code","ARTF");
		g.setAttribute("org_code","FREN");
		Set x = SubjectFinder.findAll("keith"); 
		Iterator it = x.iterator();
		Subject subject;
		while(it.hasNext()) {
			subject=(Subject)it.next();
			g.addMember(subject,FieldFinder.find("staff"));
		}
		
		x = SubjectFinder.findAll("fiona");
		it = x.iterator();
		
		while(it.hasNext()) {
			subject=(Subject)it.next();
			g.addMember(subject,FieldFinder.find("clerical"));
		}
		
		
		
		
		*/
		
		s.stop();
	}


	/**
	 * Given a GrouperStem id return a list of stems and groups for which the
	 * GrouperStem is an immediate parent
	 * @param s GrouperSession for authenticated user
	 * @param stemId GrouperStem id
	 * @return List of all stems and groups for stemId
	 */
	public static List getChildren(GrouperSession s, String stemId) throws StemNotFoundException{
		Stem stem =null;
		if("".equals(stemId)) {
			stem=StemFinder.findRootStem(s);
		}else{
			stem=StemFinder.findByName(s, stemId, true);
		}
		ArrayList res = new ArrayList();
		Set children = stem.getChildStems();
		Iterator it = children.iterator();
		Stem childStem = null;
		while(it.hasNext()) {
			childStem=(Stem)it.next();
			res.add(GroupOrStem.findByStem(s,childStem));
		}
		children=stem.getChildGroups();
		it = children.iterator();
		Group childGroup = null;
		while(it.hasNext()) {
			childGroup=(Group)it.next();
			res.add(GroupOrStem.findByGroup(s,childGroup));
		}
		return res;
	}

	/**
	 * Given a list of GrouperAttributes, return a list of GrouperStems
	 * which the attributes belong to, and load all atributes for these Stems
	 * 
	 * @param s GrouperSession for authenticated user
	 * @param list of GrouperAttributes
	 * @return List of GrouperGroups or GrouperStems
	 */
	/*public static List instantiateStems(GrouperSession s, List list) {
		return instantiateGroups(s, list);
	}*/



	/**
	 * Given a list of GrouperAttributes, return a list of GrouperGroups
	 * which the attributes belong to, and load all atributes for these groups
	 * 
	 * @param s GrouperSession for authenticated user
	 * @param list of GrouperAtributes
	 * @return List of GrouperGroups or GrouperStems
	 */
	/*public static List instantiateGroups(GrouperSession s, List list) {
		List instantiated = new ArrayList();
		Attribute attr = null;
		String key;
		Stem stem = null;
		for (int i = 0; i < list.size(); i++) {
			attr = (Attribute) list.get(i);
			key = attr.key();

			//stem=Cache.instance().getGroup(s,key);
			if (stem == null) {
				try {
				stem = (GrouperGroup) GrouperGroup.loadByKey(s, key);
				}catch(InsufficientPrivilegeException e) {
					throw new RuntimeException(e);
				}
				//Cache.instance().put(s,key,stem);
			}


	/**
	 * Given a list of GrouperList objects return a list of instantiated
	 * GrouperGroups as Maps
	 * 
	 * @param s GrouperSession for authenticated user
	 * @param groups List of GrouperLists
	 * @return List of GrouperGroups wrapped as Maps
	 */
	public static List groups2Maps(GrouperSession s, List groups) throws GroupNotFoundException{
		List maps = new ArrayList();
		Object obj;
		for (int i = 0; i < groups.size(); i++) {
			//Just in case something goes wrong - Group doesn't exist but still
			// a pointer to it
			try {
				obj = groups.get(i);
				if (obj instanceof Membership)
					obj = ((Membership) obj).getGroup();
				maps.add(group2Map(s, (Group) obj));
			} catch (NullPointerException e) {
				//@TODO What should happen?
			}
		}
		return maps;

	}

	/**
	 * Given a ist of GrouperList objects return a list of instantiated GrouperStems
	 * as Maps
	 * @param s GrouperSession for authenticated user
	 * @param stems List of GrouperLists
	 * @return List of GrouperStems wrapped as Maps
	 */
	public static List stems2Maps(GrouperSession s, List stems) throws GroupNotFoundException{
		List maps = new ArrayList();
		Object obj;
		for (int i = 0; i < stems.size(); i++) {
			//Just in case something goes wrong - Group doesn't exist but still
			// a pointer to it
			try {
				obj = stems.get(i);
				
				maps.add(stem2Map(s, (Stem) obj));
			} catch (NullPointerException e) {
				//@TODO What should happen?
			}
		}
		return maps;

	}
	
	/**
	 * Given a GrouperStem return a Map representing it
	 * @param s GrouperSession for authenticated user
	 * @param stem GrouperStem to wrap
	 * @return GrouperStem wrapped as a Map
	 */
	public static Map stem2Map(GrouperSession s, Stem stem) {
		Map stemMap = ObjectAsMap.getInstance("StemAsMap", stem, s);
		if("".equals(stem.getName())) {
			stemMap.put("isRootStem",Boolean.TRUE);
		}
		return stemMap;

	}
	
	/**
	 * Given a Stem return a Map representing it
	 * @param s GrouperSession for authenticated user
	 * @param stem GrouperStem to wrap
	 * @return Stem wrapped as a Map
	 */
	public static Map group2Map(GrouperSession s, Stem stem) {
		return ObjectAsMap.getInstance("StemAsMap", stem, s);		
	}
	
	/**
	 * Given a GroupOrStem return a Map representing it
	 * @param s GrouperSession for authenticated user
	 * @param groupOrStem GroupOrStem to wrap
	 * @return GroupOrStem wrapped as a Map
	 */
	public static Map group2Map(GrouperSession s, GroupOrStem groupOrStem) {
		return groupOrStem.getAsMap();
	}

	/**
	 * Given a Group  return
	 * a Map representation of it
	 * @param s GrouperSession for authenticated user
	 * @param group Group to wrap
	 * @return Group wrapped as a Map
	 */
	public static Map group2Map(GrouperSession s, Group group){ 
		ObjectAsMap map = new GroupAsMap(group, s);
			if (map.get("displayExtension") == null)
				map.put("displayExtension", map.get("extension"));
		return (Map) map;
	}

	/**
	 * Given a GrouperGroup or GrouperStem return a list of ancestor GrouperStems
	 * as Maps
	 * 
	 * @param s GrouperSession for authenticated user
	 * @param groupOrStem GrouperGroup or GrouperStem
	 * @return List of ancestor GrouperStems wrapped as Maps
	 */
	public static List parentStemsAsMaps(GrouperSession s, GroupOrStem groupOrStem) throws StemNotFoundException{
		List path = new ArrayList();
		if(groupOrStem==null) return path;
		Map map = group2Map(s, groupOrStem);

		Stem curStem = null;
		while (!GrouperHelper.NS_ROOT.equals(map.get("stem"))) {
			curStem = StemFinder.findByName(s, (String) map.get("stem"), true);
			if (curStem != null) {
				map = stem2Map(s, curStem);
				path.add(0, map);
			}
		}
		return path;
	}

	
	/**
	 * Given a GrouperGroup or GrouperStem return a Map where the keys
	 * are access or naming privileges that s.subject() has.
	 * 
	 * @param s GrouperSession for authenticated user
	 * @param groupOrStem GrouperGroup or GroupeStem for which privileges are being requested
	 * @return Map representing privileges
	 */
	public static Map hasAsMap(GrouperSession s, GroupOrStem groupOrStem) throws MemberNotFoundException{
		return hasAsMap(s, groupOrStem, false);
	}

	/**
	 * Given a GrouperGroup or GrouperStem return a Map where the keys are
	 * access or naming privileges that s.subject() has. If isMortal == false
	 * and the subject is the system user, all privileges are returned.
	 * @param s GrouperSession for authenticated user
	 * @param groupOrStem GrouperGroup or GroupeStem for which privileges are being requested
	 * @param isMortal if system user should they be teated as such
	 * @return Map representing privileges
	 */
	public static Map hasAsMap(GrouperSession s, GroupOrStem groupOrStem,
			boolean isMortal) throws MemberNotFoundException{
		Map privs = null;

		Group g = null;
		Stem stem = null;
		boolean isActiveWheelGroupMember = Boolean.TRUE.equals(UIThreadLocal.get("isActiveWheelGroupMember"));
		privs = new HashMap();
		if (!isMortal
				&& ("GrouperSystem".equals(s.getSubject().getId())
						|| isActiveWheelGroupMember)) {
			privs.putAll(superPrivs);
			if(groupOrStem==null) return privs;
			
			if (groupOrStem.isGroup()) {
				g = groupOrStem.getGroup();
				
				if (g.hasMember(s.getSubject())) {
					privs.put("member", Boolean.TRUE);
				}
			} else {
				stem = groupOrStem.getStem();
				if(stem.isRootStem()) privs.remove("create");
			}
			if (privs == null)
				privs = superPrivs;
			return privs;
		}
		if("GrouperSystem".equals(s.getSubject().getId())
				||isActiveWheelGroupMember) {
			privs = new HashMap();
			privs.put("stem",Boolean.TRUE);
			if(groupOrStem!=null && groupOrStem.isStem()&& !"".equals(groupOrStem.getStem().getName())) {
				privs.put("create",Boolean.TRUE);
			}
			return privs;
		}
		
		if(groupOrStem==null) return new HashMap();
		
			g = groupOrStem.getGroup();
		
			stem = groupOrStem.getStem();
		
		Set privList = null;
		if (g != null) {
			privList = g.getPrivs(s.getSubject());
		} else {
			privList = stem.getPrivs(s.getSubject());
		}
		if(privList !=null) {
			Iterator it = privList.iterator();
			Object p = null;
			while(it.hasNext()){
				p=it.next();
				if(p instanceof AccessPrivilege) {
					privs.put(((AccessPrivilege)p).getName(), Boolean.TRUE);
				}else if(p instanceof NamingPrivilege) {
					privs.put(((NamingPrivilege)p).getName(), Boolean.TRUE);
				}else{
					privs.put(it.next(), Boolean.TRUE);
				}
			}
		}
		if (g != null) {
			
			if (g.hasMember(s.getSubject()))
				privs.put("member", Boolean.TRUE);
		}
		return privs;
	}

	/**
	 * Given a GrouperGroup or GrouperStem return a Map where the keys
	 * are access or naming privileges that member.subject() has.
	 * 
	 * @param s GrouperSession for authenticated user
	 * @param groupOrStem GrouperGroup or GroupeStem for which privileges are being requested
	 * @param member Subject who privileges were granted to
	 * @return Map representing privileges
	 */
	public static Map hasAsMap(GrouperSession s, GroupOrStem groupOrStem,
		Member member,Field field) throws SubjectNotFoundException,SchemaException{
		Map privs = null;
		if ("GrouperSystem".equals(member.getSubjectId())) {
			//@TODO Review
			//return superPrivs;

		}
		Set privList = null;

		privs = new HashMap();
		Group group = null;
		Stem stem = null;
		if (groupOrStem.isGroup()) {
			group = groupOrStem.getGroup();
			privList = group.getPrivs(member.getSubject());
		} else {
			stem = groupOrStem.getStem();
			privList = stem.getPrivs(member.getSubject());
		}
		Iterator it = privList.iterator();
		Object obj;
		String p;
		while(it.hasNext()) {
			obj=it.next();
			if(obj instanceof AccessPrivilege) {
				p = ((AccessPrivilege)obj).getName();
			}else{
				p = ((NamingPrivilege)obj).getName();
			}
			privs.put(p, Boolean.TRUE);
		}
		if (group != null) {
			if (group.hasMember(member.getSubject(),field))
				privs.put("member", Boolean.TRUE);
		}

		return privs;
	}

	/** Given a Subject return a Map representation of it
	 * @param subject to be wrapped
	 * @return Subject wrapped as a Map
	 */
	public static Map subject2Map(Subject subject) {
		//@TODO what should happen if Group - see next method
		SubjectAsMap map = (SubjectAsMap)ObjectAsMap.getInstance("SubjectAsMap", subject);
		return (Map) map;
	}
	
	/** Given a Subject return a Map representation of it
	 * @param subject to be wrapped
	 * @param addAttr Map of additional attributes
	 * @return Subject wrapped as a Map
	 */
	public static Map<Object, Object> subject2Map(Subject subject,Map addAttr) {
		//@TODO what should happen if Group - see next method
		SubjectAsMap map = (SubjectAsMap)ObjectAsMap.getInstance("SubjectAsMap", subject);
		if(addAttr !=null) map.putAll(addAttr);
		return (Map) map;
	}

	/**
	 * Given a subject id and subject type and a Map, return a Map representation of the
	 * subject and add the key/value pairs from the input Map.
	 * 
	 * @param s GrouperSession for authenticated user
	 * @param subjectId Subject id
	 * @param subjectType Subject type e.g. person, group
	 * @param addAttr Map of aditional attributes
	 * @return Subject wrapped as a Map
	 */
	public static Map subject2Map(GrouperSession s, String subjectId,
			String subjectType,String sourceId,Map addAttr) throws SubjectNotFoundException{
		Map subjectMap = subject2Map(s,subjectId,subjectType,sourceId);
		if(addAttr !=null) subjectMap.putAll(addAttr);
		return subjectMap;
	}
	
	/**
	 * Given a subject id and subject type return a Map representation of it.
	 * 
	 * @param s GrouperSession for authenticated user
	 * @param subjectId Subject id
	 * @param subjectType Subject type e.g. person, group
	 * @return Subject wrapped as a Map
	 */
	public static Map subject2Map(GrouperSession s, String subjectId,
			String subjectType,String sourceId) throws SubjectNotFoundException{
		if (!"group".equals(subjectType)) {
			Subject subject = null;
			try {
				subject = SubjectFinder.findById(subjectId, subjectType,sourceId, true);
			} catch (Exception e) {
				LOG.error(e);
				subject = new UnresolvableSubject(subjectId,subjectType,sourceId); 
			}
			SubjectAsMap map =(SubjectAsMap)ObjectAsMap.getInstance("SubjectAsMap", subject);
			return (Map) map;
		}
		try {
		Group group = GroupFinder.findByUuid(s, subjectId, true);
		Map groupMap = group2Map(s, group);
		return groupMap;
		}catch(GroupNotFoundException e) {
			throw new SubjectNotFoundException(e.getMessage(), e);
		}
	}
	
	/**
	 * Given an array of Subjects return a List of Maps representing those subjects
	 * 
	 * @param objects array of Subjects
	 * @param addAttr Map of additional attributes
	 * @return List of Subjects wrapped as Maps
	 */
	public static List subjects2Maps(Object[] objects,Map addAttr) {
		if (objects instanceof Subject[])
			return subjects2Maps((Subject[]) objects);
		Subject[] subjects = new Subject[objects.length];
		for (int i = 0; i < objects.length; i++) {
			subjects[i] = (Subject) objects[i];
		}
		return subjects2Maps(subjects,addAttr);
	}
	
	/**
	 * Given an array of Subjects return a List of Maps representing those subjects
	 * 
	 * @param objects array of Subjects
	 * @return List of Subjects wrapped as Maps
	 */
	public static List subjects2Maps(Object[] objects) {
		
		return subjects2Maps(objects,null);
	}

	/**
	 * Given an array of Subjects return a List of Maps representing those subjects
	 * 
	 * @param subjects array of Subjects
	 * @param addAttr Map of aditional attributes
	 * @return List of Subjects wrapped as Maps
	 */
	public static List<Map<Object,Object>> subjects2Maps(Subject[] subjects,Map addAttr) {
		List maps = new ArrayList();
		for (int i = 0; i < subjects.length; i++) {
			if(subjects[i]!=null) maps.add(subject2Map(subjects[i],addAttr));
		}
		return maps;
	}
	
	/**
	 * Given an array of Subjects return a List of Maps representing those subjects
	 * 
	 * @param subjects array of Subjects
	 * @return List of Subjects wrapped as Maps
	 */
	public static List subjects2Maps(Subject[] subjects) {
		List maps = new ArrayList();
		for (int i = 0; i < subjects.length; i++) {
			maps.add(subject2Map(subjects[i]));
		}
		return maps;
	}

	
	/**
	 * Given a list of GrouperList objects return a sublist from 
	 * start to start + pageSize of each GrouperList.member() as a Map
	 *  
	 * @param s GrouperSession for authenticated user
	 * @param members List of GrouperLists or GrouperMembers
	 * @param start 0 based start index
	 * @param pageSize number of results to return
	 * @return List of Subjects wrapped as Maps
	 */
	public static List groupList2SubjectsMaps(GrouperSession s, List members,
			int start, int pageSize) 
		throws GroupNotFoundException,SubjectNotFoundException,
		MemberNotFoundException,SubjectNotUniqueException{
		return groupList2SubjectsMaps(s, members, null, start, pageSize);
	}

	/**
	 * Given a list of GrouperMembers return a list of Map representations of them
	 * @param s GrouperSession for authenticated user
	 * @param members List of GrouperLists or GrouperMembers
	 * @return List of Subjects wrapped as Maps
	 */
	public static List groupList2SubjectsMaps(GrouperSession s, List members) 
		throws GroupNotFoundException,SubjectNotFoundException,
		MemberNotFoundException,SubjectNotUniqueException{
		return groupList2SubjectsMaps(s, members, null);
	}

	/**
	 * Given a list of GrouperMembers return a list of Map representations of them
	 * where the key asMemberOf is set to the value of the parameter asMemberOf
	 * @param s GrouperSession for authenticated user
	 * @param members List of GrouperLists or GrouperMembers
	 * @param asMemberOf GrouperGroup id identifying context
	 * @return List of Subjects wrapped as Maps
	 */
	public static List groupList2SubjectsMaps(GrouperSession s, List members,
			String asMemberOf) 
			throws GroupNotFoundException,SubjectNotFoundException,
			MemberNotFoundException,SubjectNotUniqueException{
		return groupList2SubjectsMaps(s, members, asMemberOf, 0, members.size());
	}

	/**
	 * Given a list of GrouperMembers return a list of Map representations of 
	 * a sublist of them (from start to start + pageSize) where the key 
	 * asMemberOf is set to the value of the parameter asMemberOf
	 * @param s GrouperSession for authenticated user
	 * @param members List of GrouperLists or GrouperMembers
	 * @param asMemberOf GrouperGroup id identifying context
	 * @param start of sublist
	 * @param pageSize number of results to return
	 * @return List of Subjects wrapped as Maps
	 */
	public static List groupList2SubjectsMaps(GrouperSession s, List members,
			String asMemberOf, int start, int pageSize) 
			throws GroupNotFoundException,SubjectNotFoundException,
				MemberNotFoundException,SubjectNotUniqueException{
		int end = start + pageSize;
		if (end > members.size())
			end = members.size();
		List maps = new ArrayList();
		Membership list = null;
		Member member = null;
		Subject subject=null;
		Map subjMap = null;
		Object listItem;
		Group via = null;
		Set chain = null;
		Object chainItem = null;
		Group firstInChain = null;
		Member chainMember = null;
		String[] chainGroupIds = null;
		int chainSizeAdjustment=1;
		String[] emptyStrArray=new String[]{};
		boolean isChainSameAsList = false;
		for (int i = start; i < end; i++) {
			chainGroupIds = emptyStrArray;
			listItem = members.get(i);
			if (listItem instanceof Membership) {
				list = (Membership) listItem;
				try{
					via = (Group) list.getViaGroup();
				}catch(GroupNotFoundException e){via=null;}
				chain = list.getChildMemberships();

				if (chain != null && chain.size() > 0) {
					//chainGroupIds = getChainGroupIds(s,list);
					
				} else {
					firstInChain = null;
				}
				member = list.getMember();
				try {
					subject = member.getSubject();
				} catch (Exception e) {
					throw new RuntimeException(e);
				}

			} else if (listItem instanceof Membership) {
				member = (Member) list.getMember();
				try {
					subject = member.getSubject();
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}else if(listItem instanceof Subject) {
				subject = (Subject)listItem;
			}else if(listItem instanceof Group) {
				
				Subject subj = SubjectFinder.findById(asMemberOf, true);
				Map gSubjMap = subject2Map(subj);
				Map gMap = group2Map(s,(Group)listItem);
				gSubjMap.put("memberOfGroup",gMap);
				gSubjMap.put("asMemberOf",((Group)listItem).getUuid());
				maps.add(gSubjMap);
				continue;
			}else if(listItem instanceof Stem) {
				
				Subject subj = SubjectFinder.findById(asMemberOf, true);
				Map sSubjMap = subject2Map(subj);
				Map sMap = stem2Map(s,(Stem)listItem);
				sSubjMap.put("memberOfGroup",sMap);
				sSubjMap.put("asMemberOf",((Stem)listItem).getUuid());
				maps.add(sSubjMap);
				continue;
			}
			
			if (subject.getType().getName().equals("group")) {
				Group group = GroupFinder.findByUuid(s, subject.getId());
				subjMap = group2Map(s, group);
			} else {
				subjMap = subject2Map(subject);
			}
			if (firstInChain != null)
				subjMap.put("via", group2Map(s, firstInChain));
			//Group group = list.getGroup();
			
			if (asMemberOf != null) {
				try{
					subjMap.put("memberOfGroup",group2Map(s,GroupFinder.findByUuid(s,asMemberOf)));
					subjMap.put("asMemberOf", asMemberOf);
				}catch(GroupNotFoundException e){}
			}else{
				if(list !=null) {
					subjMap.put("memberOfGroup",group2Map(s,list.getGroup()));
					subjMap.put("asMemberOf", list.getGroup().getUuid());
					
				}
				//subjMap.put("asMemberOf", group.getUuid());
			}
			if (chain != null) {
				subjMap.put("chain", chain);
				//subjMap.put("chainSize", new Integer(chain.size()));
				//subjMap.put("chainGroupIds", chainGroupIds);
			}
			if(via!=null)subjMap.put("via", via);

			maps.add(subjMap);
		}
		return maps;
	}
	
	public static String[] getChainGroupIds(GrouperSession s,Membership list) 
		throws MemberNotFoundException,GroupNotFoundException{
		Set chainIds = new LinkedHashSet();
		Member chainMember;
		Set chain = list.getChildMemberships();
		String[] chainGroupIds={};
		Group via = list.getViaGroup();
		if(via ==null && (chain==null ||chain.size()==0)) return chainGroupIds;
		if (via !=null) chainIds.add(via.getUuid());
		Membership gl;
		Membership mv;
		Iterator it = chain.iterator();
		while(it.hasNext()) {
			try {
				gl =(Membership) it.next();
				//gl=mv.toList(s);
			    chainMember = gl.getMember();
			
			chainIds.add(chainMember.getSubjectId());
			}catch(NullPointerException npe) {
				//chainGroupIds[j] = "!";
			}
		}
		chainGroupIds = (String[])chainIds.toArray(chainGroupIds);
		return chainGroupIds;
	}
	
	
	/**
	 * Given a GroupeGroup or GrouperStem id, an array of subjects and an array of 
	 * privileges, grant the privileges to each subject for the GrouperStem or 
	 * GrouperGroup
	 * 
	 * @param s GrouperSession for authenticated user
	 * @param stemOrGroupId GrouperGroup or GrouperStem id
	 * @param members array of Subjects
	 * @param privileges array of privileges
	 * @param forStems indicates GrouperStem
	 */
	public static void assignPrivileges(GrouperSession s, String stemOrGroupId,
			Subject[] members, String[] privileges, boolean forStems) 
		throws SchemaException,MemberAddException,InsufficientPrivilegeException,MemberNotFoundException,
			GrantPrivilegeException{
		assignPrivileges(s,stemOrGroupId,members,privileges,forStems,FieldFinder.find("members"));
	}
	
	/**
	 * Given a GroupeGroup or GrouperStem id, an array of subjects and an array of 
	 * privileges, grant the privileges to each subject for the GrouperStem or 
	 * GrouperGroup
	 * 
	 * @param s GrouperSession for authenticated user
	 * @param stemOrGroupId GrouperGroup or GrouperStem id
	 * @param members array of Subjects
	 * @param privileges array of privileges
	 * @param forStems indicates GrouperStem
	 */
	public static void assignPrivileges(GrouperSession s, String stemOrGroupId,
			Subject[] members, String[] privileges, boolean forStems,Field field) 
		throws SchemaException,MemberAddException,InsufficientPrivilegeException,MemberNotFoundException,
			GrantPrivilegeException{
		Group group = null;
		Stem stem = null;
		Subject subject;
		GroupOrStem  groupOrStem=GroupOrStem.findByID(s,stemOrGroupId);
		
			stem = groupOrStem.getStem();
			group = groupOrStem.getGroup();
		boolean circular = false;
		for (int i = 0; i < members.length; i++) {
			subject = members[i];
			for (int j = 0; j < privileges.length; j++) {
				try {
					if ("member".equals(privileges[j].toLowerCase()) && !group.hasImmediateMember(subject,field)) {
						if(group.toSubject().equals(subject) && field.getName().equals("members")) {
							circular=true;
						}else{
							group.addMember(subject,field);						
						}
					} else if (groupOrStem.isStem()) {
						stem.grantPriv(subject,Privilege.getInstance(privileges[j]));

					} else if(!"member".equals(privileges[j].toLowerCase())){
						group.grantPriv(subject,Privilege.getInstance(privileges[j]));

					}
				} catch (GrantPrivilegeException e) {
					//@TODO Expect different type of Exception in future
					if (e.getMessage()==null || e.getMessage().indexOf("membership already exists") == -1)
						throw e;
				} 
			}
		}
		if(circular) {
			throw new IllegalArgumentException("Circular membership");
		}

	}

	
	/**
	 * Given the UI browsing mode return a Map where each key is a valid stem
	 * for browsing. The Map is used to filter out stems which would lead to a 
	 * dead end.<p/>
	 * In a future version, this code should be factored into a new Class with interface
	 * so that new browse modes can be defined and implemented.
	 * 
	 * @param s GrouperSession for authenticated user
	 * @param browseMode - UI browse mode
	 * @return Map where keys are valid stems
	 */
	/*public static Map getValidStems(GrouperSession s, String browseMode) {
		Map stems = new HashMap();
		List groups = null;
		GrouperAccess accessImpl = s.access();
		GrouperNaming namingImpl = s.naming();
		if ("".equals(browseMode)) {
			GrouperMember member = null;
			try {
				member = GrouperMember.load(s, s.subject().getId(), s.subject()
						.getType().getName());
			} catch (SubjectNotFoundException e) {
				throw new RuntimeException(e);
			}
			groups = member.listVals();

		} else if ("Create".equals(browseMode)) {
			groups = namingImpl.has(s, Grouper.PRIV_CREATE);
			List stemmable = namingImpl.has(s, Grouper.PRIV_STEM);
			groups.addAll(stemmable);
		} else if ("Manage".equals(browseMode)) {
			groups = accessImpl.has(s, Grouper.PRIV_ADMIN);
			List others = accessImpl.has(s, Grouper.PRIV_UPDATE);
			groups.addAll(others);
			others = accessImpl.has(s, Grouper.PRIV_READ);
			groups.addAll(others);
			List creatable = namingImpl.has(s, Grouper.PRIV_CREATE);
			List stemmable = namingImpl.has(s, Grouper.PRIV_STEM);
			groups.addAll(creatable);
			groups.addAll(stemmable);
		} else if ("Join".equals(browseMode)) {
			groups = accessImpl.has(s, Grouper.PRIV_OPTIN);
		} else if ("All".equals(browseMode)) {
			//return new HashMap();
			groups = accessImpl.has(s, Grouper.PRIV_VIEW);
			//@TODO add OPTINS?
		}
		GrouperGroup group;
		GrouperList grouperList;
		String groupKey;
		String stem;
		int pos = 0;
		String partStem;
		String gkey;
		String name;
		
		for (int i = 0; i < groups.size(); i++) {
			grouperList = (GrouperList) groups.get(i);
			name = grouperList.group().name();

			if (!stems.containsKey(name)) {
				stems.put(name, Boolean.TRUE);

				pos = 0;
				while (name.indexOf(HIER_DELIM, pos) > -1) {
					pos = name.indexOf(HIER_DELIM, pos);
					partStem = name.substring(0, pos);
					pos++;
					stems.put(partStem, Boolean.TRUE);

				}
			}
		}
		return stems;
	}*/

	
	/**
	 * Is s.subject() the system user?
	 * 
	 * @param s GrouperSession for authenticated user
	 * @return boolean
	 */
	public static boolean isSuperUser(GrouperSession s) {
		return s.getSubject().getId().equals("GrouperSystem")||Boolean.TRUE.equals(UIThreadLocal.get("isActiveWheelGroupMember"));
	}

	/**
	 * Given a Subject id and SubjectType return a Subject - or null if one
	 * not found
	 * 
	 * @param subjectId Subject id
	 * @param subjectType Subject type
	 * @return Subject
	 */
	public static Subject getSubjectFromIdAndType(String subjectId,
			String subjectType) {
		try {
			return SubjectFinder.findById(subjectId, subjectType);
		} catch (Exception e) {
		}
		return null;

	}

	/**
	 * Given a GrouperGroup id return the GrouperGroup
	 * 
	 * @param s GrouperSession for authenticated user
	 * @param id GrouperGroup id
	 * @return GrouperGroup
	 */
	public static Group groupLoadById(GrouperSession s, String id) throws GroupNotFoundException{

		Group group = null;

		group = GroupFinder.findByUuid(s, id);
		
		return group;

	}

	/**
	 * Return an array of all access privileges + member
	 * 
	 * @param s GrouperSession for authenticated user
	 * @return array of privilege names
	 */
	public static String[] getGroupPrivsWithMember(GrouperSession s) {
		return groupPrivsWithMember;
	}
	
	/**
	 * Return an array of all access privileges
	 * 
	 * @param s GrouperSession for authenticated user
	 * @return array of privilege names
	 */
	public static String[] getGroupPrivs(GrouperSession s) {
		return groupPrivs;
	}

	/**
	 * Return an array of all naming privileges
	 * @param s GrouperSession for authenticated user
	 * @return array of privilege names
	 */
	public static String[] getStemPrivs(GrouperSession s) {
		return stemPrivs;
	}
	
	/**
	 * Return a Collection of all naming privileges
	 * @param bundle ResourceBundle to lookup display name
	 * @return Collection of Maps of privilege names and display names
	 */
	public static Collection getStemPrivsWithLabels(ResourceBundle bundle) {
		List<Map<String,String>> privs = new ArrayList<Map<String,String>>();
		
		String displayName=null;
		for(int i=0;i<stemPrivs.length;i++){
			Map priv = new HashMap();
			displayName=stemPrivs[i];
			try {
				displayName=bundle.getString("priv." + stemPrivs[i]);
			}catch(MissingResourceException mre){}
			priv.put("value", stemPrivs[i]);
			priv.put("label", displayName);
			privs.add(priv);
		}
		return privs;
	}
	
	 /**
   * Return a Collection of all access privileges
   * @param bundle ResourceBundle to lookup display name
   * @return Collection of Maps of privilege names and display names
   */
  public static Collection getGroupPrivsWithLabels(ResourceBundle bundle) {
    List<Map<String,String>> privs = new ArrayList<Map<String,String>>();
    
    String displayName=null;
    for(int i=0;i<groupPrivs.length;i++){
      Map priv = new HashMap();
      displayName=groupPrivs[i];
      try {
        displayName=bundle.getString("priv." + groupPrivs[i]);
      }catch(MissingResourceException mre){}
      priv.put("value", groupPrivs[i]);
      priv.put("label", displayName);
      privs.add(priv);
    }
    return privs;
  }

	
	/**
	 * Given a simple query and scoping stem search for matching groups and return as List
	 * @param s GrouperSession for authenticated user
	 * @param query to search for
	 * @param from stem which scopes search
	 * @param attr name of attribute to search
	 * @return List of groups matched
	 */
	public static List searchGroupsByAttribute(GrouperSession s, String query, String from,String attr) throws QueryException,StemNotFoundException{

		GrouperQuery q = GrouperQuery.createQuery(s,new GroupAttributeFilter(attr,query,StemFinder.findByName(s,from, true)));
		Set res = q.getGroups();
		return new ArrayList(res);
		
	}
	
	
	/**
	 * Given a simple query and scoping stem search for matching groups and return as List
	 * @param s GrouperSession for authenticated user
	 * @param query to search for
	 * @param from stem which scopes search
	 * @param searchInDisplayNameOrExtension name=displayName / extemsion=displayExtension
	 * @param searchInNameOrExtension name=name / extemsion=extension
	 * @return List of groups matched
	 */
	public static List searchGroups(GrouperSession s, String query, String from,String searchInDisplayNameOrExtension,String searchInNameOrExtension) 
	throws StemNotFoundException,QueryException{

		if(searchInDisplayNameOrExtension==null && searchInNameOrExtension==null) {
			GrouperQuery q = GrouperQuery.createQuery(s,new GroupAnyAttributeFilter(query,StemFinder.findByName(s,from)));
			Set res = q.getGroups();
			return new ArrayList(res);
		}
		
		List displayResults = null;
		List nonDisplayResults=null; 
		String attr = null;
		if(!"".equals(searchInDisplayNameOrExtension)) {
			if("name".equals(searchInDisplayNameOrExtension)) {
				attr="displayName";
			}else{
				attr="displayExtension";
			}
			displayResults = searchGroupsByAttribute(s,query,from,attr);	
		}
		if(!"".equals(searchInNameOrExtension)) {
			if("name".equals(searchInNameOrExtension)) {
				attr="name";
			}else{
				attr="extension";
			}
			nonDisplayResults = searchGroupsByAttribute(s,query,from,attr);	
		}
		if(displayResults==null && nonDisplayResults==null) return new ArrayList();
		if(displayResults==null && nonDisplayResults!=null) return nonDisplayResults;
		if(displayResults!=null && nonDisplayResults==null) return displayResults;
		Object obj;
		for(int i=0;i<nonDisplayResults.size();i++) {
			obj = nonDisplayResults.get(i);
			if(!displayResults.contains(obj)) displayResults.add(obj);
		}
			
		return displayResults;
		
	}

  /**
   * Given simple query, scoping stem and ui browseMode return list of
   * matching groups, pruned to give results relevant to browseMode.<p/>
   * The browseMode filtering needs to be factored into a new Class with interface
   * so that new browse modes can be added easily
   * 
   * @param s GrouperSession for authenticated user
   * @param query to search for
   * @param from Stem which scopes search
   * @param searchInDisplayNameOrExtension name=displayName / extension=displayExtension
   * @param searchInNameOrExtension name=name / extension=extension
   * @param browseMode UI browse mode to filter results by
   * @return List of GrouperGroups matched
   */
  public static List<Group> searchGroups(GrouperSession s, String query,
      String from, String searchInDisplayNameOrExtension,
      String searchInNameOrExtension,String browseMode) throws Exception{
    List<Group> groups = searchGroupsHelper(s, query, from, searchInDisplayNameOrExtension, searchInNameOrExtension, browseMode);
    Group.initGroupAttributes(groups);
    return groups;
  }
	
	/**
	 * Given simple query, scoping stem and ui browseMode return list of
	 * matching groups, pruned to give results relevant to browseMode.<p/>
	 * The browseMode filtering needs to be factored into a new Class with interface
	 * so that new browse modes can be added easily
	 * 
	 * @param s GrouperSession for authenticated user
	 * @param query to search for
	 * @param from Stem which scopes search
	 * @param searchInDisplayNameOrExtension name=displayName / extension=displayExtension
	 * @param searchInNameOrExtension name=name / extension=extension
	 * @param browseMode UI browse mode to filter results by
	 * @return List of GrouperGroups matched
	 */
	private static List<Group> searchGroupsHelper(GrouperSession s, String query,
			String from, String searchInDisplayNameOrExtension,
			String searchInNameOrExtension,String browseMode) throws Exception{
		String type = null;
		List res = searchGroups(s, query, from,searchInDisplayNameOrExtension,searchInNameOrExtension);
		if (res != null)
			return res;
		List returnRes = new ArrayList();
		Set allowedSet = null;
		if ("All".equals(browseMode)) {
			return res;
		} else if ("".equals(browseMode)) {
			allowedSet = GrouperHelper.getMembershipsSet(s);
		} else if ("Manage".equals(browseMode)) {
			allowedSet = GrouperHelper.getGroupsForPrivileges(s,
					new String[] { "admin", "update",
							"read" });
		} else if ("Join".equals(browseMode)) {
			allowedSet = GrouperHelper.getGroupsForPrivileges(s,
					new String[] { "optin" });
		}
		if (allowedSet != null) {
			Map allowed = new HashMap();
			Iterator it = allowedSet.iterator();
			Group group;
			while (it.hasNext()) {
				group = (Group) it.next();
				allowed.put(group.getUuid(),Boolean.TRUE);
			}
			
			for (int i = 0; i < res.size(); i++) {
				group = (Group) res.get(i);
				if (allowed.containsKey(group.getUuid()))
					returnRes.add(group);
			}
		}
		return returnRes;
	}

	/*
	 * public static List getNestedStemChildren(GrouperSession s,String stem) {
	 * return Group.getNestedChildren(s,stem); }
	 */

	/**
	 * Given a simple query and scoping stem and attribute, search for matching stems and return as List
	 * @param s GrouperSession for authenticated user
	 * @param query to search for
	 * @param from stem which scopes search
	 * @param attr name of attribute to search
	 * @return List of stems matched
	 */
	public static List searchStemsByAttribute(GrouperSession s, String query, String from,String attr) {
			
			return new ArrayList();
			/*String type = null;

			GrouperQuery grouperQuery = new GrouperQuery(s);
			

			if (from == null && !grouperQuery.stemAttr(attr,query)) {
				List empty = new ArrayList();
				return empty;
			}
			if (from != null && !grouperQuery.stemAttr(from,attr,query)) {
				List empty = new ArrayList();
				return empty;
			}

			List res = grouperQuery.getStems();
			return res;*/
		
	}
	
	
	/**
	 * Given a simple query and scoping stem search for matching stems and return as List
	 * @param s GrouperSession for authenticated user
	 * @param query to search for
	 * @param from stem which scopes search
	 * @param searchInDisplayNameOrExtension name=displayName / extemsion=displayExtension
	 * @param searchInNameOrExtension name=name / extemsion=extension
	 * @return List of stems matched
	 */
	public static List searchStems(GrouperSession s, String query, String from,String searchInDisplayNameOrExtension,String searchInNameOrExtension) throws StemNotFoundException,QueryException{
		GrouperQuery q = GrouperQuery.createQuery(s,new StemNameAnyFilter(query,StemFinder.findByName(s,from)));
		Set res = q.getStems();
		List displayResults = new ArrayList(res);
		/*List nonDisplayResults=null; 
		String attr = null;
		if(!"".equals(searchInDisplayNameOrExtension)) {
			if("name".equals(searchInDisplayNameOrExtension)) {
				attr="displayName";
			}else{
				attr="displayExtension";
			}
			displayResults = searchStemsByAttribute(s,query,from,attr);	
		}
		if(!"".equals(searchInNameOrExtension)) {
			if("name".equals(searchInNameOrExtension)) {
				attr="name";
			}else{
				attr="extension";
			}
			nonDisplayResults = searchStemsByAttribute(s,query,from,attr);	
		}
		if(displayResults==null && nonDisplayResults==null) return new ArrayList();
		if(displayResults==null && nonDisplayResults!=null) return nonDisplayResults;
		if(displayResults!=null && nonDisplayResults==null) return displayResults;
		Object obj;
		for(int i=0;i<nonDisplayResults.size();i++) {
			obj = nonDisplayResults.get(i);
			if(!displayResults.contains(obj)) displayResults.add(obj);
		}
		*/
		return displayResults;
		
		
	}

	/**
	 * Given a GrouperSession s, determine all the GrouperGroups s.subject()
	 * is a member of and return a Set of GrouperGroups
	 * 
	 * @param s GrouperSession for authenticated user
	 * @return Set of GrouperGroups
	 */
	public static Set<Group> getMembershipsSet(GrouperSession s) {
		return getMembershipsSet(s, 0, 100000, null);

	}
	
	
	/**
	 * Given a GrouperSession s, determine all the GrouperGroups s.subject()
	 * is a member of and return a Map where the keys are GrouperGroup keys
	 * 
	 * @param s GrouperSession for authenticated user
	 * @return Map where keys are GrouperGroup keys
	 */
	public static Map getMemberships(GrouperSession s) {
		Map memberships = null;
							   

		memberships = new HashMap();
		Member member = null;
		try {
			member = MemberFinder.findBySubject(s, s.getSubject());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		Set vals = member.getGroups();
		Group group;
		Iterator it = vals.iterator();
		while(it.hasNext()){
			
			group = (Group) it.next();
			memberships.put(group.getUuid(), Boolean.TRUE);
		}

		return memberships;
	}
	
	
	/**
	 * Given a GrouperSession s, return a subset of Groups where
	 * s.subject is a member, determined by start and pageSize.<p/>
	 * totalCount is a dubious means to return a second value to allow the UI
	 * to page results
	 * 
	 * @param s GrouperSession for authenticated user
	 * @param start where subset begins
	 * @param pageSize no of groups to return
	 * @param totalCount number of overall results
	 * @return Set - subset of groups
	 */
	public static Set<Group> getMembershipsSet(GrouperSession s, int start,
			int pageSize, StringBuffer totalCount) {
		Set memberships = new LinkedHashSet();
		Member member = null;
		try {
			member = MemberFinder.findBySubject(s, s.getSubject());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		Set vals = member.getGroups();
		
		Group group;
		int end = start + pageSize;
		if (end > vals.size())
			end = vals.size();
		if (totalCount != null) {
			totalCount.setLength(0);
			totalCount.append("" + vals.size());
		}
		
		Iterator it = vals.iterator();
		while(it.hasNext()){
			
			group = (Group) it.next();
			memberships.add(group);
		}
		return memberships;
	}

	/**
	 * Given a GrouperSession s, and an array of privileges, get all groups
	 * where s.subject() has atleast one of the privileges
	 * 
	 * @param s GrouperSession for authenticated user
	 * @param privs privileges to test
	 * @return Set of Grouper Groups 
	 */
	public static Set<Group> getGroupsForPrivileges(GrouperSession s, String[] privs) throws MemberNotFoundException{

		Set<Group> groups = getGroupsForPrivileges(s, privs, 0, 100000, null);
		
		return groups;
	}

		
	/**
	 * Given a GrouperSession s, and an array of privileges, get subset of groups
	 * where s.subject() has atleast one of the privileges, determined by start and pageSize<p/>
	 * resultCount is a dubious means to return a second value to allow the UI
	 * to page results
	 * 
	 * @param s GrouperSession for authenticated user
	 * @param privs privileges to test
	 * @param start of subset
	 * @param pageSize number of GrouperGroups to return
	 * @param resultCount overall number of GrouperGroups
	 * @return Set - subset of GrouperGroups
	 */
	public static Set<Group> getGroupsForPrivileges(GrouperSession s, String[] privs,
			int start, int pageSize, StringBuffer resultCount) throws MemberNotFoundException{
		
		Set groupSet = new LinkedHashSet();
		
		Set allSet = new LinkedHashSet();
		
		
		Member member = MemberFinder.findBySubject(s,s.getSubject());
		for (int i = 0; i < privs.length; i++) {
			allSet.addAll(getGroupsWhereMemberHasPriv(member,privs[i]));
		}

		int end = start + pageSize;
		if (end > allSet.size())
			end = allSet.size();
		if (resultCount != null) {
			resultCount.setLength(0);
			resultCount.append("" + allSet.size());
		}
		
		Iterator it = allSet.iterator();
		Group group = null;
		Object obj;
		while(it.hasNext()){
				obj = it.next();
				if(obj instanceof Group) {
				  groupSet.add(obj);
				}
		}
		return groupSet;
	}

	/**
	 * Given a GrouperSession s, and an array of privileges, get all stems
	 * where s.subject() has atleast one of the privileges
	 * 
	 * @param s GrouperSession for authenticated user
	 * @param privs privileges to test
	 * @return Set of GrouperStems
	 */
	public static Set getStemsForPrivileges(GrouperSession s, String[] privs) throws MemberNotFoundException{

		Set groups = null;//Cache.instance().getSet(s,sb.toString());
		if (groups != null)
			return null;

		groups = getStemsForPrivileges(s, privs, 0, 100000, null);
		//Cache.instance().put(s,sb.toString(),groups);
		return groups;

	}

	/**
	 * Given a GrouperSession s, and an array of privileges, get subset of stems
	 * where s.subject() has atleast one of the privileges, determined by start and pageSize<p/>
	 * resultCount is a dubious means to return a second value to allow the UI
	 * to page results
	 * 
	 * @param s GrouperSession for authenticated user
	 * @param privs privileges to test
	 * @param start where subset begins
	 * @param pageSize number of GrouperStems to return
	 * @param resultCount overall number of GrouperStems
	 * @return Set of stems where session subject has one or more of Naming privileges specified by privs
	 */
	public static Set getStemsForPrivileges(GrouperSession s, String[] privs,
			int start, int pageSize, StringBuffer resultCount) throws MemberNotFoundException{

		
		Set stemSet = new LinkedHashSet();
		Set allSet = new LinkedHashSet();
		
		Member member = MemberFinder.findBySubject(s,s.getSubject());
		for (int i = 0; i < privs.length; i++) {
			allSet.addAll(getGroupsOrStemsWhereMemberHasPriv(member,privs[i]));
		}
		int end = start + pageSize;
		if (end > allSet.size())
			end = allSet.size();
		if (resultCount != null) {
			resultCount.setLength(0);
			resultCount.append("" + allSet.size());
		}
		
		Iterator it = allSet.iterator();
		Stem stem = null;
		while(it.hasNext()){
			stem = (Stem) it.next();
			stemSet.add(stem);
		}
		return stemSet;
	}
	
	
	/**
	 * Given a GrouperStem delete it and any children
	 * TODO remove - redundant
	 * @param s GrouperSession for authenticated user
	 * @param stem GrouperStem to delete
	 * @return boolean indicating success
	 * @throws Exception
	 */
	private static boolean stemDelete(GrouperSession s, Stem stem)
			throws Exception {
		if (stem == null || !stem.hasStem(s.getSubject())) {
			return false;
		}
		String stemStr = stem.getName() + HIER_DELIM;
		//@TODO: when searching scoped by stem fix
		List children = new ArrayList();//getNestedStemChildren(s,stemStr);
		if (children.size() > 100)
			throw new Exception("Too many children (" + children.size()
					+ ") - must be <=100");
		Object[] res;
		Group g = null;
		boolean deleted = true;
		GrouperSession sysSession = null;
		try {
			sysSession = GrouperSession.start(SubjectFinder.findById("GrouperSystem"));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		for (int i = 0; i < children.size(); i++) {
			res = (Object[]) children.get(i);
			g = (Group) res[1];
			if (!groupDelete(sysSession, GroupOrStem.findByGroup(s,g))) {
				sysSession.stop();
				return false;
			}
		}

		sysSession.stop();
		return groupDelete(s, GroupOrStem.findByStem(s,stem));
	}
	
	/**
	 * Given a GrouperGroup or GroupeStem delete it. GroupeStem must not have any children.
	 * 
	 * @param s GrouperSession for authenticated user
	 * @param groupOrStem GrouperGroup or GrouperStem to delete
	 * @return boolean indicating success
	 */
	public static boolean groupDelete(GrouperSession s, GroupOrStem groupOrStem) 
		throws InsufficientPrivilegeException,MemberNotFoundException,
		SubjectNotFoundException,MemberDeleteException,SessionException{
		Group group = groupOrStem.getGroup();
		Stem stem = groupOrStem.getStem();
		boolean deleted = true;
		if (groupOrStem == null)
			return false;
		if (groupOrStem.isStem()) {
			if(!stem.hasStem(s.getSubject())) {

				return false;
			}
			try {
				//Stem.delete(s, stem);
			} catch (Exception e) {
				deleted = false;
			}
			return deleted;
		} else {
			
			if (!group.hasAdmin(s.getSubject())) {
				return false;
			}
		}

		Set memberships = group.getMemberships();//Eff?
		Member member;
		Iterator it = memberships.iterator();
		while(it.hasNext()) {
			member = (Member) it.next();
			try {
				group.deleteMember(member.getSubject());
				//group.listDelVal(member);
			} catch (RuntimeException e) {
				if (!"List value does not exist".equals(e.getMessage()))
					throw e;
			}
		}

		/*
		 * members = group.listVals();
		 * 
		 * for(int i=0;i <members.size();i++) { member = ((GrouperList)
		 * members.get(i)).member(); try { group.listDelVal(member);
		 * }catch(RuntimeException e) { if(!"List value does not
		 * exist".equals(e.getMessage())) throw e; }
		 *  }
		 */
		GrouperSession sysSession = null;
		try {
			sysSession = GrouperSession.start(SubjectFinder.findById("GrouperSystem"));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		Member groupAsMember = null;
		try {
			groupAsMember = MemberFinder.findBySubject(s, SubjectFinder.findById(group.getUuid(), "group"));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		/*for (int j = 0; j < listNames.length; j++) {
			String list = listNames[j];
			memberships = groupAsMember.;
			GrouperGroup memberOf;
			for (int i = 0; i < memberships.size(); i++) {
				memberOf = (GrouperGroup) ((GrouperList) memberships.get(i))
						.group();
				//memberOf =
				// (GrouperGroup)GrouperGroup.loadByKey(s,memberOf.key());
				String type = memberOf.type();
				memberOf.listDelVal(groupAsMember, list);
			}
		}*/
		sysSession.stop();

		try {
			//Group.delete(s, group);
		} catch (Exception e) {
			deleted = false;
		}
		return deleted;
	}

	/**
	 * Given a PersonalStem create stem in correct location - depends on
	 * configuration. PersonalStem is an Interface and new implemetations may be
	 * 'plugged' into the UI
	 * 
	 * @param s GrouperSession for authenticated user
	 * @param ps PersonalStem
	 * @return GrouperStem created
	 * @throws Exception
	 */
	public static Stem createIfAbsentPersonalStem(GrouperSession s,
			PersonalStem ps) throws Exception {
		if (s == null)
			return null;
		GrouperSession sysSession = null;
		try {
			sysSession = GrouperSession.start(SubjectFinder.findById("GrouperSystem"));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		String stemName = ps.getPersonalStemId(s.getSubject());
		String parentName=ps.getPersonalStemRoot(s.getSubject());
		Stem parent = null;
		String childName = null;
		if(NS_ROOT.equals(parentName)||"".equals(parentName)) {
			parent = StemFinder.findRootStem(sysSession);
			childName=stemName;
		}else{
			try{
				childName=parentName + HIER_DELIM + stemName;
				parent = StemFinder.findByName(sysSession,parentName);
				
			}catch(StemNotFoundException e){
				throw new IllegalStateException("Cannot find parent stem for personal stem: " + childName);
			}
		}
		
		Stem stem = null;
		
		try{
			stem=StemFinder.findByName(sysSession, childName);
		}catch(Exception e){}
		
		if (stem == null) {
			
			stem = parent.addChildStem(stemName,ps.getPersonalStemDisplayName(s
					.getSubject()));
			
			stem.setDescription(ps.getPersonalStemDescription(s
					.getSubject()));
			stem.store();

			stem.grantPriv(s.getSubject(),Privilege.getInstance("create"));
			stem.grantPriv(s.getSubject(),Privilege.getInstance("stem"));
			sysSession.stop();
		}
		return stem;
	}
	
	/**
	 * Query Subject API for sources and determine which ones return 'people'
	 * @return List of Sources
	 * @throws Exception
	 */
	public static synchronized List getPersonSources() throws Exception {
		if (personSources != null)
			return personSources;
		personSources = new ArrayList();
		SourceManager sm = SourceManager.getInstance();
		Collection c = sm.getSources();
		Iterator it = c.iterator();
		Source source = null;
		Iterator typesIterator = null;
		String subjectType;
		while (it.hasNext()) {
			source = (Source) it.next();
			Set subjectTypes = source.getSubjectTypes();
			typesIterator = subjectTypes.iterator();
			while (typesIterator.hasNext()) {
				subjectType = (String) ((SubjectType) typesIterator.next())
						.getName();
				if (subjectType.equals("person"))
					personSources.add(source);
			}
		}
		return personSources;
	}
	
	/**
	 * API lets you get all memberships - this filters them for a specific group
	 * @param s
	 * @param subject
	 * @param group
	 * @return List with one item for each different way subject is a member of the specified list for the specified group
	 * @throws MemberNotFoundException
	 * @throws GroupNotFoundException
	 * @throws SchemaException
	 * @throws CompositeNotFoundException
	 */
	
	public static List getAllWaysInWhichSubjectIsMemberOFGroup(GrouperSession s,Subject subject,Group group,Field field) 
		throws MemberNotFoundException,GroupNotFoundException,SchemaException,CompositeNotFoundException{
		List ways = new ArrayList();
		Member member = MemberFinder.findBySubject(s,subject);
		Set memberships = member.getMemberships(field);
		Membership gl;
		Iterator it = memberships.iterator();
		while(it.hasNext()) {
			gl = (Membership) it.next();
			//gl.setSession(s);
			if(gl.getGroup().getUuid().equals(group.getUuid())) {
				ways.add(gl);
				/*if(group.hasComposite() && field.getName().equals("members")) {
					fixCompositeMembership(s,gl,subject,group,field);
				}*/
			}
		}
		return ways;
	}
	
	/*public static void fixCompositeMembership(GrouperSession s,Membership m,Subject subject,Group group,Field field) {
		
			String x="";
			
			if(group.hasComposite() && field.getName().equals("members")) {
				List compositeWays = new ArrayList();
				Composite comp = CompositeFinder.isOwner(group);
				
				compositeWays.addAll(getAllWaysInWhichSubjectIsMemberOFGroup(s,subject,comp.getLeftGroup(),field));
				compositeWays.addAll(getAllWaysInWhichSubjectIsMemberOFGroup(s,subject,comp.getRightGroup(),field));
				Membership pm = null;
				try {
					pm=MembershipFinder.findImmediateMembership(s,group,subject,field);
				}catch (MembershipNotFoundException e) {
					return compositeWays;
				}
				Membership m;
				for(int i=0;i<compositeWays.size();i++) {
					m=null;
					m = (Membership)compositeWays.get(i);
					try {
						if(m.getVia()==null) {
							m.via_id=comp;
						}
					}catch(OwnerNotFoundException e) {
						m.via_id=group;
					}
					
					try {
						if(m.getParentMembership()==null) {
							//m.parent_membership=pm;
						}
					}catch(MembershipNotFoundException e) {
						//m.parent_membership=pm;
					}
				}
				return compositeWays;
			
		
	}}*/
	
	/**
	 * Returns indirect privileges for member on the group or stem
	 * @param s
	 * @param groupOrStem
	 * @param member
	 * @return Map keyed on privilege name forindirect privileges for member on the group or stem, and how derived
	 */
	public static Map getExtendedHas(GrouperSession s,GroupOrStem groupOrStem,Member member) throws SchemaException{
		return getExtendedHas(s,groupOrStem,member,FieldFinder.find("members"));
	}
	
	/**
	 * Returns indirect privileges for member on the group or stem
	 * @param s
	 * @param groupOrStem
	 * @param member
	 * @return Map keyed on privilege name for indirect privileges for member on the group or stem, and how derived
	 */
	public static Map getExtendedHas(GrouperSession s,GroupOrStem groupOrStem,Member member,Field field) throws SchemaException{
		Map map  =getAllHas(s,groupOrStem,member,field);
		
		map.remove("subject");
		map.remove("effectivePrivs");
		return map;
		
	}
	
	/**
	 * Returns indirect privileges for member on the group or stem - but not how derived
	 * @param s
	 * @param groupOrStem
	 * @param member
	 * @return Map keyed on name of privs which are indirectly assigned
	 */
	public static Map getEffectiveHas(GrouperSession s,GroupOrStem groupOrStem,Member member,Field field) throws SchemaException{
		Map map  =getAllHas(s,groupOrStem,member,field);

		return (Map)map.get("effectivePrivs");
		
	}
	
	/**
	 * Returns direct privileges for member on group or stem
	 * @param s
	 * @param groupOrStem
	 * @param member
	 * @return Map keyed on name of privs which are directly assigned
	 */
	public static Map getImmediateHas(GrouperSession s,GroupOrStem groupOrStem,Member member) throws SchemaException{
		Map map = getAllHas(s,groupOrStem,member);
		
		return (Map)map.get("subject");
		
	}
	
	/**
	 * Returns direct privileges for member on group or stem
	 * @param s
	 * @param groupOrStem
	 * @param member
	 * @param field
	 * @return Map keyed on name of privs which are directly assigned
	 * @throws SchemaException
	 */
	public static Map getImmediateHas(GrouperSession s,GroupOrStem groupOrStem,Member member,Field field) throws SchemaException{
		Map map = getAllHas(s,groupOrStem,member,field);
		
		return (Map)map.get("subject");
		
	}
	
	/**
	 * Returns all privileges, direct and indirect, that member has for group or stem
	 * @param s
	 * @param groupOrStem
	 * @param member
	 * @return Map keyed on privilege names - whether direct or indirect
	 * @throws SchemaException
	 */
	
	public static Map getAllHas(GrouperSession s,GroupOrStem groupOrStem,Member member) throws SchemaException{
		return getAllHas(s,groupOrStem,member,FieldFinder.find("members"));
	}
	
	/**
	 * Returns all privileges, direct and indirect, that member has for group or stem
	 * @param s
	 * @param groupOrStem
	 * @param member
	 * @return Map keyed on privilege names - whether direct or indirect
	 * @throws SchemaException
	 */
	public static Map getAllHas(GrouperSession s,GroupOrStem groupOrStem,Member member,Field field) throws SchemaException{
		Set allPrivs = null;
		Map effectivePrivs = new HashMap();
		Map effectiveMemberships = new HashMap();
		if(groupOrStem.isGroup()) {
			allPrivs = member.getPrivs(groupOrStem.getGroup());
			try {
				effectiveMemberships = getEffectiveMembershipsForGroupAndSubject(s,groupOrStem.getGroup(),member.getSubject(),field);
			}catch(Exception e){}
		}else{
			allPrivs = member.getPrivs(groupOrStem.getStem());
		}
		 
		Map results = new LinkedHashMap();
		Map privs;
		AccessPrivilege priv = null;
		NamingPrivilege nPriv=null;
		String key="subject";
		results.put(key,new HashMap());
		List tmpList = new ArrayList();
		Iterator it = allPrivs.iterator();
		boolean isEffective = false;
		if(groupOrStem.isGroup() && member.isImmediateMember(groupOrStem.getGroup(),field)) {
			privs = new HashMap();
			privs.put("member",Boolean.TRUE);
			results.put("subject",privs);
		}
		
		while(it.hasNext()) {
			if(groupOrStem.isGroup()) {
				priv = (AccessPrivilege)it.next();
				
				if(member.getSubjectId().equals(priv.getOwner().getId())) {
					key="subject";
					isEffective = false;
				}else{
					key=priv.getOwner().getId();
					isEffective = true;
				}
				privs = (Map)results.get(key);
				if(privs==null) {
					privs=new HashMap();
					results.put(key,privs);
				}
					if(isEffective) {
						try{
							privs.put("group",group2Map(s,GroupFinder.findByUuid(s,priv.getOwner().getId())));
							privs.put(priv.getName(),Boolean.TRUE);
							if(effectiveMemberships.containsKey(priv.getOwner())) {
								privs.put("member",Boolean.TRUE);
								effectiveMemberships.remove(priv.getOwner());
							}
						}catch(GroupNotFoundException e){}
						effectivePrivs.put(priv.getName(),Boolean.TRUE);
					}
					
				
				privs.put(priv.getName()
						,Boolean.TRUE);
			}else{
				nPriv = (NamingPrivilege)it.next();
				
				if(member.getSubjectId().equals(nPriv.getOwner().getId())) {
					key="subject";
					isEffective = false;
				}else{
					key=nPriv.getOwner().getId();
					isEffective = true;
				}
					privs = (Map)results.get(key);
					if(privs==null) {
						privs=new HashMap();
						results.put(key,privs);
					}
						if(isEffective) {
							try{
								if(effectiveMemberships.containsKey(nPriv.getOwner())) {
									privs.put("member",Boolean.TRUE);
									effectiveMemberships.remove(nPriv.getOwner());
								}
								privs.put("group",group2Map(s,GroupFinder.findByUuid(s,nPriv.getOwner().getId())));
							}catch(GroupNotFoundException e){}
							effectivePrivs.put(nPriv.getName(),Boolean.TRUE);
						}
						
					
					privs.put(nPriv.getName(),Boolean.TRUE);
			}
		}
		Map.Entry entry;
		it = effectiveMemberships.entrySet().iterator();
		Group effGroup;
		while(it.hasNext()) {
			entry = (Map.Entry)it.next();
			effGroup = (Group)entry.getKey();
			privs=(Map)results.get(effGroup.getUuid());
			if(privs==null) {
				privs=new HashMap();
				privs.put("group",group2Map(s,effGroup));
				privs.put("member",Boolean.TRUE);
				results.put(effGroup.getUuid(),privs);
			}
			
		}
		results.put("effectivePrivs",effectivePrivs);
		return results;
		
	}
	
	/**
	 * Given a Group and Subject return the effective memberships keyed on
	 * the via group
	 * @param s
	 * @param group
	 * @param subject
	 * @param field
	 * @return Map keyed on via groups
	 * @throws Exception
	 */
	public static Map getEffectiveMembershipsForGroupAndSubject(GrouperSession s,Group group,Subject subject,Field field) throws Exception{
		Member member = MemberFinder.findBySubject(s,subject);
		Map res = new HashMap();
		Set memberships = member.getEffectiveMemberships(field);
		Membership m;
		Iterator it = memberships.iterator();
		while(it.hasNext()){
			m = (Membership)it.next();
			if(m.getGroup().equals(group)) res.put(m.getViaGroup(),m);
		}
		
		return res;
	}
	
	/**
	 * Given priv name return subjects with that privilege for group
	 * @param group
	 * @param privilege
	 * @return Set of subjects with specified privilege for specified group
	 */
	public static Set getSubjectsWithPriv(Group group,String privilege) {
		if(privilege.equals("admin")) return group.getAdmins();
		if(privilege.equals("update")) return group.getUpdaters();
		if(privilege.equals("read")) return group.getReaders();
		if(privilege.equals("view")) return group.getViewers();
		if(privilege.equals("optin")) return group.getOptins();
    if(privilege.equals("optout")) return group.getOptouts();
    if(privilege.equals("groupAttrRead")) return group.getGroupAttrReaders();
    if(privilege.equals("groupAttrUpdate")) return group.getGroupAttrUpdaters();
		return new HashSet();
	}
	
	/**
	 * Given a privilege return all the groups or stems where member has that privilege
	 * @param member
	 * @param privilege
	 * @return Set of groups where specified member has the specified privilege
	 */
	public static Set getGroupsOrStemsWhereMemberHasPriv(Member member,String privilege) {
		if(privilege.equals("admin")) return member.hasAdmin();
		if(privilege.equals("update")) return member.hasUpdate();
		if(privilege.equals("read")) return member.hasRead();
		if(privilege.equals("view")) return member.hasView();
		if(privilege.equals("optin")) return member.hasOptin();
		if(privilege.equals("optout")) return member.hasOptout();
    if(privilege.equals("groupAttrRead")) return member.hasGroupAttrRead();
    if(privilege.equals("groupAttrUpdate")) return member.hasGroupAttrUpdate();
		if(privilege.equals("create")) return member.hasCreate();
		if(privilege.equals("stem")) return member.hasStem();
    if(privilege.equals("stemAttrRead")) return member.hasStemAttrRead();
    if(privilege.equals("stemAttrUpdate")) return member.hasStemAttrUpdate();
		return new HashSet();
	}
	
	/**
	 * Given a privilege return all the groups where member has that privilege
	 * @param member
	 * @param privilege
	 * @return Set of groups where specified member has the specified privilege
	 */
	public static Set getGroupsWhereMemberHasPriv(Member member,String privilege) {
		if(privilege.equals("admin")) return member.hasAdmin();
		if(privilege.equals("update")) return member.hasUpdate();
		if(privilege.equals("read")) return member.hasRead();
		if(privilege.equals("view")) return member.hasView();
		if(privilege.equals("optin")) return member.hasOptin();
		if(privilege.equals("optout")) return member.hasOptout();
    if(privilege.equals("groupAttrRead")) return member.hasGroupAttrRead();
    if(privilege.equals("groupAttrUpdate")) return member.hasGroupAttrUpdate();
		return new HashSet();
	}
	
	/**
	 * Given a privilege return all the stems where member has that privilege
	 * @param member
	 * @param privilege
	 * @return Set of groups where specified member has the specified privilege
	 */
	public static Set getStemsWhereMemberHasPriv(Member member,String privilege) {

		if(privilege.equals("create")) return member.hasCreate();
		if(privilege.equals("stem")) return member.hasStem();
    if(privilege.equals("stemAttrRead")) return member.hasStemAttrRead();
    if(privilege.equals("stemAttrUpdate")) return member.hasStemAttrUpdate();
		return new HashSet();
	}
	
	/**
	 * Given priv name return subjects with that privilege for stem
	 * @param stem
	 * @param privilege
	 * @return Set of subjects with a specified Nmaing privilege for a specified stem
	 */
	public static Set getSubjectsWithPriv(Stem stem,String privilege) {
		if(privilege.equals("stem")) return stem.getStemmers();
		if(privilege.equals("create")) return stem.getCreators();
    if(privilege.equals("stemAttrRead")) return stem.getStemAttrReaders();
    if(privilege.equals("stemAttrUpdate")) return stem.getStemAttrUpdaters();
		return new HashSet();
	}
	
	/**
	 * Determine if a given subject has been granted an Access privilege through direct assignment
	 * @param s
	 * @param subject
	 * @param group
	 * @param privilege
	 * @return whether the specified subject has a specified privilege directly assigned for a specified group
	 * @throws MemberNotFoundException
	 */
	public static boolean hasSubjectImmPrivForGroup(GrouperSession s,Subject subject,Group group,String privilege) throws MemberNotFoundException,SchemaException{
		Map privs = getImmediateHas(s,GroupOrStem.findByGroup(s,group),MemberFinder.findBySubject(s,subject));
		return privs.containsKey(privilege);
	}
	
	/**
	 * Determine if a given subject has been granted a Naming privilege through direct assignment
	 * @param s
	 * @param subject
	 * @param stem
	 * @param privilege
	 * @return whether the specified subject has a specified privilege directly assigned for a specified stem
	 * @throws MemberNotFoundException
	 */
	public static boolean hasSubjectImmPrivForStem(GrouperSession s,Subject subject,Stem stem,String privilege) throws MemberNotFoundException,SchemaException{
		Map privs = getImmediateHas(s,GroupOrStem.findByStem(s,stem),MemberFinder.findBySubject(s,subject));
		return privs.containsKey(privilege);
	}
	
	/**
	 * Return the path by which this Membership is derived
	 * @param s
	 * @param m
	 * @return List where each element represents a link in the chain of the membership
	 * @throws GroupNotFoundException
	 * @throws MembershipNotFoundException
	 * @throws MemberNotFoundException
	 * @throws SchemaException
	 */

	public static List getChain(GrouperSession s,Membership m)throws GroupNotFoundException,MembershipNotFoundException,
	MemberNotFoundException,SchemaException,SubjectNotFoundException{
		List chain = new ArrayList();
		Group via = null;
		Composite comp=null;
		Membership pm=null;
		try {
			via = m.getViaGroup();
		}catch(GroupNotFoundException e) {
			//Should be an immediate member, but check for composite
			return chain;
		}
		try {
			comp = CompositeFinder.findAsOwner(via);
		}catch(Exception me) {
			
		}
		Map groupMap=null;
		if(comp==null) {
			groupMap=GrouperHelper.group2Map(s,via);
		}else{
			groupMap=GrouperHelper.getCompositeMap(s,comp);
		}
		Map viaMap = groupMap;
		groupMap.put("listField","members");
		chain.add(groupMap);
		pm = null;
		Group g=null;
		try {
			pm=m.getParentMembership();
		}catch(MembershipNotFoundException e) {
			
		}
		
		while(pm!=null) {
			if(!pm.getMember().getSubjectId().equals(via.getUuid())) {
				g = GroupFinder.findByUuid(s,pm.getMember().getSubjectId());
				groupMap=GrouperHelper.group2Map(s,g);
				groupMap.put("listField",pm.getList().getName());
				chain.add(groupMap);	
			}else{
				viaMap.put("listField",pm.getList().getName());
			}
			try {
			pm=pm.getParentMembership();
			}catch(MembershipNotFoundException e){break;}
		}
		return chain;
	}
	
	
	/**
	 * 	Given a composite return a Map for use in Tiles
	 * @param grouperSession
	 * @param comp
	 * @return a Map representing a Composite, for use in Tiles / JSTL
	 * @throws GroupNotFoundException
	 * @throws MemberNotFoundException
	 * @throws SchemaException
	 */

	public static Map getCompositeMap(GrouperSession grouperSession,Composite comp)
		throws GroupNotFoundException,MemberNotFoundException,SchemaException,SubjectNotFoundException{
		return getCompositeMap(grouperSession,comp,null);
	
	}
	
	/**
	 * Given a composite return a Map for use in Tiles. If a Subject is passed
	 * then the number of ways the Subject is a member of the left / right groups 
	 * is calculated
	 * @param grouperSession
	 * @param comp
	 * @param subj
	 * @return a Map representing the Composite and Membership infor for specified subject
	 * @throws GroupNotFoundException
	 * @throws MemberNotFoundException
	 * @throws SchemaException
	 */

	public static Map getCompositeMap(GrouperSession grouperSession,Composite comp,Subject subj)
		throws GroupNotFoundException,MemberNotFoundException,SchemaException,SubjectNotFoundException{
		Map compMap = ObjectAsMap.getInstance("Composite", comp);
		compMap.put("leftGroup",new GroupAsMap(comp.getLeftGroup(),grouperSession));
		Map membershipMap=null;
		if(subj !=null) {
			membershipMap = getMembershipAndCount(grouperSession,comp.getLeftGroup(),subj);
			if(comp.getLeftGroup().hasComposite()&& membershipMap !=null) {
				membershipMap.put("viaGroup",compMap);
			}
			((Map)compMap.get("leftGroup")).put("membership",membershipMap);
		}
		compMap.put("rightGroup",new GroupAsMap(comp.getRightGroup(),grouperSession));
		if(subj !=null) {
			membershipMap = getMembershipAndCount(grouperSession,comp.getRightGroup(),subj);
			if(comp.getRightGroup().hasComposite()&& membershipMap !=null) {
				membershipMap.put("viaGroup",compMap);
			}
			((Map)compMap.get("rightGroup")).put("membership",membershipMap);
		}
		compMap.put("compositeType",comp.getType().toString());
		compMap.put("owner",new GroupAsMap(comp.getOwnerGroup(),grouperSession));
		compMap.put("id",comp.getOwnerGroup().getUuid());
		return compMap;
	}
	
	private static Map getMembershipAndCount(GrouperSession s,Group group,Subject subject) throws MemberNotFoundException,SchemaException,SubjectNotFoundException {
		Set memberships = null;
		//memberships = MembershipFinder.findMembershipsNoPrivsNoSession(group,MemberFinder.findBySubject(s,subject),FieldFinder.find("members"));
		memberships=group.getMemberships(FieldFinder.find("members"));
		
		if(memberships.size()==0) return null;
		Iterator it = memberships.iterator();
		Membership m = null;
		Membership selectedM = null;
		int count=0;
		while(it.hasNext()) {
			m=(Membership)it.next();
			if(SubjectHelper.eq(m.getMember().getSubject(),subject)) {
				selectedM=m;
				count++;
			}
		}
		if(selectedM==null) return null;
		Map mMap = ObjectAsMap.getInstance("MembershipAsMap",selectedM);
		mMap.put("noWays",new Integer(count));
		return mMap;
	}
	
	/**
	 * Trims down input so that the 'same' membership does not occur twice
	 * @param memberships
	 * @param type
	 * @param count - keeps track of the number of times a membership occurred
	 * @param sources - keeps track of different sources providing subjects
	 * @return List with one item per subject, but also update specified Map with count of how many memberships a member has
	 * @throws MemberNotFoundException
	 * @throws GroupNotFoundException
	 */
	public static List<Membership> getOneMembershipPerSubjectOrGroup(Set memberships,String type,Map count,Map sources,int membersFilterLimit) 
		throws MemberNotFoundException,GroupNotFoundException{
		//won't pass back values but will give 'unique' list
		if(count==null) count=new HashMap();
		List res = new ArrayList();
		Iterator it = memberships.iterator();
		Membership m;
		String id=null;
		Integer curCount;
		Object obj;
		while(it.hasNext()) {
			obj=it.next();
			if(!(obj instanceof Membership)) {
				res.add(obj);
				continue;
			}
			m = (Membership)obj;
			if("subject".equals(type)){
				id = m.getGroup().getUuid();
			}else if("group".equals(type)) {
				//id =m.getMember().getSubjectId();
				id=m.getMemberUuid();
				
			}else{
				throw new IllegalArgumentException("type must be 'subject' or 'group'");
			}
			curCount=(Integer)count.get(id);
			if(curCount==null) {
				curCount = new Integer(1);
				res.add(m);
			}else{
				curCount = new Integer(curCount.intValue()+1);
			}
			count.put(id,curCount);
			if(memberships.size() < membersFilterLimit) sources.put(m.getMember().getSubjectSource().getId(), m.getMember().getSubjectSource().getName());
		}
		return res;
	}
	
	/**
	 * Given a trimmed list of memberships which are now Maps, add noWays -> number of
	 * direct and indirect memberships
	 * @param membershipMaps
	 * @param type
	 * @param count
	 * @throws GroupNotFoundException
	 * @throws MemberNotFoundException
	 */
	public static void setMembershipCountPerSubjectOrGroup(List membershipMaps,String type,Map count)
		throws GroupNotFoundException,MemberNotFoundException{
		if(count==null) return;
		MembershipAsMap mMap;
		Map gMap;
		//Map sMap;
		
		String id;
		Integer curCount;
		Membership m=null;
		for(int i=0;i<membershipMaps.size();i++) {
			mMap = (MembershipAsMap)membershipMaps.get(i);
			gMap = (Map)mMap.get("group");
			//sMap = (Map)mMap.get("subject");
		
			if("subject".equals(type)){
				id = (String)gMap.get("id");
			}else if("group".equals(type)) {
				//id = (String)sMap.get("memberUuid");
				m=(Membership)mMap.getWrappedObject();
				id=m.getMemberUuid();
			}else{
				throw new IllegalArgumentException("type must be 'subject' or 'group'");
			}
			curCount = (Integer)count.get(id);
			if(curCount!=null) {
				mMap.put("noWays",curCount);
			}
			
		}
		
	}
	
	/**
	 * Checks key groups.create.grant.all to determine pre-selected privs to
	 * be checked in the UI. If not set, checks default assignments in the Grouper API
	 * @param mediaBundle
	 * @return Map keyed on Access privilege names
	 */

	public static Map getDefaultAccessPrivsForUI(ResourceBundle mediaBundle){
		String privStr = null;
		try {
			privStr = mediaBundle.getString("groups.create.grant.all");
		}catch(Exception e){}
		if(privStr==null || "".equals(privStr)) return getDefaultAccessPrivsForGrouperAPI();
		Map privs = new HashMap();
		if("none".equals(privStr)) return privs;
		String[] privArr = privStr.split(" ");
		for(int i=0;i<privArr.length;i++) {
			privs.put(privArr[i],Boolean.TRUE);
		}
		return privs;
	}
	
	/**
	 * Queries GrouperConfig - grouper.properties - to determine which Access
	 * privs are granted to GrouperAll on group creation
	 * @return Map keyed on default Access privilege names
	 */
	public static Map getDefaultAccessPrivsForGrouperAPI() {
		Map privs = new HashMap();
    String priv;
		
		for(int i=0;i<groupPrivs.length;i++){
      priv = groupPrivs[i];

			if("true".equals(GrouperConfig.getProperty("groups.create.grant.all." + priv))){
				privs.put(priv,Boolean.TRUE);
			}
		}
		return privs;
	}
	
	
	/**
	 * Use to get SubjectPrivilegeMaps rather than SubjectMaps. Relevant UI now
	 * uses these objects which can be used for more fine-grained template resolution
	 * @param s
	 * @param subjects
	 * @param group
	 * @param privilege
	 * @return List of SubjectPrivilegeAsMap's for given subjects, group and privilege
	 */
	public static List subjects2SubjectPrivilegeMaps(GrouperSession s,Collection subjects,Group group, String privilege) {
		return subjects2SubjectPrivilegeMaps(s,subjects,GroupOrStem.findByGroup(s,group),privilege);
	}
	
	/**
	 * Use to get SubjectPrivilegeMaps rather than SubjectMaps. Relevant UI now
	 * uses these objects which can be used for more fine-grained template resolution
	 * @param s
	 * @param subjects
	 * @param stem
	 * @param privilege
	 * @return List of SubjectPrivilegeAsMap's for given subjects, stem and privilege
	 */
	public static List subjects2SubjectPrivilegeMaps(GrouperSession s,Collection subjects,Stem stem, String privilege) {
		return subjects2SubjectPrivilegeMaps(s,subjects,GroupOrStem.findByStem(s,stem),privilege);	
	}
	
	/**
	 * Use to get SubjectPrivilegeMaps rather than SubjectMaps. Relevant UI now
	 * uses these objects which can be used for more fine-grained template resolution
	 * @param s
	 * @param subjects
	 * @param groupOrStem
	 * @param privilege
	 * @return List of SubjectPrivilegeAsMap's for given subjects, GroupOrStem and privilege
	 */
	public static List subjects2SubjectPrivilegeMaps(GrouperSession s,Collection subjects,GroupOrStem groupOrStem, String privilege) {
		List res = new ArrayList();
		Subject subject;
		Iterator it = subjects.iterator();
		while(it.hasNext()) {
			subject = (Subject)it.next();
			res.add(ObjectAsMap.getInstance("SubjectPrivilegeAsMap", subject,s,groupOrStem,privilege));
		}
		return res;
	}
	
	/**
	 * Use to get SubjectPrivilegeMaps rather than SubjectMaps. Relevant UI now
	 * uses these objects which can be used for more fine-grained template resolution
	 * @param s
	 * @param groupsOrStems
	 * @param subject
	 * @param privilege
	 * @return List of SubjectPrivilegeAsMap's for given subject, GroupOrStems and privilege
	 */
	public static List subjects2SubjectPrivilegeMaps(GrouperSession s,Collection groupsOrStems,Subject subject, String privilege) {
		List res = new ArrayList();
		GroupOrStem groupOrStem;
		Iterator it = groupsOrStems.iterator();
		Object obj;
		while(it.hasNext()) {
			obj = it.next();
			if(obj instanceof Group){
				res.add(ObjectAsMap.getInstance("SubjectPrivilegeAsMap", subject,s,GroupOrStem.findByGroup(s,(Group)obj),privilege));
			}else{
				res.add(ObjectAsMap.getInstance("SubjectPrivilegeAsMap", subject,s,GroupOrStem.findByStem(s,(Stem)obj),privilege));
			}
		}
		return res;
	}
	
	/**
	 * Use to get MembershipMaps rather than SubjectMaps. Relevant UI now
	 * uses these objects which can be used for more fine-grained template resolution
	 * @param s
	 * @param memberships
	 * @return List of Memberships as Maps
	 */
	public static List memberships2Maps(GrouperSession s,Collection memberships) {
		return memberships2Maps(s,memberships,false);
	
	}
	
	/**
	 * Use to get MembershipMaps rather than SubjectMaps. Relevant UI now
	 * uses these objects which can be used for more fine-grained template resolution
	 * @param s
	 * @param memberships
	 * @param withParents
	 * @return List of Memberships as Maps
	 */
	public static List memberships2Maps(GrouperSession s,Collection memberships,boolean withParents) {
		List res = new ArrayList();
		Membership membership;
		Iterator it = memberships.iterator();
		while(it.hasNext()) {
			membership = (Membership)it.next();
			//TODO check if withParent is needed
			res.add(ObjectAsMap.getInstance("MembershipAsMap",membership)); 
		}
		return res;
	}
	
	
	/**
	 * For a given subject determine all the custom list fields they appear in
	 * @param s
	 * @param subject
	 * @return List of field names the subject is a member of
	 * @throws Exception
	 */
	public static List getListFieldsForSubject(GrouperSession s,Subject subject) throws Exception{
		Set lists = FieldFinder.findAllByType(FieldType.LIST);
		List res = new ArrayList();
		Iterator it = lists.iterator();
		Field field;
		Set memberships;
		while(it.hasNext()) {
			field = (Field)it.next();
			if(!"members".equals(field.getName())) {
				memberships = MemberFinder.findBySubject(s,subject).getMemberships(field);
				if(memberships.size()>0) {
					res.add(field.getName());
				}
			}
		}
		accumulateFields(res);
		return res;
	}
	
	public static Map listOfFieldsToMap(List fields) {
		Map map = new HashMap();
		String name;
		for(int i=0;i<fields.size();i++) {
			name=(String)fields.get(i);
			
			map.put(name,Boolean.TRUE);
		}
		return map;	
	}
	
	/**
	 * For a group id, for all its types, return fields of type LIST which the session user can write
	 * @param s
	 * @param groupId
	 * @return List of list fields for group
	 * @throws Exception
	 */
	public static List getWritableListFieldsForGroup(GrouperSession s,String groupId) throws Exception{
		Group g = GroupFinder.findByUuid(s,groupId);
		return getWritableListFieldsForGroup(s,g);
	}
	
	/**
	 * For a group id, for all its types, return fields of type LIST which the session user can write
	 * @param s
	 * @param g
	 * @return List of list fields for group
	 * @throws Exception
	 */
	public static List getWritableListFieldsForGroup(GrouperSession s,Group g) throws Exception{
		List writable = getListFieldsForGroup(s,g);
		Field field;
		String name;
		Iterator it = writable.iterator();
		while(it.hasNext()) {
			name=(String)it.next();
			field=FieldFinder.find(name);
			if(!g.canReadField(field)) it.remove();
		}
		accumulateFields(writable);
		return writable;
	}
	
	/**
	 * For a group id, for all its types, return fields of type LIST which the session user can read or write
	 * @param s
	 * @param groupId
	 * @return List of list fields for group
	 * @throws Exception
	 */
	public static List getReadableListFieldsForGroup(GrouperSession s,String groupId) throws Exception{
		Group g = GroupFinder.findByUuid(s,groupId);
		return getReadableListFieldsForGroup(s,g);
	}
	
	/**
	 * For a group id, for all its types, return fields of type LIST which the session user can read or write
	 * @param s
	 * @param g
	 * @return List of list fields for group
	 * @throws Exception
	 */
	public static List getReadableListFieldsForGroup(GrouperSession s,Group g) throws Exception{
		List readable = getListFieldsForGroup(s,g);
		Field field;
		String name;
		Iterator it = readable.iterator();
		while(it.hasNext()) {
			name=(String)it.next();
			field=FieldFinder.find(name);
			if(!g.canReadField(field)&& !g.canWriteField(field)) it.remove();
		}
		accumulateFields(readable);
		return readable;
	}
	
	/**
	 * For a group id, for all its types, return fields of type LIST
	 * @param s
	 * @param groupId
	 * @return List of list fields for group
	 * @throws Exception
	 */
	public static List getListFieldsForGroup(GrouperSession s,String groupId) throws Exception{
		Group g = GroupFinder.findByUuid(s,groupId);
		return getListFieldsForGroup(s,g);
	}
	
	
	/**
	 * For a group, for all its types, return fields of type LIST
	 * @param s
	 * @param g
	 * @return List of list fields for group
	 */
	public static List getListFieldsForGroup(GrouperSession s,Group g) throws SchemaException{
		List lists = new ArrayList();
		Set types = g.getTypes();
		Iterator it = types.iterator();
		Set fields;
		Field field;
		Iterator fieldsIt;
		GroupType type;
		while(it.hasNext()) {
			type = (GroupType)it.next();
			fields=type.getFields();
			fieldsIt=fields.iterator();
			while(fieldsIt.hasNext()) {
				field = (Field)fieldsIt.next();
				if(field.getType().equals(FieldType.LIST)&& !"members".equals(field.getName())) {
					if(canRead(s,field,g)) {
						lists.add(field.getName());
					}
				}
			}
		}
		accumulateFields(lists);
		return lists;
	}
	
	/**
	 * For a group, for all its types, return fields user can read / write
	 * @param s
	 * @param g
	 * @param priv read / write
	 * @return Map keyed on field names
	 */
	public static Map getFieldsForGroup(GrouperSession s,Group g,String priv) throws SchemaException{
		Map fieldsMap = new HashMap();
		Set types = g.getTypes();
		Iterator it = types.iterator();
		Set fields;
		Field field;
		Iterator fieldsIt;
		GroupType type;
		while(it.hasNext()) {
			type = (GroupType)it.next();
			fields=type.getFields();
			fieldsIt=fields.iterator();
			while(fieldsIt.hasNext()) {
				field = (Field)fieldsIt.next();
				if(("read".equals(priv) && canRead(s,field,g))
						||("write".equals(priv) && canWrite(s,field,g))) {
						fieldsMap.put(field.getName(),Boolean.TRUE);
				}
			}
		}
		accumulateFields(fieldsMap.keySet());
		return fieldsMap;
	}
	
	
	/**
	 * Can the current user read this field?
	 * Should probably remove, API support is there now
	 * @param s
	 * @param field
	 * @param g
	 * @return whether session subject can read specified field for specified group
	 * @throws SchemaException
	 */
	
	public static boolean canRead(GrouperSession s,Field field,Group g) throws SchemaException{
		return g.canReadField(field);
	}
	
	/**
	 * Can the current write read this field?
	 * Should probably remove, API support is there now
	 * @param s
	 * @param field
	 * @param g
	 * @return whether session subject can write specified field for specified group
	 * @throws SchemaException
	 */
	
	public static boolean canWrite(GrouperSession s,Field field,Group g) throws SchemaException{
		return g.canWriteField(field);
		
	}
	
	
	private static String noSearchFields="";//:name:displayName:extension:displayExtension:";
	
	/**
	 * Retrieve list of attributes which can be searched
	 * @return List of searchable fields
	 * @throws SchemaException
	 */
	public static  List getSearchableFields(ResourceBundle bundle) throws SchemaException{
		List res = new ArrayList();
		List<String> names=new ArrayList();
		
		for (int i=0;i<searchableGroupFields.length;i++) {
			Map map = new HashMap();
			map.put("name",searchableGroupFields[i]);
			map.put("displayName",bundle.getString("field.displayName." + searchableGroupFields[i]));
			res.add(map);
			names.add(searchableGroupFields[i]);
		}
		
		
		Set fields = FieldFinder.findAllByType(FieldType.ATTRIBUTE);
		Iterator it = fields.iterator();
		Field field;
		while(it.hasNext()) {
			field = (Field) it.next();
			if(noSearchFields.indexOf(":" + field.getName() + ":") == -1) {
				names.add(field.getName());
				res.add(ObjectAsMap.getInstance("FieldAsMap",field));
			}
		}
		accumulateFields(names);
		return res;
	}
	
	private static void accumulateFields(Collection<String> fields) {
		Set<String> accumulated = (Set<String>)UIThreadLocal.get("accumulatedFields");
		if(accumulated==null) {
			accumulated = new HashSet<String>();
			UIThreadLocal.put("accumulatedFields", accumulated);
		}
		accumulated.addAll(fields);
	}
	
	/**
	 * When we query fields we 'accumulate' them so we can check if tere are any new ones. If there are
	 * we refresh the session list
	 * @param fieldList Map of FieldAsMaps from the HttpSession
	 * @throws SchemaException
	 */
	public static void fixSessionFields(Map fieldList) throws SchemaException{
		Set<String> accumulated = (Set<String>)UIThreadLocal.get("accumulatedFields");
		if(accumulated==null) {
			return;
		}
		for(String fieldName : accumulated) {
			if(!fieldList.containsKey(fieldName)) {
				Map newFields = GrouperHelper.getFieldsAsMap();
				fieldList.clear();
				fieldList.putAll(newFields);
				break;
			}
		}
	}
	
	/**
	 * Retrieve Map of attributes which can be searched
	 * @return Map of searchable fields
	 * @throws SchemaException
	 */
	public static  Map getFieldsAsMap() throws SchemaException{

		Set fields = FieldFinder.findAll();
		Iterator it = fields.iterator();
		Field field;
		Map fieldMap;
		Map map = new LinkedHashMap();
		ResourceBundle bundle = GrouperUiFilter.retrieveSessionNavResourceBundle();
		while(it.hasNext()) {
			field = (Field) it.next();
			fieldMap=ObjectAsMap.getInstance("FieldAsMap",field);
			map.put(field.getName(),fieldMap);
		}
		String[] primaryFields = new String[] {"extension","displayExtension","name","displayName","description"};
		for (int i=0;i<primaryFields.length;i++) {
			Map dummyField = new HashMap();
			dummyField.put("displayName", bundle.getString("field.displayName." + primaryFields[i]));
			map.put(primaryFields[i], dummyField);
		}
		Map any = new HashMap();
		any.put("displayName",bundle.getString("field.displayName._any"));
		map.put("_any",any);
		Map stemMap=new HashMap();
		stemMap.put("extension","extension");
		stemMap.put("displayExtension","displayExtension");
		stemMap.put("name","name");
		stemMap.put("displayName","displayName");
		stemMap.put("description","description");
		try {
			stemMap.put("extension",bundle.getString("stems.edit.name"));
		}catch(Exception e){}
		try {
			stemMap.put("displayExtension",bundle.getString("stems.edit.display-name"));
		}catch(Exception e){}
		try {
			stemMap.put("name",bundle.getString("stems.edit.full-name"));
		}catch(Exception e){}
		try {
			stemMap.put("displayName",bundle.getString("stems.edit.full-display-name"));
		}catch(Exception e){}
		try {
			stemMap.put("description",bundle.getString("stems.edit.description"));
		}catch(Exception e){}
			map.put("stems",stemMap);
		return map;
	}
	
	
	/**
	 * Returns whether the user associated with the active GrouperSession can edit
	 * any cuustom attribute associate with the supplied group
	 * @param group
	 * @return whether the user associated with the active GrouperSession can edit
	 * any cuustom attribute associate with the supplied group
	 * @throws SchemaException
	 */
	public static boolean canUserEditAnyCustomAttribute(Group group) throws SchemaException{
		Set types = group.getTypes();
		if(types.isEmpty()) return false;
		Iterator it = types.iterator();
		GroupType groupType;
		Set fields;
		Iterator fieldsIterator;
		Field field;
		while(it.hasNext()) {
			groupType=(GroupType)it.next();
			if(groupType.isSystemType()) continue;
			fields = groupType.getFields();
			fieldsIterator = fields.iterator();
			while(fieldsIterator.hasNext()) {
				field=(Field)fieldsIterator.next();
				if(field.getType().equals(FieldType.ATTRIBUTE)&& group.canWriteField(field)) {
					return true;
				}
			}
		}
		return false;
	}
	
	private static String[] searchableStemFields=new String[] {"displayExtension","extension","displayName","name"};
	private static String[] searchableGroupFields=new String[] {"displayExtension","extension","displayName","name"};
	
	/**
	 * Returns a list of Maps representing name / displayNames for stem fields
	 * Stems don't have fields in the way Groups do. This approach allows for similar
	 * code for advanced group and stem searching
	 * @param bundle
	 * @return a list of Maps representing name / displayNames for stem fields
	 */
	public static List getSearchableStemFields(ResourceBundle bundle) {
		List res = new ArrayList();
		for (int i=0;i<searchableStemFields.length;i++) {
			Map map = new HashMap();
			map.put("name",searchableStemFields[i]);
			map.put("displayName",bundle.getString("field.stem.displayName." + searchableStemFields[i]));
			res.add(map);
		}
		return res;
	}
	
	/**
	 * Filter groups according to privileges of session subject and whether 
	 * provided subject has any privileges for this group
	 * @param s
	 * @param groups
	 * @param subject
	 * @return filtered List
	 */
	public static List filterGroupsForSubject(GrouperSession s,List groups,Subject subject) {
		List ok = new ArrayList();
		Group group;
		for (int i=0;i<groups.size();i++) {
			group = (Group)groups.get(i);
			if(group.hasAdmin(s.getSubject())) {
				if(group.hasMember(subject)||
						group.hasView(subject)||
						group.hasAdmin(subject)||
						group.hasUpdate(subject)||
						group.hasRead(subject)||
						group.hasOptin(subject)||
            group.hasOptout(subject) ||
            group.hasGroupAttrRead(subject) ||
            group.hasGroupAttrUpdate(subject)
						) {
					ok.add(group);
				}
			}
		}
		
		return ok;
	
	}
	
	/**
	 * Supplement group maps with privilege info for provided subject 
	 * assuming subject has any privileges for this group
	 * @param s
	 * @param groups
	 * @param subject
	 * @return a List of embellished groups
	 */
	public static List embellishGroupMapsWithSubjectPrivs(GrouperSession s,List groups,Subject subject) throws Exception{
		
		GroupAsMap group;
		Map privs=null;
		Member member = MemberFinder.findBySubject(s,subject);
		GroupOrStem gs = null;
		
		for (int i=0;i<groups.size();i++) {
			group = (GroupAsMap)groups.get(i);
			gs=GroupOrStem.findByGroup(s, (Group)group.getWrappedObject());
			privs=getAllHas(s,gs,member);
			group.put("subjectPrivs", privs);
			group.put("privsSubject", subject.getId());
		}
		
		return groups;
	
	}
	
	/**
	 * Rather than force a client to  call getMember which involves a SQL query
	 * expose the memberUuid where that is sufficient
	 * @param m
	 * @return memberUuid
	 */
	public static String getMemberUuid(Membership m) {
		return m.getMemberUuid();
	}
	

	public static boolean isRoot(GrouperSession s) {
		return PrivilegeHelper.isRoot(s);
	}
	
	public static GrouperSession getRootGrouperSession(GrouperSession s) {
		return s.internal_getRootSession();	
	}
	
	public static boolean isDirect(LazySubject ls) {
		Membership ms = ls.getMembership();
		//This is a hack, but will have to look at ramifications
		//later
		if(ms==null) return false;
		return ms.getDepth()==0;
	}
	
	public static boolean hasOtherReadableFields(Group g, String fieldName) {
		Field f = null;
		try {
			f=FieldFinder.find(fieldName);
		}catch(SchemaException e) {
			throw new RuntimeException(e);
		}
		Set<GroupType> types=g.getTypes();
		int count=0;
		for(GroupType type : types) {
			Set<Field> fields = type.getFields();
			for(Field field : fields) {
				try {
					if(!field.equals(f) && field.isGroupListField() && g.canReadField(field)) {
						count++;
					}
				}catch(SchemaException e) {
					LOG.error(e);
				}
			}
		}
		return count > 0 || !f.getName().equals("members");
	}

  /*public static List query(String sql) throws Exception{
  	Connection con = null;
  	try {
  		  List results = new ArrayList();
  	      Session hs = HibernateDAO.getSession();
  	      
  	      con = hs.connection();
  	      Statement stmt = con.createStatement();
  	      ResultSet rs = stmt.executeQuery(sql);
  	      Object obj;
  	      while(rs.next()) {
  	    	  obj = rs.getObject(1);
  	    	  results.add(obj);
  	      }
  	      return results;
  	    }
  	    catch (Exception e) {
  	      throw new QueryException("error finding groups: " +
  e.getMessage(), e);
  	    }finally {
  	    	try {
  	    		con.close();
  	    	}catch(Exception e){}
  	    }

  }*/

  /**
   * Return true if the following parameter values would allow a subject to 
   * copy the specified stem to another location.
   * @param stem
   * @param canCopy This is false if there's a security group limiting the subjects
   * that can copy stems and this subject is not in the group.
   * @return boolean
   */
  public static boolean canCopyStem(Stem stem, boolean canCopy) {
    if (!canCopy || stem.isRootStem()) {
      return false;
    }

    return true;
  }

  /**
   * Returns true if the following parameter values would allow a subject to 
   * move the specified stem to another location.
   * @param stem
   * @param canMove This is false if there's a security group limiting the subjects
   * that can move stems and this subject is not in the group.
   * @param privs The naming privileges that the subject has on the stem.
   * @return boolean
   */
  public static boolean canMoveStem(Stem stem, boolean canMove, Set<Privilege> privs) {
    if (!canMove || stem.isRootStem()) {
      return false;
    }

    if (privs.contains(NamingPrivilege.STEM)) {
      return true;
    }

    return false;
  }

  /**
   * Returns true if the following parameter values would allow a subject to 
   * copy a stem to the specified stem.
   * @param canCopy This is false if there's a security group limiting the subjects
   * that can copy stems and this subject is not in the group.
   * @param privs The naming privileges that the subject has on the specified stem.
   * @return boolean
   */
  public static boolean canCopyOtherStemToStem(Stem stem, boolean canCopy,
      Set<Privilege> privs) {
    if (!canCopy) {
      return false;
    }

    if (privs.contains(NamingPrivilege.STEM)) {
      return true;
    }

    return false;
  }

  /**
   * Returns true if the following parameters would allow a subject to 
   * move a stem to the specified stem.
   * @param canMove This is false if there's a security group limiting the subjects
   * that can move stems and this subject is not in the group.
   * @param privs The naming privileges that the subject has on the specified stem.
   * @return boolean
   */
  public static boolean canMoveOtherStemToStem(Stem stem, boolean canMove,
      Set<Privilege> privs) {
    if (!canMove) {
      return false;
    }

    if (privs.contains(NamingPrivilege.STEM)) {
      return true;
    }

    return false;
  }

  /**
   * Returns true if the following parameters would allow a subject to 
   * copy a group to the specified stem.
   * @param stem
   * @param privs The naming privileges that the subject has on the stem.
   * @return boolean
   */
  public static boolean canCopyGroupToStem(Stem stem, Set<Privilege> privs) {
    if (stem.isRootStem()) {
      return false;
    }

    if (privs.contains(NamingPrivilege.CREATE)) {
      return true;
    }

    return false;
  }

  /**
   * Returns true if the following parameters would allow a subject to
   * move a group to the specified stem.
   * @param stem
   * @param privs The naming privileges that the subject has on the stem.
   * @return boolean
   */
  public static boolean canMoveGroupToStem(Stem stem, Set<Privilege> privs) {
    if (stem.isRootStem()) {
      return false;
    }

    if (privs.contains(NamingPrivilege.CREATE)) {
      return true;
    }

    return false;
  }

  /**
   * Copy a group
   * @param group
   * @param destinationStem
   * @param selections
   * @return
   */
  public static Group copyGroup(Group group, Stem destinationStem, String[] selections) {

    List<String> selectionsList = new LinkedList<String>();
    if (selections != null) {
      for (int i = 0; i < selections.length; i++) {
        selectionsList.add(selections[i]);
      }
    }

    GroupCopy groupCopy = new GroupCopy(group, destinationStem);

    // set options for copy
    if (selectionsList.contains("copyPrivilegesOfGroup")) {
      groupCopy.copyPrivilegesOfGroup(true);
    } else {
      groupCopy.copyPrivilegesOfGroup(false);
    }

    if (selectionsList.contains("copyGroupAsPrivilege")) {
      groupCopy.copyGroupAsPrivilege(true);
    } else {
      groupCopy.copyGroupAsPrivilege(false);
    }

    if (selectionsList.contains("copyListMembersOfGroup")) {
      groupCopy.copyListMembersOfGroup(true);
    } else {
      groupCopy.copyListMembersOfGroup(false);
    }

    if (selectionsList.contains("copyListGroupAsMember")) {
      groupCopy.copyListGroupAsMember(true);
    } else {
      groupCopy.copyListGroupAsMember(false);
    }

    if (selectionsList.contains("copyAttributes")) {
      groupCopy.copyAttributes(true);
    } else {
      groupCopy.copyAttributes(false);
    }

    Group newGroup = groupCopy.save();

    return newGroup;
  }

  /**
   * Move a group
   * @param group
   * @param destinationStem
   * @param selections
   */
  public static void moveGroup(Group group, Stem destinationStem, String[] selections) {

    List<String> selectionsList = new LinkedList<String>();
    if (selections != null) {
      for (int i = 0; i < selections.length; i++) {
        selectionsList.add(selections[i]);
      }
    }

    GroupMove groupMove = new GroupMove(group, destinationStem);

    // set options for move
    if (selectionsList.contains("assignAlternateName")) {
      groupMove.assignAlternateName(true);
    } else {
      groupMove.assignAlternateName(false);
    }

    groupMove.save();
  }

  /**
   * Copy a stem
   * @param stemToCopy
   * @param destinationStem
   * @param selections
   * @return
   */
  public static Stem copyStem(Stem stemToCopy, Stem destinationStem, String[] selections) {

    List<String> selectionsList = new LinkedList<String>();
    if (selections != null) {
      for (int i = 0; i < selections.length; i++) {
        selectionsList.add(selections[i]);
      }
    }

    StemCopy stemCopy = new StemCopy(stemToCopy, destinationStem);

    // set options
    if (selectionsList.contains("copyPrivilegesOfStem")) {
      stemCopy.copyPrivilegesOfStem(true);
    } else {
      stemCopy.copyPrivilegesOfStem(false);
    }

    if (selectionsList.contains("copyPrivilegesOfGroup")) {
      stemCopy.copyPrivilegesOfGroup(true);
    } else {
      stemCopy.copyPrivilegesOfGroup(false);
    }

    if (selectionsList.contains("copyGroupAsPrivilege")) {
      stemCopy.copyGroupAsPrivilege(true);
    } else {
      stemCopy.copyGroupAsPrivilege(false);
    }

    if (selectionsList.contains("copyListMembersOfGroup")) {
      stemCopy.copyListMembersOfGroup(true);
    } else {
      stemCopy.copyListMembersOfGroup(false);
    }

    if (selectionsList.contains("copyListGroupAsMember")) {
      stemCopy.copyListGroupAsMember(true);
    } else {
      stemCopy.copyListGroupAsMember(false);
    }

    if (selectionsList.contains("copyAttributes")) {
      stemCopy.copyAttributes(true);
    } else {
      stemCopy.copyAttributes(false);
    }

    Stem newStem = stemCopy.save();

    return newStem;
  }

  /**
   * Move a stem
   * @param stemToMove
   * @param destinationStem
   * @param selections
   */
  public static void moveStem(Stem stemToMove, Stem destinationStem, String[] selections) {

    List<String> selectionsList = new LinkedList<String>();
    if (selections != null) {
      for (int i = 0; i < selections.length; i++) {
        selectionsList.add(selections[i]);
      }
    }

    StemMove stemMove = new StemMove(stemToMove, destinationStem);

    // set options
    if (selectionsList.contains("assignAlternateName")) {
      stemMove.assignAlternateName(true);
    } else {
      stemMove.assignAlternateName(false);
    }

    stemMove.save();
  }
  
  public static String getMemberDisplayValue(Member member, ResourceBundle bundle) {
	  String field="description";
	  
	  String value="unknown";
	  try {
		  field = bundle.getString("subject.display." + member.getSubjectSourceId());
	  }catch (MissingResourceException e) {
		  try {
		  field = bundle.getString("subject.display.default");
		  }catch(MissingResourceException ee) {
			  
		  }
	  }
	  try {
	  value=member.getSubject().getAttributeValue(field);
	  }catch(Exception e) {
		  
	  }
	  if(value==null){
		  value=member.getSubjectId().toString();
	  }
	  return value;
  }
}




