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

import java.util.*;
import java.util.concurrent.*;

import org.junit.*;
import org.xmind.core.*;
import org.xmind.core.event.*;

/**
 * @author Frank Shaka
 *
 */
public class CommentTestCase {

	@Test
	public void testCommentAutoManagement() {
		IWorkbook workbook = Core.getWorkbookBuilder().createWorkbook();
		ICommentManager commentManager = workbook.getCommentManager();

		ISheet sheet1 = workbook.getPrimarySheet();
		ITopic topic1 = sheet1.getRootTopic();

		ISheet sheet2 = workbook.createSheet();
		ITopic topic2 = sheet2.getRootTopic();

		assertFalse(topic1.isOrphan());
		assertTrue(topic2.isOrphan());

		assertSet(commentManager.getAllComments());
		assertTrue(commentManager.isEmpty());
		assertSet(commentManager.getComments(topic1.getId()));
		assertFalse(commentManager.hasComments(topic1.getId()));
		assertSet(commentManager.getComments(topic2.getId()));
		assertFalse(commentManager.hasComments(topic2.getId()));

		IComment comment1 = commentManager.createComment(author(), time(), topic1.getId());
		assertSet(commentManager.getAllComments());
		assertTrue(commentManager.isEmpty());
		assertSet(commentManager.getComments(topic1.getId()));
		assertFalse(commentManager.hasComments(topic1.getId()));
		assertSet(commentManager.getComments(topic2.getId()));
		assertFalse(commentManager.hasComments(topic2.getId()));

		commentManager.addComment(comment1);
		assertSet(commentManager.getAllComments(), comment1);
		assertFalse(commentManager.isEmpty());
		assertSet(commentManager.getComments(topic1.getId()), comment1);
		assertTrue(commentManager.hasComments(topic1.getId()));
		assertSet(commentManager.getComments(topic2.getId()));
		assertFalse(commentManager.hasComments(topic2.getId()));

		IComment comment2 = commentManager.createComment(author(), time(), topic2.getId());
		assertSet(commentManager.getAllComments(), comment1);
		assertFalse(commentManager.isEmpty());
		assertSet(commentManager.getComments(topic1.getId()), comment1);
		assertTrue(commentManager.hasComments(topic1.getId()));
		assertSet(commentManager.getComments(topic2.getId()));
		assertFalse(commentManager.hasComments(topic2.getId()));

		commentManager.addComment(comment2);
		assertSet(commentManager.getAllComments(), comment1);
		assertFalse(commentManager.isEmpty());
		assertSet(commentManager.getComments(topic1.getId()), comment1);
		assertTrue(commentManager.hasComments(topic1.getId()));
		assertSet(commentManager.getComments(topic2.getId()));
		assertFalse(commentManager.hasComments(topic2.getId()));

		workbook.addSheet(sheet2);

		assertFalse(topic2.isOrphan());
		assertSet(commentManager.getAllComments(), comment1, comment2);
		assertFalse(commentManager.isEmpty());
		assertSet(commentManager.getComments(topic1.getId()), comment1);
		assertTrue(commentManager.hasComments(topic1.getId()));
		assertSet(commentManager.getComments(topic2.getId()), comment2);
		assertTrue(commentManager.hasComments(topic2.getId()));

		sheet2.replaceRootTopic(workbook.createTopic());

		assertTrue(topic2.isOrphan());
		assertSet(commentManager.getAllComments(), comment1);
		assertFalse(commentManager.isEmpty());
		assertSet(commentManager.getComments(topic1.getId()), comment1);
		assertTrue(commentManager.hasComments(topic1.getId()));
		assertSet(commentManager.getComments(topic2.getId()));
		assertFalse(commentManager.hasComments(topic2.getId()));

		sheet2.getRootTopic().add(topic2);
		assertFalse(topic2.isOrphan());
		assertSet(commentManager.getAllComments(), comment1, comment2);
		assertFalse(commentManager.isEmpty());
		assertSet(commentManager.getComments(topic1.getId()), comment1);
		assertTrue(commentManager.hasComments(topic1.getId()));
		assertSet(commentManager.getComments(topic2.getId()), comment2);
		assertTrue(commentManager.hasComments(topic2.getId()));

		sheet2.getRootTopic().remove(topic2);
		assertSet(commentManager.getAllComments(), comment1);
		assertFalse(commentManager.isEmpty());
		assertSet(commentManager.getComments(topic1.getId()), comment1);
		assertTrue(commentManager.hasComments(topic1.getId()));
		assertSet(commentManager.getComments(topic2.getId()));
		assertFalse(commentManager.hasComments(topic2.getId()));

		commentManager.removeComment(comment2);
		assertSet(commentManager.getAllComments(), comment1);
		assertFalse(commentManager.isEmpty());
		assertSet(commentManager.getComments(topic1.getId()), comment1);
		assertTrue(commentManager.hasComments(topic1.getId()));
		assertSet(commentManager.getComments(topic2.getId()));
		assertFalse(commentManager.hasComments(topic2.getId()));

		sheet2.getRootTopic().add(topic2);
		assertSet(commentManager.getAllComments(), comment1);
		assertFalse(commentManager.isEmpty());
		assertSet(commentManager.getComments(topic1.getId()), comment1);
		assertTrue(commentManager.hasComments(topic1.getId()));
		assertSet(commentManager.getComments(topic2.getId()));
		assertFalse(commentManager.hasComments(topic2.getId()));

	}

	private static void assertSet(Set<?> set, Object... expectedObjects) {
		if (expectedObjects.length > 0) {
			assertFalse(set.isEmpty());
		} else {
			assertTrue(set.isEmpty());
		}
		assertEquals(expectedObjects.length, set.size());
		for (Object o : expectedObjects) {
			assertTrue(set.contains(o));
		}
	}

	@Test
	public void testCommentEvent() {
		IWorkbook workbook = Core.getWorkbookBuilder().createWorkbook();
		ICommentManager commentManager = workbook.getCommentManager();

		ITopic parent = workbook.getPrimarySheet().getRootTopic();
		ITopic topic = workbook.createTopic();

		assertTrue(topic instanceof ICoreEventSource);

		final Queue<CoreEvent> eventQueue = new LinkedBlockingQueue<CoreEvent>();
		assertTrue(eventQueue.isEmpty());
		CoreEvent event;

		ICoreEventListener listener = new ICoreEventListener() {
			@Override
			public void handleCoreEvent(CoreEvent event) {
				eventQueue.add(event);
			}
		};
		((ICoreEventSource) topic).registerCoreEventListener(Core.CommentAdd, listener);
		((ICoreEventSource) topic).registerCoreEventListener(Core.CommentRemove, listener);

		assertTrue(eventQueue.isEmpty());

		parent.add(topic);
		assertFalse(topic.isOrphan());
		assertTrue(eventQueue.isEmpty());

		IComment comment = commentManager.createComment(author(), time(), topic.getId());
		assertTrue(eventQueue.isEmpty());

		/// add/remove comment when topic is not orphan
		commentManager.addComment(comment);
		assertFalse(eventQueue.isEmpty());
		event = eventQueue.remove();
		assertEquals(Core.CommentAdd, event.getType());
		assertEquals(topic, event.getSource());
		assertEquals(comment, event.getTarget());
		assertTrue(eventQueue.isEmpty());

		commentManager.removeComment(comment);
		assertFalse(eventQueue.isEmpty());
		event = eventQueue.remove();
		assertEquals(Core.CommentRemove, event.getType());
		assertEquals(topic, event.getSource());
		assertEquals(comment, event.getTarget());
		assertTrue(eventQueue.isEmpty());

		/// add/remove topic when comment is added
		commentManager.addComment(comment);
		assertFalse(eventQueue.isEmpty());
		event = eventQueue.remove();
		assertEquals(Core.CommentAdd, event.getType());
		assertEquals(topic, event.getSource());
		assertEquals(comment, event.getTarget());
		assertTrue(eventQueue.isEmpty());

		parent.remove(topic);
		assertTrue(topic.isOrphan());
		assertFalse(eventQueue.isEmpty());
		event = eventQueue.remove();
		assertEquals(Core.CommentRemove, event.getType());
		assertEquals(topic, event.getSource());
		assertEquals(comment, event.getTarget());
		assertTrue(eventQueue.isEmpty());

		parent.add(topic);
		assertFalse(topic.isOrphan());
		assertFalse(eventQueue.isEmpty());
		event = eventQueue.remove();
		assertEquals(Core.CommentAdd, event.getType());
		assertEquals(topic, event.getSource());
		assertEquals(comment, event.getTarget());
		assertTrue(eventQueue.isEmpty());

		commentManager.removeComment(comment);
		assertFalse(eventQueue.isEmpty());
		event = eventQueue.remove();
		assertEquals(Core.CommentRemove, event.getType());
		assertEquals(topic, event.getSource());
		assertEquals(comment, event.getTarget());
		assertTrue(eventQueue.isEmpty());

		/// add/remove comment when topic is orphan
		parent.remove(topic);
		assertTrue(topic.isOrphan());
		assertTrue(eventQueue.isEmpty());

		commentManager.addComment(comment);
		assertTrue(eventQueue.isEmpty());

		parent.add(topic);
		assertFalse(topic.isOrphan());
		assertFalse(eventQueue.isEmpty());
		event = eventQueue.remove();
		assertEquals(Core.CommentAdd, event.getType());
		assertEquals(topic, event.getSource());
		assertEquals(comment, event.getTarget());
		assertTrue(eventQueue.isEmpty());

		parent.remove(topic);
		assertTrue(topic.isOrphan());
		assertFalse(eventQueue.isEmpty());
		event = eventQueue.remove();
		assertEquals(Core.CommentRemove, event.getType());
		assertEquals(topic, event.getSource());
		assertEquals(comment, event.getTarget());
		assertTrue(eventQueue.isEmpty());

		commentManager.removeComment(comment);
		assertTrue(eventQueue.isEmpty());

		parent.add(topic);
		assertTrue(eventQueue.isEmpty());

		parent.remove(topic);
		assertTrue(eventQueue.isEmpty());
	}

	private String author() {
		return "foobar";
	}

	private long time() {
		return System.currentTimeMillis();
	}

}
