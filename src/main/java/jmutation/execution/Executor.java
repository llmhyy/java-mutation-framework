package jmutation.execution;

import jmutation.constants.OperatingSystem;
import jmutation.execution.output.OutputHandler;
import jmutation.execution.output.OutputHandler.OutputHandlerBuilder;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

/**
 * Given an arbitrary project (maven or gradle) and a test case, we shall execute it with results and execution trace.
 *
 * @author Yun Lin
 * @author knightsong
 */
public class Executor {
    private static OperatingSystem operatingSystem;

    private final ProcessBuilder pb = new ProcessBuilder();

    private OutputHandlerBuilder outputHandlerBuilder = new OutputHandlerBuilder();

    public Executor(File root) {
        pb.directory(root);
    }

    public static OperatingSystem getOS() {
        if (operatingSystem == null) {
            operatingSystem = OperatingSystem.getOS();
        }
        return operatingSystem;
    }

    public static boolean isWindows() {
        return getOS() == OperatingSystem.WINDOWS;
    }

    public void setOutputHandlerBuilder(OutputHandlerBuilder outputHandlerBuilder) {
        this.outputHandlerBuilder = outputHandlerBuilder;
    }

    protected String exec(String cmd) {
        try {
            return this.exec(cmd, 0);
        } catch (TimeoutException e) {
            e.printStackTrace(); // should not timeout
            return null;
        }
    }

    /**
     * Run command line and get results,you can combine the multi-command by ";"
     * for example: mvn test -Dtest="testcase",or git reset;mvn compile
     *
     * @param cmd command line
     * @return return result by exec command
     */
    protected String exec(String cmd, int timeout) throws TimeoutException {
        String output = "";
        Process process = null;
        InputStreamReader inputStr = null;
        BufferedReader bufferReader = null;
        ExecutorService executorService = null;
        pb.redirectErrorStream(true); //redirect error stream to standard stream
        try {
            if (getOS() == OperatingSystem.WINDOWS) {
                if (cmd.length() > 8100) { // If command is too long, write to bat file and execute it
                    File batFile = writeCmdToTempBatFile(cmd);
                    pb.command("cmd.exe", "/c", batFile.getCanonicalPath());
                    Files.delete(batFile.toPath());
                } else {
                    pb.command("cmd.exe", "/c", cmd);
                }
            } else {
                pb.command("bash", "-c", cmd);
            }
            process = pb.start();
            inputStr = new InputStreamReader(process.getInputStream());
            bufferReader = new BufferedReader(inputStr);
            outputHandlerBuilder.setBufferedReader(bufferReader);
            OutputHandler outputHandler = outputHandlerBuilder.build();
            outputHandler.output(cmd);
            executorService = Executors.newFixedThreadPool(1);
            Future<String> outputFuture = executorService.submit(outputHandler);
            // Terminate process, then await termination on output handler thread
            boolean isComplete = true;
            if (timeout > 0) {
                process.waitFor(timeout, TimeUnit.MINUTES);
                isComplete = !process.isAlive();
                destroyProcessAndChildren(process);
            }
            shutdownAndAwaitTermination(executorService, Integer.MAX_VALUE);
            if (!isComplete) {
                throw new TimeoutException("Process exceeded timeout duration of " + timeout + " minutes.");
            }
            output = outputFuture.get();
        } catch (IOException | InterruptedException | ExecutionException ex) {
            ex.printStackTrace();
        } finally {
            try {
                if (process != null) {
                    destroyProcessAndChildren(process);
                }
                if (inputStr != null) {
                    inputStr.close();
                }
                if (bufferReader != null) {
                    bufferReader.close();
                }
                if (executorService != null) {
                    shutdownAndAwaitTermination(executorService, Integer.MAX_VALUE);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return output;
    }

    /**
     * Shut down and terminate threads in pool
     *
     * @param pool
     * @param timeout
     */
    private void shutdownAndAwaitTermination(ExecutorService pool, int timeout) {
        pool.shutdown(); // Disable new tasks from being submitted
        try {
            // Wait a while for existing tasks to terminate
            if (!pool.awaitTermination(timeout, TimeUnit.SECONDS)) {
                pool.shutdownNow(); // Cancel currently executing tasks
                // Wait a while for tasks to respond to being cancelled
                if (!pool.awaitTermination(60, TimeUnit.SECONDS))
                    System.err.println("Pool did not terminate");
            }
        } catch (InterruptedException ie) {
            // (Re-)Cancel if current thread also interrupted
            pool.shutdownNow();
            // Preserve interrupt status
            Thread.currentThread().interrupt();
        }
    }

    private List<ProcessHandle> getChildrenHandles(ProcessHandle processHandle) {
        return processHandle.children().collect(Collectors.toList());
    }

    private void destroyProcessAndChildren(ProcessHandle processHandle) {
        for (ProcessHandle childHandle : getChildrenHandles(processHandle)) {
            destroyProcessAndChildren(childHandle);
        }
        processHandle.destroy();
    }

    /**
     * Destroys process and its children.
     *
     * @param process
     */
    private void destroyProcessAndChildren(Process process) {
        Optional<ProcessHandle> optionalProcessHandle = ProcessHandle.of(process.pid());
        if (optionalProcessHandle.isEmpty()) {
            return;
        }
        destroyProcessAndChildren(optionalProcessHandle.get());
    }

    File writeCmdToTempBatFile(String cmd) {
        File tempFile = new File("");
        try {
            tempFile = Files.createTempFile("cmd", ".bat").toFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try (FileWriter fileWriter = new FileWriter(tempFile)) {
            fileWriter.write(cmd);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return tempFile;
    }
}
