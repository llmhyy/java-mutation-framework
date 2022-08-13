package jmutation.execution.output;

public class MicrobatOutputHandler extends OutputHandler {
    final private String PROGRESS_HEADER = "$progress";
    @Override
    public void output(String outputString) {
        if (outputString.startsWith(PROGRESS_HEADER)) {
            String[] fragments = outputString.split(" ");
            printProgress(Integer.valueOf(fragments[1]), Integer.valueOf(fragments[2]));
        } else {
            super.output(outputString);
        }
    }

    private void printProgress(int currentStepNum, int totalStepNum) {
        if(totalStepNum == 0) {
            return;
        }

        double progress = ((double) currentStepNum) / totalStepNum;

        double preProgr = 0;
        if (currentStepNum == 1) {
            System.out.print("progress: ");
        } else {
            preProgr = ((double) (currentStepNum - 1)) / totalStepNum;
        }

        int prog = (int) (progress * 100);
        int preP = (int) (preProgr * 100);

        int diff = prog - preP;
        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < diff; i++) {
            buffer.append("=");
        }
        System.out.print(buffer);

        int[] percentiles = { 10, 20, 30, 40, 50, 60, 70, 80, 90 };
        for (int i = 0; i < percentiles.length; i++) {
            int percentile = percentiles[i];
            if (preP < percentile && percentile <= prog) {
                super.output(prog + "%");
            }
        }
        if (currentStepNum == totalStepNum) {
            System.out.println("|");
        }
    }
}
