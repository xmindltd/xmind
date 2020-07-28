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
package org.xmind.ui.style;

import org.xmind.core.internal.dom.DOMConstants;

public class Styles {

    /**
     * 
     */
    public static final String MinorSpacing = DOMConstants.ATTR_SPACING_MINOR;

    /**
     * 
     */
    public static final String MajorSpacing = DOMConstants.ATTR_SPACING_MAJOR;

    /**
     * 
     */
    public static final String ShapeClass = DOMConstants.ATTR_SHAPE_CLASS;

    /**
     * 
     */
    public static final String LineClass = DOMConstants.ATTR_LINE_CLASS;

    /**
     * 
     */
    public static final String ArrowEndClass = DOMConstants.ATTR_ARROW_END_CLASS;

    /**
     * 
     */
    public static final String ArrowBeginClass = DOMConstants.ATTR_ARROW_BEGIN_CLASS;

    public static final String FillColor = DOMConstants.ATTR_FILL;

    /**
     * A property key used to describe the color of a line.
     */
    public static final String LineColor = DOMConstants.ATTR_LINE_COLOR;

    /**
     * A property key used to describe colors of multi-colored lines. If this
     * property is unspecified or the value is <i>'default'</i>, it's
     * recommended that the {@link #LineColor} property value be used instead.
     * Typically this property is used on a branch to define colors for
     * connections from the branch's node to its children nodes.
     * <p>
     * Example:
     * </p>
     * <p>
     * Lines with red, blue and green colors:
     * 
     * <pre>
     * &lt;line-properties multi-line-colors=&quot;#ff0000 #00ff00 #0000ff&quot; /&gt;
     * </pre>
     * </p>
     * <p>
     * Lines with default line color:
     * 
     * <pre>
     * &lt;line-properties multi-line-colors=&quot;default&quot; /&gt;
     * </pre>
     * </p>
     * <p>
     * ATTN: Used to use <i>rainbowcolor</i> property and <code>boolean</code>
     * values in version 1.0.
     * </p>
     * 
     * @see #LineColor
     */
    public static final String MultiLineColors = DOMConstants.ATTR_MULTI_LINE_COLORS;

    public static final String GradientColor = DOMConstants.ATTR_GRADIENT_COLOR;

    /**
     * A property key used to describe the width of a line.
     * <p>
     * Example:
     * </p>
     * <p>
     * A line of 1 pixel width:
     * 
     * <pre>
     * &lt;line-properties line-width=&quot;1px&quot;/&gt;
     * </pre>
     * </p>
     */
    public static final String LineWidth = DOMConstants.ATTR_LINE_WIDTH;

    /**
     * A property key used to indicate whether a line is tapered.
     * <p>
     * Example:
     * 
     * <pre>
     * &lt;line-properties line-tapered=&quot;tapered&quot;/&gt;
     * </pre>
     * </p>
     * <p>
     * ATTN: Used to use the <i>'spinylines'</i> property and
     * <code>boolean</code> values in version 1.0
     * </p>
     */
    public static final String LineTapered = DOMConstants.ATTR_LINE_TAPERED;

    public static final String LinePattern = DOMConstants.ATTR_LINE_PATTERN;

    public static final String LineCorner = DOMConstants.ATTR_LINE_CORNER;

    public static final String ShapeCorner = DOMConstants.ATTR_SHAPE_CORNER;

    public static final String BorderLineColor = DOMConstants.ATTR_BORDER_LINE_COLOR;

    public static final String BorderLineWidth = DOMConstants.ATTR_BORDER_LINE_WIDTH;

    /**
     * A property key used to describe the class of a line connecting itself
     * from its parent.
     */
    public static final String CalloutLineClass = DOMConstants.ATTR_CALLOUT_LINE_CLASS;

    public static final String CalloutShapeClass = DOMConstants.ATTR_CALLOUT_SHAPE_CLASS;

    public static final String CalloutLineColor = DOMConstants.ATTR_CALLOUT_LINE_COLOR;

    public static final String CalloutLineWidth = DOMConstants.ATTR_CALLOUT_LINE_WIDTH;

    public static final String CalloutLineCorner = DOMConstants.ATTR_CALLOUT_LINE_CORNER;

    public static final String CalloutFillColor = DOMConstants.ATTR_CALLOUT_FILL_COLOR;

    public static final String CalloutLinePattern = DOMConstants.ATTR_CALLOUT_LINE_PATTERN;

    public static final String LeftMargin = DOMConstants.ATTR_MARGIN_LEFT;

    public static final String RightMargin = DOMConstants.ATTR_MARGIN_RIGHT;

    public static final String TopMargin = DOMConstants.ATTR_MARGIN_TOP;

    public static final String BottomMargin = DOMConstants.ATTR_MARGIN_BOTTOM;

    public static final String Background = DOMConstants.ATTR_BACKGROUND;

    // Text style keys:
    public static final String TextColor = DOMConstants.ATTR_COLOR;

    public static final String BackgroundColor = DOMConstants.ATTR_BACKGROUND_COLOR;

    public static final String FontFamily = DOMConstants.ATTR_FONT_FAMILY;

    public static final String FontWeight = DOMConstants.ATTR_FONT_WEIGHT;

    public static final String FontStyle = DOMConstants.ATTR_FONT_STYLE;

    public static final String FontSize = DOMConstants.ATTR_FONT_SIZE;

    public static final String TextDecoration = DOMConstants.ATTR_FONT_DECORATION;

    public static final String TextAlign = DOMConstants.ATTR_TEXT_ALIGN;

    public static final String TextBullet = DOMConstants.ATTR_TEXT_BULLET;

    public static final String TextCase = DOMConstants.ATTR_TEXT_TRANSFORM;

    /**
     * A property key used to describing the opacity of a object. The value
     * varies from <code>0.0</code> to <code>1.0</code>. A lower value means
     * more transparency.
     */
    public static final String Opacity = DOMConstants.ATTR_OPACITY;

    /**
     * A special property key for getting a unique id associated with a
     * decoration to be applied on a {@link org.xmind.ui.mindmap.IBranchPart}.
     * The default value is {@link Styles#DEF_BRANCH_DECORATION}.
     * <p>
     * <b>NOTE:</b> This property is only regarded as a UI property and not a
     * part of the XMIND Core Specification. Graphical policy authors may extend
     * a style selector to provide their own value associated with this key.
     * </p>
     */
    public static final String BranchDecorationClass = "branch-decoration-class"; //$NON-NLS-1$

    /**
     * A special property key for getting the rotate angle. The angle will be
     * applied on {@link org.xmind.ui.internal.figures.TopicFigure}. The default
     * value is <code>0</code>.
     * <p>
     * <b>NOTE:</b> This property is only regarded as a UI property and not a
     * part of the XMIND Core Specification. Graphical policy authors may extend
     * a style selector to provide their own value associated with this key.
     * </p>
     */
    public static final String RotateAngle = "rotate-angle"; //$NON-NLS-1$

    /**
     * A special property key for determining whether or not to hide the figure
     * representing the topic's labels.
     * <p>
     * <b>NOTE:</b> This property is only regarded as a UI property and not a
     * part of the XMIND Core Specification. Graphical policy authors may extend
     * a style selector to provide their own value associated with this key.
     * </p>
     */
    public static final String HideChildrenLabels = "hide-children-labels"; //$NON-NLS-1$

    /**
     * A special property key for determining whether or not to hide the figure
     * representing callout under special branch policy.
     * <p>
     * <b>NOTE:</b> This property is only regarded as a UI property and not a
     * part of the XMIND Core Specification. Graphical policy authors may extend
     * a style selector to provide their own value associated with this key.
     * </p>
     */
    public static final String HideCallout = "hide-callout"; //$NON-NLS-1$

    /**
     * A special property key for determining whether or not to icon tip only
     * under special branch policy.
     * <p>
     * <b>NOTE:</b> This property is only regarded as a UI property and not a
     * part of the XMIND Core Specification. Graphical policy authors may extend
     * a style selector to provide their own value associated with this key.
     * </p>
     */
    public static final String IconTipOnly = "iconTip-only"; //$NON-NLS-1$

    // ===============================
    //   Values:
    // ===============================

    /**
     * A special identifier ('$NULL$') representing a <code>null</code> key or a
     * <code>null</code> value.
     * <p>
     * <b>WARNING</b>: For programming usage only! Not intended to be persisted
     * into file.
     * </p>
     */
    public static final String NULL = "$NULL$"; //$NON-NLS-1$

    /**
     * A special value ('none') indicating that no actual value should be used
     * for this property. Normally this will cause <code>null</code> to be
     * returned ignoring the default value.
     * <p>
     * Ok to be persisted into file.
     * </p>
     */
    public static final String NONE = DOMConstants.VAL_NONE;

    /**
     * A special value ('$system$') indicating that the actual value should be
     * obtained from the current operating system or graphical environment,
     * e.g., the system font, the system foreground/background color, etc.
     * <p>
     * Ok to be persisted into file.
     * </p>
     */
    public static final String SYSTEM = DOMConstants.VAL_SYSTEM;

    /**
     * Value='default'
     */
    public static final String DEFAULT = DOMConstants.VAL_DEFAULT;

    /**
     * Value='solid'
     */
    public static final String LINE_PATTERN_SOLID = DOMConstants.VAL_LINE_SOLID;

    /**
     * Value='dash'
     */
    public static final String LINE_PATTERN_DASH = DOMConstants.VAL_LINE_DASH;

    /**
     * Value='dot'
     */
    public static final String LINE_PATTERN_DOT = DOMConstants.VAL_LINE_DOT;

    /**
     * Value='dash-dot'
     */
    public static final String LINE_PATTERN_DASH_DOT = DOMConstants.VAL_LINE_DASH_DOT;

    /**
     * Value='dash-dot-dot'
     */
    public static final String LINE_PATTERN_DASH_DOT_DOT = DOMConstants.VAL_LINE_DASH_DOT_DOT;

    /**
     * Value='normal'
     */
    public static final String NORMAL = DOMConstants.VAL_NORMAL;

    /**
     * 
     */
    public static final String FONT_WEIGHT_BOLD = DOMConstants.VAL_BOLD;

    /**
     * 
     */
    public static final String FONT_STYLE_ITALIC = DOMConstants.VAL_ITALIC;

    /**
     * 
     */
    public static final String TEXT_DECORATION_UNDERLINE = DOMConstants.VAL_UNDERLINE;

    /**
     * 
     */
    public static final String TEXT_DECORATION_LINE_THROUGH = DOMConstants.VAL_LINE_THROUGH;

    /**
     * 
     */
    public static final String TEXT_STYLE_BULLET = DOMConstants.VAL_BULLET;

    /**
     * 
     */
    public static final String TEXT_STYLE_NUMBER = DOMConstants.VAL_NUMBER;
    /**
     * This property value indicating that the width of a line tapers from one
     * end to the other.
     * 
     * @see #LineTapered
     */
    public static final String TAPERED = DOMConstants.VAL_TAPERED;

    public static final String GRADIENT = DOMConstants.VAL_GRADIENT;

    public static final String ALIGN_LEFT = DOMConstants.VAL_LEFT;

    public static final String ALIGN_RIGHT = DOMConstants.VAL_RIGHT;

    public static final String ALIGN_CENTER = DOMConstants.VAL_CENTER;

    public static final String DEFAULT_MULTI_LINE_COLORS = "#017c98 #00b2a1 #ffdd00 #fc8f00 #ff1500 #00b04c"; //$NON-NLS-1$

    public static final String MANUAL = DOMConstants.VAL_MANUAL;

    public static final String UPPERCASE = DOMConstants.VAL_UPPERCASE;

    public static final String LOWERCASE = DOMConstants.VAL_LOWERCASE;

    public static final String CAPITALIZE = DOMConstants.VAL_CAPITALIZE;

    // ===============================
    //   Types:
    // ===============================

    public static final String FAMILY_MAP = "map"; //$NON-NLS-1$

    public static final String FAMILY_CENTRAL_TOPIC = "centralTopic"; //$NON-NLS-1$

    public static final String FAMILY_MAIN_TOPIC = "mainTopic"; //$NON-NLS-1$

    public static final String FAMILY_SUB_TOPIC = "subTopic"; //$NON-NLS-1$

    public static final String FAMILY_CALLOUT_TOPIC = "calloutTopic"; //$NON-NLS-1$

    public static final String FAMILY_FLOATING_TOPIC = "floatingTopic"; //$NON-NLS-1$

    public static final String FAMILY_SUMMARY_TOPIC = "summaryTopic"; //$NON-NLS-1$

    public static final String FAMILY_BOUNDARY = "boundary"; //$NON-NLS-1$

    public static final String FAMILY_RELATIONSHIP = "relationship"; //$NON-NLS-1$

    public static final String FAMILY_SUMMARY = "summary"; //$NON-NLS-1$

    // ========================
    //   Decorations
    // ------------------------

    /**
     * Default Branch Decoration ID
     * (value='org.xmind.ui.branchDecoration.default'), a decoration that draws
     * a short line connecting up the topic to its connections with subtopics.
     */
    public static final String DEF_BRANCH_DECORATION = "org.xmind.branchDecoration.default"; //$NON-NLS-1$

    // Branch Connection Shapes:
    public static final String BRANCH_CONN_STRAIGHT = "org.xmind.branchConnection.straight"; //$NON-NLS-1$

    public static final String BRANCH_CONN_CURVE = "org.xmind.branchConnection.curve"; //$NON-NLS-1$

    public static final String BRANCH_CONN_ARROWED_CURVE = "org.xmind.branchConnection.arrowedCurve"; //$NON-NLS-1$

    public static final String BRANCH_CONN_ROUNDEDELBOW = "org.xmind.branchConnection.roundedElbow"; //$NON-NLS-1$

    public static final String BRANCH_CONN_ELBOW = "org.xmind.branchConnection.elbow"; //$NON-NLS-1$

    public static final String BRANCH_CONN_NONE = "org.xmind.branchConnection.none"; //$NON-NLS-1$

    // Topic Shapes:
    public static final String TOPIC_SHAPE_ROUNDEDRECT = "org.xmind.topicShape.roundedRect"; //$NON-NLS-1$

    public static final String TOPIC_SHAPE_RECT = "org.xmind.topicShape.rect"; //$NON-NLS-1$

    public static final String TOPIC_SHAPE_ELLIPSE = "org.xmind.topicShape.ellipse"; //$NON-NLS-1$

    public static final String TOPIC_SHAPE_UNDERLINE = "org.xmind.topicShape.underline"; //$NON-NLS-1$

    public static final String TOPIC_SHAPE_DIAMOND = "org.xmind.topicShape.diamond"; //$NON-NLS-1$

    public static final String TOPIC_SHAPE_CALLOUT_ELLIPSE = "org.xmind.topicShape.callout.ellipse"; //$NON-NLS-1$

    public static final String TOPIC_SHAPE_CALLOUT_ROUNDEDRECT = "org.xmind.topicShape.callout.roundedRect"; //$NON-NLS-1$

    public static final String TOPIC_SHAPE_NO_BORDER = "org.xmind.topicShape.noBorder"; //$NON-NLS-1$

    public static final String TOPIC_SHAPE_CIRCLE = "org.xmind.topicShape.circle"; //$NON-NLS-1$

    public static final String TOPIC_SHAPE_PARALLELOGRAM = "org.xmind.topicShape.parallelogram"; //$NON-NLS-1$

    public static final String TOPIC_SHAPE_CLOUD = "org.xmind.topicShape.cloud"; //$NON-NLS-1$

    public static final String TOPIC_SHAPE_STROKE_CIRCLE = "org.xmind.topicShape.stroke"; //$NON-NLS-1$

    //Callout Topic Shapes:
    public static final String CALLOUT_TOPIC_SHAPE_BALLOON_ROUNDEDRECT = "org.xmind.calloutTopicShape.balloon.roundedRect"; //$NON-NLS-1$

    public static final String CALLOUT_TOPIC_SHAPE_BALLOON_ELLIPSE = "org.xmind.calloutTopicShape.balloon.ellipse"; //$NON-NLS-1$

    public static final String CALLOUT_TOPIC_SHAPE_BALLOON_RECTANGLE = "org.xmind.calloutTopicShape.balloon.rectangle"; //$NON-NLS-1$

    // Boundary Shapes:
    public static final String BOUNDARY_SHAPE_ROUNDEDRECT = "org.xmind.boundaryShape.roundedRect"; //$NON-NLS-1$

    public static final String BOUNDARY_SHAPE_RECT = "org.xmind.boundaryShape.rect"; //$NON-NLS-1$

    public static final String BOUNDARY_SHAPE_SCALLOPS = "org.xmind.boundaryShape.scallops"; //$NON-NLS-1$

    public static final String BOUNDARY_SHAPE_WAVES = "org.xmind.boundaryShape.waves"; //$NON-NLS-1$

    public static final String BOUNDARY_SHAPE_TENSION = "org.xmind.boundaryShape.tension"; //$NON-NLS-1$

    public static final String BOUNDARY_SHAPE_POLYGON = "org.xmind.boundaryShape.polygon"; //$NON-NLS-1$

    public static final String BOUNDARY_SHAPE_ROUNDEDPOLYGON = "org.xmind.boundaryShape.roundedPolygon"; //$NON-NLS-1$

    // Relationship Shapes:
    public static final String REL_SHAPE_CURVED = "org.xmind.relationshipShape.curved"; //$NON-NLS-1$

    public static final String REL_SHAPE_ANGLED = "org.xmind.relationshipShape.angled"; //$NON-NLS-1$

    public static final String REL_SHAPE_STRAIGHT = "org.xmind.relationshipShape.straight"; //$NON-NLS-1$

    // Arrow Shapes:
    public static final String ARROW_SHAPE_NORMAL = "org.xmind.arrowShape.normal"; //$NON-NLS-1$

    public static final String ARROW_SHAPE_SPEARHEAD = "org.xmind.arrowShape.spearhead"; //$NON-NLS-1$

    public static final String ARROW_SHAPE_TRIANGLE = "org.xmind.arrowShape.triangle"; //$NON-NLS-1$

    public static final String ARROW_SHAPE_DOT = "org.xmind.arrowShape.dot"; //$NON-NLS-1$

    public static final String ARROW_SHAPE_SQUARE = "org.xmind.arrowShape.square"; //$NON-NLS-1$

    public static final String ARROW_SHAPE_DIAMOND = "org.xmind.arrowShape.diamond"; //$NON-NLS-1$

    public static final String ARROW_SHAPE_HERRINGBONE = "org.xmind.arrowShape.herringbone"; //$NON-NLS-1$

    public static final String ARROW_SHAPE_NONE = "org.xmind.arrowShape.none"; //$NON-NLS-1$

    // Summary Shapes:
    public static final String SUMMARY_SHAPE_ANGLE = "org.xmind.summaryShape.angle"; //$NON-NLS-1$

    public static final String SUMMARY_SHAPE_ROUND = "org.xmind.summaryShape.round"; //$NON-NLS-1$

    public static final String SUMMARY_SHAPE_SQUARE = "org.xmind.summaryShape.square"; //$NON-NLS-1$

    public static final String SUMMARY_SHAPE_CURLY = "org.xmind.summaryShape.curly"; //$NON-NLS-1$

    public static final String TEXT_UNDERLINE_AND_LINE_THROUGH = TEXT_DECORATION_UNDERLINE
            + " " + TEXT_DECORATION_LINE_THROUGH; //$NON-NLS-1$

    public static final String DEF_TEXT_COLOR = "#000000"; //$NON-NLS-1$

    public static final String DEF_TOPIC_FILL_COLOR = "#d0d0d0"; //$NON-NLS-1$

    public static final String DEF_TOPIC_LINE_COLOR = "#808080"; //$NON-NLS-1$

    public static final String DEF_REL_LINE_COLOR = "#3050f0"; //$NON-NLS-1$

    public static final String DEF_REL_TITLE_FILL_COLOR = "#f0f0f0"; //$NON-NLS-1$

    public static final String DEF_SHEET_FILL_COLOR = "#ffffff"; //$NON-NLS-1$

    public static final String DEF_SUMMARY_LINE_COLOR = "#808080"; //$NON-NLS-1$

    public static final int DEF_BOUNARY_ALPHA = 0x80;

    public static final int DEF_BOUNDARY_LINE_WIDTH = 3;

    public static final String DEF_BOUNDARY_LINE_COLOR = "#afafaf"; //$NON-NLS-1$

    public static final String DEF_BOUNDARY_FILL_COLOR = "#ffffff"; //$NON-NLS-1$

    public static final double DEF_CONTROL_POINT_ANGLE = 0;

    public static final double DEF_CONTROL_POINT_AMOUNT = 0.4;

    public static final int DEFAULT_SUMMARY_WIDTH = 20;

    public static final int DEFAULT_SUMMARY_SPACING = 5;

    /**
     * TODO use style selector
     */
    public static final String LABEL_FILL_COLOR = "#ffff80"; //$NON-NLS-1$

    /**
     * TODO use style selector
     */
    public static final String LABEL_BORDER_COLOR = "#a0a0a0"; //$NON-NLS-1$

    /**
     * TODO use style selector
     */
    public static final String LABEL_TEXT_COLOR = "#000000"; //$NON-NLS-1$

    public static final String LEGEND_FILL_COLOR = "#ffffd0"; //$NON-NLS-1$

    public static final String LEGEND_LINE_COLOR = "#dddddd"; //$NON-NLS-1$

    public static final String DEF_YELLOWBOX_FILL_COLOR = "#fff8b6"; //$NON-NLS-1$

    public static final String YELLOWBOX_TEXT_COLOR = "#333333"; //$NON-NLS-1$

    public static final String YELLOWBOX_LINE_COLOR = "dddddd"; //$NON-NLS-1$

    /**
     * The default height of plus-minus figure (value=9).
     */
    public static final int PLUS_MINUS_HEIGHT = 11;

    public static final int DEFAULT_EXPANSION = 7;

    public static final String LAYER_BEFORE_USER_VALUE = "beforeUserValue"; //$NON-NLS-1$

    public static final String LAYER_BEFORE_PARENT_VALUE = "beforeParentValue"; //$NON-NLS-1$

    public static final String LAYER_BEFORE_THEME_VALUE = "beforeThemeValue"; //$NON-NLS-1$

    public static final String LAYER_BEFORE_DEFAULT_VALUE = "beforeDefaultValue"; //$NON-NLS-1$

    public static final String LAYER_AFTER_ALL_VALUE = "afterAllValue"; //$NON-NLS-1$

    public static final String YellowBoxFillColor = "yellowBoxFillColor"; //$NON-NLS-1$

    public static final String LegendFillColor = "legendFillColor"; //$NON-NLS-1$
}
