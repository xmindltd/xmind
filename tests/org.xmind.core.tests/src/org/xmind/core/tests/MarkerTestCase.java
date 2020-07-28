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
import static org.xmind.core.internal.tests.TestUtil.*;

import java.io.*;
import java.util.*;

import org.junit.*;
import org.xmind.core.*;
import org.xmind.core.internal.dom.*;
import org.xmind.core.internal.tests.*;
import org.xmind.core.internal.zip.*;
import org.xmind.core.io.*;
import org.xmind.core.marker.*;
import org.xmind.core.util.*;

/**
 * @author Frank Shaka
 *
 */
public class MarkerTestCase extends WorkbookTestCaseBase {

    /**
     * 
     */
    private static final String MARKER_PATH_PREFIX = ArchiveConstants.PATH_MARKERS;

    @Test
    public void testCreateMarkerInWorkbookUsingGivenResourcePath() throws Exception {
        IStorage storage = new ByteArrayStorage();
        IWorkbook workbook = createWorkbook(storage);
        IMarkerSheet markerSheet = workbook.getMarkerSheet();

        IMarker marker1 = markerSheet.createMarker(null);
        assertEquals("", marker1.getResourcePath());
        assertNotNull(marker1.getId());
        assertNull(marker1.getResource());

        byte[] resourceContent = randString().getBytes();
        String resourcePath = markerSheet.allocateMarkerResource(new ByteArrayInputStream(resourceContent),
                randString() + ".png");
        assertArrayEquals(resourceContent,
                readBytes(storage.getInputSource().openEntryStream(MARKER_PATH_PREFIX + resourcePath)));

        IMarker marker2 = markerSheet.createMarker(resourcePath);
        assertEquals(resourcePath, marker2.getResourcePath());
        assertNotNull(marker2.getId());
        assertNotEquals(marker1.getId(), marker2.getId());
        assertNotNull(marker2.getResource());
        assertArrayEquals(resourceContent, readBytes(marker2.getResource().openInputStream()));
    }

    @Test
    public void testCreateMarkerInWorkbookUsingGivenMarkerId() throws Exception {
        IStorage storage = new ByteArrayStorage();
        IWorkbook workbook = createWorkbook(storage);
        IMarkerSheet markerSheet = workbook.getMarkerSheet();

        String marker1Id = randString();
        IMarker marker1 = markerSheet.createMarkerById(marker1Id, null);
        assertEquals(marker1Id, marker1.getId());
        assertEquals("", marker1.getResourcePath());
        assertNull(marker1.getResource());

        byte[] resourceContent = randString().getBytes();
        String marker2Id = randString();
        String resourcePath = markerSheet.allocateMarkerResource(new ByteArrayInputStream(resourceContent),
                randString() + ".png");
        assertArrayEquals(resourceContent,
                readBytes(storage.getInputSource().openEntryStream(MARKER_PATH_PREFIX + resourcePath)));

        IMarker marker2 = markerSheet.createMarkerById(marker2Id, resourcePath);
        assertEquals(marker2Id, marker2.getId());
        assertEquals(resourcePath, marker2.getResourcePath());
        assertNotNull(marker2.getResource());
        assertArrayEquals(resourceContent, readBytes(marker2.getResource().openInputStream()));
    }

    @Test
    public void testCreateCustomMarkerUsingGivenResourcePath() throws Exception {
        IStorage storage = new ByteArrayStorage();
        IMarkerSheet markerSheet = Core.getMarkerSheetBuilder().createMarkerSheet(
                new MarkerResourceProvider(storage.getInputSource(), storage.getOutputTarget(), false));

        IMarker marker1 = markerSheet.createMarker(null);
        assertEquals("", marker1.getResourcePath());
        assertNotNull(marker1.getId());
        assertNull(marker1.getResource());

        byte[] resourceContent = randString().getBytes();
        String resourcePath = markerSheet.allocateMarkerResource(new ByteArrayInputStream(resourceContent),
                randString() + ".png");
        assertArrayEquals(resourceContent, readBytes(storage.getInputSource().openEntryStream(resourcePath)));

        IMarker marker2 = markerSheet.createMarker(resourcePath);
        assertEquals(resourcePath, marker2.getResourcePath());
        assertNotNull(marker2.getId());
        assertNotEquals(marker1.getId(), marker2.getId());
        assertNotNull(marker2.getResource());
        assertArrayEquals(resourceContent, readBytes(marker2.getResource().openInputStream()));
    }

    @Test
    public void testCreateCustomMarkerUsingGivenMarkerId() throws Exception {
        IStorage storage = new ByteArrayStorage();
        IMarkerSheet markerSheet = Core.getMarkerSheetBuilder().createMarkerSheet(
                new MarkerResourceProvider(storage.getInputSource(), storage.getOutputTarget(), false));

        String marker1Id = randString();
        IMarker marker1 = markerSheet.createMarkerById(marker1Id, null);
        assertEquals(marker1Id, marker1.getId());
        assertEquals("", marker1.getResourcePath());
        assertNull(marker1.getResource());

        byte[] resourceContent = randString().getBytes();
        String marker2Id = randString();
        String resourcePath = markerSheet.allocateMarkerResource(new ByteArrayInputStream(resourceContent),
                randString() + ".png");
        assertArrayEquals(resourceContent, readBytes(storage.getInputSource().openEntryStream(resourcePath)));

        IMarker marker2 = markerSheet.createMarkerById(marker2Id, resourcePath);
        assertEquals(marker2Id, marker2.getId());
        assertEquals(resourcePath, marker2.getResourcePath());
        assertNotNull(marker2.getResource());
        assertArrayEquals(resourceContent, readBytes(marker2.getResource().openInputStream()));
    }

    @Test
    public void testCloneMarkerFromCustomMarkerSheetToWorkbookMarkerSheet() throws Exception {
        IStorage storage1 = new ByteArrayStorage();
        IMarkerSheet markerSheet1 = Core.getMarkerSheetBuilder().createMarkerSheet(
                new MarkerResourceProvider(storage1.getInputSource(), storage1.getOutputTarget(), false));

        IStorage storage2 = new ByteArrayStorage();
        IWorkbook workbook2 = createWorkbook(storage2);
        IMarkerSheet markerSheet2 = workbook2.getMarkerSheet();

        byte[] resourceContent = randString().getBytes();
        String resourcePath1 = markerSheet1.allocateMarkerResource(new ByteArrayInputStream(resourceContent),
                randString() + ".png");
        IMarker marker1 = markerSheet1.createMarker(resourcePath1);
        assertNull(marker1.getParent());
        IMarkerGroup group1 = markerSheet1.createMarkerGroup(true);
        assertNull(group1.getParent());

        group1.addMarker(marker1);
        assertEquals(group1, marker1.getParent());
        markerSheet1.addMarkerGroup(group1);
        assertEquals(markerSheet1, group1.getParent());

        IMarker marker2 = (IMarker) new CloneHandler().withMarkerSheets(markerSheet1, markerSheet2)
                .cloneObject(marker1);
        assertNotNull(marker2);
        assertNotEquals(marker1, marker2);
        assertEquals(marker1.getId(), marker2.getId());
        assertArrayEquals(resourceContent, readBytes(marker2.getResource().openInputStream()));

        IMarkerGroup group2 = marker2.getParent();
        assertNotNull(group2);
        assertNotEquals(group1, group2);
        assertEquals(group1.getId(), group2.getId());
        assertEquals(markerSheet2, group2.getParent());

        IMarker marker3 = (IMarker) new CloneHandler().withMarkerSheets(markerSheet1, markerSheet2)
                .cloneObject(marker1);
        assertEquals(marker2, marker3);
    }

    @Test
    public void testCloneMarkerFromWorkbookMarkerSheetToWorkbookMarkerSheet() throws Exception {
        IStorage storage1 = new ByteArrayStorage();
        IWorkbook workbook1 = createWorkbook(storage1);
        IMarkerSheet markerSheet1 = workbook1.getMarkerSheet();

        IStorage storage2 = new ByteArrayStorage();
        IWorkbook workbook2 = createWorkbook(storage2);
        IMarkerSheet markerSheet2 = workbook2.getMarkerSheet();

        byte[] resourceContent = randString().getBytes();
        String resourcePath1 = markerSheet1.allocateMarkerResource(new ByteArrayInputStream(resourceContent),
                randString() + ".png");
        IMarker marker1 = markerSheet1.createMarker(resourcePath1);
        assertNull(marker1.getParent());
        IMarkerGroup group1 = markerSheet1.createMarkerGroup(true);
        assertNull(group1.getParent());

        group1.addMarker(marker1);
        assertEquals(group1, marker1.getParent());
        markerSheet1.addMarkerGroup(group1);
        assertEquals(markerSheet1, group1.getParent());

        IMarker marker2 = (IMarker) new CloneHandler().withMarkerSheets(markerSheet1, markerSheet2)
                .cloneObject(marker1);
        assertNotNull(marker2);
        assertNotEquals(marker1, marker2);
        assertEquals(marker1.getId(), marker2.getId());
        assertArrayEquals(resourceContent, readBytes(marker2.getResource().openInputStream()));

        IMarkerGroup group2 = marker2.getParent();
        assertNotNull(group2);
        assertNotEquals(group1, group2);
        assertEquals(group1.getId(), group2.getId());
        assertEquals(markerSheet2, group2.getParent());

        IMarker marker3 = (IMarker) new CloneHandler().withMarkerSheets(markerSheet1, markerSheet2)
                .cloneObject(marker1);
        assertEquals(marker2, marker3);
    }

    @Test
    public void testCloneOneMarkerFromMultiMarkerGroup() throws Exception {
        IStorage storage1 = new ByteArrayStorage();
        IWorkbook workbook1 = createWorkbook(storage1);
        IMarkerSheet markerSheet1 = workbook1.getMarkerSheet();

        IStorage storage2 = new ByteArrayStorage();
        IWorkbook workbook2 = createWorkbook(storage2);
        IMarkerSheet markerSheet2 = workbook2.getMarkerSheet();

        IMarkerGroup group1 = markerSheet1.createMarkerGroup(true);
        IMarker marker1 = markerSheet1.createMarker(null);
        IMarker marker2 = markerSheet1.createMarker(null);
        group1.addMarker(marker1);
        group1.addMarker(marker2);
        markerSheet1.addMarkerGroup(group1);

        assertEquals(group1, marker1.getParent());
        assertEquals(group1, marker2.getParent());
        assertEquals(markerSheet1, group1.getParent());

        assertEquals(2, group1.getMarkers().size());
        assertTrue(group1.getMarkers().contains(marker1));
        assertTrue(group1.getMarkers().contains(marker2));

        IMarker marker3 = (IMarker) new CloneHandler().withMarkerSheets(markerSheet1, markerSheet2)
                .cloneObject(marker1);
        assertNotNull(marker3);

        IMarkerGroup group2 = marker3.getParent();
        assertNotNull(group2);
        assertNotEquals(group1, group2);
        assertEquals(group1.getId(), group2.getId());

        assertEquals(1, group2.getMarkers().size());
        assertTrue(group2.getMarkers().contains(marker3));

        IMarker marker4 = (IMarker) new CloneHandler().withMarkerSheets(markerSheet1, markerSheet2)
                .cloneObject(marker1);
        assertEquals(marker3, marker4);
        assertEquals(group2, marker4.getParent());

        assertEquals(1, group2.getMarkers().size());
        assertTrue(group2.getMarkers().contains(marker3));

        IMarker marker5 = (IMarker) new CloneHandler().withMarkerSheets(markerSheet1, markerSheet2)
                .cloneObject(marker2);
        assertNotNull(marker5);
        assertNotEquals(marker3, marker5);
        assertNotEquals(marker4, marker5);
        assertEquals(group2, marker5.getParent());

        assertEquals(2, group2.getMarkers().size());
        assertTrue(group2.getMarkers().contains(marker3));
        assertTrue(group2.getMarkers().contains(marker5));

    }

    @Test
    public void testMarkerAutoManagementByReferences() throws Exception {
        IStorage storage = new ByteArrayStorage();
        IWorkbook workbook = createWorkbook(storage);
        IMarkerSheet markerSheet = workbook.getMarkerSheet();

        IMarker marker1 = markerSheet.createMarker(null);
        IMarkerGroup group1 = markerSheet.createMarkerGroup(true);
        group1.addMarker(marker1);
        markerSheet.addMarkerGroup(group1);

        assertEquals(markerSheet, group1.getParent());
        assertEquals(group1, marker1.getParent());

        ITopic topic1 = workbook.getPrimarySheet().getRootTopic();
        topic1.addMarker(marker1.getId());

        assertEquals(markerSheet, group1.getParent());
        assertEquals(group1, marker1.getParent());

        topic1.removeMarker(marker1.getId());

        assertNull(group1.getParent());
        assertNull(marker1.getParent());

        topic1.addMarker(marker1.getId());

        assertEquals(markerSheet, group1.getParent());
        assertEquals(group1, marker1.getParent());

        IMarker marker2 = markerSheet.createMarker(null);
        group1.addMarker(marker2);

        assertEquals(markerSheet, group1.getParent());
        assertEquals(group1, marker1.getParent());
        assertEquals(group1, marker2.getParent());
        assertEquals(2, group1.getMarkers().size());
        assertTrue(group1.getMarkers().contains(marker1));
        assertTrue(group1.getMarkers().contains(marker2));

        assertEquals(markerSheet, group1.getParent());
        assertEquals(group1, marker1.getParent());
        assertEquals(group1, marker2.getParent());
        assertEquals(2, group1.getMarkers().size());
        assertTrue(group1.getMarkers().contains(marker1));
        assertTrue(group1.getMarkers().contains(marker2));

        ITopic topic2 = workbook.createTopic();
        topic1.add(topic2);
        topic2.addMarker(marker2.getId());

        assertEquals(markerSheet, group1.getParent());
        assertEquals(group1, marker1.getParent());
        assertEquals(group1, marker2.getParent());
        assertEquals(2, group1.getMarkers().size());
        assertTrue(group1.getMarkers().contains(marker1));
        assertTrue(group1.getMarkers().contains(marker2));

        topic2.removeMarker(marker2.getId());

        assertEquals(markerSheet, group1.getParent());
        assertEquals(group1, marker1.getParent());
        assertNull(marker2.getParent());
        assertEquals(1, group1.getMarkers().size());
        assertTrue(group1.getMarkers().contains(marker1));
        assertFalse(group1.getMarkers().contains(marker2));

        topic2.addMarker(marker2.getId());

        assertEquals(markerSheet, group1.getParent());
        assertEquals(group1, marker1.getParent());
        assertEquals(group1, marker2.getParent());
        assertEquals(2, group1.getMarkers().size());
        assertTrue(group1.getMarkers().contains(marker1));
        assertTrue(group1.getMarkers().contains(marker2));

    }

    @Test
    public void testMarkerResourceAutoReferencing() throws Exception {
        IStorage storage = new ByteArrayStorage();
        IWorkbook workbook = createWorkbook(storage);
        IMarkerSheet markerSheet = workbook.getMarkerSheet();

        IFileEntry entry;

        byte[] resourceContent = randString().getBytes();
        String resourcePath = markerSheet.allocateMarkerResource(new ByteArrayInputStream(resourceContent),
                randString() + ".png");

        entry = workbook.getManifest().getFileEntry(MARKER_PATH_PREFIX + resourcePath);
        assertNotNull(entry);
        assertEquals(0, entry.getReferenceCount());
        assertFalse(workbook.getManifest().getFileEntries().contains(entry));

        IMarker marker1 = markerSheet.createMarker(resourcePath);
        IMarkerGroup group1 = markerSheet.createMarkerGroup(true);
        group1.addMarker(marker1);
        markerSheet.addMarkerGroup(group1);
        assertEquals(markerSheet, group1.getParent());
        assertEquals(group1, marker1.getParent());

        entry = workbook.getManifest().getFileEntry(MARKER_PATH_PREFIX + resourcePath);
        assertNotNull(entry);
        assertEquals(1, entry.getReferenceCount());
        assertTrue(workbook.getManifest().getFileEntries().contains(entry));

        ITopic topic = workbook.getPrimarySheet().getRootTopic();
        topic.addMarker(marker1.getId());

        assertEquals(markerSheet, group1.getParent());
        assertEquals(group1, marker1.getParent());

        entry = workbook.getManifest().getFileEntry(MARKER_PATH_PREFIX + resourcePath);
        assertNotNull(entry);
        assertEquals(2, entry.getReferenceCount());
        assertTrue(workbook.getManifest().getFileEntries().contains(entry));

        topic.removeMarker(marker1.getId());

        assertNull(group1.getParent());
        assertNull(marker1.getParent());

        entry = workbook.getManifest().getFileEntry(MARKER_PATH_PREFIX + resourcePath);
        assertNotNull(entry);
        assertEquals(0, entry.getReferenceCount());
        assertFalse(workbook.getManifest().getFileEntries().contains(entry));

        topic.addMarker(marker1.getId());

        assertEquals(markerSheet, group1.getParent());
        assertEquals(group1, marker1.getParent());

        entry = workbook.getManifest().getFileEntry(MARKER_PATH_PREFIX + resourcePath);
        assertNotNull(entry);
        assertEquals(2, entry.getReferenceCount());
        assertTrue(workbook.getManifest().getFileEntries().contains(entry));
    }

    @Test
    public void testFixBugThatFailsToCloneMarkerBecauseOfUsingCustomMarkerSheetAsParent() throws Exception {
        /// mock the system marker sheet
        IStorage systemStorage = new ByteArrayStorage();
        IMarkerSheet systemMarkerSheet = Core.getMarkerSheetBuilder().createMarkerSheet(
                new MarkerResourceProvider(systemStorage.getInputSource(), systemStorage.getOutputTarget(), true));

        /// mock the user marker sheet
        IStorage userStorage = new ByteArrayStorage();
        IMarkerSheet userMarkerSheet = Core.getMarkerSheetBuilder().createMarkerSheet(
                new MarkerResourceProvider(userStorage.getInputSource(), userStorage.getOutputTarget(), false));
        assertNotEquals(systemMarkerSheet, userMarkerSheet);
        userMarkerSheet.setParentSheet(systemMarkerSheet);

        /// create a marker in user marker sheet
        IMarker userMarker = userMarkerSheet.createMarker(null);
        IMarkerGroup userMarkerGroup = userMarkerSheet.createMarkerGroup(false);
        userMarkerGroup.addMarker(userMarker);
        userMarkerSheet.addMarkerGroup(userMarkerGroup);

        /// create a workbook marker sheet and use user marker sheet as its parent
        IStorage storage1 = new ByteArrayStorage();
        IWorkbook workbook1 = createWorkbook(storage1);
        IMarkerSheet markerSheet1 = workbook1.getMarkerSheet();
        assertNotEquals(markerSheet1, userMarkerSheet);
        markerSheet1.setParentSheet(userMarkerSheet);

        IMarker marker1 = (IMarker) new CloneHandler().withMarkerSheets(userMarkerSheet, markerSheet1)
                .cloneObject(userMarker);
        assertEquals(userMarker, marker1);
        assertEquals(userMarkerGroup, marker1.getParent());
        assertEquals(userMarkerSheet, marker1.getParent().getParent());
        assertEquals(userMarkerSheet, marker1.getOwnedSheet());

        /// create a workbook marker sheet and use system marker sheet as its parent
        IStorage storage2 = new ByteArrayStorage();
        IWorkbook workbook2 = createWorkbook(storage2);
        IMarkerSheet markerSheet2 = workbook2.getMarkerSheet();
        assertNotEquals(markerSheet2, userMarkerSheet);
        markerSheet2.setParentSheet(systemMarkerSheet);

        IMarker marker2 = (IMarker) new CloneHandler().withMarkerSheets(userMarkerSheet, markerSheet2)
                .cloneObject(userMarker);
        assertNotEquals(userMarker, marker2);
        assertNotNull(marker2);
        assertNotEquals(userMarkerGroup, marker2.getParent());
        assertNotNull(marker2.getParent());
        assertNotEquals(userMarkerSheet, marker2.getParent().getParent());
        assertNotEquals(userMarkerSheet, marker2.getOwnedSheet());
        assertEquals(markerSheet2, marker2.getOwnedSheet());
    }

    @Test
    public void testLoadWorkbookWithMarkers() throws Exception {
        IStorage storage1 = new ByteArrayStorage();

        IWorkbook workbook1 = createWorkbook(storage1);
        IMarkerSheet markerSheet1 = workbook1.getMarkerSheet();
        byte[] markerContent1 = randString().getBytes();
        String markerResourcePath1 = markerSheet1.allocateMarkerResource(new ByteArrayInputStream(markerContent1),
                randString() + ".png");
        IMarker marker1 = markerSheet1.createMarker(markerResourcePath1);
        IMarkerGroup markerGroup1 = markerSheet1.createMarkerGroup(true);
        markerGroup1.addMarker(marker1);
        markerSheet1.addMarkerGroup(markerGroup1);
        ITopic topic1 = workbook1.getPrimarySheet().getRootTopic();
        topic1.addMarker(marker1.getId());

        IFileEntry entry1 = workbook1.getManifest().getFileEntry(MARKER_PATH_PREFIX + marker1.getResourcePath());
        assertEquals(2, entry1.getReferenceCount());

        ISerializer serializer = Core.getWorkbookBuilder().newSerializer();
        serializer.setWorkbook(workbook1);
        serializer.setWorkbookStorageAsOutputTarget();
        serializer.serialize(null);

        IDeserializer deserializer = Core.getWorkbookBuilder().newDeserializer();
        deserializer.setWorkbookStorage(storage1);
        deserializer.setWorkbookStorageAsInputSource();
        deserializer.deserialize(null);

        IWorkbook workbook2 = deserializer.getWorkbook();
        assertNotEquals(workbook1, workbook2);
        IMarkerSheet markerSheet2 = workbook2.getMarkerSheet();
        assertNotEquals(markerSheet1, markerSheet2);
        IMarkerGroup markerGroup2 = markerSheet2.getMarkerGroup(markerGroup1.getId());
        assertNotNull(markerGroup2);
        assertEquals(markerSheet2, markerGroup2.getParent());
        IMarker marker2 = markerSheet2.getMarker(marker1.getId());
        assertNotNull(marker2);
        assertEquals(markerGroup2, marker2.getParent());

        ITopic topic2 = workbook2.getPrimarySheet().getRootTopic();
        Set<IMarkerRef> markerRefs = topic2.getMarkerRefs();
        assertEquals(1, markerRefs.size());
        assertEquals(marker2.getId(), markerRefs.iterator().next().getMarkerId());

        IFileEntry entry2 = workbook2.getManifest().getFileEntry(MARKER_PATH_PREFIX + marker2.getResourcePath());
        assertEquals(2, entry2.getReferenceCount());

        topic2.removeMarker(marker2.getId());

        assertNull(marker2.getParent());
        assertNull(markerGroup2.getParent());
        assertEquals(0, entry2.getReferenceCount());

    }

}
