package org.xmind.ui.internal.figures;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.swt.graphics.Font;
import org.xmind.gef.draw2d.RotatableWrapLabel;
import org.xmind.gef.draw2d.geometry.PrecisionDimension;
import org.xmind.gef.draw2d.geometry.PrecisionRectangle;

public class BoundaryTitleFigure extends RotatableWrapLabel {

    private static final float PADDING = 1.5f;

    private static final float H_MARGIN = 8.0f;

    private static final float V_MARGIN = 3.0f;

    private static final String NULLSTR = ""; //$NON-NLS-1$
    private static final String ONESPACE = " "; //$NON-NLS-1$

    private BoundaryFigure boundary;

    private PrecisionRectangle textArea;

    public BoundaryTitleFigure() {
    }

    public BoundaryTitleFigure(String text) {
        super(text);
    }

    public BoundaryTitleFigure(int renderStyle) {
        super(renderStyle);
    }

    public BoundaryTitleFigure(String text, int renderStyle) {
        super(text, renderStyle);
    }

    protected void flushCaches() {
        super.flushCaches();
        textArea = null;
    }

    private int getPreferenceWHint(int wHint) {
        if (getBoundary() != null) {
            return wHint > getBoundary().getBounds().width
                    ? getBoundary().getBounds().width : wHint;
        }
        return wHint;
    }

    protected PrecisionRectangle getTextArea(int wHint) {
        wHint = getPreferenceWHint(wHint);
        receiveWidthCaches(wHint);
        if (textArea == null) {
            PrecisionDimension size = calculateTextSize(wHint);
            textArea = new PrecisionRectangle();
            float h_margin = H_MARGIN;
            float v_margin = V_MARGIN;
            int height = getFont().getFontData()[0].getHeight();
            if (height > 30)
                h_margin = h_margin + 5;
            textArea.width = size.width + PADDING * 2 + h_margin;
            textArea.height = size.height + PADDING * 2 + v_margin;
            textArea.x = -(textArea.width / 2);
            textArea.y = -(textArea.height / 2);
        }
        return textArea;
    }

    @Override
    protected String calculateAppliedText(double wHint) {
        String theText = getText();
        if (wHint <= 5 || theText.length() == 0)
            return theText;
        Font f = getFont();
        String[] lines = forceSplitText(theText, f, wHint);
        return getForceSplitText(lines);
    }

    private String[] forceSplitText(String theText, Font f, double wHint) {
        wHint = wHint - 5;
        List<String> buffer = new ArrayList<String>();
        theText = theText.trim();
        if (getShowLooseTextSize(theText, f).width < wHint) {
            buffer.add(theText);
            return buffer.toArray(new String[buffer.size()]);
        }
        String cachedString = NULLSTR;
        String appendString = NULLSTR;
        String[] lines = theText.split(ONESPACE);
        int i = 0;
        do {
            if (lines[i].equals(NULLSTR)) {
                lines[i] = ONESPACE;
            }
            if (getShowLooseTextSize(lines[i], f).width >= wHint) {
                if (cachedString.trim() != NULLSTR) {
                    buffer.add(cachedString.trim());
                    cachedString = NULLSTR;
                }
                cachedString = truncate(lines[i], buffer, wHint, f);
                i++;
                continue;
            }
            appendString = cachedString + lines[i];
            if (getShowLooseTextSize(appendString, f).width >= wHint) {
                if (cachedString.trim() != NULLSTR) {
                    buffer.add(cachedString.trim());
                }
                cachedString = lines[i];
            } else {
                cachedString = cachedString + lines[i];
            }
            cachedString += ONESPACE;
            i++;
        } while (i < lines.length);
        if (cachedString.trim() != NULLSTR) {
            buffer.add(cachedString.trim());
        }
        return buffer.toArray(new String[buffer.size()]);
    }

    private String truncate(String s, List<String> buffer, double wHint,
            Font f) {
        String token = s;
        while (wHint > 0 && !token.equals(NULLSTR)) {
            boolean isLastSnip = true;
            String current = token;
            while (getShowLooseTextSize(current, f).width >= wHint) {
                isLastSnip = false;
                current = current.substring(0, current.length() - 1);
            }
            if (getShowLooseTextSize(current, f).width < wHint && !isLastSnip) {
                buffer.add(token.substring(0, current.length()).trim());
                token = token.substring(current.length());
            } else
                return token;
        }
        return token;
    }

    private String getForceSplitText(String[] lines) {
        StringBuffer sb = new StringBuffer();
        for (String line : lines)
            sb.append(line + '\n');
        if (sb.charAt(sb.length() - 1) == '\n')
            sb.deleteCharAt(sb.length() - 1);
        return sb.toString();
    }

    private Dimension getShowLooseTextSize(String s, Font f) {
        int textCase = getTextCase();
        s = getShowText(s, textCase);
        return getLooseTextSize(s, f);
    }

    private BoundaryFigure getBoundary() {
        return boundary;
    }

    public void setBoundary(IFigure boundary) {
        this.boundary = (BoundaryFigure) boundary;
    }
}
