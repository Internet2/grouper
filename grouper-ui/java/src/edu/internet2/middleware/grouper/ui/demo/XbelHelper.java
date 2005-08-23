package edu.internet2.middleware.grouper.ui.demo;

import xbel.model.*;
import xbel.model.types.*;
import java.io.*;
import java.net.URL;
import java.util.*;
import java.util.regex.*;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.collections.SynchronizedPriorityQueue;
import org.apache.struts.config.MessageResourcesConfig;
import org.apache.struts.config.ModuleConfig;

import sun.tools.tree.SynchronizedStatement;
import uk.ac.bris.is.xml.DOMHelper;
import org.w3c.dom.*;


public class XbelHelper {
	public static Map lookup = new HashMap();
	public static String dir = null;
	public static String urlDir = null;
	static {
		URL url =XbelHelper.class.getClassLoader().getResource("resources/init.properties");
		urlDir = url.toString().replaceAll("classes.*$","");
		urlDir = urlDir.replaceAll("%20"," ");
		dir = urlDir.replaceAll("^file:/+","");
		if(!dir.startsWith("/")&& dir.charAt(1)!=':') dir = "/" + dir;
		
	}
	
	//public static String dir = "c:/projects/groups-for-diagram/GroupsManager/WEB-INF/";
	private static List people = new ArrayList();
	public static synchronized Xbel getXbel(ServletContext context) throws Exception{
	
		
		
		Xbel xbel = null;
		FileReader reader=null;
		try {
			xbel = (Xbel)context.getAttribute("groupsDatabase");
			if(xbel==null) {
				reader = new FileReader(dir + "groups.xml");
				xbel = (Xbel) Xbel.unmarshal(reader);
				init(xbel);
				readPeople();
				context.setAttribute("groupsDatabase",xbel);	
			}
			return xbel;
		}finally {
			if(reader !=null) reader.close();	
		}  
	}
	
	public static synchronized void saveXbel(ServletContext context) throws Exception{
		Xbel xbel = null;
		xbel = (Xbel)context.getAttribute("groupsDatabase");
		if(xbel==null) {
			throw new Exception("No database to save");	
		}
		init(xbel);
		FileWriter writer = null;
		StringWriter swriter = new StringWriter();
		try {
		writer=new FileWriter(dir + "groups.xml");
		xbel.marshal(swriter);
		writer.write(swriter.toString());//.replaceAll("</folder>","</folder>\n"));
		}finally {
			if(writer !=null) {
				writer.flush();
				writer.close();
				//context.removeAttribute("groupsDatabase");
			}
			
		}	
	}
	
	
	public static String render(String currentNodeId,String mode,HttpServletRequest request) throws Exception {
		return render(currentNodeId,mode,false,"false",request);
	}
	public static String render(String currentNodeId,String mode,String isFlat,HttpServletRequest request) throws Exception {
		return render(currentNodeId,mode,false,isFlat,request);
	}
	public static String render(String currentNodeId,String mode,boolean forFind,String isFlat,HttpServletRequest request) throws Exception {
		ServletContext context = request.getSession().getServletContext();
		Subject subject = (Subject)request.getSession().getAttribute("activeSubject");
		ModuleConfig mc = (ModuleConfig)request.getAttribute("org.apache.struts.action.MODULE");
		String modulePath=mc.getPrefix();
		if(modulePath.length()>0){
			modulePath=modulePath.substring(1) + "/";
		}
		Map params = new HashMap();
		if(currentNodeId==null) currentNodeId="root";
		params.put("activeNodeId",currentNodeId);
		params.put("mode",mode);
		params.put("strutsModule",modulePath);
		params.put("subject",subject.getId());
		if(forFind) {
			params.put("forFind","true");
			params.put("linkMode","Find");
			
		}else {
			params.put("linkMode",mode);
		}
		params.put("flat",isFlat);
		Xbel xbel = getXbel(context);
		StringWriter writer = new StringWriter();
		xbel.marshal(writer);
		Document doc = DOMHelper.newDocument(writer.toString());
		Document result = DOMHelper.transform(doc,urlDir+ "xbel.xsl",params);
		String output = DOMHelper.domToString(result,false);
		return output;			
	}
	
	public static String asXml(ServletContext context) throws Exception {
		
		
		Xbel xbel = getXbel(context);
		StringWriter writer = new StringWriter();
		xbel.marshal(writer);
		
		return writer.toString();			
	}
	
	public static synchronized void addFolder(Folder folder,String curNode,ServletContext context) throws Exception{
		if(lookup.get(folder.getId())==null){ 
			
			Folder parent = getFolder(curNode,context);
			if(parent==null) parent = getFolder("root",context);
			FolderChoice fc = new FolderChoice();
			FolderChoiceItem fci = new FolderChoiceItem();
			fci.setFolder(folder);
			fc.setFolderChoiceItem(fci);
			parent.addFolderChoice(fc);
			saveXbel(context);
			lookup.put(folder.getId(),folder);
		}else{
			saveXbel(context);
		}
	}
	
	public static String nodePathToString(String folderId,ServletContext context) throws Exception{
		 	StringBuffer sb = new StringBuffer();
			boolean isGroup = isGroup(folderId,context);
			String parentId = null;
			Folder folder = null;
			int pos=0;
			int lastPos=-1;
			do {
				if(lastPos!=-1) sb.append(" - ");
				pos = folderId.indexOf("/",pos+1);
				if(pos==-1) {
					folder = getFolder(folderId,context);
					if(isGroup) sb.append("[");
					sb.append(folder.getTitle().getContent());
					if(isGroup) sb.append("]");
				}else {
					folder = getFolder(folderId.substring(0,pos),context);
					sb.append(folder.getTitle().getContent());
				}
				lastPos = pos;
			}while (pos> -1);
			
			return sb.toString();
	}
	
	public static void deleteFolder(String folderId,ServletContext context) throws Exception{
		 
			Folder folder = getFolder(folderId,context);
			String parentId = folderId.substring(0,folderId.lastIndexOf("/"));
			Folder parent = getFolder(parentId,context);
			FolderChoice[] children = parent.getFolderChoice();
			int toRemove=-1;
			Folder child;
			for(int i=0;i<children.length;i++) {
				child=children[i].getFolderChoiceItem().getFolder();
				if(child !=null && child.getId().equals(folderId)) toRemove=i;
			}
			if(toRemove > -1) parent.removeFolderChoice(toRemove);

			
			saveXbel(context);
			
		
	}
	
	public static synchronized void removeMember(String memberId,String folderId,ServletContext context) throws Exception{
		 
			Folder folder = getFolder(folderId,context);
			if(folder==null) return;
			FolderChoice[] children = folder.getFolderChoice();
			if(children==null) return;
			int toRemove=-1;
			for(int i=0;i<children.length;i++) {
				if(children[i].getFolderChoiceItem().getBookmark() !=null &&children[i].getFolderChoiceItem().getBookmark().getId().endsWith("*" + memberId)) toRemove=i;
			}
			if(toRemove > -1) folder.removeFolderChoice(toRemove);

		
			saveXbel(context);
			
		
	}
	
	public static void saveMember(String memberId,Map privilegesMap,String folderId,ServletContext context) throws Exception{
		if("SuperUser".equals(memberId)) return;
		String[] privileges = new String[privilegesMap.size()];
		Iterator it = privilegesMap.keySet().iterator();
		int c=0;
		while(it.hasNext()){
			privileges[c++] = (String)it.next();
		}
		saveMember(memberId,privileges,folderId,false,context);
		
	}
	
	
	
	public static synchronized void saveMember(String memberId,String[] privileges,String folderId,ServletContext context) throws Exception{
		saveMember(memberId,privileges,folderId,false,context);
		
	}
	public static synchronized void saveMember(String memberId,String[] privileges,String folderId,boolean create, ServletContext context) throws Exception{
		if(privileges==null || privileges.length==0) {
			removeMember(memberId,folderId,context);
			return;
		}
		Bookmark member = getMemberAsBookmark(memberId,folderId,create,context);
		StringBuffer sb = new StringBuffer();
		for(int i=0;i<privileges.length;i++) {
			if(i>0) sb.append("/");
			sb.append(privileges[i]);
		}
		sb.append("*");
		sb.append(memberId);
		member.setId(sb.toString());
		saveXbel(context);
		
	}
	
	public static Map getMembers(String folderId,ServletContext context) throws Exception{
		 	Map members = new HashMap();
		 	if(folderId==null) return members;
			Folder folder = getFolder(folderId,context);
			FolderChoice[] children = folder.getFolderChoice();
			if(children==null) return members;
			Bookmark member;
			for(int i=0;i<children.length;i++) {
				member = children[i].getFolderChoiceItem().getBookmark();
				if(member !=null) {
					String id = member.getId();
					
					String[] parts = id.substring(0,id.lastIndexOf("*")).split("/");
					String memberId = id.substring(id.lastIndexOf("*") + 1);
					if("SuperUser".equals(memberId)) continue;
					Map map = new HashMap();
					members.put(memberId,map);
					map.put("displayName",id.substring(id.lastIndexOf("*") + 1));
					if(id.lastIndexOf("/")>id.lastIndexOf("*")) {
						Folder f = getFolder(memberId,context);
						map.put("displayName",f.getTitle().getContent());
					
					}
					for (int j=0;j<parts.length;j++) {
						map.put(parts[j],"1");
					}
				}
			}
			
			return members;
	}
	
	public static Map getMember(String memberId,String folderId,ServletContext context) throws Exception{
	 	
		Folder folder = getFolder(folderId,context);
		FolderChoice[] children = folder.getFolderChoice();
		Bookmark member;
		for(int i=0;i<children.length;i++) {
			member = children[i].getFolderChoiceItem().getBookmark();
			if(member !=null) {
				String id = member.getId();
				String[] parts = id.substring(0,id.lastIndexOf("*")).split("/");
				String curMemberId = id.substring(id.lastIndexOf("*")+1);
				if(curMemberId.equals(memberId)) {
					Map map = new HashMap();
					map.put("displayName",memberId);
					if(memberId.indexOf("/")>-1) {
						Folder group = getFolder(memberId,context);
						String name=group.getTitle().getContent();
						map.put("displayName",name);
					}
					for (int j=0;j<parts.length;j++) {
						map.put(parts[j],"1");
					}
					return map;
				}
			}
		}
		
		return new HashMap();
	}
	
	public static void joinGroup(String memberId,String folderId,ServletContext context) throws Exception{
	 	
		Folder folder = getFolder(folderId,context);
		FolderChoice[] children = folder.getFolderChoice();
		Bookmark member;
		for(int i=0;i<children.length;i++) {
			member = children[i].getFolderChoiceItem().getBookmark();
			if(member !=null) {
				String id = member.getId();
				String[] parts = id.substring(0,id.lastIndexOf("*")).split("/");
				String curMemberId = id.substring(id.lastIndexOf("*")+1);
				if(curMemberId.equals(memberId)) {
					Map map = new HashMap();
					
					map.put("member","1");
					for (int j=0;j<parts.length;j++) {
						map.put(parts[j],"1");
					}
					saveMember(memberId,map,folderId,context);
				}
			}
		}
		
		
	}
	
	public static void leaveGroup(String memberId,String folderId,ServletContext context) throws Exception{
	 	
		Folder folder = getFolder(folderId,context);
		FolderChoice[] children = folder.getFolderChoice();
		Bookmark member;
		for(int i=0;i<children.length;i++) {
			member = children[i].getFolderChoiceItem().getBookmark();
			if(member !=null) {
				String id = member.getId();
				String[] parts = id.substring(0,id.lastIndexOf("*")).split("/");
				String curMemberId = id.substring(id.lastIndexOf("*")+1);
				if(curMemberId.equals(memberId)) {
					Map map = new HashMap();
					
					
					for (int j=0;j<parts.length;j++) {
						if(!"member".equals(parts[j])) map.put(parts[j],"1");
					}
					saveMember(memberId,map,folderId,context);
				}
			}
		}
		
		
	}
	
	public static Bookmark getMemberAsBookmark(String memberId,String folderId,boolean create,ServletContext context) throws Exception{
	 	
		Folder folder = getFolder(folderId,context);
		FolderChoice[] children = folder.getFolderChoice();
		Bookmark member;
		for(int i=0;i<children.length;i++) {
			member = children[i].getFolderChoiceItem().getBookmark();
			if(member !=null) {
				String id = member.getId();
				String[] parts = id.substring(0,id.lastIndexOf("*")).split("/");
				String curMemberId = id.substring(id.lastIndexOf("*")+1);
				if(curMemberId.equals(memberId)) {
					return member;
				}
			}
		}
		if(create) {
			member = new Bookmark();
			member.setId("--waiting--");
			FolderChoice fc = new FolderChoice();
			FolderChoiceItem fci = new FolderChoiceItem();
			fci.setBookmark(member);
			fc.setFolderChoiceItem(fci);
			folder.addFolderChoice(fc);
			return member;
		}
		return null;
	}
	
	public static boolean isGroup(String folderId,ServletContext context) throws Exception{
		if(folderId==null) return false;
		Folder folder = getFolder(folderId,context);
		if(folder.getInfo().getMetadata(0).getOwner().equals("group")) {
			return true;
		}
		return false;
		
		
		}
	public static Folder getFolder(String folderId,ServletContext context) throws Exception{
		Xbel xbel = getXbel(context);
		Folder folder = (Folder)lookup.get(folderId);
		return folder;
	}
	
	private static void init(Xbel xbel) {
		lookup.clear();
		traverseXbel(xbel);
	}
	
	public static void traverseXbel(Xbel bookmarks)
	{
	 
	 int topCount = bookmarks.getXbelChoiceCount();
	
	 for(int i=0;i<topCount;i++)
	  {
		XbelChoiceItem xbci = bookmarks.getXbelChoice(i).getXbelChoiceItem();
		if (xbci.getBookmark()!=null)
		{
		    Bookmark bk = xbci.getBookmark();
		    
		}
		if (xbci.getFolder()!=null)
		{
		    Folder fd = xbci.getFolder();
		    String id = fd.getId();
		    lookup.put(id,fd);
		    traverseFolder(fd); 
		}
	  }
	}
	
	


	public static void traverseFolder(Folder fd)
	{
	
		int folderCount = fd.getFolderChoiceCount();
		for(int i=0;i<folderCount;i++)
		{
		FolderChoiceItem fci = fd.getFolderChoice(i).getFolderChoiceItem();
			if (fci.getBookmark()!=null)
			{
			    Bookmark bk = fci.getBookmark();
			    
			}
			if (fci.getFolder()!=null)
			{
				
			    Folder fd2 = fci.getFolder();
			    String id = fd2.getId();
			    lookup.put(id,fd2);
			    traverseFolder(fd2);
			}	
		}	
		
	}
	
	private static void readPeople() throws Exception{
		Document persons = DOMHelper.newDocument(new File(dir + "people.xml"));
		NodeList nl = persons.getElementsByTagName("person");
		Element person;
		String name;
		people.clear();
		for(int i=0;i<nl.getLength();i++) {
			person = (Element)nl.item(i);
			name= person.getAttribute("forename") + " " + person.getAttribute("surname");
			people.add(name);
		}
	}
	
	public static List findPeople(String query,String targetId,ServletContext context) throws Exception{
		Map members = getMembers(targetId,context);
		Pattern pat = Pattern.compile("(?i)" + query);
		Matcher m;
		List res = new ArrayList();
		String name;
		for(int i=0;i<people.size();i++) {
			name = (String) people.get(i);
			if(members!=null && !members.containsKey(name)){
				m=pat.matcher(name);
				if(m.find()) {
					res.add(name);
				}
			}
		}
		return res;
	}
	public static List findPeople(String query,String searchFrom,String targetId,ServletContext context) throws Exception{
		Map members = getMembers(targetId,context);
		Pattern pat = Pattern.compile("(?i)" + query);
		Matcher m;
		List res = new ArrayList();
		String name;
		for(int i=0;i<people.size();i++) {
			name = (String) people.get(i);
			if(members!=null && !members.containsKey(name)){
				m=pat.matcher(name);
				if(m.find()) {
					res.add(name);
				}
			}
		}
		return res;
	}
	
	
	public static List findGroups(String query,String from,String targetId,ServletContext context) throws Exception {
		return findGroups(query,from,targetId,"group",context);
	
	}
	public static List findGroups(String query,String from,String targetId,String type,ServletContext context) throws Exception {
		Pattern pat = Pattern.compile("(?i)" + query);
		List res = new ArrayList();
		if(from==null || from.equals("")) from = "root";
		Folder fromFolder = getFolder(from,context);
		findGroups(fromFolder,query,res,pat,getMembers(targetId,context),type,context);
		return res;
	}
	
	public static void findGroups(Folder fd, String query,List res,Pattern pat,Map members,String type,ServletContext context) throws Exception
	{
	
		int folderCount = fd.getFolderChoiceCount();
		Map group;
		Matcher m;
		for(int i=0;i<folderCount;i++)
		{
		FolderChoiceItem fci = fd.getFolderChoice(i).getFolderChoiceItem();
			if (fci.getBookmark()!=null)
			{
			    Bookmark bk = fci.getBookmark();   
			}
			if (fci.getFolder()!=null)
			{
			    Folder fd2 = fci.getFolder();
			    String id = fd2.getId();
			    if(fd2.getInfo().getMetadata(0).getOwner().equals(type)) {
			    	StringBuffer toQuery = new StringBuffer(fd2.getId());
			    	if(fd2.getTitle()!=null) toQuery.append(fd2.getTitle().getContent());
			    	if(fd2.getDesc()!=null)toQuery.append(fd2.getDesc().getContent());
			    	m = pat.matcher(toQuery.toString());
			    	if(members !=null && !members.containsKey(id) && m.find()) {
			    		group = new HashMap();
			    		group.put("id",fd2.getId());
			    		group.put("displayName",fd2.getTitle().getContent());
			    		group.put("path",nodePathToString(fd.getId(),context));
			    		res.add(group);
			    	}
			    }
			    findGroups(fd2,query,res,pat,members,type,context);
			}	
		}	
	}
	
	public static void assignFolderPrivileges(String folderId,String[] people,String[] groups,String[] privileges,ServletContext context) throws Exception{
		if(privileges == null || privileges.length==0) throw new IllegalArgumentException("Must supply privileges");
		Folder folder = getFolder(folderId,context);
		Map members = getMembers(folderId,context);
		if(people != null) {
			for(int i=0;i<people.length;i++) {
				if(members.get(people[i])!=null) removeMember(people[i],folderId,context);
				saveMember(people[i],privileges,folderId,true,context);
			}
			
		}
		if(groups != null) { 
			for(int i=0;i<groups.length;i++) {
				if(members.get(groups[i])!=null) removeMember(groups[i],folderId,context);
				saveMember(groups[i],privileges,folderId,true,context);
			}
			
		}
	}
	
	public static List getAllPeople() throws Exception{
	if(people.size()==0) readPeople();	
	return people;
	}

}
