package edu.internet2.middleware.grouper.hooks.examples;

import edu.internet2.middleware.grouper.hooks.AttributeAssignValueHooks;
import edu.internet2.middleware.grouper.hooks.beans.HooksAttributeAssignValueBean;
import edu.internet2.middleware.grouper.hooks.beans.HooksContext;

/**
 * grouper.properties:
 * hooks.attributeAssignValue.class=edu.internet2.middleware.grouper.hooks.examples.LoaderAttributeValueVetoHook
 * 
 */
public class LoaderAttributeValueVetoHook extends AttributeAssignValueHooks {

  @Override
  public void attributeAssignValuePreUpdate(HooksContext hooksContext,
      HooksAttributeAssignValueBean hooksAttributeAssignValueBean) {
    if (hooksAttributeAssignValueBean.getAttributeAssignValue() == null || hooksAttributeAssignValueBean.getAttributeAssignValue().getAttributeAssign() == null) {
      return;
    }
    LoaderAttributeVetoHook.validateAttributeDefNameId(hooksContext, hooksAttributeAssignValueBean.getAttributeAssignValue().getAttributeAssign().getAttributeDefNameId());

  }

  @Override
  public void attributeAssignValuePreInsert(HooksContext hooksContext,
      HooksAttributeAssignValueBean hooksAttributeAssignValueBean) {
    if (hooksAttributeAssignValueBean.getAttributeAssignValue() == null || hooksAttributeAssignValueBean.getAttributeAssignValue().getAttributeAssign() == null) {
      return;
    }
    LoaderAttributeVetoHook.validateAttributeDefNameId(hooksContext, hooksAttributeAssignValueBean.getAttributeAssignValue().getAttributeAssign().getAttributeDefNameId());
  }

  @Override
  public void attributeAssignValuePreDelete(HooksContext hooksContext,
      HooksAttributeAssignValueBean hooksAttributeAssignValueBean) {
    if (hooksAttributeAssignValueBean.getAttributeAssignValue() == null || hooksAttributeAssignValueBean.getAttributeAssignValue().getAttributeAssign() == null) {
      return;
    }
    LoaderAttributeVetoHook.validateAttributeDefNameId(hooksContext, hooksAttributeAssignValueBean.getAttributeAssignValue().getAttributeAssign().getAttributeDefNameId());
  }


}
