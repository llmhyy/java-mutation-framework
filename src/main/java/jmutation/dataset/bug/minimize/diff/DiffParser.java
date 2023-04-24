package jmutation.dataset.bug.minimize.diff;

import jmutation.constants.OperatingSystem;
import jmutation.dataset.bug.minimize.instruction.Instruction;
import jmutation.dataset.bug.minimize.instruction.Instruction.InstructionType;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static jmutation.dataset.bug.minimize.instruction.Instruction.InstructionType.ADD;

/**
 * Parses the result from git diff between working and buggy project, and generates instructions
 */
public class DiffParser {
    public static String ADDED_OR_REMOVED_FILE_NAME = "/dev/null";
    private static String SOURCE_FILE_PREFIX = "a" + File.separator;
    private static String TARGET_FILE_PREFIX = "b" + File.separator;

    public static List<Instruction> parse(List<String> gitDiff, String workingProject, String buggyProject) {
        // TODO: Handle obtaining relative paths. Handle delete command.
        List<Instruction> instructionList = new ArrayList<>();

        for (String line : gitDiff) {
            if (!filter(line)) continue;
            FilePair filePair = createFilePair(cleanUpLine(line), workingProject, buggyProject);
            instructionList.add(generateInstructionFromFilePair(ADD, filePair));
        }

        return instructionList;
    }

    private static FilePair createFilePair(String line, String workingProject, String buggyProject) {
        assert (line.startsWith("diff"));
        int indexOfB = line.indexOf(TARGET_FILE_PREFIX);
        String workingFile;
        if (OperatingSystem.getOS().equals(OperatingSystem.WINDOWS)) {
            workingFile = line.substring(line.indexOf(SOURCE_FILE_PREFIX) + 2, indexOfB - 3);
        } else {
            workingFile = line.substring(line.indexOf(SOURCE_FILE_PREFIX) + 2, indexOfB - 1);
        }
        workingFile = workingFile.substring(workingProject.length() + 1);
        String buggyFile;
        if (OperatingSystem.getOS().equals(OperatingSystem.WINDOWS)) {
            buggyFile = line.substring(indexOfB + 2, line.length() - 1);
        } else {
            buggyFile = line.substring(indexOfB + 2);
        }
        buggyFile = buggyFile.substring(buggyProject.length() + 1);
        return new FilePair(workingFile, buggyFile);
    }

    private static Instruction generateInstructionFromFilePair(InstructionType type, FilePair filePair) {
        switch (type) {
            case ADD:
                return new Instruction(type, filePair.buggyFile, filePair.buggyFile);
            case DELETE:
                return new Instruction(type, null, filePair.workingFile);
            default:
                return null;
        }
    }

    private static String cleanUpLine(String line) {
        String result = line.replace("\\\\", File.separator);
        result = result.replace("/", File.separator);
        return result;
    }

    private static boolean filter(String line) {
        return line.contains(".java") && line.startsWith("diff");
    }

    private static class FilePair {
        private final String workingFile;
        private final String buggyFile;

        public FilePair(String workingFile, String buggyFile) {
            this.workingFile = workingFile;
            this.buggyFile = buggyFile;
        }

        public String workingFile() {
            return workingFile;
        }

        public String buggyFile() {
            return buggyFile;
        }
    }
}
