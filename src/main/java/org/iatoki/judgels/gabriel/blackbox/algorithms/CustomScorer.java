package org.iatoki.judgels.gabriel.blackbox.algorithms;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import org.apache.commons.io.FileUtils;
import org.iatoki.judgels.gabriel.blackbox.languages.JavaGradingLanguage;
import org.iatoki.judgels.gabriel.GradingLanguage;
import org.iatoki.judgels.gabriel.blackbox.CompilationException;
import org.iatoki.judgels.gabriel.blackbox.CompilationResult;
import org.iatoki.judgels.gabriel.blackbox.CompilationVerdict;
import org.iatoki.judgels.gabriel.blackbox.PreparationException;
import org.iatoki.judgels.gabriel.blackbox.ScoringException;
import org.iatoki.judgels.gabriel.sandboxes.Sandbox;
import org.iatoki.judgels.gabriel.sandboxes.SandboxExecutionResult;
import org.iatoki.judgels.gabriel.sandboxes.SandboxExecutionStatus;

import java.io.File;
import java.io.IOException;
import java.util.List;

public final class CustomScorer extends AbstractScorer {

    private static final String SCORING_OUTPUT_FILENAME = "_scoring.out";

    private final Sandbox sandbox;
    private final String scorerExecutableFilename;
    private final List<String> customScorerExecutionCommand;

    public CustomScorer(Sandbox sandbox, File scoringDir, GradingLanguage language, File scorerFile, int compilationTimeLimitInMilliseconds, int compilationMemoryLimitInKilobytes, int scoringTimeLimitInMilliseconds, int scoringMemoryLimitInKilobytes) throws PreparationException {
        try {
            SingleSourceFileCompiler compiler = new SingleSourceFileCompiler(sandbox, scoringDir, language, "customScorer", scorerFile, compilationTimeLimitInMilliseconds, compilationMemoryLimitInKilobytes);
            CompilationResult result = compiler.compile();
            if (result.getVerdict() == CompilationVerdict.COMPILATION_ERROR) {
                throw new PreparationException("Compilation of custom scorer resulted in compilation error:\n " + result.getOutputs().get("customScorer"));
            }
        } catch (CompilationException e) {
            throw new PreparationException(e.getMessage());
        }

        this.scorerExecutableFilename = language.getExecutableFilename(scorerFile.getName());
        sandbox.addFile(new File(scoringDir, scorerExecutableFilename));
        File scorerExecutableFile = sandbox.getFile(scorerExecutableFilename);
        if (!scorerExecutableFile.setExecutable(true)) {
            throw new PreparationException("Cannot set " + scorerExecutableFile.getAbsolutePath() + " as executable");
        }
        sandbox.setTimeLimitInMilliseconds(scoringTimeLimitInMilliseconds);
        sandbox.setMemoryLimitInKilobytes(scoringMemoryLimitInKilobytes);

        if (language instanceof JavaGradingLanguage) {
            sandbox.setTimeLimitInMilliseconds(scoringTimeLimitInMilliseconds*2);
            sandbox.setMemoryLimitInKilobytes(scoringMemoryLimitInKilobytes*2 + 8000);
            sandbox.setStackSizeInKilobytes(scoringMemoryLimitInKilobytes*2 + 8000);
        }

        this.sandbox = sandbox;
        this.customScorerExecutionCommand = language.getExecutionCommand(scorerFile.getName());
    }


    @Override
    public String executeScoring(File testCaseInput, File testCaseOutput, File evaluationOutputFile) throws ScoringException {
        sandbox.addFile(evaluationOutputFile);
        sandbox.addFile(testCaseInput);
        sandbox.addFile(testCaseOutput);

        ImmutableList.Builder<String> scoringCommandBuilder = ImmutableList.builder();
        scoringCommandBuilder.addAll(customScorerExecutionCommand);
        scoringCommandBuilder.add(testCaseInput.getName());
        scoringCommandBuilder.add(testCaseOutput.getName());
        scoringCommandBuilder.add(evaluationOutputFile.getName());

        sandbox.redirectStandardOutput(SCORING_OUTPUT_FILENAME);

        List<String> scoringCommand = scoringCommandBuilder.build();
        SandboxExecutionResult executionResult = sandbox.execute(scoringCommand);

        if (executionResult.getStatus() != SandboxExecutionStatus.ZERO_EXIT_CODE) {
            throw new ScoringException(Joiner.on(" ").join(scoringCommand) + " resulted in " + executionResult);
        }

        String scoringOutput;
        try {
            File scoringOutputFile = sandbox.getFile(SCORING_OUTPUT_FILENAME);
            scoringOutput = FileUtils.readFileToString(scoringOutputFile);
        } catch (IOException e) {
            throw new ScoringException(e.getMessage());
        }

        sandbox.removeAllFilesExcept(ImmutableSet.of(scorerExecutableFilename));

        return scoringOutput;
    }
}
