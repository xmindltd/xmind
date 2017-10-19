/* ******************************************************************************
 * Copyright (c) 2006-2012 XMind Ltd. and others.
 * 
 * This file is a part of XMind 3. XMind releases 3 and
 * above are dual-licensed under the Eclipse Public License (EPL),
 * which is available at http://www.eclipse.org/legal/epl-v10.html
 * and the GNU Lesser General Public License (LGPL), 
 * which is available at http://www.gnu.org/licenses/lgpl.html
 * See http://www.xmind.net/license.html for details.
 * 
 * Contributors:
 *     XMind Ltd. - initial API and implementation
 *******************************************************************************/
package org.xmind.ui.internal.mindmap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.draw2d.AbstractBackground;
import org.eclipse.draw2d.FreeformFigure;
import org.eclipse.draw2d.FreeformLayer;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.util.Util;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;
import org.xmind.gef.GEF;
import org.xmind.gef.IGraphicalViewer;
import org.xmind.gef.ISourceProvider;
import org.xmind.gef.command.Command;
import org.xmind.gef.command.CommandStackEvent;
import org.xmind.gef.command.ICommandStack;
import org.xmind.gef.command.ICommandStackListener;
import org.xmind.gef.draw2d.ITransparentableFigure;
import org.xmind.gef.draw2d.PathFigure;
import org.xmind.gef.draw2d.RotatableWrapLabel;
import org.xmind.gef.draw2d.graphics.Path;
import org.xmind.gef.part.IGraphicalPart;
import org.xmind.gef.part.IPart;
import org.xmind.gef.service.GraphicalViewerService;
import org.xmind.ui.internal.MindMapMessages;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.resources.FontUtils;
import org.xmind.ui.util.Cancelable;
import org.xmind.ui.util.ICancelable;

public class UndoRedoTipsService extends GraphicalViewerService implements
        ICommandStackListener {

//    private static class FrameFigure extends PathFigure {

//        private int alpha = 0xff;
//
//        public int getAlpha() {
//            return alpha;
//        }
//
//        public void setAlpha(int alpha) {
//            if (alpha == this.alpha)
//                return;
//            this.alpha = alpha;
//            repaint();
//        }
//
//        public void paintFigure(Graphics graphics) {
//            graphics.setAlpha(getAlpha());
//            super.paintFigure(graphics);
//        }

//    }

    /**
     * 
     * @author MANGOSOFT this class is used to paint the UndoRedo tips'
     *         background
     */
    private static class TitleBackground extends AbstractBackground {

        private int r;

        private int d;

        private Insets ins;

        private Color color;

        public TitleBackground(int r, Color color) {
            this.r = r;
            this.color = color;
            this.d = r * 2;
            this.ins = new Insets(0, r, 0, r);
        }

        @Override
        public Insets getInsets(IFigure figure) {
            return ins;
        }

        @Override
        public void paintBackground(IFigure figure, Graphics graphics,
                Insets insets) {
            Rectangle rect = getPaintRectangle(figure, insets);
            Rectangle inner = rect.getShrinked(getInsets(figure));
            Path p = new Path(Display.getCurrent());
            p.addArc(inner.x - r, inner.y, d, d, 90, 180);
            p.addRectangle(inner);
            p.addArc(inner.right() - r, inner.y, d, d, 270, 180);
            graphics.setFillRule(SWT.FILL_WINDING);
            graphics.setBackgroundColor(color);
            graphics.fillPath(p);
            p.dispose();
        }
    }

    private class Tip {

        private IGraphicalPart source;

        private String label;

        private PathFigure frame;

        private ITransparentableFigure title;

        public Tip(IGraphicalPart source, String label) {
            this.source = source;
            this.label = label;
        }

        public void setAlpha(int alpha) {
            if (frame == null)
                frame = createFrameFigure();
            if (title == null)
                title = createTitleFigure();

            if (frame != null)
                frame.setAlpha(alpha);
            if (title != null) {
                title.setMainAlpha(alpha);
                title.setSubAlpha(alpha);
            }
        }

        private PathFigure createFrameFigure() {
            if (getFrameLayer() == null)
                return null;

            IFigure srcFigure = source.getFigure();
            if (srcFigure instanceof FreeformFigure)
                return null;

            Rectangle frameBounds = srcFigure.getBounds().getExpanded(4, 4);
            PathFigure figure = new PathFigure();
            getFrameLayer().add(figure);

            figure.setFill(false);
            figure.setOutline(true);
            figure.setBackgroundColor(getFrameColor());
            figure.setForegroundColor(getFrameColor());
            figure.setLineStyle(SWT.LINE_SOLID);
            figure.setLineWidth(4);

            Path framePath = new Path(Display.getCurrent());
            framePath.addRoundedRectangle(frameBounds, 4);
            figure.setPath(framePath);
//            figure.setBounds(figure.getPreferredBounds());
            return figure;
        }

        private ITransparentableFigure createTitleFigure() {
            if (getTitleLayer() == null)
                return null;

            RotatableWrapLabel figure = new RotatableWrapLabel(label);
            getTitleLayer().add(figure);

            figure.setFont(FontUtils.getBoldRelative(
                    JFaceResources.DEFAULT_FONT, Util.isMac() ? 2 : 1));
            figure.setForegroundColor(Display.getCurrent().getSystemColor(
                    SWT.COLOR_WHITE));
            figure.setBackgroundColor(getFrameColor());

            Dimension size = figure.getPreferredSize();
            TitleBackground titleBg = new TitleBackground(size.height / 2,
                    getFrameColor());
            figure.setBorder(titleBg);

            IFigure srcFigure = source.getFigure();
            Point titleLoc;
            if (srcFigure instanceof FreeformFigure) {
                Rectangle extent = ((FreeformFigure) srcFigure)
                        .getFreeformExtent();
                titleLoc = new Point(extent.x + extent.width / 2, extent.y - 20);
            } else {
                Rectangle frameBounds = srcFigure.getBounds().getExpanded(4, 4);
                titleLoc = new Point(frameBounds.x + frameBounds.width / 2,
                        frameBounds.y - 2);
            }

            size = figure.getPreferredSize();
            int x = titleLoc.x - size.width / 2;
            int y = titleLoc.y - size.height;
            Rectangle titleBounds = new Rectangle(x, y, size.width, size.height);
            figure.setBounds(titleBounds);
            return figure;
        }

        private Color getFrameColor() {
            return Display.getCurrent().getSystemColor(SWT.COLOR_GRAY);
        }

        public void dispose() {
            if (frame != null) {
                if (frame.getParent() != null) {
                    frame.getParent().remove(frame);
                }
                org.eclipse.swt.graphics.Path path = frame.getPath();
                frame.setPath(null);
                if (path != null)
                    path.dispose();
                frame = null;
            }
            if (title != null) {
                if (title.getParent() != null) {
                    title.getParent().remove(title);
                }
                title = null;
            }
        }

    }

    private class TipsUpdater extends Cancelable {

        private Command command;

        private List<Tip> tips;

        private double alpha = -1;

        private boolean showingOrHiding = true;

        private long shownTime = -1;

        public TipsUpdater(Command command, List<IGraphicalPart> parts,
                String label) {
            this.command = command;
            tips = new ArrayList<Tip>(parts.size());
            for (IGraphicalPart part : parts) {
                tips.add(new Tip(part, label));
            }
        }

        public void cancel() {
            super.cancel();
            disposeTips();
            removeSubTask(command);
        }

        private void disposeTips() {
            if (tips != null) {
                for (Tip tip : tips) {
                    tip.dispose();
                }
                tips = null;
            }
        }

        private double getRealAlphaStep(double step) {
            return step * DEFAULT_DURATION / getDuration();
        }

        protected void doJob() {
            if (MindMapUI.isAnimationEnabled()) {
                if (showingOrHiding) {
                    if (alpha < 0) {
                        alpha = 0;
                    } else {
                        alpha += getRealAlphaStep(ALPHA_STEP);
                        if (alpha > 0xff) {
                            alpha = 0xff;
                            showingOrHiding = false;
                        }
                    }
                } else if (alpha > 0) {
                    if (alpha > SPEEDING_UP_ALPHA) {
                        alpha -= getRealAlphaStep(ALPHA_STEP2);
                    } else {
                        alpha -= getRealAlphaStep(ALPHA_STEP3);
                    }
                }
            } else {
                if (showingOrHiding) {
                    alpha = 0xff;
                    showingOrHiding = false;
                    if (shownTime < 0) {
                        shownTime = System.currentTimeMillis();
                    }
                } else {
                    if (shownTime > 0) {
                        if (System.currentTimeMillis() > shownTime
                                + getDuration()) {
                            cancel();
                        } else {
                            return;
                        }
                    } else {
                        cancel();
                    }
                }
            }

            if (alpha <= 0) {
                if (!showingOrHiding)
                    cancel();
            } else if (tips != null) {
                for (Tip tip : tips) {
                    tip.setAlpha((int) alpha);
                }
            }
        }

    }

    private class MainTask implements Runnable {

        private Display display;

        public MainTask(Display display) {
            this.display = display;
        }

        public void run() {
            if (subTasks == null || subTasks.isEmpty()) {
                cancel();
                return;
            }

            if (Thread.currentThread() == display.getThread()) {
                for (Object o : subTasks.values().toArray()) {
                    ((ICancelable) o).run();
                }
            } else {
                display.syncExec(new Runnable() {
                    public void run() {
                        for (Object o : subTasks.values().toArray()) {
                            ((ICancelable) o).run();
                        }
                    }
                });
            }

            if (subTasks == null || subTasks.isEmpty()) {
                cancel();
                return;
            }

            display.timerExec(UPDATE_INTERVALS, this);
        }

        private void cancel() {
            mainTaskEnded();
        }

    }

    /**
     * The total duration of showing the tips.
     */
    public static final int DEFAULT_DURATION = 800;

    /**
     * Intervals between two update task: 30 (milliseconds)
     */
    private static final int UPDATE_INTERVALS = 20;

    private static final double ALPHA_STEP = 30;

    private static final double ALPHA_STEP2 = 2;

    private static final double ALPHA_STEP3 = 8;

    private static final int SPEEDING_UP_ALPHA = 0xc0;

    private ICommandStack commandStack;

    private IFigure layer;

    private IFigure frameLayer;

    private IFigure titleLayer;

    private Runnable mainTask;

    private Map<Command, ICancelable> subTasks;

    private int duration = DEFAULT_DURATION;

    public UndoRedoTipsService(IGraphicalViewer viewer) {
        super(viewer);
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = Math.max(1, duration);
    }

    public IFigure getLayer() {
        return layer;
    }

    public void setLayer(IFigure layer) {
        boolean hadLayers = hasLayers();
        this.layer = layer;
        boolean hasLayers = hasLayers();
        if (isActive()) {
            if (hadLayers && !hasLayers) {
                stopListenToCommandChange();
            } else if (!hadLayers && hasLayers) {
                startListenToCommandChange();
            }
        }
    }

    private IFigure getFrameLayer() {
        ensureLayers();
        return frameLayer;
    }

    private IFigure getTitleLayer() {
        ensureLayers();
        return titleLayer;
    }

    private void ensureLayers() {
        if (layer == null)
            return;
        if (frameLayer != null && titleLayer != null)
            return;

        if (frameLayer == null) {
            frameLayer = new FreeformLayer();
            if (titleLayer != null) {
                int index = layer.getChildren().indexOf(titleLayer);
                layer.add(frameLayer, index);
            } else {
                layer.add(frameLayer);
            }
        }

        if (titleLayer == null) {
            titleLayer = new FreeformLayer();
            layer.add(titleLayer);
        }
    }

    private boolean hasLayers() {
        return layer != null;
    }

    protected void activate() {
        commandStack = getViewer().getEditDomain().getCommandStack();
        if (hasLayers()) {
            startListenToCommandChange();
        }
    }

    protected void deactivate() {
        if (subTasks != null) {
            for (Object o : subTasks.keySet().toArray()) {
                removeSubTask((Command) o);
            }
            subTasks = null;
        }
        stopListenToCommandChange();
        commandStack = null;
        mainTask = null;
    }

    private void startListenToCommandChange() {
        if (commandStack != null)
            commandStack.addCSListener(this);
    }

    private void stopListenToCommandChange() {
        if (commandStack != null)
            commandStack.removeCSListener(this);
    }

    public void handleCommandStackEvent(CommandStackEvent event) {
        Command command = event.getCommand();
        if (command == null)
            return;

        int commandType = command.getType();
        if (event.getStatus() == GEF.CS_PRE_UNDO
                && commandType != GEF.CMD_DELETE) {
            showTipsFor(command, true, commandType != GEF.CMD_CREATE);
        } else if (event.getStatus() == GEF.CS_POST_UNDO
                && commandType == GEF.CMD_DELETE) {
            showTipsFor(command, true, true);
        } else if (event.getStatus() == GEF.CS_PRE_REDO
                && commandType != GEF.CMD_CREATE) {
            showTipsFor(command, false, commandType != GEF.CMD_DELETE);
        } else if (event.getStatus() == GEF.CS_POST_REDO
                && commandType == GEF.CMD_CREATE) {
            showTipsFor(command, false, true);
        }
    }

    private void showTipsFor(Command command, boolean undoOrRedo, boolean async) {
        if (!(command instanceof ISourceProvider))
            return;

        Object[] elements = ((ISourceProvider) command).getSources().toArray();
        List<IGraphicalPart> parts = new ArrayList<IGraphicalPart>(
                elements.length);
        for (Object element : elements) {
            IPart part = getViewer().findPart(element);
            if (part instanceof IGraphicalPart) {
                parts.add((IGraphicalPart) part);
            }
        }
        if (parts.isEmpty())
            return;

        showTipsFor(command, parts, getLabel(command, undoOrRedo), async);
    }

    private void showTipsFor(final Command command,
            final List<IGraphicalPart> parts, final String label, boolean async) {
        removeSubTask(command);

        final TipsUpdater task = new TipsUpdater(command, parts, label);
        if (async) {
            Display.getCurrent().asyncExec(new Runnable() {
                public void run() {
                    getViewer().getCanvas().getLightweightSystem()
                            .getUpdateManager().runWithUpdate(new Runnable() {
                                public void run() {
                                    Display.getCurrent().asyncExec(
                                            new Runnable() {
                                                public void run() {
                                                    addSubTask(command, task);
                                                }
                                            });
                                }
                            });
                }
            });
        } else {
            addSubTask(command, task);
        }
    }

    private void addSubTask(Command command, ICancelable task) {
        if (subTasks == null)
            subTasks = new HashMap<Command, ICancelable>();
        subTasks.put(command, task);
        ensureMainTaskStarted();
    }

    private void removeSubTask(Command command) {
        if (subTasks == null || subTasks.isEmpty())
            return;

        ICancelable task = subTasks.remove(command);
        if (task != null) {
            task.cancel();
        }
    }

    private void ensureMainTaskStarted() {
        if (mainTask != null)
            return;

        mainTask = new MainTask(Display.getCurrent());
        mainTask.run();
    }

    private void mainTaskEnded() {
        mainTask = null;
        subTasks = null;
    }

    private String getLabel(Command command, boolean undoOrRedo) {
        String label = command.getLabel();
        if (label == null)
            label = ""; //$NON-NLS-1$
        if (undoOrRedo)
            return NLS.bind(MindMapMessages.UndoRedoTipsService_Undo, label);
        return NLS.bind(MindMapMessages.UndoRedoTipsService_Redo, label);
    }

}