package fr.main.lexing;

import fr.main.Program;
import fr.main.op.Op;
import fr.main.op.OpType;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static fr.main.NotSoGood.logError;
import static java.lang.System.exit;

public class Lexer {
    private static final HashMap<String, List<Op>> functions = new HashMap<>();
    private static final HashMap<String, List<Op>> macros = new HashMap<>();

    private static ArrayList<Token> parseTokenFromFile(String filePath) {
        assert TokenType.values().length == 3 : "Exhaustive handling of TokenType in compileContentToTokens";
        ArrayList<Token> tokens = new ArrayList<>();

        String fileContent = null;
        try {
            fileContent = Files.readString(Path.of(filePath)) + "\n";
        } catch (IOException e) {
            logError("Cannot read " + filePath);
        }

        assert fileContent != null;
        Object[] lines = fileContent.lines().toArray();
        for (int line = 0; line < lines.length; line++) {
            String currLine = (String) lines[line];
            int start = 0;

            for (int col = 0; col < currLine.length(); col++) {
                if (col != currLine.length() - 1) {
                    if (currLine.charAt(col) == '/' && currLine.charAt(col + 1) == '/') break;
                    if (currLine.charAt(col) == '\'') {
                        if (start != col)
                            logError(new Location(filePath, line + 1, col + 1) + " ' character must only be used in character declaration");
                        int i = 0;
                        for (int j = col + 1; j < currLine.length(); j++) {
                            if (currLine.charAt(j) != '\'') continue;
                            i = j;
                            break;
                        }
                        if (i == 0)
                            logError(new Location(filePath, line + 1, col + 1) + " Character declaration unfinished");
                        if (i == col + 1)
                            logError(new Location(filePath, line + 1, col + 1) + " Empty character declaration is not allowed");
                        if (i == col + 3 && currLine.charAt(col + 1) != '\\')
                            logError(new Location(filePath, line + 1, col + 1) + " Character declaration should only contain a single character");
                        if (i > col + 3)
                            logError(new Location(filePath, line + 1, col + 1) + " Character declaration should only contain a single character");
                        col = i;
                        int value = currLine.substring(start + 1, col)
                                .replace("\\\\", "\\") // replace "\\" with "\"
                                .replace("\\n", "\n") // replace "\n" with newline character
                                .replace("\\r", "\r") // replace "\r" with carriage return character
                                .replace("\\t", "\t") // replace "\t" with tab character
                                .replace("\\\"", "\"") // replace "\"" with double quote character
                                .replace("\\'", "'") // replace "\'" with single quote character
                                .charAt(0);
                        tokens.add(new Token(TokenType.INT, new Location(filePath, line + 1, col + 1), value));
                        start = col + 1;
                        continue;
                    }
                    if (currLine.charAt(col) == '"') {
                        for (int i = col + 1; i < currLine.length(); i++) {
                            if (currLine.charAt(i) == '"') {
                                col = i + 1;
                                break;
                            }
                        }
                        if (col == start)
                            logError(new Location(filePath, line, start) + " Non-terminated string declaration");

                        String value = currLine.substring(start + 1, col - 1);
                        String strValue = value
                                .replace("\\\\", "\\") // replace "\\" with "\"
                                .replace("\\n", "\n") // replace "\n" with newline character
                                .replace("\\r", "\r") // replace "\r" with carriage return character
                                .replace("\\t", "\t") // replace "\t" with tab character
                                .replace("\\\"", "\"") // replace "\"" with double quote character
                                .replace("\\'", "'"); // replace "\'" with single quote character
                        tokens.add(new Token(TokenType.STRING, new Location(filePath, line + 1, col + 1), strValue));
                        start = col + 1;
                        continue;
                    }
                    if ((int) currLine.charAt(col) > (int) ' ') continue;
                    if (start == col) {
                        start++;
                        continue;
                    }
                } else col++;

                String value = currLine.substring(start, col);
                try {
                    tokens.add(new Token(TokenType.INT, new Location(filePath, line + 1, col + 1), Integer.parseInt(value)));
                } catch (NumberFormatException ignored) {
                    tokens.add(new Token(TokenType.WORD, new Location(filePath, line + 1, col + 1), value));
                }
                start = col + 1;
            }
        }
        return tokens;
    }

    private static Program parseProgramFromTokens(ArrayList<Token> tokens) {
        assert TokenType.values().length == 3 : "Exhaustive handling of OpTypes";
        ArrayList<Op> ops = new ArrayList<>();

        for (int i = 0; i < tokens.size(); i++) {
            Token token = tokens.get(i);
            if (token.type == TokenType.INT) {
                ops.add(new Op(token.loc, OpType.OP_PUSH_INT, token.intValue()));
            } else if (token.type == TokenType.WORD) {
                OpType opType;
                if ((opType = BuiltinWords.getOpTypeFromWord(token.strValue())) == OpType.OP_NONE) {
                    if (functions.containsKey(token.strValue()) || macros.containsKey(token.strValue())) {
                        ops.add(new Op(token.loc, OpType.OP_CALL, token.strValue()));
                        continue;
                    } else {
                        System.err.println(token.loc + " [ERROR] : Unknown word " + token.strValue());
                        exit(1);
                    }
                }
                if (opType == OpType.OP_FUNC || opType == OpType.OP_MACRO) {
                    ArrayList<Token> tokenList = new ArrayList<>();
                    if (tokens.size() < i + 2) logError(token.loc + " Unfinished function declaration");
                    String name = tokens.get(i + 1).strValue;
                    int nestedEnd = 1;
                    for (int j = i + 2; j < tokens.size(); j++) {
                        Token currToken = tokens.get(j);
                        if (currToken.type == TokenType.WORD) {
                            if (BuiltinWords.getOpTypeFromWord(currToken.strValue()) == OpType.OP_FUNC && opType == OpType.OP_FUNC)
                                logError(token.loc + "Function definition cannot be inside another function definition");
                            if (BuiltinWords.getOpTypeFromWord(currToken.strValue()) == OpType.OP_FUNC && opType == OpType.OP_MACRO)
                                logError(token.loc + "Function definition cannot be inside a macro definition");
                            if (BuiltinWords.getOpTypeFromWord(currToken.strValue()) == OpType.OP_MACRO && opType == OpType.OP_FUNC)
                                logError(token.loc + "Macro definition cannot be inside a function definition");
                            if (BuiltinWords.getOpTypeFromWord(currToken.strValue()) == OpType.OP_MACRO && opType == OpType.OP_MACRO)
                                logError(token.loc + "Macro definition cannot be inside another macro definition");

                            if (BuiltinWords.getOpTypeFromWord(currToken.strValue()) == OpType.OP_IF || BuiltinWords.getOpTypeFromWord(currToken.strValue()) == OpType.OP_WHILE)
                                nestedEnd++;
                            if (BuiltinWords.getOpTypeFromWord(currToken.strValue()) == OpType.OP_END) {
                                if (--nestedEnd == 0) {
                                    i = j;
                                    break;
                                }
                            }
                        }
                        tokenList.add(currToken);
                    }
                    if (BuiltinWords.getOpTypeFromWord(name) != OpType.OP_NONE)
                        logError(token.loc + "Functions and Macro names cannot be a built-in word");
                    if (functions.containsKey(name) || macros.containsKey(name))
                        logError(tokenList.get(0).loc + " Redefinition of `" + name + "`.\nFirst definition at " + functions.get(name).get(0).loc);
                    if (tokenList.isEmpty())
                        logError(token.loc + " Functions and Macros empty definitions is not allowed");
                    if (opType == OpType.OP_FUNC) functions.put(name, parseProgramFromTokens(tokenList).ops);
                    if (opType == OpType.OP_MACRO) macros.put(name, parseProgramFromTokens(tokenList).ops);
                    continue;
                }
                if (opType == OpType.OP_INCLUDE) {
                    Token filenameToken = tokens.get(++i);
                    if (filenameToken.type != TokenType.STRING)
                        logError(token.loc + "include keyword should always be followed by a string literal representing the filename to include");
                    String filename = filenameToken.strValue;
                    lexFile(filename);
                    continue;
                }
                ops.add(new Op(token.loc, opType));
            } else if (token.type == TokenType.STRING) {
                ops.add(new Op(token.loc, OpType.OP_PUSH_STR, token.strValue));
            } else assert false : "Unreachable";
        }
        return new Program(ops, functions, macros);
    }

    public static Program lexFile(String program_path) {

        ArrayList<Token> tokens = parseTokenFromFile(program_path);
        return parseProgramFromTokens(tokens);
    }

}
