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
package org.xmind.ui.internal.editor;

import org.eclipse.draw2d.FigureListener;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.TextLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Caret;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.IME;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.ScrollBar;
import org.xmind.gef.EditDomain;
import org.xmind.gef.GEF;
import org.xmind.gef.IGraphicalViewer;
import org.xmind.gef.Request;
import org.xmind.gef.part.IGraphicalPart;
import org.xmind.gef.part.IPart;
import org.xmind.gef.tool.PartTextSelection;
import org.xmind.gef.ui.editor.IGraphicalEditor;
import org.xmind.ui.mindmap.ITitleTextPart;
import org.xmind.ui.mindmap.MindMapUI;

public class IMESupport implements ISelectionChangedListener, Listener,
        ControlListener, SelectionListener, FigureListener, PaintListener {

    public static final String PROP_IGNORE_KEY_DOWN = "org.xmind.ui.ignoreKeyDown"; //$NON-NLS-1$

    private static final int DEFAULT_CARET_WIDTH = 2;

    private MindMapEditorPage page;

    private IGraphicalViewer viewer;

    private Canvas canvas;

    private ScrollBar hBar;

    private ScrollBar vBar;

    private IME ime;

    private Caret caret;

    private boolean imeNeedDispose;

    private boolean caretNeedDispose;

    private int caretWidth;

    private int caretHeight;

    private IPart focus;

    private IFigure focusFigure = null;

    private TextLayout composition = null;

    private int compositionLeft = 0;

    private int compositionTop = 0;

    private StringBuilder composedCache = null;

    public IMESupport(MindMapEditorPage page, IGraphicalViewer viewer) {
        this.page = page;
        this.viewer = viewer;

        this.canvas = viewer.getCanvas();
        this.canvas.addControlListener(this);
        this.canvas.addPaintListener(this);

        this.hBar = this.canvas.getHorizontalBar();
        if (this.hBar != null) {
            this.hBar.addSelectionListener(this);
        }
        this.vBar = this.canvas.getVerticalBar();
        if (this.vBar != null) {
            this.vBar.addSelectionListener(this);
        }

        this.ime = this.canvas.getIME();
        if (this.ime == null) {
            this.ime = new IME(this.canvas, SWT.NONE);
            this.imeNeedDispose = true;
        } else {
            this.imeNeedDispose = false;
        }
        this.ime.addListener(SWT.ImeComposition, this);

        this.caret = this.canvas.getCaret();
        if (this.caret == null) {
            this.caret = new Caret(this.canvas, SWT.NONE);
            this.caretNeedDispose = true;
        } else {
            this.caretNeedDispose = false;
        }
        this.caret.setVisible(false);
        this.caretWidth = DEFAULT_CARET_WIDTH;
        this.caretHeight = SWT.DEFAULT;

        viewer.addFocusedPartChangedListener(this);
    }

    public void dispose() {
        deactivateContext();
        if (composition != null) {
            composition.dispose();
            composition = null;
        }
        setFocusFigure(null);
        viewer.removeFocusedPartChangedListener(this);
        if (ime != null) {
            if (!ime.isDisposed()) {
                ime.removeListener(SWT.ImeComposition, this);
            }
            if (imeNeedDispose) {
                ime.dispose();
            }
            ime = null;
        }
        if (caret != null) {
            if (caretNeedDispose) {
                caret.dispose();
            }
            caret = null;
        }
        if (vBar != null && !vBar.isDisposed()) {
            vBar.removeSelectionListener(this);
        }
        if (hBar != null && !hBar.isDisposed()) {
            hBar.removeSelectionListener(this);
        }
        if (canvas != null && !canvas.isDisposed()) {
            canvas.removeControlListener(this);
        }
    }

    public void selectionChanged(SelectionChangedEvent event) {
        if (event.getSelection() instanceof IStructuredSelection) {
            focus = viewer
                    .findPart(((IStructuredSelection) event.getSelection())
                            .getFirstElement());
        } else if (event.getSelection() instanceof PartTextSelection) {
            focus = ((PartTextSelection) event.getSelection()).getPart();
        } else {
            focus = null;
        }
        IFigure figure = null;
        if (focus instanceof ITitleTextPart) {
            figure = ((ITitleTextPart) focus).getTextFigure();
        } else if (focus != null) {
            ITitleTextPart title = (ITitleTextPart) focus
                    .getAdapter(ITitleTextPart.class);
            if (title != null) {
                figure = title.getTextFigure();
            } else if (focus instanceof IGraphicalPart) {
                figure = ((IGraphicalPart) focus).getFigure();
            }
        }
        setFocusFigure(figure);
    }

    private void setFocusFigure(IFigure figure) {
        if (figure != focusFigure) {
            if (focusFigure != null) {
                focusFigure.removeFigureListener(this);
            }
            if (figure != null) {
                figure.addFigureListener(this);
            }
            focusFigure = figure;
        }
        updateCompositionLocation();
    }

    private void updateCompositionLocation() {
        if (focusFigure != null) {
            Point pos = focusFigure.getBounds().getTopLeft();
            Insets border = focusFigure.getInsets();
            pos.translate(border.left, border.top);
            focusFigure.translateToAbsolute(pos);
            setCompositionLocation(pos.x, pos.y);
        } else if (!canvas.isDisposed()) {
            Rectangle r = canvas.getBounds();
            setCompositionLocation(Math.max(0, r.width / 2 - 100), Math.max(0,
                    Math.min(r.height / 2 + 100, r.height - caretHeight)));
        }
    }

    private void setCompositionLocation(int x, int y) {
        this.compositionLeft = x;
        this.compositionTop = y;
        updateCaretLocation();
    }

    private void updateCaretLocation() {
        if (caret == null || caret.isDisposed())
            return;

        int x = compositionLeft;
        int y = compositionTop;
        caretWidth = DEFAULT_CARET_WIDTH;
        caretHeight = 10;
        if (ime.getCompositionOffset() >= 0 && composition != null
                && !composition.isDisposed()) {
            int cacheLength = composedCache == null ? 0
                    : composedCache.length();
            int caretOffset = cacheLength + ime.getCaretOffset();
            org.eclipse.swt.graphics.Point p = composition
                    .getLocation(caretOffset, false);
            x = compositionLeft + p.x;
            y = compositionTop + p.y;
            if (ime.getWideCaret()) {
                Rectangle size = composition.getBounds(cacheLength,
                        caretOffset);
                caretWidth = size.width;
                caretHeight = size.height;
            } else {
                caretWidth = DEFAULT_CARET_WIDTH;
                Rectangle size = composition.getBounds(caretOffset,
                        caretOffset);
                caretHeight = size.height;
            }
        }
        caret.setBounds(x, y, caretWidth, caretHeight);
    }

    public void handleEvent(Event event) {
        if (event.type == SWT.ImeComposition) {
//            forwardCompositionEvent(event);
            switch (event.detail) {
            case SWT.COMPOSITION_CHANGED:
                handleCompositionChanged(event);
                break;
            case SWT.COMPOSITION_SELECTION:
                handleCompositionSelection(event);
                break;
            case SWT.COMPOSITION_OFFSET:
                handleCompositionOffset(event);
                break;
            }
        }
    }

    public void controlMoved(ControlEvent e) {
        updateCompositionLocation();
    }

    public void controlResized(ControlEvent e) {
        updateCompositionLocation();
    }

    public void widgetSelected(SelectionEvent e) {
        updateCompositionLocation();
    }

    public void widgetDefaultSelected(SelectionEvent e) {
        updateCompositionLocation();
    }

    public void figureMoved(IFigure source) {
        updateCompositionLocation();
    }

    public void paintControl(PaintEvent e) {
        paintComposition(e.gc);
    }

    private void handleCompositionChanged(final Event event) {
        final String text = event.text;
        final int length = text.length();
        if (length == ime.getCommitCount()) {
//            try {
//                System.out.println("IME completed: "
//                        + Arrays.toString(text.getBytes("UTF-8")));
//            } catch (UnsupportedEncodingException e) {
//                e.printStackTrace();
//            }
            if (length > 0) {
                if (composedCache == null) {
                    composedCache = new StringBuilder();
                }
                composedCache.append(text);
            }
            int ignoreCount = viewer.getProperties()
                    .getInteger(PROP_IGNORE_KEY_DOWN, 0);
            viewer.getProperties().set(PROP_IGNORE_KEY_DOWN,
                    ignoreCount + text.length());
            Display.getCurrent().asyncExec(new Runnable() {

                public void run() {
                    if (canvas == null || canvas.isDisposed())
                        return;
                    if (ime.getCompositionOffset() < 0) {
                        if (composedCache != null
                                && composedCache.length() > 0) {
                            final EditDomain domain = viewer.getEditDomain();
                            Request request = new Request(GEF.REQ_EDIT)
                                    .setPrimaryTarget(focus).setDomain(domain)
                                    .setViewer(viewer)
                                    .setParameter(GEF.PARAM_TEXT,
                                            composedCache.toString());
////                                        .setParameter(GEF.PARAM_TEXT_SELECTION,
////                                                new TextSelection(length, 0))
////                                        .setParameter(GEF.PARAM_FOCUS, Boolean.FALSE);
                            domain.handleRequest(request);
                        }
                        composedCache = null;
                        removeCompositionFeedback();
                    } else {
                        showCompositionFeedback(ime.getText());
                    }
                }
            });
        } else {
//            try {
//                System.out.println("Edit composition: "
//                        + Arrays.toString(text.getBytes("UTF-8")));
//            } catch (UnsupportedEncodingException e) {
//                e.printStackTrace();
//            }
            showCompositionFeedback(text);
        }
    }

    private void handleCompositionSelection(Event event) {
//        System.out.println("IME require selection: " + ime.getCaretOffset());
        event.start = ime.getCompositionOffset() < 0 ? 0 : ime.getCaretOffset();
        event.end = ime.getCompositionOffset() < 0 ? 0 : ime.getCaretOffset();
        event.text = ""; //$NON-NLS-1$
    }

    private void handleCompositionOffset(Event event) {
//        System.out.println("IME require offset.");
        if (ime.getCompositionOffset() < 0 || composition == null
                || composition.isDisposed()) {
            event.index = 0;
            event.count = 0;
        } else {
            int x = event.x - compositionLeft;
            int y = event.y - compositionTop;
            int[] trailing = new int[1];
            int offset = composition.getOffset(x, y, trailing);
            event.index = offset;
            event.count = trailing[0];
        }
    }

    private void showCompositionFeedback(String text) {
        if (focusFigure == null) {
            removeCompositionFeedback();
            return;
        }
        if (composition == null || composition.isDisposed()) {
            composition = new TextLayout(Display.getCurrent());
        }
        composition.setFont(focusFigure.getFont());
        if (composedCache != null) {
            text = composedCache.toString() + text;
        }
        composition.setText(text);
        canvas.redraw();

        updateCaretLocation();
        caret.setVisible(true);
        activateContext();
    }

    private void removeCompositionFeedback() {
        deactivateContext();
        if (composition != null) {
            composition.dispose();
            composition = null;
        }
        canvas.redraw();
        caret.setVisible(false);
    }

    private void activateContext() {
        IGraphicalEditor editor = page.getParentEditor();
        if (editor instanceof MindMapEditor) {
            ((MindMapEditor) editor)
                    .changeContext(MindMapUI.CONTEXT_MINDMAP_TEXTEDIT);
        }
    }

    private void deactivateContext() {
        IGraphicalEditor editor = page.getParentEditor();
        if (editor instanceof MindMapEditor) {
            ((MindMapEditor) editor)
                    .changeContext(page.getEditDomain().getActiveTool());
        }
    }

    private void paintComposition(GC gc) {
        if (composition == null || composition.isDisposed())
            return;

        Color fg = gc.getForeground();
        Color bg = gc.getBackground();
        int lineWidth = gc.getLineWidth();
        int lineStyle = gc.getLineStyle();
        int lineJoin = gc.getLineJoin();
        int lineCap = gc.getLineCap();
        Rectangle clipping = gc.getClipping();

        gc.setLineWidth(1);
        gc.setLineStyle(SWT.LINE_SOLID);
        gc.setLineJoin(SWT.JOIN_BEVEL);
        gc.setLineCap(SWT.CAP_FLAT);

        Rectangle size = composition.getBounds();
        gc.setClipping(compositionLeft - 2, compositionTop - 2, size.width + 4,
                size.height + 4);

        gc.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
        gc.fillRectangle(compositionLeft - 2, compositionTop - 2,
                size.width + 4, size.height + 4);

        gc.setForeground(
                Display.getCurrent().getSystemColor(SWT.COLOR_DARK_GRAY));
        gc.drawRectangle(compositionLeft - 2, compositionTop - 2,
                size.width + 3, size.height + 3);
        gc.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_BLACK));
        composition.draw(gc, compositionLeft, compositionTop);

        gc.setClipping(clipping);
        gc.setLineCap(lineCap);
        gc.setLineJoin(lineJoin);
        gc.setLineStyle(lineStyle);
        gc.setLineWidth(lineWidth);
        gc.setBackground(bg);
        gc.setForeground(fg);
    }

//    private void forwardCompositionEvent(final Event event) {
//        if (focus == null || !focus.hasRole(GEF.ROLE_EDITABLE))
//            return;
//        final EditDomain domain = viewer.getEditDomain();
//        Request request = new Request(GEF.REQ_EDIT).setPrimaryTarget(focus)
//                .setDomain(domain).setViewer(viewer);
//////                    .setParameter(GEF.PARAM_TEXT, text)
//////                    .setParameter(GEF.PARAM_TEXT_SELECTION,
//////                            new TextSelection(length, 0))
//////                    .setParameter(GEF.PARAM_FOCUS, Boolean.FALSE);
//        domain.handleRequest(request);
//
//        Display.getCurrent().asyncExec(new Runnable() {
//            public void run() {
//                ITool tool = domain.getActiveTool();
//                if (tool instanceof FloatingTextEditTool) {
//                    FloatingTextEditor editor = ((FloatingTextEditTool) tool)
//                            .getEditor();
//                    if (editor != null && !editor.isClosed()) {
//                        IME ime2 = editor.getTextViewer().getTextWidget()
//                                .getIME();
//                        final Event e = new Event();
//                        e.button = event.button;
//                        e.character = event.character;
//                        e.count = event.count;
//                        e.data = event.data;
//                        e.detail = event.detail;
//                        e.display = event.display;
//                        e.doit = event.doit;
//                        e.end = event.end;
//                        e.gc = event.gc;
//                        e.height = event.height;
//                        e.index = event.index;
//                        e.item = event.item;
//                        e.keyCode = event.keyCode;
//                        e.keyLocation = event.keyLocation;
//                        e.magnification = event.magnification;
//                        e.rotation = event.rotation;
//                        e.segments = event.segments;
//                        e.segmentsChars = event.segmentsChars;
//                        e.start = event.start;
//                        e.stateMask = event.stateMask;
//                        e.text = event.text;
//                        e.time = event.time;
//                        e.touches = event.touches;
//                        e.type = event.type;
//                        e.widget = ime2;
//                        e.width = event.width;
//                        e.x = event.x;
//                        e.xDirection = event.xDirection;
//                        e.y = event.y;
//                        e.yDirection = event.yDirection;
//                        Display.getCurrent().post(e);
//
//                    }
//                }
//            }
//        });
//    }

//    public static void main(String[] args) {
//        final Display display = new Display();
//        final Shell shell = new Shell(display);
//        shell.setBounds(200, 100, 500, 400);
//        shell.setLayout(new FillLayout());
//
//        final Canvas canvas = new Canvas(shell, SWT.NONE);
//        final IME ime = new IME(canvas, SWT.NONE);
//        final Caret caret = new Caret(canvas, SWT.NONE);
//
//        caret.setBounds(10, 10, 2, 20);
//
//        ime.addListener(SWT.ImeComposition, new Listener() {
//            public void handleEvent(Event event) {
//                if (event.detail == SWT.COMPOSITION_CHANGED) {
//                    System.out.println("Composition changed: " + event.text);
//                } else if (event.detail == SWT.COMPOSITION_OFFSET) {
//                    System.out.println("Composition require offset.");
//                } else if (event.detail == SWT.COMPOSITION_SELECTION) {
//                    System.out.println("Composition require selection.");
//                }
//            }
//        });
//
//        Listener canvasListener = new Listener() {
//            public void handleEvent(Event event) {
//                if (event.type == SWT.Verify) {
//                    System.out.println("Key verify: " + event.keyCode + ", "
//                            + event.stateMask);
//                } else if (event.type == SWT.KeyDown) {
//                    System.out.println("Key down: " + event.keyCode + ", "
//                            + event.stateMask);
//                } else if (event.type == SWT.Traverse) {
//                    System.out.println("Key traverse: " + event.detail);
//                }
//            }
//        };
//        canvas.addListener(SWT.Verify, canvasListener);
//        canvas.addListener(SWT.KeyDown, canvasListener);
//        canvas.addListener(SWT.Traverse, canvasListener);
//
//        shell.open();
//
//        while (!shell.isDisposed()) {
//            if (!display.readAndDispatch()) {
//                display.sleep();
//            }
//        }
//        shell.dispose();
//        display.dispose();
//    }

}
