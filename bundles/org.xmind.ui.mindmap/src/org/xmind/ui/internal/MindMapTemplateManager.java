/* ******************************************************************************
 * Copyright (c) 2006-2010 XMind Ltd. and others.
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

/**
 * @author Frank Shaka
 * @deprecated Use MindMapUI.getResourceManager()
 */
@Deprecated
public class MindMapTemplateManager {

//    private static final MindMapTemplateManager instance = new MindMapTemplateManager();
//
//    private static final String DEFAULT_TEMPLATE_ID = "org.xmind.ui.template.default"; //$NON-NLS-1$
//
//    private static final String TEMPLATES_PATH = "templates"; //$NON-NLS-1$
//    private static final String TEMPLATES_DIR = TEMPLATES_PATH + "/"; //$NON-NLS-1$
//
//    private List<ITemplateManagerListener> listeners = new ArrayList<ITemplateManagerListener>();
//
//    private MindMapTemplateManager() {
//    }
//
//    public void addTemplateManagerListener(ITemplateManagerListener listener) {
//        listeners.add(listener);
//    }
//
//    public void removeTemplateManagerListener(
//            ITemplateManagerListener listener) {
//        listeners.remove(listener);
//    }
//
//    private void fireTemplateAdded(ITemplateDescriptor template) {
//        for (Object listener : listeners.toArray()) {
//            try {
//                ((ITemplateManagerListener) listener).templateAdded(template);
//            } catch (Throwable e) {
//                Logger.log(e);
//            }
//        }
//    }
//
//    private void fireTemplateRemoved(ITemplateDescriptor template) {
//        for (Object listener : listeners.toArray()) {
//            try {
//                ((ITemplateManagerListener) listener).templateRemoved(template);
//            } catch (Throwable e) {
//                Logger.log(e);
//            }
//        }
//    }
//
//    public List<ITemplateDescriptor> importTemplates(String... fileNames) {
//        List<ITemplateDescriptor> importedTemplates = new ArrayList<ITemplateDescriptor>(
//                fileNames.length);
//        for (String fileName : fileNames) {
//            ITemplateDescriptor template = importCustomTemplate(fileName);
//            if (template != null) {
//                importedTemplates.add(template);
//            }
//        }
//        return importedTemplates;
//    }
//
//    public ITemplateDescriptor importCustomTemplate(IWorkbook workbook,
//            String name) {
//        if (!name.endsWith(MindMapUI.FILE_EXT_TEMPLATE)
//                && !name.endsWith(MindMapUI.FILE_EXT_XMIND)) {
//            name = name + MindMapUI.FILE_EXT_TEMPLATE;
//        }
//        File targetFile = createCustomTemplateOutputFile(name);
//        IWorkbookRef wr = MindMapUIPlugin.getDefault().getWorkbookRefFactory()
//                .createWorkbookRef(targetFile.toURI(), null);
//        if (wr == null)
//            return null;
//
//        final boolean[] finished = new boolean[1];
//        finished[0] = false;
//        wr.importFrom(new PreLoadedWorkbookRef(workbook, null),
//                new IEditableOperationCallback() {
//                    public void handleCompletion(boolean successful,
//                            IEditableStatus status) {
//                        finished[0] = true;
//                    }
//                });
//
//        try {
//            while (!finished[0]) {
//                Thread.sleep(0);
//            }
//        } catch (InterruptedException e) {
//            return null;
//        }
//        if (!wr.exists())
//            return null;
//
//        FileTemplateDescriptor template = new FileTemplateDescriptor(
//                targetFile);
//        fireTemplateAdded(template);
//        return template;
//    }
//
//    public ITemplateDescriptor importCustomTemplate(String path) {
//        File sourceFile = new File(path);
//        String fileName = sourceFile.getName();
//        File targetFile = createCustomTemplateOutputFile(fileName);
//        try {
//            FileUtils.copy(sourceFile, targetFile);
//            FileTemplateDescriptor template = new FileTemplateDescriptor(
//                    targetFile);
//            fireTemplateAdded(template);
//            return template;
//        } catch (IOException e) {
//        }
//        return null;
//    }
//
//    private File createCustomTemplateOutputFile(String fileName) {
//        String dirPath = getCustomTemplatesLocation();
//        File dir = new File(dirPath);
//        FileUtils.ensureDirectory(dir);
//        return createNonConflictingFile(dir, fileName);
//    }
//
//    public boolean removeTemplate(ITemplateDescriptor template) {
//        if (template instanceof FileTemplateDescriptor) {
//            File file = ((FileTemplateDescriptor) template).getFile();
//            if (file.delete()) {
//                fireTemplateRemoved(template);
//                return true;
//            }
//        }
//        return false;
//    }
//
//    public List<ITemplateDescriptor> loadAllTemplates() {
//        List<ITemplateDescriptor> templates = new ArrayList<ITemplateDescriptor>();
//        loadDefaultTemplates(templates);
//        loadSystemTemplates(templates);
//        loadCustomTemplates(templates);
//        return templates;
//    }
//
//    public List<ITemplateDescriptor> loadDefaultTemplates() {
//        List<ITemplateDescriptor> templates = new ArrayList<ITemplateDescriptor>();
//        loadDefaultTemplates(templates);
//        return templates;
//    }
//
//    public List<ITemplateDescriptor> loadSystemTemplates() {
//        List<ITemplateDescriptor> templates = new ArrayList<ITemplateDescriptor>();
//        loadSystemTemplates(templates);
//        return templates;
//    }
//
//    public List<ITemplateDescriptor> loadCustomTemplates() {
//        List<ITemplateDescriptor> templates = new ArrayList<ITemplateDescriptor>();
//        loadCustomTemplates(templates);
//        return templates;
//    }
//
//    private void loadDefaultTemplates(List<ITemplateDescriptor> templates) {
//        templates.add(new DefaultTemplateDescriptor(DEFAULT_TEMPLATE_ID,
//                WizardMessages.ChooseTemplateWizardPage_BlankMap_title));
//    }
//
//    private void loadCustomTemplates(List<ITemplateDescriptor> templates) {
//        String templatesDir = getCustomTemplatesLocation();
//        loadTemplatesFromDir(templates, new File(templatesDir));
//    }
//
//    private String getCustomTemplatesLocation() {
//        return Core.getWorkspace().getAbsolutePath(TEMPLATES_PATH);
//    }
//
//    private void loadTemplatesFromDir(List<ITemplateDescriptor> templates,
//            File templatesDir) {
//        List<ITemplateDescriptor> list = new ArrayList<ITemplateDescriptor>();
//        if (templatesDir != null && templatesDir.isDirectory()) {
//            for (String fileName : templatesDir.list()) {
//                if (fileName.endsWith(MindMapUI.FILE_EXT_TEMPLATE)
//                        || fileName.endsWith(MindMapUI.FILE_EXT_XMIND)) {
//                    File file = new File(templatesDir, fileName);
//                    if (file.isFile() && file.canRead()) {
//                        list.add(new FileTemplateDescriptor(file));
//                    }
//                }
//            }
//        }
//        Collections.sort(list, new Comparator<ITemplateDescriptor>() {
//            public int compare(ITemplateDescriptor t1, ITemplateDescriptor t2) {
//                File f1 = ((FileTemplateDescriptor) t1).getFile();
//                File f2 = ((FileTemplateDescriptor) t2).getFile();
//                return (int) (f1.lastModified() - f2.lastModified());
//            }
//        });
//        templates.addAll(list);
//    }
//
//    private void loadSystemTemplates(List<ITemplateDescriptor> templates) {
//        Bundle bundle = Platform.getBundle(MindMapUI.PLUGIN_ID);
//        if (bundle != null) {
//            Element element = getTemplateListElement(bundle);
//            if (element != null) {
//                java.util.Properties properties = getTemplateListProperties(
//                        bundle);
//                Iterator<Element> it = DOMUtils.childElementIterByTag(element,
//                        "template"); //$NON-NLS-1$
//                while (it.hasNext()) {
//                    Element templateEle = it.next();
//                    String resourcePath = templateEle.getAttribute("resource"); //$NON-NLS-1$
//                    if (!"".equals(resourcePath)) { //$NON-NLS-1$
//                        if (!resourcePath.startsWith("/")) { //$NON-NLS-1$
//                            resourcePath = TEMPLATES_DIR + resourcePath;
//                        }
//                        URL url = findTemplateResource(bundle, resourcePath);
//                        if (url != null) {
//                            String name = templateEle.getAttribute("name"); //$NON-NLS-1$
//                            if (name.startsWith("%")) { //$NON-NLS-1$
//                                if (properties != null) {
//                                    name = properties
//                                            .getProperty(name.substring(1));
//                                } else {
//                                    name = null;
//                                }
//                            }
//                            if (name == null || "".equals(name)) { //$NON-NLS-1$
//                                name = FileUtils
//                                        .getNoExtensionFileName(resourcePath);
//                            }
//                            templates.add(new URLTemplateDescriptor(url, name));
//                        }
//                    }
//                }
//            }
//        }
//    }
//
//    private URL findTemplateResource(Bundle bundle, String resourcePath) {
//        return FileLocator.find(bundle, new Path("$nl$/" + resourcePath), null); //$NON-NLS-1$
//    }
//
//    private java.util.Properties getTemplateListProperties(Bundle bundle) {
//        URL propURL = ResourceFinder.findResource(bundle, TEMPLATES_DIR,
//                "templates", ".properties"); //$NON-NLS-1$ //$NON-NLS-2$
//        if (propURL != null) {
//            try {
//                InputStream is = propURL.openStream();
//                try {
//                    java.util.Properties properties = new java.util.Properties();
//                    properties.load(is);
//                    return properties;
//                } finally {
//                    is.close();
//                }
//            } catch (IOException e) {
//            }
//        }
//        return null;
//    }
//
//    private Element getTemplateListElement(Bundle bundle) {
//        URL xmlURL = FileLocator.find(bundle,
//                new Path(TEMPLATES_DIR + "templates.xml"), null); //$NON-NLS-1$
//        if (xmlURL == null)
//            return null;
//        try {
//            InputStream is = xmlURL.openStream();
//            if (is != null) {
//                try {
//                    Document doc = DOMUtils.loadDocument(is);
//                    if (doc != null)
//                        return doc.getDocumentElement();
//                } finally {
//                    is.close();
//                }
//            }
//        } catch (Throwable e) {
//        }
//        return null;
//    }
//
//    protected File createNonConflictingFile(File rootDir, String fileName) {
//        int dotIndex = fileName.lastIndexOf('.');
//        String name = dotIndex < 0 ? fileName : fileName.substring(0, dotIndex);
//        String ext = dotIndex < 0 ? "" : fileName.substring(dotIndex); //$NON-NLS-1$
//        File targetFile = new File(rootDir, fileName);
//        int i = 1;
//        while (targetFile.exists()) {
//            i++;
//            targetFile = new File(rootDir,
//                    String.format("%s %s%s", name, i, ext)); //$NON-NLS-1$
//        }
//        return targetFile;
//    }
//
//    public ITemplateDescriptor loadTemplate(String symbolicName) {
//        if (symbolicName == null || "".equals(symbolicName)) //$NON-NLS-1$
//            return null;
//        if (symbolicName.startsWith("theme:")) { //$NON-NLS-1$
//            IStyle theme = findTheme(symbolicName.substring(6));
//            if (theme != null) {
//                return new ThemeTemplateDescriptor(theme);
//            }
//            return null;
//        } else if (symbolicName.startsWith("file:")) { //$NON-NLS-1$
//            String path = symbolicName.substring(5);
//            File file = new File(path);
//            if (file.isFile() && file.canRead()) {
//                return new FileTemplateDescriptor(file);
//            }
//            return null;
//        } else if (symbolicName.startsWith("default:")) { //$NON-NLS-1$
//            String id = symbolicName.substring(8);
//            return new DefaultTemplateDescriptor(id, ""); //$NON-NLS-1$
//        } else {
//            return loadTemplateFromURL(symbolicName);
//        }
//    }
//
//    private ITemplateDescriptor loadTemplateFromURL(String uri) {
//        try {
//            URL url = new URL(uri);
//            return new URLTemplateDescriptor(url, ""); //$NON-NLS-1$
//        } catch (MalformedURLException e) {
//        }
//        return null;
//    }
//
//    private IStyle findTheme(String themeId) {
//        IResourceManager resourceManager = MindMapUI.getResourceManager();
//        if ("blank".equals(themeId)) { //$NON-NLS-1$
//            return resourceManager.getBlankTheme();
//        }
//        IStyleSheet systemThemeSheet = resourceManager.getSystemThemeSheet();
//        IStyle style = systemThemeSheet.findStyle(themeId);
//        if (style != null)
//            return style;
//        IStyleSheet userThemeSheet = resourceManager.getUserThemeSheet();
//        return userThemeSheet.findStyle(themeId);
//    }
//
//    public static MindMapTemplateManager getInstance() {
//        return instance;
//    }
//
}
