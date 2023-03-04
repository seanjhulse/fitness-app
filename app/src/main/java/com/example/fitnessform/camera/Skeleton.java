package com.example.fitnessform.camera;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

/**
 * An overlay for showing the human skeleton on top of the Camera Preview.
 */
public class Skeleton extends View
{
    private final Object lock = new Object();
    private final List<Graphic> graphics = new ArrayList<>();
    // Matrix for transforming from image coordinates to overlay view coordinates.
    private final Matrix transformationMatrix = new Matrix();

    private int imageWidth;
    private int imageHeight;
    // The factor of overlay View size to image size. Anything in the image coordinates need to be
    // scaled by this amount to fit with the area of overlay View.
    private float scaleFactor = 1.0f;
    // The number of horizontal pixels needed to be cropped on each side to fit the image with the
    // area of overlay View after scaling.
    private float postScaleWidthOffset;
    // The number of vertical pixels needed to be cropped on each side to fit the image with the
    // area of overlay View after scaling.
    private float postScaleHeightOffset;
    private boolean isImageFlipped;
    private boolean needUpdateTransformation = true;

    public Skeleton(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        addOnLayoutChangeListener(
            (view, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) ->
                needUpdateTransformation = true);
    }

    /**
     * Removes all graphics from the overlay.
     */
    public void clear()
    {
        synchronized (lock)
        {
            graphics.clear();
        }
        postInvalidate();
    }

    /**
     * Adds a graphic to the overlay.
     */
    public void add(Graphic graphic)
    {
        synchronized (lock)
        {
            graphics.add(graphic);
        }

        invalidate();
    }

    /**
     * Removes a graphic from the overlay.
     */
    public void remove(Graphic graphic)
    {
        synchronized (lock)
        {
            graphics.remove(graphic);
        }
        postInvalidate();
    }

    /**
     * Sets the source information of the image being processed by detectors, including size and whether it is flipped,
     * which informs how to transform image coordinates later.
     *
     * @param imageWidth  the width of the image sent to ML Kit detectors
     * @param imageHeight the height of the image sent to ML Kit detectors
     * @param isFlipped   whether the image is flipped. Should set it to true when the image is from the front camera.
     */
    public void setImageSourceInfo(int imageWidth, int imageHeight, boolean isFlipped)
    {
        synchronized (lock)
        {
            this.imageWidth = imageWidth;
            this.imageHeight = imageHeight;
            this.isImageFlipped = isFlipped;
            needUpdateTransformation = true;
        }
        postInvalidate();
    }

    public boolean isImageFlipped()
    {
        return isImageFlipped;
    }

    public float getPostScaleHeightOffset()
    {
        return postScaleHeightOffset;
    }

    public float getPostScaleWidthOffset()
    {
        return postScaleWidthOffset;
    }

    public float getScaleFactor()
    {
        return scaleFactor;
    }

    public boolean isNeedUpdateTransformation()
    {
        return needUpdateTransformation;
    }

    public Matrix getTransformationMatrix()
    {
        return transformationMatrix;
    }

    private void updateTransformationIfNeeded()
    {
        if (!needUpdateTransformation || imageWidth <= 0 || imageHeight <= 0)
        {
            return;
        }
        float viewAspectRatio = (float) getWidth() / getHeight();
        float imageAspectRatio = (float) imageWidth / imageHeight;
        postScaleWidthOffset = 0;
        postScaleHeightOffset = 0;
        if (viewAspectRatio > imageAspectRatio)
        {
            // The image needs to be vertically cropped to be displayed in this view.
            scaleFactor = (float) getWidth() / imageWidth;
            postScaleHeightOffset = ((float) getWidth() / imageAspectRatio - getHeight()) / 2;
        } else
        {
            // The image needs to be horizontally cropped to be displayed in this view.
            scaleFactor = (float) getHeight() / imageHeight;
            postScaleWidthOffset = ((float) getHeight() * imageAspectRatio - getWidth()) / 2;
        }

        transformationMatrix.reset();
        transformationMatrix.setScale(scaleFactor, scaleFactor);
        transformationMatrix.postTranslate(-postScaleWidthOffset, -postScaleHeightOffset);

        if (isImageFlipped)
        {
            transformationMatrix.postScale(-1f, 1f, getWidth() / 2f, getHeight() / 2f);
        }

        needUpdateTransformation = false;
    }

    /**
     * Draws the overlay with its associated graphic objects.
     */
    @Override
    protected void onDraw(Canvas canvas)
    {
        super.onDraw(canvas);

        synchronized (lock)
        {
            updateTransformationIfNeeded();

            for (Graphic graphic : graphics)
            {
                graphic.draw(canvas);
            }
        }
    }
}
