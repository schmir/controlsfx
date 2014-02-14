package org.controlsfx.tools.rectangle;

import java.util.Objects;

import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;

import org.controlsfx.tools.MathTools;

/**
 * Moves the rectangle around.
 */
public class MoveChangeStrategy extends AbstractPreviousRectangleChangeStrategy {

    /*
     * The previous rectangle will be moved around using a vector computed from the start to the current point.
     * The moved rectangle will be forced within defined bounds.
     */

    // ATTRIBUTES

    /**
     * A rectangle which defines the bounds within which the previous rectangle can be moved.
     */
    private final Rectangle2D bounds;

    /**
     * The starting point of the selection change. The move will be computed relative to this point.
     */
    private Point2D startingPoint;

    // CONSTRUCTORS

    /**
     * Creates a new change strategy which moves the specified rectangle within the specified bounds.
     * 
     * @param previous
     *            the previous rectangle this move is based on
     * @param bounds
     *            the bounds within which the rectangle can be moved
     */
    protected MoveChangeStrategy(Rectangle2D previous, Rectangle2D bounds) {
        super(previous, false, 0);
        Objects.requireNonNull(bounds, "The specified bounds must not be null.");
        this.bounds = bounds;
    }

    /**
     * Creates a new change strategy which moves the specified rectangle within the specified bounds defined by the
     * rectangle from {@code (0, 0)} to {@code (maxX, maxY)}.
     * 
     * @param previous
     *            the previous rectangle this move is based on
     * @param maxX
     *            the maximal x-coordinate of the right edge of the created rectangles; must be greater than or equal to
     *            the previous rectangle's width
     * @param maxY
     *            the maximal y-coordinate of the lower edge of the created rectangles; must be greater than or equal to
     *            the previous rectangle's height
     */
    public MoveChangeStrategy(Rectangle2D previous, double maxX, double maxY) {
        super(previous, false, 0);
        if (maxX < previous.getWidth())
            throw new IllegalArgumentException(
                    "The specified maximal x-coordinate must be greater than or equal to the previous rectangle's width.");
        if (maxY < previous.getHeight())
            throw new IllegalArgumentException(
                    "The specified maximal y-coordinate must be greater than or equal to the previous rectangle's height.");

        bounds = new Rectangle2D(0, 0, maxX, maxY);
    }

    // IMPLEMENTATION OF 'do...'

    /**
     * Moves the previous rectangle to the specified point relative to the {@link #startingPoint}.
     * 
     * @param point
     *            the vector from the {@link #startingPoint} to this point defines the movement
     * @return the moved rectangle
     */
    private final Rectangle2D moveRectangleToPoint(Point2D point) {

        /*
         * The computation makes sure that no part of the rectangle can be moved out the bounds.
         * To achieve this, the coordinates of the future rectangle's upper left corner are forced into the intervals
         *  - [boundsMinX, boundsMaxX - previousRectangleWidth],
         *  - [boundsMinY, boundsMaxY - previousRectangleHeight] respectively.
         */

        // vector from starting to specified point
        double xMove = point.getX() - startingPoint.getX();
        double yMove = point.getY() - startingPoint.getY();

        // upper left corner
        double upperLeftX = getPrevious().getMinX() + xMove;
        double upperLeftY = getPrevious().getMinY() + yMove;

        // upper bounds for upper left corner
        double maxX = bounds.getMaxX() - getPrevious().getWidth();
        double maxY = bounds.getMaxY() - getPrevious().getHeight();

        // corrected upper left corner
        double correctedUpperLeftX = MathTools.inInterval(bounds.getMinX(), upperLeftX, maxX);
        double correctedUpperLeftY = MathTools.inInterval(bounds.getMinY(), upperLeftY, maxY);

        // rectangle from corected upper left corner with the previous rectangle's width and height
        return new Rectangle2D(
                correctedUpperLeftX, correctedUpperLeftY,
                getPrevious().getWidth(), getPrevious().getHeight());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Rectangle2D doBegin(Point2D point) {
        this.startingPoint = point;
        return getPrevious();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Rectangle2D doContinue(Point2D point) {
        return moveRectangleToPoint(point);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Rectangle2D doEnd(Point2D point) {
        return moveRectangleToPoint(point);
    }

}
