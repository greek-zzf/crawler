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
 * 使用传统方式执行 sql
 *
 * @author Zhouzf
 * @date 2021/7/5/005 8:53
 */
public class MybatisOldCrawlerDao implements CrawlerDao {

    private SqlSessionFactory sqlSessionFactory;

    public MybatisOldCrawlerDao() {
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
            String link = (String) session.selectOne("mapper.NewsMapper.selectNextAvailableLink");

            if (link != null) {
                session.delete("mapper.NewsMapper.deleteLink", link);
            }
            return link;
        }
    }


    @Override
    public void insertIntoDatabase(String title, String content, String link) {
        try (SqlSession session = sqlSessionFactory.openSession(true)) {
            session.insert("mapper.NewsMapper.insertNews", new News(title, content, link));
        }
    }

    @Override
    public boolean isLinkProcessed(String link) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            int count = session.selectOne("mapper.NewsMapper.countLink", link);
            return count != 0;
        }
    }

    @Override
    public void insertProcessedLink(String link) {
        insertLink(link,"LINKS_ALREADY_PROCESSED");
    }

    @Override
    public void insertLinkToBeProcessed(String link) {
        insertLink(link,"LINKS_TO_BE_PROCESSED");
    }

    private void insertLink(String link, String tableName) {
        Map<String, String> param = new HashMap<>();
        param.put("tableName", tableName);
        param.put("link", link);
        try (SqlSession session = sqlSessionFactory.openSession(true)) {
            session.insert("mapper.NewsMapper.insertLink", param);
        }
    }
}
