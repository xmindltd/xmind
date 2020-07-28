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
package org.xmind.ui.wizards;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.xmind.core.IFileEntry;
import org.xmind.core.IRelationship;
import org.xmind.core.ISheet;
import org.xmind.core.ITopic;
import org.xmind.core.marker.IMarker;
import org.xmind.core.style.IStyle;
import org.xmind.gef.util.Properties;
import org.xmind.ui.internal.wizards.WizardMessages;
import org.xmind.ui.mindmap.GhostShellProvider;
import org.xmind.ui.mindmap.IMindMapViewer;
import org.xmind.ui.mindmap.MindMap;
import org.xmind.ui.mindmap.MindMapImageExporter;

public abstract class Exporter implements IExporter {

    protected static final String Message_FailedToCreateOverview = WizardMessages.DocumentExport_FailedToCreateOverview;

    protected static final String Message_FailedToCopyAttachment = WizardMessages.DocumentExport_FailedToCopyAttachmentFile;

    protected static final String Message_FailedToCopyMarker = WizardMessages.DocumentExport_FailedToCopyMarker;

    private static final List<IExportPart> EMPTY_PARTS = Collections
            .emptyList();

    private static final List<Map.Entry<Throwable, String>> NO_ERRORS = Collections
            .emptyList();

    private final ISheet sheet;

    private final ITopic centralTopic;

    private Display display = null;

    private Shell shell = null;

    private List<IExportPart> parts = null;

    private Iterator<IExportPart> partIter = null;

    private IExportPart next = null;

    private List<IRelationship> relationships = null;

    private IDialogSettings dialogSettings = null;

    private List<Map.Entry<Throwable, String>> errors = null;

    private GhostShellProvider overviewExportShellProvider = null;

    public Exporter(ISheet sheet, ITopic centralTopic) {
        this.sheet = sheet;
        this.centralTopic = centralTopic;
    }

    public ITopic getCentralTopic() {
        return centralTopic;
    }

    public ISheet getSheet() {
        return sheet;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.xmind.ui.internal.wizards.ILinearExporter#append(org.xmind.ui.internal
     * .wizards.ExportPart)
     */
    public void append(IExportPart part) {
        if (parts == null)
            parts = new ArrayList<IExportPart>();
        parts.add(part);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xmind.ui.internal.wizards.ILinearExporter#getTotalWork()
     */
    public int getTotalWork() {
        return parts == null ? 0 : parts.size();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xmind.ui.internal.wizards.ILinearExporter#canStart()
     */
    public boolean canStart() {
        return parts != null && !parts.isEmpty();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xmind.ui.internal.wizards.ILinearExporter#start()
     */
    public void start(Display display, Shell shell)
            throws InvocationTargetException {
        this.display = display;
        this.shell = shell;
        if (partIter == null && parts != null) {
            partIter = parts.iterator();
            next = partIter.hasNext() ? partIter.next() : null;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xmind.ui.internal.wizards.ILinearExporter#hasNext()
     */
    public boolean hasNext() {
        return next != null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xmind.ui.internal.wizards.ILinearExporter#getNextName()
     */
    public String getNextName() {
        return next != null ? next.toString() : null;
    }

    public void writeNext(IProgressMonitor monitor)
            throws InvocationTargetException, InterruptedException {
        if (next != null) {
            write(monitor, next);
            if (partIter != null) {
                next = partIter.hasNext() ? partIter.next() : null;
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xmind.ui.internal.wizards.ILinearExporter#end()
     */
    public void end() throws InvocationTargetException {
        next = null;
        partIter = null;
        if (overviewExportShellProvider != null) {
            display.syncExec(new Runnable() {
                public void run() {
                    overviewExportShellProvider.dispose();
                }
            });
            overviewExportShellProvider = null;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xmind.ui.internal.wizards.ILinearExporter#getParts()
     */
    public List<IExportPart> getParts() {
        return parts == null ? EMPTY_PARTS : parts;
    }

    protected abstract void write(IProgressMonitor monitor, IExportPart part)
            throws InvocationTargetException, InterruptedException;

    public List<IRelationship> getRelationships() {
        if (relationships == null) {
            relationships = new ArrayList<IRelationship>(getSheet()
                    .getRelationships());
        }
        return relationships;
    }

//    public MindMapPreviewBuilder createOverviewBuilder(ITopic topic) {
//        if (!hasOverview(topic))
//            return null;
//
//        MindMapPreviewBuilder builder = new MindMapPreviewBuilder(topic
//                .getOwnedSheet(), topic);
//
//        if (getBoolean(ExportContants.SEPARATE_OVERVIEW)) {
//            builder.setProperty(IMindMapViewer.VIEWER_MAX_TOPIC_LEVEL, 1);
//        }
//
//        return builder;
//    }

    public MindMapImageExporter createOverviewExporter(ITopic topic) {
        if (!hasOverview(topic))
            return null;
        if (overviewExportShellProvider == null) {
            overviewExportShellProvider = new GhostShellProvider(getDisplay());
        }
        Properties properties = new Properties();
        if (getBoolean(ExportContants.SEPARATE_OVERVIEW)) {
            properties.set(IMindMapViewer.VIEWER_MAX_TOPIC_LEVEL, 1);
        }
        MindMapImageExporter exporter = new MindMapImageExporter(getDisplay());
        exporter.setSource(new MindMap(topic.getOwnedSheet(), topic),
                overviewExportShellProvider, properties, null);
        return exporter;
    }

    public boolean hasOverview(ITopic topic) {
        if (!getBoolean(ExportContants.INCLUDE_OVERVIEW))
            return false;

        if (topic.equals(getCentralTopic())
                || topic.hasChildren(ITopic.DETACHED))
            return true;

        if (getBoolean(ExportContants.SEPARATE_OVERVIEW)) {
            if (topic.getAllChildren().isEmpty())
                return false;
            return true;
        }
        return false;
    }

    public Display getDisplay() {
        return display;
    }

    public Shell getShell() {
        return shell;
    }

    public boolean getBoolean(String propertyName) {
        if (dialogSettings != null)
            return dialogSettings.getBoolean(propertyName);
        return false;
    }

    public double getDouble(String propertyName, double defaultValue) {
        if (dialogSettings != null)
            try {
                return dialogSettings.getDouble(propertyName);
            } catch (NumberFormatException e) {
            }
        return defaultValue;
    }

    public int getInt(String propertyName, int defaultValue) {
        if (dialogSettings != null)
            try {
                return dialogSettings.getInt(propertyName);
            } catch (NumberFormatException e) {
            }
        return defaultValue;
    }

    public String getString(String propertyName, String defaultValue) {
        if (dialogSettings != null) {
            String value = dialogSettings.get(propertyName);
            if (value != null)
                return value;
        }
        return defaultValue;
    }

    public void setDialogSettings(IDialogSettings dialogSettings) {
        this.dialogSettings = dialogSettings;
    }

    public void log(final Throwable e, final String message) {
        if (errors == null)
            errors = new ArrayList<Map.Entry<Throwable, String>>();
        errors.add(new Map.Entry<Throwable, String>() {

            public Throwable getKey() {
                return e;
            }

            public String getValue() {
                return message;
            }

            public String setValue(String value) {
                return getValue();
            }

        });
    }

    public List<Map.Entry<Throwable, String>> getErrors() {
        return errors == null ? NO_ERRORS : errors;
    }

    public IStyle getStyle(String styleId) {
        if (styleId == null)
            return null;
        return getSheet().getOwnedWorkbook().getStyleSheet().findStyle(styleId);
    }

    public IMarker getMarker(String markerId) {
        if (markerId == null)
            return null;
        return getSheet().getOwnedWorkbook().getMarkerSheet()
                .findMarker(markerId);
    }

    public IFileEntry getFileEntry(String entryPath) {
        if (entryPath == null)
            return null;
        return getSheet().getOwnedWorkbook().getManifest()
                .getFileEntry(entryPath);
    }

}