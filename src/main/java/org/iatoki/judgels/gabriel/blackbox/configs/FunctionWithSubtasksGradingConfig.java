package org.iatoki.judgels.gabriel.blackbox.configs;

import org.iatoki.judgels.gabriel.blackbox.TestGroup;

import java.util.List;

public final class FunctionWithSubtasksGradingConfig extends SingleSourceFileWithSubtasksBlackBoxGradingConfig {

    private final String mainSourceFile;
    private final String customScorer;

    public FunctionWithSubtasksGradingConfig(int timeLimitInMilliseconds, int memoryLimitInKilobytes, List<TestGroup> testData, List<Integer> subtaskPoints, String mainSourceFile, String customScorer) {
        super(timeLimitInMilliseconds, memoryLimitInKilobytes, testData, subtaskPoints);

        this.mainSourceFile = mainSourceFile;
        this.customScorer = customScorer;
    }

    public String getMainSourceFile() {
        return mainSourceFile;
    }

    public String getCustomScorer() {
        return customScorer;
    }
}
