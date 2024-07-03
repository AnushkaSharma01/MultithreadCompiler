

package com.lambda01;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

public class CodeExecutionLambdaHandler implements RequestHandler<CodeExecutionRequest, String> {

    private final CodeExecutionService codeExecutionService = new CodeExecutionService();

    @Override
    public String handleRequest(CodeExecutionRequest request, Context context) {
        String operation = request.getOperation();
        String javaCode = request.getJavaCode();
        String userInput = request.getUserInput();

        switch (operation) {
            case "execute":
                return codeExecutionService.executeJavaCode(javaCode);
            case "provideUserInput":
                return codeExecutionService.provideUserInput(javaCode, userInput);
            default:
                return "Unsupported operation: " + operation;
        }
    }
}
