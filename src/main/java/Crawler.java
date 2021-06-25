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

            String link = LINKS_POOL.remove(LINKS_POOL.size() - 1);

            if (PROCESSED_LINKS.contains(link)) {
                continue;
            }

            if (!link.contains("sina.cn")) {
                continue;

            } else {

                CloseableHttpClient httpclient = HttpClients.createDefault();
                HttpGet httpGet = new HttpGet(link);
                httpGet.addHeader(HttpHeaders.USER_AGENT, "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/89.0.4389.114 Safari/537.36");

                try (CloseableHttpResponse response = httpclient.execute(httpGet)) {
                    HttpEntity entity = response.getEntity();

                    System.out.println(link);
                    System.out.println(response.getStatusLine());

                    String html = EntityUtils.toString(entity);
                    Document doc = Jsoup.parse(html);

                    Elements elements = doc.select("a[href]");

                    for (Element element : elements) {
                        String hrefLink = element.attr("href");

                        if (hrefLink.contains("news.sina.cn")) {
                            LINKS_POOL.add(hrefLink);
                        }

                    }

                    Elements articleTag = doc.select("article");
                    if (!articleTag.isEmpty()) {
                        for (Element element : articleTag) {
                            System.out.println(element.child(0).text());
                        }
                    }

                    PROCESSED_LINKS.add(link);
                }
            }

        }


    }

}
