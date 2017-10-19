package org.xmind.ui.internal.print.multipage;

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
import org.xmind.ui.internal.print.PrintClient;
import org.xmind.ui.internal.print.PrintConstants;
import org.xmind.ui.mindmap.IMindMap;

/**
 * Support single page print and multi page print of map.
 * 
 * @author Shawn
 *
 */
public class PrintMapAction2 extends PageAction {

    public PrintMapAction2(IGraphicalEditorPage page) {
        super(ActionFactory.PRINT.getId(), page);
        setActionDefinitionId(ActionFactory.PRINT.getCommandId());
    }

    public void run() {
        final IGraphicalEditor editor = getEditor();
        if (editor == null) {
            return;
        }

        Shell parentShell = editor.getSite().getShell();
        if (parentShell == null || parentShell.isDisposed()) {
            return;
        }

        IGraphicalEditorPage page = editor.getActivePageInstance();
        if (page == null) {
            return;
        }

        final IMindMap mindMap = findMindMap(page);
        if (mindMap == null) {
            return;
        }

        MultipageSetupDialog pageSetupDialog = new MultipageSetupDialog(
                parentShell, page, mindMap);

        int open = pageSetupDialog.open();
        if (open == MultipageSetupDialog.CANCEL)
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
            if (settings.getBoolean(PrintConstants.MULTI_PAGES)) {
                //multiple page print
                final MultipagePrintClient client = new MultipagePrintClient(
                        getJobName(mindMap), parentShell, printerData,
                        settings);
                try {
                    if (settings.getBoolean(PrintConstants.CONTENTWHOLE)
                            && !settings
                                    .getBoolean(PrintConstants.MULTI_PAGES)) {
                        IGraphicalEditorPage[] pages = editor.getPages();
                        for (int i = 0; i < pages.length; i++) {
                            client.print(findMindMap(pages[i]));
                        }
                    } else {
                        client.print(mindMap);
                    }
                } finally {
                    client.dispose();
                }

            } else {
                //single page print
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
                } finally {
                    client.dispose();
                }
            }
        }
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

    private String getJobName(IMindMap map) {
        return map.getCentralTopic().getTitleText().replaceAll("\r\n|\r|\n", //$NON-NLS-1$
                " "); //$NON-NLS-1$
    }

}
