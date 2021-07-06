import dao.CrawlerDao;
import dao.MybatisNewCrawlerDao;

/**
 * @author Zhozf
 * @Date 2021-07-06
 */
public class Main {

    public static void main(String[] args) {
        CrawlerDao dao = new MybatisNewCrawlerDao();
        for (int i = 0; i < 4; ++i) {
            new Crawler(dao).start();
        }
    }
}
