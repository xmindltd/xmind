package org.xmind.ui.internal.editor;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.UUID;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.xmind.core.Core;
import org.xmind.core.IWorkbook;
import org.xmind.core.io.DirectoryStorage;
import org.xmind.core.io.IStorage;
import org.xmind.core.util.FileUtils;
import org.xmind.ui.internal.MarkerImpExpUtils;
import org.xmind.ui.internal.MindMapUIPlugin;
import org.xmind.ui.internal.dialogs.DialogMessages;
import org.xmind.ui.internal.prefs.MarkerManagerPrefPage;
import org.xmind.ui.internal.protocols.FilePathParser;
import org.xmind.ui.mindmap.IWorkbookRef;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.util.PrefUtils;

public class MME {

    private static final String SUBDIR_WORKBOOKS = "workbooks/"; //$NON-NLS-1$

    /**
     * 
     * @param uri
     * @return
     * @deprecated Use {@link MindMapUI#getEditorInputFactory()}
     */
    public static IEditorInput createEditorInput(URI uri) {
        return new MindMapEditorInput(uri);
    }

    /**
     * 
     * @param workbookRef
     * @return
     * @deprecated Use {@link MindMapUI#getEditorInputFactory()}
     */
    public static IEditorInput createEditorInput(IWorkbookRef workbookRef) {
        return new MindMapEditorInput(workbookRef);
    }

    /**
     * 
     * @param workbook
     * @return
     * @deprecated Use {@link MindMapUI#getEditorInputFactory()}
     */
    public static IEditorInput createLoadedEditorInput(IWorkbook workbook) {
        return new MindMapEditorInput(
                PreLoadedWorkbookRef.createFromLoadedWorkbook(workbook, null));
    }

    /**
     * 
     * @param name
     * @param workbook
     * @return
     * @deprecated Use {@link MindMapUI#getEditorInputFactory()}
     */
    public static IEditorInput createLoadedEditorInput(String name,
            IWorkbook workbook) {
        return new MindMapEditorInput(
                PreLoadedWorkbookRef.createFromLoadedWorkbook(workbook, name));
    }

    /**
     * 
     * @return
     * @deprecated Use {@link MindMapUI#getEditorInputFactory()}
     */
    public static IEditorInput createNonExistingEditorInput() {
        return MindMapUI.getEditorInputFactory().createDefaultEditorInput();
    }

    /**
     * @param name
     * @return
     * @deprecated Use {@link MindMapUI#getEditorInputFactory()}
     */
    @Deprecated
    public static IEditorInput createNamedEditorInput(String name) {
        return MindMapUI.getEditorInputFactory().createDefaultEditorInput();
    }

    /**
     * @deprecated Use {@link MindMapUI#getEditorInputFactory()}
     */
    @Deprecated
    public static IEditorInput createTemplatedEditorInput(
            InputStream templateStream) {
        return MindMapUI.getEditorInputFactory().createDefaultEditorInput();

    }

    /**
     * @deprecated Use {@link MindMapUI#getEditorInputFactory()}
     */
    @Deprecated
    public static IEditorInput createTemplatedEditorInput(String name,
            InputStream templateStream) {
        return MindMapUI.getEditorInputFactory().createDefaultEditorInput();

    }

    /**
     * Creates an editor input using the given file. Note that if there's
     * Eclipse IDE running, the result will be an
     * {@link org.eclipse.ui.ide.FileStoreEditorInput}; otherwise an instance of
     * {@link FileEditorInput} will be returned.
     * 
     * @param path
     *            The absolute path of a file
     * @return A new editor input representing the given file
     * @throws CoreException
     *             if the creation failed
     * @deprecated Use {@link MindMapUI#getEditorInputFactory()}
     */
    public static IEditorInput createFileEditorInput(String path)
            throws CoreException {
        if (path == null)
            throw new IllegalArgumentException("Path is null"); //$NON-NLS-1$
        return createFileEditorInput(new File(path));
    }

    /**
     * Creates an editor input using the given file.
     * 
     * @param file
     *            The file
     * @return A new editor input representing the given file.
     * @throws CoreException
     *             if the creation failed
     * @deprecated Use {@link MindMapUI#getEditorInputFactory()}
     */
    public static IEditorInput createFileEditorInput(File file)
            throws CoreException {
        if (file == null)
            throw new IllegalArgumentException("File is null"); //$NON-NLS-1$
        return new MindMapEditorInput(file.toURI());
    }

    /**
     * Creates an editor input using the given file store.
     * <p>
     * <b>IMPORTANT:</b> This method should ONLY be called when there's Eclipse
     * IDE in the runtime environment.
     * </p>
     * 
     * @param fileStore
     * @return
     * @throws CoreException
     *             if the creation failed
     * @throws IllegalStateException
     *             if the Eclipse IDE plugin isn't in the runtime environment
     * @deprecated Use {@link MindMapUI#getEditorInputFactory()}
     */
    public static IEditorInput createFileEditorInput(IFileStore fileStore)
            throws CoreException {
        if (fileStore == null)
            throw new IllegalArgumentException("File store is null"); //$NON-NLS-1$
        return new MindMapEditorInput(fileStore.toURI());
    }

    /**
     * Creates an editor input using the given file.
     * <p>
     * <b>IMPORTANT:</b> This method should ONLY be called when there's Eclipse
     * IDE in the runtime environment.
     * </p>
     * 
     * @param file
     *            The file
     * @return A new editor input representing the given file.
     * @throws CoreException
     *             if the creation failed
     * @throws IllegalStateException
     *             if the Eclipse IDE isn't in the runtime environment
     * @deprecated Use {@link MindMapUI#getEditorInputFactory()}
     */
    public static IEditorInput createFileEditorInput(IFile file)
            throws CoreException {
        if (file == null)
            throw new IllegalArgumentException("File is null"); //$NON-NLS-1$
        return new MindMapEditorInput(file.getLocationURI());
    }

    /**
     * 
     * @param uri
     * @return
     * @deprecated Use {@link MindMapUI#getEditorInputFactory()}
     */
    public static IEditorInput createEditorInputFromURI(String uri) {
        URI theURI;
        try {
            theURI = new URI(uri);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(e);
        }
        return new MindMapEditorInput(theURI);
    }

    public static File getFile(Object input) {
        File file = MindMapUIPlugin.getAdapter(input, File.class);
        if (file != null)
            return file;

        IFileStore fileStore = MindMapUIPlugin.getAdapter(input,
                IFileStore.class);
        if (fileStore != null) {
            URI uri = fileStore.toURI();
            if (FilePathParser.URI_SCHEME.equals(uri.getScheme()))
                return new File(uri);
        }

        URI uri = MindMapUIPlugin.getAdapter(input, URI.class);
        if (uri != null && FilePathParser.URI_SCHEME.equals(uri.getScheme())) {
            return new File(uri);
        }
        return null;
    }

    public static IFileStore getFileStore(Object input) {
        IFileStore fileStore = MindMapUIPlugin.getAdapter(input,
                IFileStore.class);
        if (fileStore == null) {
            File file = MindMapUIPlugin.getAdapter(input, File.class);
            if (file != null) {
                try {
                    fileStore = EFS.getStore(file.toURI());
                } catch (CoreException ignore) {
                }
            }
        }

        if (fileStore == null) {
            URI uri = MindMapUIPlugin.getAdapter(input, URI.class);
            if (uri != null) {
                try {
                    fileStore = EFS.getStore(uri);
                } catch (CoreException ignore) {
                }
            }
        }

        return fileStore;
    }

    public static URI getURIFromEditorInput(IEditorInput input) {
        URI uri = MindMapUIPlugin.getAdapter(input, URI.class);
        if (uri != null)
            return uri;

        IWorkbookRef workbookRef = MindMapUIPlugin.getAdapter(input,
                IWorkbookRef.class);
        if (workbookRef != null)
            return workbookRef.getURI();

        File file = input.getAdapter(File.class);
        if (file != null)
            return file.toURI();

        return null;
    }

    /**
     * Launches a local file at the specified path.
     * 
     * @param window
     * @param path
     * @param fileName
     */
    public static void launch(IWorkbenchWindow window, String path,
            String fileName) {
        File file = new File(path);
        if (!file.exists()) {
            if (Display.getCurrent() != null) {
                if (!MessageDialog.openConfirm(window.getShell(),
                        DialogMessages.InfoFileNotExists_title,
                        NLS.bind(DialogMessages.InfoFileNotExists_message,
                                path))) {
                    return;
                }
            }
        }
        String extension = FileUtils.getExtension(path);
        if (MindMapUI.FILE_EXT_TEMPLATE.equalsIgnoreCase(extension)) {
            if (window != null && Display.getCurrent() != null) {
                if (openTemplate(window, path, fileName))
                    return;
            }
        } else if (MindMapUI.FILE_EXT_XMIND.equalsIgnoreCase(extension)) {
            if (window != null && Display.getCurrent() != null) {
                if (openMindMap(window, path, fileName))
                    return;
            }
        } else if (MindMapUI.FILE_EXT_MARKER_PACKAGE
                .equalsIgnoreCase(extension)) {
            if (importMarkers(path))
                return;
        }

        org.xmind.ui.viewers.FileUtils.launch(file.getAbsolutePath());
    }

    /**
     * @param window
     * @param path
     */
    private static boolean openTemplate(IWorkbenchWindow window, String path,
            String fileName) {
        return openMindMap(window, path, fileName);
    }

    /**
     * @param window
     * @param path
     */
    private static boolean openMindMap(final IWorkbenchWindow window,
            final String path, final String fileName) {
        String errMessage = NLS
                .bind(DialogMessages.FailedToLoadWorkbook_message, path);
        final boolean[] ret = new boolean[1];
        SafeRunner.run(new SafeRunnable(errMessage) {
            public void run() throws Exception {
                window.getActivePage()
                        .openEditor(MindMapUI.getEditorInputFactory()
                                .createEditorInputForFile(new File(path)),
                                MindMapUI.MINDMAP_EDITOR_ID);
                ret[0] = true;
            }
        });
        return ret[0];
    }

    /**
     * @param path
     */
    private static boolean importMarkers(String path) {
        try {
            MarkerImpExpUtils.importMarkerPackage(path);

            Display display = Display.getCurrent();
            if (display != null) {
                display.asyncExec(new Runnable() {
                    public void run() {
                        PrefUtils.openPrefDialog(null,
                                MarkerManagerPrefPage.ID);
                    }
                });
            }
            return true;
        } catch (IOException e) {
        }
        return false;
    }

    public static IStorage createTempStorage() {
        String tempName = UUID.randomUUID().toString()
                + MindMapUI.FILE_EXT_XMIND_TEMP;
        String tempDirPath = Core.getWorkspace()
                .getTempDir(SUBDIR_WORKBOOKS + tempName);
        return new DirectoryStorage(new File(tempDirPath));
    }

    /**
     * 
     * @param workbook
     * @param editors
     * @return
     * @deprecated Use IWorkbookRef instances instead of IWorkbook instances
     *             where possible within the workbench
     */
    @Deprecated
    public static IEditorInput getEditorInputWithWorkbookAndEditors(
            IWorkbook workbook, IEditorReference[] editors) {
        for (IEditorReference ep : editors) {
            try {
                IEditorInput editorInput = ep.getEditorInput();
                if (editorInput != null && editorInput
                        .getAdapter(IWorkbook.class) == workbook) {
                    return editorInput;
                }
            } catch (PartInitException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

}
