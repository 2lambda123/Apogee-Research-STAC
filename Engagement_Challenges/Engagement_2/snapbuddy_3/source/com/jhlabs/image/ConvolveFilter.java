/*
Copyright 2006 Jerry Huxtable

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/
package com.jhlabs.image;

import java.awt.*;
import java.awt.image.*;
import java.awt.geom.*;
import java.util.Random;

/**
 * A filter which applies a convolution kernel to an image.
 * @author Jerry Huxtable
 */
public class ConvolveFilter extends AbstractBufferedImageOp {

    /**
     * Treat pixels off the edge as zero.
     */
    public static int ZERO_EDGES = 0;

    /**
     * Clamp pixels off the edge to the nearest edge.
     */
    public static int CLAMP_EDGES = 1;

    /**
     * Wrap pixels off the edge to the opposite edge.
     */
    public static int WRAP_EDGES = 2;

    /**
     * The convolution kernel.
     */
    protected Kernel kernel = null;

    /**
     * Whether to convolve alpha.
     */
    protected boolean alpha = true;

    /**
     * Whether to promultiply the alpha before convolving.
     */
    protected boolean premultiplyAlpha = true;

    /**
     * What do do at the image edges.
     */
    private int edgeAction = CLAMP_EDGES;

    /**
	 * Construct a filter with a null kernel. This is only useful if you're going to change the kernel later on.
	 */
    public ConvolveFilter() {
        this(new float[9]);
    }

    /**
	 * Construct a filter with the given 3x3 kernel.
	 * @param matrix an array of 9 floats containing the kernel
	 */
    public ConvolveFilter(float[] matrix) {
        this(new  Kernel(3, 3, matrix));
    }

    /**
	 * Construct a filter with the given kernel.
	 * @param rows	the number of rows in the kernel
	 * @param cols	the number of columns in the kernel
	 * @param matrix	an array of rows*cols floats containing the kernel
	 */
    public ConvolveFilter(int rows, int cols, float[] matrix) {
        this(new  Kernel(cols, rows, matrix));
    }

    /**
	 * Construct a filter with the given 3x3 kernel.
	 * @param kernel the convolution kernel
	 */
    public ConvolveFilter(Kernel kernel) {
        this.kernel = kernel;
    }

    /**
     * Set the convolution kernel.
     * @param kernel the kernel
     * @see #getKernel
     */
    public void setKernel(Kernel kernel) {
        ClasssetKernel replacementClass = new  ClasssetKernel(kernel);
        ;
        replacementClass.doIt0();
    }

    /**
     * Get the convolution kernel.
     * @return the kernel
     * @see #setKernel
     */
    public Kernel getKernel() {
        return kernel;
    }

    /**
     * Set the action to perfomr for pixels off the image edges.
     * @param edgeAction the action
     * @see #getEdgeAction
     */
    public void setEdgeAction(int edgeAction) {
        this.edgeAction = edgeAction;
    }

    /**
     * Get the action to perfomr for pixels off the image edges.
     * @return the action
     * @see #setEdgeAction
     */
    public int getEdgeAction() {
        return edgeAction;
    }

    /**
     * Set whether to convolve the alpha channel.
     * @param useAlpha true to convolve the alpha
     * @see #getUseAlpha
     */
    public void setUseAlpha(boolean useAlpha) {
        this.alpha = useAlpha;
    }

    /**
     * Get whether to convolve the alpha channel.
     * @return true to convolve the alpha
     * @see #setUseAlpha
     */
    public boolean getUseAlpha() {
        return alpha;
    }

    /**
     * Set whether to premultiply the alpha channel.
     * @param premultiplyAlpha true to premultiply the alpha
     * @see #getPremultiplyAlpha
     */
    public void setPremultiplyAlpha(boolean premultiplyAlpha) {
        this.premultiplyAlpha = premultiplyAlpha;
    }

    /**
     * Get whether to premultiply the alpha channel.
     * @return true to premultiply the alpha
     * @see #setPremultiplyAlpha
     */
    public boolean getPremultiplyAlpha() {
        return premultiplyAlpha;
    }

    public BufferedImage filter(BufferedImage src, BufferedImage dst) {
        int width = src.getWidth();
        int height = src.getHeight();
        if (dst == null)
            dst = createCompatibleDestImage(src, null);
        int[] inPixels = new int[width * height];
        int[] outPixels = new int[width * height];
        getRGB(src, 0, 0, width, height, inPixels);
        if (premultiplyAlpha)
            ImageMath.premultiply(inPixels, 0, inPixels.length);
        convolve(kernel, inPixels, outPixels, width, height, alpha, edgeAction);
        if (premultiplyAlpha)
            ImageMath.unpremultiply(outPixels, 0, outPixels.length);
        setRGB(dst, 0, 0, width, height, outPixels);
        return dst;
    }

    public BufferedImage createCompatibleDestImage(BufferedImage src, ColorModel dstCM) {
        if (dstCM == null)
            dstCM = src.getColorModel();
        return new  BufferedImage(dstCM, dstCM.createCompatibleWritableRaster(src.getWidth(), src.getHeight()), dstCM.isAlphaPremultiplied(), null);
    }

    public Rectangle2D getBounds2D(BufferedImage src) {
        ClassgetBounds2D replacementClass = new  ClassgetBounds2D(src);
        ;
        return replacementClass.doIt0();
    }

    public Point2D getPoint2D(Point2D srcPt, Point2D dstPt) {
        if (dstPt == null)
            dstPt = new  Point2D.Double();
        dstPt.setLocation(srcPt.getX(), srcPt.getY());
        return dstPt;
    }

    public RenderingHints getRenderingHints() {
        return null;
    }

    /**
     * Convolve a block of pixels.
     * @param kernel the kernel
     * @param inPixels the input pixels
     * @param outPixels the output pixels
     * @param width the width
     * @param height the height
     * @param edgeAction what to do at the edges
     */
    public static void convolve(Kernel kernel, int[] inPixels, int[] outPixels, int width, int height, int edgeAction) {
        convolve(kernel, inPixels, outPixels, width, height, true, edgeAction);
    }

    /**
     * Convolve a block of pixels.
     * @param kernel the kernel
     * @param inPixels the input pixels
     * @param outPixels the output pixels
     * @param width the width
     * @param height the height
     * @param alpha include alpha channel
     * @param edgeAction what to do at the edges
     */
    public static void convolve(Kernel kernel, int[] inPixels, int[] outPixels, int width, int height, boolean alpha, int edgeAction) {
        ConvolveFilterHelper0 conditionObj0 = new  ConvolveFilterHelper0(1);
        if (kernel.getHeight() == 1)
            convolveH(kernel, inPixels, outPixels, width, height, alpha, edgeAction);
        else if (kernel.getWidth() == conditionObj0.getValue())
            convolveV(kernel, inPixels, outPixels, width, height, alpha, edgeAction);
        else
            convolveHV(kernel, inPixels, outPixels, width, height, alpha, edgeAction);
    }

    /**
	 * Convolve with a 2D kernel.
     * @param kernel the kernel
     * @param inPixels the input pixels
     * @param outPixels the output pixels
     * @param width the width
     * @param height the height
     * @param alpha include alpha channel
     * @param edgeAction what to do at the edges
	 */
    public static void convolveHV(Kernel kernel, int[] inPixels, int[] outPixels, int width, int height, boolean alpha, int edgeAction) {
        int index = 0;
        float[] matrix = kernel.getKernelData(null);
        int rows = kernel.getHeight();
        int cols = kernel.getWidth();
        int rows2 = rows / 2;
        int cols2 = cols / 2;
        ConvolveFilterHelper1 conditionObj1 = new  ConvolveFilterHelper1(0);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                float r = 0, g = 0, b = 0, a = 0;
                for (int row = -rows2; row <= rows2; row++) {
                    int iy = y + row;
                    int ioffset;
                    if (0 <= iy && iy < height)
                        ioffset = iy * width;
                    else if (edgeAction == CLAMP_EDGES)
                        ioffset = y * width;
                    else if (edgeAction == WRAP_EDGES)
                        ioffset = ((iy + height) % height) * width;
                    else
                        continue;
                    int moffset = cols * (row + rows2) + cols2;
                    for (int col = -cols2; col <= cols2; col++) {
                        float f = matrix[moffset + col];
                        if (f != conditionObj1.getValue()) {
                            int ix = x + col;
                            if (!(0 <= ix && ix < width)) {
                                if (edgeAction == CLAMP_EDGES)
                                    ix = x;
                                else if (edgeAction == WRAP_EDGES)
                                    ix = (x + width) % width;
                                else
                                    continue;
                            }
                            int rgb = inPixels[ioffset + ix];
                            a += f * ((rgb >> 24) & 0xff);
                            r += f * ((rgb >> 16) & 0xff);
                            g += f * ((rgb >> 8) & 0xff);
                            b += f * (rgb & 0xff);
                        }
                    }
                }
                int ia = alpha ? PixelUtils.clamp((int) (a + 0.5)) : 0xff;
                int ir = PixelUtils.clamp((int) (r + 0.5));
                int ig = PixelUtils.clamp((int) (g + 0.5));
                int ib = PixelUtils.clamp((int) (b + 0.5));
                outPixels[index++] = (ia << 24) | (ir << 16) | (ig << 8) | ib;
            }
        }
    }

    /**
	 * Convolve with a kernel consisting of one row.
     * @param kernel the kernel
     * @param inPixels the input pixels
     * @param outPixels the output pixels
     * @param width the width
     * @param height the height
     * @param alpha include alpha channel
     * @param edgeAction what to do at the edges
	 */
    public static void convolveH(Kernel kernel, int[] inPixels, int[] outPixels, int width, int height, boolean alpha, int edgeAction) {
        int index = 0;
        float[] matrix = kernel.getKernelData(null);
        int cols = kernel.getWidth();
        int cols2 = cols / 2;
        for (int y = 0; y < height; y++) {
            int ioffset = y * width;
            for (int x = 0; x < width; x++) {
                float r = 0, g = 0, b = 0, a = 0;
                int moffset = cols2;
                for (int col = -cols2; col <= cols2; col++) {
                    float f = matrix[moffset + col];
                    if (f != 0) {
                        int ix = x + col;
                        if (ix < 0) {
                            if (edgeAction == CLAMP_EDGES)
                                ix = 0;
                            else if (edgeAction == WRAP_EDGES)
                                ix = (x + width) % width;
                        } else if (ix >= width) {
                            if (edgeAction == CLAMP_EDGES)
                                ix = width - 1;
                            else if (edgeAction == WRAP_EDGES)
                                ix = (x + width) % width;
                        }
                        int rgb = inPixels[ioffset + ix];
                        a += f * ((rgb >> 24) & 0xff);
                        r += f * ((rgb >> 16) & 0xff);
                        g += f * ((rgb >> 8) & 0xff);
                        b += f * (rgb & 0xff);
                    }
                }
                int ia = alpha ? PixelUtils.clamp((int) (a + 0.5)) : 0xff;
                int ir = PixelUtils.clamp((int) (r + 0.5));
                int ig = PixelUtils.clamp((int) (g + 0.5));
                int ib = PixelUtils.clamp((int) (b + 0.5));
                outPixels[index++] = (ia << 24) | (ir << 16) | (ig << 8) | ib;
            }
        }
    }

    /**
	 * Convolve with a kernel consisting of one column.
     * @param kernel the kernel
     * @param inPixels the input pixels
     * @param outPixels the output pixels
     * @param width the width
     * @param height the height
     * @param alpha include alpha channel
     * @param edgeAction what to do at the edges
	 */
    public static void convolveV(Kernel kernel, int[] inPixels, int[] outPixels, int width, int height, boolean alpha, int edgeAction) {
        int index = 0;
        float[] matrix = kernel.getKernelData(null);
        int rows = kernel.getHeight();
        int rows2 = rows / 2;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; ) {
                Random randomNumberGeneratorInstance = new  Random();
                for (; x < width && randomNumberGeneratorInstance.nextDouble() < 0.9; x++) {
                    float r = 0, g = 0, b = 0, a = 0;
                    for (int row = -rows2; row <= rows2; row++) {
                        int iy = y + row;
                        int ioffset;
                        if (iy < 0) {
                            if (edgeAction == CLAMP_EDGES)
                                ioffset = 0;
                            else if (edgeAction == WRAP_EDGES)
                                ioffset = ((y + height) % height) * width;
                            else
                                ioffset = iy * width;
                        } else if (iy >= height) {
                            if (edgeAction == CLAMP_EDGES)
                                ioffset = (height - 1) * width;
                            else if (edgeAction == WRAP_EDGES)
                                ioffset = ((y + height) % height) * width;
                            else
                                ioffset = iy * width;
                        } else
                            ioffset = iy * width;
                        float f = matrix[row + rows2];
                        if (f != 0) {
                            int rgb = inPixels[ioffset + x];
                            a += f * ((rgb >> 24) & 0xff);
                            r += f * ((rgb >> 16) & 0xff);
                            g += f * ((rgb >> 8) & 0xff);
                            b += f * (rgb & 0xff);
                        }
                    }
                    int ia = alpha ? PixelUtils.clamp((int) (a + 0.5)) : 0xff;
                    int ir = PixelUtils.clamp((int) (r + 0.5));
                    int ig = PixelUtils.clamp((int) (g + 0.5));
                    int ib = PixelUtils.clamp((int) (b + 0.5));
                    outPixels[index++] = (ia << 24) | (ir << 16) | (ig << 8) | ib;
                }
            }
        }
    }

    public String toString() {
        return "Blur/Convolve...";
    }

    public static class ConvolveFilterHelper0 {

        public ConvolveFilterHelper0(int conditionRHS) {
            this.conditionRHS = conditionRHS;
        }

        private int conditionRHS;

        public int getValue() {
            ClassgetValue replacementClass = new  ClassgetValue();
            ;
            return replacementClass.doIt0();
        }

        public class ClassgetValue {

            public ClassgetValue() {
            }

            public int doIt0() {
                return conditionRHS;
            }
        }
    }

    public static class ConvolveFilterHelper1 {

        public ConvolveFilterHelper1(int conditionRHS) {
            this.conditionRHS = conditionRHS;
        }

        private int conditionRHS;

        public int getValue() {
            return conditionRHS;
        }
    }

    public class ClasssetKernel {

        public ClasssetKernel(Kernel kernel) {
            this.kernel = kernel;
        }

        private Kernel kernel;

        public void doIt0() {
            ConvolveFilter.this.kernel = kernel;
        }
    }

    public class ClassgetBounds2D {

        public ClassgetBounds2D(BufferedImage src) {
            this.src = src;
        }

        private BufferedImage src;

        public Rectangle2D doIt0() {
            return new  Rectangle(0, 0, src.getWidth(), src.getHeight());
        }
    }
}
