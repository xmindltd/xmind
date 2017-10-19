package org.xmind.ui.internal.svgsupport;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 
 * @author Enki Xiong
 *
 */
public class PathParser {
    public final Pattern pathPattern = Pattern.compile(
            "([MmLlHhVvAaQqTtCcSsZz])|([-+]?((\\d*\\.\\d+)|(\\d+))([eE][-+]?\\d+)?)"); //$NON-NLS-1$
    private static PathParser instance = null;

    private PathParser() {

    }

    public List<PathElement> parseSVGPath(String pathDefinitionString) {

        final Matcher matchPathCmd = pathPattern.matcher(pathDefinitionString);
        LinkedList<String> tokens = new LinkedList<String>();
        List<PathElement> elements = new LinkedList<PathElement>();
        PathLocationInfo info = new PathLocationInfo();

        char curCmd = 'Z';
        while (matchPathCmd.find()) {
            String detailElement = matchPathCmd.group();
            tokens.addLast(detailElement);
        }

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
                moveTo(elements, info, nextDouble(tokens), nextDouble(tokens),
                        true);
                break;
            case 'm':
                moveTo(elements, info, nextDouble(tokens), nextDouble(tokens),
                        false);
                break;
            case 'L':
                lineTo(elements, info, nextDouble(tokens), nextDouble(tokens),
                        true);
                break;
            case 'l':
                lineTo(elements, info, nextDouble(tokens), nextDouble(tokens),
                        false);
                break;
            case 'H':
                horizontalTo(elements, info, nextDouble(tokens), true);
                break;
            case 'h':
                horizontalTo(elements, info, nextDouble(tokens), false);
                break;
            case 'V':
                verticalTo(elements, info, nextDouble(tokens), true);
                break;
            case 'v':
                verticalTo(elements, info, nextDouble(tokens), false);
                break;
            case 'A':
            case 'a':
                break;
            case 'Q':
                quadraticBelzierCurveTo(elements, info, nextDouble(tokens),
                        nextDouble(tokens), nextDouble(tokens),
                        nextDouble(tokens), true);
                break;
            case 'q':
                quadraticBelzierCurveTo(elements, info, nextDouble(tokens),
                        nextDouble(tokens), nextDouble(tokens),
                        nextDouble(tokens), false);
                break;
            case 'T':
                smoothQuadraticBelzierCurveTo(elements, info,
                        nextDouble(tokens), nextDouble(tokens), true);
                break;
            case 't':
                smoothQuadraticBelzierCurveTo(elements, info,
                        nextDouble(tokens), nextDouble(tokens), false);
                break;
            case 'C':
                curveTo(elements, info, nextDouble(tokens), nextDouble(tokens),
                        nextDouble(tokens), nextDouble(tokens),
                        nextDouble(tokens), nextDouble(tokens), true);
                break;
            case 'c':
                curveTo(elements, info, nextDouble(tokens), nextDouble(tokens),
                        nextDouble(tokens), nextDouble(tokens),
                        nextDouble(tokens), nextDouble(tokens), false);
                break;
            case 'S':
                smoothCurveTo(elements, info, nextDouble(tokens),
                        nextDouble(tokens), nextDouble(tokens),
                        nextDouble(tokens), true);
                break;
            case 's':
                smoothCurveTo(elements, info, nextDouble(tokens),
                        nextDouble(tokens), nextDouble(tokens),
                        nextDouble(tokens), false);
                break;
            case 'Z':
            case 'z':
                closePath(elements, info);
                break;
            default:
                throw new RuntimeException("Invalid path element"); //$NON-NLS-1$
            }
        }
        return elements;
    }

    private void closePath(List<PathElement> elements, PathLocationInfo info) {
        info.setLastPointX(0.0);
        info.setLastPointY(0.0);
        info.setLastKnotX(0.0);
        info.setLastKnotY(0.0);
        elements.add(PathElement.getClosePathElement());
    }

    private void smoothCurveTo(List<PathElement> elements,
            PathLocationInfo info, double x2, double y2, double x, double y,
            boolean isAbsolute) {
        if (!isAbsolute) {
            x2 += info.getLastPointX();
            y2 += info.getLastPointY();
            x += info.getLastPointX();
            y += info.getLastPointY();
        }
        curveTo(elements, info, info.getLastKnotX() * 2 - info.getLastPointX(),
                info.getLastKnotY() * 2 - info.getLastPointY(), x2, y2, x, y,
                true);

    }

    private void curveTo(List<PathElement> elements, PathLocationInfo info,
            double x1, double y1, double x2, double y2, double x, double y,
            boolean isAbsolute) {
        if (!isAbsolute) {
            x1 += info.getLastPointX();
            x2 += info.getLastPointX();
            x += info.getLastPointX();
            y1 += info.getLastPointY();
            y2 += info.getLastPointY();
            y += info.getLastPointY();
        }
        elements.add(PathElement.getCurveElement(x1, y1, x2, y2, x, y));

        info.setLastPointX(x);
        info.setLastPointY(y);
        info.setLastKnotX(x2);
        info.setLastKnotY(y2);

    }

    private void smoothQuadraticBelzierCurveTo(List<PathElement> elements,
            PathLocationInfo info, double x, double y, boolean isAbsolute) {
        if (!isAbsolute) {
            x += info.getLastPointX();
            y += info.getLastPointY();
        }
        quadraticBelzierCurveTo(elements, info, info.getLastKnotX(),
                info.getLastKnotY(), x, y, true);
    }

    private void quadraticBelzierCurveTo(List<PathElement> elements,
            PathLocationInfo info, double x1, double y1, double x, double y,
            boolean isAbsolute) {
        if (!isAbsolute) {
            x1 += info.getLastPointX();
            y1 += info.getLastPointY();
            x += info.getLastPointX();
            y += info.getLastPointY();
        }
        elements.add(PathElement.getQuadraticBelzierCurveElement(x1, y1, x, y));

        info.setLastPointX(x);
        info.setLastPointY(y);
        info.setLastKnotX(x1);
        info.setLastKnotY(y1);

    }

    private void verticalTo(List<PathElement> elements, PathLocationInfo info,
            double y, boolean isAbsolute) {
        if (!isAbsolute)
            y += info.getLastPointY();
        lineTo(elements, info, info.getLastPointX(), y, true);

    }

    private void horizontalTo(List<PathElement> elements, PathLocationInfo info,
            double x, boolean isAbsolute) {
        if (!isAbsolute)
            x += info.getLastPointX();
        lineTo(elements, info, x, info.getLastPointY(), true);
    }

    private void lineTo(List<PathElement> elements, PathLocationInfo info,
            double x, double y, boolean isAbsolute) {

        if (!isAbsolute) {
            x += info.getLastPointX();
            y += info.getLastPointY();
        }

        elements.add(PathElement.getLineElement(x, y));

        info.setLastPointX(x);
        info.setLastPointY(y);

        info.setLastKnotX(x);
        info.setLastKnotY(y);

    }

    private void moveTo(List<PathElement> elements, PathLocationInfo info,
            double x, double y, boolean isAbsolute) {
        elements.add(PathElement.getMoveElement(x, y));
        info.setLastPointX(x);
        info.setLastPointY(y);

    }

    private double nextDouble(LinkedList<String> tokens) {
        String token = tokens.removeFirst();
        return Double.valueOf(token);
    }

    public static PathParser getInstance() {
        if (instance == null) {
            instance = new PathParser();
        }
        return instance;
    }

}
