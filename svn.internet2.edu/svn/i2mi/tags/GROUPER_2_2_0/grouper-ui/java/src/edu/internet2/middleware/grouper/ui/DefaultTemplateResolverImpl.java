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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

import javax.servlet.http.HttpServletRequest;

import edu.internet2.middleware.grouper.Field;
import edu.internet2.middleware.grouper.GroupType;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.audit.AuditType;
import edu.internet2.middleware.grouper.ui.util.GroupAsMap;
import edu.internet2.middleware.grouper.ui.util.MembershipAsMap;
import edu.internet2.middleware.grouper.ui.util.ObjectAsMap;
import edu.internet2.middleware.grouper.ui.util.StemAsMap;
import edu.internet2.middleware.grouper.ui.util.SubjectAsMap;
import edu.internet2.middleware.grouper.ui.util.SubjectPrivilegeAsMap;
import edu.internet2.middleware.subject.Source;

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
			if (obj instanceof Field)
				return "Field";
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
	 *     <li>group.view.&lt;view&gt;</li>
	 *     <li>group.view.default</li>
	 * </ul>
	 * TODO groups can have multiple types - how best to cope?
	 * 
	 * @param obj object to find template for 
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

		if (tName == null) {
			tName = getResource(mediaResources, "group.view." + view);
		}

		if (tName == null) {
			tName = getResource(mediaResources, "group.view.default");
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
	 * @param obj object to find template for 
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
			//groupType = Grouper.DEF_GROUP_TYPE;
		}

		tName = getResource(mediaResources, "stem.view." + view);

		if (tName == null) {
			tName = getResource(mediaResources, "stem.view.default");
		}

		return tName;
	}
	
	/**
	 * Get template name for a Membership. Look for:
	 * <ul>
	 *     <li>membership.view.&lt;view&gt;</li>
	 *     <li>membership.view.default</li>
	 * </ul>
	 * 
	 * @param obj object to find template for 
	 * @param view name of template to find
	 * @param mediaResources ResourceBundle containing template names and values
	 * @param request HttpServletRequest 
	 * @return name of template
	 */

	public String getMembershipTemplateName(Object obj, String view,
			ResourceBundle mediaResources, HttpServletRequest request) {

		MembershipAsMap group = (MembershipAsMap) obj;
		String tName = null;
		

		tName = getResource(mediaResources, "membership.view." + view);

		if (tName == null) {
			tName = getResource(mediaResources, "membership.view.default");
		}

		return tName;
	}
	
	/**
	 * Find template for a group type. Look for
	 * more specific keys first: 
	 * <ul>
	 *     <li>groupType.&lt;name&gt;view.&lt;view&gt;</li>
	 *     <li>groupType.&lt;name&gt;view.default</li>
	 *     <li>groupType.view.&lt;view&gt;</li>
	 *     <li>groupType.view.default</li>
	 * </ul>
	 * 
	 * @param obj object to find template for 
	 * @param view name of template to find
	 * @param mediaResources ResourceBundle containing template names and values
	 * @param request HttpServletRequest 
	 * @return name of template
	 *  
	 */
	public String getGroupTypeTemplateName(Object obj, String view,
			ResourceBundle mediaResources, HttpServletRequest request) {

		GroupType type = (GroupType) obj;
		String tName = null;
		tName = getResource(mediaResources, "groupType." + type.getName() + ".view." + view);
		if (tName == null) {
			tName = getResource(mediaResources, "groupType." + type.getName() + ".view.default");
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
	 * Composites don't have types so simple lookup. Look for
	 * more specific keys first, then default: 
	 * <ul>
	 *     <li>composite.view.&lt;view&gt;</li>
	 *     <li>composite.view.default</li>
	 * </ul>
	 * 
	 * @param obj object to find template for 
	 * @param view name of template to find
	 * @param mediaResources ResourceBundle containing template names and values
	 * @param request HttpServletRequest 
	 * @return name of template
	 *  
	 */
	public String getCompositeTemplateName(Object obj, String view,
			ResourceBundle mediaResources, HttpServletRequest request) {

		ObjectAsMap comp = (ObjectAsMap) obj;
		String tName = null;
		
		tName = getResource(mediaResources, "composite.view." + view);
		

		if (tName == null) {
			tName = getResource(mediaResources, "composite.view.default");
		}

		return tName;
	}
	
	
	public String getAuditEntryTemplateName(Object obj, String view,
			ResourceBundle mediaResources, HttpServletRequest request) {

		ObjectAsMap entry = (ObjectAsMap) obj;
		AuditType auditType = (AuditType)entry.get("auditType");
		String type = auditType.getActionName() + "-" + auditType.getAuditCategory();
		String tName = null;
		
		tName = getResource(mediaResources, "auditEntry.view." + view + ".type." + type);
		
		if (tName == null) {
			tName = getResource(mediaResources, "auditEntry.view." + view);
		}
		
		if (tName == null) {
			tName = getResource(mediaResources, "auditEntry.view.default");
		}

		return tName;
	}
	
	/**
	 * Depending on the Field type (list or atribute) may want to have different template. Look for
	 * more specific keys first: 
	 * <ul>
	 *     <li>field.&lt;fieldName&gt;view.&lt;view&gt;</li>
	 *     <li>field.&lt;fieldName&gt;view.default</li>
	 * 	   <li>field.&lt;fieldType&gt;view.&lt;view&gt;</li>
	 *     <li>field.&lt;fieldType&gt;view.default</li>
	 *     <li>field.view.&lt;view&gt;</li>
	 *     <li>field.view.default</li>
	 * </ul>
	 * 
	 * @param obj object to find template for 
	 * @param view name of template to find
	 * @param mediaResources ResourceBundle containing template names and values
	 * @param request HttpServletRequest 
	 * @return name of template
	 *  
	 */
	public String getFieldTemplateName(Object obj, String view,
			ResourceBundle mediaResources, HttpServletRequest request) {

		Field field = (Field) obj;
		String tName = null;
		tName = getResource(mediaResources, "field." + field.getName() + ".view." + view);
		if (tName == null) {
			tName = getResource(mediaResources, "field." + field.getName() + ".view.default");
		}
		if (tName == null) {
			tName = getResource(mediaResources, "field." + field.getType() + ".view." + view);
		}
		if (tName == null) {
			tName = getResource(mediaResources, "field." + field.getType() + ".view.default");
		}
		if (tName == null) {
			tName = getResource(mediaResources, "field.view." + view);
		}

		if (tName == null) {
			tName = getResource(mediaResources, "field.view.default");
		}

		return tName;
	}
	
	/**
	 * 
	 * @param obj
	 * @param view
	 * @param mediaResources
	 * @param request
	 * @return name of template
	 */
  public String getAttributeDefNameTemplateName(Object obj, String view,
	      ResourceBundle mediaResources, HttpServletRequest request) {

	    AttributeDefName attr = (AttributeDefName) obj;
	    String tName = null;
	    tName = getResource(mediaResources, "field." + attr.getLegacyAttributeName(true) + ".view." + view);
	    if (tName == null) {
	      tName = getResource(mediaResources, "field." + attr.getLegacyAttributeName(true) + ".view.default");
	    }
	    if (tName == null) {
	      tName = getResource(mediaResources, "field.attribute.view." + view);
	    }
	    if (tName == null) {
	      tName = getResource(mediaResources, "field.attribute.view.default");
	    }
	    if (tName == null) {
	      tName = getResource(mediaResources, "field.view." + view);
	    }

	    if (tName == null) {
	      tName = getResource(mediaResources, "field.view.default");
	    }

	    return tName;
	  }
	

	/**
	 * Get template name for a SubjectPrivilege (type=access/naming). Look for:
	 * <ul>
	 *     <li>subjectprivilege.&lt;type&gt;.view.&lt;view&gt;</li>
	 *     <li>subjectprivilege.&lt;type&gt;.view.default</li>
	 *     <li>subjectprivilege.view.&lt;view&gt;</li>
	 *     <li>subjectprivilege.view.default</li>
	 * </ul>
	 * 
	 * @param obj object to find template for 
	 * @param view name of template to find
	 * @param mediaResources ResourceBundle containing template names and values
	 * @param request HttpServletRequest 
	 * @return name of template
	 */

	public String getSubjectPrivilegeTemplateName(Object obj, String view,
			ResourceBundle mediaResources, HttpServletRequest request) {

		SubjectPrivilegeAsMap sPriv = (SubjectPrivilegeAsMap) obj;
		String tName = null;
		tName = getResource(mediaResources, "subjectprivilege." + sPriv.get("type") + ".view." + view);
		if(tName==null){
			tName = getResource(mediaResources, "subjectprivilege." + sPriv.get("type") + ".view.default");
		}
		if(tName==null){
			tName = getResource(mediaResources, "subjectprivilege.view." + view);
		}

		if (tName == null) {
			tName = getResource(mediaResources, "subjectprivilege.view.default");
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
		Source so = (Source)subject.get("source");
		String source = so.getId();
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
