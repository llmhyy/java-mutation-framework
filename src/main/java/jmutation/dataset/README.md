# bug-dataset-creator

## To read bugs from existing dataset

Refer to `jmutation.dataset.BugDataset#sample`
The following variables can be modified in the `sample` method, based on your usage:

- `largestBugId`
- `repoPath`
- `projName`
- `traceCollectionTimeoutSeconds`

The method then does the following for each bug.

- Check a bug's zip directory exists
- Unzips
- Maximise the buggy project
- Collects buggy and working traces
- Reads the data (Creates BugData object. Contains the buggy/working traces, root cause number, test case name, etc)
- Minimizes the buggy project
- Zips

Based on the usage, any of the above steps could be commented-out/removed.

## To create dataset

Refer to `jmutation.dataset.execution.Main#main`

It first analyses a project for all possible test cases, and possible mutations for each of them.
It then

- Clones the original project into the dataset
- Mutates the cloned project
- Deletes all files except the mutated files
- Zips the entire mutation