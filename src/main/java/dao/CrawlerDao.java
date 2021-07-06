package dao;

import java.sql.SQLException;

/**
 * @author Zhouzf
 * @Date 2020-07-04
 */
public interface CrawlerDao {
    String getNextLinkThenDelete() throws SQLException;

    void insertIntoDatabase(String title, String content, String link) throws SQLException;

    boolean isLinkProcessed(String link) throws SQLException;

    void insertProcessedLink(String link) throws SQLException;

    void insertLinkToBeProcessed(String link) throws SQLException;
}
