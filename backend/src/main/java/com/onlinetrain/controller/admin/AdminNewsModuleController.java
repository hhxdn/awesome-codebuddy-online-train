package com.onlinetrain.controller.admin;

import com.onlinetrain.common.Result;
import com.onlinetrain.entity.NewsModule;
import com.onlinetrain.service.NewsModuleService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@Api(tags = "管理端-新闻模块管理")
public class AdminNewsModuleController {

    @Autowired
    private NewsModuleService newsModuleService;

    @GetMapping("/news-modules")
    @ApiOperation("模块列表")
    public Result<List<NewsModule>> list() {
        List<NewsModule> list = newsModuleService.lambdaQuery()
                .orderByAsc(NewsModule::getSortOrder)
                .list();
        return Result.ok(list);
    }

    @PostMapping("/news-modules")
    @ApiOperation("创建模块")
    public Result<NewsModule> create(@RequestBody NewsModule module) {
        if (module.getSortOrder() == null) module.setSortOrder(0);
        if (module.getStatus() == null) module.setStatus(1);
        newsModuleService.save(module);
        return Result.ok(module);
    }

    @PutMapping("/news-modules/{id}")
    @ApiOperation("更新模块")
    public Result<NewsModule> update(@PathVariable Long id, @RequestBody NewsModule module) {
        module.setId(id);
        newsModuleService.updateById(module);
        return Result.ok(module);
    }

    @DeleteMapping("/news-modules/{id}")
    @ApiOperation("删除模块")
    public Result<Void> delete(@PathVariable Long id) {
        newsModuleService.removeById(id);
        return Result.ok();
    }
}
