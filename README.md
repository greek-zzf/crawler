# crawler
多线程爬虫项目

爬取新浪新闻首页数据，并使用多线程进行优化，后期使用 ES 进行数据搜索

该项目整合了 MySQL 和 H2 数据库，想迅速体验推荐使用 H2 数据库。

使用方式：clone 该项目，使用 H2 数据库配置，执行命令 `mvn flyway:migrate`，执行成功后，打开 Main.java 运行即可。

注意：
- 执行命令 `mvn flyway:migrate`前，需要建好数据库 news，该命令会自动创建表以及初始数据。
- 不知道如何执行命令，可以直接在 IDEA（不要说没有 IDEA，现在是 2021 年了） 点击 ![image.png](https://i.loli.net/2021/07/07/9GYHycZE7BnWX8m.png)
- 切换数据库配置需要更改两处，分别是 `resources/db/mybatis/config.xml`，`pom.xml`。注释掉 MySQL 配置，放开 H2 配置即可。
使用 MySQL 同理。

