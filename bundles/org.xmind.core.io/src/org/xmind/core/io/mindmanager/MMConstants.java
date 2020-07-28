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
package org.xmind.core.io.mindmanager;


public interface MMConstants {

    String FILE_EXTENSION = ".mmap"; //$NON-NLS-1$

    String bs1 = "0000000000000001"; //cor:Bitset //$NON-NLS-1$
    String bs0 = "0000000000000000"; //cor:Bitset //$NON-NLS-1$

    String umNone = "urn:mindjet:None"; //$NON-NLS-1$
    String umAutomatic = "urn:mindjet:Automatic"; //$NON-NLS-1$

    String umCenter = "urn:mindjet:Center"; //$NON-NLS-1$
    String umLeft = "urn:mindjet:Left"; //$NON-NLS-1$
    String umRight = "urn:mindjet:Right"; //$NON-NLS-1$
    String umMiddle = "urn:mindjet:Middle"; //$NON-NLS-1$
    String umTop = "urn:mindjet:Top"; //$NON-NLS-1$
    String umBottom = "urn:mindjet:Bottom"; //$NON-NLS-1$

    String umOutside = "urn:mindjet:Outside"; //$NON-NLS-1$
    String umInside = "urn:mindjet:Inside"; //$NON-NLS-1$

    String umHorizontal = "urn:mindjet:Horizontal"; //$NON-NLS-1$
    String umVertical = "urn:mindjet:Vertical"; //$NON-NLS-1$

    String umAutoHorizontal = "urn:mindjet:AutomaticHorizontal"; //$NON-NLS-1$
    String umAutoVertical = "urn:mindjet:AutomaticVertical"; //$NON-NLS-1$

    String umDown = "urn:mindjet:Down"; //$NON-NLS-1$
    String umUp = "urn:mindjet:Up"; //$NON-NLS-1$
    String umLeftAndRight = "urn:mindjet:LeftAndRight"; //$NON-NLS-1$
    String umUpAndDown = "urn:mindjet:UpAndDown"; //$NON-NLS-1$

    String umLine = "urn:mindjet:Line"; //$NON-NLS-1$
    String umRectangle = "urn:mindjet:Rectangle"; //$NON-NLS-1$
    String umRoundedRectangle = "urn:mindjet:RoundedRectangle"; //$NON-NLS-1$
    String umOval = "urn:mindjet:Oval"; //$NON-NLS-1$
    String umRoundedRectangleBalloon = "urn:mindjet:RoundedRectangleBalloon"; //$NON-NLS-1$
    String umRectangleBalloon = "urn:mindjet:RectangleBalloon"; //$NON-NLS-1$

    String umElbow = "urn:mindjet:Elbow"; //$NON-NLS-1$
    String umRoundedElbow = "urn:mindjet:RoundedElbow"; //$NON-NLS-1$
    String umCurve = "urn:mindjet:Curve"; //$NON-NLS-1$
    String umStraight = "urn:mindjet:Straight"; //$NON-NLS-1$
    String umAngled = "urn:mindjet:Angled"; //$NON-NLS-1$
    String umBezier = "urn:mindjet:Bezier"; //$NON-NLS-1$

    String umArrow = "urn:mindjet:Arrow"; //$NON-NLS-1$
    String umStealthArrow = "urn:mindjet:StealthArrow"; //$NON-NLS-1$
    String umDiamondArrow = "urn:mindjet:DiamondArrow"; //$NON-NLS-1$
    String umOvalArrow = "urn:mindjet:OvalArrow"; //$NON-NLS-1$
    String umOpenArrow = "urn:mindjet:OpenArrow"; //$NON-NLS-1$
    String umNoArrow = "urn:mindjet:NoArrow"; //$NON-NLS-1$

    String umSquareDot = "urn:mindjet:SquareDot"; //$NON-NLS-1$
    String umSolid = "urn:mindjet:Solid"; //$NON-NLS-1$
    String umRoundDot = "urn:mindjet:RoundDot"; //$NON-NLS-1$
    String umDash = "urn:mindjet:Dash"; //$NON-NLS-1$
    String umDashDot = "urn:mindjet:DashDot"; //$NON-NLS-1$
    String umLongDash = "urn:mindjet:LongDash"; //$NON-NLS-1$
    String umLongDashDot = "urn:mindjet:LongDashDot"; //$NON-NLS-1$
    String umLongDashDotDot = "urn:mindjet:LongDashDotDot"; //$NON-NLS-1$

    String umCurvedLine = "urn:mindjet:CurvedLine"; //$NON-NLS-1$
    String umLines = "urn:mindjet:Lines"; //$NON-NLS-1$
    String umZigzag = "urn:mindjet:Zigzag"; //$NON-NLS-1$
    String umScallops = "urn:mindjet:Scallops"; //$NON-NLS-1$
    String umWaves = "urn:mindjet:Waves"; //$NON-NLS-1$
    String umCurvedRectangle = "urn:mindjet:CurvedRectangle"; //$NON-NLS-1$

    String LineThinnest = "1.000000"; //$NON-NLS-1$
    String LineThin = "2.250000"; //$NON-NLS-1$
    String LineMedium = "3.000000"; //$NON-NLS-1$
    String LineFat = "4.500000"; //$NON-NLS-1$
    String LineFattest = "6.000000"; //$NON-NLS-1$

    String umSentenceStyle = "urn:mindjet:SentenceStyle"; //$NON-NLS-1$

    String umTextRightImageLeft = "urn:mindjet:TextRightImageLeft"; //$NON-NLS-1$
    String umTextLeftImageRight = "urn:mindjet:TextLeftImageRight"; //$NON-NLS-1$
    String umTextBottomImageTop = "urn:mindjet:TextBottomImageTop"; //$NON-NLS-1$
    String umTextTopImageBottom = "urn:mindjet:TextTopImageBottom"; //$NON-NLS-1$

    String umAutoWidth = "urn:mindjet:AutoWidth"; //$NON-NLS-1$

    String umNoFlip = "urn:mindjet:NoFlip"; //$NON-NLS-1$

    String umPngImage = "urn:mindjet:PngImage"; //$NON-NLS-1$

    String umCopySource = "urn:mindjet:CopySource"; //$NON-NLS-1$

    String umFile = "urn:mindjet:File"; //$NON-NLS-1$

    String umUnknown = "urn:mindjet:Unknown"; //$NON-NLS-1$

    String MMARCH = "mmarch://"; //$NON-NLS-1$

    String MMARCH_BIN = "mmarch://bin/"; //$NON-NLS-1$
    
}