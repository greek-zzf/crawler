package dao;

import mapper.NewsMapper;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import po.News;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * 使用 Mapper 方法执行sql
 *
 * @author Zhouzf
 * @date 2021/7/6/006 16:40
 */
public class MybatisNewCrawlerDao implements CrawlerDao {

    private SqlSessionFactory sqlSessionFactory;

    public MybatisNewCrawlerDao() {
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
            NewsMapper mapper = session.getMapper(NewsMapper.class);

            String nextAvailableLink = mapper.selectNextAvailableLink();
            if (nextAvailableLink != null) {
                mapper.deleteLink(nextAvailableLink);
            }
            return nextAvailableLink;
        }
    }

    @Override
    public void insertIntoDatabase(String title, String content, String link) throws SQLException {
        try (SqlSession session = sqlSessionFactory.openSession(true)) {
            NewsMapper mapper = session.getMapper(NewsMapper.class);
            mapper.insertNews(new News(title, content, link));
        }
    }

    @Override
    public boolean isLinkProcessed(String link) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            NewsMapper mapper = session.getMapper(NewsMapper.class);
            int count = mapper.countLink(link);
            return count != 0;
        }
    }

    @Override
    public void insertProcessedLink(String link) {
        Map<String, String> param = new HashMap<>();
        param.put("tableName", "LINKS_ALREADY_PROCESSED");
        param.put("link", link);
        try (SqlSession session = sqlSessionFactory.openSession(true)) {
            NewsMapper mapper = session.getMapper(NewsMapper.class);
            mapper.insertLink(param);
        }
    }

    @Override
    public void insertLinkToBeProcessed(String link) {
        Map<String, String> param = new HashMap<>();
        param.put("tableName", "LINKS_TO_BE_PROCESSED");
        param.put("link", link);
        try (SqlSession session = sqlSessionFactory.openSession(true)) {
            NewsMapper mapper = session.getMapper(NewsMapper.class);
            mapper.insertLink(param);
        }

    }
}
