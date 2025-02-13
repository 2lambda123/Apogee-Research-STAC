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
package infotrader.parser.io.reader;

import java.io.*;
import java.util.List;

import infotrader.parser.exception.InfoTraderParserException;
import infotrader.parser.exception.ParserCancelledException;
import infotrader.parser.exception.UnsupportedInfoTraderCharsetException;
import infotrader.parser.io.event.FileProgressEvent;
import infotrader.parser.parser.InfoTraderParser;

/**
 * An encoding-agnostic class for reading the GEDCOM files and handling ASCII, ANSEL, and UNICODE coding as needed. It's
 * basic job is to turn the bytes from the file into a buffer (a { List} of Strings) that the
 * { infotrader.parser.parser.InfoTraderParser} can work with. This class is needed because the built-in character encodings
 * in Java don't support ANSEL encoding, which is the default encoding for gedcom files in v5.5 standard.
 * 
 * @author frizbog1
 */
public final class InfoTraderFileReader {

    /**
     * The size of the first chunk of the GEDCOM to just load into memory for easy review. 16K.
     */
    private static final int FIRST_CHUNK_SIZE = 16384;

    /**
     * A long constant representing the UTF-8 Byte Order Marker signature, which is the six hex characters EF BB BF.
     */
    private static final long UTF8_BYTE_ORDER_MARKER = 0xEFBBBFL;

    /**
     * The first chunk of the file
     */
    final byte[] firstChunk = new byte[FIRST_CHUNK_SIZE];

    /**
     * The buffered input stream of bytes to read
     */
    private final BufferedInputStream byteStream;

    /**
     * The encoding-specific reader helper class to actually read the bytes
     */
    private final AbstractEncodingSpecificReader encodingSpecificReader;

    /**
     * The { InfoTraderParser} we're reading files for
     */
    private final InfoTraderParser parser;

    /**
     * Number of lines processed
     */
    private int linesProcessed = 0;

    /**
     * Constructor
     * 
     * @param parser
     *            the { InfoTraderParser} which is using this object to read files
     * 
     * @param bufferedInputStream
     *            the buffered input stream of bytes
     * @throws IOException
     *             if there is a problem reading the data
     * @throws UnsupportedInfoTraderCharsetException
     *             if the file is using an unsupported character encoding
     */
    public InfoTraderFileReader(InfoTraderParser parser, BufferedInputStream bufferedInputStream) throws IOException, UnsupportedInfoTraderCharsetException {
        this.parser = parser;
        byteStream = bufferedInputStream;
        saveFirstChunk();
        encodingSpecificReader = getEncodingSpecificReader();
    }

    
    
    /**
     * Get the next line of the file.
     * 
     * @return the next line of the file, or null if no more lines to read.
     * @throws IOException
     *             if there is a problem reading the data
     * @throws InfoTraderParserException
     *             if the file is malformed and cannot be processed as a result
     */
    public String nextLine() throws IOException, InfoTraderParserException {
        if (parser.isCancelled()) {
            throw new ParserCancelledException("File load is cancelled");
        }
        String result = encodingSpecificReader.nextLine();
        linesProcessed++;
        if (linesProcessed % parser.getReadNotificationRate() == 0 || result == null) {
            parser.notifyFileObservers(new FileProgressEvent(this, linesProcessed, encodingSpecificReader.bytesRead, result == null));
        }
        return result;
    }

    /**
     * Return the first n bytes of the array as a single long value, for checking against hex literals
     * 
     * @param n
     *            the number of bytes to grab off the front of the file
     * @return the first n bytes, as an long
     */
    long firstNBytes(int n) {
        long result = 0;
        for (int i = 0; i < n; i++) {
            result = (result << 8) + (firstChunk[i] & 0xFF); // Shift existing bits 8 to the left, and AND in this
            // byte
        }
        return result;
    }

    /**
     * Tries to determined from examining the first 1000 lines/2k of the file if the file is ASCII, ANSEL, or UTF-8
     * encoded using a variety of means.
     * 
     * @return which encoding we think it is
     * @throws IOException
     *             if we have trouble previewing the first 2k of the file
     * @throws UnsupportedInfoTraderCharsetException
     *             if the encoding cannot be narrowed down to Ansel, ASCII, or UTF-8. This could be caused by a number
     *             of things:
     *             <ul>
     *             <li>Illegal value for the CHAR tag</li>
     *             <li>No CHAR tag was found within the first 2k or so of the file</li>
     *             </ul>
     */
    private AbstractEncodingSpecificReader anselAsciiOrUtf8() throws IOException, UnsupportedInfoTraderCharsetException {
        /*
         * Try reading as UTF-8. Most likely to successfully read and be useful for figuring out what the encoding
         * really is
         */
        BufferedReader r = null;
        try {
            r = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(firstChunk), "UTF8"));
            String s = r.readLine();
            int lineCount = 1;
            while (lineCount < 1000 && s != null) {
                lineCount++;
                if (s.startsWith("1 CHAR ")) {
                    String e = s.substring("1 CHAR ".length());
                    if ("ANSEL".equalsIgnoreCase(e)) {
                        return new AnselReader(parser, byteStream);
                    } else if ("UTF-8".equalsIgnoreCase(e)) {
                        return new Utf8Reader(parser, byteStream);
                    } else if ("ASCII".equalsIgnoreCase(e)) {
                        return new AsciiReader(parser, byteStream);
                    } else if ("ANSI".equalsIgnoreCase(e)) {
                        /*
                         * Technically, this is illegal, but UTF_8 is the most-likely-to-work scenario, so let's try it
                         * and be a bit forgiving
                         */
                        return new Utf8Reader(parser, byteStream);
                    } else {
                        throw new UnsupportedInfoTraderCharsetException("Specified charset " + e + " is not a supported charset encoding for InfoTrader");
                    }
                }
                s = r.readLine();
            }

        } finally {
            if (r != null) {
                r.close();
            }
        }
        // All other avenues exhausted, go with an ANSEL reader since that's the default encoding in GEDCOM 5.5
        return new AnselReader(parser, byteStream);
    }

    /**
     * <p>
     * Inspect the first few bytes of the file to determine which encoding is in play, and return an encoding-specific
     * reader to read that data.
     * </p>
     * 
     * @return an { AbstractEncodingSpecificReader} that should work with the data in the byte stream
     * 
     * @throws IOException
     *             if there is a problem reading the byte stream
     * @throws UnsupportedInfoTraderCharsetException
     *             if a suitable charset encoding is not found.
     */
    private AbstractEncodingSpecificReader getEncodingSpecificReader() throws IOException, UnsupportedInfoTraderCharsetException {

        if (firstNBytes(3) == UTF8_BYTE_ORDER_MARKER) {
            /*
             * Special byte order marker to indicate UTF-8 encoding. Not every program does this, but if it does, we
             * KNOW it's UTF-8 and should discard the BOM
             */
            AbstractEncodingSpecificReader result = new Utf8Reader(parser, byteStream);
            ((Utf8Reader) result).setByteOrderMarkerRead(true);
            return result;
        }

        if (firstNBytes(2) == 0xFFFE || firstNBytes(2) == 0x3000 || firstNBytes(2) == 0x0D00 || firstNBytes(2) == 0x0A00) {
            // If the first two firstChunk make up a single zero character, a single line feed character, or a single
            // carriage return character, using the bytes shown, it's unicode little-endian
            return new UnicodeLittleEndianReader(parser, byteStream);
        } else if (firstNBytes(2) == 0xFEFF || firstNBytes(2) == 0x0030 || firstNBytes(2) == 0x000D || firstNBytes(2) == 0x000A) {
            // If the first two firstChunk make up a single zero character, a single line feed character, or a single
            // carriage return character, using the bytes shown, it's unicode big-endian
            return new UnicodeBigEndianReader(parser, byteStream);
        } else {
            boolean zeroFollowedBySpace = firstNBytes(2) == 0x3020;
            boolean blankLineFollowedByZero = firstNBytes(2) == 0x0A30 || firstNBytes(2) == 0x0D30;
            boolean twoBlankLines = firstNBytes(2) == 0x0D0D || firstNBytes(2) == 0x0A0A;
            boolean crlf = firstNBytes(2) == 0x0D0A;
            boolean lfcr = firstNBytes(2) == 0x0A0D;
            if (zeroFollowedBySpace || blankLineFollowedByZero || twoBlankLines || crlf || lfcr) {
                /*
                 * Could be ANSEL, ASCII, or UTF-8. Figure out which
                 */
                return anselAsciiOrUtf8();
            }
            throw new IOException("Does not appear to be a valid InfoTrader file - " + "doesn't begin with a zero or newline in any supported encoding, "
                    + "and does not begin with a BOM marker for UTF-8 encoding. ");
        }

    }

    /**
     * Save off a chunk of the beginning of the input stream to memory for easy inspection. The data is loaded into the
     * field
     * 
     * @throws IOException
     *             if the stream of bytes cannot be read.
     */
    private void saveFirstChunk() throws IOException {
        byteStream.mark(FIRST_CHUNK_SIZE);
        int read = byteStream.read(firstChunk);
        if (read < 0) {
            throw new IOException("Unable to read bytes off stream");
        }
        byteStream.reset();

    }
}
