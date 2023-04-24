package jmutation.dataset.bug.minimize.diff;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Wrapper for git commands for dataset creation
 */
public class GitWrapper {

    public static List<String> getRawDiff(String pathToWorking, String pathToBuggy) {
        List<String> cmdList = new ArrayList<>();

        cmdList.add("git");
        cmdList.add("diff");
        cmdList.add("--no-index");
        cmdList.add("--ignore-space-change");
        cmdList.add(pathToWorking);
        cmdList.add(pathToBuggy);

        String[] cmds = cmdList.toArray(new String[0]);
        try {
            ProcessBuilder pb = new ProcessBuilder(cmds);
            pb.redirectErrorStream(true); // merge stdout and stderr
            Process proc = pb.start();

            InputStream stdin = proc.getInputStream();
            InputStreamReader isr = new InputStreamReader(stdin);
            BufferedReader br = new BufferedReader(isr);

            List<String> diffContent = new ArrayList<>();
            String line;
            while ((line = br.readLine()) != null)
                diffContent.add(line);

            stdin.close();

            return diffContent;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
}
