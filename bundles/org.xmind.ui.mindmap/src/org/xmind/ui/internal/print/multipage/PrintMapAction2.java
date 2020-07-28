package org.xmind.ui.internal.print.multipage;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.util.Util;
import org.eclipse.osgi.util.NLS;
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
import org.xmind.ui.internal.MindMapUIPlugin;
import org.xmind.ui.internal.print.PrintClient;
import org.xmind.ui.internal.print.PrintConstants;
import org.xmind.ui.mindmap.IMindMap;

/**
 * Support single page print and multi page print of map.
 * 
 * @author Shawn
 */
public class PrintMapAction2 extends PageAction {

    private String logMessage = ""; //$NON-NLS-1$

    public PrintMapAction2(IGraphicalEditorPage page) {
        super(ActionFactory.PRINT.getId(), page);
        setActionDefinitionId(ActionFactory.PRINT.getCommandId());
    }

    public void run() {
        try {
            appendLog("[print] start..."); //$NON-NLS-1$

            final IGraphicalEditor editor = getEditor();
            if (editor == null) {
                appendLog("[print] editor is null...over..."); //$NON-NLS-1$
                return;
            }

            Shell parentShell = editor.getSite().getShell();
            if (parentShell == null || parentShell.isDisposed()) {
                appendLog("[print] parent shell is null...over..."); //$NON-NLS-1$
                return;
            }

            IGraphicalEditorPage page = editor.getActivePageInstance();
            if (page == null) {
                appendLog("[print] editor page is null...over..."); //$NON-NLS-1$
                return;
            }

            final IMindMap mindMap = findMindMap(page);
            if (mindMap == null) {
                appendLog("[print] mind map is null...over..."); //$NON-NLS-1$
                return;
            }

            MultipageSetupDialog pageSetupDialog = new MultipageSetupDialog(
                    parentShell, page, mindMap);
            int open = pageSetupDialog.open();
            appendLog("[print] page setup dialog shown..."); //$NON-NLS-1$
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
            appendLog("[print] print dialog shown..."); //$NON-NLS-1$

            if (printerData != null) {
                if (settings.getBoolean(PrintConstants.MULTI_PAGES)) {
                    appendLog("[print] multiple page print start..."); //$NON-NLS-1$
                    //multiple page print
                    final MultipagePrintClient client = new MultipagePrintClient(
                            getJobName(mindMap), parentShell, printerData,
                            settings);
                    try {
                        if (settings.getBoolean(PrintConstants.CONTENTWHOLE)
                                && !settings.getBoolean(
                                        PrintConstants.MULTI_PAGES)) {
                            IGraphicalEditorPage[] pages = editor.getPages();
                            for (int i = 0; i < pages.length; i++) {
                                client.print(findMindMap(pages[i]));
                                appendLog(
                                        NLS.bind("[print] print sheet [{0}]...", //$NON-NLS-1$
                                                i));
                            }
                        } else {
                            client.print(mindMap);
                            appendLog("[print] print sheet..."); //$NON-NLS-1$
                        }
                        appendLog("[print] print success..."); //$NON-NLS-1$
                    } finally {
                        client.dispose();
                    }

                } else {
                    appendLog("[print] single page print start..."); //$NON-NLS-1$
                    //single page print
                    final PrintClient client = new PrintClient(
                            getJobName(mindMap), parentShell, printerData,
                            settings);
                    Display display = parentShell.getDisplay();

                    try {
                        BusyIndicator.showWhile(display, new Runnable() {
                            public void run() {
                                if (settings.getBoolean(
                                        PrintConstants.CONTENTWHOLE)) {
                                    IGraphicalEditorPage[] pages = editor
                                            .getPages();
                                    for (int i = 0; i < pages.length; i++) {
                                        client.print(findMindMap(pages[i]));
                                        appendLog(NLS.bind(
                                                "[print] print sheet [{0}]...", //$NON-NLS-1$
                                                i));
                                    }
                                } else {
                                    client.print(mindMap);
                                    appendLog("[print] print sheet..."); //$NON-NLS-1$
                                }
                                appendLog("[print] print success..."); //$NON-NLS-1$
                                return;
                            }
                        });
                    } finally {
                        client.dispose();
                    }
                }
            } else {
                appendLog("[print] printer data is null...over..."); //$NON-NLS-1$
            }
        } finally {
            log(logMessage);
            logMessage = ""; //$NON-NLS-1$
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

    private void appendLog(String message) {
        logMessage += message;
    }

    private void log(String message) {
        if (Util.isMac()) {
            MindMapUIPlugin.log(null, message);
        }
    }

}
