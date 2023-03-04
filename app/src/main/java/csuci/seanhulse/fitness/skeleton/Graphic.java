package csuci.seanhulse.fitness.skeleton;

import android.graphics.Canvas;
import android.graphics.Matrix;

/**
 * Base class for a custom graphics object to be rendered within the graphic overlay. Subclass this and implement
 * the {@link Graphic#draw(Canvas)} method to define the graphics element. Add instances to the overlay
 * using {@link Skeleton#add(Graphic)}.
 */
public abstract class Graphic
{
    private final Skeleton overlay;

    public Graphic(Skeleton overlay)
    {
        this.overlay = overlay;
    }

    /**
     * Draw the graphic on the supplied canvas. Drawing should use the following methods to convert to view
     * coordinates for the graphics that are drawn:
     *
     * <ol>
     *   <li>{@link Graphic#scale(float)} adjusts the size of the supplied value from the image
     *       scale to the view scale.
     *   <li>{@link Graphic#translateX(float)} and {@link Graphic#translateY(float)} adjust the
     *       coordinate from the image's coordinate system to the view coordinate system.
     * </ol>
     *
     * @param canvas drawing canvas
     */
    public abstract void draw(Canvas canvas);

    /**
     * Adjusts the supplied value from the image scale to the view scale.
     */
    public float scale(float imagePixel)
    {
        return imagePixel * overlay.getScaleFactor();
    }

    /**
     * Adjusts the x coordinate from the image's coordinate system to the view coordinate system.
     */
    public float translateX(float x)
    {
        if (overlay.isImageFlipped())
        {
            return overlay.getWidth() - (scale(x) - overlay.getPostScaleWidthOffset());
        } else
        {
            return scale(x) - overlay.getPostScaleWidthOffset();
        }
    }

    /**
     * Adjusts the y coordinate from the image's coordinate system to the view coordinate system.
     */
    public float translateY(float y)
    {
        return scale(y) - overlay.getPostScaleHeightOffset();
    }

    /**
     * Returns a {@link Matrix} for transforming from image coordinates to overlay view coordinates.
     */
    public Matrix getTransformationMatrix()
    {
        return overlay.getTransformationMatrix();
    }

    public void postInvalidate()
    {
        overlay.postInvalidate();
    }
}