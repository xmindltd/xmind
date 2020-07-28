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
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.zip.ZipOutputStream;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.xmind.core.Core;
import org.xmind.core.CoreException;
import org.xmind.core.internal.dom.MarkerResourceProvider;
import org.xmind.core.internal.zip.ArchiveConstants;
import org.xmind.core.internal.zip.ZipStreamOutputTarget;
import org.xmind.core.io.DirectoryInputSource;
import org.xmind.core.io.DirectoryOutputTarget;
import org.xmind.core.io.IInputSource;
import org.xmind.core.io.IOutputTarget;
import org.xmind.core.marker.IMarkerGroup;
import org.xmind.core.marker.IMarkerSheet;
import org.xmind.core.util.FileUtils;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.util.ImageFormat;

public class MarkerImpExpUtils {

    private static class ImageFileFilter implements FileFilter {

        public boolean accept(File pathname) {
            if (ImageFormat.findByExtension(
                    FileUtils.getExtension(pathname.getAbsolutePath()), null) == null)
                return false;

            try {
                new Image(Display.getCurrent(), pathname.getAbsolutePath())
                        .dispose();
                return true;
            } catch (Throwable e) {
                return false;
            }
        }

    }

    private MarkerImpExpUtils() {
    }

    public static void exportMarkerPackage(List<IMarkerGroup> sourceGroups,
            String targetPath, boolean fileOrDirectory) throws IOException {
        IOutputTarget target;
        if (fileOrDirectory) {
            target = new ZipStreamOutputTarget(new ZipOutputStream(
                    new FileOutputStream(targetPath)), true);
        } else {
            target = new DirectoryOutputTarget(targetPath);
        }

        IMarkerSheet targetSheet = Core.getMarkerSheetBuilder()
                .createMarkerSheet(new MarkerResourceProvider(null, target));
        for (IMarkerGroup group : sourceGroups) {
            targetSheet.importGroup(group);
        }
        try {
            targetSheet.save(target
                    .getEntryStream(ArchiveConstants.MARKER_SHEET_XML));
        } catch (CoreException e) {
            throw new IOException();
        } finally {
            if (target instanceof ZipStreamOutputTarget) {
                ((ZipStreamOutputTarget) target).close();
            }
        }
    }

    public static void importMarkerPackage(String sourcePath)
            throws IOException {
        try {
            if (new File(sourcePath).isDirectory()) {
                MindMapUI
                        .getResourceManager()
                        .getUserMarkerSheet()
                        .importFrom(createImageFilesInputSource(sourcePath),
                                new File(sourcePath).getName());
            } else {
                MindMapUI.getResourceManager().getUserMarkerSheet()
                        .importFrom(sourcePath);
            }
            MindMapUI.getResourceManager().saveUserMarkerSheet();
        } catch (CoreException e) {
            throw new IOException();
        }
    }

    private static IInputSource createImageFilesInputSource(String sourcePath) {
        DirectoryInputSource source = new DirectoryInputSource(sourcePath);
        source.setFilter(new ImageFileFilter());
        return source;
    }

}