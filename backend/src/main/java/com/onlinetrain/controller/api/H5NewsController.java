package com.onlinetrain.controller.api;

import com.onlinetrain.common.Result;
import com.onlinetrain.entity.NewsArticle;
import com.onlinetrain.service.NewsArticleService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * H5新闻资讯接口（无需登录）
 */
@RestController
@RequestMapping("/api/news")
@Api(tags = "H5-新闻资讯接口")
public class H5NewsController {

    @Autowired
    private NewsArticleService newsArticleService;

    @GetMapping
    @ApiOperation("新闻列表（已发布，按排序+时间）")
    public Result<List<NewsArticle>> list() {
        List<NewsArticle> list = newsArticleService.lambdaQuery()
                .eq(NewsArticle::getStatus, 1)
                .orderByAsc(NewsArticle::getSortOrder)
                .orderByDesc(NewsArticle::getCreateTime)
                .list();
        return Result.ok(list);
    }

    @GetMapping("/{id}")
    @ApiOperation("新闻详情")
    public Result<NewsArticle> detail(@PathVariable Long id) {
        NewsArticle article = newsArticleService.getById(id);
        if (article == null || article.getStatus() != 1) {
            return Result.notFound("新闻不存在");
        }
        // 增加阅读量
        article.setViewCount((article.getViewCount() == null ? 0 : article.getViewCount()) + 1);
        newsArticleService.updateById(article);
        return Result.ok(article);
    }
}
