package org.xmind.ui.viewers;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.Transform;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

public class AdaptiveLabel extends Composite {

    private String text = ""; //$NON-NLS-1$

    public AdaptiveLabel(Composite parent, int style) {
        super(parent, style);
        addListener(SWT.Paint, new Listener() {
            public void handleEvent(Event event) {
                onPaint(event);
            }
        });
    }

    public String getText() {
        checkWidget();
        return text;
    }

    public void setText(String text) {
        checkWidget();
        if (text == null)
            text = ""; //$NON-NLS-1$
        if (text.equals(this.text))
            return;
        this.text = text;
        redraw();
    }

    private void onPaint(Event event) {
        paintLabel(event.gc);
    }

    private void paintLabel(GC gc) {
        Rectangle r = getClientArea();

        gc.setForeground(getForeground());
        gc.setFont(getFont());
        Point textSize = gc.textExtent(text);

        Transform t = new Transform(gc.getDevice());
        try {
            float scaleX = Math.min(1, ((float) r.width) / textSize.x);
            float scaleY = Math.min(1, ((float) r.height) / textSize.y);
            float scale = Math.min(scaleX, scaleY);
            t.translate(r.x + ((float) r.width) / 2,
                    r.y + ((float) r.height) / 2);
            t.scale(scale, scale);
            gc.setTransform(t);
            gc.setTextAntialias(SWT.ON);
            gc.drawText(text, -textSize.x / 2, -textSize.y / 2);
        } finally {
            t.dispose();
        }
    }

    @Override
    public Point computeSize(int wHint, int hHint, boolean changed) {
        Point size;
        if (wHint != SWT.DEFAULT && hHint != SWT.DEFAULT) {
            size = new Point(wHint, hHint);
        } else if ("".equals(text)) { //$NON-NLS-1$
            size = new Point(0, 0);
        } else {
            GC gc = new GC(this);
            try {
                gc.setFont(getFont());
                size = gc.textExtent(text);
            } finally {
                gc.dispose();
            }
        }
        Rectangle trimmed = computeTrim(0, 0, size.x, size.y);
        return new Point(trimmed.width, trimmed.height);
    }

}
