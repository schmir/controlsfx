/**
 * Copyright (c) 2013, 2014 ControlsFX
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *     * Neither the name of ControlsFX, any associated website, nor the
 * names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL CONTROLSFX BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package impl.org.controlsfx.spreadsheet;

import com.sun.javafx.scene.control.skin.TableCellSkin;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.beans.value.WeakChangeListener;
import javafx.collections.ObservableList;
import javafx.css.PseudoClass;
import javafx.scene.control.TableCell;
import javafx.scene.layout.Region;
import org.controlsfx.control.spreadsheet.SpreadsheetCell;
import org.controlsfx.control.spreadsheet.SpreadsheetCell.CornerPosition;

/**
 *
 * This is the skin for the {@link CellView}.
 *
 * Its main goal is to draw an object (a triangle) on cells which have their
 * {@link SpreadsheetCell#commentedProperty()} set to true.
 *
 */
public class CellViewSkin extends TableCellSkin<ObservableList<SpreadsheetCell>, SpreadsheetCell> {

    private final static PseudoClass TOP_LEFT_PSEUDO_CLASS = PseudoClass.getPseudoClass("top-left"); //$NON-NLS-1$
    private final static PseudoClass TOP_RIGHT_PSEUDO_CLASS = PseudoClass.getPseudoClass("top-right"); //$NON-NLS-1$
    private final static PseudoClass BOTTOM_RIGHT_PSEUDO_CLASS = PseudoClass.getPseudoClass("bottom-right"); //$NON-NLS-1$
    private final static PseudoClass BOTTOM_LEFT_PSEUDO_CLASS = PseudoClass.getPseudoClass("bottom-left"); //$NON-NLS-1$
    /**
     * The size of the edge of the triangle FIXME Handling of static variable
     * will be changed.
     */
    private static final int TRIANGLE_SIZE = 8;
    /**
     * The region we will add on the cell when necessary.
     */
    private Region topLeftRegion = null;
    private Region topRightRegion = null;
    private Region bottomRightRegion = null;
    private Region bottomLeftRegion = null;

    public CellViewSkin(TableCell<ObservableList<SpreadsheetCell>, SpreadsheetCell> tableCell) {
        super(tableCell);
        tableCell.itemProperty().addListener(new WeakChangeListener<>(itemChangeListener));
        if (tableCell.getItem() != null) {
            tableCell.getItem().cornerProperty().addListener(triangleListener);
        }
    }

    @Override
    protected void layoutChildren(double x, final double y, final double w, final double h) {
        super.layoutChildren(x, y, w, h);
        if (getSkinnable().getItem() != null) {
            layoutTriangle();
        }
    }

    private void layoutTriangle() {
        SpreadsheetCell cell = getSkinnable().getItem();
        
        handleTopLeft(cell);
        handleTopRight(cell);
        handleBottomLeft(cell);
        handleBottomRight(cell);

        getSkinnable().requestLayout();
    }

    private void handleTopLeft(SpreadsheetCell cell) {
        if (cell.getCorner(CornerPosition.TOP_LEFT)) {
            if (topLeftRegion == null) {
                topLeftRegion = getRegion(CornerPosition.TOP_LEFT);
            }
            if (!getChildren().contains(topLeftRegion)) {
                getChildren().add(topLeftRegion);
            }
            topLeftRegion.relocate(0, snappedTopInset() - 1);
        } else if (topLeftRegion != null) {
            getChildren().remove(topLeftRegion);
            topLeftRegion = null;
        }
    }

    private void handleTopRight(SpreadsheetCell cell) {
        if (cell.getCorner(CornerPosition.TOP_RIGHT)) {
            if (topRightRegion == null) {
                topRightRegion = getRegion(CornerPosition.TOP_RIGHT);
            }
            if (!getChildren().contains(topRightRegion)) {
                getChildren().add(topRightRegion);
            }
            topRightRegion.relocate(getSkinnable().getWidth() - TRIANGLE_SIZE, snappedTopInset() - 1);
        } else if (topRightRegion != null) {
            getChildren().remove(topRightRegion);
            topRightRegion = null;
        }
    }

    private void handleBottomRight(SpreadsheetCell cell) {
        if (cell.getCorner(CornerPosition.BOTTOM_RIGHT)) {
            if (bottomRightRegion == null) {
                bottomRightRegion = getRegion(CornerPosition.BOTTOM_RIGHT);
            }
            if (!getChildren().contains(bottomRightRegion)) {
                getChildren().add(bottomRightRegion);
            }
            bottomRightRegion.relocate(getSkinnable().getWidth() - TRIANGLE_SIZE, getSkinnable().getHeight() - TRIANGLE_SIZE);
        } else if (bottomRightRegion != null) {
            getChildren().remove(bottomRightRegion);
            bottomRightRegion = null;
        }
    }
    private void handleBottomLeft(SpreadsheetCell cell) {
        if (cell.getCorner(CornerPosition.BOTTOM_LEFT)) {
            if (bottomLeftRegion == null) {
                bottomLeftRegion = getRegion(CornerPosition.BOTTOM_LEFT);
            }
            if (!getChildren().contains(bottomLeftRegion)) {
                getChildren().add(bottomLeftRegion);
            }
            bottomLeftRegion.relocate(0, getSkinnable().getHeight() - TRIANGLE_SIZE);
        } else if (bottomLeftRegion != null) {
            getChildren().remove(bottomLeftRegion);
            bottomLeftRegion = null;
        }
    }

    private static Region getRegion(CornerPosition position) {
        Region region = new Region();
        region.resize(TRIANGLE_SIZE, TRIANGLE_SIZE);
        region.getStyleClass().add("comment"); //$NON-NLS-1$
        switch (position) {
            case TOP_LEFT:
                region.pseudoClassStateChanged(TOP_LEFT_PSEUDO_CLASS, true);
                break;
            case TOP_RIGHT:
                region.pseudoClassStateChanged(TOP_RIGHT_PSEUDO_CLASS, true);
                break;
            case BOTTOM_RIGHT:
                region.pseudoClassStateChanged(BOTTOM_RIGHT_PSEUDO_CLASS, true);
                break;
            case BOTTOM_LEFT:
                region.pseudoClassStateChanged(BOTTOM_LEFT_PSEUDO_CLASS, true);
                break;

        }

        return region;
    }
    
    private final InvalidationListener triangleListener = new InvalidationListener() {

        @Override
        public void invalidated(Observable o) {
            getSkinnable().requestLayout();
        }
    };

    private final ChangeListener<SpreadsheetCell> itemChangeListener = new ChangeListener<SpreadsheetCell>() {
        @Override
        public void changed(ObservableValue<? extends SpreadsheetCell> arg0, SpreadsheetCell oldCell,
                SpreadsheetCell newCell) {
            if (oldCell != null) {
                oldCell.cornerProperty().removeListener(triangleListener);
            }
            if (newCell != null) {
                newCell.cornerProperty().addListener(triangleListener);
            }
            if (getSkinnable().getItem() != null) {
                layoutTriangle();
            }
        }
    };
}
