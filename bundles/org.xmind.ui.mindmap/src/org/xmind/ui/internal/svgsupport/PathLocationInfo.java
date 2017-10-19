package org.xmind.ui.internal.svgsupport;

/**
 * 
 * @author Enki Xiong
 *
 */
public class PathLocationInfo {

    private double centralX = 0.0;
    private double centralY = 0.0;

    private double lastPointX = 0.0;
    private double lastPointY = 0.0;

    private double lastKnotX = 0.0;
    private double lastKnotY = 0.0;

    @Override
    public PathLocationInfo clone() {
        PathLocationInfo info = new PathLocationInfo();
        info.centralX = this.centralX;
        info.centralY = this.centralY;
        return info;
    }

    public double getCentralX() {
        return centralX;
    }

    public void setCentralX(double centralX) {
        this.centralX = centralX;
    }

    public double getCentralY() {
        return centralY;
    }

    public void setCentralY(double centralY) {
        this.centralY = centralY;
    }

    public double getLastPointX() {
        return lastPointX;
    }

    public void setLastPointX(double lastPointX) {
        this.lastPointX = lastPointX;
    }

    public double getLastPointY() {
        return lastPointY;
    }

    public void setLastPointY(double lastPointY) {
        this.lastPointY = lastPointY;
    }

    public double getLastKnotX() {
        return lastKnotX;
    }

    public void setLastKnotX(double lastKnotX) {
        this.lastKnotX = lastKnotX;
    }

    public double getLastKnotY() {
        return lastKnotY;
    }

    public void setLastKnotY(double lastKnotY) {
        this.lastKnotY = lastKnotY;
    }

}
