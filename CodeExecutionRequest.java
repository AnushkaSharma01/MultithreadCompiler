
package com.lambda01;

public class CodeExecutionRequest {
    private String operation;
    private String javaCode;
    private String userInput;

    public CodeExecutionRequest() {}

    public CodeExecutionRequest(String operation, String javaCode, String userInput) {
        this.operation = operation;
        this.javaCode = javaCode;
        this.userInput = userInput;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public String getJavaCode() {
        return javaCode;
    }

    public void setJavaCode(String javaCode) {
        this.javaCode = javaCode;
    }

    public String getUserInput() {
        return userInput;
    }

    public void setUserInput(String userInput) {
        this.userInput = userInput;
    }
}
