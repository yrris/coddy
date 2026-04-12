package com.yrris.coddy.ai.guardrail;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.guardrail.OutputGuardrail;
import dev.langchain4j.guardrail.OutputGuardrailResult;

public class RetryOutputGuardrail implements OutputGuardrail {

    @Override
    public OutputGuardrailResult validate(AiMessage responseFromLLM) {
        String response = responseFromLLM.text();
        if (response == null || response.trim().isEmpty()) {
            return reprompt("Response was empty", "Please generate complete content");
        }
        if (response.trim().length() < 10) {
            return reprompt("Response was too short", "Please provide more detailed content");
        }
        return success();
    }
}
