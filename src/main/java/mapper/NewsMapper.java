package mapper;

import po.News;

import java.util.Map;

/**
 * @author Zhouzf
 * @date 2021/7/5/005 9:24
 */
public interface NewsMapper {
    String selectNextAvailableLink();

    void deleteLink(String link);

    void insertNews(News news);

    int countLink(String link);

    void insertLink(Map param);
}
