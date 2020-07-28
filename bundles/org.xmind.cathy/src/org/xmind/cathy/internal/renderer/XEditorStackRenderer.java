package org.xmind.cathy.internal.renderer;

import org.eclipse.draw2d.TextUtilities;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.workbench.renderers.swt.StackRenderer;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.resource.ResourceManager;
import org.eclipse.jface.util.Util;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.ui.PlatformUI;
import org.xmind.cathy.internal.CathyPlugin;
import org.xmind.cathy.internal.WorkbenchMessages;
import org.xmind.ui.internal.utils.CommandUtils;
import org.xmind.ui.resources.ColorUtils;

public class XEditorStackRenderer extends StackRenderer {

    private static final int CORNER = 5;

    private ResourceManager resources;

    private Rectangle buttonBounds;

    private Color buttonBackground;

    @Override
    public Object createWidget(MUIElement element, Object parent) {
        final CTabFolder ctf = (CTabFolder) super.createWidget(element, parent);
        resources = new LocalResourceManager(JFaceResources.getResources(),
                ctf);
        buttonBackground = (Color) resources.get(
                ColorUtils.toDescriptor(Util.isMac() ? "#ffffff" : "#e1e1e1")); //$NON-NLS-1$ //$NON-NLS-2$

        ctf.addPaintListener(new PaintListener() {

            @Override
            public void paintControl(PaintEvent e) {

                if (!isNullEditorState(ctf)) {
                    return;
                }

                /// Set focus with no edit content, let the dashboard lose focus (Shift + Command + C key binding )
                if (e.display.getActiveShell() == ctf.getShell()) {
                    ctf.setFocus();
                }

                /// paint null-content-tip area (size: 144 * 172)
                GC gc = e.gc;
                boolean oldAdvanced = gc.getAdvanced();
                int oldAntialias = gc.getAntialias();
                try {
                    gc.setAdvanced(true);
                    gc.setAntialias(SWT.ON);

                    /// compute size & location
                    Rectangle clientArea = ctf.getClientArea();

                    Image image = (Image) resources.get(CathyPlugin
                            .imageDescriptorFromPlugin(CathyPlugin.PLUGIN_ID,
                                    "icons/views/null_editor_tip.png")); //$NON-NLS-1$
                    Point imageSize = new Point(image.getBounds().width,
                            image.getBounds().height);

                    Dimension textExtents = TextUtilities.INSTANCE
                            .getTextExtents(
                                    WorkbenchMessages.XStackRenderer_BottomArea_Add_button,
                                    gc.getFont());
                    Point buttonSize = new Point(
                            Math.max(128, textExtents.width + 10),
                            Util.isMac() ? 21 : 25);

                    /// drag tip image
                    Point imagePos = new Point(
                            clientArea.x + (clientArea.width - imageSize.x) / 2,
                            clientArea.y + (clientArea.height
                                    - (imageSize.y + 40 + buttonSize.y)) / 2);
                    gc.drawImage(image, imagePos.x, imagePos.y);

                    /// draw create button
                    Point buttonPos = new Point(
                            clientArea.x
                                    + (clientArea.width - buttonSize.x) / 2,
                            imagePos.y + image.getBounds().height + 40);
                    buttonBounds = new Rectangle(buttonPos.x, buttonPos.y,
                            buttonSize.x, buttonSize.y);

                    drawButton(gc, buttonBounds);

                    /// draw text
                    Point textPos = new Point(buttonBounds.x
                            + (buttonBounds.width - textExtents.width) / 2,
                            buttonBounds.y
                                    + (buttonBounds.height - textExtents.height)
                                            / 2);

                    gc.setBackground(buttonBackground);
                    gc.setForeground((Color) resources
                            .get(ColorUtils.toDescriptor("#000000"))); //$NON-NLS-1$
                    gc.drawText(
                            WorkbenchMessages.XStackRenderer_BottomArea_Add_button,
                            textPos.x, textPos.y);

                } finally {
                    gc.setAdvanced(oldAdvanced);
                    gc.setAntialias(oldAntialias);
                }
            }
        });

        ctf.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseDown(MouseEvent e) {
                if (isNullEditorState(ctf) && buttonBounds != null
                        && buttonBounds.contains(e.x, e.y)) {
                    CommandUtils.executeCommand(
                            "org.xmind.ui.command.newWorkbook", //$NON-NLS-1$
                            PlatformUI.getWorkbench()
                                    .getActiveWorkbenchWindow());
                }
            }
        });

        return ctf;
    }

    private boolean isNullEditorState(CTabFolder ctf) {
        return ctf.getItemCount() == 0;
    }

    private void drawButton(GC gc, Rectangle buttonBounds) {
        if (!Util.isMac()) {
            gc.setForeground(
                    (Color) resources.get(ColorUtils.toDescriptor("#f0f0f0"))); //$NON-NLS-1$
            gc.drawRectangle(buttonBounds.x - 1, buttonBounds.y - 1,
                    buttonBounds.width + 2, buttonBounds.height + 2);
        }

        gc.setBackground(buttonBackground);
        gc.setForeground(
                (Color) resources.get(ColorUtils.toDescriptor("#adadad"))); //$NON-NLS-1$
        if (Util.isMac()) {
            gc.fillRoundRectangle(buttonBounds.x, buttonBounds.y,
                    buttonBounds.width, buttonBounds.height, CORNER, CORNER);
            gc.drawRoundRectangle(buttonBounds.x, buttonBounds.y,
                    buttonBounds.width, buttonBounds.height, CORNER, CORNER);
        } else {
            gc.fillRectangle(buttonBounds);
            gc.drawRectangle(buttonBounds);
        }
    }

}
