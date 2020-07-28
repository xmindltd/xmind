/* ******************************************************************************
 * Copyright (c) 2006-2012 XMind Ltd. and others.
 * 
 * This file is a part of XMind 3. XMind releases 3 and
 * above are dual-licensed under the Eclipse Public License (EPL),
 * which is available at http://www.eclipse.org/legal/epl-v10.html
 * and the GNU Lesser General Public License (LGPL), 
 * which is available at http://www.gnu.org/licenses/lgpl.html
 * See https://www.xmind.net/license.html for details.
 * 
 * Contributors:
 *     XMind Ltd. - initial API and implementation
 *******************************************************************************/
package org.xmind.ui.internal.views;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.UUID;

import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.xmind.core.Core;
import org.xmind.core.IBoundary;
import org.xmind.core.IControlPoint;
import org.xmind.core.IRelationship;
import org.xmind.core.ISheet;
import org.xmind.core.ITopic;
import org.xmind.core.IWorkbook;
import org.xmind.core.io.DirectoryStorage;
import org.xmind.core.io.IStorage;
import org.xmind.core.style.IStyle;
import org.xmind.core.style.IStyleSheet;
import org.xmind.gef.draw2d.graphics.GraphicsUtils;
import org.xmind.gef.image.ImageExportUtils;
import org.xmind.gef.image.ResizeConstants;
import org.xmind.ui.internal.MindMapMessages;
import org.xmind.ui.internal.MindMapUIPlugin;
import org.xmind.ui.mindmap.MindMap;
import org.xmind.ui.mindmap.MindMapImageExporter;
import org.xmind.ui.util.ImageFormat;

public class ThemeFigure extends Figure {

    private class ThemePreview {

        private IWorkbook previewWorkbook;

        private IStorage previewStorage;

        private File previewFile;

        public ThemePreview(IStyle theme) {
            initPreviewStorage();
            initPreview();
        }

        private void initPreviewStorage() {
            File root = MindMapUIPlugin.getDefault().getStateLocation()
                    .toFile();
            previewFile = new File(root, ".themePreview"); //$NON-NLS-1$
            File dir = new File(previewFile, UUID.randomUUID().toString());
            dir.mkdirs();
            previewStorage = new DirectoryStorage(dir);
            previewWorkbook = Core.getWorkbookBuilder()
                    .createWorkbook(previewStorage);
//            previewWorkbook.setTempStorage(previewStorage);
        }

        private void initPreview() {
            ISheet sheet = previewWorkbook.getPrimarySheet();
            createPreviewContents(previewWorkbook, sheet);
        }

        private void createPreviewContents(IWorkbook workbook, ISheet sheet) {
            IStyleSheet styleSheet = workbook.getStyleSheet();
            IStyle importStyle = styleSheet.importStyle(theme);

            if (importStyle != null)
                sheet.setThemeId(importStyle.getId());

            ITopic rootTopic = sheet.getRootTopic();
            rootTopic.setTitleText(MindMapMessages.TitleText_CentralTopic);
            rootTopic.setStructureClass("org.xmind.ui.map.clockwise"); //$NON-NLS-1$

            ITopic mainTopic1 = workbook.createTopic();
            mainTopic1.setTitleText(
                    NLS.bind(MindMapMessages.TitleText_MainTopic, 1));
            rootTopic.add(mainTopic1);

            ITopic subTopic1 = workbook.createTopic();
            subTopic1.setTitleText(
                    NLS.bind(MindMapMessages.TitleText_Subtopic, 1));
            mainTopic1.add(subTopic1);

            ITopic subTopic2 = workbook.createTopic();
            subTopic2.setTitleText(
                    NLS.bind(MindMapMessages.TitleText_Subtopic, 2));
            mainTopic1.add(subTopic2);

            ITopic subTopic3 = workbook.createTopic();
            subTopic3.setTitleText(
                    NLS.bind(MindMapMessages.TitleText_Subtopic, 3));
            mainTopic1.add(subTopic3);

            ITopic floatingTopic = workbook.createTopic();
            floatingTopic.setTitleText(MindMapMessages.TitleText_FloatingTopic);
            floatingTopic.setPosition(0, -120);
            rootTopic.add(floatingTopic, ITopic.DETACHED);

            IBoundary boundary = workbook.createBoundary();
            boundary.setTitleText(MindMapMessages.TitleText_Boundary);
            boundary.setStartIndex(0);
            boundary.setEndIndex(0);
            rootTopic.addBoundary(boundary);

            IRelationship relationship = workbook.createRelationship();
            relationship.setTitleText(MindMapMessages.TitleText_Relationship);
            relationship.setEnd1Id(mainTopic1.getId());
            relationship.setEnd2Id(floatingTopic.getId());
            IControlPoint cp1 = relationship.getControlPoint(0);
            cp1.setPosition(50, -100);
            IControlPoint cp2 = relationship.getControlPoint(1);
            cp2.setPosition(100, 0);
            sheet.addRelationship(relationship);
        }

        public void createPreviewImage(int wHint, int hHint, String fileName) {
            MindMapImageExporter exporter = new MindMapImageExporter(
                    Display.getCurrent());
            exporter.setSource(new MindMap(previewWorkbook.getPrimarySheet()),
                    null, null);
            exporter.setTargetWorkbook(previewWorkbook);
            Image image = exporter.createImage();

            int width = image.getBounds().width;
            int height = image.getBounds().height;

            float scale = (height * 1.0f / width * 1.0f)
                    / (hHint * 1.0f / wHint * 1.0f);
            if (scale > 1.0) {
                hHint = (int) (scale * hHint);
            } else {
                wHint = (int) (wHint / scale);
            }

            if (image != null) {
                image.dispose();
                image = null;
            }

            MindMapImageExporter exp = new MindMapImageExporter(
                    Display.getCurrent());
            exp.setSource(new MindMap(previewWorkbook.getPrimarySheet()), null,
                    null);
            exp.setTargetWorkbook(previewWorkbook);
            exp.setResize(ResizeConstants.RESIZE_MAXPIXELS, wHint, hHint);
            image = exp.createImage();

            String id = theme.getId();

            File dir = new File(previewFile, id);
            dir.mkdirs();

            File preview = new File(dir, fileName);

            OutputStream output = null;

            try {
                output = new FileOutputStream(preview);
                ImageExportUtils.saveImage(image, output,
                        ImageFormat.BMP.getSWTFormat());
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } finally {
                if (image != null) {
                    image.dispose();
                    image = null;
                }

                try {
                    output.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            clear();
        }

        private void clear() {
            if (previewStorage != null) {
                previewStorage.clear();
                previewStorage = null;
            }

        }

    }

    private static final Rectangle RECT = new Rectangle();

    private IStyle theme = null;

    private Image defaultImage = null;

    public ThemeFigure() {
    }

    public IStyle getTheme() {
        return theme;
    }

    public void setTheme(IStyle theme) {
        if (theme == this.theme)
            return;

        this.theme = theme;
        repaint();
    }

    public Image getDefaultImage() {
        return defaultImage;
    }

    public void setDefaultImage(Image defaultImage) {
        if (defaultImage == this.defaultImage)
            return;
        this.defaultImage = defaultImage;
        repaint();
    }

    public Image getPreviewImage(IStyle theme) {
        return getImageFromSource(theme, new Rectangle(0, 0, 200, 100));
    }

//    public boolean isDefault() {
//        return isDefault;
//    }
//
//    public void setDefault(boolean isDefault) {
//        if (isDefault == this.isDefault)
//            return;
//        this.isDefault = isDefault;
//        repaint();
//    }

    public void paint(Graphics graphics) {
        GraphicsUtils.fixGradientBugForCarbon(graphics, this);
        super.paint(graphics);
    }

    protected void paintFigure(Graphics graphics) {
        super.paintFigure(graphics);
        drawTheme(graphics);
    }

    protected void drawTheme(Graphics graphics) {
        if (theme == null)
            return;

        graphics.setAntialias(SWT.ON);
        graphics.setTextAntialias(SWT.ON);

        Rectangle r = getClientArea(RECT);
        drawTheme(graphics, theme, r);
    }

    protected void drawTheme(Graphics graphics, IStyle theme, Rectangle r) {
        Image image = getImageFromSource(theme, r);

        if (image != null)
            graphics.drawImage(image, r.x, r.y);

        if (defaultImage != null) {
            graphics.drawImage(defaultImage, r.x + 1, r.y + 1);
        }
    }

    private Image getImageFromSource(IStyle theme, Rectangle r) {
        if (this.theme != theme)
            this.theme = theme;

        File root = MindMapUIPlugin.getDefault().getStateLocation().toFile();
        root = new File(root, ".themePreview"); //$NON-NLS-1$

        String id = theme.getId();

        File dir = new File(root, id);

        String previewName = "preview" + r.width + ".bmp"; //$NON-NLS-1$ //$NON-NLS-2$

        String[] list = dir.list();
        if (list == null || !Arrays.asList(list).contains(previewName)) {
            ThemePreview themePreview = new ThemePreview(theme);
            themePreview.createPreviewImage(r.width, r.height, previewName);
        }

        File preview = new File(dir, previewName);

        return new Image(Display.getCurrent(), preview.getAbsolutePath());
    }

}
