/*
 * Created on 23-Nov-2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package edu.internet2.middleware.grouper.ui.demo;
import javax.servlet.*;
import javax.servlet.http.*;
import java.util.*;
/**
 * @author isgwb
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class Subject {
	private String id;
	private String displayName;
	public Subject(String id) {
		this.id = id;
	}
	
	
	public boolean isAble(String folderId,String priv,HttpServletRequest request) throws Exception {
		if("SuperUser".equals(id)) return true;
		ServletContext context = getContext(request);
		Map member = XbelHelper.getMember(getId(),folderId,context);
		return member.containsKey(priv);
		
	}
	
	public boolean isAble(String folderId,String[] privs,HttpServletRequest request) throws Exception {
		if("SuperUser".equals(id)) return true;
		ServletContext context = getContext(request);
		Map member = XbelHelper.getMember(getId(),folderId,context);
		for(int i=0;i<privs.length;i++) {
			if(member.containsKey(privs[i])) return true;
		}
		return false;
		
	}
	
	private ServletContext getContext(HttpServletRequest request) {
		return request.getSession().getServletContext();
	}
	
	
	/**
	 * @return Returns the displayName.
	 */
	public String getDisplayName() {
		return displayName;
	}
	/**
	 * @param displayName The displayName to set.
	 */
	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}
	/**
	 * @return Returns the id.
	 */
	public String getId() {
		return id;
	}
	/**
	 * @param id The id to set.
	 */
	public void setId(String id) {
		this.id = id;
	}
}
