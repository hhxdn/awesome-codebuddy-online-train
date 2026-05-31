package com.onlinetrain.controller.api;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.onlinetrain.common.Result;
import com.onlinetrain.entity.SystemConfig;
import com.onlinetrain.mapper.SystemConfigMapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 公开接口 — 关于我们等系统配置
 */
@RestController
@RequestMapping("/api/config")
@Api(tags = "公开-系统配置接口")
public class H5SystemConfigController {

    @Autowired
    private SystemConfigMapper systemConfigMapper;

    @GetMapping("/{key}")
    @ApiOperation("获取配置（如about_us）")
    public Result<SystemConfig> get(@PathVariable String key) {
        SystemConfig config = systemConfigMapper.selectOne(
                new LambdaQueryWrapper<SystemConfig>().eq(SystemConfig::getConfigKey, key));
        return Result.ok(config);
    }
}
