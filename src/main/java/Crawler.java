import dao.CrawlerDao;
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
import java.sql.SQLException;
import java.util.stream.Collectors;


/**
 * @author Zhouzf
 * @date 2021-6-21
 */
public class Crawler extends Thread {

    private CrawlerDao dao;

    public Crawler(CrawlerDao dao) {
        this.dao = dao;
    }

    @Override
    public void run() {

        try {
            String link;
            // 从数据库中加载下一个链接，如果加载成功，则进行循环
            while ((link = dao.getNextLinkThenDelete()) != null) {

                // 判断链接是否被处理
                if (dao.isLinkProcessed(link)) {
                    continue;
                }

                if (isInterestingLink(link)) {

                    Document doc = httpGetAndParseHtml(link);

                    // 添加链接到待处理的数据表中
                    parseUrlsFromPageAndStoreIntoDatabase(doc);

                    // 如果是新闻页面就插入到数据库
                    storeIntoDatabaseIfItIsNewsPage(doc, link);

                    // 处理完成的链接放数据库
                    dao.insertProcessedLink(link);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    private void parseUrlsFromPageAndStoreIntoDatabase(Document doc) throws SQLException {
        for (Element element : doc.select("a[href]")) {
            String link = element.attr("href");
            if (!link.toLowerCase().startsWith("javascript")) {
                dao.insertLinkToBeProcessed(link);
            }
        }
    }

    private void storeIntoDatabaseIfItIsNewsPage(Document doc, String link) throws SQLException {
        Elements articleTag = doc.select("article");
        if (!articleTag.isEmpty()) {
            for (Element element : articleTag) {
                String title = element.child(0).text();
                String content = element.select("p").stream().map(Element::text).collect(Collectors.joining("\n"));
                dao.insertIntoDatabase(title, content, link);
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

    private static boolean isIndexPage(String link) {
        return link.equals("https://sina.cn");
    }

    private static boolean isNewsPage(String link) {
        return link.contains("news.sina.cn");
    }
}
