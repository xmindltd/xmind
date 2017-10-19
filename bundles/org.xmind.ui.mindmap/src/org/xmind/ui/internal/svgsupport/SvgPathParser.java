package org.xmind.ui.internal.svgsupport;

import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.xmind.gef.draw2d.graphics.Path;

public class SvgPathParser {

    private static SvgPathParser instance;

    private float centralX;

    private float centralY;

    private float clientWidth;

    private float clientHeight;

    private float lastPointX = 0;
    private float lastPointY = 0;

    private float lastKnotX = 0;
    private float lastKnotY = 0;

    private float top = 0;
    private float bottom = 0;
    private float left = 0;
    private float right = 0;

    private SvgPathParser() {
    }

    public void parseSvgPath(Path path, float centralX, float centralY,
            float clientWidth, float clientHeight, String svgPath) {
        this.centralX = centralX;
        this.centralY = centralY;
        this.clientWidth = clientWidth;
        this.clientHeight = clientHeight;

        for (int i = 0; i < 2; i++) {
            final Matcher matchPathCmd = Pattern.compile(
                    "([MmLlHhVvAaQqTtCcSsZz])|([-+]?((\\d*\\.\\d+)|(\\d+))([eE][-+]?\\d+)?)") //$NON-NLS-1$
                    .matcher(svgPath);
            LinkedList<String> tokens = new LinkedList<String>();
            while (matchPathCmd.find())
                tokens.addLast(matchPathCmd.group());

            char curCmd = 'Z';
            while (tokens.size() != 0) {
                String curToken = tokens.removeFirst();
                char initChar = curToken.charAt(0);
                if ((initChar >= 'A' && initChar <= 'Z')
                        || (initChar >= 'a' && initChar <= 'z'))
                    curCmd = initChar;
                else
                    tokens.addFirst(curToken);

                switch (curCmd) {
                case 'M':
                    movetoAbs(path, nextFloat(tokens), nextFloat(tokens),
                            i != 0);
                    curCmd = 'L';
                    break;
                case 'm':
                    movetoRel(path, nextFloat(tokens), nextFloat(tokens),
                            i != 0);
                    curCmd = 'l';
                    break;
                case 'L':
                    linetoAbs(path, nextFloat(tokens), nextFloat(tokens),
                            i != 0);
                    break;
                case 'l':
                    linetoRel(path, nextFloat(tokens), nextFloat(tokens),
                            i != 0);
                    break;
                case 'H':
                    linetoHorizontalAbs(path, nextFloat(tokens), i != 0);
                    break;
                case 'h':
                    linetoHorizontalRel(path, nextFloat(tokens), i != 0);
                    break;
                case 'V':
                    linetoVerticalAbs(path, nextFloat(tokens), i != 0);
                    break;
                case 'v':
                    linetoVerticalRel(path, nextFloat(tokens), i != 0);
                    break;
                case 'A':
                case 'a':
                    break;
                case 'Q':
                    curvetoQuadraticAbs(path, nextFloat(tokens),
                            nextFloat(tokens), nextFloat(tokens),
                            nextFloat(tokens), i != 0);
                    break;
                case 'q':
                    curvetoQuadraticRel(path, nextFloat(tokens),
                            nextFloat(tokens), nextFloat(tokens),
                            nextFloat(tokens), i != 0);
                    break;
                case 'T':
                    curvetoQuadraticSmoothAbs(path, nextFloat(tokens),
                            nextFloat(tokens), i != 0);
                    break;
                case 't':
                    curvetoQuadraticSmoothRel(path, nextFloat(tokens),
                            nextFloat(tokens), i != 0);
                    break;
                case 'C':
                    curvetoCubicAbs(path, nextFloat(tokens), nextFloat(tokens),
                            nextFloat(tokens), nextFloat(tokens),
                            nextFloat(tokens), nextFloat(tokens), i != 0);
                    break;
                case 'c':
                    curvetoCubicRel(path, nextFloat(tokens), nextFloat(tokens),
                            nextFloat(tokens), nextFloat(tokens),
                            nextFloat(tokens), nextFloat(tokens), i != 0);
                    break;
                case 'S':
                    curvetoCubicSmoothAbs(path, nextFloat(tokens),
                            nextFloat(tokens), nextFloat(tokens),
                            nextFloat(tokens), i != 0);
                    break;
                case 's':
                    curvetoCubicSmoothRel(path, nextFloat(tokens),
                            nextFloat(tokens), nextFloat(tokens),
                            nextFloat(tokens), i != 0);
                    break;
                case 'Z':
                case 'z':
                    if (i != 0) {
                        path.close();
                        left = 0;
                        right = 0;
                        top = 0;
                        bottom = 0;
                    }
                    lastPointX = 0;
                    lastPointY = 0;
                    lastKnotX = 0;
                    lastKnotY = 0;
                    break;
                default:
                    throw new RuntimeException("Invalid path element"); //$NON-NLS-1$
                }
            }
        }

    }

    private void movetoAbs(Path path, float x, float y, boolean draw) {
        if (draw)
            path.moveTo(correctX(x), correctY(y));
        lastPointX = x;
        lastPointY = y;

        if (!draw) {
            left = x;
            right = x;
            top = y;
            bottom = y;
        }
    }

    private void movetoRel(Path path, float x, float y, boolean draw) {
        movetoAbs(path, x + lastPointX, y + lastPointY, draw);
    }

    private void linetoAbs(Path path, float x, float y, boolean draw) {
        if (draw)
            path.lineTo(correctX(x), correctY(y));

        lastPointX = x;
        lastPointY = y;

        lastKnotX = x;
        lastKnotY = y;

        if (!draw)
            calcTBLR(x, y);
    }

    private void linetoRel(Path path, float x, float y, boolean draw) {
        linetoAbs(path, x + lastPointX, y + lastPointY, draw);
    }

    private void linetoHorizontalAbs(Path path, float x, boolean draw) {
        linetoAbs(path, x, lastPointY, draw);
    }

    private void linetoHorizontalRel(Path path, float x, boolean draw) {
        linetoHorizontalAbs(path, x + lastPointX, draw);
    }

    private void linetoVerticalAbs(Path path, float y, boolean draw) {
        linetoAbs(path, lastPointX, y, draw);
    }

    private void linetoVerticalRel(Path path, float y, boolean draw) {
        linetoVerticalAbs(path, y + lastPointY, draw);
    }

    private void curvetoQuadraticAbs(Path path, float x1, float y1, float x,
            float y, boolean draw) {
        if (draw)
            path.quadTo(correctX(x1), correctY(y1), correctX(x), correctY(y));

        lastPointX = x;
        lastPointY = y;

        lastKnotX = x1;
        lastKnotY = y1;

        if (!draw)
            calcTBLR(x, y);
    }

    private void curvetoQuadraticRel(Path path, float x1, float y1, float x,
            float y, boolean draw) {
        curvetoQuadraticAbs(path, x1 + lastPointX, y1 + lastPointY,
                x + lastPointX, y + lastPointY, draw);
    }

    private void curvetoQuadraticSmoothAbs(Path path, float x, float y,
            boolean draw) {
        curvetoQuadraticAbs(path, lastKnotX, lastKnotY, x, y, draw);
    }

    private void curvetoQuadraticSmoothRel(Path path, float x, float y,
            boolean draw) {
        curvetoQuadraticSmoothAbs(path, x + lastPointX, y + lastPointY, draw);
    }

    private void curvetoCubicAbs(Path path, float x1, float y1, float x2,
            float y2, float x, float y, boolean draw) {
        if (draw)
            path.cubicTo(correctX(x1), correctY(y1), correctX(x2), correctY(y2),
                    correctX(x), correctY(y));

        lastPointX = x;
        lastPointY = y;

        lastKnotX = x2;
        lastKnotY = y2;

        if (!draw)
            calcTBLR(x, y);
    }

    private void curvetoCubicRel(Path path, float x1, float y1, float x2,
            float y2, float x, float y, boolean draw) {
        curvetoCubicAbs(path, x1 + lastPointX, y1 + lastPointY, x2 + lastPointX,
                y2 + lastPointY, x + lastPointX, y + lastPointY, draw);
    }

    private void curvetoCubicSmoothAbs(Path path, float x2, float y2, float x,
            float y, boolean draw) {
        curvetoCubicAbs(path, lastKnotX * 2f - lastPointX,
                lastKnotY * 2f - lastPointY, x2, y2, x, y, draw);
    }

    private void curvetoCubicSmoothRel(Path path, float x2, float y2, float x,
            float y, boolean draw) {
        curvetoCubicSmoothAbs(path, x2 + lastPointX, y2 + lastPointY,
                x + lastPointX, y + lastPointY, draw);
    }

    private float nextFloat(LinkedList<String> tokens) {
        String token = tokens.removeFirst();
        return Float.parseFloat(token);
    }

    private void calcTBLR(float x, float y) {
        left = Math.min(left, x);
        right = Math.max(right, x);
        top = Math.min(top, y);
        bottom = Math.max(bottom, y);
    }

    private float correctX(float x) {
        float baseX = x - (left + right) / 2;

        baseX = baseX * clientWidth / (right - left);

        return baseX + centralX;
    }

    private float correctY(float y) {
        float baseY = y - (top + bottom) / 2;

        baseY = baseY * clientHeight / (bottom - top);

        return baseY + centralY;
    }

    public static SvgPathParser getInstance() {
        if (instance == null)
            instance = new SvgPathParser();
        return instance;
    }

}
