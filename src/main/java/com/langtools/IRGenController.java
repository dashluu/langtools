package com.langtools;

import ast.ASTNode;
import cfg.CFG;
import exceptions.SyntaxErr;
import lex.LexReader;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import parse.module.ModuleParser;
import parse.scope.Scope;
import parse.scope.ScopeStack;
import parse.scope.ScopeType;
import parse.utils.ParseContext;
import parse.utils.ParseResult;
import passes.BasicBlockBuilder;
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

        try (BufferedReader reader = new BufferedReader(new StringReader(code));
             StringWriter irWriter = new StringWriter();
             StringWriter jsonArrWriter = new StringWriter()) {
            ASTNode moduleNode = parseSrc(reader);
            emitIR(moduleNode, irWriter);
            emitJSONArr(irWriter.toString(), jsonArrWriter);
            success = true;
            output = jsonArrWriter.toString();
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
     * Outputs the intermediate representation generated from the source code.
     *
     * @param astRoot the root of the AST.
     * @param writer  a Writer object that writes the generated intermediate representation to a destination.
     * @throws IOException if there is an IO exception.
     */
    private void emitIR(ASTNode astRoot, Writer writer) throws IOException {
        IRContext irContext = IRContext.createContext();
        BasicBlockBuilder blockBuilder = new BasicBlockBuilder();
        blockBuilder.run(astRoot, irContext);
        JmpTargetResolver jmpTargetResolver = new JmpTargetResolver();
        jmpTargetResolver.run(irContext);
        CFG cfg = irContext.getCfg();
        cfg.out(writer);
    }

    /**
     * Outputs a JSON array of intermediate representation instructions.
     *
     * @param ir     the string containing the intermediate representation instructions.
     * @param writer a Writer object that writes the generated JSON array to a destination.
     */
    private void emitJSONArr(String ir, Writer writer) throws IOException {
        String[] lines = ir.split(System.lineSeparator());
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
        writer.write(strBuff.toString());
    }
}
