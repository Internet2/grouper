/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.xml.export;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.javabean.JavaBeanConverter;
import com.thoughtworks.xstream.io.xml.XppDriver;


/**
 * utils about xml export
 */
public class XmlExportUtils {

  /**
   * @return xstream
   */
  public static XStream xstream() {

    final XStream xStream = new XStream(new XppDriver());
    
    //do javabean properties, not fields
    xStream.registerConverter(new JavaBeanConverter(xStream.getMapper()) {

      /**
       * @see com.thoughtworks.xstream.converters.javabean.JavaBeanConverter#canConvert(java.lang.Class)
       */
      @SuppressWarnings("unchecked")
      @Override
      public boolean canConvert(Class type) {
        //see if one of our beans
        return type.getName().startsWith("edu.internet2");
      }
      
    }); 

    registerClass(xStream, XmlExportAttribute.class);
    registerClass(xStream, XmlExportAttributeDef.class);
    registerClass(xStream, XmlExportComposite.class);
    registerClass(xStream, XmlExportField.class);
    registerClass(xStream, XmlExportGroup.class);
    registerClass(xStream, XmlExportGroupType.class);
    registerClass(xStream, XmlExportGroupTypeTuple.class);
    registerClass(xStream, XmlExportMember.class);
    registerClass(xStream, XmlExportMembership.class);
    registerClass(xStream, XmlExportStem.class);
    return xStream;
  }

  /**
   * 
   * @param xStream
   * @param theClass
   */
  private static void registerClass(XStream xStream, Class<?> theClass) {
    xStream.alias(theClass.getSimpleName(), theClass);
  }
}
