// $Id: BaseHandler.java 1153 2008-11-30 16:14:45Z bobtarling $
// Copyright (c) 1996-2006 The Regents of the University of California. All
// Rights Reserved. Permission to use, copy, modify, and distribute this
// software and its documentation without fee, and without a written
// agreement is hereby granted, provided that the above copyright notice
// and this paragraph appear in all copies.  This software program and
// documentation are copyrighted by The Regents of the University of
// California. The software program and documentation are supplied "AS
// IS", without any accompanying services from The Regents. The Regents
// does not warrant that the operation of the program will be
// uninterrupted or error-free. The end-user understands that the program
// was developed for research purposes and is advised not to rely
// exclusively on the program for any reason.  IN NO EVENT SHALL THE
// UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY FOR DIRECT, INDIRECT,
// SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES, INCLUDING LOST PROFITS,
// ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
// THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF
// SUCH DAMAGE. THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY
// WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
// MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
// PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
// CALIFORNIA HAS NO OBLIGATIONS TO PROVIDE MAINTENANCE, SUPPORT,
// UPDATES, ENHANCEMENTS, OR MODIFICATIONS.
package org.tigris.gef.persistence.pgml;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import org.xml.sax.helpers.DefaultHandler;

/**
 * Implements a SAX ContentHandler that contains a reference to an object
 * derived from PGMLStackParser and uses that object to get handlers for
 * sub-elements. Classes that know how to parse elements appearing in a PGML
 * file are derived from this class.
 * <p>
 *
 * TODO: Investigate if this could be renamed AbstractHandler?
 *
 * @see PGMLStackParser
 * @author Michael A. MacDonald
 */
public abstract class BaseHandler extends DefaultHandler {

    /**
     * Accumulates all the characters that the SAX parser has seen for this
     * element and provided in the {@link #characters} call.
     */
    private StringBuffer elementContents;
    private PGMLStackParser parser;

    // ////////////////////////////////////////////////////////////////////////
    // Constructors
    /**
     * Constructor.
     *
     * @param theParser The parser for this handler.
     */
    public BaseHandler(PGMLStackParser theParser) {
        elementContents = new StringBuffer();
        parser = theParser;
    }

    // ////////////////////////////////////////////////////////////////////////
    // Public interface
    /**
     * @return The parser for this handler.
     */
    public PGMLStackParser getPGMLStackParser() {
        return parser;
    }

    /**
     * Override this to do processing when we have all the information about the
     * element.
     *
     * @param contents The text contents of the element.
     * @throws SAXException on error
     */
    public void gotElement(String contents) throws SAXException {
    }

    /**
     * Called to obtain a handler for a sub-element. The default implementation
     * is to call getElementHandler. If getElementHandler can find a handler, it
     * returns that; otherwise, it returns an UnknownHandler which will skip
     * over the element (and any contained sub-elements) without doing anything.
     * The only reason to override this would be to customize the behavior for
     * an unknown element.
     *
     * @see UnknownHandler
     * @see #getElementHandler
     * @param stack
     * @param container
     * @param uri
     * @param localname
     * @param qname
     * @param attributes
     * @return DefaultHandler implementation appropriate for the element.
     * @throws SAXException
     */
    protected DefaultHandler getElementOrUnknownHandler(HandlerStack stack,
            Object container, String uri, String localname, String qname,
            Attributes attributes) throws SAXException {

        DefaultHandler result = getElementHandler(stack, container, uri,
                localname, qname, attributes);

        if (result == null) {
            result = new UnknownHandler(getPGMLStackParser());
        }
        return result;
    }

    /**
     * Called to return a SAX handler appropriate for an element. The default
     * implementation calls the getHandler method of the BaseHandler's
     * PGMLStackParser object. If no appropriate handler is found for the
     * element, returns null.
     *
     * @param stack
     * @param container
     * @param uri
     * @param localname
     * @param qname
     * @param attributes
     * @return DefaultHandler implementation appropriate for the element.
     * @throws SAXException
     */
    protected DefaultHandler getElementHandler(HandlerStack stack,
            Object container, String uri, String localname, String qname,
            Attributes attributes) throws SAXException {

        return getPGMLStackParser().getHandler(stack, container, uri,
                localname, qname, attributes);
    }

    // ////////////////////////////////////////////////////////////////////////
    // ContentHandler implementation
    /**
     * @see org.xml.sax.ContentHandler#startElement( java.lang.String,
     * java.lang.String, java.lang.String, org.xml.sax.Attributes)
     */
    public void startElement(String uri, String localname, String qname,
            Attributes attributes) throws SAXException {
        DefaultHandler handler = getElementOrUnknownHandler(
                getPGMLStackParser(), this, uri, localname, qname, attributes);
        getPGMLStackParser().pushHandlerStack(handler);
    }

    /**
     * @see org.xml.sax.ContentHandler#endElement( java.lang.String,
     * java.lang.String, java.lang.String)
     */
    public void endElement(String uri, String localname, String qname)
            throws SAXException {
        getPGMLStackParser().popHandlerStack();
        gotElement(elementContents.toString());
    }

    /**
     * @see org.xml.sax.ContentHandler#characters(char[], int, int)
     */
    public void characters(char[] ch, int start, int length) {
        elementContents.append(ch, start, length);
    }

    /**
     * @see org.xml.sax.ContentHandler#ignorableWhitespace(char[], int, int)
     */
    public void ignorableWhitespace(char[] ch, int start, int length) {
        elementContents.append(ch, start, length);
    }
}
