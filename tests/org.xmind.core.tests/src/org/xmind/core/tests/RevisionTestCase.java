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

import java.io.*;

import org.junit.*;
import org.xmind.core.*;

/**
 * @author Frank Shaka
 *
 */
public class RevisionTestCase {

	@Test
	public void testAddRevision() throws IOException, CoreException {
		IWorkbook workbook = Core.getWorkbookBuilder().createWorkbook();
		ISheet sheet1 = workbook.getPrimarySheet();

		assertEquals(sheet1, workbook.getElementById(sheet1.getId()));

		IRevisionManager revManager = workbook.getRevisionRepository().getRevisionManager(sheet1.getId(),
				IRevision.SHEET);
		assertNotNull(revManager);
		IRevision rev = revManager.addRevision(sheet1);
		ISheet sheet2 = (ISheet) rev.getContent();
		assertNotNull(sheet2);
		assertNotEquals(sheet1, sheet2);

		assertEquals(sheet1, workbook.getElementById(sheet1.getId()));
	}

}
