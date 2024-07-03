package com.lambda01;

import java.io.*;
import java.util.concurrent.*;

public class CodeExecutionService {

    private final ExecutorService executorService = Executors.newFixedThreadPool(3);
    private final BlockingQueue<CodeExecutionRequest> queue = new LinkedBlockingQueue<>();

    public String executeJavaCode(String javaCode) {
        CodeExecutionRequest request = new CodeExecutionRequest("execute", javaCode, null);
        return execute(request);
    }

    public String provideUserInput(String javaCode, String userInput) {
        CodeExecutionRequest request = new CodeExecutionRequest("provideUserInput", javaCode, userInput);
        return execute(request);
    }

    private String execute(CodeExecutionRequest request) {
        String executionResult = null;

        try {
            queue.put(request);
            Future<String> future = executorService.submit(new CodeExecutionThread(queue));
            executionResult = future.get();
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            executionResult = "Error executing code: " + e.getMessage();
        }

        return executionResult;
    }

    private static class CodeExecutionThread implements Callable<String> {
        private final BlockingQueue<CodeExecutionRequest> queue;

        public CodeExecutionThread(BlockingQueue<CodeExecutionRequest> queue) {
            this.queue = queue;
        }

        @Override
        public String call() {
            String output = "";
            try {
                CodeExecutionRequest request = queue.take();
                if (request == null) {
                    return "No request provided.";
                }

                String operation = request.getOperation();
                String javaCode = request.getJavaCode();
                String userInput = request.getUserInput();


                String className = extractClassName(javaCode);


                File tempFile = new File(System.getProperty("java.io.tmpdir"), className + ".java");
                try (FileWriter writer = new FileWriter(tempFile)) {
                    writer.write(javaCode);
                }


                ProcessBuilder compileProcessBuilder = new ProcessBuilder("javac", tempFile.getAbsolutePath());
                Process compileProcess = compileProcessBuilder.start();
                int compileExitCode = compileProcess.waitFor();

                if (compileExitCode == 0) {
                    // Execute the compiled Java code
                    ProcessBuilder runProcessBuilder = new ProcessBuilder("java", "-cp", tempFile.getParent(), className);
                    Process runProcess = runProcessBuilder.start();

                    if ("provideUserInput".equals(operation) && userInput != null) {
                        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(runProcess.getOutputStream()))) {
                            writer.write(userInput);
                            writer.newLine();
                            writer.flush();
                        }
                    }

                    StringBuilder runOutput = new StringBuilder();
                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(runProcess.getInputStream()))) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            runOutput.append(line).append("\n");
                        }
                    }

                    int runExitCode = runProcess.waitFor();
                    if (runExitCode == 0) {
                        output = "Output:\n" + runOutput.toString();
                    } else {
                        String errorOutput = readStream(runProcess.getErrorStream());
                        output = "Execution failed:\n" + errorOutput;
                    }
                } else {
                    String compileErrorOutput = readStream(compileProcess.getErrorStream());
                    output = "Compilation failed:\n" + compileErrorOutput;
                }

            } catch (IOException | InterruptedException e) {
                Thread.currentThread().interrupt();
                output = "Error executing code: " + e.getMessage();
            }

            return output;
        }

        private String readStream(InputStream stream) throws IOException {
            StringBuilder result = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    result.append(line).append("\n");
                }
            }
            return result.toString();
        }

        private String extractClassName(String javaCode) {

            int classIndex = javaCode.indexOf("public class");
            if (classIndex == -1) {
                return "Main";
            }
            int startIndex = classIndex + "public class".length();
            int endIndex = javaCode.indexOf("{", startIndex);
            return javaCode.substring(startIndex, endIndex).trim().split("\\s+")[0];
        }
    }
}








//package com.lambda01;
//
//import java.io.*;
//import java.util.concurrent.*;
//
//public class CodeExecutionService {
//
//    private ExecutorService executorService = Executors.newFixedThreadPool(3);
//    private BlockingQueue<CodeExecutionRequest> queue = new LinkedBlockingQueue<>();
//    private CodeExecutionRequest currentRequest = null;
//
//    public String executeJavaCode(String javaCode) {
//        CodeExecutionRequest request = new CodeExecutionRequest("execute", javaCode, null);
//        return execute(request);
//    }
//
//    public String provideUserInput(String javaCode, String userInput) {
//        CodeExecutionRequest request = new CodeExecutionRequest("user-input", javaCode, userInput);
//        return execute(request);
//    }
//
//    private String execute(CodeExecutionRequest request) {
//        String executionResult = null;
//
//        try {
//            queue.put(request);
//            Future<String> future = executorService.submit(new CodeExecutionThread(queue));
//            executionResult = future.get(); // Get the execution result from the thread
//        } catch (InterruptedException | ExecutionException e) {
//            Thread.currentThread().interrupt();
//            executionResult = "Error executing code: " + e.getMessage();
//        }
//
//        return executionResult;
//    }
//
//    private static class CodeExecutionThread implements Callable<String> {
//        private final BlockingQueue<CodeExecutionRequest> queue;
//
//        public CodeExecutionThread(BlockingQueue<CodeExecutionRequest> queue) {
//            this.queue = queue;
//        }
//
//        @Override
//        public String call() {
//            String output = "";
//            try {
//                CodeExecutionRequest request = queue.take();
//                if (request == null) {
//                    return "No request provided.";
//                }
//
//                String operation = request.getOperation();
//                String javaCode = request.getJavaCode();
//                String userInput = request.getUserInput();
//
//                // Extract the class name from the Java code
//                String className = extractClassName(javaCode);
//
//                // Create a temporary file for Java code
//                File tempFile = new File(System.getProperty("java.io.tmpdir"), className + ".java");
//                try (FileWriter writer = new FileWriter(tempFile)) {
//                    writer.write(javaCode);
//                }
//
//                // Compile the Java code
//                ProcessBuilder compileProcessBuilder = new ProcessBuilder("javac", tempFile.getAbsolutePath());
//                Process compileProcess = compileProcessBuilder.start();
//                int compileExitCode = compileProcess.waitFor();
//
//                if (compileExitCode == 0) {
//                    // Execute the compiled Java code
//                    ProcessBuilder runProcessBuilder = new ProcessBuilder("java", "-cp", tempFile.getParent(), className);
//                    Process runProcess = runProcessBuilder.start();
//
//                    if (userInput != null) {
//                        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(runProcess.getOutputStream()))) {
//                            writer.write(userInput);
//                            writer.newLine();
//                            writer.flush();
//                        }
//                    }
//
//                    StringBuilder runOutput = new StringBuilder();
//                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(runProcess.getInputStream()))) {
//                        String line;
//                        while ((line = reader.readLine()) != null) {
//                            runOutput.append(line).append("\n");
//                        }
//                    }
//
//                    int runExitCode = runProcess.waitFor();
//                    if (runExitCode == 0) {
//                        output = "Output:\n" + runOutput.toString();
//                    } else {
//                        String errorOutput = readStream(runProcess.getErrorStream());
//                        output = "Execution failed:\n" + errorOutput;
//                    }
//                } else {
//                    String compileErrorOutput = readStream(compileProcess.getErrorStream());
//                    output = "Compilation failed:\n" + compileErrorOutput;
//                }
//
//            } catch (IOException | InterruptedException e) {
//                Thread.currentThread().interrupt();
//                output = "Error executing code: " + e.getMessage();
//            }
//
//            return output;
//        }
//
//        private String readStream(InputStream stream) throws IOException {
//            StringBuilder result = new StringBuilder();
//            try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream))) {
//                String line;
//                while ((line = reader.readLine()) != null) {
//                    result.append(line).append("\n");
//                }
//            }
//            return result.toString();
//        }
//
//        private String extractClassName(String javaCode) {
//            // Basic extraction of the class name assuming "public class <classname>"
//            int classIndex = javaCode.indexOf("public class");
//            if (classIndex == -1) {
//                return "Main";
//            }
//            int startIndex = classIndex + "public class".length();
//            int endIndex = javaCode.indexOf("{", startIndex);
//            return javaCode.substring(startIndex, endIndex).trim().split("\\s+")[0];
//        }
//    }
//}
