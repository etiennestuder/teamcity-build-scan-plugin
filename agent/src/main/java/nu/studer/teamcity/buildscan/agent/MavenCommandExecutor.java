package nu.studer.teamcity.buildscan.agent;

import com.intellij.openapi.diagnostic.Logger;
import jetbrains.buildServer.agent.BuildRunnerContext;
import jetbrains.buildServer.agent.ToolCannotBeFoundException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;

/**
 * Executes Maven commands and returns their output, both standard output and standard error, when given a {@link BuildRunnerContext}.
 */
final class MavenCommandExecutor {

    private static final Logger LOG = Logger.getInstance("jetbrains.buildServer.BUILDSCAN");

    private final BuildRunnerContext runnerContext;

    MavenCommandExecutor(BuildRunnerContext context) {
        this.runnerContext = context;
    }

    @NotNull
    Result execute(String args, long timeout, TimeUnit unit) throws IOException, InterruptedException {
        File mavenExec = getMvnExec();
        if (mavenExec == null) {
            return Result.didNotExecute();
        }

        String command = mavenExec.getAbsolutePath() + " " + args;
        ProcessBuilder processBuilder = new ProcessBuilder(command.split(" ")).redirectErrorStream(true);
        Process process = processBuilder.start();
        waitFor(process, timeout, unit);
        return Result.forExecutedProcess(process);
    }

    @Nullable
    private File getMvnExec() {
        String mavenPath;
        try {
            mavenPath = runnerContext.getToolPath("maven");
        } catch (ToolCannotBeFoundException e) {
            return null;
        }

        File installationBinDir = new File(mavenPath, "bin");
        String mvnExecutableName = System.getProperty("os.name").toLowerCase().contains("win") ? "mvn.cmd" : "mvn";
        return new File(installationBinDir, mvnExecutableName);
    }

    static class Result {

        private final boolean success;
        @NotNull
        private final String output;

        private Result(boolean success, @NotNull String output) {
            this.success = success;
            this.output = output;
        }

        static Result didNotExecute() {
            return new Result(false, "");
        }

        static Result forExecutedProcess(Process process) {
            String output = readOutput(process);
            return new Result(process.exitValue() == 0, output);
        }

        boolean isSuccessful() {
            return success;
        }

        @NotNull
        String getOutput() {
            return output;
        }

        @NotNull
        private static String readOutput(Process process) {
            StringBuilder sb = new StringBuilder();

            // this logic eagerly consumes the entire output into memory, which should not be an issue when only
            // used for `mvn --version`, which generates ~5 lines of output
            // this should be revisited if other commands are executed here

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            try {
                int ch;
                while ((ch = reader.read()) != -1) {
                    sb.append((char) ch);
                }
            } catch (IOException e) {
                LOG.warn("Unable to read output from Maven process", e);
            } finally {
                try {
                    reader.close();
                } catch (IOException e) {
                    LOG.warn("Unable to close reader of Maven process", e);
                }
            }

            return sb.toString();
        }

    }

    // this implementation of waitFor is adapted from later JDKs that implement waitFor(long, TimeUnit)
    // this implementation polls the exit value every 100ms until the timeout is reached or an exit value is returned
    private static boolean waitFor(Process process, long timeout, TimeUnit unit) throws InterruptedException {
        long startTime = System.nanoTime();
        long remaining = unit.toNanos(timeout);

        do {
            try {
                process.exitValue();
                return true;
            } catch (IllegalThreadStateException e) {
                if (remaining > 0) {
                    long waitTime = Math.min(TimeUnit.NANOSECONDS.toMillis(remaining) + 1, 100);
                    Thread.sleep(waitTime);
                }
            }
            remaining = unit.toNanos(timeout) - (System.nanoTime() - startTime);
        } while (remaining > 0);

        return false;
    }
}
