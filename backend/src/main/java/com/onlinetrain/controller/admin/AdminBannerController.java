package com.onlinetrain.controller.admin;

import com.onlinetrain.common.Result;
import com.onlinetrain.entity.Banner;
import com.onlinetrain.service.BannerService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@Api(tags = "管理端-Banner轮播图")
public class AdminBannerController {

    @Autowired
    private BannerService bannerService;

    @GetMapping("/banners")
    @ApiOperation("Banner列表")
    public Result<List<Banner>> list() {
        List<Banner> list = bannerService.lambdaQuery()
                .orderByAsc(Banner::getSortOrder)
                .list();
        return Result.ok(list);
    }

    @PostMapping("/banners")
    @ApiOperation("创建Banner")
    public Result<Banner> create(@RequestBody Banner banner) {
        if (banner.getStatus() == null) banner.setStatus(1);
        if (banner.getSortOrder() == null) banner.setSortOrder(0);
        bannerService.save(banner);
        return Result.ok(banner);
    }

    @PutMapping("/banners/{id}")
    @ApiOperation("更新Banner")
    public Result<Banner> update(@PathVariable Long id, @RequestBody Banner banner) {
        banner.setId(id);
        bannerService.updateById(banner);
        return Result.ok(banner);
    }

    @DeleteMapping("/banners/{id}")
    @ApiOperation("删除Banner")
    public Result<Void> delete(@PathVariable Long id) {
        bannerService.removeById(id);
        return Result.ok();
    }

    @PutMapping("/banners/{id}/status")
    @ApiOperation("切换Banner状态")
    public Result<Void> toggleStatus(@PathVariable Long id, @RequestBody Map<String, Object> params) {
        Banner banner = bannerService.getById(id);
        if (banner != null && params.get("status") != null) {
            banner.setStatus(Integer.valueOf(params.get("status").toString()));
            bannerService.updateById(banner);
        }
        return Result.ok();
    }
}
