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
package org.xmind.gef.part;



/**
 * @author Brian Sun
 */
public interface IPartListener {
    
    public void childAdded( PartEvent event );
    
    public void childRemoving( PartEvent event );
    
    public class Stub implements IPartListener {
        /**
         * @see cn.brainy.gef.part.IPartListener#childAdded(cn.brainy.gef.part.PartEvent)
         */
        public void childAdded( PartEvent event ) {
        }
        /**
         * @see cn.brainy.gef.part.IPartListener#childRemoving(cn.brainy.gef.part.PartEvent)
         */
        public void childRemoving( PartEvent event ) {
        }
    }
}