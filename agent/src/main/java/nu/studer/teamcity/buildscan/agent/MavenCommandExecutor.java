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
    Result execute(String args) throws IOException, InterruptedException {
        File mavenExec = getMvnExec();
        if (mavenExec == null) {
            return new Result();
        }

        String command = mavenExec.getAbsolutePath() + " " + args;
        ProcessBuilder processBuilder = new ProcessBuilder(command.split(" ")).redirectErrorStream(true);
        Process process = processBuilder.start();
        process.waitFor();

        return new Result(process);
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

        private final Process process;

        Result() {
            process = null;
        }

        Result(Process process) {
            this.process = process;
        }

        boolean isSuccessful() {
            if (process == null) {
                return false;
            }

            return process.exitValue() == 0;
        }

        @NotNull
        String getOutput() {
            if (process == null || !isSuccessful()) {
                return "";
            }

            StringBuilder sb = new StringBuilder();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            try {
                // this logic eagerly consumes the entire output into memory, which should not be an issue when only
                // used for `mvn --version`, which generates ~5 lines of output
                // this should be revisited if other commands are executed here

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
                    LOG.warn("Unable to close reader", e);
                }
            }

            return sb.toString();
        }

    }

}
