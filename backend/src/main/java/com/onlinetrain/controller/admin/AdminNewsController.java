package com.onlinetrain.controller.admin;

import com.onlinetrain.common.Result;
import com.onlinetrain.entity.NewsArticle;
import com.onlinetrain.service.NewsArticleService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@Api(tags = "管理端-新闻资讯")
public class AdminNewsController {

    @Autowired
    private NewsArticleService newsArticleService;

    @GetMapping("/news")
    @ApiOperation("新闻列表")
    public Result<List<NewsArticle>> list() {
        List<NewsArticle> list = newsArticleService.lambdaQuery()
                .orderByAsc(NewsArticle::getSortOrder)
                .orderByDesc(NewsArticle::getCreateTime)
                .list();
        return Result.ok(list);
    }

    @GetMapping("/news/{id}")
    @ApiOperation("新闻详情")
    public Result<NewsArticle> detail(@PathVariable Long id) {
        NewsArticle article = newsArticleService.getById(id);
        if (article == null) {
            return Result.notFound("新闻不存在");
        }
        return Result.ok(article);
    }

    @PostMapping("/news")
    @ApiOperation("创建新闻")
    public Result<NewsArticle> create(@RequestBody NewsArticle article) {
        if (article.getStatus() == null) article.setStatus(1);
        if (article.getSortOrder() == null) article.setSortOrder(0);
        if (article.getViewCount() == null) article.setViewCount(0);
        newsArticleService.save(article);
        return Result.ok(article);
    }

    @PutMapping("/news/{id}")
    @ApiOperation("更新新闻")
    public Result<NewsArticle> update(@PathVariable Long id, @RequestBody NewsArticle article) {
        article.setId(id);
        newsArticleService.updateById(article);
        return Result.ok(article);
    }

    @DeleteMapping("/news/{id}")
    @ApiOperation("删除新闻")
    public Result<Void> delete(@PathVariable Long id) {
        newsArticleService.removeById(id);
        return Result.ok();
    }

    @PutMapping("/news/{id}/status")
    @ApiOperation("切换新闻状态")
    public Result<Void> toggleStatus(@PathVariable Long id, @RequestBody Map<String, Object> params) {
        NewsArticle article = newsArticleService.getById(id);
        if (article != null && params.get("status") != null) {
            article.setStatus(Integer.valueOf(params.get("status").toString()));
            newsArticleService.updateById(article);
        }
        return Result.ok();
    }
}
