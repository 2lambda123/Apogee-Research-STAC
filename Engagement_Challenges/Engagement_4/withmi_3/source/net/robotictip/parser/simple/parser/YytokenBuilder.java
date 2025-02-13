package net.robotictip.parser.simple.parser;

public class YytokenBuilder {
    private Object value;
    private int type;

    public YytokenBuilder setValue(Object value) {
        this.value = value;
        return this;
    }

    public YytokenBuilder assignType(int type) {
        this.type = type;
        return this;
    }

    public Yytoken generateYytoken() {
        return new Yytoken(type, value);
    }
}