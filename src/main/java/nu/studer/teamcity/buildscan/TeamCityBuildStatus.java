package nu.studer.teamcity.buildscan;

import jetbrains.buildServer.messages.Status;
import org.jetbrains.annotations.NotNull;

public enum TeamCityBuildStatus {

    SUCCESS, FAILURE, ERROR, UNKNOWN;

    @NotNull
    public static TeamCityBuildStatus from(@NotNull Status status) {
        if (status.equals(Status.NORMAL)) {
            return SUCCESS;
        } else if (status.equals(Status.FAILURE)) {
            return FAILURE;
        } else if (status.equals(Status.ERROR)) {
            return ERROR;
        } else {
            return UNKNOWN;
        }
    }

}
