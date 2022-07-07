package nu.studer.teamcity.buildscan.agent;

import jetbrains.buildServer.agent.BuildRunnerContext;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
        String command = getMavenExe().getAbsolutePath() + " " + args;
        ProcessBuilder processBuilder = new ProcessBuilder(command.split(" "))
            .redirectErrorStream(true);
        return new Result(processBuilder.start());
    }

    private File getMavenExe() {
        String mavenPath = runnerContext.getToolPath("maven");
        String mvnExe = System.getProperty("os.name").toLowerCase().contains("win") ? "mvn.cmd" : "mvn";
        return new File(mavenPath, "bin/" + mvnExe);
    }

    public static class Result {

        public final Process process;
        private String output;

        public Result(Process process) {
            this.process = process;
        }

        @NotNull
        public String getOutput() throws InterruptedException, IOException {
            if (output == null) {
                process.waitFor();

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

        @NotNull
        public Matcher match(Pattern pattern) throws InterruptedException, IOException {
            return pattern.matcher(getOutput());
        }
    }
}
