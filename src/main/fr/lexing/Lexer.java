package main.fr.lexing;

import main.fr.Program;
import main.fr.UnreachableCodeException;
import main.fr.op.Op;
import main.fr.op.OpType;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

import static java.lang.System.exit;

public class Lexer {
    private static ArrayList<Token> parseTokenFromFile(String filePath) {
        assert TokenType.values().length == 2 : "Exhaustive handling of TokenType in compileContentToTokens";
        ArrayList<Token> tokens = new ArrayList<>();

        String fileContent = null;
        try{
            fileContent = Files.readString(Path.of(filePath)) + "\n";
        }catch (IOException e){
            System.err.println("[ERROR]: Cannot read " + filePath);
            exit(1);
        }

        Object[] lines = fileContent.lines().toArray();
        for (int line = 0; line < lines.length; line++) {
            String currLine = (String) lines[line];
            int start = 0;

            for (int col = 0; col < currLine.length(); col++) {
                if(col != currLine.length() - 1){
                    if (currLine.charAt(col) > ' ') continue;
                    if (start == col) continue;
                }else col++;

                String value = currLine.substring(start, col);
                try{
                    tokens.add(new Token(new Location(filePath, line+1, col+1), Integer.parseInt(value)));
                }catch (NumberFormatException ignored){
                    tokens.add(new Token(new Location(filePath, line+1, col+1), value));
                }
                start = col + 1;
            }
        }
        return tokens;
    }

    private static ArrayList<Op> parseProgramFromTokens(ArrayList<Token> tokens) throws UnreachableCodeException {
        ArrayList<Op> ops = new ArrayList<>();
        for (Token token : tokens){
            if(token.type == TokenType.INT){
                ops.add(new Op(token.loc, OpType.OP_PUSH, token.intValue()));
            } else if (token.type == TokenType.WORD) {
                OpType opType;
                if((opType = BuiltinWords.getOpTypeFromWord(token.strValue())) == OpType.OP_NONE){
                    System.err.println(token.loc + " [ERROR] : Unknown word " + token.strValue());
                    exit(1);
                }
                ops.add(new Op(token.loc, opType));
            }
        }
        return ops;
    }

    public static Program lex_file(String program_path) throws UnreachableCodeException {

        ArrayList<Token> tokens = parseTokenFromFile(program_path);
        ArrayList<Op> ops = parseProgramFromTokens(tokens);
        return new Program(ops);
    }

}
