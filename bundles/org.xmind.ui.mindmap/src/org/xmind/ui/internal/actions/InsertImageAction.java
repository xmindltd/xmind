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
package org.xmind.ui.internal.actions;

import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.xmind.core.ITopic;
import org.xmind.core.util.FileUtils;
import org.xmind.gef.EditDomain;
import org.xmind.gef.GEF;
import org.xmind.gef.IGraphicalViewer;
import org.xmind.gef.IViewer;
import org.xmind.gef.Request;
import org.xmind.gef.part.IPart;
import org.xmind.gef.ui.actions.ISelectionAction;
import org.xmind.gef.ui.actions.PageAction;
import org.xmind.gef.ui.editor.IGraphicalEditorPage;
import org.xmind.ui.actions.MindMapActionFactory;
import org.xmind.ui.internal.dialogs.DialogMessages;
import org.xmind.ui.internal.dialogs.DialogUtils;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.util.MindMapUtils;

public class InsertImageAction extends PageAction implements ISelectionAction {

    private static final String PNG_FORMAT = "png"; //$NON-NLS-1$
    private static final String JPG_FORMAT = "jpg"; //$NON-NLS-1$
    private static final String JPEG_FORMAT = "jpeg"; //$NON-NLS-1$

    public InsertImageAction(IGraphicalEditorPage page) {
        super(MindMapActionFactory.INSERT_IMAGE.getId(), page);
    }

    public void run() {
        if (isDisposed())
            return;

        EditDomain domain = getEditDomain();
        if (domain == null)
            return;

        IGraphicalViewer viewer = getViewer();
        if (viewer == null)
            return;

        Control control = viewer.getControl();
        if (control == null || control.isDisposed())
            return;

        ISelection selection = viewer.getSelection();
        if (selection.isEmpty() || !(selection instanceof IStructuredSelection))
            return;

        Object o = ((IStructuredSelection) selection).getFirstElement();
        IPart part = viewer.findPart(o);
        ITopic topic = (ITopic) part.getAdapter(ITopic.class);
        if (topic == null)
            return;

        IPart topicPart = viewer.findPart(topic);
        if (topicPart == null)
            return;

        FileDialog dialog = new FileDialog(control.getShell(), SWT.OPEN);
        DialogUtils.makeDefaultImageSelectorDialog(dialog, true);
        dialog.setText(DialogMessages.SelectImageDialog_title);
        String path = dialog.open();
        if (path == null)
            return;

        String lowerPath = path.toLowerCase();
        boolean converted = false;
        if (lowerPath.endsWith(JPG_FORMAT) || lowerPath.endsWith(JPEG_FORMAT)) {
            path = convertJpegToPng(path);
            converted = true;
        }
        insertImage(path, topicPart, viewer, domain);
        if (converted) {
            FileUtils.delete(new File(path));
        }
    }

    protected void insertImage(String path, IPart topicPart, IViewer viewer,
            EditDomain domain) {
        Request request = new Request(MindMapUI.REQ_ADD_IMAGE);
        request.setViewer(viewer);
        request.setPrimaryTarget(topicPart);
        request.setParameter(GEF.PARAM_PATH, new String[] { path });
        domain.handleRequest(request);
    }

    public void setSelection(ISelection selection) {
        setEnabled(MindMapUtils.isSingleTopic(selection));
    }

    private final static String convertJpegToPng(String jpg) {
        try {
            BufferedImage source = ImageIO.read(new File(jpg));
            String png = jpg.substring(0, jpg.lastIndexOf('.') - 1)
                    + PNG_FORMAT;
            ImageIO.write(source, PNG_FORMAT, new File(png));
            return png;
        } catch (Exception e) {
            return jpg;
        }
    }

}
