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
package org.xmind.gef.draw2d.geometry;

import java.io.Serializable;

/**
 * @author Frank Shaka
 */
public class PrecisionPointPair extends PrecisionPointPairBase implements Cloneable, Serializable {

    private static final long serialVersionUID = 8473420997351793529L;
    
    public PrecisionPointPair() {
    }
    
    public PrecisionPointPair( PrecisionPoint p1, PrecisionPoint p2 ) {
        super( p1, p2 );
    }
    
    public PrecisionPoint p1() {
        return point1;
    }
    
    public PrecisionPoint p2() {
        return point2;
    }
    
    public void setP1( PrecisionPoint p1 ) {
        this.point1 = p1;
    }
    
    public void setP2( PrecisionPoint p2 ) {
        this.point2 = p2;
    }
    
    public PrecisionPointPair getCopy() {
        return new PrecisionPointPair( point1.getCopy(), point2.getCopy() );
    }
    
    public PrecisionPointPair swap() {
        return (PrecisionPointPair) super.swap();
    }
    
    protected Object clone() throws CloneNotSupportedException {
        return getCopy();
    }
    
}