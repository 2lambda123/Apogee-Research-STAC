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

/**
 * A colormap with the colors of the spectrum.
 */
public class SpectrumColormap implements Colormap {

    /**
	 * Construct a spcetrum color map.
	 */
    public SpectrumColormap() {
    }

    /**
	 * Convert a value in the range 0..1 to an RGB color.
	 * @param v a value in the range 0..1
	 * @return an RGB color
	 */
    public int getColor(float v) {
        ClassgetColor replacementClass = new  ClassgetColor(v);
        ;
        return replacementClass.doIt0();
    }

    public class ClassgetColor {

        public ClassgetColor(float v) {
            this.v = v;
        }

        private float v;

        public int doIt0() {
            return Spectrum.wavelengthToRGB(380 + 400 * ImageMath.clamp(v, 0, 1.0f));
        }
    }
}
