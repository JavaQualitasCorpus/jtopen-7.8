///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: HTMLTableCellBeanInfo.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.util.html;

import java.awt.Image;
import java.beans.SimpleBeanInfo;
import java.beans.PropertyDescriptor;
import java.beans.EventSetDescriptor;
import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.BeanInfo;

/**
The HTMLTableCellBeanInfo class provides
bean information for the HTMLTableCell class.
**/
public class HTMLTableCellBeanInfo extends SimpleBeanInfo
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

    // Class this bean info represents.
    private final static Class beanClass = HTMLTableCell.class;

    // Handles loading the appropriate resource bundle
    private static ResourceBundleLoader_h loader_;

    private static EventSetDescriptor[] events_;
    private static PropertyDescriptor[] properties_;

    static
    {
      try
      {
        EventSetDescriptor changed = new EventSetDescriptor(beanClass,
                         "propertyChange",
                         java.beans.PropertyChangeListener.class,
                         "propertyChange");
        changed.setDisplayName(loader_.getText("EVT_NAME_PROPERTY_CHANGE"));
        changed.setShortDescription(loader_.getText("EVT_DESC_PROPERTY_CHANGE"));

        EventSetDescriptor veto = new EventSetDescriptor(beanClass,
        		"propertyChange",
                         java.beans.VetoableChangeListener.class,
                         "vetoableChange");
        veto.setDisplayName(loader_.getText("EVT_NAME_PROPERTY_VETO"));
        veto.setShortDescription(loader_.getText("EVT_DESC_PROPERTY_VETO"));

        EventSetDescriptor[] events = {changed, veto};

        events_ = events;

        // ***** PROPERTIES
        PropertyDescriptor columnSpan = new PropertyDescriptor("columnSpan", beanClass,
                                        "getColumnSpan", "setColumnSpan");
        columnSpan.setBound(true);
        columnSpan.setConstrained(true);
        columnSpan.setDisplayName(loader_.getText("PROP_NAME_CSPAN"));
        columnSpan.setShortDescription(loader_.getText("PROP_DESC_CSPAN"));

        PropertyDescriptor height = new PropertyDescriptor("height", beanClass, "getHeight", "setHeight");
        height.setBound(true);
        height.setConstrained(true);
        height.setDisplayName(loader_.getText("PROP_NAME_HEIGHT"));
        height.setShortDescription(loader_.getText("PROP_HTCELL_DESC_HEIGHT"));
      
        PropertyDescriptor heightInPercent = new PropertyDescriptor("heightInPercent", beanClass,
                                        "isHeightInPercent", "setHeightInPercent");
        heightInPercent.setBound(true);
        heightInPercent.setConstrained(true);
        heightInPercent.setDisplayName(loader_.getText("PROP_NAME_HPERCENT"));
        heightInPercent.setShortDescription(loader_.getText("PROP_DESC_HPERCENT"));

        PropertyDescriptor horizontalAlignment = new PropertyDescriptor("horizontalAlignment", beanClass,
                                        "getHorizontalAlignment", "setHorizontalAlignment");
        horizontalAlignment.setBound(true);
        horizontalAlignment.setConstrained(true);
        horizontalAlignment.setDisplayName(loader_.getText("PROP_NAME_HALIGN"));
        horizontalAlignment.setShortDescription(loader_.getText("PROP_HTCELL_DESC_HALIGN"));

        PropertyDescriptor rowSpan = new PropertyDescriptor("rowSpan", beanClass,
                                        "getRowSpan", "setRowSpan");
        rowSpan.setBound(true);
        rowSpan.setConstrained(true);
        rowSpan.setDisplayName(loader_.getText("PROP_NAME_RSPAN"));
        rowSpan.setShortDescription(loader_.getText("PROP_DESC_RSPAN"));

        PropertyDescriptor verticalAlignment = new PropertyDescriptor("verticalAlignment", beanClass,
                                        "getVerticalAlignment", "setVerticalAlignment");
        verticalAlignment.setBound(true);
        verticalAlignment.setConstrained(true);
        verticalAlignment.setDisplayName(loader_.getText("PROP_NAME_VALIGN"));
        verticalAlignment.setShortDescription(loader_.getText("PROP_HTCELL_DESC_VALIGN"));

        PropertyDescriptor width = new PropertyDescriptor("width", beanClass,
                                        "getWidth", "setWidth");
        width.setBound(true);
        width.setConstrained(true);
        width.setDisplayName(loader_.getText("PROP_NAME_WIDTH"));
        width.setShortDescription(loader_.getText("PROP_HTCELL_DESC_WIDTH"));

        PropertyDescriptor widthInPercent = new PropertyDescriptor("widthInPercent", beanClass, "isWidthInPercent", "setWidthInPercent");
        widthInPercent.setBound(true);
        widthInPercent.setConstrained(true);
        widthInPercent.setDisplayName(loader_.getText("PROP_NAME_WPERCENT"));
        widthInPercent.setShortDescription(loader_.getText("PROP_HTCELL_DESC_WPERCENT"));

        PropertyDescriptor wrap = new PropertyDescriptor("wrap", beanClass, "isWrap", "setWrap");
        wrap.setBound(true);
        wrap.setConstrained(true);
        wrap.setDisplayName(loader_.getText("PROP_NAME_WRAP"));
        wrap.setShortDescription(loader_.getText("PROP_DESC_WRAP"));

        PropertyDescriptor lang = new PropertyDescriptor("lang", beanClass, "getLanguage", "setLanguage");   //$B1A
        lang.setBound(true);                                                                                 //$B1A
        lang.setConstrained(true);                                                                           //$B1A
        lang.setDisplayName(loader_.getText("PROP_NAME_LANGUAGE"));                                          //$B1A
        lang.setShortDescription(loader_.getText("PROP_DESC_LANGUAGE"));                                     //$B1A
        
        PropertyDescriptor dir = new PropertyDescriptor("dir", beanClass, "getDirection", "setDirection");   //$B1A
        dir.setBound(true);                                                                                  //$B1A
        dir.setConstrained(true);                                                                            //$B1A
        dir.setDisplayName(loader_.getText("PROP_NAME_DIRECTION"));                                          //$B1A
        dir.setShortDescription(loader_.getText("PROP_DESC_DIRECTION"));                                     //$B1A

        PropertyDescriptor useFO = new PropertyDescriptor("useFO", beanClass, "isUseFO", "setUseFO");           //@C1A
        useFO.setBound(true);                                                                                   //@C1A
        useFO.setConstrained(false);                                                                            //@C1A
        useFO.setDisplayName(loader_.getText("PROP_NAME_FORMATTING_OBJECT"));                                   //@C1A
        useFO.setShortDescription(loader_.getText("PROP_DESC_FORMATTING_OBJECT"));                              //@C1A

        
        properties_ = new PropertyDescriptor[] { columnSpan, height, heightInPercent, horizontalAlignment, rowSpan, 
                                                 verticalAlignment, width, widthInPercent, wrap, lang, dir, useFO };        //$B1C           //@C1C
        
      }
      catch (Exception e)
      {         
         throw new Error(e.toString());
      }
    }


    /**
     * Returns the BeanInfo for the superclass of this bean.  Since
     * HTMLTableCell is a subclass of HTMLTagAttributes, this method
     * will return a HTMLTagAttributesBeanInfo object.
     *
     * @return BeanInfo[] containing this bean's superclass BeanInfo
     **/
    public BeanInfo[] getAdditionalBeanInfo()                            // @Z1A
    {
       return new BeanInfo[] { new HTMLTagAttributesBeanInfo() };        
    }


    /**
    Returns the bean descriptor.
      @return The bean descriptor.
    **/
    public BeanDescriptor getBeanDescriptor()
    {
        return new BeanDescriptor(beanClass);
    }


    /**
    Returns the index of the default event.
      @return The index to the default event.
    **/
    public int getDefaultEventIndex()
    {
        return 0;
    }

    /**
      Returns the index of the default property.
      @return The index to the default property.
    **/
    public int getDefaultPropertyIndex()
    {
        return 0;
    }

    /**
      Returns the descriptors for all events.
      @return The descriptors for all events.
    **/
    public EventSetDescriptor[] getEventSetDescriptors()
    {
        return events_;
    }

    /**
      Returns an image for the icon.

      @param icon    The icon size and color.
      @return        The image.
    **/
    public Image getIcon (int icon)
    {
        Image image = null;
        switch (icon) {
            case BeanInfo.ICON_MONO_16x16:
            case BeanInfo.ICON_COLOR_16x16:
                image = loadImage ("HTMLTableCell16.gif");
                break;
            case BeanInfo.ICON_MONO_32x32:
            case BeanInfo.ICON_COLOR_32x32:
                image = loadImage ("HTMLTableCell32.gif");
                break;
        }
        return image;
    }
    
    /**
    Returns the descriptors for all properties.
      @return The descriptors for all properties.
    **/
    public PropertyDescriptor[] getPropertyDescriptors()
    {
        return properties_;
    }

}
