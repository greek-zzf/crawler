package dao;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import po.News;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Zhouzf
 * @date 2021/7/5/005 8:53
 */
public class MybatisCrawlerDao implements CrawlerDao {

    private SqlSessionFactory sqlSessionFactory;

    public MybatisCrawlerDao() {
        String resource = "db/mybatis/config.xml";
        try (InputStream inputStream = Resources.getResourceAsStream(resource)) {
            sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public String getNextLinkThenDelete() {
        try (SqlSession session = sqlSessionFactory.openSession(true)) {
            String link = (String) session.selectOne("db.NewsMapper.selectNextAvailableLink");

            if (link != null) {
                session.delete("db.NewsMapper.deleteLink", link);
            }
            return link;
        }
    }

    @Override
    public String getNextLink(String sql) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            News news = (News) session.selectOne("org.mybatis.example.BlogMapper.selectBlog", 101);
        }

        return null;
    }

    @Override
    public void updateDatabase(String link, String sql) {

    }

    @Override
    public void insertIntoDatabase(String title, String content, String link) {
        try (SqlSession session = sqlSessionFactory.openSession(true)) {
            session.insert("db.NewsMapper.insertNews", new News(title, content, link));
        }
    }

    @Override
    public boolean isLinkProcessed(String link) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            int count = session.selectOne("db.NewsMapper.countLink", link);
            return count != 0;
        }
    }

    @Override
    public void insertProcessedLink(String link) {
        Map<String, String> param = new HashMap<>();
        param.put("tableName", "LINKS_TO_BE_PROCESSED");
        param.put("link", link);
        try (SqlSession session = sqlSessionFactory.openSession(true)) {
            session.insert("db.NewsMapper.insertLink", param);
        }
    }

    @Override
    public void insertLinkToBeProcessed(String link) {
        Map<String, String> param = new HashMap<>();
        param.put("tableName", "LINKS_ALREADY_PROCESSED");
        param.put("link", link);
        try (SqlSession session = sqlSessionFactory.openSession(true)) {
            session.insert("db.NewsMapper.insertLink", param);
        }

    }
}
