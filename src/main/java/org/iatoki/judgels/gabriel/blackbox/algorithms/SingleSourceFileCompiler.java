package org.iatoki.judgels.gabriel.blackbox.algorithms;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.io.FileUtils;
import org.iatoki.judgels.gabriel.blackbox.languages.JavaGradingLanguage;
import org.iatoki.judgels.gabriel.sandboxes.SandboxExecutionResult;
import org.iatoki.judgels.gabriel.GradingLanguage;
import org.iatoki.judgels.gabriel.sandboxes.Sandbox;
import org.iatoki.judgels.gabriel.blackbox.CompilationException;
import org.iatoki.judgels.gabriel.blackbox.CompilationResult;
import org.iatoki.judgels.gabriel.blackbox.CompilationVerdict;
import org.iatoki.judgels.gabriel.sandboxes.SandboxExecutionStatus;

import java.io.File;
import java.io.IOException;
import java.util.List;

public final class SingleSourceFileCompiler implements org.iatoki.judgels.gabriel.blackbox.Compiler {

    private static final String COMPILATION_OUTPUT_FILENAME = "_compilation.out";

    private final Sandbox sandbox;
    private final File compilationDir;
    private final String sourceKey;

    private List<String> compilationCommand;
    private String executableFilename;

    public SingleSourceFileCompiler(Sandbox sandbox, File compilationDir, GradingLanguage language, String sourceKey, File sourceFile, int timeLimitInMilliseconds, int memoryLimitInKilobytes) {
        sandbox.addFile(sourceFile);

        sandbox.setTimeLimitInMilliseconds(timeLimitInMilliseconds);
        sandbox.setMemoryLimitInKilobytes(memoryLimitInKilobytes);
        sandbox.setStackSizeInKilobytes(memoryLimitInKilobytes);

        if (language instanceof JavaGradingLanguage) {
            sandbox.setTimeLimitInMilliseconds(timeLimitInMilliseconds*2);
            sandbox.setMemoryLimitInKilobytes(memoryLimitInKilobytes*2 + 8000);
            sandbox.setStackSizeInKilobytes(memoryLimitInKilobytes*2 + 8000);
        }

        sandbox.resetRedirections();
        sandbox.redirectStandardOutput(COMPILATION_OUTPUT_FILENAME);
        sandbox.redirectStandardError(COMPILATION_OUTPUT_FILENAME);

        this.sandbox = sandbox;
        this.compilationDir = compilationDir;
        this.sourceKey = sourceKey;

        this.compilationCommand = language.getCompilationCommand(sourceFile.getName());
        this.executableFilename = language.getExecutableFilename(sourceFile.getName());
    }

    @Override
    public CompilationResult compile() throws CompilationException {
        SandboxExecutionResult executionResult = sandbox.execute(compilationCommand);

        if (executionResult.getStatus() == SandboxExecutionStatus.ZERO_EXIT_CODE) {
            File compilationOutputFile = sandbox.getFile(COMPILATION_OUTPUT_FILENAME);
            try {
                String compilationOutput = FileUtils.readFileToString(compilationOutputFile);
                FileUtils.forceDelete(compilationOutputFile);
                FileUtils.copyFileToDirectory(sandbox.getFile(executableFilename), compilationDir);
                return new CompilationResult(CompilationVerdict.OK, ImmutableMap.of(sourceKey, compilationOutput));
            } catch (IOException e) {
                throw new CompilationException(e.getMessage());
            }

        } else if (executionResult.getStatus() == SandboxExecutionStatus.NONZERO_EXIT_CODE) {
            File compilationOutputFile = sandbox.getFile(COMPILATION_OUTPUT_FILENAME);
            try {
                String compilationOutput = FileUtils.readFileToString(compilationOutputFile);
                FileUtils.forceDelete(compilationOutputFile);

                return new CompilationResult(CompilationVerdict.COMPILATION_ERROR, ImmutableMap.of(sourceKey, compilationOutput));
            } catch (IOException e) {
                throw new CompilationException(e.getMessage());
            }
        } else {
            throw new CompilationException(Joiner.on(" ").join(compilationCommand) + " resulted in " + executionResult);
        }
    }
}
