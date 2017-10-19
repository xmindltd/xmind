package org.xmind.ui.viewers;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.SWT;

public abstract class HintedListLayout implements IListLayout {

    public static final String MARGIN_TOP = "margin.top"; //$NON-NLS-1$
    public static final String MARGIN_LEFT = "margin.left"; //$NON-NLS-1$
    public static final String MARGIN_RIGHT = "margin.right"; //$NON-NLS-1$
    public static final String MARGIN_BOTTOM = "margin.bottom"; //$NON-NLS-1$
    public static final String SPACING_HORIZONTAL = "spacing.horizontal"; //$NON-NLS-1$
    public static final String SPACING_VERTICAL = "spacing.vertical"; //$NON-NLS-1$
    public static final String ITEM_WIDTH = "item.width"; //$NON-NLS-1$
    public static final String ITEM_HEIGHT = "item.height"; //$NON-NLS-1$

    // SWT.LEAD, SWT.TRAIL, SWT.CENTER, SWT.FILL
    public static final String ALIGNMENT_ITEM_HORIZONTAL = "alignment.item.horizontal"; //$NON-NLS-1$
    public static final String ALIGNMENT_ITEM_VERTICAL = "alignment.item.vertical"; //$NON-NLS-1$
    public static final String ALIGNMENT_LIST_HORIZONTAL = "alignment.list.horizontal"; //$NON-NLS-1$
    public static final String ALIGNMENT_LIST_VERTICAL = "alignment.list.vertical"; //$NON-NLS-1$

    private Map<String, Integer> hints = new HashMap<String, Integer>();

    public int getHint(String key, int defaultValue) {
        Integer hint = hints.get(key);
        return hint == null ? defaultValue : hint.intValue();
    }

    public void setHint(String key, int value) {
        if (value == SWT.DEFAULT) {
            hints.remove(key);
        } else {
            hints.put(key, Integer.valueOf(value));
        }
    }

}
