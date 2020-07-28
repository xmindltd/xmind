/*
 * Copyright (c) 2006-2012 XMind Ltd. and others.
 * 
 * This file is a part of XMind 3. XMind releases 3 and above are dual-licensed
 * under the Eclipse Public License (EPL), which is available at
 * http://www.eclipse.org/legal/epl-v10.html and the GNU Lesser General Public
 * License (LGPL), which is available at http://www.gnu.org/licenses/lgpl.html
 * See https://www.xmind.net/license.html for details.
 * 
 * Contributors: XMind Ltd. - initial API and implementation
 */
package org.xmind.ui.internal.spelling;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.text.ITextListener;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.TextEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.xmind.ui.texteditor.IControlContentAdapter2;
import org.xmind.ui.texteditor.IMenuContributor;
import org.xmind.ui.texteditor.ISpellingActivation;
import org.xmind.ui.texteditor.ISpellingSupport;
import org.xmind.ui.texteditor.StyledTextContentAdapter;

import com.swabunga.spell.event.SpellCheckEvent;
import com.swabunga.spell.event.SpellCheckListener;
import com.swabunga.spell.event.SpellChecker;
import com.swabunga.spell.event.StringWordTokenizer;

public class SpellingHelper
        implements ISpellingActivation, Listener, ITextListener {

    private static final long CHECK_DELAY = 200;

    private class SuggestionAction extends Action {

        private SpellCheckEvent range;

        private String suggestion;

        public SuggestionAction(SpellCheckEvent range, String suggestion) {
            super(range.getInvalidWord() + " -> " + suggestion); //$NON-NLS-1$
            this.range = range;
            this.suggestion = suggestion;
        }

        public void run() {
            if (!isActive())
                return;
            String old = contentAdapter.getControlContents(control);
            int oldLength = old.length();
            int start = range.getWordContextPosition();
            String invalidWord = range.getInvalidWord();
            int invalidLength = invalidWord.length();
            if (start < oldLength && start + invalidLength <= oldLength) {
                contentAdapter.replaceControlContents(control, start,
                        invalidLength, suggestion);
                check(Display.getCurrent());
            }
        }
    }

    private class NewWordAction extends Action {

        private SpellCheckEvent range;

        public NewWordAction(SpellCheckEvent range) {
            super(Messages.addToDictionary);
            this.range = range;
        }

        public void run() {
            addToDict(Display.getCurrent(), range);
        }

    }

    private static class NoSuggestionAction extends Action {
        public NoSuggestionAction() {
            super(Messages.noSpellSuggestion);
            setEnabled(false);
        }
    }

    private class SpellingMenuContributor implements IMenuContributor {

        private SpellCheckEvent getCurrentRange() {
            int pos = contentAdapter.getCursorPosition(control);
            for (Entry<Integer, SpellCheckEvent> en : ranges.entrySet()) {
                SpellCheckEvent range = en.getValue();
                int start = en.getKey().intValue();
                int length = range.getInvalidWord().length();
                if (start <= pos && pos <= start + length) {
                    return range;
                }
            }
            return null;
        }

        public void fillMenu(IMenuManager menu) {
            SpellCheckEvent range = getCurrentRange();
            if (range != null) {
                List list = range.getSuggestions();
                if (list.isEmpty()) {
                    menu.add(new NoSuggestionAction());
                } else {
                    for (Object o : list) {
                        String suggestion = o.toString();
                        menu.add(new SuggestionAction(range, suggestion));
                    }
                }
                menu.add(new Separator());
                menu.add(new NewWordAction(range));
            }
        }

    }

    private class CheckJob extends Job implements SpellCheckListener {

        private Display display;

        private long start = -1;

        private boolean rescheduling = false;

        public CheckJob() {
            super(Messages.spellCheckProgress_Text);
            setSystem(true);
        }

        public synchronized void check(Display display) {
            if (display == null || display.isDisposed() || !isActive())
                return;

            if (start == -1) {
                // not scheduled
                this.display = display;
                schedule();
            } else if (start == -2) {
                // working
                rescheduling = true;
            } else {
                // scheduling
                start = System.currentTimeMillis();
            }
        }

        public void dispose() {
            rescheduling = false;
            cancel();
        }

        protected IStatus run(IProgressMonitor monitor) {
            try {
                return doRun(monitor);
            } finally {
                start = -1;
                if (rescheduling) {
                    rescheduling = false;
                    schedule();
                }
            }
        }

        protected IStatus doRun(final IProgressMonitor monitor) {
            start = System.currentTimeMillis();
            if (monitor.isCanceled() || display.isDisposed() || !isActive())
                return Status.CANCEL_STATUS;

            final String[] context = new String[1];
            display.syncExec(new Runnable() {
                public void run() {
                    if (monitor.isCanceled() || display.isDisposed()
                            || !isActive())
                        return;
                    context[0] = contentAdapter.getControlContents(control);
                }
            });
            if (monitor.isCanceled() || display.isDisposed() || !isActive())
                return Status.CANCEL_STATUS;

            if (context[0] == null || "".equals(context[0])) {//$NON-NLS-1$
                if (!ranges.isEmpty()) {
                    ranges.clear();
                    redraw(display);
                }
                return Status.OK_STATUS;
            }

            while (System.currentTimeMillis() < start + CHECK_DELAY) {
                if (monitor.isCanceled() || display.isDisposed() || !isActive())
                    return Status.CANCEL_STATUS;
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    return Status.CANCEL_STATUS;
                }
            }
            start = -2;
            if (monitor.isCanceled() || display.isDisposed() || !isActive())
                return Status.CANCEL_STATUS;

            display.syncExec(new Runnable() {
                public void run() {
                    if (monitor.isCanceled() || display.isDisposed()
                            || !isActive())
                        return;
                    context[0] = contentAdapter.getControlContents(control);
                }
            });

            if (monitor.isCanceled() || display.isDisposed() || !isActive())
                return Status.CANCEL_STATUS;

            if (context[0] != null) {
                ranges.clear();

                if (!"".equals(context[0])) { //$NON-NLS-1$
                    SpellChecker theSpellChecker = spellChecker;
                    theSpellChecker.addSpellCheckListener(this);
                    theSpellChecker
                            .checkSpelling(new StringWordTokenizer(context[0]));
                    theSpellChecker.removeSpellCheckListener(this);
                }
                if (monitor.isCanceled() || display.isDisposed() || !isActive())
                    return Status.CANCEL_STATUS;

                redraw(display);
            }

            return Status.OK_STATUS;
        }

        public void spellingError(SpellCheckEvent event) {
            int start = event.getWordContextPosition();
            ranges.put(Integer.valueOf(start), event);
        }

    }

    private static class Line {

        int x1, x2, y;

    }

    private ISpellingSupport support;

    private ITextViewer viewer;

    private Control control;

    private IControlContentAdapter2 contentAdapter;

    private SpellChecker spellChecker;

    private Map<Integer, SpellCheckEvent> ranges = new HashMap<Integer, SpellCheckEvent>();

    private SpellingMenuContributor contributor;

    private CheckJob job = null;

    private boolean disposed = false;

    private static Map<Integer, Line> lineCache = new HashMap<Integer, Line>();

    private ISpellCheckerVisitor visitor = null;

    public SpellingHelper(ISpellingSupport support, ITextViewer viewer) {
        this.support = support;
        this.viewer = viewer;
        this.control = viewer.getTextWidget();
        this.contentAdapter = new StyledTextContentAdapter();
        init(viewer);
    }

    public SpellingHelper(ISpellingSupport support, Control control,
            IControlContentAdapter2 adapter) {
        this.support = support;
        this.viewer = null;
        this.control = control;
        this.contentAdapter = adapter;
        init(control);
    }

    private void init(ITextViewer viewer) {
        viewer.addTextListener(this);
        init();
    }

    private void init(Control control) {
        control.addListener(SWT.Modify, this);
        init();
    }

    /**
     * 
     */
    private void init() {
        control.addListener(SWT.Paint, this);
        control.addListener(SWT.Dispose, this);
        final Display display = Display.getCurrent();

        visitor = new ISpellCheckerVisitor() {
            public void handleWith(SpellChecker spellChecker) {
                if (control == null || control.isDisposed() || disposed)
                    return;
                SpellingHelper.this.spellChecker = spellChecker;
                check(display);
            }
        };
        SpellCheckerAgent.visitSpellChecker(visitor);
        SpellCheckerAgent.addListener(visitor);
    }

    /**
     * @param range
     */
    private void addToDict(final Display display, final SpellCheckEvent range) {
        SpellCheckerAgent.visitSpellChecker(new ISpellCheckerVisitor() {
            //This listener is just to get value from a long time delay.
            public void handleWith(SpellChecker spellChecker) {
                if (!isActive())
                    return;
                spellChecker.addToDictionary(range.getInvalidWord());
                check(display);
            }
        });
    }

    /**
     * @param gc
     */
    private void paintSpellError(GC gc) {
        if (ranges.isEmpty())
            return;

        int lineStyle = gc.getLineStyle();
        int lineWidth = gc.getLineWidth();
        Color lineColor = gc.getForeground();
        gc.setLineWidth(2);
        gc.setLineStyle(SWT.LINE_DOT);
        gc.setForeground(gc.getDevice().getSystemColor(SWT.COLOR_RED));
        int charCount = contentAdapter.getControlContents(control).length();
        Rectangle clipping = gc.getClipping();
        lineCache.clear();
        for (Object obj : ranges.values().toArray()) {
            SpellCheckEvent range = (SpellCheckEvent) obj;
            int start = range.getWordContextPosition();
            if (start < 0 || start >= charCount)
                continue;

            int length = Math.min(range.getInvalidWord().length(),
                    charCount - start);
            if (length <= 0)
                continue;

            drawLines(gc, start, start + length - 1, clipping);
        }
        gc.setLineWidth(lineWidth);
        gc.setLineStyle(lineStyle);
        gc.setForeground(lineColor);
    }

    private void drawLines(GC gc, int start, int end, Rectangle clipping) {
        Line startLine = getLine(gc, start);
        Line endLine = getLine(gc, end);
        if (startLine.y == endLine.y) {
            gc.drawLine(startLine.x1, startLine.y, endLine.x2, endLine.y);
        } else if (start < end) {
            int mid = (start + end) / 2;
            drawLines(gc, start, mid, clipping);
            if (mid < end) {
                drawLines(gc, mid + 1, end, clipping);
            }
        }
    }

    private Line getLine(GC gc, int offset) {
        Line p = lineCache.get(Integer.valueOf(offset));
        if (p == null) {
            p = new Line();
            Point loc = contentAdapter.getLocationAtOffset(control, offset + 1);
            int h = contentAdapter.getLineHeightAtOffset(control, offset + 1);
            p.y = loc.y + h - 1;
            p.x2 = loc.x;
            p.x1 = p.x2 - gc.stringExtent(
                    contentAdapter.getControlContents(control, offset, 1)).x;
            lineCache.put(Integer.valueOf(offset), p);
        }
        return p;
    }

    /**
     * 
     */
    private void check(Display display) {
        if (!isActive())
            return;
        if (job == null)
            job = new CheckJob();
        job.check(display);
    }

    public void handleEvent(Event event) {
        if (control.isDisposed())
            return;
        int type = event.type;
        switch (type) {
        case SWT.Modify:
            handleTextModified(event);
            break;
        case SWT.Paint:
            paintSpellError(event.gc);
            break;
        case SWT.Dispose:
            handleWidgetDispose();
            break;
        }
    }

    public ISpellingSupport getSpellingSupport() {
        return support;
    }

    public boolean isActive() {
        return !disposed && spellChecker != null && control != null
                && !control.isDisposed();
    }

    @SuppressWarnings("unchecked")
    public Object getAdapter(Class adapter) {
        if (adapter == IMenuContributor.class) {
            if (contributor == null)
                contributor = new SpellingMenuContributor();
            return contributor;
        }
        return null;
    }

    private void handleTextModified(Event event) {
        check(event.display);
    }

    private void handleWidgetDispose() {
        deactivate();
    }

    private void deactivate() {
        SpellCheckerAgent.removeListener(visitor);
        if (viewer != null) {
            viewer.removeTextListener(this);
            viewer = null;
        }
        if (control != null && !control.isDisposed()) {
            control.removeListener(SWT.Modify, this);
            control.removeListener(SWT.Paint, this);
            control.removeListener(SWT.Dispose, this);
            redraw(control.getDisplay());
            control = null;
        }
        if (job != null) {
            job.dispose();
            job = null;
        }
        ranges.clear();
    }

    void dispose() {
        deactivate();
        disposed = true;
    }

    private void redraw(Display display) {
        display.asyncExec(new Runnable() {
            public void run() {
                if (control != null && !control.isDisposed()) {
                    control.redraw();
                }
            }
        });
    }

    public void textChanged(TextEvent event) {
        check(Display.getCurrent());
    }

}