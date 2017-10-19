package org.xmind.ui.internal.editor;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.Platform;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.xmind.ui.internal.dialogs.DialogMessages;
import org.xmind.ui.internal.dialogs.DialogUtils;
import org.xmind.ui.internal.protocols.FilePathParser;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.wizards.ISaveContext;
import org.xmind.ui.wizards.ISaveWizard;
import org.xmind.ui.wizards.SaveOptions;

/**
 * @author Frank Shaka
 * @since 3.6.50
 */
public class LocalFileSaveWizard implements ISaveWizard {

    public static final String ID = "org.xmind.ui.saveWizards.localFile"; //$NON-NLS-1$

    public LocalFileSaveWizard() {
    }

    @Override
    public URI askForTargetURI(ISaveContext context, SaveOptions options) {
        String proposalName = options.proposalName();
        URI oldURI = options.oldURI();

        // Hide busy cursor
        List<Shell> cursorHiddenShells = new ArrayList<Shell>();
        Display display = Display.getCurrent();
        Shell[] shells = display.getShells();
        Cursor busyCursor = display.getSystemCursor(SWT.CURSOR_WAIT);
        for (Shell shell : shells) {
            Cursor cursor = shell.getCursor();
            if (cursor != null && cursor.equals(busyCursor)) {
                shell.setCursor(null);
                cursorHiddenShells.add(shell);
            }
        }

        // Show save dialog
        String filterExtension = MindMapUI.FILE_EXT_XMIND;
        String extensionFullName = "*" + filterExtension; //$NON-NLS-1$
        String filterFullName;
        if (Platform.OS_MACOSX.equals(Platform.getOS())) {
            filterFullName = NLS.bind("{0} ({1})", //$NON-NLS-1$
                    DialogMessages.WorkbookFilterName, extensionFullName);
        } else {
            filterFullName = DialogMessages.WorkbookFilterName;
        }

        String dirPath = null;
        if (oldURI != null
                && FilePathParser.URI_SCHEME.equals(oldURI.getScheme())) {
            dirPath = new File(oldURI).getParent();
        }

        Shell parentShell = display.getActiveShell() != null
                ? display.getActiveShell() : new Shell();
        String result = DialogUtils.save(parentShell, proposalName,
                new String[] { extensionFullName },
                new String[] { filterFullName }, 0, dirPath);
        if (result == null)
            return null;

        if ("win32".equals(SWT.getPlatform())) { //$NON-NLS-1$
            if (!result.endsWith(filterExtension)) {
                result = result + filterExtension;
            }
        }
        return new File(result).toURI();
    }

    public int getPriorityFor(ISaveContext context, SaveOptions options) {
        return 50;
    }

}
