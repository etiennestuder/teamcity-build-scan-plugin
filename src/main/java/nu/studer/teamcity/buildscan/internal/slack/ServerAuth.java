package nu.studer.teamcity.buildscan.internal.slack;

import org.jetbrains.annotations.NotNull;

final class ServerAuth {

    final String server;
    final PasswordCredentials credentials;

    private ServerAuth(String server, PasswordCredentials credentials) {
        this.server = server;
        this.credentials = credentials;
    }

    @NotNull
    static ServerAuth fromConfigString(@NotNull String s) {
        String[] serverAndCredentials = s.split("=>", 2);
        String server = serverAndCredentials[0];
        String credentials = serverAndCredentials[1];
        String[] usernameAndPassword = credentials.split(":", 2);
        PasswordCredentials passwordCredentials = new PasswordCredentials(usernameAndPassword[0], usernameAndPassword[1]);
        return new ServerAuth(server, passwordCredentials);
    }

}
