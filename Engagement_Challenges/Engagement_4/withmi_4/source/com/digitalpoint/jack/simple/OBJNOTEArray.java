/*
 * $Id: JSONArray.java,v 1.1 2006/04/15 14:10:48 platform Exp $
 * Created on 2006-4-10
 */
package com.digitalpoint.jack.simple;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 * A JSON array. JSONObject supports java.util.List interface.
 * 
 * @author FangYidong<fangyidong@yahoo.com.cn>
 */
public class OBJNOTEArray extends ArrayList implements List, OBJNOTEAware, OBJNOTEStreamAware {
	private static final long serialVersionUID = 3957988303675231981L;

    /**
     * Encode a list into JSON text and write it to out. 
     * If this list is also a JSONStreamAware or a JSONAware, JSONStreamAware and JSONAware specific behaviours will be ignored at this top level.
     * 
     * @see OBJNOTEValue#writeOBJNOTEString(Object, Writer)
     * 
     * @param list
     * @param out
     */
	public static void writeOBJNOTEString(List list, Writer out) throws IOException{
		if(list == null){
			out.write("null");
			return;
		}
		
		boolean first = true;
		Iterator iter=list.iterator();
		
        out.write('[');
		while(iter.hasNext()){
            if(first)
                first = false;
            else
                out.write(',');
            
			Object value=iter.next();
			if(value == null){
                writeOBJNOTEStringHerder(out);
                continue;
			}
			
			OBJNOTEValue.writeOBJNOTEString(value, out);
		}
		out.write(']');
	}

    private static void writeOBJNOTEStringHerder(Writer out) throws IOException {
        out.write("null");
        return;
    }

    public void writeOBJNOTEString(Writer out) throws IOException{
		writeOBJNOTEString(this, out);
	}
	
	/**
	 * Convert a list to JSON text. The result is a JSON array. 
	 * If this list is also a JSONAware, JSONAware specific behaviours will be omitted at this top level.
	 * 
	 * @see OBJNOTEValue#toOBJNOTEString(Object)
	 * 
	 * @param list
	 * @return JSON text, or "null" if list is null.
	 */
	public static String toOBJNOTEString(List list){
		if(list == null)
			return "null";
		
        boolean first = true;
        StringBuffer sb = new StringBuffer();
		Iterator iter=list.iterator();
        
        sb.append('[');
		while(iter.hasNext()){
            if(first)
                first = false;
            else
                sb.append(',');
            
			Object value=iter.next();
			if(value == null){
				sb.append("null");
				continue;
			}
			sb.append(OBJNOTEValue.toOBJNOTEString(value));
		}
        sb.append(']');
		return sb.toString();
	}

	public String toOBJNOTEString(){
		return toOBJNOTEString(this);
	}
	
	public String toString() {
		return toOBJNOTEString();
	}

	
		
}
