package org.xmind.ui.internal.e4handlers;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.swt.SWT;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.xmind.ui.commands.MindMapCommandConstants;
import org.xmind.ui.internal.MindMapUIPlugin;
import org.xmind.ui.internal.dialogs.DialogUtils;
import org.xmind.ui.mindmap.MindMapUI;

public class OpenWorkbooksHandler {

    private static final List<String> NO_URIS = Collections.emptyList();

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
            List<File> files = DialogUtils.openXMindFiles(window.getShell(),
                    SWT.MULTI);
            uris = new ArrayList<String>(files.size());
            for (File file : files) {
                uris.add(file.toURI().toString());
            }
        }

        IEditorPart lastEditor = null;
        for (String uri : uris) {
            if (uri != null) {
                IEditorPart editor = openMindMapEditor(page, uri);
                if (editor != null) {
                    MindMapUIPlugin.getDefault().getUsageDataCollector()
                            .increase("OpenWorkbookCount"); //$NON-NLS-1$
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
                IEditorInput input = MindMapUI.getEditorInputFactory()
                        .createEditorInput(new URI(uri));
                editor[0] = page.openEditor(input, MindMapUI.MINDMAP_EDITOR_ID,
                        false);
            }
        });
        return editor[0];
    }

}
