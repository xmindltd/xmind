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
package org.xmind.ui.internal.browser;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.ScrollBar;

public abstract class FallbackScrolledComposite extends ScrolledComposite {
    private static final int H_SCROLL_INCREMENT = 5;

    private static final int V_SCROLL_INCREMENT = 64;

    /**
     * Creates the new instance.
     * 
     * @param parent
     *            the parent composite
     * @param style
     *            the style to use
     */
    public FallbackScrolledComposite(Composite parent, int style) {
        super(parent, style);
        addListener(SWT.Resize, new Listener() {
            public void handleEvent(Event e) {
                reflow(true);
            }
        });
        setExpandVertical(true);
        setExpandHorizontal(true);
        initializeScrollBars();
    }

    /**
     * Sets the foreground of the control and its content.
     * 
     * @param fg
     *            the new foreground color
     */
    @Override
    public void setForeground(Color fg) {
        super.setForeground(fg);
        if (getContent() != null)
            getContent().setForeground(fg);
    }

    /**
     * Sets the background of the control and its content.
     * 
     * @param bg
     *            the new background color
     */
    @Override
    public void setBackground(Color bg) {
        super.setBackground(bg);
        if (getContent() != null)
            getContent().setBackground(bg);
    }

    /**
     * Sets the font of the form. This font will be used to render the title
     * text. It will not affect the body.
     */
    @Override
    public void setFont(Font font) {
        super.setFont(font);
        if (getContent() != null)
            getContent().setFont(font);
    }

    /**
     * Overrides 'super' to pass the proper colors and font
     */
    @Override
    public void setContent(Control content) {
        super.setContent(content);
        if (content != null) {
            content.setForeground(getForeground());
            content.setBackground(getBackground());
            content.setFont(getFont());
        }
    }

    /**
     * If content is set, transfers focus to the content.
     */
    @Override
    public boolean setFocus() {
        if (getContent() != null)
            return getContent().setFocus();
        return super.setFocus();
    }

    /**
     * Recomputes the body layout and the scroll bars. The method should be used
     * when changes somewhere in the form body invalidate the current layout
     * and/or scroll bars.
     * 
     * @param flushCache
     *            if <code>true</code>, drop the cached data
     */
    public void reflow(boolean flushCache) {
        Composite c = (Composite) getContent();
        Rectangle clientArea = getClientArea();
        if (c == null)
            return;

        Point newSize = c
                .computeSize(clientArea.width, SWT.DEFAULT, flushCache);

        setMinSize(newSize);
        updatePageIncrement();
        layout(flushCache);
    }

    private void updatePageIncrement() {
        ScrollBar vbar = getVerticalBar();
        if (vbar != null) {
            Rectangle clientArea = getClientArea();
            int increment = clientArea.height - 5;
            vbar.setPageIncrement(increment);
        }
    }

    private void initializeScrollBars() {
        ScrollBar hbar = getHorizontalBar();
        if (hbar != null) {
            hbar.setIncrement(H_SCROLL_INCREMENT);
        }
        ScrollBar vbar = getVerticalBar();
        if (vbar != null) {
            vbar.setIncrement(V_SCROLL_INCREMENT);
        }
        updatePageIncrement();
    }
}