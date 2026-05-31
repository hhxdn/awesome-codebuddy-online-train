package com.onlinetrain.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.onlinetrain.common.Result;
import com.onlinetrain.entity.SystemConfig;
import com.onlinetrain.mapper.SystemConfigMapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/admin/config")
@Api(tags = "管理后台-系统配置")
public class AdminSystemConfigController {

    @Autowired
    private SystemConfigMapper systemConfigMapper;

    @GetMapping("/{key}")
    @ApiOperation("获取配置")
    public Result<SystemConfig> get(@PathVariable String key) {
        SystemConfig config = systemConfigMapper.selectOne(
                new LambdaQueryWrapper<SystemConfig>().eq(SystemConfig::getConfigKey, key));
        return Result.ok(config);
    }

    @PutMapping("/{key}")
    @ApiOperation("更新配置")
    public Result<Void> update(@PathVariable String key, @RequestBody SystemConfig body) {
        SystemConfig config = systemConfigMapper.selectOne(
                new LambdaQueryWrapper<SystemConfig>().eq(SystemConfig::getConfigKey, key));
        if (config == null) {
            config = new SystemConfig();
            config.setConfigKey(key);
            config.setConfigValue(body.getConfigValue());
            systemConfigMapper.insert(config);
        } else {
            config.setConfigValue(body.getConfigValue());
            systemConfigMapper.updateById(config);
        }
        log.info("Config updated: {}={}", key, body.getConfigValue() != null ? "..." : null);
        return Result.ok();
    }
}
