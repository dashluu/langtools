package com.langtools;

import ast.ASTNode;
import exceptions.SyntaxErr;
import ir.passes.IRDumper;
import ir.passes.IRStructBuilder;
import ir.passes.JmpTargetResolver;
import ir.structures.IRModule;
import ir.utils.IRContext;
import lexers.Lexer;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import parsers.module.ModuleParser;
import parsers.utils.ParseContext;
import parsers.utils.ParseResult;
import parsers.utils.Scope;
import parsers.utils.ScopeStack;

import java.io.*;

@RestController
public class IRGenController {
    @CrossOrigin(origins = "http://localhost:3000")
    @PostMapping("/genIR")
    public String genIR(@ModelAttribute("code") String code) {
        boolean success;
        String output;
        BufferedReader reader = new BufferedReader(new StringReader(code));
        StringWriter writer = new StringWriter();

        try {
            ASTNode moduleNode = parseSrc(reader);
            dumpsIR(moduleNode, writer);
            success = true;
            output = out(writer.toString());
        } catch (IOException | SyntaxErr e) {
            success = false;
            output = "\"Error: " + e.getMessage() + "\"";
        }

        BuildResult buildResult = new BuildResult(success, output);
        return buildResult.toJSONStr();
    }

    /**
     * Parses the source code in the request.
     *
     * @param reader a Reader object that reads in text content.
     * @return an AST node as the root of the AST if successful.
     * @throws IOException if there is an IO exception.
     * @throws SyntaxErr   if there is a syntax error.
     */
    private ASTNode parseSrc(Reader reader) throws IOException, SyntaxErr {
        Lexer lexer = new Lexer(reader);
        ModuleParser moduleParser = new ModuleParser(lexer);
        moduleParser.init();
        ParseContext parseContext = ParseContext.createContext();
        Scope globalScope = new Scope(null);
        ScopeStack scopeStack = parseContext.getScopeStack();
        scopeStack.push(globalScope);
        ParseResult<ASTNode> moduleResult = moduleParser.parseModule(parseContext);
        scopeStack.pop();
        if (parseContext.hasErr()) {
            throw new SyntaxErr(parseContext.getErrMsg());
        }
        return moduleResult.getData();
    }

    /**
     * Dumps the intermediate representation generated from the source code.
     *
     * @param astRoot the root of the AST.
     * @param writer  a Writer object that writes the intermediate representation to a destination.
     * @throws IOException if there is an IO exception.
     */
    private void dumpsIR(ASTNode astRoot, Writer writer) throws IOException {
        IRStructBuilder structBuilder = new IRStructBuilder();
        IRContext irContext = IRContext.createContext();
        IRModule module = structBuilder.runPass(astRoot, irContext);
        JmpTargetResolver jmpTargetResolver = new JmpTargetResolver();
        jmpTargetResolver.runPass(module);
        IRDumper irDumper = new IRDumper();
        irDumper.dump(module, writer);
    }

    /**
     * Generates output as a JSON array of instructions.
     *
     * @param irStr the string containing the intermediate representation instructions.
     * @return the generated JSON string.
     */
    private String out(String irStr) throws IOException {
        String[] lines = irStr.split(System.lineSeparator());
        StringBuilder strBuff = new StringBuilder("[");
        boolean firstLine = true;
        for (String line : lines) {
            if (!firstLine) {
                strBuff.append(",");
            }
            firstLine = false;
            strBuff.append("\"").append(line).append("\"");
        }
        strBuff.append("]");
        return strBuff.toString();
    }
}
