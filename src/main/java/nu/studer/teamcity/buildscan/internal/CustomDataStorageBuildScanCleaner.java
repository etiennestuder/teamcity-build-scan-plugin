package nu.studer.teamcity.buildscan.internal;

import jetbrains.buildServer.serverSide.SBuildServer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import static nu.studer.teamcity.buildscan.internal.CustomDataStorageBuildScanDataStore.BUILD_SCAN_STORAGE_ID;

@SuppressWarnings("deprecation")
public final class CustomDataStorageBuildScanCleaner {

    private static final int BATCH_SIZE = 10;
    private static final String DELETE_CUSTOM_DATA_BODY_SQL = "delete from custom_data_body where id = ?";
    private static final String DELETE_FROM_CUSTOM_DATA_SQL = "delete from custom_data where data_id = ?";

    private final SBuildServer server;

    public CustomDataStorageBuildScanCleaner(SBuildServer buildServer) {
        server = buildServer;
    }

    public Integer itemsToRemove() {
        return server.getSQLRunner().runSql(connection -> {
            try (Statement statement = connection.createStatement()) {
                try (ResultSet resultSet = statement.executeQuery("select count(data_id) from custom_data where data_key='" + BUILD_SCAN_STORAGE_ID + "'")) {
                    if (resultSet.next()) {
                        return resultSet.getInt("count");
                    } else {
                        return 0;
                    }
                }
            }
        });
    }

    public void removeStoredItems() {
        List<Long> dataIds = new ArrayList<>();
        server.getSQLRunner().runSql(connection -> {
            try (Statement statement = connection.createStatement()) {
                try (ResultSet resultSet = statement.executeQuery("select data_id from custom_data where data_key='" + BUILD_SCAN_STORAGE_ID + "'")) {
                    while (resultSet.next()) {
                        dataIds.add(resultSet.getLong("data_id"));
                    }
                }
            }
        });

        server.getSQLRunner().runSql(connection -> {
            boolean autoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
            try (PreparedStatement deleteCustomDataBody = deleteCustomDataBodySQLStatement(connection); PreparedStatement deleteCustomData = deleteCustomDataSQLStatement(connection)) {
                for (int count = 0; count < dataIds.size(); count++) {
                    Long dataId = dataIds.get(count);
                    int remaining = dataIds.size() - count - 1;

                    deleteCustomDataBody.setLong(1, dataId);
                    deleteCustomDataBody.addBatch();

                    deleteCustomData.setLong(1, dataId);
                    deleteCustomData.addBatch();

                    if ((count + 1) % BATCH_SIZE == 0 || remaining == 0) {
                        executeBatch(deleteCustomDataBody, deleteCustomData);
                        connection.commit();
                    }
                }
            } finally {
                connection.setAutoCommit(autoCommit);
            }
        });
    }

    private static PreparedStatement deleteCustomDataBodySQLStatement(Connection connection) throws SQLException {
        return connection.prepareStatement(DELETE_CUSTOM_DATA_BODY_SQL);
    }

    private static PreparedStatement deleteCustomDataSQLStatement(Connection connection) throws SQLException {
        return connection.prepareStatement(DELETE_FROM_CUSTOM_DATA_SQL);
    }

    private static void executeBatch(PreparedStatement deleteCustomDataBody, PreparedStatement deleteCustomData) throws SQLException {
        deleteCustomDataBody.executeBatch();
        deleteCustomData.executeBatch();
    }

}
