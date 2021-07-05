package dao;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import po.News;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;

/**
 * @author Zhouzf
 * @date 2021/7/5/005 8:53
 */
public class MybatisCrawlerDao implements CrawlerDao {

    private SqlSessionFactory sqlSessionFactory;

    public MybatisCrawlerDao() {
        String resource = "db/mybatis/config.xml";
        InputStream inputStream = null;
        try {
            inputStream = Resources.getResourceAsStream(resource);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
    }

    @Override
    public String getNextLinkThenDelete() throws SQLException {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            News blog = (News) session.selectOne("org.mybatis.example.BlogMapper.selectBlog", 101);
        }


        return null;
    }

    @Override
    public String getNextLink(String sql) throws SQLException {
        return null;
    }

    @Override
    public void updateDatabase(String link, String sql) throws SQLException {

    }

    @Override
    public void insertIntoDatabase(String title, String content, String link) throws SQLException {

    }

    @Override
    public boolean isLinkProcessed(String link) throws SQLException {
        return false;
    }
}
