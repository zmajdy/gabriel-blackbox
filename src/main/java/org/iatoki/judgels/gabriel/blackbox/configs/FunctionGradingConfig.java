package org.iatoki.judgels.gabriel.blackbox.configs;

import org.iatoki.judgels.gabriel.blackbox.TestGroup;

import java.util.List;

public final class FunctionGradingConfig extends SingleSourceFileWithoutSubtasksBlackBoxGradingConfig {

    private final String mainSourceFile;
    private final String customScorer;

    public FunctionGradingConfig(int timeLimitInMilliseconds, int memoryLimitInKilobytes, List<TestGroup> testData, String mainSourceFile, String customScorer) {
        super(timeLimitInMilliseconds, memoryLimitInKilobytes, testData);

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
