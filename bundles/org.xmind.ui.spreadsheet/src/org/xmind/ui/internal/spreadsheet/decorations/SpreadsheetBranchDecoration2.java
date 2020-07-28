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
package org.xmind.ui.internal.spreadsheet.decorations;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Display;
import org.xmind.gef.draw2d.IDecoratedFigure;
import org.xmind.gef.draw2d.decoration.AbstractDecoration;
import org.xmind.gef.draw2d.decoration.ICorneredDecoration;
import org.xmind.gef.draw2d.decoration.IDecoration;
import org.xmind.gef.draw2d.geometry.PrecisionLine;
import org.xmind.gef.draw2d.geometry.PrecisionRectangle;
import org.xmind.gef.draw2d.graphics.Path;
import org.xmind.gef.graphicalpolicy.IStructure;
import org.xmind.gef.graphicalpolicy.IStyleSelector;
import org.xmind.gef.part.IGraphicalPart;
import org.xmind.ui.branch.IInsertion;
import org.xmind.ui.decorations.IBranchDecoration;
import org.xmind.ui.decorations.ITopicDecoration;
import org.xmind.ui.internal.spreadsheet.Spreadsheet;
import org.xmind.ui.internal.spreadsheet.structures.Chart2;
import org.xmind.ui.internal.spreadsheet.structures.Column2;
import org.xmind.ui.internal.spreadsheet.structures.Row2;
import org.xmind.ui.internal.spreadsheet.structures.RowHead;
import org.xmind.ui.internal.spreadsheet.structures.SpreadsheetColumnStructure;
import org.xmind.ui.mindmap.IBranchPart;
import org.xmind.ui.mindmap.ITitleTextPart;
import org.xmind.ui.mindmap.ITopicPart;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.resources.ColorUtils;
import org.xmind.ui.style.StyleUtils;
import org.xmind.ui.style.Styles;
import org.xmind.ui.util.MindMapUtils;

public class SpreadsheetBranchDecoration2 extends AbstractDecoration
        implements IBranchDecoration {

    private static final Rectangle CLIP_RECT = new Rectangle();

    private static final int INSERTION_ALPHA = 0x60;

    static class Block {
        PrecisionRectangle bounds;
        IGraphicalPart part;
        int alpha = 0xff;

        public Block(PrecisionRectangle bounds, IGraphicalPart part) {
            this.bounds = bounds;
            this.part = part;
        }

        public void paint(Graphics graphics, Path path, int alpha,
                Rectangle clipRect) {
            if (!bounds.intersects(clipRect))
                return;

            Color fillColor = getFillColor();
            if (fillColor == null)
                return;

            graphics.clipRect(bounds.toDraw2DRectangle());
            graphics.setBackgroundColor(fillColor);
            graphics.setAlpha(alpha * this.alpha / 0xff);
            graphics.fillPath(path);
            graphics.restoreState();
        }

        private Color getFillColor() {
            if (part == null) // insertion
                return ColorConstants.gray;

            IStyleSelector ss = StyleUtils.getStyleSelector(part);
            String decorationId = StyleUtils.getString(part, ss,
                    Styles.ShapeClass, null);
            return StyleUtils.getColor(part, ss, Styles.FillColor, decorationId,
                    null);
        }
    }

    private static class Text {

        PrecisionRectangle bounds;

        String text;

        Font font;

        Point textLocation;

        public Text(PrecisionRectangle bounds, String text,
                Point textLocation) {
            this.bounds = bounds;
            this.text = text;
            this.textLocation = textLocation;
        }

        public void paint(Graphics graphics, Rectangle clipRect) {
            if (!bounds.intersects(clipRect))
                return;

            graphics.clipRect(bounds.toDraw2DRectangle());
            if (font != null)
                graphics.setFont(font);
            graphics.drawText(text, textLocation);
            graphics.restoreState();
        }

    }

    private IBranchPart branch;

    private PrecisionRectangle bounds;

    private List<Block> blocks;

    private List<PrecisionLine> lines;

    private PrecisionRectangle insertedCellBounds;

    private List<Text> rowHeads;

    public SpreadsheetBranchDecoration2(IBranchPart branch, String id) {
        super(id);
        this.branch = branch;
    }

    public void validate(IFigure figure) {
        super.validate(figure);

        IStyleSelector ss = StyleUtils.getStyleSelector(branch);
        String decorationId = StyleUtils.getString(branch, ss,
                Styles.ShapeClass, null);
        int lineWidth = StyleUtils.getInteger(branch, ss, Styles.LineWidth,
                decorationId, 1);

        double halfLineWidth1 = lineWidth / 2.0;
        double halfLineWidth2 = lineWidth - halfLineWidth1;
        bounds = new PrecisionRectangle(branch.getFigure().getBounds())
                .shrink(lineWidth, lineWidth);

        double left = bounds.x;
        double right = bounds.right();
        double top = bounds.y;
        double bottom = bounds.bottom();

        boolean ignoreFirstLine;

        Chart2 chart = getChart();

        if (chart != null) {
            int titleHeight = chart.getTitleAreaHeight();
            top += titleHeight;
            if (chart.hasRows()) {
                ignoreFirstLine = false;
            } else {
                ignoreFirstLine = true;
            }
            IInsertion ins = ((IInsertion) MindMapUtils.getCache(branch,
                    IInsertion.CACHE_INSERTION));
            int insWidth = ins == null ? 0
                    : ins.getSize().width + chart.getMajorSpacing();

            int numCols = chart.getNumColumns();
            int numRows = numCols > 0 ? chart.getNumRows() : 0;
            int numLines = Math.max(0, numRows) + Math.max(0, numCols);
            if (ins != null)
                numLines++;
            if (numLines > 0) {
                lines = new ArrayList<PrecisionLine>(numLines);
                double rowHeadLeft;
                double rowHeadWidth;

                int majorSpacing = chart.getMajorSpacing();
                int minorSpacing = chart.getMinorSpacing();
                double x = left;
                double y = top + minorSpacing;
                addHorizontalLine(left, right, top + halfLineWidth2);
                if (chart.hasRows()) {
                    ignoreFirstLine = false;
                    rowHeadLeft = x + lineWidth;
                    rowHeadWidth = chart.getRowHeadWidth()
                            + chart.getMajorSpacing();
                    x += rowHeadWidth + lineWidth;
                    if (chart.hasRows())
                        y += chart.getColHeadHeight();
                } else {
                    x += majorSpacing + lineWidth;
                    addVerticalLine(x, top, bottom);
                    rowHeadLeft = 0;
                    rowHeadWidth = 0;
                }

                RowHead insertionRowHead = null;
                boolean insertionInColumn = false;
                for (int i = 0; i < numCols; i++) {
                    Column2 col = chart.getColumn(i);
                    if (insertionRowHead == null) {
                        insertionRowHead = (RowHead) MindMapUtils.getCache(
                                col.getHead(),
                                Spreadsheet.KEY_INSERTION_ROW_HEAD);
                        if (insertionRowHead != null) {
                            insertionInColumn = true;
                            insertedCellBounds = new PrecisionRectangle();
                        }
                    }
                    if (!ignoreFirstLine) {
                        addVerticalLine(x, top, bottom);
                        if (insertionInColumn)
                            insertedCellBounds.x = x + halfLineWidth1;
                    }
                    ignoreFirstLine = false;

                    int colWidth;
                    if (i == numCols - 1 && ins == null) {
                        colWidth = (int) Math.ceil(right - x);
                    } else {
                        colWidth = col.getHead().getFigure().getBounds().width
                                + minorSpacing;
                    }

                    if (ins != null && i == ins.getIndex()) {
                        Block block = addBlock(null, new PrecisionRectangle(x,
                                top, colWidth, bottom - top));
                        block.alpha = INSERTION_ALPHA;
                        x += insWidth;
                        addVerticalLine(x, top, bottom);
                        x += lineWidth;
                        ins = null;
                    }

                    addBlock(col.getHead(), new PrecisionRectangle(x, top,
                            colWidth, bottom - top));
                    x += colWidth;

                    if (insertionInColumn) {
                        insertedCellBounds.width = x + halfLineWidth1
                                - insertedCellBounds.x;
                        insertionInColumn = false;
                    }
                }
                if (ins != null && ins.getIndex() == numCols) {
                    addVerticalLine(x + halfLineWidth1, top, bottom);
                    Block block = addBlock(null, new PrecisionRectangle(x, top,
                            (int) Math.ceil(right - x), top - bottom));
                    block.alpha = INSERTION_ALPHA;
                }

                IInsertion rowIns = (IInsertion) MindMapUtils.getCache(branch,
                        Spreadsheet.CACHE_ROW_INSERTION);
                for (int i = 0; i < numRows; i++) {
                    if (rowIns != null && rowIns.getIndex() == i) {
                        int rowInsHeight = rowIns.getSize().height + lineWidth;
                        Block block = addBlock(null,
                                new PrecisionRectangle(left, y + halfLineWidth1,
                                        right - left, rowInsHeight));
                        block.alpha = INSERTION_ALPHA;
                        addHorizontalLine(left, right, y + halfLineWidth1);
                        y += rowInsHeight;
                    }
                    Row2 row = chart.getRow(i);
                    RowHead rowHead = row.getHead();
                    boolean insertionInRow = insertionRowHead != null
                            && insertionRowHead.equals(rowHead)
                            && insertedCellBounds != null;
                    if (insertionInRow)
                        insertedCellBounds.y = y + halfLineWidth1;

                    addHorizontalLine(left, right, y);
                    double rowHeight = row.getPrefHeight() + minorSpacing;
                    PrecisionRectangle rowHeadBounds = new PrecisionRectangle(
                            rowHeadLeft, y + lineWidth, rowHeadWidth,
                            rowHeight);
                    String text = rowHead.toString();
                    Dimension size = rowHead.getPrefSize();
                    Text rowHeadText = addRowHeadText(rowHeadBounds, text,
                            center(rowHeadBounds, size.width, size.height));
                    rowHeadText.font = rowHead.getFont();

                    y += lineWidth + rowHeight;
                    if (insertionInRow) {
                        if (i == numRows - 1) {
                            insertedCellBounds.height = bottom
                                    - insertedCellBounds.y;
                        } else {
                            insertedCellBounds.height = y + halfLineWidth1
                                    - insertedCellBounds.y;
                        }
                    }
                }

                if (rowIns != null && rowIns.getIndex() == numRows) {
                    addHorizontalLine(left, right, y + halfLineWidth1);
                    int rowInsHeight = rowIns.getSize().height + minorSpacing
                            + lineWidth;
                    Block block = addBlock(null, new PrecisionRectangle(left, y,
                            right - left, rowInsHeight));
                    block.alpha = INSERTION_ALPHA;
                }
            }
        }

    }

    private Point center(PrecisionRectangle bounds, int width, int height) {
        double x = bounds.x + (bounds.width - width) / 2;
        double y = bounds.y + (bounds.height - height) / 2;
        return new Point((int) x, (int) y);
    }

    List<Block> getBlocks() {
        return blocks;
    }

    private Block addBlock(IGraphicalPart part, PrecisionRectangle bounds) {
        if (blocks == null)
            blocks = new ArrayList<Block>();
        Block block = new Block(bounds, part);
        blocks.add(block);
        return block;
    }

    private Text addRowHeadText(PrecisionRectangle bounds, String text,
            Point textLocation) {
        if (rowHeads == null)
            rowHeads = new ArrayList<Text>();
        Text rowHeadText = new Text(bounds, text, textLocation);
        rowHeads.add(rowHeadText);
        return rowHeadText;
    }

    private void addHorizontalLine(double x1, double x2, double y) {
        lines.add(new PrecisionLine(x1, y, x2, y));
    }

    private void addVerticalLine(double x, double y1, double y2) {
        lines.add(new PrecisionLine(x, y1, x, y2));
    }

    protected int getMinorSpacing() {
        return StyleUtils.getInteger(branch,
                StyleUtils.getStyleSelector(branch), Styles.MinorSpacing, 5);
    }

    protected int getMajorSpacing() {
        return StyleUtils.getMajorSpacing(branch, 5);
    }

    private Chart2 getChart() {
        IStructure sa = branch.getBranchPolicy().getStructure(branch);
        if (sa instanceof SpreadsheetColumnStructure)
            return ((SpreadsheetColumnStructure) sa).getChart(branch);
        return null;
    }

    private ITopicDecoration getTopicDecoration() {
        IFigure topicFigure = getTopicFigure();
        if (topicFigure instanceof IDecoratedFigure) {
            IDecoration decoration = ((IDecoratedFigure) topicFigure)
                    .getDecoration();
            if (decoration instanceof ITopicDecoration)
                return ((ITopicDecoration) decoration);
        }
        return null;
    }

    private IFigure getTopicFigure() {
        ITopicPart topicPart = branch.getTopicPart();
        return topicPart == null ? null : topicPart.getFigure();
    }

    public void invalidate() {
        super.invalidate();
        bounds = null;
        rowHeads = null;
        blocks = null;
        lines = null;
        insertedCellBounds = null;
    }

    private Color getTextColor() {
        ITopicPart topicPart = branch.getTopicPart();
        if (topicPart != null) {
            ITitleTextPart title = topicPart.getTitle();
            if (title != null)
                return title.getFigure().getForegroundColor();
        }
        return null;
    }

    protected void performPaint(IFigure figure, Graphics graphics) {
        graphics.setAntialias(SWT.ON);

        ITopicDecoration topicDecoration = getTopicDecoration();
        int fillAlpha;
        int corner;
        Color fillColor;
        if (topicDecoration != null) {
            fillAlpha = topicDecoration.getFillAlpha();
            fillColor = topicDecoration.getFillColor();
            corner = getCornerSize(topicDecoration);
        } else {
            fillColor = null;
            fillAlpha = 0xff;
            corner = 0;
        }
        if (bounds != null) {
            graphics.pushState();

            int alpha = getAlpha() * fillAlpha / 0xff;
            Path path = new Path(Display.getCurrent());
            addOutline(path, bounds, corner);

            try {
                if (fillColor != null) {
                    graphics.setAlpha(alpha);
                    graphics.setBackgroundColor(fillColor);
                    graphics.fillPath(path);
                    graphics.restoreState();
                }
                if (blocks != null && !blocks.isEmpty()) {
                    for (Block block : blocks) {
                        block.paint(graphics, path, alpha,
                                graphics.getClip(CLIP_RECT));
                    }
                }
                if (rowHeads != null && !rowHeads.isEmpty()) {
                    Color textColor = getTextColor();
                    for (Text head : rowHeads) {
                        graphics.setTextAntialias(SWT.ON);
                        graphics.setForegroundColor(textColor);
                        head.paint(graphics, graphics.getClip(CLIP_RECT));
                    }
                }
            } finally {
                path.dispose();
                graphics.popState();
            }
        }
    }

    private void addOutline(Path path, PrecisionRectangle bounds, int corner) {
        if (corner == 0) {
            path.addRectangle(bounds);
        } else {
            path.addRoundedRectangle(bounds, corner);
        }
    }

    private int getCornerSize(ITopicDecoration topicDecoration) {
        int corner;
        if (topicDecoration instanceof ICorneredDecoration) {
            corner = ((ICorneredDecoration) topicDecoration).getCornerSize();
        } else {
            corner = 0;
        }
        return corner;
    }

    public void paintAboveChildren(IFigure figure, Graphics graphics) {
        if (!isVisible())
            return;

        checkValidation(figure);

        ITopicDecoration topicDecoration = getTopicDecoration();
        if (topicDecoration == null)
            return;

        Color lineColor = topicDecoration.getLineColor();
        if (lineColor == null)
            return;

        int lineAlpha = topicDecoration.getLineAlpha();
        int lineWidth = topicDecoration.getLineWidth();
        int lineStyle = topicDecoration.getLineStyle();
//        int corner = getCornerSize(topicDecoration);
        int corner = 2;

        graphics.setAntialias(SWT.ON);

        if (bounds != null || (lines != null && !lines.isEmpty())
                || insertedCellBounds != null) {
            graphics.setAlpha(getAlpha() * lineAlpha / 0xff);
            graphics.setLineWidth(lineWidth);
            graphics.setLineStyle(lineStyle);
            graphics.setForegroundColor(lineColor);

            Path path = new Path(Display.getCurrent());
            if (bounds != null) {
                addOutline(path, bounds, corner);
            }
            if (lines != null && !lines.isEmpty()) {
                for (PrecisionLine line : lines) {
                    path.moveTo(line.getOrigin());
                    path.lineTo(line.getTerminus());
                }
            }
            graphics.drawPath(path);
            path.dispose();

            if (insertedCellBounds != null) {
                graphics.setAlpha(0x80);
                graphics.setLineWidth(lineWidth + 2);
                graphics.setForegroundColor(
                        ColorUtils.getColor(MindMapUI.COLOR_WARNING));
                path = new Path(Display.getCurrent());
                path.addRectangle(insertedCellBounds);
                graphics.drawPath(path);
                path.dispose();

            }
        }
    }

}