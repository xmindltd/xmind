/* ******************************************************************************
 * Copyright (c) 2006-2012 XMind Ltd. and others.
 * 
 * This file is a part of XMind 3. XMind releases 3 and
 * above are dual-licensed under the Eclipse Public License (EPL),
 * which is available at http://www.eclipse.org/legal/epl-v10.html
 * and the GNU Lesser General Public License (LGPL), 
 * which is available at http://www.gnu.org/licenses/lgpl.html
 * See http://www.xmind.net/license.html for details.
 * 
 * Contributors:
 *     XMind Ltd. - initial API and implementation
 *******************************************************************************/
package org.xmind.ui.internal.actions;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.printing.PrintDialog;
import org.eclipse.swt.printing.PrinterData;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.actions.ActionFactory;
import org.xmind.gef.IGraphicalViewer;
import org.xmind.gef.ui.actions.PageAction;
import org.xmind.gef.ui.editor.IGraphicalEditor;
import org.xmind.gef.ui.editor.IGraphicalEditorPage;
import org.xmind.ui.internal.print.PageSetupDialog;
import org.xmind.ui.internal.print.PrintClient;
import org.xmind.ui.internal.print.PrintConstants;
import org.xmind.ui.mindmap.IMindMap;

public class PrintMapAction extends PageAction {

    public PrintMapAction(IGraphicalEditorPage page) {
        super(ActionFactory.PRINT.getId(), page);
        setActionDefinitionId(ActionFactory.PRINT.getCommandId());
    }

    private IMindMap findMindMap(IGraphicalEditorPage page) {
        IMindMap map = (IMindMap) page.getAdapter(IMindMap.class);
        if (map != null)
            return map;

        if (page.getInput() instanceof IMindMap)
            return (IMindMap) page.getInput();

        IGraphicalViewer viewer = page.getViewer();
        if (viewer != null) {
            map = (IMindMap) viewer.getAdapter(IMindMap.class);
            if (map != null)
                return map;

            if (viewer.getInput() instanceof IMindMap)
                return (IMindMap) viewer.getInput();
        }
        return null;
    }

    public void run() {
        final IGraphicalEditor editor = getEditor();
        if (editor == null)
            return;

        IGraphicalEditorPage page = getPage();
        if (page == null)
            return;

//        IGraphicalViewer viewer = page.getViewer();
//        if (viewer == null || !(viewer instanceof IMindMapViewer))
//            return;
//
//        IMindMapViewer mmv = (IMindMapViewer) viewer;
//        IMindMap mindMap = mmv.getMindMap();
//        if (mindMap == null)
//            return;

        Shell parentShell = editor.getSite().getShell();
        if (parentShell == null || parentShell.isDisposed())
            return;

        final IMindMap mindMap = findMindMap(page);
        if (mindMap == null)
            return;

        PageSetupDialog pageSetupDialog = new PageSetupDialog(parentShell,
                mindMap);

        while (true) {
            int open = pageSetupDialog.open();
            if (open == PageSetupDialog.CANCEL)
                return;

            final IDialogSettings settings = pageSetupDialog.getSettings();
            PrinterData printerData = new PrinterData();
            try {
                printerData.orientation = settings
                        .getInt(PrintConstants.ORIENTATION);
            } catch (NumberFormatException e) {
                printerData.orientation = PrinterData.LANDSCAPE;
            }
            PrintDialog printDialog = new PrintDialog(parentShell);
            printDialog.setPrinterData(printerData);
            printerData = printDialog.open();
            if (printerData != null) {
                final PrintClient client = new PrintClient(getJobName(mindMap),
                        parentShell, printerData, settings);
                Display display = parentShell.getDisplay();

                try {
                    BusyIndicator.showWhile(display, new Runnable() {
                        public void run() {
                            if (settings
                                    .getBoolean(PrintConstants.CONTENTWHOLE)) {
                                IGraphicalEditorPage[] pages = editor
                                        .getPages();
                                for (int i = 0; i < pages.length; i++) {

                                    client.print(findMindMap(pages[i]));
                                }
                            } else {
                                client.print(mindMap);
                            }
                            return;
                        }
                    });
                    return;
                } finally {
                    client.dispose();
                }
            }

        }
    }

    private String getJobName(IMindMap map) {
        return map.getCentralTopic().getTitleText().replaceAll("\r\n|\r|\n", //$NON-NLS-1$
                " "); //$NON-NLS-1$
    }
}