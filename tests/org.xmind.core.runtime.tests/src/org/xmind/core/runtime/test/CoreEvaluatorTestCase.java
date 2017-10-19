package org.xmind.core.runtime.test;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.xmind.core.Core;
import org.xmind.core.IDeserializer;
import org.xmind.core.ITopic;
import org.xmind.core.IWorkbook;
import org.xmind.core.internal.CoreAxisProvider;
import org.xmind.core.internal.xpath.Evaluator;
import org.xmind.core.internal.xpath.IAxisProvider;
import org.xmind.core.io.BundleResourceInputSource;
import org.xmind.core.io.IInputSource;
import org.xmind.core.marker.IMarkerSheet;

@SuppressWarnings({ "rawtypes" })
public class CoreEvaluatorTestCase {

    private IWorkbook workbook;

    private IAxisProvider axisProvider;

    private List<Object> eval(Object context, String expression) {
        return new Evaluator(expression, axisProvider).evaluate(context);
    }

    @Test
    public void testPrimitiveValues() {
        assertOrderedResultSet(eval(null, ""));
        assertOrderedResultSet(eval(null, "1"), Integer.valueOf(1));
        assertOrderedResultSet(eval(null, "'asdf'"), "asdf");
        assertOrderedResultSet(eval(null, "'as''df'"), "as'df");
        assertOrderedResultSet(eval(null, "1=1"), Boolean.TRUE);
        assertOrderedResultSet(eval(null, "'asdf'='asdf'"), Boolean.TRUE);
        assertOrderedResultSet(eval(null, "1=2"), Boolean.FALSE);
        assertOrderedResultSet(eval(null, "'asdf'='qwer'"), Boolean.FALSE);
    }

    @Test
    public void testGetTitle() {
        ITopic ct = workbook.getPrimarySheet().getRootTopic();
        ITopic mt1 = ct.getChildren(ITopic.ATTACHED).get(0);
        ITopic mt2 = ct.getChildren(ITopic.ATTACHED).get(1);
        ITopic mt3 = ct.getChildren(ITopic.ATTACHED).get(2);

        assertOrderedResultSet(eval(ct, "@title"), "Central Topic");
        assertOrderedResultSet(eval(mt1, "@title"), "Main Topic 1");
        assertOrderedResultSet(eval(mt2, "@title"), "Task");
        assertOrderedResultSet(eval(mt3, "@title"), "Platform Expression Framework.txt");

        assertOrderedResultSet(eval(ct, "@title='Central Topic'"), Boolean.TRUE);
        assertOrderedResultSet(eval(mt1, "@title='Main Topic 1'"), Boolean.TRUE);
    }

    @Test
    public void testGetFolded() {
        ITopic ct = workbook.getPrimarySheet().getRootTopic();
        ITopic mt1 = ct.getChildren(ITopic.ATTACHED).get(0);
        ITopic mt2 = ct.getChildren(ITopic.ATTACHED).get(1);
        ITopic mt3 = ct.getChildren(ITopic.ATTACHED).get(2);

        assertOrderedResultSet(eval(ct, "@folded"), Boolean.FALSE);
        assertOrderedResultSet(eval(mt1, "@folded"), Boolean.FALSE);
        assertOrderedResultSet(eval(mt2, "@folded"), Boolean.FALSE);
        assertOrderedResultSet(eval(mt3, "@folded"), Boolean.TRUE);
    }

    @Test
    public void testGetType() {
        ITopic ct = workbook.getPrimarySheet().getRootTopic();
        ITopic mt1 = ct.getChildren(ITopic.ATTACHED).get(0);
        ITopic mt2 = ct.getChildren(ITopic.ATTACHED).get(1);
        ITopic mt3 = ct.getChildren(ITopic.ATTACHED).get(2);
        ITopic ft1 = ct.getChildren(ITopic.DETACHED).get(0);
        ITopic st1 = ct.getChildren(ITopic.SUMMARY).get(0);

        assertOrderedResultSet(eval(ct, "@type"), ITopic.ROOT);
        assertOrderedResultSet(eval(mt1, "@type"), ITopic.ATTACHED);
        assertOrderedResultSet(eval(mt2, "@type"), ITopic.ATTACHED);
        assertOrderedResultSet(eval(mt3, "@type"), ITopic.ATTACHED);
        assertOrderedResultSet(eval(ft1, "@type"), ITopic.DETACHED);
        assertOrderedResultSet(eval(st1, "@type"), ITopic.SUMMARY);
    }

    @Test
    public void testGetMarkers() {
        ITopic ct = workbook.getPrimarySheet().getRootTopic();
        ITopic mt1 = ct.getChildren(ITopic.ATTACHED).get(0);
        ITopic mt2 = ct.getChildren(ITopic.ATTACHED).get(1);
        ITopic mt3 = ct.getChildren(ITopic.ATTACHED).get(2);

        assertUnorderedResultSet(eval(ct, "marker"), ct.getMarkerRefs().toArray());
        assertUnorderedResultSet(eval(mt1, "marker"), mt1.getMarkerRefs().toArray());
        assertUnorderedResultSet(eval(mt2, "marker"), mt2.getMarkerRefs().toArray());
        assertUnorderedResultSet(eval(mt3, "marker"), mt3.getMarkerRefs().toArray());
    }

    @Test
    public void testGetMarkerIds() {
        ITopic ct = workbook.getPrimarySheet().getRootTopic();
        ITopic mt1 = ct.getChildren(ITopic.ATTACHED).get(0);
        ITopic mt2 = ct.getChildren(ITopic.ATTACHED).get(1);
        ITopic mt3 = ct.getChildren(ITopic.ATTACHED).get(2);

        assertUnorderedResultSet(eval(ct, "marker/@id"));
        assertUnorderedResultSet(eval(mt1, "marker/@id"), "priority-2", "smiley-laugh");
        assertUnorderedResultSet(eval(mt2, "marker/@id"), "task-3oct");
        assertUnorderedResultSet(eval(mt3, "marker/@id"));

        assertOrderedResultSet(eval(mt2, "marker/@id='task-3oct'"), Boolean.TRUE);
    }

    @Test
    public void testGetCount() {
        ITopic ct = workbook.getPrimarySheet().getRootTopic();
        ITopic mt1 = ct.getChildren(ITopic.ATTACHED).get(0);
        ITopic mt2 = ct.getChildren(ITopic.ATTACHED).get(1);
        ITopic mt3 = ct.getChildren(ITopic.ATTACHED).get(2);

        assertOrderedResultSet(eval(ct, "count(marker)"), Integer.valueOf(0));
        assertOrderedResultSet(eval(mt1, "count(marker)"), Integer.valueOf(2));
        assertOrderedResultSet(eval(mt2, "count(marker)"), Integer.valueOf(1));
        assertOrderedResultSet(eval(mt3, "count(marker)"), Integer.valueOf(0));

        assertOrderedResultSet(eval(ct, "count(marker)=0"), Boolean.TRUE);
        assertOrderedResultSet(eval(mt1, "count(marker)=2"), Boolean.TRUE);
    }

    @Test
    public void testGetChildTopics() {
        ITopic ct = workbook.getPrimarySheet().getRootTopic();

        assertOrderedResultSet(eval(ct, "topic"), ct.getAllChildren().toArray());

        assertOrderedResultSet(eval(ct, "topic[1]"), ct.getAllChildren().get(0));
        assertOrderedResultSet(eval(ct, "topic[@type='attached']"), ct.getChildren(ITopic.ATTACHED).toArray());
        assertOrderedResultSet(eval(ct, "topic[@type='detached']"), ct.getChildren(ITopic.DETACHED).toArray());
        assertOrderedResultSet(eval(ct, "topic[@type='summary']"), ct.getChildren(ITopic.SUMMARY).toArray());

        assertOrderedResultSet(eval(ct, "topic[matches(@type,'(at|de)tached')]"),
                concat(ct.getChildren(ITopic.ATTACHED), ct.getChildren(ITopic.DETACHED)).toArray());
    }

    @Test
    public void testGetParent() {
        ITopic ct = workbook.getPrimarySheet().getRootTopic();
        ITopic mt1 = ct.getChildren(ITopic.ATTACHED).get(0);
        ITopic mt2 = ct.getChildren(ITopic.ATTACHED).get(1);

        assertOrderedResultSet(eval(ct, "../"), ct.getOwnedSheet());
        assertOrderedResultSet(eval(mt1, "../"), ct);
        assertOrderedResultSet(eval(mt1, "../@type='root'"), Boolean.TRUE);
        assertOrderedResultSet(eval(mt2, "../../"), ct.getOwnedSheet());
    }

    @Test
    public void testCommonTopicTests() {
        ITopic ct = workbook.getPrimarySheet().getRootTopic();
        ITopic mt1 = ct.getChildren(ITopic.ATTACHED).get(0);
        ITopic mt2 = ct.getChildren(ITopic.ATTACHED).get(1);
        ITopic mt3 = ct.getChildren(ITopic.ATTACHED).get(2);
        ITopic ft1 = ct.getChildren(ITopic.DETACHED).get(0);
        ITopic st1 = ct.getChildren(ITopic.SUMMARY).get(0);

        // topic has a specific title
        String test1 = "@title='Task'";
        assertOrderedResultSet(eval(ct, test1), Boolean.FALSE);
        assertOrderedResultSet(eval(mt1, test1), Boolean.FALSE);
        assertOrderedResultSet(eval(mt2, test1), Boolean.TRUE);
        assertOrderedResultSet(eval(mt3, test1), Boolean.FALSE);
        assertOrderedResultSet(eval(ft1, test1), Boolean.FALSE);
        assertOrderedResultSet(eval(st1, test1), Boolean.FALSE);

        // topic is the root topic
        String test2 = "@type='root'";
        assertOrderedResultSet(eval(ct, test2), Boolean.TRUE);
        assertOrderedResultSet(eval(mt1, test2), Boolean.FALSE);
        assertOrderedResultSet(eval(mt2, test2), Boolean.FALSE);
        assertOrderedResultSet(eval(mt3, test2), Boolean.FALSE);
        assertOrderedResultSet(eval(ft1, test2), Boolean.FALSE);
        assertOrderedResultSet(eval(st1, test2), Boolean.FALSE);

        // topic is a summary topic
        String test3 = "@type='summary'";
        assertOrderedResultSet(eval(ct, test3), Boolean.FALSE);
        assertOrderedResultSet(eval(mt1, test3), Boolean.FALSE);
        assertOrderedResultSet(eval(mt2, test3), Boolean.FALSE);
        assertOrderedResultSet(eval(mt3, test3), Boolean.FALSE);
        assertOrderedResultSet(eval(ft1, test3), Boolean.FALSE);
        assertOrderedResultSet(eval(st1, test3), Boolean.TRUE);

        // topic is an attachment
        String test4 = "matches(@hyperlink,'^xap\\:.*')";
        assertOrderedResultSet(eval(ct, test4), Boolean.FALSE);
        assertOrderedResultSet(eval(mt1, test4), Boolean.FALSE);
        assertOrderedResultSet(eval(mt2, test4), Boolean.FALSE);
        assertOrderedResultSet(eval(mt3, test4), Boolean.TRUE);
        assertOrderedResultSet(eval(ft1, test4), Boolean.FALSE);
        assertOrderedResultSet(eval(st1, test4), Boolean.FALSE);

        // topic has markers
        String test5 = "count(marker)>0";
        assertOrderedResultSet(eval(ct, test5), Boolean.FALSE);
        assertOrderedResultSet(eval(mt1, test5), Boolean.TRUE);
        assertOrderedResultSet(eval(mt2, test5), Boolean.TRUE);
        assertOrderedResultSet(eval(mt3, test5), Boolean.FALSE);
        assertOrderedResultSet(eval(ft1, test5), Boolean.FALSE);
        assertOrderedResultSet(eval(st1, test5), Boolean.FALSE);

        // topic has image
        String test6 = "count(image[@source])>0";
        assertOrderedResultSet(eval(ct, test6), Boolean.FALSE);
        assertOrderedResultSet(eval(mt1, test6), Boolean.FALSE);
        assertOrderedResultSet(eval(mt2, test6), Boolean.TRUE);
        assertOrderedResultSet(eval(mt3, test6), Boolean.FALSE);
        assertOrderedResultSet(eval(ft1, test6), Boolean.FALSE);
        assertOrderedResultSet(eval(st1, test6), Boolean.FALSE);

        // topic has markers belonging to a specific group
        String test7 = "count(marker[@groupId='priorityMarkers'])>0";
        assertOrderedResultSet(eval(ct, test7), Boolean.FALSE);
        assertOrderedResultSet(eval(mt1, test7), Boolean.TRUE);
        assertOrderedResultSet(eval(mt2, test7), Boolean.FALSE);
        assertOrderedResultSet(eval(mt3, test7), Boolean.FALSE);
        assertOrderedResultSet(eval(ft1, test7), Boolean.FALSE);
        assertOrderedResultSet(eval(st1, test7), Boolean.FALSE);

    }

    private static List<Object> concat(Collection<?> c1, Collection<?> c2) {
        List<Object> results = new ArrayList<Object>(c1.size() + c2.size());
        results.addAll(c1);
        results.addAll(c2);
        return results;
    }

    @Before
    public void setUp() throws Exception {
        IMarkerSheet globalMarkers;
        InputStream markerSheetStream = new URL("platform:/plugin/org.xmind.core.runtime.tests/samples/markers.xml")
                .openStream();
        try {
            globalMarkers = Core.getMarkerSheetBuilder().loadFromStream(markerSheetStream, null);
        } finally {
            markerSheetStream.close();
        }
        IInputSource source = new BundleResourceInputSource("org.xmind.core.runtime.tests", "/samples/sample1.xmind"); //$NON-NLS-2$
        IDeserializer deserializer = Core.getWorkbookBuilder().newDeserializer();
        deserializer.setInputSource(source);
        deserializer.deserialize(null);
        workbook = deserializer.getWorkbook();
        workbook.getMarkerSheet().setParentSheet(globalMarkers);

        axisProvider = new CoreAxisProvider();
    }

    @After
    public void tearDown() {
        axisProvider = null;
        workbook = null;
    }

    private static void assertUnorderedResultSet(List<Object> actual, Object... expected) {
        Set<Object> expectedSet = new HashSet<Object>(asList(expected));
        assertEquals(expectedSet.size(), actual.size());
        assertTrue(expectedSet.containsAll(actual));
        assertTrue(actual.containsAll(expectedSet));
    }

    private static void assertOrderedResultSet(List<Object> actual, Object... expected) {
        List<Object> expectedList = asList(expected);
        assertEquals(expectedList.size(), actual.size());
        Iterator it1 = expectedList.iterator();
        Iterator it2 = actual.iterator();
        while (it1.hasNext() && it2.hasNext()) {
            assertEquals(it1.next(), it2.next());
        }
        assertFalse(it1.hasNext());
        assertFalse(it2.hasNext());
    }

}
