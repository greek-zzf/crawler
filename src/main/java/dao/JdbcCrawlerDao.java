package dao;

import java.sql.*;

public class JdbcCrawlerDao implements CrawlerDao {

    private static final String USER_NAME = "root";
    private static final String PASSWORD = "root";

    private final Connection connection;

    public JdbcCrawlerDao() {
        try {
            this.connection = DriverManager.getConnection("jdbc:h2:file:./news", USER_NAME, PASSWORD);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }


    @Override
    public String getNextLinkThenDelete() throws SQLException {
        // 从数据库中获取需要处理的链接,然后删除
        String link = getNextLink("SELECT LINK FROM LINKS_TO_BE_PROCESSED LIMIT 1");
        if (link != null) {
            updateDatabase(link, "DELETE FROM LINKS_TO_BE_PROCESSED WHERE link = ?");
        }
        return link;
    }

    @Override
    public String getNextLink(String sql) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                return resultSet.getString("link");
            }
        }
        return null;
    }

    @Override
    public void updateDatabase(String link, String sql) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, link);
            statement.executeUpdate();
        }
    }


    @Override
    public void insertIntoDatabase(String title, String content, String link) throws SQLException {
        try (PreparedStatement preparedStatement = connection.prepareStatement("insert into news (title, content, url, created_at, modified_at) values (?,?,?,now(),now())")) {
            preparedStatement.setString(1, title);
            preparedStatement.setString(2, content);
            preparedStatement.setString(3, link);

            preparedStatement.executeUpdate();
        }


    }

    @Override
    public boolean isLinkProcessed(String link) throws SQLException {

        try (PreparedStatement statement = connection.prepareStatement("SELECT link FROM LINKS_ALREADY_PROCESSED WHERE link = ?")) {
            statement.setString(1, link);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                return true;
            }
        }
        return false;
    }


}
