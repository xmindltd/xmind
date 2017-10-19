package org.xmind.ui.internal;

@Deprecated
public class TemplateManagerImpl {

//    private static final String TEMPLATES_PATH = "templates"; //$NON-NLS-1$
//    private static final String TEMPLATES_DIR = TEMPLATES_PATH + "/"; //$NON-NLS-1$
//
//    private ListenerList listeners = new ListenerList();
//    private ITemplate defaultTemplate;
//
//    @Override
//    public ITemplate getTemplate(URI templateURI) {
//        String path = templateURI.getPath();
//        if (path == null)
//            return null;
//
//        if (path.equals(ClonedTemplate.URI_PATH)) {
//            return ClonedTemplate.create(templateURI);
//        }
//
//        return null;
//    }
//
//    @Override
//    public List<ITemplate> getSystemTemplates() {
//        List<ITemplate> sysTemplates = new ArrayList<ITemplate>();
//        loadSystemTemplates(sysTemplates);
//        return sysTemplates;
//    }
//
//    private void loadSystemTemplates(List<ITemplate> templates) {
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
//                            URI uri = null;
//                            try {
//                                uri = URIUtil.toURI(url);
//                            } catch (URISyntaxException e) {
//                            }
//                            if (uri != null) {
//                                String name = templateEle.getAttribute("name"); //$NON-NLS-1$
//                                if (name.startsWith("%")) { //$NON-NLS-1$
//                                    if (properties != null) {
//                                        name = properties
//                                                .getProperty(name.substring(1));
//                                    } else {
//                                        name = null;
//                                    }
//                                }
//                                if (name == null || "".equals(name)) { //$NON-NLS-1$
//                                    name = FileUtils.getNoExtensionFileName(
//                                            resourcePath);
//                                }
//                                templates.add(ClonedTemplate
//                                        .createFromSourceWorkbookURI(uri,
//                                                name));
//                            }
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
//    private File createNonConflictingFile(File rootDir, String fileName) {
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
//    @Override
//    public List<ITemplate> getCustomTemplates() {
//        List<ITemplate> customTemplates = new ArrayList<ITemplate>();
//        loadCustomTemplates(customTemplates);
//        return customTemplates;
//    }
//
//    private void loadCustomTemplates(List<ITemplate> templates) {
//        loadTemplatesFromDir(templates, getCustomTemplatesDir());
//    }
//
//    private static File getCustomTemplatesDir() {
//        return new File(Core.getWorkspace().getAbsolutePath(TEMPLATES_PATH));
//    }
//
//    private void loadTemplatesFromDir(List<ITemplate> templates,
//            File templatesDir) {
//        List<ITemplate> list = new ArrayList<ITemplate>();
//        if (templatesDir != null && templatesDir.isDirectory()) {
//            for (String fileName : templatesDir.list()) {
//                if (fileName.endsWith(MindMapUI.FILE_EXT_TEMPLATE)
//                        || fileName.endsWith(MindMapUI.FILE_EXT_XMIND)) {
//                    File file = new File(templatesDir, fileName);
//                    if (file.isFile() && file.canRead()) {
//                        list.add(ClonedTemplate.createFromSourceWorkbookURI(
//                                file.toURI(), fileName));
//                    }
//                }
//            }
//        }
//        Collections.sort(list, new Comparator<ITemplate>() {
//            public int compare(ITemplate t1, ITemplate t2) {
//                if (!(t1 instanceof ClonedTemplate)
//                        || !(t2 instanceof ClonedTemplate))
//                    return 0;
//                ClonedTemplate ct1 = (ClonedTemplate) t1;
//                ClonedTemplate ct2 = (ClonedTemplate) t2;
//
//                File f1 = URIUtil.toFile(ct1.getSourceWorkbookURI());
//                File f2 = URIUtil.toFile(ct2.getSourceWorkbookURI());
//                if (f1 == null || f2 == null || !f1.exists() || !f2.exists())
//                    return 0;
//                return (int) (f1.lastModified() - f2.lastModified());
//            }
//        });
//        templates.addAll(list);
//    }
//
//    @Override
//    public ITemplate addCustomTemplateFromWorkbookURI(URI workbookURI)
//            throws CoreException {
//        if (URIUtil.isFileURI(workbookURI)) {
//            File sourceFile = URIUtil.toFile(workbookURI);
//            String fileName = sourceFile.getName();
//            File targetFile = createCustomTemplateOutputFile(fileName);
//            try {
//                FileUtils.copy(sourceFile, targetFile);
//                ClonedTemplate template = ClonedTemplate
//                        .createFromSourceWorkbookURI(targetFile.toURI(),
//                                fileName);
//                fireTemplateAdded(template);
//                return template;
//            } catch (IOException e) {
//            }
//
//        }
//        return null;
//    }
//
//    private void fireTemplateAdded(ITemplate template) {
//        for (Object listener : listeners.getListeners()) {
//            try {
//                ((ITemplateManagerListener) listener)
//                        .customTemplateAdded(template);
//            } catch (Throwable e) {
//                Logger.log(e);
//            }
//        }
//    }
//
//    private void fireTemplateRemoved(ITemplate template) {
//        for (Object listener : listeners.getListeners()) {
//            try {
//                ((ITemplateManagerListener) listener)
//                        .customTemplateRemoved(template);
//            } catch (Throwable e) {
//                Logger.log(e);
//            }
//        }
//    }
//
//    private File createCustomTemplateOutputFile(String fileName) {
//        File dir = getCustomTemplatesDir();
//        FileUtils.ensureDirectory(dir);
//        return createNonConflictingFile(dir, fileName);
//    }
//
//    @Override
//    public void removeCustomTemplate(ITemplate template) {
//        if (template instanceof ISourceWorkbookProvider) {
//            URI templateURI = ((ISourceWorkbookProvider) template)
//                    .getSourceWorkbookURI();
//            if (URIUtil.isFileURI(templateURI)) {
//                File file = URIUtil.toFile(templateURI);
//                if (file.delete()) {
//                    fireTemplateRemoved(template);
//                }
//            }
//        }
//    }
//
//    @Override
//    public void addTemplateManagerListener(ITemplateManagerListener listener) {
//        listeners.add(listener);
//    }
//
//    @Override
//    public void removeTemplateManagerListener(
//            ITemplateManagerListener listener) {
//        listeners.remove(listener);
//    }
//
//    @Override
//    public ITemplate getDefaultTemplate() {
//        return this.defaultTemplate;
//    }
//
//    @Override
//    public void setDefaultTemplate(ITemplate defaultTemplate) {
//        this.defaultTemplate = defaultTemplate;
//    }

}
