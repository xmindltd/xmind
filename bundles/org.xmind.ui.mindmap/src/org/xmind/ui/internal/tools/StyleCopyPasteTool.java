package org.xmind.ui.internal.tools;

import org.xmind.core.style.IStyle;

public class StyleCopyPasteTool {

    private volatile static StyleCopyPasteTool instance;

    private IStyle sourceStyle;

    private StyleCopyPasteTool() {
    }

    public static StyleCopyPasteTool getInstance() {
        if (instance == null) {
            synchronized (StyleCopyPasteTool.class) {
                if (instance == null)
                    instance = new StyleCopyPasteTool();
            }
        }
        return instance;
    }

    public IStyle getSourceStyle() {
        return sourceStyle;
    }

    public void setSourceStyle(IStyle sourceStyle) {
        this.sourceStyle = sourceStyle;
    }
}
