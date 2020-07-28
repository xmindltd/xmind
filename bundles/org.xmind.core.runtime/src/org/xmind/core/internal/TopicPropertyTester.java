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
package org.xmind.core.internal;

import java.util.List;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.core.runtime.Assert;
import org.xmind.core.IImage;
import org.xmind.core.ITopic;
import org.xmind.core.internal.xpath.Evaluator;

/**
 * A property tester used to test whether a topic contains expected properties.
 * 
 * <p>
 * The <code>receiver</code> to test against MUST be an {@link ITopic} instance.
 * Otherwise, {@link IllegalArgumentException} will be thrown.
 * </p>
 * 
 * <h2>Supported <code>property</code> Values</h2>
 * 
 * <dl>
 * 
 * <dt>eval</dt>
 * <dd>Evaluates the topic property evaluation expression (specified by the
 * expected value) against the receiver topic. See 'Topic Property Evaluation'
 * for details of syntax. The evaluation result MUST be a boolean value.</dd>
 * 
 * <dt>type</dt>
 * <dd>Compares the expected value against the type of the receiver topic
 * retrieved via {@link ITopic#getType()}. This property is a shortcut of
 * testing the <code>eval</code> property with 'args' specified as
 * <code>matches(@type,'EXPECTED_VALUE')</code> if the expected value starts
 * with <code>"^"</code> or <code>@type='EXPECTED_VALUE'</code> otherwise.</dd>
 * 
 * <dt>title</dt>
 * <dd>Compares the expected value against the topic title text retrieved via
 * {@link ITopic#getTitleText()}. This property is a shortcut of testing the
 * <code>eval</code> property with 'args' specified as
 * <code>matches(@title,'EXPECTED_VALUE')</code> if the expected value starts
 * with <code>"^"</code> or <code>@title='EXPECTED_VALUE'</code> otherwise.</dd>
 * 
 * <dt>structureClass</dt>
 * <dd>Compares the expected value against the structure class retrieved via
 * {@link ITopic#getStructureClass()}. This property is a shortcut of testing
 * the <code>eval</code> property with 'args' specified as
 * <code>matches(@structureClass,'EXPECTED_VALUE')</code> if the expected value
 * starts with <code>"^"</code> or <code>@structureClass='EXPECTED_VALUE'</code>
 * otherwise.</dd>
 * 
 * <dt>folded</dt>
 * <dd>Compares the expected value against the folded state retrieved via
 * {@link ITopic#isFolded()}. This property is a shortcut of testing the
 * <code>eval</code> property with 'args' specified as <code>@folded</code>.
 * </dd>
 * 
 * <dt>hyperlink</dt>
 * <dd>Compares the expected value against the hyperlink value retrieved via
 * {@link ITopic#getHyperlink()}. This property is a shortcut of testing the
 * <code>eval</code> property with 'args' specified as
 * <code>matches(@hyperlink,'EXPECTED_VALUE')</code> if the expected value
 * starts with <code>"^"</code> or <code>@hyperlink='EXPECTED_VALUE'</code>
 * otherwise.</dd>
 * 
 * <dt>imageSource</dt>
 * <dd>Compares the expected value against the image source retrieved via
 * {@link ITopic#getImage()} and {@link IImage#getSource()}. This property is a
 * shortcut of testing the <code>eval</code> property with 'args' specified as
 * <code>matches(image/@source,'EXPECTED_VALUE')</code> if the expected value
 * starts with <code>"^"</code> or <code>image/@source='EXPECTED_VALUE'</code>
 * otherwise.</dd>
 * 
 * </dl>
 * 
 * <h2>Topic Property Evaluation</h2>
 * 
 * <p>
 * A topic property evaluation expression is a function call (e.g.
 * <code>matches(@type,'(de|at)tached')</code>), a comparison (e.g.
 * <code>@type!='attached'</code>) or an evaluation of a boolean value (e.g.
 * <code>@folded</code>). Variables may be an XPath-like query (e.g.
 * <code>topic[@type='attached']/label/text()</code>), a string value (e.g.
 * <code>'xxx'</code>), a number value (e.g. <code>23</code>) or a boolean value
 * (e.g. <code>true()</code> or <code>false()</code>).
 * </p>
 * 
 * <p>
 * An XPath-like query is evaluated against the receiver topic, so it starts
 * <em>without</em> a leading slash (<code>/</code>) to indicate that this is a
 * relative path. See the following Topic Model Hierarchy for what elements and
 * attributes can be selected.
 * </p>
 * 
 * <p>
 * This class only supports a small set of XPath 2 specifications.
 * </p>
 * 
 * <dl>
 * <dt><em>label</em></dt>
 * <dd>(Collection) Returns the child element labeled by the specified label
 * </dd>
 * <dt><em>@attr</em></dt>
 * <dd>(String, Boolean, Number) Returns the specified attribute value of the
 * context object (or the first object of the context collection)</dd>
 * <dt><em>x</em>=<em>y</em>, <em>x</em>!=<em>y</em>, <em>x</em>&gt;<em>y</em>,
 * <em>x</em>&lt;<em>y</em>, <em>x</em>&gt;=<em>y</em>, <em>x</em>&lt;=
 * <em>y</em></dt>
 * <dd>(Boolean) Compares <em>x</em> and <em>y</em> using the literal operator
 * </dd>
 * <dt>count(Collection <em>node-set</em>)</dt>
 * <dd>(Number) Returns the count of the nodes in the specified node set</dd>
 * <dt>matches(String <em>str</em>, String <em>pattern</em>)</dt>
 * <dd>(Boolean) Returns whether the string matches the specified regular
 * expression pattern</dd>
 * <dt>true()</dt>
 * <dd>(Boolean) Returns <em>true</em></dd>
 * <dt>false()</dt>
 * <dd>(Boolean) Returns <em>false</em></dd>
 * </dl>
 * 
 * <h2>Topic Model Hierarchy</h2>
 * 
 * <dl>
 * 
 * <dt>topic</dt>
 * <dd>
 * <dl>
 * <dt>Type</dt>
 * <dd>{@link org.xmind.core.ITopic}</dd>
 * <dt>Attributes</dt>
 * <dd>
 * <dl>
 * <dt>type</dt>
 * <dd>(String) {@link org.xmind.core.ITopic#getType()}</dd>
 * <dt>title</dt>
 * <dd>(String) {@link org.xmind.core.ITopic#getTitleText()}</dd>
 * <dt>structureClass</dt>
 * <dd>(String) {@link org.xmind.core.ITopic#getStructureClass()}</dd>
 * <dt>folded</dt>
 * <dd>(boolean) {@link org.xmind.core.ITopic#isFolded()}</dd>
 * <dt>hyperlink</dt>
 * <dd>(String) {@link org.xmind.core.ITopic#getHyperlink()}</dd>
 * </dl>
 * </dd>
 * <dt>Elements</dt>
 * <dd>
 * <dl>
 * <dt>image</dt>
 * <dd>A singleton list of {@link org.xmind.core.ITopic#getImage()}</dd>
 * <dt>marker</dt>
 * <dd>(Set) {@link org.xmind.core.ITopic#getMarkerRefs()}</dd>
 * <dt>label</dt>
 * <dd>(Set) {@link org.xmind.core.ITopic#getLabels()}</dd>
 * <dt>extension</dt>
 * <dd>(List) {@link org.xmind.core.ITopic#getExtension(String)}</dd>
 * <dt>topic</dt>
 * <dd>(List) {@link org.xmind.core.ITopic#getAllChildren()}</dd>
 * </dl>
 * </dd>
 * </dl>
 * </dd>
 * 
 * <dt>image</dt>
 * <dd>
 * <dl>
 * <dt>Type</dt>
 * <dd>{@link org.xmind.core.IImage}</dd>
 * <dt>Attributes</dt>
 * <dd>
 * <dl>
 * <dt>source</dt>
 * <dd>(String) {@link org.xmind.core.IImage#getSource()}</dd>
 * </dl>
 * </dd>
 * </dl>
 * </dd>
 * 
 * <dt>marker</dt>
 * <dd>
 * <dl>
 * <dt>Type</dt>
 * <dd>{@link org.xmind.core.marker.IMarkerRef}</dd>
 * <dt>Attributes</dt>
 * <dd>
 * <dl>
 * <dt>id</dt>
 * <dd>(String) {@link org.xmind.core.marker.IMarkerRef#getMarkerId()}</dd>
 * <dt>name</dt>
 * <dd>(String) {@link org.xmind.core.marker.IMarkerRef#getDescription()}</dd>
 * <dt>groupId</dt>
 * <dd>(String) The marker group id retrieved by
 * <code>element.getMarker().getParent().getId()</code></dd>
 * </dl>
 * </dd>
 * </dl>
 * </dd>
 * 
 * <dt>label</dt>
 * <dd>
 * <dl>
 * <dt>Type</dt>
 * <dd>String</dd>
 * <dt>Text Content (<code>text()</code>)</dt>
 * <dd>The string value of this label</dd>
 * </dl>
 * </dd>
 * 
 * <dt>extension</dt>
 * <dd>
 * <dl>
 * <dt>Type</dt>
 * <dd>{@link org.xmind.core.ITopicExtension}</dd>
 * <dt>Attributes</dt>
 * <dd>
 * <dl>
 * <dt>provider</dt>
 * <dd>(String) {@link org.xmind.core.ITopicExtension#getProviderName()}</dd>
 * </dl>
 * </dd>
 * <dt>Elements</dt>
 * <dd>
 * <dl>
 * <dt>content</dt>
 * <dd>A singleton collection of
 * {@link org.xmind.core.ITopicExtension#getContent()}</dd>
 * <dt>resource</dt>
 * <dd>(List) {@link org.xmind.core.ITopicExtension#getResourceRefs()}</dd>
 * </dl>
 * </dd>
 * </dl>
 * </dd>
 * 
 * <dt>resource</dt>
 * <dd>
 * <dl>
 * <dt>Type</dt>
 * <dd>{@link org.xmind.core.IResourceRef}</dd>
 * <dt>Attributes</dt>
 * <dd>
 * <dl>
 * <dt>type</dt>
 * <dd>(String) {@link org.xmind.core.IResourceRef#getType()}</dd>
 * <dt>id</dt>
 * <dd>(String) {@link org.xmind.core.IResourceRef#getResourceId()}</dd>
 * </dl>
 * </dd>
 * </dl>
 * </dd>
 * 
 * <dt>content</dt>
 * <dd>
 * <dl>
 * <dt>Type</dt>
 * <dd>{@link org.xmind.core.ITopicExtensionElement}</dd>
 * <dt>Attributes</dt>
 * <dd>(String)
 * {@link org.xmind.core.ITopicExtensionElement#getAttribute(String)}</dd>
 * <dt>Elements</dt>
 * <dd>(List) {@link org.xmind.core.ITopicExtensionElement#getChildren(String)}
 * </dd>
 * <dt>Text Content</dt>
 * <dd>(String) {@link org.xmind.core.ITopicExtensionElement#getTextContent()}
 * </dd>
 * </dl>
 * </dd>
 * </dl>
 * 
 * @author Frank Shaka
 *
 */
public class TopicPropertyTester extends PropertyTester {

    private static final String P_EVAL = "eval"; //$NON-NLS-1$

    private static final String P_TYPE = "type"; //$NON-NLS-1$

    private static final String P_TITLE = "title"; //$NON-NLS-1$

    private static final String P_STRUCTURE_CLASS = "structureClass"; //$NON-NLS-1$

    private static final String P_FOLDED = "folded"; //$NON-NLS-1$

    private static final String P_HYPERLINK = "hyperlink"; //$NON-NLS-1$

    private static final String P_IMAGE_SOURCE = "imageSource"; //$NON-NLS-1$

    public boolean test(Object receiver, String property, Object[] args,
            Object expectedValue) {
        Assert.isLegal(receiver instanceof ITopic,
                "Receiver is not an ITopic object: " + receiver); //$NON-NLS-1$

        ITopic topic = (ITopic) receiver;

        if (P_TYPE.equals(property)) {
            return evaluates(topic, propEvalExp("@type", expectedValue)); //$NON-NLS-1$
        } else if (P_HYPERLINK.equals(property)) {
            return evaluates(topic, propEvalExp("@hyperlink", expectedValue)); //$NON-NLS-1$
        } else if (P_TITLE.equals(property)) {
            return evaluates(topic, propEvalExp("@title", expectedValue)); //$NON-NLS-1$
        } else if (P_STRUCTURE_CLASS.equals(property)) {
            return evaluates(topic,
                    propEvalExp("@structureClass", expectedValue)); //$NON-NLS-1$
        } else if (P_FOLDED.equals(property)) {
            return evaluates(topic, "@folded"); //$NON-NLS-1$
        } else if (P_IMAGE_SOURCE.equals(property)) {
            return evaluates(topic,
                    propEvalExp("image/@source", expectedValue)); //$NON-NLS-1$
        } else if (P_EVAL.equals(property)) {
            if (!(expectedValue instanceof String))
                return false;
            return evaluates(topic, (String) expectedValue);
        }

        throw new IllegalArgumentException(
                "Unrecognized property: " + property); //$NON-NLS-1$
    }

    private static String propEvalExp(String propertyPath,
            Object expectedValue) {
        String value = expectedValue == null ? "" : expectedValue.toString(); //$NON-NLS-1$
        if (value.startsWith("^")) //$NON-NLS-1$
            return String.format("matches(%s,'%s')", propertyPath, value); //$NON-NLS-1$
        return String.format("%s='%s'", propertyPath, value); //$NON-NLS-1$
    }

    private static boolean evaluates(ITopic topic, String expression) {
        Evaluator evaluator = new Evaluator(expression, new CoreAxisProvider());
        List<Object> sequence = evaluator.evaluate(topic);
        if (sequence.isEmpty())
            return false;
        Object result = sequence.get(0);
        if (result instanceof Boolean)
            return ((Boolean) result).booleanValue();
        return result != null;
    }

}
