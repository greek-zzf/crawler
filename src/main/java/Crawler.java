import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Zhouzf
 * @date 2021-6-21
 */
public class Crawler {

    private static List<String> linkPool = null;

    private static final String USER_NAME = "root";
    private static final String PASSWORD = "root";


    private static List<String> loadUrlsFromDatabase(Connection connection, String sql) throws SQLException {
        List<String> results = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                results.add(resultSet.getString("link"));
            }
        }
        return results;
    }

    public static void main(String[] args) throws IOException, SQLException {
        Connection connection = DriverManager.getConnection("jdbc:h2:file:./news", USER_NAME, PASSWORD);

        // 从数据库中获取需要处理的链接
        linkPool = loadUrlsFromDatabase(connection, "SELECT link FROM LINKS_TO_BE_PROCESSED");

        // 从数据库中获取已经处理过的连接池
        Set<String> processedLink = new HashSet<>(loadUrlsFromDatabase(connection, "SELECT link FROM LINKS_ALREADY_PROCESSED"));

        while (true) {

            if (linkPool.isEmpty()) {
                break;
            }

            // 删除已经处理（包括数据库）的链接
            String link = linkPool.remove(linkPool.size() - 1);
            deleteLinkFromDatabase(connection, link, "DELETE FROM LINKS_TO_BE_PROCESSED WHERE link = ?");

            // 判断链接是否被处理
            if (isLinkProcessed(connection, link)) {
                continue;
            }

            // 如果不是处理过的链接
            if (isInterestingLink(link)) {

                Document doc = httpGetAndParseHtml(link);

                // 添加链接到链接池中（包括数据库）
                parseUrlsfromPageAndStoreIntoDatabase(connection, link, doc);

                // 如果是新闻页面就插入到数据库
                storeIntoDatabaseIfItIsNewsPage(doc, connection);

                // 处理完成的链接放到已处理的池子中(包括数据库)
                processedLink.add(link);
                insertIntoDatabase(connection, link, "INSERT INTO LINKS_ALREADY_PROCESSED (link) VALUES(?)");

            }


        }


    }

    private static void deleteLinkFromDatabase(Connection connection, String link, String s) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(s)) {
            statement.setString(1, link);
            statement.executeUpdate();
        }
    }

    private static void parseUrlsfromPageAndStoreIntoDatabase(Connection connection, String link, Document doc) throws SQLException {
        for (Element element : doc.select("a[href]")) {
            linkPool.add(element.attr("href"));
            insertIntoDatabase(connection, link, "INSERT INTO LINKS_TO_BE_PROCESSED (link) VALUES(?)");
        }
    }

    private static boolean isLinkProcessed(Connection connection, String link) throws SQLException {

        try (PreparedStatement statement = connection.prepareStatement("SELECT link FROM LINKS_ALREADY_PROCESSED WHERE link = ?")) {
            statement.setString(1, link);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                return true;
            }
        }
        return false;
    }

    private static void insertIntoDatabase(Connection connection, String link, String sql) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, link);
            statement.executeUpdate();
        }
    }


    private static void storeIntoDatabaseIfItIsNewsPage(Document doc, Connection connection) throws SQLException {
        Elements articleTag = doc.select("article");
        if (!articleTag.isEmpty()) {
            for (Element element : articleTag) {
//                try (PreparedStatement statement = connection.prepareStatement("insert into news (title) value (?)")) {
//                    statement.setString(1, element.child(0).text());
//                    statement.executeUpdate();
//                }
                System.out.println(element.child(0).text());
            }
        }
    }

    private static Document httpGetAndParseHtml(String link) throws IOException {

        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet(link);
        httpGet.addHeader(HttpHeaders.USER_AGENT, "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/89.0.4389.114 Safari/537.36");

        try (CloseableHttpResponse response = httpclient.execute(httpGet)) {
            HttpEntity entity = response.getEntity();

            System.out.println(link);
            System.out.println(response.getStatusLine());

            String html = EntityUtils.toString(entity);
            return Jsoup.parse(html);
        }
    }

    private static boolean isInterestingLink(String link) {
        return (isIndexPage(link) || isNewsPage(link)) && isNotLoginPage(link);
    }

    private static boolean isNotLoginPage(String link) {
        return !link.contains("passport.sina.cn");
    }

    private static boolean isNewsPage(String link) {
        return link.contains("news.sina.cn");
    }

    private static boolean isIndexPage(String link) {
        return link.equals("https://sina.cn");
    }

}
