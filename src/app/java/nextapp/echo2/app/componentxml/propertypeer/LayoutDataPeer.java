package nextapp.echo2.app.componentxml.propertypeer;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Iterator;

import nextapp.echo2.app.LayoutData;
import nextapp.echo2.app.Style;
import nextapp.echo2.app.componentxml.ComponentIntrospector;
import nextapp.echo2.app.componentxml.ComponentXmlException;
import nextapp.echo2.app.componentxml.InvalidPropertyException;
import nextapp.echo2.app.componentxml.PropertyLoader;
import nextapp.echo2.app.componentxml.PropertyXmlPeer;
import nextapp.echo2.app.util.DomUtil;

import org.w3c.dom.Element;

/**
 * 
 */
public class LayoutDataPeer 
implements PropertyXmlPeer {
    
    //BUGBUG. cleanup.
    //BUGBUG. indexed properties.
    
    /**
     * @see nextapp.echo2.app.componentxml.PropertyXmlPeer#getValue(java.lang.ClassLoader, 
     *      java.lang.Class, org.w3c.dom.Element)
     */
    public Object getValue(ClassLoader classLoader, Class objectClass, Element propertyElement)
    throws InvalidPropertyException {
        try {
            Element layoutDataElement = DomUtil.getChildElementByTagName(propertyElement, "layoutdata");
            String type = layoutDataElement.getAttribute("type");

            // Load properties from XML.
            PropertyLoader propertyLoader = PropertyLoader.forClassLoader(classLoader);
            Element propertiesElement = DomUtil.getChildElementByTagName(layoutDataElement, "properties");
            Style propertyStyle = propertyLoader.getProperties(propertiesElement, type);
            
            // Instantiate object.
            Class propertyClass = Class.forName(type, true, classLoader);
            LayoutData layoutData = (LayoutData) propertyClass.newInstance();
            
            ComponentIntrospector ci = ComponentIntrospector.forName(type, classLoader);

            //BUGBUG. probably want to move the real work here into something generic in the componentxml package.
            Iterator it = propertyStyle.getPropertyNames();
            while (it.hasNext()) {
                String propertyName = (String) it.next();
                Method writeMethod = ci.getWriteMethod(propertyName);
                writeMethod.invoke(layoutData, new Object[]{propertyStyle.getProperty(propertyName)});
            }
            
            return layoutData;
        } catch (ClassNotFoundException ex) {
            throw new InvalidPropertyException("Unable to process properties.", ex);
        } catch (ComponentXmlException ex) {
            throw new InvalidPropertyException("Unable to process properties.", ex);
        } catch (InstantiationException ex) {
            throw new InvalidPropertyException("Unable to process properties.", ex);
        } catch (IllegalAccessException ex) {
            throw new InvalidPropertyException("Unable to process properties.", ex);
        } catch (IllegalArgumentException ex) {
            throw new InvalidPropertyException("Unable to process properties.", ex);
        } catch (InvocationTargetException ex) {
            throw new InvalidPropertyException("Unable to process properties.", ex);
        }
    }

}
