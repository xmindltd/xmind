package org.xmind.ui.internal.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.xmind.core.Core;
import org.xmind.core.internal.ElementRegistry;
import org.xmind.core.marker.IMarker;
import org.xmind.core.marker.IMarkerGroup;
import org.xmind.core.marker.IMarkerSheet;
import org.xmind.core.style.IStyle;
import org.xmind.core.style.IStyleSheet;
import org.xmind.core.util.FileUtils;
import org.xmind.core.util.Property;
import org.xmind.ui.internal.MindMapMessages;
import org.xmind.ui.internal.MindMapUIPlugin;
import org.xmind.ui.internal.dialogs.DialogUtils;
import org.xmind.ui.mindmap.IResourceManager;
import org.xmind.ui.mindmap.ITemplate;
import org.xmind.ui.mindmap.IWorkbookRef;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.style.Styles;
import org.xmind.ui.util.Logger;

public class ResourceUtils {

    public static List<IStyle> duplicateStyles(List<IStyle> styles) {
        ArrayList<IStyle> newStyles = new ArrayList<IStyle>();
        IStyleSheet styleSheet = MindMapUI.getResourceManager()
                .getUserStyleSheet();
        for (IStyle styleDuplicated : styles) {
            IStyle newStyle = styleSheet.createStyle(styleDuplicated.getType());
            Iterator<Property> ps = styleDuplicated.properties();
            while (ps.hasNext()) {
                Property p = ps.next();
                newStyle.setProperty(p.key, p.value);
            }
            newStyle.setName(NLS.bind(MindMapMessages.ResourceUtil_Copy_name,
                    styleDuplicated.getName()));
            styleSheet.addStyle(newStyle, IStyleSheet.NORMAL_STYLES);

            newStyles.add(newStyle);
        }
        MindMapUI.getResourceManager().saveUserStyleSheet();
        return newStyles;
    }

    public static List<IStyle> duplicateThemes(List<IStyle> themes) {
        ArrayList<IStyle> newThemes = new ArrayList<IStyle>();
        IStyleSheet styleSheet = MindMapUI.getResourceManager()
                .getUserThemeSheet();
        for (IStyle styleDuplicated : themes) {
            IStyle themeCreated = styleSheet
                    .createStyle(styleDuplicated.getType());
            themeCreated
                    .setName(NLS.bind(MindMapMessages.ResourceUtil_Copy_name,
                            styleDuplicated.getName()));
            Iterator<Property> defaultStyles = styleDuplicated.defaultStyles();
            while (defaultStyles.hasNext()) {
                Property p = defaultStyles.next();
                IStyle defaultStyle = styleDuplicated.getDefaultStyle(p.key);
                if (defaultStyle != null) {
                    IStyle styleCreated = styleSheet.createStyle(
                            transformStyleFamilyToStyleType(p.key));
                    Iterator<Property> ps = defaultStyle.properties();
                    while (ps.hasNext()) {
                        Property next = ps.next();
                        styleCreated.setProperty(next.key, next.value);
                    }
                    themeCreated.setDefaultStyleId(p.key, styleCreated.getId());
                    styleSheet.addStyle(styleCreated,
                            IStyleSheet.AUTOMATIC_STYLES);
                }
            }
            styleSheet.addStyle(themeCreated, IStyleSheet.MASTER_STYLES);
            newThemes.add(themeCreated);
        }
        MindMapUI.getResourceManager().saveUserThemeSheet();
        return newThemes;
    }

    private static String transformStyleFamilyToStyleType(String family) {
        if (Styles.FAMILY_CENTRAL_TOPIC.equals(family)
                || Styles.FAMILY_MAIN_TOPIC.equals(family)
                || Styles.FAMILY_SUB_TOPIC.equals(family)
                || Styles.FAMILY_SUMMARY_TOPIC.equals(family)
                || Styles.FAMILY_FLOATING_TOPIC.equals(family)
                || Styles.FAMILY_CALLOUT_TOPIC.equals(family))
            return IStyle.TOPIC;
        else if (Styles.FAMILY_RELATIONSHIP.equals(family))
            return IStyle.RELATIONSHIP;
        else if (Styles.FAMILY_BOUNDARY.equals(family))
            return IStyle.BOUNDARY;
        else if (Styles.FAMILY_MAP.equals(family))
            return IStyle.MAP;
        else if (Styles.FAMILY_SUMMARY.equals(family))
            return IStyle.SUMMARY;
        return null;
    }

    public static void deleteStyles(List<IStyle> styles) {
        boolean isTheme = false;
        for (IStyle style : styles) {
            if (IStyle.THEME.equals(style.getType()))
                isTheme = true;
            IStyleSheet sheet = style.getOwnedStyleSheet();
            ElementRegistry elementRegistry = (ElementRegistry) sheet
                    .getAdapter(ElementRegistry.class);
            elementRegistry.unregister(style);
            sheet.removeStyle(style);
        }
        if (isTheme)
            MindMapUI.getResourceManager().saveUserThemeSheet();
        else
            MindMapUI.getResourceManager().saveUserStyleSheet();
    }

    public static boolean confirmToDeleteStyles(Shell shell,
            List<IStyle> styles) {
        StringBuilder sb = new StringBuilder(styles.size() * 10);
        boolean isTheme = false;
        for (IStyle style : styles) {
            if (IStyle.THEME.equals(style.getType()))
                isTheme = true;
            if (sb.length() > 0) {
                sb.append(',');
                sb.append(' ');
            }
            sb.append('\'');
            sb.append(style.getName());
            sb.append('\'');
        }
        String styleNames = sb.toString();
        return MessageDialog.openConfirm(shell,
                NLS.bind(MindMapMessages.DeleteStyles_MessageDialog_title,
                        isTheme ? MindMapMessages.DeleteStyle_MessageDialog_themes
                                : MindMapMessages.DeleteStyle_MessageDialog_styles),
                NLS.bind(MindMapMessages.DeleteStyle_MessageDialog_description,
                        styleNames));
    }

    public static void deleteTemplates(List<ITemplate> templates) {
        IResourceManager resourceManager = MindMapUI.getResourceManager();
        List<ITemplate> userTemplates = resourceManager.getUserTemplates();
        for (ITemplate template : templates)
            if (userTemplates.contains(template)) {
                resourceManager.removeUserTemplate(template);
            }
    }

    public static List<ITemplate> duplicateTemplates(
            List<ITemplate> templates) {
        final ArrayList<ITemplate> newTemplates = new ArrayList<ITemplate>();

        File tempFolder = new File(
                Core.getWorkspace().getTempDir("transient-templates")); //$NON-NLS-1$
        tempFolder.mkdirs();

        for (ITemplate template : templates) {
            final File tempFile = new File(tempFolder,
                    template.getName() + " " //$NON-NLS-1$
                            + MindMapMessages.ResourceUtil_Duplicate_name
                            + MindMapUI.FILE_EXT_TEMPLATE);
            if (!tempFile.exists()) {
                try {
                    tempFile.createNewFile();
                } catch (IOException e) {
                }
            }
            final IWorkbookRef tempWR = MindMapUIPlugin.getDefault()
                    .getWorkbookRefFactory()
                    .createWorkbookRef(tempFile.toURI(), null);
            final IWorkbookRef clonedWR = template.createWorkbookRef();
            SafeRunnable.run(new SafeRunnable() {
                @Override
                public void run() throws Exception {
                    NullProgressMonitor monitor = new NullProgressMonitor();
                    try {
                        SubMonitor subMonitor = SubMonitor.convert(monitor,
                                100);
                        clonedWR.open(subMonitor.newChild(30));
                        try {
                            tempWR.importFrom(subMonitor.newChild(60),
                                    clonedWR);

                            /// Fix duplicate template, thumbnail lose.
                            tempWR.open(monitor);
                            tempWR.save(monitor);
                        } finally {
                            subMonitor.setWorkRemaining(10);
                            clonedWR.close(subMonitor.newChild(5));
                            tempWR.close(subMonitor.newChild(5));
                        }
                    } finally {
                        if (monitor != null)
                            monitor.done();
                    }

                    ITemplate newTemplate = MindMapUI.getResourceManager()
                            .addUserTemplateFromWorkbookURI(tempWR.getURI());
                    newTemplates.add(newTemplate);

                    if (tempFile != null && tempFile.exists()) {
                        tempFile.delete();
                    }
                }
            });
        }
        return newTemplates;
    }

    public static void deleteMarkers(List<IMarker> markers) {
        for (IMarker marker : markers)
            marker.getParent().removeMarker(marker);
    }

    public static List<IMarker> addMarkersFor(IMarkerGroup markerGroup) {
        List<IMarker> newMarkers = new ArrayList<IMarker>();
        String[] sourcePaths = selectImageFile(
                Display.getCurrent().getActiveShell());
        if (sourcePaths == null)
            return newMarkers;

        IMarkerSheet ownedSheet = markerGroup.getOwnedSheet();
        for (String sourcePath : sourcePaths) {
            if (imageValid(sourcePath)) {
                String targetPath = null;
                FileInputStream sourceFIS = null;
                try {
                    sourceFIS = new FileInputStream(sourcePath);
                    targetPath = ownedSheet.allocateMarkerResource(sourceFIS,
                            sourcePath);
                } catch (IOException e) {
                    Logger.log(e);
                } finally {
                    if (sourceFIS != null) {
                        try {
                            sourceFIS.close();
                        } catch (IOException e) {
                            Logger.log(e);
                        }
                    }
                }

                if (targetPath != null) {
                    IMarker marker = ownedSheet.createMarker(targetPath);
                    marker.setName(FileUtils.getFileName(sourcePath));
                    markerGroup.addMarker(marker);
                    newMarkers.add(marker);
                }

            }
        }
        return newMarkers;
    }

    private static String[] selectImageFile(Shell shell) {
        FileDialog dialog = new FileDialog(shell, SWT.OPEN | SWT.MULTI);
        DialogUtils.makeDefaultImageSelectorDialog(dialog, true);
        String open = dialog.open();
        if (open == null)
            return null;
        String parent = dialog.getFilterPath();
        String[] fileNames = dialog.getFileNames();
        String[] paths = new String[fileNames.length];
        for (int i = 0; i < fileNames.length; i++) {
            paths[i] = new File(parent, fileNames[i]).getAbsolutePath();
        }
        return paths;
    }

    private static boolean imageValid(String sourcePath) {
        try {
            new Image(Display.getCurrent(), sourcePath).dispose();
            return true;
        } catch (Throwable e) {
        }
        return false;
    }

}
