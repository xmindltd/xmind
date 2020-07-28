/* ******************************************************************************
 * Copyright (c) 2006-2012 XMind Ltd. and others.
 * 
 * This file is a part of XMind 3. XMind releases 3 and
 * above are dual-licensed under the Eclipse Public License (EPL),
 * which is available at http://www.eclipse.org/legal/epl-v10.html
 * and the GNU Lesser General Public License (LGPL), 
 * which is available at http://www.gnu.org/licenses/lgpl.html
 * See https://www.xmind.net/license.html for details.
 * 
 * Contributors:
 *     XMind Ltd. - initial API and implementation
 *******************************************************************************/
package org.xmind.ui.internal;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import javax.imageio.ImageIO;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.URIUtil;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.ui.PlatformUI;
import org.osgi.framework.Bundle;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xmind.core.Core;
import org.xmind.core.IAdaptable;
import org.xmind.core.IDeserializer;
import org.xmind.core.IManifest;
import org.xmind.core.ISerializer;
import org.xmind.core.IWorkbook;
import org.xmind.core.event.ICoreEventListener;
import org.xmind.core.event.ICoreEventRegistration;
import org.xmind.core.event.ICoreEventSource;
import org.xmind.core.event.ICoreEventSupport;
import org.xmind.core.internal.MarkerGroup;
import org.xmind.core.internal.MarkerVariation;
import org.xmind.core.internal.dom.StyleSheetImpl;
import org.xmind.core.internal.event.CoreEventSupport;
import org.xmind.core.internal.zip.ArchiveConstants;
import org.xmind.core.io.BundleResource;
import org.xmind.core.io.DirectoryInputSource;
import org.xmind.core.io.DirectoryOutputTarget;
import org.xmind.core.io.DirectoryStorage;
import org.xmind.core.marker.AbstractMarkerResource;
import org.xmind.core.marker.IMarker;
import org.xmind.core.marker.IMarkerGroup;
import org.xmind.core.marker.IMarkerResource;
import org.xmind.core.marker.IMarkerResourceAllocator;
import org.xmind.core.marker.IMarkerResourceProvider;
import org.xmind.core.marker.IMarkerSheet;
import org.xmind.core.marker.IMarkerVariation;
import org.xmind.core.style.IStyle;
import org.xmind.core.style.IStyleSheet;
import org.xmind.core.util.DOMUtils;
import org.xmind.core.util.FileUtils;
import org.xmind.core.util.IPropertiesProvider;
import org.xmind.ui.mindmap.IResourceManager;
import org.xmind.ui.mindmap.IResourceManagerListener;
import org.xmind.ui.mindmap.ITemplate;
import org.xmind.ui.mindmap.ITemplateGroup;
import org.xmind.ui.mindmap.IWorkbookRef;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.prefs.PrefConstants;
import org.xmind.ui.util.Logger;
import org.xmind.ui.util.ResourceFinder;
import org.xmind.ui.util.XMLUtils;

public class MindMapResourceManager implements IResourceManager {

    private static final String DEFAULT_THEME_ID = "xminddefaultthemeid"; //$NON-NLS-1$

    private static final String PATH_MARKERS = "markers/"; //$NON-NLS-1$

    private static final String PATH_USER_MARKERS = "markers/"; //$NON-NLS-1$

    private static final String PATH_STYLES = "styles/"; //$NON-NLS-1$

    private static final String PATH_STYLES_DIR = "$nl$/styles/"; //$NON-NLS-1$

    private static final String PATH_USER_STYLES = PATH_STYLES + "userStyles/"; //$NON-NLS-1$

    private static final String PATH_USER_THEMES = PATH_STYLES + "userThemes/"; //$NON-NLS-1$

    private static final String MARKER_SHEET_XML = "markerSheet.xml"; //$NON-NLS-1$

    private static final String MARKER_SHEET = "markerSheet"; //$NON-NLS-1$

    private static final String DEFAULT_STYLES_XML = "defaultStyles.xml"; //$NON-NLS-1$

    private static final String STYLES = "styles"; //$NON-NLS-1$

    private static final String THEMES = "themes"; //$NON-NLS-1$

    private static final String EXT_PROPERTIES = ".properties"; //$NON-NLS-1$

    private static final String EXT_XML = ".xml"; //$NON-NLS-1$

    private static final String SYS_TEMPLATES_DIR = "$nl$/templates/"; //$NON-NLS-1$

    private static final String SYS_TEMPLATES_XML_PATH = SYS_TEMPLATES_DIR
            + "templates.xml"; //$NON-NLS-1$

    private static final String USER_TEMPLATES_DIR = "templates/"; //$NON-NLS-1$

    private static final String PATH_TEMPLATES = "templates/"; //$NON-NLS-1$

    private static final String TEMPLATES = "templates"; //$NON-NLS-1$

    private static class SystemMarkerResourceProvider
            implements IMarkerResourceProvider {

        public IMarkerResource getMarkerResource(IMarker marker) {
            return new SystemMarkerResource(marker);
        }

        public boolean isPermanent() {
            return true;
        }

    }

    private static class SystemMarkerResource extends AbstractMarkerResource {

        public SystemMarkerResource(IMarker marker) {
            super(marker, PATH_MARKERS);
        }

        public InputStream getInputStream() {
            return getInputStreamForPath(getFullPath(), 100);
        }

        @Override
        public InputStream openInputStream(int zoom) throws IOException {
            return getInputStreamForPath(getFullPath(), zoom);
        }

        @Override
        public InputStream openInputStream(IMarkerVariation variation, int zoom)
                throws IOException {
            return getInputStreamForPath(variation.getVariedPath(getFullPath()),
                    zoom);
        }

        private InputStream getInputStreamForPath(String fullPath, int zoom) {
            String xfullPath = getxPath(fullPath, zoom);
            URL url = find(xfullPath);
            if (url == null)
                url = find(fullPath);

            if (url == null)
                return null;

            try {
                return url.openStream();
            } catch (IOException e) {
            }
            return null;
        }

        private String getxPath(String path, int zoom) {
            int dot = path.lastIndexOf('.');
            if (dot != -1 && zoom > 100) {
                String lead = path.substring(0, dot);
                String tail = path.substring(dot);
                String x = "@2x"; //$NON-NLS-1$ 
                return lead + x + tail;
            }
            return path;
        }

        public OutputStream getOutputStream() {
            return null;
        }

        public InputStream openInputStream() throws IOException {
            URL url = find(getFullPath());
            if (url == null)
                throw new FileNotFoundException(getFullPath());
            return url.openStream();
        }

        public OutputStream openOutputStream() throws IOException {
            throw new FileNotFoundException("System marker is read only."); //$NON-NLS-1$
        }

        @Override
        protected void loadVariations(List<IMarkerVariation> variations) {

            IMarkerVariation v = new MarkerVariation("@16", 16, 16); //$NON-NLS-1$
            if (find(v.getVariedPath(getFullPath())) != null)
                variations.add(v);

            v = new MarkerVariation("@24", 24, 24); //$NON-NLS-1$
            if (find(v.getVariedPath(getFullPath())) != null)
                variations.add(v);

            v = new MarkerVariation("@32", 32, 32); //$NON-NLS-1$
            if (find(v.getVariedPath(getFullPath())) != null)
                variations.add(v);
        }

        @Override
        public InputStream getInputStream(IMarkerVariation variation) {
            return getInputStreamForPath(variation.getVariedPath(getFullPath()),
                    100);
        }

        @Override
        public InputStream openInputStream(IMarkerVariation variation)
                throws IOException {
            InputStream stream = getInputStreamForPath(
                    variation.getVariedPath(getFullPath()), 100);
            if (stream == null)
                throw new FileNotFoundException();
            return stream;
        }

        @Override
        public OutputStream getOutputStream(IMarkerVariation variation) {
            return null;
        }

        @Override
        public OutputStream openOutputStream(IMarkerVariation variation)
                throws IOException {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this)
                return true;
            if (obj == null || !(obj instanceof SystemMarkerResource))
                return false;
            return super.equals(obj);
        }
    }

    private static class UserMarkerResourceProvider
            implements IMarkerResourceProvider, IMarkerResourceAllocator {

        public IMarkerResource getMarkerResource(IMarker marker) {
            return new UserMarkerResource(marker);
        }

        public boolean isPermanent() {
            return false;
        }

        /*
         * (non-Javadoc)
         * @see org.xmind.core.marker.IMarkerResourceAllocator#
         * allocateMarkerResourcePath(java.io.InputStream, java.lang.String)
         */
        @Override
        public String allocateMarkerResource(InputStream source,
                String suggestedPath) throws IOException {
            String ext = suggestedPath == null ? ".png" //$NON-NLS-1$
                    : FileUtils.getExtension(suggestedPath);
            String path = Core.getIdFactory().createId() + ext;
            File file = new File(Core.getWorkspace()
                    .getAbsolutePath(PATH_USER_MARKERS + path));
            FileUtils.ensureFileParent(file);
            OutputStream target = new FileOutputStream(file);
            try {
                FileUtils.transfer(source, target, false);
            } finally {
                target.close();
            }
            return path;
        }

    }

    private static class UserMarkerResource extends AbstractMarkerResource {

        private final String JPG_FORMAT = "jpg"; //$NON-NLS-1$
        private final String JPEG_FORMAT = "jpeg"; //$NON-NLS-1$
        private final String PNG_FORMAT = "png"; //$NON-NLS-1$

        public UserMarkerResource(IMarker marker) {
            super(marker, PATH_USER_MARKERS);
        }

        private File getFile() {
            File origin = FileUtils.ensureFileParent(new File(
                    Core.getWorkspace().getAbsolutePath(getFullPath())));
            String lowerFullPath = getFullPath().toLowerCase();
            if (lowerFullPath.endsWith(JPEG_FORMAT)
                    || lowerFullPath.endsWith(JPG_FORMAT)) {
                try {
                    String jpg = Core.getWorkspace()
                            .getAbsolutePath(getFullPath());
                    BufferedImage source = ImageIO.read(new File(jpg));
                    String png = jpg.substring(0, jpg.lastIndexOf('.') - 1)
                            + PNG_FORMAT;
                    File pngFile = new File(png);
                    ImageIO.write(source, PNG_FORMAT, pngFile);
                    return pngFile;
                } catch (Exception e) {
                }
            }

            return origin;
        }

        public InputStream getInputStream() {
            File file = getFile();
            if (file != null)
                try {
                    return new FileInputStream(file);
                } catch (FileNotFoundException e) {
                }
            return null;
        }

        public OutputStream getOutputStream() {
            File file = getFile();
            if (file != null)
                try {
                    return new FileOutputStream(file);
                } catch (FileNotFoundException e) {
                }
            return null;
        }

        public InputStream openInputStream() throws IOException {
            return new FileInputStream(getFile());
        }

        public OutputStream openOutputStream() throws IOException {
            return new FileOutputStream(getFile());
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this)
                return true;
            if (obj == null || !(obj instanceof UserMarkerResource))
                return false;
            return super.equals(obj);
        }

    }

    protected static class RecentMarkerGroup extends MarkerGroup
            implements ICoreEventSource {

        public static final RecentMarkerGroup instance = new RecentMarkerGroup();

        private static final int CAPACITY = 20;

        private List<IMarker> markers = new ArrayList<IMarker>(CAPACITY);

        private ICoreEventSupport eventSupport = new CoreEventSupport();

        private RecentMarkerGroup() {
        }

        public void addMarker(IMarker marker) {
            if (markers.contains(marker))
                return;

            while (markers.size() >= CAPACITY) {
                markers.remove(markers.size() - 1);
            }
            markers.add(0, marker);
            eventSupport.dispatchTargetChange(this, Core.MarkerAdd, marker);
        }

        public <T> T getAdapter(Class<T> adapter) {
            if (adapter == ICoreEventSource.class)
                return adapter.cast(this);
            return super.getAdapter(adapter);
        }

        public List<IMarker> getMarkers() {
            return markers;
        }

        /*
         * (non-Javadoc)
         * @see org.xmind.core.marker.IMarkerGroup#isEmpty()
         */
        @Override
        public boolean isEmpty() {
            return markers.isEmpty();
        }

        public String getName() {
            return MindMapMessages.RecentUsed;
        }

        public void setSingleton(boolean singleton) {
        }

        public IMarkerSheet getOwnedSheet() {
            return null;
        }

        public IMarkerSheet getParent() {
            return null;
        }

        public boolean isSingleton() {
            return false;
        }

        public boolean isHidden() {
            return false;
        }

        public void setHidden(boolean hidden) {

        }

        public void removeMarker(IMarker marker) {
            if (!markers.contains(marker))
                return;
            markers.remove(marker);
            eventSupport.dispatchTargetChange(this, Core.MarkerRemove, marker);
        }

        public void setName(String name) {
        }

        public String getId() {
            return "org.xmind.ui.RecentMarkerGroup"; //$NON-NLS-1$
        }

        public Object getRegisterKey() {
            return getId();
        }

        public ICoreEventRegistration registerCoreEventListener(String type,
                ICoreEventListener listener) {
            return eventSupport.registerCoreEventListener(this, type, listener);
        }

        public int hashCode() {
            return super.hashCode();
        }

        public ICoreEventSupport getCoreEventSupport() {
            return eventSupport;
        }

    }

    private IMarkerSheet systemMarkerSheet = null;

    private IMarkerSheet userMarkerSheet = null;

    private IMarkerGroup recentMarkerGroup = null;

    private IStyleSheet defaultStyleSheet = null;

    private IStyleSheet systemStyleSheet = null;

    private IWorkbook userStylesContainer = null;

    private IStyle blankTheme = null;

    private IStyle defaultTheme = null;

    private IStyleSheet systemThemeSheet = null;

    private IWorkbook userThemesContainer = null;

    private ITemplate defaultTemplate;

    private ListenerList resourceManagerListeners = new ListenerList();

    /*
     * (non-Javadoc)
     * @see org.xmind.ui.internal.IMarkerSheetManager#getSystemMarkerSheet()
     */
    public IMarkerSheet getSystemMarkerSheet() {
        if (systemMarkerSheet == null) {
            systemMarkerSheet = createSystemMarkerShet();
        }
        return systemMarkerSheet;
    }

    private IMarkerSheet createSystemMarkerShet() {
        URL url = find(PATH_MARKERS, MARKER_SHEET_XML);
        if (url != null) {
            try {
                IMarkerSheet sheet = Core.getMarkerSheetBuilder()
                        .loadFromURL(url, new SystemMarkerResourceProvider());
                loadPropertiesFor(sheet, PATH_MARKERS, MARKER_SHEET);
                return sheet;
            } catch (Exception e) {
                Logger.log(e, "Failed to load system marker from: " + url); //$NON-NLS-1$
            }
        }
        return Core.getMarkerSheetBuilder()
                .createMarkerSheet(new SystemMarkerResourceProvider());
    }

    /*
     * (non-Javadoc)
     * @see org.xmind.ui.internal.IMarkerSheetManager#getUserMarkerSheet()
     */
    public IMarkerSheet getUserMarkerSheet() {
        if (userMarkerSheet == null) {
            userMarkerSheet = createUserMarkerSheet();
            initUserMarkerSheet(userMarkerSheet);
        }
        return userMarkerSheet;
    }

    public void saveUserMarkerSheet() {
        if (userMarkerSheet != null) {
            String path = Core.getWorkspace()
                    .getAbsolutePath(PATH_USER_MARKERS + MARKER_SHEET_XML);
            File file = FileUtils.ensureFileParent(new File(path));
            FileOutputStream out = null;
            try {
                out = new FileOutputStream(file);
                userMarkerSheet.save(out);
            } catch (Exception e) {
                Logger.log(e);
            } finally {
                if (out != null)
                    try {
                        out.close();
                    } catch (IOException e) {
                        Logger.log(e);
                    }
            }
        }
    }

    private void initUserMarkerSheet(IMarkerSheet sheet) {
        sheet.setParentSheet(getSystemMarkerSheet());
    }

    private IMarkerSheet createUserMarkerSheet() {
        String path = Core.getWorkspace()
                .getAbsolutePath(PATH_USER_MARKERS + MARKER_SHEET_XML);
        if (path != null) {
            File file = new File(path);
            if (file.exists()) {
                try {
                    return Core.getMarkerSheetBuilder().loadFromFile(file,
                            new UserMarkerResourceProvider());
                } catch (Exception e) {
                    Logger.log(e, "Failed to load user marker from: " + file); //$NON-NLS-1$
                }
            }
        }
        return Core.getMarkerSheetBuilder()
                .createMarkerSheet(new UserMarkerResourceProvider());
    }

    public IMarkerGroup getRecentMarkerGroup() {
        if (recentMarkerGroup == null) {
            recentMarkerGroup = new RecentMarkerGroup();
        }
        return recentMarkerGroup;
    }

    public IStyleSheet getDefaultStyleSheet() {
        if (defaultStyleSheet == null) {
            defaultStyleSheet = createDefaultStyleSheet();
        }
        return defaultStyleSheet;
    }

    private IStyleSheet createDefaultStyleSheet() {
        URL url = find(PATH_STYLES, DEFAULT_STYLES_XML);
        if (url != null) {
            try {
                return Core.getStyleSheetBuilder().loadFromUrl(url);
            } catch (Exception e) {
                Logger.log(e, "Failed to load default styles: " + url); //$NON-NLS-1$
            }
        }
        return Core.getStyleSheetBuilder().createStyleSheet();
    }

    public IStyleSheet getSystemStyleSheet() {
        if (systemStyleSheet == null) {
            systemStyleSheet = createSystemStyleSheet();
            IManifest manifest = Core.getWorkbookBuilder().createWorkbook()
                    .getManifest();
            ((StyleSheetImpl) systemStyleSheet).setManifest(manifest);
        }
        return systemStyleSheet;
    }

    private IStyleSheet createSystemStyleSheet() {
        URL url = find(PATH_STYLES, STYLES, EXT_XML);
        if (url != null) {
            try {
                IStyleSheet sheet = Core.getStyleSheetBuilder()
                        .loadFromUrl(url);
                loadPropertiesFor(sheet, PATH_STYLES, STYLES);
                return sheet;
            } catch (Exception e) {
                Logger.log(e, "Falied to load saved styles: " + url); //$NON-NLS-1$
            }
        }
        return Core.getStyleSheetBuilder().createStyleSheet();
    }

    public IStyleSheet getUserStyleSheet() {
        if (userStylesContainer == null) {
            userStylesContainer = loadResourceContainer(PATH_USER_STYLES);
        }
        return userStylesContainer.getStyleSheet();
    }

    public void saveUserStyleSheet() {
        saveResourceContainer(userStylesContainer, PATH_USER_STYLES);
    }

    public IStyle getBlankTheme() {
        if (blankTheme == null) {
            blankTheme = Core.getStyleSheetBuilder().createStyleSheet()
                    .createStyle(IStyle.THEME);
            blankTheme.setName(MindMapMessages.DefaultTheme_title);
        }
        return blankTheme;
    }

    public IStyle getDefaultTheme() {
        if (defaultTheme == null) {
            defaultTheme = findDefaultTheme();
        }
        return checkDefaultTheme(defaultTheme);
    }

    private IStyle checkDefaultTheme(IStyle defaultTheme) {
        String defaultId = defaultTheme.getId();
        boolean exist = getSystemThemeSheet().findStyle(defaultId) != null
                || getUserThemeSheet().findStyle(defaultId) != null
                || getBlankTheme().getId().equals(defaultId);
        return exist ? defaultTheme
                : getSystemThemeSheet().findStyle(DEFAULT_THEME_ID);

    }

    private IStyle findDefaultTheme() {
        if (Platform.isRunning()) {
            String defaultId = MindMapUIPlugin.getDefault().getPreferenceStore()
                    .getString(PrefConstants.DEFUALT_THEME);
            if (defaultId != null && !"".equals(defaultId)) { //$NON-NLS-1$
                IStyle theme = getSystemThemeSheet().findStyle(defaultId);
                if (theme == null) {
//                    theme = getUserStyleSheet().findStyle(defaultId);
                    theme = getUserThemeSheet().findStyle(defaultId);
                }
                if (theme == null && defaultId.equals(getBlankTheme().getId()))
                    theme = getBlankTheme();

                if (theme != null)
                    return theme;
            }
        }
        return getSystemThemeSheet().findStyle(DEFAULT_THEME_ID);
    }

    public void setDefaultTheme(String id) {
        IStyle theme = null;
        if (id != null && !"".equals(id)) { //$NON-NLS-1$
            theme = getSystemThemeSheet().findStyle(DEFAULT_THEME_ID);
            if (!id.equals(theme.getId())) {
                theme = getSystemThemeSheet().findStyle(id);
                if (theme == null) {
                    theme = getUserThemeSheet().findStyle(id);
                }
                if (theme == null && id.equals(getBlankTheme().getId())) {
                    theme = getBlankTheme();
                }
            }
        }
        if (theme == null)
            id = null;

        this.defaultTheme = theme;
        MindMapUIPlugin.getDefault().getPreferenceStore()
                .setValue(PrefConstants.DEFUALT_THEME, id);
    }

    public IStyleSheet getSystemThemeSheet() {
        if (systemThemeSheet == null) {
            systemThemeSheet = createSystemThemeSheet();
            IManifest manifest = Core.getWorkbookBuilder().createWorkbook()
                    .getManifest();
            ((StyleSheetImpl) systemThemeSheet).setManifest(manifest);
        }
        return systemThemeSheet;
    }

    private IStyleSheet createSystemThemeSheet() {
//        URL url = find(PATH_STYLES, THEMES_XML);
        URL url = find(PATH_STYLES_DIR, THEMES, EXT_XML);
        if (url != null) {
            try {
                IStyleSheet sheet = Core.getStyleSheetBuilder()
                        .loadFromUrl(url);
                loadPropertiesFor(sheet, PATH_STYLES, THEMES);
                return sheet;
            } catch (Exception e) {
                Logger.log(e, "Falied to load system themes: " + url); //$NON-NLS-1$
            }
        }
        return Core.getStyleSheetBuilder().createStyleSheet();
    }

    public IStyleSheet getUserThemeSheet() {
        if (userThemesContainer == null) {
            userThemesContainer = loadResourceContainer(PATH_USER_THEMES);
        }
        return userThemesContainer.getStyleSheet();
    }

    public void saveUserThemeSheet() {
        saveResourceContainer(userThemesContainer, PATH_USER_THEMES);
    }

    /**
     * @param sourcePath
     * @return
     */
    private static IWorkbook loadResourceContainer(String sourcePath) {
        IWorkbook container = null;
        File file = new File(Core.getWorkspace().getAbsolutePath(sourcePath));
        File tempLoc = new File(Core.getWorkspace().getTempDir(sourcePath));
        FileUtils.ensureDirectory(tempLoc);
        DirectoryStorage tempStorage = new DirectoryStorage(tempLoc);

        if (file.exists() && file.isDirectory()
                && new File(file, ArchiveConstants.CONTENT_XML).exists()) {
            try {
                IDeserializer deserializer = Core.getWorkbookBuilder()
                        .newDeserializer();
                deserializer.setWorkbookStorage(tempStorage);
                deserializer.setInputSource(new DirectoryInputSource(file));
                deserializer.deserialize(null);
                container = deserializer.getWorkbook();
            } catch (Exception e) {
                Logger.log(e, "Failed to load user styles from: " + file); //$NON-NLS-1$
            }
        }
        if (container == null) {
            container = Core.getWorkbookBuilder().createWorkbook(tempStorage);
        }
        return container;
    }

    private static void saveResourceContainer(IWorkbook workbook,
            String targetPath) {
        if (workbook == null)
            return;

        File file = new File(Core.getWorkspace().getAbsolutePath(targetPath));
        FileUtils.ensureDirectory(file);
        try {
            ISerializer serializer = Core.getWorkbookBuilder().newSerializer();
            serializer.setWorkbook(workbook);
            serializer.setOutputTarget(new DirectoryOutputTarget(file));
            serializer.serialize(null);
        } catch (Exception e) {
            Logger.log(e);
        }
    }

    private static URL find(String fullPath) {
        Bundle bundle = Platform.getBundle(MindMapUI.PLUGIN_ID);
        if (bundle != null) {
            return FileLocator.find(bundle, new Path(fullPath), null);
        }
        return null;
    }

    private static URL find(String mainPath, String subPath) {
        return find(mainPath + subPath);
    }

    private static URL find(String mainPath, String prefix, String suffix) {
        return ResourceFinder.findResource(MindMapUI.PLUGIN_ID, mainPath,
                prefix, suffix);
    }

    private static void loadPropertiesFor(IAdaptable resourceManager,
            String mainPath, String propertiesFilePrefix) {
        IPropertiesProvider propertiesProvider = (IPropertiesProvider) resourceManager
                .getAdapter(IPropertiesProvider.class);
        if (propertiesProvider == null)
            return;

        Properties defaultProperties = new Properties();
        URL defaultPropertiesURL = FileLocator.find(
                Platform.getBundle(MindMapUI.PLUGIN_ID),
                new Path(mainPath + propertiesFilePrefix + EXT_PROPERTIES),
                null);
        if (defaultPropertiesURL != null) {
            try {
                InputStream stream = defaultPropertiesURL.openStream();
                try {
                    defaultProperties.load(stream);
                } finally {
                    stream.close();
                }
            } catch (IOException e) {
                Logger.log(e, "Failed to load default properties file from: " //$NON-NLS-1$
                        + mainPath + propertiesFilePrefix + EXT_PROPERTIES);
            }
        }

        Properties properties = new Properties(defaultProperties);
        URL propertiesURL = find(mainPath, propertiesFilePrefix,
                EXT_PROPERTIES);
        if (propertiesURL != null) {
            try {
                InputStream stream = propertiesURL.openStream();
                try {
                    properties.load(stream);
                } finally {
                    stream.close();
                }
            } catch (IOException e) {
                Logger.log(e,
                        "Failed to load locale-specific properties file from: " //$NON-NLS-1$
                                + mainPath + propertiesFilePrefix
                                + EXT_PROPERTIES);
            }
        }

        propertiesProvider.setProperties(properties);
    }

    private static final String SCHEMA_MARKER = "marker"; //$NON-NLS-1$
    private static final String SCHEMA_STYLE = "style"; //$NON-NLS-1$
    private static final String SCHEMA_THEME = "theme"; //$NON-NLS-1$
    private static final String CATEGORY_SYSTEM = "system"; //$NON-NLS-1$
    private static final String CATEGORY_USER = "user"; //$NON-NLS-1$
    private static final String CATEGORY_DEFAULT = "default"; //$NON-NLS-1$
    private static final String GROUP_ANY = "any"; //$NON-NLS-1$
    private static final String ID_BLANK = "blank"; //$NON-NLS-1$
    private static final String ID_DEFAULT = "__default__"; //$NON-NLS-1$

    /*
     * (non-Javadoc)
     * @see org.xmind.ui.mindmap.IResourceManager#findResource(java.lang.String)
     */
    public Object findResource(String uri) {
        if (uri == null)
            return null;

        int sep = uri.indexOf(':');
        if (sep < 0)
            return null;

        String schema = uri.substring(0, sep);
        String path = uri.substring(sep + 1);
        String[] segments = path.split("/"); //$NON-NLS-1$
        if (segments.length == 0)
            // Not possible to happen:
            return null;

        String category = segments[0];
        if (SCHEMA_MARKER.equals(schema)) {
            // marker:
            IMarkerSheet markerSheet = findMarkerSheet(category);
            if (markerSheet == null || segments.length == 1)
                return markerSheet;
            String groupId = segments[1];
            boolean anyGroup = GROUP_ANY.equals(groupId);
            IMarkerGroup markerGroup = anyGroup ? null
                    : markerSheet.findMarkerGroup(groupId);
            if (segments.length == 2)
                return markerGroup;
            String markerId = segments[2];
            if (markerGroup == null)
                return anyGroup ? markerSheet.findMarker(markerId) : null;
            return markerGroup.getMarker(markerId);
        } else if (SCHEMA_STYLE.equals(schema)) {
            // style:
            IStyleSheet styleSheet = findStyleSheet(category);
            if (styleSheet == null || segments.length == 1)
                return styleSheet;
            String styleId = segments[1];
            return styleSheet.findStyle(styleId);
        } else if (SCHEMA_THEME.equals(schema)) {
            // theme:
            IStyleSheet themeSheet = findThemeSheet(category);
            if (themeSheet == null || segments.length == 1)
                return themeSheet;
            String styleId = segments[1];
            if (themeSheet == getSystemThemeSheet()) {
                if (ID_BLANK.equals(styleId))
                    return getBlankTheme();
                if (ID_DEFAULT.equals(styleId))
                    return getDefaultTheme();
            }
            return themeSheet.findStyle(styleId);
        }

        return null;
    }

    private IMarkerSheet findMarkerSheet(String category) {
        if (CATEGORY_SYSTEM.equals(category)) {
            return getSystemMarkerSheet();
        } else if (CATEGORY_USER.equals(category)) {
            return getUserMarkerSheet();
        } else {
            return null;
        }
    }

    private IStyleSheet findStyleSheet(String category) {
        if (CATEGORY_DEFAULT.equals(category))
            return getDefaultStyleSheet();
        if (CATEGORY_SYSTEM.equals(category))
            return getSystemStyleSheet();
        if (CATEGORY_USER.equals(category))
            return getUserStyleSheet();
        return null;
    }

    private IStyleSheet findThemeSheet(String category) {
        if (CATEGORY_SYSTEM.equals(category))
            return getSystemThemeSheet();
        if (CATEGORY_USER.equals(category))
            return getUserThemeSheet();
        return null;
    }

    /*
     * (non-Javadoc)
     * @see
     * org.xmind.ui.mindmap.IResourceManager#toResourceURI(java.lang.Object)
     */
    public String toResourceURI(Object resource) {
        if (resource instanceof IMarkerSheet) {
            if (resource == getSystemMarkerSheet())
                return "marker:system"; //$NON-NLS-1$
            if (resource == getUserMarkerSheet())
                return "marker:user"; //$NON-NLS-1$
            return null;
        }
        if (resource instanceof IMarkerGroup) {
            IMarkerGroup markerGroup = (IMarkerGroup) resource;
            IMarkerSheet markerSheet = markerGroup.getParent();
            if (markerSheet == getSystemMarkerSheet())
                return "marker:system/" + markerGroup.getId(); //$NON-NLS-1$
            if (markerSheet == getUserMarkerSheet())
                return "marker:user/" + markerGroup.getId(); //$NON-NLS-1$
            return null;
        }
        if (resource instanceof IMarker) {
            IMarker marker = (IMarker) resource;
            IMarkerGroup markerGroup = marker.getParent();
            if (markerGroup == null)
                return null;
            IMarkerSheet markerSheet = markerGroup.getParent();
            if (markerSheet == getSystemMarkerSheet())
                return "marker:system/" + markerGroup.getId() + "/" //$NON-NLS-1$ //$NON-NLS-2$
                        + marker.getId();
            if (markerSheet == getUserMarkerSheet())
                return "marker:user/" + markerGroup.getId() + "/" //$NON-NLS-1$ //$NON-NLS-2$
                        + marker.getId();
            return null;
        }
        if (resource instanceof IStyleSheet) {
            if (resource == getDefaultStyleSheet())
                return "style:default"; //$NON-NLS-1$
            if (resource == getSystemStyleSheet())
                return "style:system"; //$NON-NLS-1$
            if (resource == getUserStyleSheet())
                return "style:user"; //$NON-NLS-1$
            if (resource == getSystemThemeSheet())
                return "theme:system"; //$NON-NLS-1$
            if (resource == getUserThemeSheet())
                return "theme:user"; //$NON-NLS-1$
        }
        if (resource instanceof IStyle) {
            if (resource == getBlankTheme())
                return "theme:system/blank"; //$NON-NLS-1$
            IStyle style = (IStyle) resource;
            IStyleSheet styleSheet = style.getOwnedStyleSheet();
            if (styleSheet == getDefaultStyleSheet())
                return "style:default/" + style.getId(); //$NON-NLS-1$
            if (styleSheet == getSystemStyleSheet())
                return "style:system/" + style.getId(); //$NON-NLS-1$
            if (styleSheet == getUserStyleSheet())
                return "style:user/" + style.getId(); //$NON-NLS-1$
            if (styleSheet == getSystemThemeSheet())
                return "theme:system/" + style.getId(); //$NON-NLS-1$
            if (styleSheet == getUserThemeSheet())
                return "theme:user/" + style.getId(); //$NON-NLS-1$
        }
        return null;
    }

    public List<ITemplate> getSystemTemplates() {
        List<ITemplate> sysTemplates = new ArrayList<ITemplate>();
        loadSystemTemplates(sysTemplates);
        return sysTemplates;
    }

    public List<ITemplateGroup> getSystemTemplateGroups() {
        List<ITemplateGroup> sysTemplateGroups = new ArrayList<ITemplateGroup>();
        loadSystemTemplateGroups(sysTemplateGroups);
        return sysTemplateGroups;
    }

    private void loadSystemTemplateGroups(
            List<ITemplateGroup> sysTemplateGroups) {
        Bundle bundle = Platform.getBundle(MindMapUI.PLUGIN_ID);
        if (bundle == null)
            return;

        BundleResource listXMLResource = new BundleResource(bundle,
                new Path(SYS_TEMPLATES_XML_PATH)).resolve();
        if (listXMLResource == null) {
            MindMapUIPlugin.getDefault().getLog()
                    .log(new Status(IStatus.ERROR, MindMapUIPlugin.PLUGIN_ID,
                            "Failed to locate system template xml: " //$NON-NLS-1$
                                    + bundle.getSymbolicName() + "/" //$NON-NLS-1$
                                    + SYS_TEMPLATES_XML_PATH));
            return;
        }

        URL listXMLURL = listXMLResource.toPlatformURL();
        Element element = getTemplateListElement(listXMLURL);
        if (element == null)
            return;

        Properties properties = getTemplateListProperties(bundle);
        Iterator<Element> categoryIt = DOMUtils.childElementIterByTag(element,
                "category"); //$NON-NLS-1$

        while (categoryIt.hasNext()) {
            Element categoryEle = categoryIt.next();
            String name = categoryEle.getAttribute("name"); //$NON-NLS-1$

            if (name.startsWith("%")) { //$NON-NLS-1$
                if (properties != null) {
                    name = properties.getProperty(name.substring(1));
                } else {
                    name = null;
                }
            }

            TemplateGroup templateGroup = new TemplateGroup(name);

            Iterator<Element> templateIt = DOMUtils
                    .childElementIterByTag(categoryEle, "template"); //$NON-NLS-1$
            ArrayList<ITemplate> templates = new ArrayList<ITemplate>();
            loadTemplates(templates, templateIt, bundle);
            templateGroup.setTemplates(templates);
            sysTemplateGroups.add(templateGroup);
        }
    }

    private void loadSystemTemplates(List<ITemplate> templates) {
        Bundle bundle = Platform.getBundle(MindMapUI.PLUGIN_ID);
        if (bundle == null)
            return;

        BundleResource listXMLResource = new BundleResource(bundle,
                new Path(SYS_TEMPLATES_XML_PATH)).resolve();
        if (listXMLResource == null) {
            MindMapUIPlugin.getDefault().getLog()
                    .log(new Status(IStatus.ERROR, MindMapUIPlugin.PLUGIN_ID,
                            "Failed to locate system template xml: " //$NON-NLS-1$
                                    + bundle.getSymbolicName() + "/" //$NON-NLS-1$
                                    + SYS_TEMPLATES_XML_PATH));
            return;
        }

        URL listXMLURL = listXMLResource.toPlatformURL();
        Element element = getTemplateListElement(listXMLURL);
        if (element == null)
            return;

        Iterator<Element> it = DOMUtils.childElementIterByTag(element,
                "template"); //$NON-NLS-1$
        loadTemplates(templates, it, bundle);
    }

    private void loadTemplates(List<ITemplate> templates, Iterator<Element> it,
            Bundle bundle) {
        if (bundle == null) {
            return;
        }
        Properties properties = getTemplateListProperties(bundle);

        while (it.hasNext()) {
            Element templateEle = it.next();
            String resource = templateEle.getAttribute("resource"); //$NON-NLS-1$
            if (resource == null || "".equals(resource)) //$NON-NLS-1$
                continue;

            URI resourceURI;
            try {
                resourceURI = URIUtil.toURI(new URL(resource));
            } catch (IOException e) {
                MindMapUIPlugin.getDefault().getLog().log(new Status(
                        IStatus.ERROR, MindMapUIPlugin.PLUGIN_ID,
                        "Failed to load system template: " + resource, e)); //$NON-NLS-1$
                continue;
            } catch (URISyntaxException e) {
                MindMapUIPlugin.getDefault().getLog().log(new Status(
                        IStatus.ERROR, MindMapUIPlugin.PLUGIN_ID,
                        "Failed to load system template: " + resource, e)); //$NON-NLS-1$
                continue;
            }

            if (!resourceURI.isAbsolute()) {
                BundleResource templateResource = new BundleResource(
                        Platform.getBundle(MindMapUI.PLUGIN_ID),
                        new Path(SYS_TEMPLATES_DIR + resource)).resolve();
                try {
                    resourceURI = templateResource.toPlatformURL().toURI();
                } catch (URISyntaxException e) {
                    MindMapUIPlugin.getDefault().getLog().log(new Status(
                            IStatus.ERROR, MindMapUIPlugin.PLUGIN_ID,
                            "Failed to load system template: " + resource, e)); //$NON-NLS-1$
                    continue;
                }
            }

            String name = templateEle.getAttribute("name"); //$NON-NLS-1$
            if (name.startsWith("%")) { //$NON-NLS-1$
                if (properties != null) {
                    name = properties.getProperty(name.substring(1));
                } else {
                    name = null;
                }
            }

            if (name == null || "".equals(name)) { //$NON-NLS-1$
                name = FileUtils.getNoExtensionFileName(resource);
            }
            templates.add(new ClonedTemplate(resourceURI, name));
        }
    }

    private Properties getTemplateListProperties(Bundle bundle) {
        final IPropertiesProvider provider = new IPropertiesProvider() {

            Properties properties;

            @Override
            public void setProperties(Properties properties) {
                this.properties = properties;
            }

            @Override
            public Properties getProperties() {
                return properties;
            }
        };

        IAdaptable adaptable = new IAdaptable() {

            @Override
            public <T> T getAdapter(Class<T> adapter) {
                if (adapter == IPropertiesProvider.class) {
                    return adapter.cast(provider);
                }
                return null;
            }
        };
        loadPropertiesFor(adaptable, PATH_TEMPLATES, TEMPLATES);

        return provider.getProperties();
    }

    private Element getTemplateListElement(URL xmlURL) {
        xmlURL = FileLocator.find(xmlURL);
        try {
            InputStream is = xmlURL.openStream();
            if (is != null) {
                try {
                    Document doc = XMLUtils.getDefaultDocumentBuilder()
                            .parse(is);
                    if (doc != null)
                        return doc.getDocumentElement();
                } finally {
                    is.close();
                }
            }
        } catch (Throwable e) {
            MindMapUIPlugin.getDefault().getLog()
                    .log(new Status(IStatus.WARNING, MindMapUIPlugin.PLUGIN_ID,
                            "Failed to load template list from " //$NON-NLS-1$
                                    + xmlURL.toExternalForm(),
                            e));
        }
        return null;
    }

    private File createNonConflictingFile(File rootDir, String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        String name = dotIndex < 0 ? fileName : fileName.substring(0, dotIndex);
        String ext = dotIndex < 0 ? "" : fileName.substring(dotIndex); //$NON-NLS-1$
        File targetFile = new File(rootDir, fileName);
        int i = 1;
        while (targetFile.exists()) {
            i++;
            targetFile = new File(rootDir,
                    String.format("%s %s%s", name, i, ext)); //$NON-NLS-1$
        }
        return targetFile;
    }

    @Override
    public List<ITemplate> getUserTemplates() {
        List<ITemplate> customTemplates = new ArrayList<ITemplate>();
        loadUserTemplates(customTemplates);
        return customTemplates;
    }

    private void loadUserTemplates(List<ITemplate> templates) {
        loadTemplatesFromDir(templates, getUserTemplatesDir());
    }

    private static File getUserTemplatesDir() {
        return new File(
                Core.getWorkspace().getAbsolutePath(USER_TEMPLATES_DIR));
    }

    private void loadTemplatesFromDir(List<ITemplate> templates,
            File templatesDir) {
        List<ITemplate> list = new ArrayList<ITemplate>();
        if (templatesDir != null && templatesDir.isDirectory()) {
            for (String fileName : templatesDir.list()) {
                if (fileName.endsWith(MindMapUI.FILE_EXT_TEMPLATE)
                        || fileName.endsWith(MindMapUI.FILE_EXT_XMIND)) {
                    File file = new File(templatesDir, fileName);
                    if ((file.isFile() && file.canRead())
                            || file.isDirectory()) {
                        list.add(new ClonedTemplate(file.toURI(), null));
                    }
                }
            }
        }
        Collections.sort(list, new Comparator<ITemplate>() {
            public int compare(ITemplate t1, ITemplate t2) {
                if (!(t1 instanceof ClonedTemplate)
                        || !(t2 instanceof ClonedTemplate))
                    return 0;
                ClonedTemplate ct1 = (ClonedTemplate) t1;
                ClonedTemplate ct2 = (ClonedTemplate) t2;

                File f1 = URIUtil.toFile(ct1.getSourceWorkbookURI());
                File f2 = URIUtil.toFile(ct2.getSourceWorkbookURI());
                if (f1 == null || f2 == null || !f1.exists() || !f2.exists())
                    return 0;
                return (int) (f1.lastModified() - f2.lastModified());
            }
        });
        templates.addAll(list);
    }

    @Override
    public ITemplate addUserTemplateFromWorkbookURI(URI workbookURI)
            throws InvocationTargetException {
        Assert.isNotNull(workbookURI);
        final IWorkbookRef sourceWorkbookRef = MindMapUIPlugin.getDefault()
                .getWorkbookRefFactory().createWorkbookRef(workbookURI, null);
        if (sourceWorkbookRef == null)
            throw new IllegalArgumentException(
                    "Invalid workbook URI: " + workbookURI); //$NON-NLS-1$

        final File userTemplateFile = createUserTemplateOutputFile(
                sourceWorkbookRef.getName() + MindMapUI.FILE_EXT_TEMPLATE);
        final URI templateURI = userTemplateFile.toURI();

        final IWorkbookRef templateWorkbookRef = MindMapUIPlugin.getDefault()
                .getWorkbookRefFactory().createWorkbookRef(templateURI, null);
        if (templateWorkbookRef == null)
            throw new IllegalStateException(
                    "Failed to obtain workbook ref for local file URI: " //$NON-NLS-1$
                            + templateURI);

        IRunnableWithProgress runnable = new IRunnableWithProgress() {
            @Override
            public void run(IProgressMonitor monitor)
                    throws InvocationTargetException, InterruptedException {
                try {
                    SubMonitor subMonitor = SubMonitor.convert(monitor, 100);
                    sourceWorkbookRef.open(subMonitor.newChild(30));
                    try {
                        templateWorkbookRef.importFrom(subMonitor.newChild(60),
                                sourceWorkbookRef);
                    } finally {
                        subMonitor.setWorkRemaining(10);
                        sourceWorkbookRef.close(subMonitor.newChild(10));
                    }
                } finally {
                    if (monitor != null)
                        monitor.done();
                }
            }
        };

        try {
            if (PlatformUI.isWorkbenchRunning()) {
                PlatformUI.getWorkbench().getProgressService().run(false, true,
                        runnable);
            } else {
                runnable.run(new NullProgressMonitor());
            }
        } catch (InterruptedException e) {
            // canceled
            return null;
        }

        ITemplate template = new ClonedTemplate(templateURI, FileUtils
                .getNoExtensionFileName(userTemplateFile.getAbsolutePath()));
        fireUserTemplateAdded(template);
        return template;
    }

    private void fireUserTemplateAdded(final ITemplate template) {
        for (final Object listener : resourceManagerListeners.getListeners()) {
            SafeRunner.run(new SafeRunnable() {
                @Override
                public void run() throws Exception {
                    ((IResourceManagerListener) listener)
                            .userTemplateAdded(template);
                }
            });
        }
    }

    private void fireUserTemplateRemoved(final ITemplate template) {
        for (final Object listener : resourceManagerListeners.getListeners()) {
            SafeRunner.run(new SafeRunnable() {
                @Override
                public void run() throws Exception {
                    ((IResourceManagerListener) listener)
                            .userTemplateRemoved(template);
                }
            });
        }
    }

    private File createUserTemplateOutputFile(String fileName) {
        File dir = getUserTemplatesDir();
        FileUtils.ensureDirectory(dir);
        File file = createNonConflictingFile(dir, fileName);
        file.mkdirs();
        return file;
    }

    @Override
    public void removeUserTemplate(ITemplate template) {
        URI templateURI = template.getSourceWorkbookURI();
        if (URIUtil.isFileURI(templateURI)) {
            File templateFile = URIUtil.toFile(templateURI);
            File templatesDir = getUserTemplatesDir();
            if (templatesDir.equals(templateFile.getParentFile())) {
                FileUtils.delete(templateFile);
                fireUserTemplateRemoved(template);
            }
        }
    }

    @Override
    public void addResourceManagerListener(IResourceManagerListener listener) {
        resourceManagerListeners.add(listener);
    }

    @Override
    public void removeResourceManagerListener(
            IResourceManagerListener listener) {
        resourceManagerListeners.remove(listener);
    }

    @Override
    public ITemplate getDefaultTemplate() {
        return this.defaultTemplate;
    }

    @Override
    public void setDefaultTemplate(ITemplate defaultTemplate) {
        this.defaultTemplate = defaultTemplate;
    }

    @Override
    public boolean isUserTemplate(ITemplate template) {
        URI templateURI = template.getSourceWorkbookURI();
        if (URIUtil.isFileURI(templateURI)) {
            File templateFile = URIUtil.toFile(templateURI);
            File templatesDir = getUserTemplatesDir();
            return templatesDir.equals(templateFile.getParentFile());
        }
        return false;
    }

    @Override
    public boolean isSystemTemplate(ITemplate template) {
        // TODO check source workbook URI to determine system template
        boolean isSysTemplate = getSystemTemplates().contains(template);

        List<ITemplateGroup> systemTemplateGroups = getSystemTemplateGroups();
        for (ITemplateGroup group : systemTemplateGroups) {
            if (group.getTemplates().contains(template))
                return true;
        }

        return isSysTemplate;
    }

}
