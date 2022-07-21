package nu.studer.teamcity.buildscan.agent

import jetbrains.buildServer.agent.AgentBuildFeature
import jetbrains.buildServer.agent.AgentCheckoutMode
import jetbrains.buildServer.agent.AgentRunningBuild
import jetbrains.buildServer.agent.BuildAgentConfiguration
import jetbrains.buildServer.agent.BuildInterruptReason
import jetbrains.buildServer.agent.BuildParametersMap
import jetbrains.buildServer.agent.BuildProgressLogger
import jetbrains.buildServer.agent.BuildRunnerContext
import jetbrains.buildServer.agent.BuildRunnerSettings
import jetbrains.buildServer.agent.ToolCannotBeFoundException
import jetbrains.buildServer.agent.VirtualContext
import jetbrains.buildServer.agentServer.AgentBuild
import jetbrains.buildServer.artifacts.ArtifactDependencyInfo
import jetbrains.buildServer.parameters.ValueResolver
import jetbrains.buildServer.util.Option
import jetbrains.buildServer.util.PasswordReplacer
import jetbrains.buildServer.vcs.VcsChangeInfo
import jetbrains.buildServer.vcs.VcsRoot
import jetbrains.buildServer.vcs.VcsRootEntry
import org.jetbrains.annotations.NotNull

class TestBuildRunnerContext implements BuildRunnerContext {

    private final String runType
    private final AgentRunningBuild agentRunningBuild
    private final BuildParametersMap buildParameters
    private final Map<String, String> configParameters
    private final Map<String, String> runnerParameters
    private final Map<String, String> toolPaths

    TestBuildRunnerContext(String runType, File agentTempDirectory, Map<String, String> configParameters, Map<String, String> runnerParameters, Map<String, String> toolPaths = [:]) {
        this.runType = runType
        this.agentRunningBuild = new TestAgentRunningBuild(agentTempDirectory)
        this.buildParameters = new TestBuildParametersMap()
        this.configParameters = configParameters
        this.runnerParameters = runnerParameters
        this.toolPaths = toolPaths
    }

    @Override
    String getRunType() {
        return runType
    }

    @Override
    AgentRunningBuild getBuild() {
        return agentRunningBuild
    }

    @Override
    BuildParametersMap getBuildParameters() {
        return buildParameters
    }

    @Override
    Map<String, String> getConfigParameters() {
        return configParameters
    }

    @Override
    Map<String, String> getRunnerParameters() {
        return runnerParameters
    }

    @Override
    void addSystemProperty(@NotNull String key, @NotNull String value) {
        buildParameters.systemProperties.put(key, value)
    }

    @Override
    void addEnvironmentVariable(@NotNull String key, @NotNull String value) {
        buildParameters.environmentVariables.put(key, value)
    }

    @Override
    void addConfigParameter(@NotNull String key, @NotNull String value) {
        configParameters.put(key, value)
    }

    @Override
    void addRunnerParameter(@NotNull String key, @NotNull String value) {
        runnerParameters.put(key, value)
    }

    @Override
    String getToolPath(@NotNull String toolName) throws ToolCannotBeFoundException {
        if (!toolPaths.containsKey(toolName)) {
            throw new ToolCannotBeFoundException(toolName)
        }

        return toolPaths[toolName]
    }

    @Override
    ValueResolver getParametersResolver() {
        return null
    }

    @Override
    boolean parametersHaveReferencesTo(@NotNull Collection<String> keys) {
        return false
    }

    @Override
    boolean isVirtualContext() {
        return false
    }

    @Override
    VirtualContext getVirtualContext() {
        return null
    }

    @Override
    String getId() {
        return null
    }

    @Override
    String getName() {
        return null
    }

    @Override
    File getWorkingDirectory() {
        return null
    }

    private static final class TestBuildParametersMap implements BuildParametersMap {

        private final Map<String, String> sysProps = [:]
        private final Map<String, String> envVars = [:]

        @Override
        Map<String, String> getSystemProperties() {
            return sysProps
        }

        @Override
        Map<String, String> getEnvironmentVariables() {
            return envVars
        }

        @Override
        Map<String, String> getAllParameters() {
            return null
        }

    }

    private static final class TestAgentRunningBuild implements AgentRunningBuild {

        private final File agentTempDirectory

        TestAgentRunningBuild(File agentTempDirectory) {
            this.agentTempDirectory = agentTempDirectory
        }

        @Override
        File getAgentTempDirectory() {
            return agentTempDirectory
        }

        @Override
        File getCheckoutDirectory() {
            return null
        }

        @Override
        AgentCheckoutMode getEffectiveCheckoutMode() {
            return null
        }

        @Override
        File getWorkingDirectory() {
            return null
        }

        @Override
        String getArtifactsPaths() {
            return null
        }

        @Override
        boolean getFailBuildOnExitCode() {
            return false
        }

        @Override
        BuildParametersMap getBuildParameters() {
            return null
        }

        @Override
        Map<String, String> getRunnerParameters() {
            return null
        }

        @Override
        String getBuildNumber() {
            return null
        }

        @Override
        Map<String, String> getSharedConfigParameters() {
            return null
        }

        @Override
        void addSharedConfigParameter(@NotNull String key, @NotNull String value) {

        }

        @Override
        void addSharedSystemProperty(@NotNull String key, @NotNull String value) {

        }

        @Override
        void addSharedEnvironmentVariable(@NotNull String key, @NotNull String value) {

        }

        @Override
        BuildParametersMap getSharedBuildParameters() {
            return null
        }

        @Override
        ValueResolver getSharedParametersResolver() {
            return null
        }

        @Override
        Collection<AgentBuildFeature> getBuildFeatures() {
            return null
        }

        @Override
        Collection<AgentBuildFeature> getBuildFeaturesOfType(@NotNull String type) {
            return null
        }

        @Override
        void stopBuild(@NotNull String reason) {

        }

        @Override
        BuildInterruptReason getInterruptReason() {
            return null
        }

        @Override
        void interruptBuild(@NotNull String comment, boolean reQueue) {

        }

        @Override
        boolean isBuildFailingOnServer() throws InterruptedException {
            return false
        }

        @Override
        boolean isInAlwaysExecutingStage() {
            return false
        }

        @Override
        PasswordReplacer getPasswordReplacer() {
            return null
        }

        @Override
        Map<String, String> getArtifactStorageSettings() {
            return null
        }

        @Override
        String getProjectName() {
            return null
        }

        @Override
        String getBuildTypeId() {
            return null
        }

        @Override
        String getBuildTypeExternalId() {
            return null
        }

        @Override
        String getBuildTypeName() {
            return null
        }

        @Override
        long getBuildId() {
            return 0
        }

        @Override
        boolean isCleanBuild() {
            return false
        }

        @Override
        boolean isPersonal() {
            return false
        }

        @Override
        boolean isPersonalPatchAvailable() {
            return false
        }

        @Override
        boolean isCheckoutOnAgent() {
            return false
        }

        @Override
        boolean isCheckoutOnServer() {
            return false
        }

        @Override
        AgentBuild.CheckoutType getCheckoutType() {
            return null
        }

        @Override
        long getExecutionTimeoutMinutes() {
            return 0
        }

        @Override
        List<ArtifactDependencyInfo> getArtifactDependencies() {
            return null
        }

        @Override
        String getAccessUser() {
            return null
        }

        @Override
        String getAccessCode() {
            return null
        }

        @Override
        List<VcsRootEntry> getVcsRootEntries() {
            return null
        }

        @Override
        String getBuildCurrentVersion(@NotNull VcsRoot vcsRoot) {
            return null
        }

        @Override
        String getBuildPreviousVersion(@NotNull VcsRoot vcsRoot) {
            return null
        }

        @Override
        boolean isCustomCheckoutDirectory() {
            return false
        }

        @Override
        List<VcsChangeInfo> getVcsChanges() {
            return null
        }

        @Override
        List<VcsChangeInfo> getPersonalVcsChanges() {
            return null
        }

        @Override
        File getBuildTempDirectory() {
            return null
        }

        @Override
        BuildProgressLogger getBuildLogger() {
            return null
        }

        @Override
        BuildAgentConfiguration getAgentConfiguration() {
            return null
        }

        @Override
        <T> T getBuildTypeOptionValue(@NotNull Option<T> option) {
            return null
        }

        @Override
        File getDefaultCheckoutDirectory() {
            return null
        }

        @Override
        String getVcsSettingsHashForCheckoutMode(AgentCheckoutMode agentCheckoutMode) {
            return null
        }

        @Override
        List<BuildRunnerSettings> getBuildRunners() {
            return null
        }

        @Override
        String describe(boolean verbose) {
            return null
        }

    }

}
