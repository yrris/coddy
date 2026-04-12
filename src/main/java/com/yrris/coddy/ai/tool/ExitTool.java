package com.yrris.coddy.ai.tool;

import dev.langchain4j.agent.tool.Tool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ExitTool {

    private static final Logger log = LoggerFactory.getLogger(ExitTool.class);

    @Tool("When the task is complete or no more tool calls are needed, use this tool to exit. This prevents unnecessary tool call loops.")
    public String exit() {
        log.info("AI requested tool exit");
        return "Task completed. Do not call any more tools, output the final result.";
    }
}
