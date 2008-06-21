package edu.internet2.middleware.grouper.annotations;

import static java.lang.annotation.ElementType.FIELD;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * Marker annotation applied to fields which should not have field constants
 * generated through GenerateFieldConstants
 * 
 * @version $Id: GrouperIngoreFieldConstant.java,v 1.2 2008-06-21 04:16:13 mchyzer Exp $
 * @author mchyzer
 */
@Target(FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface GrouperIngoreFieldConstant{

}
