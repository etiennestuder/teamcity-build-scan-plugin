package nu.studer.teamcity.buildscan.connection;

import jetbrains.buildServer.serverSide.Parameter;
import jetbrains.buildServer.serverSide.SBuild;
import jetbrains.buildServer.serverSide.SimpleParameter;
import jetbrains.buildServer.serverSide.parameters.types.PasswordsProvider;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

import static nu.studer.teamcity.buildscan.connection.GradleEnterpriseConnectionConstants.GRADLE_ENTERPRISE_ACCESS_KEY_ENV_VAR;

/**
 * This class implements {@link PasswordsProvider} in order to hide the env.GRADLE_ENTERPRISE_ACCESS_KEY secret in the
 * parameters output screen
 */
public final class GradleEnterprisePasswordProvider implements PasswordsProvider {
    @NotNull
    @Override
    public Collection<Parameter> getPasswordParameters(@NotNull SBuild build) {
        Collection<Parameter> passwordParameters = new ArrayList<Parameter>(1);
        String geAccessKey = build.getParametersProvider().get(GRADLE_ENTERPRISE_ACCESS_KEY_ENV_VAR);

        if (geAccessKey != null) {
            Parameter parameter = new SimpleParameter(GRADLE_ENTERPRISE_ACCESS_KEY_ENV_VAR, geAccessKey);
            passwordParameters.add(parameter);
        }

        return passwordParameters;
    }
}
