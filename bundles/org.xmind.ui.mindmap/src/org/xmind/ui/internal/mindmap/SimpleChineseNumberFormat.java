package org.xmind.ui.internal.mindmap;

import org.xmind.ui.mindmap.INumberFormat;
import org.xmind.ui.util.NumberUtils;

public class SimpleChineseNumberFormat implements INumberFormat {

    @Override
    public String getText(int index) {
        return NumberUtils.toSimpleChinese(index);
    }

}
