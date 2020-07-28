/* ******************************************************************************
 * Copyright (c) 2006-2016 XMind Ltd. and others.
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
/**
 * 
 */
package org.xmind.core.tests;

import static org.junit.Assert.*;

import org.junit.*;
import org.xmind.core.*;
import org.xmind.core.util.*;

/**
 * @author Frank Shaka
 *
 */
public class TopicIteratorTestCase {

	@Test
	public void testTopicIterator() {
		IWorkbook workbook = Core.getWorkbookBuilder().createWorkbook();
		ITopic root = workbook.getPrimarySheet().getRootTopic();

		ITopic t1 = workbook.createTopic();
		root.add(t1);
		ITopic t2 = workbook.createTopic();
		root.add(t2);

		ITopic t1s1 = workbook.createTopic();
		t1.add(t1s1);
		ITopic t1s2 = workbook.createTopic();
		t1.add(t1s2);

		ITopic t2s1 = workbook.createTopic();
		t2.add(t2s1);
		ITopic t2s2 = workbook.createTopic();
		t2.add(t2s2);

		TopicIterator it = new TopicIterator(root);
		assertEquals(root, it.next());
		assertEquals(t1, it.next());
		assertEquals(t1s1, it.next());
		assertEquals(t1s2, it.next());
		assertEquals(t2, it.next());
		assertEquals(t2s1, it.next());
		assertEquals(t2s2, it.next());
		assertFalse(it.hasNext());

		it = new TopicIterator(root, TopicIterator.REVERSED);
		assertEquals(t2s2, it.next());
		assertEquals(t2s1, it.next());
		assertEquals(t2, it.next());
		assertEquals(t1s2, it.next());
		assertEquals(t1s1, it.next());
		assertEquals(t1, it.next());
		assertEquals(root, it.next());
		assertFalse(it.hasNext());

	}

}
