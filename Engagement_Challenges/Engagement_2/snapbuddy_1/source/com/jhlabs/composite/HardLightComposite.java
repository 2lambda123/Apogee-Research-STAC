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

public final class HardLightComposite extends RGBComposite {

    public HardLightComposite(float alpha) {
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
            int conditionObj0 = 127;
            int conditionObj1 = 127;
            int conditionObj2 = 127;
            for (int i = 0; i < w; i += 4) {
                int sr = src[i];
                int dir = dst[i];
                int sg = src[i + 1];
                int dig = dst[i + 1];
                int sb = src[i + 2];
                int dib = dst[i + 2];
                int sa = src[i + 3];
                int dia = dst[i + 3];
                int dor, dog, dob;
                if (sr > conditionObj2)
                    dor = 255 - 2 * multiply255(255 - sr, 255 - dir);
                else
                    dor = 2 * multiply255(sr, dir);
                if (sg > conditionObj1)
                    dog = 255 - 2 * multiply255(255 - sg, 255 - dig);
                else
                    dog = 2 * multiply255(sg, dig);
                if (sb > conditionObj0)
                    dob = 255 - 2 * multiply255(255 - sb, 255 - dib);
                else
                    dob = 2 * multiply255(sb, dib);
                float a = alpha * sa / 255f;
                float ac = 1 - a;
                dst[i] = (int) (a * dor + ac * dir);
                dst[i + 1] = (int) (a * dog + ac * dig);
                dst[i + 2] = (int) (a * dob + ac * dib);
                dst[i + 3] = (int) (sa * alpha + dia * ac);
            }
        }
    }
}
