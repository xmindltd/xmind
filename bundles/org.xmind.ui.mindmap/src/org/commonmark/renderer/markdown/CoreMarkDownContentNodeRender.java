package org.commonmark.renderer.markdown;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.commonmark.node.AbstractVisitor;
import org.commonmark.node.BlockQuote;
import org.commonmark.node.BulletList;
import org.commonmark.node.Code;
import org.commonmark.node.Document;
import org.commonmark.node.Emphasis;
import org.commonmark.node.FencedCodeBlock;
import org.commonmark.node.HardLineBreak;
import org.commonmark.node.Heading;
import org.commonmark.node.HtmlBlock;
import org.commonmark.node.HtmlInline;
import org.commonmark.node.Image;
import org.commonmark.node.IndentedCodeBlock;
import org.commonmark.node.Link;
import org.commonmark.node.ListItem;
import org.commonmark.node.Node;
import org.commonmark.node.OrderedList;
import org.commonmark.node.Paragraph;
import org.commonmark.node.SoftLineBreak;
import org.commonmark.node.StrongEmphasis;
import org.commonmark.node.Text;
import org.commonmark.node.ThematicBreak;
import org.commonmark.renderer.NodeRenderer;

public class CoreMarkDownContentNodeRender extends AbstractVisitor
        implements NodeRenderer {

    private final MarkDownContentWriter mdWriter;
    protected final MarkDownContentNodeRendererContext context;

    public CoreMarkDownContentNodeRender(
            MarkDownContentNodeRendererContext context) {
        this.context = context;
        this.mdWriter = context.getWriter();
    }

    @Override
    public void visit(Heading heading) {

        int level = heading.getLevel();

        if (level == 1 || level == 2) {
            mdWriter.write("\n");
            for (int i = 0; i < level; i++) {
                mdWriter.write("#");
            }
            mdWriter.whitespace();

            visitChildren(heading);

            mdWriter.whitespace();
            for (int i = 0; i < level; i++) {
                mdWriter.write("#");
            }

            mdWriter.line();
        }

        if (level == 3) {
            mdWriter.write("-");
            mdWriter.whitespace();
            visitChildren(heading);
            mdWriter.line();
        }
        if (level == 4) {
            mdWriter.write("  -");
            mdWriter.whitespace();
            visitChildren(heading);
            mdWriter.line();
        }

    }

    @Override
    public void visit(Text text) {
        writeText(text.getLiteral());
    }

    private void writeText(String text) {
        mdWriter.write(text);
    }

    private void writeEndOfLine(Node node, Character c) {

        mdWriter.write(c);

        /*
         * if (node.getNext() != null) { mdWriter.line(); }
         */

    }

    public void render(Node node) {
        node.accept(this);

    }

    @Override
    public void visit(Document document) {
        // No rendering itself
        visitChildren(document);
    }

    @Override
    public void visit(Code code) {

    }

    public Set<Class<? extends Node>> getNodeTypes() {
        return new HashSet(Arrays.asList(Document.class, Heading.class,
                Paragraph.class, BlockQuote.class, BulletList.class,
                FencedCodeBlock.class, HtmlBlock.class, ThematicBreak.class,
                IndentedCodeBlock.class, Link.class, ListItem.class,
                OrderedList.class, Image.class, Emphasis.class,
                StrongEmphasis.class, Text.class, Code.class, HtmlInline.class,
                SoftLineBreak.class, HardLineBreak.class));
    }

}
