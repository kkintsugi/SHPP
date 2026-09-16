package com.shpp.p2p.cs.dyushchenko.assignment11;

/**
 * Stores the lenght and width of the silhouette, which are calculated based
 * on its coordinates in the pixel grid (minimum/maximum row and column of the silhouette).
 */
public class SilhouetteCharacteristics {
    /**
     * The lenght of the silhouette.
     */
    private final int lenght;
    /**
     * The width of the silhouette.
     */
    private final int width;

    /**
     * Constructs a new SilhouetteCharacteristics object with specified minimum/maximum row and column
     * coordinates of the silhouette.
     * Calculates the length and width of silhouette based on coordinates.
     *
     * @param minRow The minimum row index of the silhouette.
     * @param maxRow The maximum row index of the silhouette.
     * @param minCol The minimum column index of the silhouette.
     * @param maxCol The maximum column index of the silhouette.
     */
    public SilhouetteCharacteristics(int minRow, int maxRow, int minCol, int maxCol) {
        this.lenght = maxRow - minRow;
        this.width = maxCol - minCol;
    }

    public int getLenght() {
        return lenght;
    }

    public int getWidth() {
        return width;
    }
}
