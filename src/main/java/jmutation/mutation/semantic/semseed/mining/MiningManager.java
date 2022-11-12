package jmutation.mutation.semantic.semseed.mining;

import org.kohsuke.github.GHCommit;
import org.kohsuke.github.GHCommit.File;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GHRepositorySearchBuilder;
import org.kohsuke.github.GHRepositorySearchBuilder.Sort;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.PagedIterable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.Iterator;
import java.util.List;

/**
 * Manages bug-fix pattern mining process
 */
public class MiningManager {
    private final GitHub gitHub;
    private final String filePath;

    public MiningManager(GitHub gitHub, String filePath) {
        this.gitHub = gitHub;
        this.filePath = filePath;
    }

    public static void main(String[] args) {
        try {
            MiningManager m = new MiningManager(GitHub.connect(), "some file");
            m.run();
        } catch (IOException e) {

        }
    }

    public void run() throws IOException {
        Iterator<GHRepository> repositoryIterator = getRepositories();
        while (repositoryIterator.hasNext()) {
            GHRepository repo = repositoryIterator.next();
            Iterator<GHCommit> commits = repo.listCommits().iterator();
            while (commits.hasNext()) {
                // do some check on commit, then convert to pattern
                GHCommit commit = commits.next();
                if (commitCanBeUsed(commit)) {
                    writeCommitToBugFixFile(commit, filePath);
                }
            }
        }
    }

    private boolean commitCanBeUsed(GHCommit commit) throws IOException {
        List<File> files = commit.getFiles();
        if (files.size() != 1) {
            return false;
        }
        File file = files.get(0);
        if (!file.getFileName().endsWith(".java")) {
            return false;
        }
        if (commit.getLinesChanged() != 2) {
            return false;
        }
        String[] strToContain = new String[]{"bug", "fix", "error", "issue", "problem", "correct"};
        boolean containsAtLeastOne = false;
        for (String str : strToContain) {
            if (commit.getCommitShortInfo().getMessage().contains(str)) {
                containsAtLeastOne = true;
                break;
            }
        }
        if (!containsAtLeastOne) {
            return false;
        }
        return commit.getParentSHA1s().size() == 1;
    }

    private Iterator<GHRepository> getRepositories() {
        GHRepositorySearchBuilder repositorySearchBuilder = gitHub.searchRepositories();
        repositorySearchBuilder.sort(Sort.STARS);
        repositorySearchBuilder.language("Java");
        PagedIterable<GHRepository> result = repositorySearchBuilder.list();
        return result.iterator();
    }

    private void writeCommitToBugFixFile(GHCommit commit, String bugFixFilePath) throws IOException {
        StringBuilder content = new StringBuilder();
        StringBuilder oldContent = new StringBuilder();
        File committedFile = commit.getFiles().get(0);
        URL url = committedFile.getRawUrl();
        String[] patchToApply = getPatchToApply(committedFile.getPatch());
        URLConnection urlConnection = url.openConnection();
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            content.append(line);
            content.append("\n");
            if (line.equals(patchToApply[1])) {
                oldContent.append(patchToApply[0]);
            } else {
                oldContent.append(line);
                oldContent.append("\n");
            }
        }
        bufferedReader.close();
        // Get their compilation units.
        // Get minimum mutation range.
        // Form bug-fix pattern
    }

    private String[] getPatchToApply(String patch) throws IOException {
        BufferedReader bufReader = new BufferedReader(new StringReader(patch));
        String line;
        String[] res = new String[2];
        while ((line = bufReader.readLine()) != null) {
            if (line.startsWith("+")) {
                res[0] = line.substring(1);
            } else if (line.startsWith("-")) {
                res[1] = line.substring(1);
            }
        }
        return res;
    }
}
