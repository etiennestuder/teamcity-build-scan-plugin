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
    Result execute(String args, long timeout, TimeUnit unit)  {
        File mavenExec = getMvnExec();
        if (mavenExec == null) {
            return Result.forFailedToExecute();
        }

        String command = mavenExec.getAbsolutePath() + " " + args;
        ProcessBuilder processBuilder = new ProcessBuilder(command.split(" ")).redirectErrorStream(true);

        String m2Home = mavenExec.getParentFile().getParentFile().getAbsolutePath();
        processBuilder.environment().put("M2_HOME", m2Home);
        LOG.info("Initial M2_HOME: " + System.getenv("M2_HOME"));
        LOG.info("Current M2_HOME: " + m2Home);

        Process process;
        try {
            LOG.info("Executing Maven command: " + command);
            process = processBuilder.start();
        } catch (IOException e) {
            LOG.warn("Failed to execute Maven command: " + command, e);
            return Result.forFailedToExecute();
        }

        boolean finished = waitFor(process, timeout, unit);

        if (finished) {
            return Result.forExecutedProcess(process);
        } else {
            process.destroy();
            return Result.forFailedToExecute();
        }
    }

    @Nullable
    private File getMvnExec() {
        String mavenPath;
        try {
            mavenPath = runnerContext.getToolPath("maven");
        } catch (ToolCannotBeFoundException e) {
            LOG.warn("Could not find \"maven\" tool path in BuildRunnerContext", e);
            return null;
        }

        File installationBinDir = new File(mavenPath, "bin");
        String mvnExecutableName = System.getProperty("os.name").toLowerCase().contains("win") ? "mvn.cmd" : "mvn";
        File mvnExecutable = new File(installationBinDir, mvnExecutableName);

        if (!mvnExecutable.exists()) {
            LOG.warn("Could not find Maven executable: " + mvnExecutable.getAbsolutePath());
            return null;
        }

        return mvnExecutable;
    }

    static class Result {

        @NotNull
        private final String output;
        @Nullable
        private final Integer exitValue;


        private Result(@NotNull String output, @Nullable Integer exitValue) {
            this.output = output;
            this.exitValue = exitValue;
        }

        static Result forFailedToExecute() {
            return new Result("", null);
        }

        static Result forExecutedProcess(Process process) {
            String output = readOutput(process);
            return new Result(output, process.exitValue());
        }

        @NotNull
        String getOutput() {
            return output;
        }

        @Nullable
        Integer getExitValue() {
            return exitValue;
        }

        boolean isSuccessful() {
            return exitValue != null && exitValue == 0;
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
    private static boolean waitFor(Process process, long timeout, TimeUnit unit) {
        long startTime = System.nanoTime();
        long remaining = unit.toNanos(timeout);

        do {
            try {
                process.exitValue();
                return true;
            } catch (IllegalThreadStateException e) {
                if (remaining > 0) {
                    long waitTime = Math.min(TimeUnit.NANOSECONDS.toMillis(remaining) + 1, 100);
                    try {
                        Thread.sleep(waitTime);
                    } catch (InterruptedException ex) {
                        return false;
                    }
                }
            }
            remaining = unit.toNanos(timeout) - (System.nanoTime() - startTime);
        } while (remaining > 0);

        return false;
    }

}
