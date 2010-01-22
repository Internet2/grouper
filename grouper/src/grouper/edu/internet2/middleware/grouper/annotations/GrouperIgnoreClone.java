package edu.internet2.middleware.grouper.annotations;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.TYPE;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * Marker annotation applied to fields which should not be included in clone method
 * 
 * @version $Id: GrouperIgnoreClone.java,v 1.1 2008-07-11 05:11:28 mchyzer Exp $
 * @author mchyzer
 */
@Target({FIELD, TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface GrouperIgnoreClone{

}
