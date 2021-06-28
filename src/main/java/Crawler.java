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
    private static final Set<String> processed_Links = new HashSet<>();


    private static List<String> loadUrlsFromDatabase(Connection connection, String sql) throws SQLException {
        List<String> results = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                System.out.println(resultSet.getString("1"));
                results.add(resultSet.getString("1"));
            }
        }
        return results;
    }

    public static void main(String[] args) throws IOException, SQLException {
        Connection connection = DriverManager.getConnection("jdbc:h2:file:./news", "root", "root");

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
            try (PreparedStatement statement = connection.prepareStatement("DELETE FROM LINKS_TO_BE_PROCESSED WHERE link = ?")) {
                statement.setString(1, link);
                statement.executeUpdate();
            }


            // 判断链接是否被处理

            boolean isProcessed = false;
            List<String> results = new ArrayList<>();
            try (PreparedStatement statement = connection.prepareStatement("SELECT link FORM LINKS_ALREADY_PROCESSED WHERE link = ?")) {
                statement.setString(1, link);
                ResultSet resultSet = statement.executeQuery();
                while (resultSet.next()) {
                    isProcessed = true;
                }
            }

            // 如果不是处理过的链接
            if (!isProcessed && isInterestingLink(link)) {


                Document doc = httpGetAndParseHtml(link);
                // 添加链接到链接池中（包括数据库）
                for (Element element : doc.select("a[href]")) {
                    linkPool.add(element.attr("href"));
                    try (PreparedStatement statement = connection.prepareStatement("INSERT INTO LINKS_TO_BE_PROCESSED (link) VALUES(?)")) {
                        statement.setString(1, link);
                        statement.executeUpdate();
                    }
                }

                // 如果是新闻页面就插入到数据库
                storeIntoDatabaseIfItIsNewsPage(doc);


                // 处理完成的链接放到已处理的池子中(包括数据库)

                processed_Links.add(link);
                try (PreparedStatement statement = connection.prepareStatement("INSERT INTO LINKS_ALREADY_PROCESSED (link) VALUES(?)")) {
                    statement.setString(1, link);
                    statement.executeUpdate();
                }

            }


        }


    }


    private static void storeIntoDatabaseIfItIsNewsPage(Document doc) {
        Elements articleTag = doc.select("article");
        if (!articleTag.isEmpty()) {
            for (Element element : articleTag) {
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
