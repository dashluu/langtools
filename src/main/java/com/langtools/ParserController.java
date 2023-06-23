package com.langtools;

import exceptions.SyntaxError;
import nodes.Node;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import parsers.SrcParser;

import java.io.*;

@RestController
public class ParserController {
    @CrossOrigin(origins = "http://localhost:3000")
    @PostMapping("/parse")
    public String parseCode(@ModelAttribute("code") String code) {
        boolean success;
        String output;
        BufferedReader reader = new BufferedReader(new StringReader(code));
        SrcParser srcParser = new SrcParser(reader);

        try {
            Node srcRoot = srcParser.parseSrc();
            success = true;
            output = "{" + srcRoot.toJsonStr() + "}";
        } catch (IOException | SyntaxError e) {
            success = false;
            output = "\"Error: " + e.getMessage() + "\"";
        }

        ParseResult parseResult = new ParseResult(success, output);
        return parseResult.toJsonStr();
    }

}
