package edu.internet2.middleware.grouper.annotations;

import static java.lang.annotation.ElementType.FIELD;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * Marker annotation applied to fields which should not have field constants
 * generated through GenerateFieldConstants
 * 
 * @version $Id: GrouperIgnoreFieldConstant.java,v 1.1 2008-06-30 04:01:33 mchyzer Exp $
 * @author mchyzer
 */
@Target(FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface GrouperIgnoreFieldConstant{

}
