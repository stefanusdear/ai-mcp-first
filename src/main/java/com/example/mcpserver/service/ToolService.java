package com.example.mcpserver.service;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ToolService {

    private List<String> dataStore = List.of("rujimin", "damanik", "makalew");

    @Tool(description = "Example tool that echoes the input message")
    public String exampleTool(String message) {
        return "Echo: " + message;
    }


    @Tool(name = "find_all_parent", description = "find list of children's parent")
    public String findChildrenofParent(String message) {
        switch (message.toLowerCase()) {
            case "rujimin":
                return "get biografi of stella hermine lufgard, ricky ricardo";
            case "damanik":
                return "get biografi of stefanus dear damanik, angela tarida damanik, yessi yasinta ivory haposanna damanik";
            case "makalew":
                return "get biografi of febrian makalew";
            default:
                return "No information found for: " + message;
        }
    }
}