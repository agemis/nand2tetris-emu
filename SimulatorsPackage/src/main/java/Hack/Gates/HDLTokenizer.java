/********************************************************************************
 * The contents of this file are subject to the GNU General Public License      *
 * (GPL) Version 2 or later (the "License"); you may not use this file except   *
 * in compliance with the License. You may obtain a copy of the License at      *
 * http://www.gnu.org/copyleft/gpl.html                                         *
 *                                                                              *
 * Software distributed under the License is distributed on an "AS IS" basis,   *
 * without warranty of any kind, either expressed or implied. See the License   *
 * for the specific language governing rights and limitations under the         *
 * License.                                                                     *
 *                                                                              *
 * This file was originally developed as part of the software suite that        *
 * supports the book "The Elements of Computing Systems" by Nisan and Schocken, *
 * MIT Press 2005. If you modify the contents of this file, please document and *
 * mark your changes clearly, for the benefit of others.                        *
 ********************************************************************************/

package Hack.Gates;

import java.io.*;
import java.util.*;

/**
 * HDLTokenizer object: Reads input from an HDL reader and produces a stream of
 * tokens.
 */
public class HDLTokenizer {

    // Token types
    public static final int TYPE_KEYWORD     = 1;
    public static final int TYPE_SYMBOL      = 2;
    public static final int TYPE_IDENTIFIER  = 3;
    public static final int TYPE_INT_CONST   = 4;

    // Keywords of the scripting language
    public static final int KW_CHIP      = 1;
    public static final int KW_IN        = 2;
    public static final int KW_OUT       = 3;
    public static final int KW_BUILTIN   = 4;
    public static final int KW_CLOCKED   = 5;
    public static final int KW_PARTS     = 6;

    // The parser
    private StreamTokenizer parser;

    // Hashtable containing the keywords of the language
    private Hashtable<String, Integer> keywords;

    // The type of the current token
    private int tokenType;

    // The type of the current keyword
    private int keyWordType;

    // The current symbol
    private char symbol;

    // The current identifier
    private String identifier;

    // The source file name
    private String fileName;

    /**
     * Constructs a new HDLTokenizer with the given file name.
     */
    public HDLTokenizer(String fileName) throws HDLException {
        this.fileName = fileName;
        Reader input;

        try {
            input = new FileReader(fileName);
        } catch (IOException ioe) {
            throw new HDLException("Can't find HDL file " + fileName);
        }

        try {
            initializeInput(input);
        } catch (IOException ioe) {
            throw new HDLException("Error while initializing for reading", fileName);
        }
    }

    public HDLTokenizer() {}

    /**
     * Initializes the tokenizer input
     */
    protected void initializeInput(Reader input) throws IOException {
        parser = new StreamTokenizer(input);
        parser.parseNumbers();
        parser.slashSlashComments(true);
        parser.slashStarComments(true);
        parser.wordChars(':', ':');
        parser.wordChars('[', '[');
        parser.wordChars(']', ']');
        parser.nextToken();
        initKeywords();
    }

    /**
     * Advances the parser to the next token
     * if has no more tokens, throws an exception.
     */
    public void advance() throws HDLException {
        if (!hasMoreTokens())
            HDLError("Unexpected end of file");

        try {
            switch (parser.ttype) {
                case StreamTokenizer.TT_NUMBER:
                    tokenType = TYPE_INT_CONST;
                    // The current int value
                    // The current token
                    String currentToken;
                    break;
                case StreamTokenizer.TT_WORD:
                    currentToken = parser.sval;
                    Integer keywordCode = keywords.get(currentToken);
                    if (keywordCode != null) {
                        tokenType = TYPE_KEYWORD;
                        keyWordType = keywordCode;
                    }
                    else {
                        tokenType = TYPE_IDENTIFIER;
                        identifier = currentToken;
                    }
                    break;
                default:
                    tokenType = TYPE_SYMBOL;
                    symbol = (char)parser.ttype;
                    break;
            }
            parser.nextToken();
        } catch (IOException ioe) {
            throw new HDLException("Error while reading HDL file");
        }
    }

    /**
     * Returns the current token type
     */
    public int getTokenType() {
        return tokenType;
    }

    /**
     * Returns the keyword type of the current token
     * May only be called when getTokenType() == KEYWORD
     */
    public int getKeywordType() {
        return keyWordType;
    }

    /**
     * Returns the symbol of the current token
     * May only be called when getTokenType() == SYMBOL
     */
    public char getSymbol() {
        return symbol;
    }

    /**
     * Returns the identifier value of the current token
     * May only be called when getTokenType() == IDENTIFIER
     */
    public String getIdentifier() {
        return identifier;
    }

    /**
     * Returns if there are more tokens in the stream
     */
    public boolean hasMoreTokens() {
        return (parser.ttype != StreamTokenizer.TT_EOF);
    }

    // Initializes the keywords hashtable
    private void initKeywords() {
        keywords = new Hashtable<>();
        keywords.put("CHIP", KW_CHIP);
        keywords.put("IN", KW_IN);
        keywords.put("OUT", KW_OUT);
        keywords.put("BUILTIN", KW_BUILTIN);
        keywords.put("CLOCKED", KW_CLOCKED);
        keywords.put("PARTS:", KW_PARTS);
    }

    /**
     * Generates an HDLException with the given message.
     */
    public void HDLError(String message) throws HDLException {
        throw new HDLException(message, fileName, parser.lineno());
    }
}
