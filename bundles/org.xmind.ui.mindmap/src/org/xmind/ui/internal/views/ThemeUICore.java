package org.xmind.ui.internal.views;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
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

/**
 * @author Ren Siu
 * @since 3.6.50
 */
public class ThemeUICore {

    protected class ThemeUIGroup {

        private String name;

        private String id;

        private IStyle[] items;

        public ThemeUIGroup(String id, String name, IStyle[] items) {
            this.id = id;
            this.name = name;
            this.items = items;
        }

        public String getName() {
            return name;
        }

        public IStyle[] getItems() {
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
            if (obj == null || !(obj instanceof ThemeUIGroup))
                return false;
            if (((ThemeUIGroup) obj).id.equals(this.id))
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

    private static ThemeUICore instance = null;

    private List<ThemeUIGroup> systemGroups = null;

    private Properties properties = null;

    private ThemeUICore() {
    }

    public List<ThemeUIGroup> getThemeGroups() {
        ArrayList<ThemeUIGroup> list = new ArrayList<ThemeUIGroup>();
        ThemeUIGroup defaultGroup = getDefaultGroup();
        if (defaultGroup != null && defaultGroup.getItems().length != 0) {
            list.add(defaultGroup);
        }

        list.addAll(getSystemGroups());

        ThemeUIGroup userGroup = getUserGroup();
        if (userGroup != null && userGroup.getItems().length != 0) {
            list.add(userGroup);
        }

        return list;
    }

    private ThemeUIGroup getUserGroup() {
        IResourceManager rm = MindMapUI.getResourceManager();
        Set<IStyle> userThemeSets = rm.getUserThemeSheet()
                .getStyles(IStyleSheet.MASTER_STYLES);
        ThemeUIGroup userGroup = new ThemeUIGroup(USER_GROUP_ID,
                MindMapMessages.ThemeUICore_group_user_name,
                userThemeSets.toArray(new IStyle[userThemeSets.size()]));
        return userGroup;
    }

    private List<ThemeUIGroup> getSystemGroups() {
        if (systemGroups == null)
            systemGroups = createSystemGroups();
        return systemGroups;
    }

    private List<ThemeUIGroup> createSystemGroups() {
        IResourceManager rm = MindMapUI.getResourceManager();
        IStyleSheet sts = rm.getSystemThemeSheet();
        Bundle bundle = Platform.getBundle(MindMapUI.PLUGIN_ID);
        String path = PATH_STYLES + THEME_GROUP_XML;
        DocumentBuilder documentBuilder = null;
        List<ThemeUIGroup> systemGroups = new ArrayList<ThemeUIGroup>();
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
                List<IStyle> themes = new ArrayList<IStyle>();
                for (int j = 0; j < themeList.getLength(); j++) {
                    Element themeElement = (Element) themeList.item(j);
                    String themeId = themeElement.getAttribute(THEME_ID);
                    IStyle theme = sts.findStyle(themeId);
                    themes.add(theme);
                }
                ThemeUIGroup themeGroup = new ThemeUIGroup(groupId, groupName,
                        themes.toArray(new IStyle[themes.size()]));
                if (themeGroup.getItems().length != 0) {
                    systemGroups.add(themeGroup);
                }
            }
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return systemGroups;

    }

    private ThemeUIGroup getDefaultGroup() {
        IResourceManager rm = MindMapUI.getResourceManager();
        IStyle[] defaultGroup = { rm.getDefaultTheme() };
        return new ThemeUIGroup(DEFAULT_GROUP_ID,
                MindMapMessages.ThemeUICore_group_default_name, defaultGroup);
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

    public static ThemeUICore getInstance() {
        if (instance == null) {
            instance = new ThemeUICore();
        }
        return instance;
    }

}
