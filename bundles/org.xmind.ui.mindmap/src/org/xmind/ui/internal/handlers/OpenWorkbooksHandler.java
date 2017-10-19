package org.xmind.ui.internal.handlers;

import java.util.Map;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.program.Program;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.commands.IElementUpdater;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.menus.UIElement;
import org.xmind.core.util.FileUtils;
import org.xmind.ui.commands.MindMapCommandConstants;
import org.xmind.ui.internal.protocols.FilePathParser;

public class OpenWorkbooksHandler extends AbstractHandler
        implements IElementUpdater {

    private static boolean SHOWS_FILE_ICON = false;

    public Object execute(ExecutionEvent event) throws ExecutionException {
        IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindow(event);
        if (window == null)
            return null;

        String uri = event
                .getParameter(MindMapCommandConstants.OPEN_WORKBOOK_PARAM_URI);

        org.xmind.ui.internal.e4handlers.OpenWorkbooksHandler.execute(window,
                uri);

        return null;
    }

    public void updateElement(UIElement element, Map parameters) {
        String uri = (String) parameters
                .get(MindMapCommandConstants.OPEN_WORKBOOK_PARAM_URI);
        if (uri != null) {

            // The default icon for a parameterized Open command should be empty.
            ImageDescriptor icon = null;

            // The following condition block is just for testing purpose.
            // TODO: Should be removed once the design is determined.
            if (FilePathParser.isFileURI(uri) && SHOWS_FILE_ICON) {
                String path = FilePathParser.toPath(uri);
                Program p = Program.findProgram(FileUtils.getExtension(path));
                if (p != null) {
                    ImageData imageData = p.getImageData();
                    icon = ImageDescriptor.createFromImageData(imageData);
                }
            }

            element.setIcon(icon);
        }
    }

}
