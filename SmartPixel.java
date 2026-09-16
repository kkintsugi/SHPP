package com.shpp.p2p.cs.dyushchenko.assignment11;

/**
 * Acts like an individual pixel in the image pixel grid with extra data used to analyze silhouette.
 * <p>
 * Stores pixel coordinates in the grid, whether the pixel was already visited during traversal,
 * and whether it belongs to background of the image.
 * </p>
 */
public class SmartPixel {
    /**
     * The row coordinate in the pixel grid.
     */
    private final int row;
    /**
     * The column coordinate in the pixel grid.
     */
    private final int col;

    /**
     * Whether the pixel was visited.
     */
    private boolean visited = false;

    /**
     * Whether the pixel belongs to background of the image.
     */
    private final boolean isBackground;

    /**
     * Constructs a new SmartPixel with specified coordinates and background status.
     *
     * @param row  The row coordinate in the pixel grid.
     * @param col  The column coordinate in the pixel grid.
     * @param isBg True if the color of the pixel is similar to background color of the image, false otherwise.
     */
    public SmartPixel(int row, int col, boolean isBg) {
        this.row = row;
        this.col = col;
        this.isBackground = isBg;
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    public boolean isVisited() {
        return visited;
    }

    public void setVisited() {
        visited = true;
    }

    public boolean isBackground() {
        return isBackground;
    }
}
