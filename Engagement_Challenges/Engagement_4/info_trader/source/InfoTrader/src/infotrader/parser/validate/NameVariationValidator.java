/*
 * Copyright (c) 2009-2016 Matthew R. Harrah
 *
 * MIT License
 *
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following
 * conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 */
package infotrader.parser.validate;

import infotrader.parser.model.AbstractNameVariation;

/**
 * Validator for { AbstractNameVariation}
 * 
 * @author frizbog1
 * 
 */
class NameVariationValidator extends AbstractValidator {

    /**
     * The name variation being validated
     */
    protected AbstractNameVariation nv;

    /**
     * Constructor
     * 
     * @param rootValidator
     *            the root { InfoTraderValidator} that contains all the findings and settings
     * @param nv
     *            the name variation being validated
     */
    public NameVariationValidator(InfoTraderValidator rootValidator, AbstractNameVariation nv) {
        this.rootValidator = rootValidator;
        this.nv = nv;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validate() {
        if (nv == null) {
            addError("Name variation is null and cannot be validated");
            return;
        }
        checkCustomTags(nv);
        checkRequiredString(nv.getVariation(), "variation on a personal name", nv);
        checkOptionalString(nv.getVariationType(), "type of variation on a personal name", nv);

    }
}
