package nu.studer.teamcity.buildscan.internal.cleanup;

import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.SQLRunner;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
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

    public Integer count() {
        return server.getSQLRunner().runSql(connection -> {
            try (Statement statement = connection.createStatement()) {
                ResultSet resultSet = statement.executeQuery("select count(data_id) from custom_data where data_key='" + BUILD_SCAN_STORAGE_ID + "'");
                resultSet.next();
                return resultSet.getInt("count");
            }
        });
    }

    public void cleanup() {
        List<Long> dataIds = server.getSQLRunner().runSql((SQLRunner.SQLAction<List<Long>>) connection -> {
            ArrayList<Long> ids = new ArrayList<>();
            try (Statement statement = connection.createStatement()) {
                ResultSet resultSet = statement.executeQuery("select data_id from custom_data where data_key='" + BUILD_SCAN_STORAGE_ID + "'");
                while (resultSet.next()) {
                    ids.add(resultSet.getLong("data_id"));
                }
            }
            return ids;
        });

        final Integer allEntriesCount = dataIds.size();

        server.getSQLRunner().runSql(new SQLRunner.NoResultSQLAction() {
            @Override
            public void run(Connection connection) throws SQLException {
                boolean autoCommitSetting = connection.getAutoCommit();
                connection.setAutoCommit(false);
                try (PreparedStatement deleteCustomDataBody = deleteCustomDataBodySQLStatement(connection); PreparedStatement deleteCustomData = deleteCustomDataSQLStatement(connection)) {
                    for (int count = 1; count <= dataIds.size();count++) {
                        Long dataId = dataIds.get(count-1);
                        int remaining = allEntriesCount - count ;
                        deleteCustomDataBody.setLong(1, dataId);
                        deleteCustomData.setLong(1, dataId);
                        deleteCustomDataBody.addBatch();
                        deleteCustomData.addBatch();
                        if (count % BATCH_SIZE == 0) {
                            executeBatch(deleteCustomDataBody, deleteCustomData);
                        } else if (remaining == 0) {
                            executeBatch(deleteCustomDataBody, deleteCustomData);
                        }
                    }
                } finally {
                    connection.setAutoCommit(autoCommitSetting);
                }
            }

            private PreparedStatement deleteCustomDataBodySQLStatement(Connection connection) throws SQLException {
                return connection.prepareStatement(DELETE_CUSTOM_DATA_BODY_SQL);
            }

            private PreparedStatement deleteCustomDataSQLStatement(Connection connection) throws SQLException {
                return connection.prepareStatement(DELETE_FROM_CUSTOM_DATA_SQL);
            }

            private void executeBatch(PreparedStatement deleteCustomDataBody, PreparedStatement deleteCustomData) throws SQLException {
                deleteCustomDataBody.executeBatch();
                deleteCustomData.executeBatch();
                deleteCustomData.getConnection().commit();
            }
        });
    }

}
