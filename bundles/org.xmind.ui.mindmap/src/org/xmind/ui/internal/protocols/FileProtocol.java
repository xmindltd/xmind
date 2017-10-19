package org.xmind.ui.internal.protocols;

import java.io.File;
import java.net.URI;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.URIUtil;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.xmind.core.IWorkbook;
import org.xmind.ui.internal.MindMapMessages;
import org.xmind.ui.internal.editor.MME;
import org.xmind.ui.mindmap.IHyperlinked;
import org.xmind.ui.mindmap.IMindMapImages;
import org.xmind.ui.mindmap.IProtocol;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.util.MindMapUtils;

public class FileProtocol implements IProtocol {

    private static class OpenFileAction extends Action implements IHyperlinked {

        private IWorkbenchWindow window;

        private String path;

        public OpenFileAction(IWorkbenchWindow window, String path) {
            this.window = window;
            this.path = path;
        }

        public void run() {
            MME.launch(window, path, new File(path).getName());
        }

        @Override
        public String getHyperlink() {
            return path;
        }
    }

    public FileProtocol() {
    }

    public IAction createOpenHyperlinkAction(Object context, final String uri) {
        IWorkbenchWindow window = getWindow(context);
        String path = FilePathParser.toPath(uri);
        String absolutePath = getAbsolutePath(context, path);
        File file = new File(absolutePath);
        ImageDescriptor image = MindMapUI.getImages().getFileIcon(absolutePath,
                true);
        if (image == null) {
            if (file.isDirectory()) {
                image = MindMapUI.getImages().get(IMindMapImages.OPEN, true);
            } else {
                image = MindMapUI.getImages().get(IMindMapImages.UNKNOWN_FILE,
                        true);
            }
        }
        String text;
        if (file.isDirectory()) {
            text = MindMapMessages.FileProtocol_OpenFolder_text;
        } else {
            text = MindMapMessages.FileProtocol_OpenFile_text;
        }
        OpenFileAction action = new OpenFileAction(window, absolutePath);
        action.setText(text);
        action.setImageDescriptor(image);
        action.setToolTipText(absolutePath);
        return action;
    }

    public static String getAbsolutePath(Object context, String path) {
        if (FilePathParser.isPathRelative(path)) {
            //TODO FIXME
            IWorkbook workbook = MindMapUtils.findWorkbook(context);
            if (workbook != null) {
                String base = null;
                URI fileURI = getFileURIFrom(workbook);
                if (fileURI != null && URIUtil.isFileURI(fileURI)) {
                    base = URIUtil.toFile(fileURI).getAbsolutePath();
                }
                if (base != null) {
                    base = new File(base).getParent();
                    if (base != null) {
                        return FilePathParser.toAbsolutePath(base, path);
                    }
                }
            }
            return FilePathParser
                    .toAbsolutePath(FilePathParser.ABSTRACT_FILE_BASE, path);
        }
        return path;
    }

    private static URI getFileURIFrom(IWorkbook workbook) {
        IWorkbench workbench = PlatformUI.getWorkbench();
        IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();
        if (window == null) {
            IWorkbenchWindow[] windows = workbench.getWorkbenchWindows();
            if (windows != null && windows.length != 0) {
                window = windows[0];
            }
        }
        if (window == null) {
            return null;
        }
        IWorkbenchPage[] pages = window.getPages();
        for (IWorkbenchPage wp : pages) {
            IEditorReference[] ers = wp.getEditorReferences();
            for (IEditorReference er : ers) {
                IEditorInput editorInput = null;
                try {
                    editorInput = er.getEditorInput();
                } catch (PartInitException e) {
                    e.printStackTrace();
                }
                if (editorInput == null)
                    continue;
                IWorkbook w = editorInput.getAdapter(IWorkbook.class);
                if (workbook.equals(w)) {
                    return editorInput.getAdapter(URI.class);
                }
            }
        }
        return null;
    }

    private static IWorkbenchWindow getWindow(Object context) {
        if (context instanceof IAdaptable) {
            Object adapter = ((IAdaptable) context)
                    .getAdapter(IWorkbenchWindow.class);
            if (adapter == null) {
                adapter = ((IAdaptable) context).getAdapter(IEditorPart.class);
                if (adapter == null) {
                    adapter = ((IAdaptable) context)
                            .getAdapter(IWorkbenchPart.class);
                }
                if (adapter instanceof IWorkbenchPart)
                    adapter = ((IWorkbenchPart) adapter).getSite()
                            .getWorkbenchWindow();
            }
            if (adapter instanceof IWorkbenchWindow)
                return (IWorkbenchWindow) adapter;
        }
        if (context instanceof IWorkbenchWindow)
            return (IWorkbenchWindow) context;
        return PlatformUI.getWorkbench().getActiveWorkbenchWindow();
    }

    public boolean isHyperlinkModifiable(Object source, String uri) {
        return true;
    }
}
