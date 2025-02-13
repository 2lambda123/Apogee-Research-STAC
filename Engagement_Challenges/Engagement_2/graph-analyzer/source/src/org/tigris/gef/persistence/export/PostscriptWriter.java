package org.tigris.gef.persistence.export;

import java.io.*;
import java.util.Hashtable;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.image.*;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import org.tigris.gef.persistence.ScalableGraphics;

public class PostscriptWriter extends ScalableGraphics {

    private PrintWriter p;

    private boolean autoClose = false;

    private static final String ellipseDef = "%%BeginProcSet: ellipse 1.0 0 \n"
            + "/ellipsedict 8 dict def \n" + "ellipsedict /mtrx matrix put \n"
            + "/ellipse { ellipsedict begin \n" + "/endangle exch def \n"
            + "/startangle exch def \n" + "/yrad exch def \n"
            + "/xrad exch def \n" + "/y exch def \n" + "/x exch def \n"
            + "/savematrix mtrx currentmatrix def \n" + "x y translate \n"
            + "xrad yrad scale \n" + "0 0 1 0 360 arc \n"
            + "savematrix setmatrix end } def \n"
            + "%%EndProcSet: ellipse 1.0 0 \n";

    private static final String reencodeDef = "%%BeginProcSet: reencode 1.0 0 \n"
            + "/RE \n"
            + "{  findfont begin \n"
            + "  currentdict dup length dict begin \n"
            + "  {1 index /FID ne {def} {pop pop} ifelse} forall \n"
            + "  /FontName exch def dup length 0 ne \n"
            + "  { /Encoding Encoding 256 array copy def \n"
            + "      0 exch \n"
            + "      { dup type /nametype eq \n"
            + "        { Encoding 2 index 2 index put \n"
            + "          pop 1 add \n"
            + "        } \n"
            + "        { exch pop \n"
            + "        } ifelse \n"
            + "      } forall \n"
            + "  } if pop \n"
            + "  currentdict dup end end \n"
            + "  /FontName get exch definefont pop \n"
            + "    } bind def \n"
            + "%%EndProcSet: reencode 1.0 0 \n";

    private static final String isolatin1encoding = "/isolatin1encoding \n"
            + "[ 32 /space /exclam /quotedbl /numbersign /dollar /percent /ampersand /quoteright \n"
            + " /parenleft /parenright /asterisk /plus /comma /hyphen /period /slash /zero /one \n"
            + " /two /three /four /five /six /seven /eight /nine /colon /semicolon \n"
            + " /less /equal /greater /question /at /A /B /C /D /E \n"
            + " /F /G /H /I /J /K /L /M /N /O \n"
            + " /P /Q /R /S /T /U /V /W /X /Y \n"
            + " /Z /bracketleft /backslash /bracketright /asciicircum /underscore /quoteleft /a /b /c \n"
            + " /d /e /f /g /h /i /j /k /l /m \n"
            + " /n /o /p /q /r /s /t /u /v /w \n"
            + " /x /y /z /braceleft /bar /braceright /asciitilde /.notdef /.notdef /.notdef \n"
            + " /.notdef /.notdef /.notdef /.notdef /.notdef /.notdef /.notdef /.notdef /.notdef /.notdef \n"
            + " /.notdef /.notdef /.notdef /.notdef /.notdef /.notdef /.notdef /.notdef /.notdef /.notdef \n"
            + " /.notdef /.notdef /.notdef /.notdef /.notdef /.notdef /.notdef /.notdef /.notdef /.notdef \n"
            + " /space /exclamdown /cent /sterling /currency /yen /brokenbar /section /dieresis /copyright \n"
            + " /ordfeminine /guillemotleft /logicalnot /hyphen /registered /macron /degree /plusminus /twosuperior /threesuperior \n"
            + " /acute /mu /paragraph /periodcentered /cedilla /onesuperior /ordmasculine /guillemotright /onequarter /onehalf \n"
            + " /threequarters /questiondown /Agrave /Aacute /Acircumflex /Atilde /Adieresis /Aring /AE /Ccedilla \n"
            + " /Egrave /Eacute /Ecircumflex /Edieresis /Igrave /Iacute /Icircumflex /Idieresis /Eth /Ntilde \n"
            + " /Ograve /Oacute /Ocircumflex /Otilde /Odieresis /multiply /Oslash /Ugrave /Uacute /Ucircumflex \n"
            + " /Udieresis /Yacute /Thorn /germandbls /agrave /aacute /acircumflex /atilde /adieresis /aring \n"
            + " /ae /ccedilla /egrave /eacute /ecircumflex /edieresis /igrave /iacute /icircumflex /idieresis \n"
            + " /eth /ntilde /ograve /oacute /ocircumflex /otilde /odieresis /divide /oslash /ugrave \n"
            + " /uacute /ucircumflex /udieresis /yacute /thorn /ydieresis] def \n";

    private Color fColor = null;

    private Font fFont = null;

    private Rectangle clip;

    private Hashtable fontmap = new Hashtable();

    private Hashtable colormap = new Hashtable();

    // Dash and line joint/cap styles (aka Stroke) -- aslo
    private java.awt.BasicStroke stroke = null;

    // last used stroke, so we don't set it two times
    private String lastStroke = "[] 0 setdash";

    public PostscriptWriter(String filename) throws IOException {
        this(filename, null);
    }

    public PostscriptWriter(String filename, Rectangle boundingBox)
            throws IOException {
        this(new FileOutputStream(filename), boundingBox);
        autoClose = true;
    }

    public PostscriptWriter(OutputStream stream) throws IOException {
        this(stream, null);
    }

    public PostscriptWriter(OutputStream stream, Rectangle bb)
            throws IOException {
        fontmap.put("Dialog", "Helvetica");
        fontmap.put("SansSerif", "Helvetica");
        fontmap.put("DialogInput", "Monospaced");
        p = new PrintWriter(new OutputStreamWriter(stream, "ISO8859-1"));
        if (bb == null) {
            p.println("%!PS-Adobe-3.0");
        } else {
            p.println("%!PS-Adobe-3.0 EPSF-3.0");
            p.println("%%BoundingBox: " + bb.x + " " + bb.y + " "
                    + (bb.x + bb.width) + " " + (bb.y + bb.height));
        }
        p.print(reencodeDef);
        p.print(ellipseDef);
        p.println("%%EndProlog");
        p.println("%%BeginSetup");
        p.print(isolatin1encoding);
        p.println("%%EndSetup");
        p.println("1 setlinewidth");
        setFont(new Font("Helvetica", Font.PLAIN, 12));
        setColor(Color.black);
        if (bb != null) {
            translate(0, bb.height + 2 * bb.y);
        }
    }

    public Graphics create() {
        return this;
    }

    public Graphics create(int x, int y, int width, int height) {
        return this;
    }

    public void dispose() {
        p.println("showpage");
        p.println("%%Trailer");
        if (autoClose) {
            p.close();
        } else {
            p.flush();
        }
    }

    public void setColorConversion(Color source, Color target) {
        colormap.put(source, target);
    }

    public Color getColor() {
        return fColor;
    }

    public void setColor(Color c) {
        Color replaceColor = (Color) colormap.get(c);
        if (replaceColor != null) {
            c = replaceColor;
        }
        if (c.equals(fColor)) {
            return;
        }
        fColor = c;
        final float maxColor = 255;
        p.print(((float) c.getRed()) / maxColor + " ");
        p.print(((float) c.getGreen()) / maxColor + " ");
        p.print(((float) c.getBlue()) / maxColor + " ");
        p.println("setrgbcolor");
    }

    public void setPaintMode() {
    }

    public void setXORMode(Color otherColor) {
    }

    public Font getFont() {
        return fFont;
    }

    public void setFont(Font font) {
        if (!font.equals(fFont)) {
            fFont = font;
            FontMetrics metrics = getFontMetrics();
            String name = font.getName();
            if (fontmap.containsKey(name)) {
                name = (String) fontmap.get(name);
            }
            if (font.isBold() || font.isItalic()) {
                name += "-";
                if (font.isBold()) {
                    name += "Bold";
                }
                if (font.isItalic()) {
                    name += "Oblique";
                }
            }

            p.println("isolatin1encoding /_" + name + " /" + name + " RE");
            p.println("/_" + name + " findfont");
            p.println(font.getSize() + " scalefont setfont");
        }
    }

    public FontMetrics getFontMetrics() {
        return getFontMetrics(fFont);
    }

    public FontMetrics getFontMetrics(Font font) {
        return FontUtility.getFontMetrics(font);
    }

    public java.awt.Rectangle getClipBounds() {
        return clip;
    }

    public void clipRect(int x, int y, int w, int h) {
        setClip(x, y, w, h);
    }

    public Shape getClip() {
        return clip;
    }

    public void copyArea(int x, int y, int width, int height, int dx, int dy) {
    }

    public boolean drawImage(Image img, int x, int y, ImageObserver observer) {
        return false;
    }

    private void handlesinglepixel(int x, int y, int pixel) {
        if (((pixel >> 24) & 0xff) == 0) {
            // should be transparent, is printed white:
            pixel = 0xffffff;
        }
        p.print(Integer.toHexString((pixel >> 20) & 0x0f)
                + Integer.toHexString((pixel >> 12) & 0x0f)
                + Integer.toHexString((pixel >> 4) & 0x0f));
    }

    public boolean drawImage(Image img, int x, int y, int w, int h,
            ImageObserver observer) {
        int iw = img.getWidth(observer), ih = img.getHeight(observer);

        p.println("gsave");
        writeCoords(x, y + h);
        p.println("translate");
        writeCoords(w, -h);
        p.println("scale");
        p.println("/DatenString " + iw + " string def");
        writeCoords(iw, -ih);
        p.println("4 [" + iw + " 0 0 " + (-ih) + " 0 " + ih + "]");
        p.println("{currentfile DatenString readhexstring pop} bind");
        p.println("false 3 colorimage");

        int[] pixels = new int[iw * ih];
        PixelGrabber pg = new PixelGrabber(img, 0, 0, iw, ih, pixels, 0, iw);
        // pg.setColorModel(Toolkit.getDefaultToolkit().getColorModel());
        try {
            pg.grabPixels();
        } catch (InterruptedException e) {
            System.err.println("interrupted waiting for pixels!");
            return false;
        }
        if ((pg.getStatus() & ImageObserver.ABORT) != 0) {
            System.err.println("image fetch aborted or errored");
            return false;
        }
        for (int j = 0; j < ih; j++) {
            for (int i = 0; i < iw; i++) {
                handlesinglepixel(i, j, pixels[j * iw + i]);
            }
            if (iw % 2 == 1) {
                p.print("0");
            }
            p.println();
        }
        if (ih % 2 == 1) {
            for (int i = 0; i < 3 * (iw + iw % 2); i++) {
                p.print("0");
            }
            p.println();
        }
        p.println("grestore");
        return true;
    }

    public boolean drawImage(Image img, int x, int y, Color bgcolor,
            ImageObserver observer) {
        return false;
    }

    public boolean drawImage(Image img, int x, int y, int width, int height,
            Color bgcolor, ImageObserver observer) {
        return false;
    }

    public boolean drawImage(Image img, int dx1, int dy1, int dx2, int dy2,
            int sx1, int sy1, int sx2, int sy2, ImageObserver observer) {
        return false;
    }

    public boolean drawImage(Image img, int dx1, int dy1, int dx2, int dy2,
            int sx1, int sy1, int sx2, int sy2, Color bgcolor,
            ImageObserver observer) {
        return false;
    }

    private void writeSetDash() {
        String result = "[] 0 setdash";
        if (stroke != null) {
            float[] dash = stroke.getDashArray();
            if (dash != null) {
                StringBuffer ps = new StringBuffer();
                ps.append('[');
                for (int i = 0; i < dash.length; i++) {
                    ps.append(dash[i]).append(' ');
                }
                ps.append("] 0 setdash");
                result = ps.toString();
            }
        }
        // doit only if necessary
        if (!lastStroke.equals(result)) {
            lastStroke = result;
            p.println(result);
        }
    }

    private void writeCoords(int x, int y) {
        p.print(x + " " + (-y) + " ");
    }

    private void writeRectanglePath(int x, int y, int w, int h) {
        p.println("newpath");
        writeCoords(x, y);
        p.println("moveto");
        writeCoords(w - 1, 0);
        p.println("rlineto");
        writeCoords(0, h - 1);
        p.println("rlineto");
        writeCoords(-(w - 1), 0);
        p.println("rlineto");
        p.println("closepath");
    }

    public void drawRect(int x, int y, int w, int h) {
        writeRectanglePath(x, y, w + 1, h + 1);
        p.println("stroke");
    }

    public void fillRect(int x, int y, int w, int h) {
        writeRectanglePath(x, y, w, h);
        p.println("eofill");
    }

    public void clearRect(int x, int y, int w, int h) {
        writeRectanglePath(x, y, w, h);
        setColor(Color.white);
        p.println("eofill");
    }

    private void writeEllipsePath(int x, int y, int w, int h, int startAngle,
            int arcAngle) {
        writeSetDash();
        p.println("newpath");
        int dx = w / 2, dy = h / 2;
        writeCoords(x + dx, y + dy);
        writeCoords(dx, dy);
        writeCoords(startAngle, -(startAngle + arcAngle));
        p.println("ellipse");

    }

    public void drawOval(int x, int y, int w, int h) {
        writeEllipsePath(x, y, w + 1, h + 1, 0, 360);
        p.println("stroke");
    }

    public void fillOval(int x, int y, int w, int h) {
        writeEllipsePath(x, y, w, h, 0, 360);
        p.println("eofill");
    }

    public void drawArc(int x, int y, int w, int h, int startAngle, int arcAngle) {
        writeEllipsePath(x, y, w + 1, h + 1, startAngle, arcAngle);
        p.println("stroke");
    }

    public void fillArc(int x, int y, int w, int h, int startAngle, int arcAngle) {
        writeEllipsePath(x, y, w, h, startAngle, arcAngle);
        p.println("eofill");
    }

    private void writeRoundRectPath(int x, int y, int w, int h, int arcw,
            int arch) {
        String curve = (Math.min(arcw, arch)) + " arcto 4 {pop} repeat";
        writeSetDash();
        p.println("newpath");
        writeCoords(x, y + arch);
        p.println("moveto");
        writeCoords(x, y);
        writeCoords(x + arcw, y);
        p.println(curve);
        writeCoords(x + w, y);
        writeCoords(x + w, y + arch);
        p.println(curve);
        writeCoords(x + w, y + h);
        writeCoords(x + w - arcw, y + h);
        p.println(curve);
        writeCoords(x, y + h);
        writeCoords(x, y + h - arch);
        p.println(curve);
        p.println("closepath");
    }

    public void drawRoundRect(int x, int y, int w, int h, int arcw, int arch) {
        writeRoundRectPath(x, y, w + 1, h + 1, arcw, arch);
        p.println("stroke");
    }

    public void fillRoundRect(int x, int y, int w, int h, int arcw, int arch) {
        writeRoundRectPath(x, y, w, h, arcw, arch);
        p.println("eofill");
    }

    private void writePolyLinePath(int xPoints[], int yPoints[], int nPoints) {
        writeSetDash();
        p.println("newpath");
        for (int i = 0; i < nPoints; ++i) {
            writeCoords(xPoints[i], yPoints[i]);
            if (i == 0) {
                p.println("moveto");
            } else {
                p.println("lineto");
            }
        }
    }

    public void writePolygonPath(int xPoints[], int yPoints[], int nPoints) {
        writePolyLinePath(xPoints, yPoints, nPoints);
        p.println("closepath");
    }

    public void drawPolygon(int xPoints[], int yPoints[], int nPoints) {
        writePolygonPath(xPoints, yPoints, nPoints);
        p.println("stroke");
    }

    public void drawPolygon(Polygon poly) {
        drawPolygon(poly.xpoints, poly.ypoints, poly.npoints);
    }

    public void fillPolygon(int xPoints[], int yPoints[], int nPoints) {
        writePolygonPath(xPoints, yPoints, nPoints);
        p.println("eofill");
    }

    public void fillPolygon(Polygon poly) {
        fillPolygon(poly.xpoints, poly.ypoints, poly.npoints);
    }

    public void drawPolyline(int xPoints[], int yPoints[], int nPoints) {
        writePolyLinePath(xPoints, yPoints, nPoints);
        p.println("stroke");
    }

    public void drawLine(int x1, int y1, int x2, int y2) {
        writeSetDash();
        p.println("newpath");
        writeCoords(x1, y1);
        p.println("moveto");
        writeCoords(x2, y2);
        p.println("lineto");
        p.println("stroke");
    }

    public void setClip(int x, int y, int w, int h) {
        clip = new Rectangle(x, y, w, h);
        writeRectanglePath(x, y, w, h);
        p.println("clip");
    }

    public void setClip(Shape clip) {
        setClip(clip.getBounds());
    }

    public void translate(int x, int y) {
        writeCoords(x, -y);
        p.println("translate");
    }

    public void scale(double xscale, double yscale) {
        p.println(xscale + " " + yscale + " scale");
    }

    public void drawString(String text, int x, int y) {
        StringBuffer buf = new StringBuffer(text);
        int c;
        String code;
        for (int i = 0; i < buf.length(); ++i) {
            c = (int) buf.charAt(i);
            if (c >= 192 && c < 256) {
                buf.setCharAt(i, '\\');
                code = Integer.toOctalString(c);
                buf.insert(i + 1, code);
                i += code.length();
            } else if (c == '\\' || c == '(' || c == ')') {
                buf.insert(i++, '\\');
            }
        }
        writeCoords(x, y);
        p.println("moveto");
        p.println("(" + buf.toString() + ") show");
    }

    public void comment(String cmt) {
        p.println("% " + cmt);
    }

    // For Java 1.2 uncomment the following lines:
    //
    public void drawString(java.text.AttributedCharacterIterator iterator,
            int i, int j) {
        throw new RuntimeException("Not supported.");
    }

    public void addRenderingHints(java.util.Map map) {
    }

    public void clip(java.awt.Shape shape) {
    }

    public void draw(java.awt.Shape shape) {

        if (shape instanceof Ellipse2D) {
            Ellipse2D e = (Ellipse2D) shape;
            drawOval((int) e.getX(), (int) e.getY(), (int) e.getWidth(), (int) e.getHeight());
        }

        if (shape instanceof Line2D) {
            Line2D e = (Line2D) shape;
            drawLine((int) e.getP1().getX(), (int) e.getP1().getY(), (int) e.getP2().getX(), (int) e.getP2().getY());
        }

    }

    public void drawGlyphVector(java.awt.font.GlyphVector glyphVector,
            float param, float param2) {
    }

    public boolean drawImage(java.awt.Image image,
            java.awt.geom.AffineTransform affineTransform,
            java.awt.image.ImageObserver imageObserver) {
        return false;
    }

    public void drawImage(java.awt.image.BufferedImage bufferedImage,
            java.awt.image.BufferedImageOp bufferedImageOp, int param,
            int param3) {
    }

    public void drawRenderableImage(
            java.awt.image.renderable.RenderableImage renderableImage,
            java.awt.geom.AffineTransform affineTransform) {
    }

    public void drawRenderedImage(java.awt.image.RenderedImage renderedImage,
            java.awt.geom.AffineTransform affineTransform) {
    }

    public void drawString(
            java.text.AttributedCharacterIterator attributedCharacterIterator,
            float param, float param2) {
    }

    public void drawString(String str, float param, float param2) {
    }

    public void fill(java.awt.Shape shape) {
    }

    public java.awt.Color getBackground() {
        return null;
    }

    public java.awt.Composite getComposite() {
        return null;
    }

    public java.awt.GraphicsConfiguration getDeviceConfiguration() {
        return null;
    }

    public java.awt.font.FontRenderContext getFontRenderContext() {
        return null;
    }

    public java.awt.Paint getPaint() {
        return null;
    }

    public Object getRenderingHint(java.awt.RenderingHints.Key key) {
        return null;
    }

    public java.awt.RenderingHints getRenderingHints() {
        return null;
    }

    public java.awt.Stroke getStroke() {
        return stroke;
    }

    public java.awt.geom.AffineTransform getTransform() {
        return null;
    }

    public boolean hit(java.awt.Rectangle rectangle, java.awt.Shape shape,
            boolean param) {
        return false;
    }

    public void rotate(double param) {
    }

    public void rotate(double param, double param1, double param2) {
    }

    public void setBackground(java.awt.Color color) {
    }

    public void setComposite(java.awt.Composite composite) {
    }

    public void setPaint(java.awt.Paint paint) {
    }

    public void setRenderingHint(java.awt.RenderingHints.Key key, Object obj) {
    }

    public void setRenderingHints(java.util.Map map) {
    }

    public void setStroke(java.awt.Stroke stroke) {
        try {
            this.stroke = (BasicStroke) stroke;
        } catch (ClassCastException e) {
            this.stroke = null;
        }
    }

    public void setTransform(java.awt.geom.AffineTransform affineTransform) {
    }

    public void shear(double param, double param1) {
    }

    public void transform(java.awt.geom.AffineTransform affineTransform) {
    }

    public void translate(double param, double param1) {
    }

    @Override
    public void setScale(double s) {
        DecimalFormat df = new DecimalFormat("0", DecimalFormatSymbols.getInstance(Locale.ENGLISH));
        df.setMaximumFractionDigits(340);
        p.println(df.format(s) + " " + df.format(s) + " scale");
    }

}
