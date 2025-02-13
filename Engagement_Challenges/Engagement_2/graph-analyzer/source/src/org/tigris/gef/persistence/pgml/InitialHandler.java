// $Id: InitialHandler.java 1153 2008-11-30 16:14:45Z bobtarling $
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

import java.io.ByteArrayInputStream;
import java.net.URL;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import org.xml.sax.helpers.DefaultHandler;

/**
 * The default intitial content handler used by PGMLStackParser, corresponding
 * to the whole PGML document.
 */
public class InitialHandler extends DefaultHandler {

    /**
     * The parser for this handler.
     */
    private PGMLStackParser parser;

    /**
     * @param theParser The PGMLStackParser object associated with the diagram
     * that is to be read.
     */
    InitialHandler(PGMLStackParser theParser) {
        parser = theParser;
    }

    /**
     * Creates a PGMLHandler object to parse a PGML element corresponding to a
     * diagram in a PGML file.
     *
     * @see org.xml.sax.ContentHandler#startElement( java.lang.String,
     * java.lang.String, java.lang.String, org.xml.sax.Attributes)
     */
    public void startElement(String uri, String localname, String qname,
            Attributes attributes) throws SAXException {
        if (qname.equals("pgml")) {
            parser.pushHandlerStack(new PGMLHandler(parser, attributes));
        }
    }

    public InputSource resolveEntity(String publicId, String systemId) {
        if (systemId.endsWith("pgml.dtd")) {
            URL dtdUrl = this.getClass().getResource(
                    "/org/tigris/gef/xml/dtd/pgml.dtd");
            if (dtdUrl != null) {
                return new InputSource(dtdUrl.toExternalForm());
            } else {
                // Return empty DTD if it can't be found
                return new InputSource(new ByteArrayInputStream(
                        "<?xml version='1.0' encoding='UTF-8'?>".getBytes()));
            }
        } else {
            return null;
        }
    }
}
