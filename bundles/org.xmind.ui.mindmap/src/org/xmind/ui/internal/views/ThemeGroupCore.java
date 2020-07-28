package org.xmind.ui.internal.views;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.osgi.framework.Bundle;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xmind.core.style.IStyle;
import org.xmind.core.style.IStyleSheet;
import org.xmind.ui.internal.MindMapMessages;
import org.xmind.ui.mindmap.IResourceManager;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.util.Logger;
import org.xmind.ui.util.XMLUtils;
import org.xml.sax.SAXException;

public class ThemeGroupCore {

    private ThemeGroupCore() {
    };

    private static ThemeGroupCore instance = null;

    public class CategorizedThemeGroup {
        List<IStyle> items = new ArrayList<IStyle>();

        private String name;

        private String id;

        public CategorizedThemeGroup(String id, String name,
                List<IStyle> items) {
            this.id = id;
            this.name = name;
            this.items = items;
        }

        public String getName() {
            return name;
        }

        public List<IStyle> getItems() {
//            List<IStyle> list = new ArrayList<IStyle>(items);

//            for (IStyle style : items) {
//                if (!this.equals(defaultThemeGroup)
//                        && style.getId().equals(getDefaultThemeId())) {
////                    list.remove(style);
//                    return list;
//                }
//            }
            return items;
        }

        public String getId() {
            return id;
        }

        public int hashCode() {
            return id.hashCode();
        }

        public boolean equals(Object obj) {
            if (obj == this)
                return true;
            if (obj == null || !(obj instanceof CategorizedThemeGroup))
                return false;
            if (((CategorizedThemeGroup) obj).id.equals(this.id))
                return true;
            return false;
        }

    }

    private static final String GROUP_ID = "id"; //$NON-NLS-1$

    private static final String GROUP_NAME = "name"; //$NON-NLS-1$

    private static final String THEME_ID = "id"; //$NON-NLS-1$

    private static final String THEME_GROUP = "theme-group"; //$NON-NLS-1$

    private static final String THEME_ELEMENT = "theme"; //$NON-NLS-1$

    private static final String DEFAULT_GROUP_ID = "default"; //$NON-NLS-1$

    private static final String USER_GROUP_ID = "user";//$NON-NLS-1$

    private static final String PATH_STYLES = "styles/"; //$NON-NLS-1$

    private static final String THEME_GROUP_XML = "themeGroups.xml"; //$NON-NLS-1$

    private static final String THEME_PROPERTIES = "themeGroups.properties"; //$NON-NLS-1$

    private List<CategorizedThemeGroup> systemGroups = null;

    private Properties properties = null;

    private CategorizedThemeGroup defaultThemeGroup = null;

    public List<CategorizedThemeGroup> getThemeGroups() {

        ArrayList<CategorizedThemeGroup> list = new ArrayList<CategorizedThemeGroup>();
        list.add(getDefaultGroup());
        list.addAll(getSystemGroups());
        CategorizedThemeGroup group = getUserGroup();
        if (group.getItems() != null && !group.getItems().isEmpty())
            list.add(group);

        return list;
    }

    private CategorizedThemeGroup getUserGroup() {
        IResourceManager rm = MindMapUI.getResourceManager();
        Set<IStyle> userThemeSets = rm.getUserThemeSheet()
                .getStyles(IStyleSheet.MASTER_STYLES);
        List<IStyle> userThemeList = new ArrayList<IStyle>();
        Iterator<IStyle> iterUserTheme = userThemeSets.iterator();

        while (iterUserTheme.hasNext()) {
            IStyle userStyle = iterUserTheme.next();
//            if (!userStyle.getId().equals(getDefaultThemeId())) {
            userThemeList.add(userStyle);
//            }
        }
        CategorizedThemeGroup userGroup = new CategorizedThemeGroup(
                USER_GROUP_ID, MindMapMessages.ThemeGroupCore_UserGroup_name,
                userThemeList);
        return userGroup;
    }

    private List<CategorizedThemeGroup> getSystemGroups() {
        if (systemGroups == null)
            systemGroups = createSystemGroups();
        return systemGroups;
    }

    private List<CategorizedThemeGroup> createSystemGroups() {
        IResourceManager rm = MindMapUI.getResourceManager();
        Set<IStyle> systemThemeSets = rm.getSystemThemeSheet()
                .getStyles(IStyleSheet.MASTER_STYLES);
        Bundle bundle = Platform.getBundle(MindMapUI.PLUGIN_ID);
        String path = PATH_STYLES + THEME_GROUP_XML;
        DocumentBuilder documentBuilder = null;
        List<CategorizedThemeGroup> systemGroups = new ArrayList<CategorizedThemeGroup>();
        try {
            documentBuilder = XMLUtils.getDefaultDocumentBuilder();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }

        try {

            URL url = FileLocator.find(bundle, new Path(path), null);
            Document doc = documentBuilder.parse(url.openStream());
            NodeList nodeList = doc.getElementsByTagName(THEME_GROUP);
            for (int i = 0; i < nodeList.getLength(); i++) {
                Element ele = (Element) nodeList.item(i);
                String groupId = ele.getAttribute(GROUP_ID);
                String groupName = ele.getAttribute(GROUP_NAME).substring(1);
                groupName = getProperties().getProperty(groupName);
                NodeList themeList = ele.getElementsByTagName(THEME_ELEMENT);
                List<IStyle> themeGroupList = new ArrayList<IStyle>();
                for (int j = 0; j < themeList.getLength(); j++) {
                    Element theme = (Element) themeList.item(j);
                    String themeId = theme.getAttribute(THEME_ID);
                    Iterator<IStyle> iterSystemTheme = systemThemeSets
                            .iterator();
                    while (iterSystemTheme.hasNext()) {
                        IStyle themeStyle = iterSystemTheme.next();
                        if (themeId.equals(themeStyle.getId())) {
                            themeGroupList.add(themeStyle);
                            systemThemeSets.remove(themeStyle);
                            break;
                        }
                    }
                }
                CategorizedThemeGroup themeGroup = new CategorizedThemeGroup(
                        groupId, groupName, themeGroupList);
                systemGroups.add(themeGroup);
            }
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return systemGroups;

    }

    private CategorizedThemeGroup getDefaultGroup() {
        IResourceManager rm = MindMapUI.getResourceManager();
        IStyle defaultTheme = rm.getDefaultTheme();
        List<IStyle> defaultThemeList = new ArrayList<IStyle>();
        defaultThemeList.add(defaultTheme);
        defaultThemeGroup = new CategorizedThemeGroup(DEFAULT_GROUP_ID,
                MindMapMessages.ThemeGroupCore_DefaultGroup_name,
                defaultThemeList);

        return defaultThemeGroup;
    }

    public Properties getProperties() {
        if (properties == null) {
            properties = loadProperties();
        }
        return properties;
    }

    private Properties loadProperties() {
        Bundle bundle = Platform.getBundle(MindMapUI.PLUGIN_ID);
        URL propertiesUrl = FileLocator.find(bundle,
                new Path(PATH_STYLES + THEME_PROPERTIES), null);
        Properties properties = new Properties();
        InputStream stream = null;
        try {
            stream = propertiesUrl.openStream();
            properties.load(stream);
        } catch (IOException e) {
            Logger.log(e, "Failed to load default properties file from: " //$NON-NLS-1$
                    + THEME_PROPERTIES);
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return properties;
    }

    public static ThemeGroupCore getInstance() {
        if (instance == null) {
            instance = new ThemeGroupCore();
        }
        return instance;
    }

}
