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
package infotrader.parser.io.writer;

import java.io.IOException;
import java.io.OutputStream;

/**
 * An { OutputStream} Decorator that tracks/exposes the number of bytes written to the stream
 * 
 * @author frizbog
 *
 */
class ProgressTrackingOutputStream extends OutputStream {

    /**
     * The number of bytes written to this stream
     */
    int bytesWritten;

    /**
     * The output stream being decorated
     */
    private final OutputStream out;

    /**
     * Constructor
     * 
     * @param out
     *            the output stream to be decorated
     */
    public ProgressTrackingOutputStream(OutputStream out) {
        this.out = out;
    }

    @Override
    public void write(byte[] b) throws IOException {
        out.write(b);
        bytesWritten += b.length;
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        out.write(b, off, len);
        bytesWritten += len;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void write(int b) throws IOException {
        out.write(b);
        bytesWritten++;
    }

    /**
     * Get the bytesWritten
     * 
     * @return the bytesWritten
     */
    int getBytesWritten() {
        return bytesWritten;
    }

}
