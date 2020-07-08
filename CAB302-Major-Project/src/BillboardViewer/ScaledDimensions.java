package BillboardViewer;

import java.awt.*;

public class ScaledDimensions {

    private Dimension imageSize;
    private Dimension boundary;

    /**
     * Scale the image to the appropriate size, whilst maintaining aspect ratio
     * @param imageSize size of the image
     * @param boundary boundary
     */
    public ScaledDimensions(Dimension imageSize, Dimension boundary) {
        this.imageSize = imageSize;
        this.boundary = boundary;
    }

    /**
     * Scale the image
     * @return scaled image
     */
    public Dimension getScaledDimension() {
        double widthRatio = boundary.getWidth() / imageSize.getWidth();
        double heightRatio = boundary.getHeight() / imageSize.getHeight();
        double ratio = Math.min(widthRatio, heightRatio);
        return new Dimension((int) (imageSize.width  * ratio), (int) (imageSize.height * ratio));
    }

}
