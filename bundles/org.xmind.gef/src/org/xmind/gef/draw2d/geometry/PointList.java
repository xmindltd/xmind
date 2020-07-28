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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.draw2d.geometry.Point;



public class PointList {
    
    private int num;
    private int min;
    private int max;
    private List<Point> points;
    public static final int POINTS_MAX     = 4;
    public static final int POINTS_MIN     = 2;
    
    public PointList() {
        this( PointList.POINTS_MIN, PointList.POINTS_MAX );
    }
    
    public PointList( int min, int max ) {
        this.min = min;
        this.max = max;
        this.num = min;
        this.points = new ArrayList<Point>( max );
        for ( int i = 0; i < max; i++ ) {
            this.points.add( null );
        }
    }
    
    public PointList( PointList another ) {
        this( another.min, another.max );
        setNumPoints( another.getNumPoints() );
        for ( int i = 0; i < max; i++ ) {
            setPoint( i, another.getPoint( i ) );
        }
    }
    
    public int getNumPoints() {
        return num;
    }
    
    public boolean setNumPoints( int points ) {
        int n = Math.max( min, Math.min( max, points ) );
        if ( n == num )
            return false;
        this.num = n;
        return true;
    }
    
    public Point getPoint( int index ) {
        if ( index < 0 || index >= num )
            return null;
        Point p = points.get( index );
        return p == null ? null : new Point( p );
    }
    
    public boolean setPoint( int index, Point p ) {
        if ( index < 0 || index >= num )
            return false;
        Point old = getPoint( index );
        if ( p == old || ( p != null && p.equals( old ) ) )
            return false;
        points.set( index, p == null ? null : new Point( p ) );
        return true;
    }

    public List<Point> getPoints() {
        return new ArrayList<Point>( points ).subList( 0, num );
    }
    
    public boolean equals( Object obj ) {
        if ( obj == this )
            return true;
        if ( !( obj instanceof PointList ) )
            return false;
        PointList pm = (PointList) obj;
        return this.min == pm.min && this.max == pm.max && this.num == pm.num &&
                this.points.equals( pm.points );
    }
    
}