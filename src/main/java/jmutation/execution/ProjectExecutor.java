package jmutation.execution;

import jmutation.model.Project;
import jmutation.model.TestCase;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 *
 * Given an arbitrary project (maven or gradle) and a test case, we shall execute it with results and execution trace.
 *
 * @author Yun Lin
 * @author knightsong
 *
 */
public class ProjectExecutor {

	public final static String OS_WINDOWS = "windows";
	public final static String OS_MAC = "mac";
	public final static String OS_UNIX = "unix";

	private final ProcessBuilder pb = new ProcessBuilder();
	private final Project project;

	public ProjectExecutor(Project proj) {
		this.project = proj;
		pb.directory(proj.getRoot());
	}
	/**
	 * Set working directory to process
	 *
	 * @param file working directory
	 */
	public ProjectExecutor setDirectory(File file) {
		pb.directory(file);
		return this;
	}

	public String getOS() {
		return System.getProperty("os.name").toLowerCase();
	}

	public ExecutionResult exec(TestCase testCase) {
		String singleTestCmd = this.project.singleTestCommand(testCase);
		String out = exec(singleTestCmd);
		return new ExecutionResult(out);
	}

	private String exec(String cmd) {
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
	private String exec(String cmd, int timeout) throws TimeoutException{
		StringBuilder builder = new StringBuilder();
		Process process = null;
		InputStreamReader inputStr = null;
		BufferedReader bufferReader = null;
		pb.redirectErrorStream(true); //redirect error stream to standard stream
		try {
			if (getOS().contains(OS_WINDOWS)) {
				pb.command("cmd.exe", "/c", cmd);
			} else {
				pb.command("bash", "-c", cmd);
			}
			process = pb.start();
			if (timeout > 0) {
				boolean completed = process.waitFor(timeout, TimeUnit.MINUTES);
				if (!completed)
					throw new TimeoutException();
			}
			inputStr = new InputStreamReader(process.getInputStream());
			bufferReader = new BufferedReader(inputStr);
			String line;
			while ((line = bufferReader.readLine()) != null) {
				builder.append("\n").append(line);
			}
		} catch (IOException | InterruptedException ex) {
			ex.printStackTrace();
		} finally {
			try {
				if (process != null) {
					process.destroy();
				}
				if (inputStr != null) {
					inputStr.close();
				}
				if (bufferReader != null) {
					bufferReader.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return builder.toString();
	}

}
