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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Zhouzf
 * @date 2021-6-21
 */
public class Crawler {

    private static final String URL = "https://sina.cn";
    private static final List<String> LINKS_POOL = new ArrayList<>();
    private static final Set<String> PROCESSED_LINKS = new HashSet<>();


    public static void main(String[] args) throws IOException {

        LINKS_POOL.add(URL);

        while (true) {

            if (LINKS_POOL.isEmpty()) {
                break;
            }

            // 列表使用 ArrayList，末尾删除元素效率较高
            String link = LINKS_POOL.remove(LINKS_POOL.size() - 1);

            // 如果是处理过的链接，跳出本次循环
            if (PROCESSED_LINKS.contains(link)) {
                continue;
            }

            // 如果是需要的新闻链接，则继续执行，否则跳出本次循环
            if (isInterestingLink(link)) {

                Document doc = httpGetAndParseHtml(link);
                // 添加链接到链接池中
                doc.select("a[href]").forEach(element -> LINKS_POOL.add(element.attr("href")));

                // 如果是新闻页面就插入到数据库
                storeIntoDatabaseIfItIsNewsPage(doc);
                PROCESSED_LINKS.add(link);
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
