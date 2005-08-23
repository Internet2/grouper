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

package edu.internet2.middleware.grouper.ui;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

import javax.servlet.http.HttpServletRequest;

import edu.internet2.middleware.grouper.Grouper;
import edu.internet2.middleware.grouper.ui.util.GroupAsMap;
import edu.internet2.middleware.grouper.ui.util.StemAsMap;
import edu.internet2.middleware.grouper.ui.util.SubjectAsMap;

/**
 * Default implementation of the Grouper TemplateResolver interface. Deals with
 * groups, subjects, stems and Collections. GroupFields will be added. New
 * objectTypes can be supported by extending this Class
 * <p />
 * 
 * @author Gary Brown.
 * @version $Id: DefaultTemplateResolverImpl.java,v 1.4 2005/07/28 10:10:36
 *          isgwb Exp $
 */
public class DefaultTemplateResolverImpl implements TemplateResolver {
	private Map cache = new HashMap();

	/**
	 * Default constructor
	 */
	public DefaultTemplateResolverImpl() {
		super();
		// TODO Auto-generated constructor stub
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.internet2.middleware.grouper.ui.TemplateResolver#getObjectType(java.lang.Object)
	 */
	public String getObjectType(Object obj) {
		Class claz = obj.getClass();
		Method method = null;
		String objType = null;
		try {
			//
			method = claz.getMethod("getObjectType", new Class[] {});
			objType = (String) method.invoke(obj, new Object[] {});
		} catch (NoSuchMethodException e) {
			if (obj instanceof Collection)
				return "Collection";
			objType = claz.getName().substring(
					claz.getName().lastIndexOf(".") + 1);
		} catch (IllegalAccessException e) {
			objType = "Unknown";
		} catch (InvocationTargetException e) {
			objType = "Unknown";
		}
		return objType;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.internet2.middleware.grouper.ui.TemplateResolver#getTemplateName(java.lang.Object,
	 *      java.lang.String, java.util.ResourceBundle,
	 *      javax.servlet.http.HttpServletRequest)
	 */
	public String getTemplateName(Object obj, String view,
			ResourceBundle bundle, HttpServletRequest request) {
		String templateName = null;
		String objectType = getObjectType(obj);

		Class claz = this.getClass();
		Method method = (Method) cache.get(objectType);
		if (method == null) {
			try {
				method = claz
						.getMethod("get" + objectType + "TemplateName",
								new Class[] { Object.class, view.getClass(),
										ResourceBundle.class,
										HttpServletRequest.class });
				cache.put(objectType, method);
			} catch (Exception e) {
				throw new IllegalArgumentException(claz.getName()
						+ " does not know how to find templates for "
						+ obj.getClass().getName());
			}
		}
		try {
			templateName = (String) method.invoke(this, new Object[] { obj,
					view, bundle, request });
		} catch (Exception e) {
			throw new IllegalArgumentException(
					"Problem executing code to find template name for "
							+ obj.getClass().getName() + ":" + e.getMessage());
		}
		return templateName;
	}

	//Here so we don't catch MissingResource exceptions in code and can be sure
	// of null or a value
	protected String getResource(ResourceBundle bundle, String key) {
		String val = null;
		try {
			val = bundle.getString(key);
			if ("".equals(val))
				val = null;

		} catch (Exception e) {

		}
		if (val != null)
			UIThreadLocal.put("lastDynamicTemplateKey", key);
		return val;
	}

	//Following should be protected / private - need to check if I can
	//use introspection to find and invoke them if they are not public
	//The first attempt failed

	/**
	 * Depending on the Group type may want to have different template. Look for
	 * more specific keys first: 
	 * <ul>
	 *     <li>groupType.&lt;type&gt;.view.&lt;view&gt;</li>
	 *     <li>groupType.&lt;type&gt;.view.default</li>
	 *     <li>groupType.view.&lt;view&gt;</li>
	 *     <li>groupType.view.default</li>
	 * </ul>
	 * 
	 * @param object to find template for 
	 * @param view name of template to find
	 * @param mediaResources ResourceBundle containing template names and values
	 * @param request HttpServletRequest 
	 * @return name of template
	 *  
	 */
	public String getGrouperGroupTemplateName(Object obj, String view,
			ResourceBundle mediaResources, HttpServletRequest request) {
		GroupAsMap group = (GroupAsMap) obj;
		String tName = null;
		String groupType = (String) group.get("type");
		if (groupType == null || "".equals(groupType)) {
			groupType = Grouper.DEF_GROUP_TYPE;
		}

		tName = getResource(mediaResources, "groupType." + groupType + ".view."
				+ view);

		if (tName == null) {
			tName = getResource(mediaResources, "groupType." + groupType
					+ ".view.default");
		}

		if (tName == null) {
			tName = getResource(mediaResources, "groupType.view." + view);
		}

		if (tName == null) {
			tName = getResource(mediaResources, "groupType.view.default");
		}
		return tName;
	}

	/**
	 * Get template name for a stem. Look for:
	 * <ul>
	 *     <li>stem.view.&lt;view&gt;</li>
	 *     <li>stem.view.default</li>
	 * </ul>
	 * 
	 * @param object to find template for 
	 * @param view name of template to find
	 * @param mediaResources ResourceBundle containing template names and values
	 * @param request HttpServletRequest 
	 * @return name of template
	 */

	public String getGrouperStemTemplateName(Object obj, String view,
			ResourceBundle mediaResources, HttpServletRequest request) {

		StemAsMap group = (StemAsMap) obj;
		String tName = null;
		String groupType = (String) group.get("type");
		if (groupType == null || "".equals(groupType)) {
			groupType = Grouper.DEF_GROUP_TYPE;
		}

		tName = getResource(mediaResources, "stem.view." + view);

		if (tName == null) {
			tName = getResource(mediaResources, "stem.view.default");
		}

		return tName;
	}

	/*
	 * Group fields may be displayed differently depending on group type. Group
	 * field values may be edited - so different defaults applicable hence
	 * 'prefix' which is determined as view or edit Look for more specific keys
	 * first: groupField. <field>.groupType. <type>. <prefix>. <view>
	 * groupField. <field>.groupType. <type>. <prefix>.default groupField.
	 * <field>. <prefix>. <view> groupField. <field>. <prefix>.default
	 * groupField. <prefix>.default - Grouper may only set this
	 * 
	 * 
	 * protected String getGroupFieldTemplateName(Map groupField,String view,
	 * ResourceBundle mediaResources,HttpServletRequest request){ String tName =
	 * null; String groupType = (String) groupField.get("type");
	 * 
	 * if(groupType==null || "".equals(groupType)) { groupType="base"; } String
	 * prefix = ".view.";
	 * 
	 * if(view.startsWith("edit.")) prefix = ".";
	 * 
	 * String field = (String)groupField.get("groupField");
	 * 
	 * tName=getResource(mediaResources,"groupField." + field + ".groupType." +
	 * groupType + prefix + view);
	 * 
	 * if(tName==null) { tName=getResource(mediaResources,"groupField." + field +
	 * ".groupType." + groupType + prefix + "default"); }
	 * 
	 * if(tName==null) { tName=getResource(mediaResources,"groupField." + field +
	 * prefix + view); }
	 * 
	 * if(tName==null) { tName=getResource(mediaResources,"groupField." + field +
	 * prefix + "default"); }
	 * 
	 * if(tName==null) { tName=getResource(mediaResources,"groupField" + prefix +
	 * "default"); }
	 * 
	 * return tName; }
	 */

	/**
	 * Depending on the subject type may want to have a different template. The
	 * same subject type may be retrieved by different Sources, and may have
	 * different attributes available.
	 * 
	 * Look for more specific keys first. If a value=* then don`t use the specific 
	 * default - try the next key instead/ 
	 * <ul><li>&lt;source&gt;.subjectType.&lt;type&gt;.view.&lt;view&gt;</li>
	 *     <li>&lt;source&gt;.subjectType.&lt;type&gt;.view.default</li>
	 *     <li>&lt;source&gt;.view.&lt;view&gt;</li>
	 *     <li>&lt;source&gt;.view.default</li>
	 *     <li>subjectType.&lt;type&gt;.view.&lt;view&gt;</li>
	 *     <li>subjectType.&lt;type&lt;.view.default</li>
	 *     <li>subject.view.&lt;view&gt;</li>
	 *     <li>subject.view.default</li>
	 * </ul>
	 * 
	 * @param object to find template for 
	 * @param view name of template to find
	 * @param mediaResources ResourceBundle containing template names and values
	 * @param request HttpServletRequest 
	 * @return name of template
	 *  
	 */
	public String getI2miSubjectTemplateName(Object object, String view,
			ResourceBundle mediaResources, HttpServletRequest request) {
		SubjectAsMap subject = (SubjectAsMap) object;
		String tName = null;
		String source = (String) subject.get("source");
		String subjectType = (String) subject.get("subjectType");

		tName = getResource(mediaResources, source + ".subjectType."
				+ subjectType + ".view." + view);

		if (tName == null) {
			tName = getResource(mediaResources, source + ".subjectType."
					+ subjectType + ".view.default");
		}

		if (tName == null) {
			tName = getResource(mediaResources, source + ".view." + view);
		}
		if (tName == null) {
			tName = getResource(mediaResources, source + ".view.default");
		}

		if (tName == null || tName.equals("*")) {//ignore Source
			tName = getResource(mediaResources, "subjectType." + subjectType
					+ ".view." + view);
		}
		if (tName == null || tName.equals("*")) {//use default for type
			tName = getResource(mediaResources, "subjectType." + subjectType
					+ ".view.default");
		}
		if (tName == null || tName.equals("*")) {//Ignore type
			tName = getResource(mediaResources, "subject.view." + view);
		}
		if (tName == null) {
			tName = getResource(mediaResources, "subject.view.default");
		}
		return tName;
	}

	/**
	 * Get template name for provided Collection and view. If none specified use
	 * default.
	 * <ul>
	 *     <li>list.view.&lt;view&gt;</li>
	 *     <li>list.view.default</li>
	 * </ul>
	 * 
	 * @param object to find template for 
	 * @param view name of template to find
	 * @param mediaResources ResourceBundle containing template names and values
	 * @param request HttpServletRequest 
	 * @return name of template
	 */

	public String getCollectionTemplateName(Object object, String view,
			ResourceBundle mediaResources, HttpServletRequest request) {
		Collection c = (Collection) object;
		String tName = null;

		tName = getResource(mediaResources, "list.view." + view);

		if (tName == null) {
			tName = getResource(mediaResources, "list.view.default");
		}

		return tName;
	}
}