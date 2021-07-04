package dao;

import java.sql.SQLException;

/**
 * @author Zhouzf
 * @Date 2020-07-04
 */
public interface CrawlerDao {
    String getNextLinkThenDelete() throws SQLException;

    String getNextLink(String sql) throws SQLException;

    void updateDatabase(String link, String sql) throws SQLException;

    void insertIntoDatabase(String title, String content, String link) throws SQLException;

    boolean isLinkProcessed(String link) throws SQLException;

}
