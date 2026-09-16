package com.shpp.p2p.cs.dyushchenko.assignment11;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;

/**
 * Counts the number of large silhouettes in a given image. Small silhouettes
 * (noise, tiny objects) are ignored.
 *
 * <p>The user enters the image name, if nothing is entered, the program uses the default name {@code DEFAULT_IMAGE_PATH}.
 * The program first determines the background color of the given photo, converts the image into
 * a two-dimensional {@code SmartPixel} array, and uses the Depth-First Search (DFS) algorithm
 * to traverse the image and calculate the dimensions of the silhouette.
 * </p>
 * Silhouettes will be counted only if they satisfy the minimum size requirements.
 */
public class Assignment11Part1 {

    /**
     * The default image name, used when user has not entered any image name.
     */
    private static final String DEFAULT_IMAGE_NAME = "test.jpg";

    /**
     * Maximum allowed RGB distance for two colors to be considered similar.
     */
    private static final int SIMILARITY_THRESHOLD = 30;

    /**
     * The number of neighbors to check around target pixel.
     */
    private static final int NUM_NEIGHBORS = 4;

    /**
     * Array of row offsets used to get neighboring pixels around the target pixel.
     */
    private static final int[] ROW_OFFSETS = new int[]{-1, 1, 0, 0};
    /**
     * Array of column offsets used to get neighboring pixels around the target pixel.
     */
    private static final int[] COL_OFFSETS = new int[]{0, 0, -1, 1};

    /**
     * The minimum ratio of the source image's dimensions to silhouette's dimensions
     * that a large silhouette must exceed to be considered valid.
     */
    public static final double MIN_SILHOUETTE_SCALE = 0.3;

    /**
     * The entry point of the program.
     * Reads the image name from the program arguments or uses the default name if user has not entered anything.
     * Counts and displays the number of large silhouettes.
     *
     * @param args A line entered by the user, where args[0] is the name of the image.
     */
    void main(String[] args) {
        String imagePath = args.length == 0 ? DEFAULT_IMAGE_NAME : args[0];
        BufferedImage bufferedImage;
        try {
            bufferedImage = ImageIO.read(new File(imagePath));
        } catch (IOException _) {
            throw new IllegalArgumentException("Failed to read image file: " + imagePath);
        }

        int[][] pixelArray = get2DPixelArray(bufferedImage);
        Color bgColor = getBackgroundColor(pixelArray);
        SmartPixel[][] smartPixelArray = createSmartPixelArray(pixelArray, bgColor);

        int silhouetteCount = findSilhouettes(smartPixelArray);
        System.out.println(silhouetteCount);
    }

    /**
     * Get two-dimensional array of integers, which stores the RGB values of the pixels in the image.
     *
     * @param image The given image in BufferedImage format.
     * @return 2D array of integers, which stores the RGB values of the pixels in the image.
     */
    private int[][] get2DPixelArray(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        int[][] result = new int[height][width];

        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                result[row][col] = image.getRGB(col, row);
            }
        }
        return result;
    }

    /**
     * Gets the background color from the given pixel array.
     * The background color is the color that appears most frequently in the pixel array.
     *
     * @param pixelArray 2D pixel array of RGB pixel values.
     * @return The background color of image.
     */
    private Color getBackgroundColor(int[][] pixelArray) {
        ArrayList<Color> colors1 = new ArrayList<>();
        ArrayList<Color> colors2 = new ArrayList<>();

        Color firstPixelColor = new Color(pixelArray[0][0]);
        for (int row = 0; row < pixelArray.length; row++) {
            for (int col = 1; col < pixelArray[0].length; col++) {
                Color currentColor = new Color(pixelArray[row][col]);
                if (isSimilar(firstPixelColor, currentColor)) {
                    colors1.add(currentColor);
                } else {
                    colors2.add(currentColor);
                }
            }
        }

        return colors1.size() > colors2.size() ? firstPixelColor : colors2.getFirst();
    }

    /**
     * Checks if two given colors are similar.
     *
     * @param color1 The first color to compare.
     * @param color2 The second color to compare.
     * @return True if similar, false otherwise.
     */
    private boolean isSimilar(Color color1, Color color2) {
        int rDiff = color1.getRed() - color2.getRed();
        int gDiff = color1.getGreen() - color2.getGreen();
        int bDiff = color1.getBlue() - color2.getBlue();

        double distance = Math.sqrt((double) rDiff * rDiff + gDiff * gDiff + bDiff * bDiff);

        return distance < SIMILARITY_THRESHOLD;
    }


    /**
     * Creates a 2D array of SmartPixels from given pixel array.
     *
     * @param pixelArray The given 2D pixel array.
     * @param bgColor    The background color of the image.
     * @return 2D array of SmartPixels.
     */
    private SmartPixel[][] createSmartPixelArray(int[][] pixelArray, Color bgColor) {
        SmartPixel[][] smartPixels = new SmartPixel[pixelArray.length][pixelArray[0].length];
        Color currentPixelColor;

        for (int row = 0; row < smartPixels.length; row++) {
            for (int col = 0; col < smartPixels[0].length; col++) {
                currentPixelColor = new Color(pixelArray[row][col]);
                smartPixels[row][col] = new SmartPixel(row, col, isSimilar(currentPixelColor, bgColor));
            }
        }
        return smartPixels;
    }

    /**
     * Counts the number of silhouettes that are large enough to be counted.
     *
     * @param smartPixelArray The 2D array of SmartPixels.
     * @return The number of found silhouettes.
     * @throws NullPointerException if it can't find SmartPixel at specific coordinates.
     */
    private int findSilhouettes(SmartPixel[][] smartPixelArray) {
        int silCount = 0;
        SmartPixel currentPixel;

        for (int row = 0; row < smartPixelArray.length; row++) {
            for (int col = 0; col < smartPixelArray[0].length; col++) {
                currentPixel = smartPixelArray[row][col];
                if (currentPixel == null) {
                    throw new NullPointerException("Can't find SmartPixel at row: " + row + ", col: " + col);
                }
                if (!currentPixel.isBackground() && !currentPixel.isVisited()) {
                    SilhouetteCharacteristics silhouette = exploreSilhouette(smartPixelArray, currentPixel);
                    if (isSilhouetteBigEnough(silhouette.getLenght(), silhouette.getWidth(), smartPixelArray)) {
                        silCount++;
                    }
                }
            }
        }
        return silCount;
    }

    /**
     * Explores a single silhouette starting from the given pixel using DFS (Depth-First Search) algorithm.
     * Keeps track of the maximum and minimum coordinates of the silhouette.
     *
     * @param smartPixelArray The 2D array of SmartPixels.
     * @param startPixel      The SmartPixel which is the starting point of the DFS algorithm.
     * @return The boundaries (min/max row and col) of the found silhouette.
     */
    private SilhouetteCharacteristics exploreSilhouette(SmartPixel[][] smartPixelArray, SmartPixel startPixel) {
        int maxRow = startPixel.getRow();
        int minRow = startPixel.getRow();
        int maxCol = startPixel.getCol();
        int minCol = startPixel.getCol();

        SmartPixel currentPixel;
        SmartPixel[] neighbors;

        Deque<SmartPixel> stack = new ArrayDeque<>();
        stack.push(startPixel);
        while (!stack.isEmpty()) {
            currentPixel = stack.pop();
            currentPixel.setVisited();
            int currentPixelRow = currentPixel.getRow();
            int currentPixelCol = currentPixel.getCol();

            maxRow = Math.max(currentPixelRow, maxRow);
            minRow = Math.min(currentPixelRow, minRow);
            maxCol = Math.max(currentPixelCol, maxCol);
            minCol = Math.min(currentPixelCol, minCol);

            neighbors = getNeighboringPixels(smartPixelArray, currentPixel);
            for (SmartPixel neighbor : neighbors) {
                if (!neighbor.isBackground() && !neighbor.isVisited()) {
                    stack.push(neighbor);
                }
            }
        }
        return new SilhouetteCharacteristics(minRow, maxRow, minCol, maxCol);
    }

    /**
     * Finds all valid neighboring pixels around the target pixel.
     *
     * @param smartPixelArray The 2D array of SmartPixels.
     * @param mainPixel       The target pixel whose neighbors are searched for.
     * @return An array of SmartPixels
     */
    private SmartPixel[] getNeighboringPixels(SmartPixel[][] smartPixelArray, SmartPixel mainPixel) {
        int mainPixelRow = mainPixel.getRow();
        int mainPixelCol = mainPixel.getCol();

        List<SmartPixel> neighborPixels = new ArrayList<>();
        for (int i = 0; i < NUM_NEIGHBORS; i++) {
            int neighborRow = mainPixelRow + ROW_OFFSETS[i];
            int neighborCol = mainPixelCol + COL_OFFSETS[i];
            if (isRowAndColValid(neighborRow, neighborCol, smartPixelArray)) {
                SmartPixel neighborPixel = smartPixelArray[neighborRow][neighborCol];
                neighborPixels.add(neighborPixel);
            }
        }
        return neighborPixels.toArray(new SmartPixel[0]);
    }

    /**
     * Calculates if the given row and column numbers are within the array's size limits.
     *
     * @param row    The given row number.
     * @param col    The given col number.
     * @param pixels The 2D array of SmartPixels.
     * @return True if row and col numbers are valid, false otherwise.
     */
    private boolean isRowAndColValid(int row, int col, SmartPixel[][] pixels) {
        return (row >= 0 && row < pixels.length)
                && (col >= 0 && col < pixels[0].length);
    }

    /**
     * Checks whether the silhouette is large enough to be counted.
     *
     * @param lenght          The lenght of the silhouette.
     * @param width           The width of the silhouette.
     * @param smartPixelArray The 2D array of SmartPixels.
     * @return True if large enough to be counted, false otherwise.
     */
    private boolean isSilhouetteBigEnough(int lenght, int width, SmartPixel[][] smartPixelArray) {
        return (lenght > (smartPixelArray.length * MIN_SILHOUETTE_SCALE)) || (width > (smartPixelArray[0].length * MIN_SILHOUETTE_SCALE));
    }
}
