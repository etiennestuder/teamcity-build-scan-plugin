package nu.studer.teamcity.buildscan.agent;

import jetbrains.buildServer.agent.BuildRunnerContext;
import jetbrains.buildServer.agent.ToolCannotBeFoundException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * This class executes Maven commands and returns their output, both standard output and standard error, when given a
 * {@link BuildRunnerContext}.
 */
final class MavenCommandExecutor {

    private final BuildRunnerContext runnerContext;

    public MavenCommandExecutor(BuildRunnerContext context) {
        this.runnerContext = context;
    }

    @NotNull
    public Result execute(String args) throws IOException {
        File mavenExe = getMavenExe();
        if (mavenExe == null) {
            return new Result();
        }

        String command = mavenExe.getAbsolutePath() + " " + args;
        ProcessBuilder processBuilder = new ProcessBuilder(command.split(" "))
            .redirectErrorStream(true);
        return new Result(processBuilder.start());
    }

    @Nullable
    private File getMavenExe() {
        try {
            String mavenPath = runnerContext.getToolPath("maven");
            String mvnExe = System.getProperty("os.name").toLowerCase().contains("win") ? "mvn.cmd" : "mvn";
            return new File(mavenPath, "bin/" + mvnExe);
        } catch (ToolCannotBeFoundException e) {
            return null;
        }
    }

    public static class Result {

        private final Process process;
        private String output;

        public Result() {
            process = null;
        }

        public Result(Process process) {
            this.process = process;
        }

        public boolean isSuccessful() throws InterruptedException {
            if (process == null) {
                return false;
            }

            process.waitFor();

            return process.exitValue() == 0;
        }

        @NotNull
        public String getOutput() throws InterruptedException, IOException {
            if (!isSuccessful() || process == null) {
                return "";
            }

            if (output == null) {
                // this logic eagerly consumes the entire output into memory, which should not be an issue when only
                // used for `mvn --version`, which generates ~5 lines of output
                // this may should be revisited if other commands are executed here
                StringBuilder sb = new StringBuilder();
                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

                int ch;
                while ((ch = reader.read()) != -1) {
                    sb.append((char) ch);
                }

                output = sb.toString();
            }

            return output;
        }
    }
}
