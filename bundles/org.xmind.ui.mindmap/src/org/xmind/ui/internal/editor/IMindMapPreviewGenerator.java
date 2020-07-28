/* ******************************************************************************
 * Copyright (c) 2006-2016 XMind Ltd. and others.
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
/**
 * 
 */
package org.xmind.ui.internal.editor;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Properties;

import org.xmind.core.IMeta;
import org.xmind.core.ISheet;
import org.xmind.ui.mindmap.IWorkbookRef;

/**
 * @author Frank Shaka
 * @since 3.6.50
 */
public interface IMindMapPreviewGenerator {

    String PREVIEW_ORIGIN_X = IMeta.ORIGIN_X;
    String PREVIEW_ORIGIN_Y = IMeta.ORIGIN_Y;
    String PREVIEW_BACKGROUND = IMeta.BACKGROUND_COLOR;

    /**
     * Generates a preview image for a specific sheet and stores the result into
     * the specified output stream. A key-value map will be returned containing
     * properties of the generated preview image.
     * 
     * @param workbookRef
     * @param sheet
     * @param output
     * @param options
     * @return
     * @throws IOException
     */
    Properties generateMindMapPreview(IWorkbookRef workbookRef, ISheet sheet,
            OutputStream output, MindMapPreviewOptions options)
                    throws IOException;

}
