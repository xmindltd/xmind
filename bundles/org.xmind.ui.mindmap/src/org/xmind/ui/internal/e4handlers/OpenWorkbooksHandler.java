package org.xmind.ui.internal.e4handlers;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.xmind.core.internal.UserDataConstants;
import org.xmind.ui.commands.MindMapCommandConstants;
import org.xmind.ui.internal.MindMapMessages;
import org.xmind.ui.internal.MindMapUIPlugin;
import org.xmind.ui.internal.dialogs.DialogUtils;
import org.xmind.ui.mindmap.IWorkbookRef;
import org.xmind.ui.mindmap.IWorkbookRefFactory;
import org.xmind.ui.mindmap.MindMapUI;

public class OpenWorkbooksHandler {

    private static final List<String> NO_URIS = Collections.emptyList();
    private static final String LOCAL_FILE_SCHEME = "file"; //$NON-NLS-1$

    @Inject
    public void execute(final IWorkbenchWindow window,
            ParameterizedCommand command) {
        String uri = (String) command.getParameterMap()
                .get(MindMapCommandConstants.OPEN_WORKBOOK_PARAM_URI);
        execute(window, uri);
    }

    public static void execute(IWorkbenchWindow window, String uri) {
        execute(window, uri == null ? NO_URIS : Arrays.asList(uri));
    }

    public static void execute(IWorkbenchWindow window, List<String> uris) {
        if (window == null)
            return;

        IWorkbenchPage page = window.getActivePage();
        if (page == null)
            return;

        if (uris.isEmpty()) {
            List<File> files = DialogUtils.openXMindFiles(
                    Display.getDefault().getActiveShell(), SWT.MULTI);
            uris = new ArrayList<String>(files.size());
            for (File file : files) {
                uris.add(file.toURI().toString());
                MindMapUIPlugin.getDefault().getUsageDataCollector().trackEvent(
                        UserDataConstants.CATEGORY_WORKBOOK,
                        UserDataConstants.OPEN_FILE);
            }
        }

        IEditorPart lastEditor = null;
        for (String uri : uris) {
            if (uri != null) {
                IEditorPart editor = openMindMapEditor(page, uri);
                if (editor != null) {
                    lastEditor = editor;
                }
            }
        }
        if (lastEditor != null) {
            page.activate(lastEditor);
        }

    }

    private static IEditorPart openMindMapEditor(final IWorkbenchPage page,
            final String uri) {
        final IEditorPart[] editor = new IEditorPart[1];
        SafeRunner.run(new SafeRunnable() {
            public void run() throws Exception {

                if (uri == null || "".equals(uri)) //$NON-NLS-1$
                    return;

                URI workbookURI = URI.create(uri);
                IWorkbookRefFactory factory = MindMapUIPlugin.getDefault()
                        .getWorkbookRefFactory();
                IWorkbookRef workbookRef = factory
                        .createWorkbookRef(workbookURI, null);

                if (!workbookRef.exists()) {
                    showFileNotFoundDialog(workbookURI);
                    return;
                }

                IEditorInput input = MindMapUI.getEditorInputFactory()
                        .createEditorInput(workbookURI);
                editor[0] = page.openEditor(input, MindMapUI.MINDMAP_EDITOR_ID,
                        false);
            }
        });
        return editor[0];
    }

    private static void showFileNotFoundDialog(URI uri) {
        Assert.isNotNull(uri);
        String scheme = uri.getScheme();
        if (scheme == null || "".equalsIgnoreCase(scheme)) //$NON-NLS-1$
            return;

        if (LOCAL_FILE_SCHEME.equalsIgnoreCase(scheme))
            MessageDialog.openWarning(null,
                    MindMapMessages.FileNotExistDialog_Title,
                    MindMapMessages.FileNotExistDialog_Message);
    }
}
