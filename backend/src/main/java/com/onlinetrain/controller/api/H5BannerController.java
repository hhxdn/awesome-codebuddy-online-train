package com.onlinetrain.controller.api;

import com.onlinetrain.common.Result;
import com.onlinetrain.entity.Banner;
import com.onlinetrain.service.BannerService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * H5 Banner接口（无需登录）
 */
@RestController
@RequestMapping("/api/banners")
@Api(tags = "H5-Banner接口")
public class H5BannerController {

    @Autowired
    private BannerService bannerService;

    @GetMapping
    @ApiOperation("获取启用的Banner列表")
    public Result<List<Banner>> list() {
        List<Banner> list = bannerService.lambdaQuery()
                .eq(Banner::getStatus, 1)
                .orderByAsc(Banner::getSortOrder)
                .list();
        return Result.ok(list);
    }
}
