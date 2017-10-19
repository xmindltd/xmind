package org.xmind.ui.internal.print;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Display;
import org.xmind.gef.draw2d.RotatableWrapLabel;
import org.xmind.ui.resources.FontUtils;
import org.xmind.ui.util.UnitConvertor;

public class PrintUtils {

    public static int getHeaderHeight(IDialogSettings settings, int targetDpi) {
        String headerText = settings.get(PrintConstants.HEADER_TEXT);
        Font headerFont = getFont(settings, PrintConstants.HEADER_FONT,
                targetDpi);
        int headerHeight = getHeight(headerText, headerFont);
        return headerHeight;
    }

    public static int getBottomHeight(IDialogSettings settings, int targetDpi) {
        int bottomHeight = Math.max(getFooterHeight(settings, targetDpi),
                getPageNumberHeight(settings, targetDpi));
        return bottomHeight;
    }

    private static int getFooterHeight(IDialogSettings settings,
            int targetDpi) {
        String footerText = settings.get(PrintConstants.FOOTER_TEXT);
        Font footerFont = getFont(settings, PrintConstants.FOOTER_FONT,
                targetDpi);
        return getHeight(footerText, footerFont);
    }

    private static int getPageNumberHeight(IDialogSettings settings,
            int targetDpi) {
        boolean multiPages = settings.getBoolean(PrintConstants.MULTI_PAGES);
        if (!multiPages) {
            return 0;
        }
        String pageNumberText = "- 1 -"; //$NON-NLS-1$
        Font pageNumberFont = Display.getCurrent().getSystemFont();
        pageNumberFont = FontUtils.getNewHeight(pageNumberFont,
                (pageNumberFont.getFontData())[0].getHeight() * targetDpi
                        / UnitConvertor.getScreenDpi().y);
        return getHeight(pageNumberText, pageNumberFont);
    }

    private static Font getFont(IDialogSettings settings, String fontKey,
            int targetDpi) {
        Font font = null;
        String fontValue = settings.get(fontKey);
        if (fontValue != null) {
            FontData[] fontData = FontUtils.toFontData(fontValue);
            if (fontData != null) {
                for (FontData fd : fontData) {
                    fd.setHeight(fd.getHeight() * targetDpi
                            / UnitConvertor.getScreenDpi().y);
                }
                font = new Font(Display.getCurrent(), fontData);
            }
        }
        if (font == null) {
            FontData[] defaultFontData = JFaceResources
                    .getDefaultFontDescriptor().getFontData();
            int defaultHeight = defaultFontData[0].getHeight();
            font = new Font(Display.getCurrent(),
                    FontUtils.newHeight(defaultFontData, defaultHeight
                            * targetDpi / UnitConvertor.getScreenDpi().y));
        }
        return font;
    }

    private static int getHeight(String text, Font font) {
        RotatableWrapLabel label = new RotatableWrapLabel();
        label.setText(text);
        label.setFont(font);

        return (text == null || text.equals("")) ? 0 //$NON-NLS-1$
                : label.getPreferredSize().height;
    }

}
