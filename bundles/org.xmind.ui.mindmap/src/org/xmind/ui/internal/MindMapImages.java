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

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.Util;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.internal.util.BundleUtility;
import org.xmind.core.Core;
import org.xmind.core.IBoundary;
import org.xmind.core.IRelationship;
import org.xmind.core.ISheet;
import org.xmind.core.ITopic;
import org.xmind.core.IWorkbook;
import org.xmind.core.util.FileUtils;
import org.xmind.ui.mindmap.IMindMapImages;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.resources.ImageUtils;

public class MindMapImages implements IMindMapImages {

    private Map<String, ImageDescriptor> descriptors = new HashMap<String, ImageDescriptor>();

    private Map<Object, Cursor> cursors = new HashMap<Object, Cursor>();

    /* package */ MindMapImages() {
    }

    public ImageDescriptor get(String fullPath) {
        ImageDescriptor descriptor = descriptors.get(fullPath);
        if (descriptor == null) {
            descriptor = createImageDescriptor(fullPath);
            if (descriptor != null)
                descriptors.put(fullPath, descriptor);
        }
        return descriptor;
    }

    private ImageDescriptor createImageDescriptor(String path) {
        URL url = BundleUtility.find(MindMapUI.PLUGIN_ID, path);
        if (url != null)
            return ImageDescriptor.createFromURL(url);
        return null;
    }

    public ImageDescriptor get(String fileName, String mainPath) {
        return get(mainPath + fileName);
    }

    public ImageDescriptor get(String fileName, boolean enabled) {
        String mainPath = enabled ? PATH_E : PATH_D;
        return get(fileName, mainPath);
    }

    /*
     * (non-Javadoc)
     * @see
     * org.xmind.ui.mindmap.IMindMapImages#getTopicIcon(org.xmind.core.ITopic,
     * boolean)
     */
    public ImageDescriptor getTopicIcon(ITopic topic, boolean enabled) {
        String type = topic.getType();
        if (ITopic.ROOT.equals(type)) {
            return get(CENTRAL, enabled);
        }
        if (ITopic.SUMMARY.equals(type)) {
            return get(SUMMARY_TOPIC, enabled);
        }
        if (ITopic.DETACHED.equals(type)) {
            return get(FLOATING, enabled);
        }
        if (ITopic.CALLOUT.equals(type)) {
            return get(CALLOUT, enabled);
        }
        ITopic parent = topic.getParent();
        if (parent != null && parent.isRoot()) {
            return get(MAIN, enabled);
        }
        return get(TOPIC, enabled);
    }

    /*
     * (non-Javadoc)
     * @see org.xmind.ui.mindmap.IMindMapImages#getElementIcon(java.lang.Object)
     */
    public ImageDescriptor getElementIcon(Object element, boolean enabled) {
        if (element instanceof ITopic) {
            ITopic topic = (ITopic) element;
            String hyperlink = topic.getHyperlink();
            if (hyperlink != null) {
                IAction action = MindMapUI.getProtocolManager()
                        .createOpenHyperlinkAction(topic, hyperlink);
                if (action != null && action.getImageDescriptor() != null)
                    return action.getImageDescriptor();
            }
            return getTopicIcon(topic, enabled);
        } else if (element instanceof ISheet) {
            return get(SHEET, enabled);
        } else if (element instanceof IWorkbook) {
            return get(WORKBOOK, enabled);
        } else if (element instanceof IBoundary) {
            return get(BOUNDARY, enabled);
        } else if (element instanceof IRelationship) {
            return get(RELATIONSHIP, enabled);
        }
        return null;
    }

    public ImageDescriptor getWizBan(String fileName) {
        return get(fileName, PATH_WIZ);
    }

    public Cursor getCursor(CursorFactory factory) {
        if (factory == null)
            return null;
        String id = factory.getId();
        if (id == null)
            return null;
        Cursor cursor = cursors.get(id);
        if (cursor == null) {
            cursor = factory.createCursor(this);
            if (cursor != null)
                cursors.put(id, cursor);
        }
        return cursor;
    }

    public Cursor createCursor(String name, int hotX, int hotY) {
        ImageDescriptor sourceDesc = get(name + ".bmp", PATH_POINTERS); //$NON-NLS-1$
        if (sourceDesc == null)
            return null;
        ImageData source = sourceDesc.getImageData();
        if (source == null)
            return null;
        ImageDescriptor maskDesc = get(name + "_mask.bmp", PATH_POINTERS); //$NON-NLS-1$
        if (maskDesc != null) {
            ImageData mask = maskDesc.getImageData();
            if (mask != null) {
                return new Cursor(null, source, mask, hotX, hotY);
            }
        }
        return new Cursor(null, source, hotX, hotY);
    }

    public ImageDescriptor getFileIcon(String path) {
        return getFileIcon(path, false);
    }

    public ImageDescriptor getFileIcon(String path,
            boolean returnNullIfUnidentifiable) {
        String extension = FileUtils.getExtension(path);
        if (extension == null || "".equals(extension)) { //$NON-NLS-1$
            if (new File(path).isDirectory()) {
                return get(OPEN, true);
            }
            if (returnNullIfUnidentifiable)
                return null;
            return get(UNKNOWN_FILE, true);
        }
        String key = "org.xmind.ui.fileIcon" + extension; //$NON-NLS-1$
        ImageDescriptor image = ImageUtils.getDescriptor(key);
        if (image == null) {
            image = createFileIcon(extension, new File(path).isDirectory(),
                    returnNullIfUnidentifiable);
            ImageUtils.putImageDescriptor(key, image);
        }
        return image;
    }

    private ImageDescriptor createFileIcon(String fileExtension,
            boolean directory, boolean returnNullIfUnidentifiable) {
        Program p = Program.findProgram(fileExtension);
        if (p != null) {
            ImageData data = p.getImageData();
            if (data != null) {
                if (Util.isMac())
                    return ImageUtils.scaleImage(Display.getCurrent(),
                            ImageDescriptor.createFromImageData(data), 16, 16);

                //fix bug: icon has black shadow in Windows.
                String tempPath = getTempFileIconPath();

                ImageLoader imageLoader = new ImageLoader();
                imageLoader.data = new ImageData[] { data };
                imageLoader.save(tempPath, SWT.IMAGE_ICO);

                Image image = new Image(Display.getCurrent(), tempPath);
                return ImageDescriptor.createFromImage(image);
            }
        }
        if (directory)
            return get(OPEN, true);
        if (returnNullIfUnidentifiable)
            return null;
        return get(UNKNOWN_FILE, true);
    }

    private String getTempFileIconPath() {
        String tempFile = Core.getWorkspace().getTempFile("fileIcons/" //$NON-NLS-1$
                + Core.getIdFactory().createId() + ".ico"); //$NON-NLS-1$
        return tempFile;
    }

}
