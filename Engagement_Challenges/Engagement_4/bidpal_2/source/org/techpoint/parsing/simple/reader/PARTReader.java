/*
 * $Id: JSONParser.java,v 1.1 2006/04/15 14:10:48 platform Exp $
 * Created on 2006-4-15
 */
package org.techpoint.parsing.simple.reader;

import org.techpoint.parsing.simple.PARTArray;
import org.techpoint.parsing.simple.PARTObject;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


/**
 * Parser for JSON text. Please note that JSONParser is NOT thread-safe.
 * 
 * @author FangYidong<fangyidong@yahoo.com.cn>
 */
public class PARTReader {
	public static final int S_INIT=0;
	public static final int S_IN_FINISHED_VALUE=1;//string,number,boolean,null,object,array
	public static final int S_IN_OBJECT=2;
	public static final int S_IN_ARRAY=3;
	public static final int S_PASSED_PAIR_KEY=4;
	public static final int S_IN_PAIR_VALUE=5;
	public static final int S_END=6;
	public static final int S_IN_ERROR=-1;
	
	private LinkedList managerStatusStack;
	private Yylex lexer = new Yylex((Reader)null);
	private Yytoken token = null;
	private int status = S_INIT;
	
	private int peekStatus(LinkedList statusStack){
		if(statusStack.size()==0)
			return -1;
		Integer status=(Integer)statusStack.getFirst();
		return status.intValue();
	}
	
    /**
     *  Reset the parser to the initial state without resetting the underlying reader.
     *
     */
    public void reset(){
        token = null;
        status = S_INIT;
        managerStatusStack = null;
    }
    
    /**
     * Reset the parser to the initial state with a new character reader.
     * 
     * @param in - The new character reader.
     * @throws IOException
     * @throws ParseRaiser
     */
	public void reset(Reader in){
		lexer.yyreset(in);
		reset();
	}
	
	/**
	 * @return The position of the beginning of the current token.
	 */
	public int fetchPosition(){
		return lexer.grabPosition();
	}
	
	public Object parse(String s) throws ParseRaiser {
		return parse(s, (ContainerFactory)null);
	}
	
	public Object parse(String s, ContainerFactory containerFactory) throws ParseRaiser {
		StringReader in=new StringReader(s);
		try{
			return parse(in, containerFactory);
		}
		catch(IOException ie){
			/*
			 * Actually it will never happen.
			 */
			throw new ParseRaiser(-1, ParseRaiser.ERROR_UNEXPECTED_EXCEPTION, ie);
		}
	}
	
	public Object parse(Reader in) throws IOException, ParseRaiser {
		return parse(in, (ContainerFactory)null);
	}
	
	/**
	 * Parse JSON text into java object from the input source.
	 * 	
	 * @param in
     * @param containerFactory - Use this factory to createyour own JSON object and JSON array containers.
	 * @return Instance of the following:
	 *  org.json.simple.JSONObject,
	 * 	org.json.simple.JSONArray,
	 * 	java.lang.String,
	 * 	java.lang.Number,
	 * 	java.lang.Boolean,
	 * 	null
	 * 
	 * @throws IOException
	 * @throws ParseRaiser
	 */
	public Object parse(Reader in, ContainerFactory containerFactory) throws IOException, ParseRaiser {
		reset(in);
		LinkedList statusStack = new LinkedList();
		LinkedList valueStack = new LinkedList();
		
		try{
			do{
				nextToken();
				switch(status){
				case S_INIT:
					switch(token.type){
					case Yytoken.TYPE_VALUE:
						status=S_IN_FINISHED_VALUE;
						statusStack.addFirst(new Integer(status));
						valueStack.addFirst(token.value);
						break;
					case Yytoken.TYPE_LEFT_BRACE:
						status=S_IN_OBJECT;
						statusStack.addFirst(new Integer(status));
						valueStack.addFirst(composeObjectContainer(containerFactory));
						break;
					case Yytoken.TYPE_LEFT_SQUARE:
						status=S_IN_ARRAY;
						statusStack.addFirst(new Integer(status));
						valueStack.addFirst(composeArrayContainer(containerFactory));
						break;
					default:
						status=S_IN_ERROR;
					}//inner switch
					break;
					
				case S_IN_FINISHED_VALUE:
					if(token.type==Yytoken.TYPE_EOF)
						return valueStack.removeFirst();
					else
						throw new ParseRaiser(fetchPosition(), ParseRaiser.ERROR_UNEXPECTED_TOKEN, token);
					
				case S_IN_OBJECT:
					switch(token.type){
					case Yytoken.TYPE_COMMA:
						break;
					case Yytoken.TYPE_VALUE:
						if(token.value instanceof String){
							String key=(String)token.value;
							valueStack.addFirst(key);
							status=S_PASSED_PAIR_KEY;
							statusStack.addFirst(new Integer(status));
						}
						else{
                            parseManager();
                        }
						break;
					case Yytoken.TYPE_RIGHT_BRACE:
						if(valueStack.size()>1){
							statusStack.removeFirst();
							valueStack.removeFirst();
							status=peekStatus(statusStack);
						}
						else{
							status=S_IN_FINISHED_VALUE;
						}
						break;
					default:
						status=S_IN_ERROR;
						break;
					}//inner switch
					break;
					
				case S_PASSED_PAIR_KEY:
					switch(token.type){
					case Yytoken.TYPE_COLON:
						break;
					case Yytoken.TYPE_VALUE:
						statusStack.removeFirst();
						String key=(String)valueStack.removeFirst();
						Map parent=(Map)valueStack.getFirst();
						parent.put(key,token.value);
						status=peekStatus(statusStack);
						break;
					case Yytoken.TYPE_LEFT_SQUARE:
						statusStack.removeFirst();
						key=(String)valueStack.removeFirst();
						parent=(Map)valueStack.getFirst();
						List newArray= composeArrayContainer(containerFactory);
						parent.put(key,newArray);
						status=S_IN_ARRAY;
						statusStack.addFirst(new Integer(status));
						valueStack.addFirst(newArray);
						break;
					case Yytoken.TYPE_LEFT_BRACE:
						statusStack.removeFirst();
						key=(String)valueStack.removeFirst();
						parent=(Map)valueStack.getFirst();
						Map newObject= composeObjectContainer(containerFactory);
						parent.put(key,newObject);
						status=S_IN_OBJECT;
						statusStack.addFirst(new Integer(status));
						valueStack.addFirst(newObject);
						break;
					default:
						status=S_IN_ERROR;
					}
					break;
					
				case S_IN_ARRAY:
					switch(token.type){
					case Yytoken.TYPE_COMMA:
						break;
					case Yytoken.TYPE_VALUE:
						List val=(List)valueStack.getFirst();
						val.add(token.value);
						break;
					case Yytoken.TYPE_RIGHT_SQUARE:
						if(valueStack.size()>1){
                            parseAdviser(statusStack, valueStack);
                        }
						else{
                            parseEntity();
                        }
						break;
					case Yytoken.TYPE_LEFT_BRACE:
						val=(List)valueStack.getFirst();
						Map newObject= composeObjectContainer(containerFactory);
						val.add(newObject);
						status=S_IN_OBJECT;
						statusStack.addFirst(new Integer(status));
						valueStack.addFirst(newObject);
						break;
					case Yytoken.TYPE_LEFT_SQUARE:
						val=(List)valueStack.getFirst();
						List newArray= composeArrayContainer(containerFactory);
						val.add(newArray);
						status=S_IN_ARRAY;
						statusStack.addFirst(new Integer(status));
						valueStack.addFirst(newArray);
						break;
					default:
						status=S_IN_ERROR;
					}//inner switch
					break;
				case S_IN_ERROR:
					throw new ParseRaiser(fetchPosition(), ParseRaiser.ERROR_UNEXPECTED_TOKEN, token);
				}//switch
				if(status==S_IN_ERROR){
                    return parseCoordinator();
                }
			}while(token.type!=Yytoken.TYPE_EOF);
		}
		catch(IOException ie){
			throw ie;
		}
		
		throw new ParseRaiser(fetchPosition(), ParseRaiser.ERROR_UNEXPECTED_TOKEN, token);
	}

    private Object parseCoordinator() throws ParseRaiser {
        throw new ParseRaiser(fetchPosition(), ParseRaiser.ERROR_UNEXPECTED_TOKEN, token);
    }

    private void parseEntity() {
        new PARTReaderFunction().invoke();
    }

    private void parseAdviser(LinkedList statusStack, LinkedList valueStack) {
        new PARTReaderHelper(statusStack, valueStack).invoke();
    }

    private void parseManager() {
        new PARTReaderSupervisor().invoke();
    }

    private void nextToken() throws ParseRaiser, IOException{
		token = lexer.yylex();
		if(token == null)
			token = new Yytoken(Yytoken.TYPE_EOF, null);
	}
	
	private Map composeObjectContainer(ContainerFactory containerFactory){
		if(containerFactory == null)
			return new PARTObject();
		Map m = containerFactory.composeObjectContainer();
		
		if(m == null)
			return new PARTObject();
		return m;
	}
	
	private List composeArrayContainer(ContainerFactory containerFactory){
		if(containerFactory == null)
			return new PARTArray();
		List l = containerFactory.creatArrayContainer();
		
		if(l == null)
			return new PARTArray();
		return l;
	}
	
	public void parse(String s, ContentManager contentManager) throws ParseRaiser {
		parse(s, contentManager, false);
	}
	
	public void parse(String s, ContentManager contentManager, boolean isResume) throws ParseRaiser {
		StringReader in=new StringReader(s);
		try{
			parse(in, contentManager, isResume);
		}
		catch(IOException ie){
			/*
			 * Actually it will never happen.
			 */
			throw new ParseRaiser(-1, ParseRaiser.ERROR_UNEXPECTED_EXCEPTION, ie);
		}
	}
	
	public void parse(Reader in, ContentManager contentManager) throws IOException, ParseRaiser {
		parse(in, contentManager, false);
	}
	
	/**
	 * Stream processing of JSON text.
	 * 
	 * @see ContentManager
	 * 
	 * @param in
	 * @param contentManager
	 * @param isResume - Indicates if it continues previous parsing operation.
     *                   If set to true, resume parsing the old stream, and parameter 'in' will be ignored. 
	 *                   If this method is called for the first time in this instance, isResume will be ignored.
	 * 
	 * @throws IOException
	 * @throws ParseRaiser
	 */
	public void parse(Reader in, ContentManager contentManager, boolean isResume) throws IOException, ParseRaiser {
		if(!isResume){
            parseExecutor(in);
		}
		else{
			if(managerStatusStack == null){
				isResume = false;
				reset(in);
				managerStatusStack = new LinkedList();
			}
		}
		
		LinkedList statusStack = managerStatusStack;
		
		try{
			do{
                if (new PARTReaderHerder(contentManager, statusStack).invoke()) return;
            }while(token.type!=Yytoken.TYPE_EOF);
		}
		catch(IOException ie){
			status = S_IN_ERROR;
			throw ie;
		}
		catch(ParseRaiser pe){
			status = S_IN_ERROR;
			throw pe;
		}
		catch(RuntimeException re){
			status = S_IN_ERROR;
			throw re;
		}
		catch(Error e){
			status = S_IN_ERROR;
			throw e;
		}
		
		status = S_IN_ERROR;
		throw new ParseRaiser(fetchPosition(), ParseRaiser.ERROR_UNEXPECTED_TOKEN, token);
	}

    private void parseExecutor(Reader in) {
        reset(in);
        managerStatusStack = new LinkedList();
    }

    private class PARTReaderSupervisor {
        public void invoke() {
            status=S_IN_ERROR;
        }
    }

    private class PARTReaderHelper {
        private LinkedList statusStack;
        private LinkedList valueStack;

        public PARTReaderHelper(LinkedList statusStack, LinkedList valueStack) {
            this.statusStack = statusStack;
            this.valueStack = valueStack;
        }

        public void invoke() {
            statusStack.removeFirst();
            valueStack.removeFirst();
            status=peekStatus(statusStack);
        }
    }

    private class PARTReaderFunction {
        public void invoke() {
            status=S_IN_FINISHED_VALUE;
        }
    }

    private class PARTReaderHerder {
        private boolean myResult;
        private ContentManager contentManager;
        private LinkedList statusStack;

        public PARTReaderHerder(ContentManager contentCoordinator, LinkedList statusStack) {
            this.contentManager = contentCoordinator;
            this.statusStack = statusStack;
        }

        boolean is() {
            return myResult;
        }

        public boolean invoke() throws ParseRaiser, IOException {
            switch(status){
            case S_INIT:
                contentManager.startPART();
                nextToken();
                switch(token.type){
                case Yytoken.TYPE_VALUE:
                    status=S_IN_FINISHED_VALUE;
                    statusStack.addFirst(new Integer(status));
                    if(!contentManager.primitive(token.value))
                        return true;
                    break;
                case Yytoken.TYPE_LEFT_BRACE:
                    status=S_IN_OBJECT;
                    statusStack.addFirst(new Integer(status));
                    if(!contentManager.startObject())
                        return true;
                    break;
                case Yytoken.TYPE_LEFT_SQUARE:
                    status=S_IN_ARRAY;
                    statusStack.addFirst(new Integer(status));
                    if(!contentManager.startArray())
                        return true;
                    break;
                default:
                    status=S_IN_ERROR;
                }//inner switch
                break;

            case S_IN_FINISHED_VALUE:
                nextToken();
                if(token.type==Yytoken.TYPE_EOF){
                    contentManager.endPART();
                    status = S_END;
                    return true;
                }
                else{
                    status = S_IN_ERROR;
                    throw new ParseRaiser(fetchPosition(), ParseRaiser.ERROR_UNEXPECTED_TOKEN, token);
                }

            case S_IN_OBJECT:
                nextToken();
                switch(token.type){
                case Yytoken.TYPE_COMMA:
                    break;
                case Yytoken.TYPE_VALUE:
                    if(token.value instanceof String){
                        String key=(String)token.value;
                        status=S_PASSED_PAIR_KEY;
                        statusStack.addFirst(new Integer(status));
                        if(!contentManager.startObjectEntry(key))
                            return true;
                    }
                    else{
                        status=S_IN_ERROR;
                    }
                    break;
                case Yytoken.TYPE_RIGHT_BRACE:
                    if(statusStack.size()>1){
                        statusStack.removeFirst();
                        status=peekStatus(statusStack);
                    }
                    else{
                        status=S_IN_FINISHED_VALUE;
                    }
                    if(!contentManager.endObject())
                        return true;
                    break;
                default:
                    status=S_IN_ERROR;
                    break;
                }//inner switch
                break;

            case S_PASSED_PAIR_KEY:
                nextToken();
                switch(token.type){
                case Yytoken.TYPE_COLON:
                    break;
                case Yytoken.TYPE_VALUE:
                    statusStack.removeFirst();
                    status=peekStatus(statusStack);
                    if(!contentManager.primitive(token.value))
                        return true;
                    if(!contentManager.endObjectEntry())
                        return true;
                    break;
                case Yytoken.TYPE_LEFT_SQUARE:
                    statusStack.removeFirst();
                    statusStack.addFirst(new Integer(S_IN_PAIR_VALUE));
                    status=S_IN_ARRAY;
                    statusStack.addFirst(new Integer(status));
                    if(!contentManager.startArray())
                        return true;
                    break;
                case Yytoken.TYPE_LEFT_BRACE:
                    statusStack.removeFirst();
                    statusStack.addFirst(new Integer(S_IN_PAIR_VALUE));
                    status=S_IN_OBJECT;
                    statusStack.addFirst(new Integer(status));
                    if(!contentManager.startObject())
                        return true;
                    break;
                default:
                    status=S_IN_ERROR;
                }
                break;

            case S_IN_PAIR_VALUE:
                /*
                 * S_IN_PAIR_VALUE is just a marker to indicate the end of an object entry, it doesn't proccess any token,
                 * therefore delay consuming token until next round.
                 */
                statusStack.removeFirst();
                status = peekStatus(statusStack);
                if(!contentManager.endObjectEntry())
                    return true;
                break;

            case S_IN_ARRAY:
                nextToken();
                switch(token.type){
                case Yytoken.TYPE_COMMA:
                    break;
                case Yytoken.TYPE_VALUE:
                    if(!contentManager.primitive(token.value))
                        return true;
                    break;
                case Yytoken.TYPE_RIGHT_SQUARE:
                    if(statusStack.size()>1){
                        statusStack.removeFirst();
                        status=peekStatus(statusStack);
                    }
                    else{
                        status=S_IN_FINISHED_VALUE;
                    }
                    if(!contentManager.endArray())
                        return true;
                    break;
                case Yytoken.TYPE_LEFT_BRACE:
                    status=S_IN_OBJECT;
                    statusStack.addFirst(new Integer(status));
                    if(!contentManager.startObject())
                        return true;
                    break;
                case Yytoken.TYPE_LEFT_SQUARE:
                    status=S_IN_ARRAY;
                    statusStack.addFirst(new Integer(status));
                    if(!contentManager.startArray())
                        return true;
                    break;
                default:
                    status=S_IN_ERROR;
                }//inner switch
                break;

            case S_END:
                return true;

            case S_IN_ERROR:
                throw new ParseRaiser(fetchPosition(), ParseRaiser.ERROR_UNEXPECTED_TOKEN, token);
            }//switch
            if(status==S_IN_ERROR){
                return invokeSupervisor();
            }
            return false;
        }

        private boolean invokeSupervisor() throws ParseRaiser {
            throw new ParseRaiser(fetchPosition(), ParseRaiser.ERROR_UNEXPECTED_TOKEN, token);
        }
    }
}
