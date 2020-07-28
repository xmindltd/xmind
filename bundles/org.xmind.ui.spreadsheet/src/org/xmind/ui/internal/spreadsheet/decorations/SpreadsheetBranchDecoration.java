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
import org.xmind.ui.internal.spreadsheet.structures.Chart;
import org.xmind.ui.internal.spreadsheet.structures.Column;
import org.xmind.ui.internal.spreadsheet.structures.ColumnHead;
import org.xmind.ui.internal.spreadsheet.structures.Row;
import org.xmind.ui.internal.spreadsheet.structures.SpreadsheetStructure;
import org.xmind.ui.mindmap.IBranchPart;
import org.xmind.ui.mindmap.ITitleTextPart;
import org.xmind.ui.mindmap.ITopicPart;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.resources.ColorUtils;
import org.xmind.ui.style.StyleUtils;
import org.xmind.ui.style.Styles;
import org.xmind.ui.util.MindMapUtils;

public class SpreadsheetBranchDecoration extends AbstractDecoration
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

    private List<Text> columnHeads;

    public SpreadsheetBranchDecoration(IBranchPart branch, String id) {
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

        double y;
        boolean ignoreFirstLine;

        Chart chart = getChart();
        if (chart != null) {
            int titleHeight = chart.getTitleAreaHeight();
            if (titleHeight > 0) {
                y = top + titleHeight;
                ignoreFirstLine = false;
                top = y + halfLineWidth1;
            } else {
                y = top - halfLineWidth1;
                ignoreFirstLine = true;
            }
            IInsertion ins = ((IInsertion) MindMapUtils.getCache(branch,
                    IInsertion.CACHE_INSERTION));
            int insHeight = ins == null ? 0
                    : ins.getSize().height + chart.getMajorSpacing();

            int numRows = chart.getNumRows();
            int numCols = numRows > 0 ? chart.getNumColumns() : 0;
            int numLines = Math.max(0, numCols) + Math.max(0, numRows);
            if (ins != null)
                numLines++;
            if (numLines > 0) {
                lines = new ArrayList<PrecisionLine>(numLines);
                double colHeadTop;
                double colHeadHeight;
                if (chart.hasColumns()) {
                    if (!ignoreFirstLine) {
                        addHorizontalLine(left, right, y + halfLineWidth1);
                    }
                    ignoreFirstLine = false;
                    colHeadTop = y + lineWidth;
                    colHeadHeight = chart.getColumnHeadHeight()
                            + chart.getMajorSpacing();
                    y += lineWidth + colHeadHeight;
                } else {
                    colHeadTop = 0;
                    colHeadHeight = 0;
                }

                int minorSpacing = chart.getMinorSpacing();
                ColumnHead insertionColHead = null;
                boolean insertionInRow = false;
                for (int i = 0; i < numRows; i++) {
                    Row row = chart.getRow(i);
                    if (insertionColHead == null) {
                        insertionColHead = (ColumnHead) MindMapUtils.getCache(
                                row.getHead(),
                                Spreadsheet.KEY_INSERTION_COLUMN_HEAD);
                        if (insertionColHead != null) {
                            insertionInRow = true;
                            insertedCellBounds = new PrecisionRectangle();
                        }
                    }
                    if (!ignoreFirstLine) {
                        addHorizontalLine(left, right, y + halfLineWidth1);
                        if (insertionInRow) {
                            insertedCellBounds.y = y + halfLineWidth1;
                        }

                    }
                    ignoreFirstLine = false;
                    y += lineWidth;

                    if (ins != null && i == ins.getIndex()) {
                        Block block = addBlock(null, new PrecisionRectangle(
                                left, y, right - left, insHeight));
                        block.alpha = INSERTION_ALPHA;
                        y += insHeight;
                        addHorizontalLine(left, right, y + halfLineWidth1);
                        y += lineWidth;
                        ins = null;
                    }

                    int rowHeight;
                    if (i == numRows - 1 && ins == null) {
                        rowHeight = (int) Math.ceil(bottom - y);
                    } else {
                        rowHeight = row.getHead().getFigure().getBounds().height
                                + minorSpacing;
                    }
                    addBlock(row.getHead(), new PrecisionRectangle(left, y,
                            right - left, rowHeight));
                    y += rowHeight;

                    if (insertionInRow) {
                        insertedCellBounds.height = y + halfLineWidth1
                                - insertedCellBounds.y;
                        insertionInRow = false;
                    }
                }

                if (ins != null && ins.getIndex() == numRows) {
                    addHorizontalLine(left, right, y + halfLineWidth1);
                    Block block = addBlock(null, new PrecisionRectangle(left, y,
                            right - left, (int) Math.ceil(bottom - y)));
                    block.alpha = INSERTION_ALPHA;
                }

                double x = left + halfLineWidth2 + chart.getRowHeadWidth()
                        + minorSpacing;

                IInsertion colIns = (IInsertion) MindMapUtils.getCache(branch,
                        Spreadsheet.CACHE_COLUMN_INSERTION);
                for (int i = 0; i < numCols; i++) {
                    if (colIns != null && colIns.getIndex() == i) {
                        int colInsWidth = colIns.getSize().width
                                + chart.getMinorSpacing() + lineWidth;
                        Block block = addBlock(null,
                                new PrecisionRectangle(x + halfLineWidth1, top,
                                        colInsWidth, bottom - top));
                        block.alpha = INSERTION_ALPHA;
                        addVerticalLine(x + halfLineWidth1, top, bottom);
                        x += colInsWidth;
                    }
                    Column col = chart.getColumn(i);
                    ColumnHead colHead = col.getHead();
                    boolean insertionInColumn = insertionColHead != null
                            && insertionColHead.equals(colHead)
                            && insertedCellBounds != null;
                    if (insertionInColumn) {
                        insertedCellBounds.x = x + halfLineWidth1;
                    }
                    addVerticalLine(x + halfLineWidth1, top, bottom);
                    double columnWidth = col.getWidth();//getPrefCellWidth() + minorSpacing;
                    PrecisionRectangle colHeadBounds = new PrecisionRectangle(
                            x + lineWidth, colHeadTop, columnWidth,
                            colHeadHeight);
                    String text = colHead.toString();
                    Dimension size = colHead.getPrefSize();
                    Text colHeadText = addColumnHeadText(colHeadBounds, text,
                            center(colHeadBounds, size.width, size.height));
                    colHeadText.font = colHead.getFont();

                    x += lineWidth + columnWidth;
                    if (insertionInColumn) {
                        if (i == numCols - 1) {
                            insertedCellBounds.width = right
                                    - insertedCellBounds.x;
                        } else {
                            insertedCellBounds.width = x + halfLineWidth1
                                    - insertedCellBounds.x;
                        }
                    }
                }

                if (colIns != null && colIns.getIndex() == numCols) {
                    addVerticalLine(x + halfLineWidth1, top, bottom);
                    int colInsWidth = colIns.getSize().width
                            + chart.getMinorSpacing() + lineWidth;
                    Block block = addBlock(null,
                            new PrecisionRectangle(x + halfLineWidth1, top,
                                    colInsWidth, bottom - top));
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

    private Text addColumnHeadText(PrecisionRectangle bounds, String text,
            Point textLocation) {
        if (columnHeads == null)
            columnHeads = new ArrayList<Text>();
        Text columnHeadText = new Text(bounds, text, textLocation);
        columnHeads.add(columnHeadText);
        return columnHeadText;
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

    private Chart getChart() {
        IStructure sa = branch.getBranchPolicy().getStructure(branch);
        if (sa instanceof SpreadsheetStructure)
            return ((SpreadsheetStructure) sa).getChart(branch);
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
        columnHeads = null;
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
                if (columnHeads != null && !columnHeads.isEmpty()) {
                    Color textColor = getTextColor();
                    for (Text head : columnHeads) {
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