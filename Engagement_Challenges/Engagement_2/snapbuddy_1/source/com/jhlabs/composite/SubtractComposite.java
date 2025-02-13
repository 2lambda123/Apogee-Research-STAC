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
package com.jhlabs.composite;

import java.awt.*;
import java.awt.image.*;

public final class SubtractComposite extends RGBComposite {

    public SubtractComposite(float alpha) {
        super(alpha);
    }

    public CompositeContext createContext(ColorModel srcColorModel, ColorModel dstColorModel, RenderingHints hints) {
        return new  Context(extraAlpha, srcColorModel, dstColorModel);
    }

    static class Context extends RGBCompositeContext {

        public Context(float alpha, ColorModel srcColorModel, ColorModel dstColorModel) {
            super(alpha, srcColorModel, dstColorModel);
        }

        public void composeRGB(int[] src, int[] dst, float alpha) {
            int w = src.length;
            int conditionObj0 = 0;
            int conditionObj1 = 0;
            int conditionObj2 = 0;
            for (int i = 0; i < w; i += 4) {
                composeRGBHelper(alpha, dst, src, conditionObj0, conditionObj1, conditionObj2, i);
            }
        }

        private void composeRGBHelper(float alpha, int[] dst, int[] src, int conditionObj0, int conditionObj1, int conditionObj2, int i) {
            int sr = src[i];
            int dir = dst[i];
            int sg = src[i + 1];
            int dig = dst[i + 1];
            int sb = src[i + 2];
            int dib = dst[i + 2];
            int sa = src[i + 3];
            int dia = dst[i + 3];
            int dor, dog, dob;
            dor = dir - sr;
            if (dor < conditionObj2)
                dor = 0;
            dog = dig - sg;
            if (dog < conditionObj1)
                dog = 0;
            dob = dib - sb;
            if (dob < conditionObj0)
                dob = 0;
            float a = alpha * sa / 255f;
            float ac = 1 - a;
            dst[i] = (int) (a * dor + ac * dir);
            dst[i + 1] = (int) (a * dog + ac * dig);
            dst[i + 2] = (int) (a * dob + ac * dib);
            dst[i + 3] = (int) (sa * alpha + dia * ac);
        }
    }
}
