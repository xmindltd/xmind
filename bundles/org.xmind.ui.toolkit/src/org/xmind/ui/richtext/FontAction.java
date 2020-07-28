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
package org.xmind.ui.richtext;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Point;
import org.xmind.ui.font.FontDialog;
import org.xmind.ui.font.IFontChooser;
import org.xmind.ui.font.IFontChooserListener;
import org.xmind.ui.internal.ToolkitImages;
import org.xmind.ui.resources.ColorUtils;
import org.xmind.ui.resources.FontUtils;

public class FontAction extends Action implements IRichTextAction {

    private IRichTextEditViewer viewer;

    public FontAction(IRichTextEditViewer viewer) {
        this(viewer, RichTextMessages.FontAction_text, ToolkitImages
                .get(ToolkitImages.FONT), RichTextMessages.FontAction_toolTip);
    }

    public FontAction(IRichTextEditViewer viewer, String text,
            ImageDescriptor image, String tooltip) {
        super(text, AS_CHECK_BOX);
        this.viewer = viewer;
        setId(TextActionConstants.FONT_ID);
        setImageDescriptor(image);
        setToolTipText(tooltip);
    }

    public void dispose() {
        viewer = null;
    }

    public void selectionChanged(IRichTextEditViewer viewer, ISelection selection) {
    }

    public void run() {
        if (viewer == null || viewer.getControl().isDisposed())
            return;

        IRichTextRenderer renderer = viewer.getRenderer();
        Font selectionFont = renderer.getSelectionFont();
        Color selectionForeground = renderer.getSelectionForeground();
        final Color selectionBackground = renderer.getSelectionBackground();
        boolean selectionBold = renderer.getSelectionFontBold();
        boolean selectionItalic = renderer.getSelectionFontItalic();
        boolean selectionUnderline = renderer.getSelectionFontUnderline();
        boolean selectionStrikeout = renderer.getSelectionFontStrikeout();

        FontDialog dialog = new FontDialog(viewer.getControl().getShell());

        if (selectionFont != null) {
            dialog.setInitialFont(selectionFont.getFontData()[0]);
        } else {
            dialog.setInitialFont(RichTextUtils.DEFAULT_FONT_DATA);
        }
        if (selectionForeground != null)
            dialog.setColor(selectionForeground.getRGB());

        if (selectionBold)
            dialog.setBold(selectionBold);
        if (selectionItalic)
            dialog.setItalic(selectionItalic);
        if (selectionStrikeout)
            dialog.setStrikeout(selectionStrikeout);
        if (selectionUnderline)
            dialog.setUnderline(selectionUnderline);

        final StyledText textWidget = viewer.getTextViewer().getTextWidget();
        Point sel = textWidget.getSelectionRange();
        final int start = sel.x;
        final int length = sel.y;
        StyleRange[] oldStyles = textWidget.getStyleRanges(start, length);
        dialog.addFontChooserListener(new IFontChooserListener() {
            public void fontChanged(IFontChooser source) {
                String fontName = source.getFontName();
                int fontHeight = source.getFontHeight();
                boolean bold = source.getBold();
                boolean italic = source.getItalic();
                boolean strikeout = source.getStrikeout();
                boolean underline = source.getUnderline();
                Color foreground = ColorUtils.getColor(source.getColor());
                StyleRange sr = new StyleRange(start, length, foreground,
                        selectionBackground);
                sr.font = FontUtils.getFont(fontName, fontHeight, bold, italic);
                sr.strikeout = strikeout;
                sr.underline = underline;
                textWidget.replaceStyleRanges(start, length,
                        new StyleRange[] { sr });
            }
        });

        int ret = dialog.open();

        textWidget.setRedraw(false);
        textWidget.replaceStyleRanges(start, length, oldStyles);

        if (ret == FontDialog.OK) {
            String fontName = dialog.getFontName();
            int fontHeight = dialog.getFontHeight();
            boolean bold = dialog.getBold();
            boolean italic = dialog.getItalic();
            boolean strikeout = dialog.getStrikeout();
            boolean underline = dialog.getUnderline();
            Color foreground = ColorUtils.getColor(dialog.getColor());
            renderer.setSelectionFont(FontUtils.getFont(fontName, fontHeight,
                    bold, italic));
            renderer.setSelectionForeground(foreground);
            renderer.setSelectionBackground(selectionBackground);
            renderer.setSelectionFontUnderline(underline);
            renderer.setSelectionFontStrikeout(strikeout);
        }
        textWidget.setRedraw(true);

        setChecked(false);
    }

}