package com.langtools;

import ast.ASTNode;
import exceptions.SyntaxErr;
import lexers.LexReader;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import parsers.module.ModuleParser;
import parsers.scope.Scope;
import parsers.scope.ScopeStack;
import parsers.scope.ScopeType;
import parsers.utils.ParseContext;
import parsers.utils.ParseResult;
import passes.BasicBlockBuilder;
import passes.IRDumper;
import passes.InstrBuilder;
import passes.JmpTargetResolver;
import utils.IRContext;

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
            reader.close();
            writer.close();
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
        LexReader lexReader = new LexReader(reader);
        ModuleParser moduleParser = new ModuleParser(lexReader);
        moduleParser.init();
        ParseContext parseContext = ParseContext.createContext();
        Scope globalScope = new Scope(ScopeType.MODULE, null);
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
        InstrBuilder instrBuilder = new InstrBuilder();
        IRContext irContext = IRContext.createContext();
        instrBuilder.run(astRoot, irContext);
        JmpTargetResolver jmpTargetResolver = new JmpTargetResolver();
        jmpTargetResolver.run(irContext);
        BasicBlockBuilder blockBuilder = new BasicBlockBuilder();
        blockBuilder.run(irContext);
        IRDumper.dumpCFG(irContext, writer);
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
