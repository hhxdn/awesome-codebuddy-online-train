package com.onlinetrain.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.onlinetrain.common.Result;
import com.onlinetrain.entity.SystemConfig;
import com.onlinetrain.service.SystemConfigService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@Api(tags = "管理端-系统配置管理")
public class AdminSystemConfigController {

    @Autowired
    private SystemConfigService systemConfigService;

    // ====== 按 configKey 读写（给「关于我们」「系统配置」等页面使用） ======

    @GetMapping("/config/{key}")
    @ApiOperation("根据key获取配置")
    public Result<SystemConfig> getByKey(@PathVariable String key) {
        SystemConfig config = systemConfigService.getOne(
                new LambdaQueryWrapper<SystemConfig>().eq(SystemConfig::getConfigKey, key));
        return Result.ok(config);
    }

    @PutMapping("/config/{key}")
    @ApiOperation("根据key更新配置（不存在则自动创建）")
    public Result<SystemConfig> updateByKey(@PathVariable String key, @RequestBody Map<String, String> body) {
        String value = body.get("configValue");
        SystemConfig exist = systemConfigService.getOne(
                new LambdaQueryWrapper<SystemConfig>().eq(SystemConfig::getConfigKey, key));
        if (exist != null) {
            exist.setConfigValue(value);
            systemConfigService.updateById(exist);
            return Result.ok(exist);
        } else {
            SystemConfig config = new SystemConfig();
            config.setConfigKey(key);
            config.setConfigValue(value);
            systemConfigService.save(config);
            return Result.ok(config);
        }
    }

    // ====== CRUD 接口 ======

    @GetMapping("/system-configs")
    @ApiOperation("系统配置列表")
    public Result<List<SystemConfig>> list() {
        List<SystemConfig> list = systemConfigService.list();
        return Result.ok(list);
    }

    @PostMapping("/system-configs")
    @ApiOperation("创建系统配置")
    public Result<SystemConfig> create(@RequestBody SystemConfig config) {
        systemConfigService.save(config);
        return Result.ok(config);
    }

    @PutMapping("/system-configs/{id}")
    @ApiOperation("更新系统配置")
    public Result<SystemConfig> update(@PathVariable Long id, @RequestBody SystemConfig config) {
        config.setId(id);
        systemConfigService.updateById(config);
        return Result.ok(config);
    }

    @DeleteMapping("/system-configs/{id}")
    @ApiOperation("删除系统配置")
    public Result<Void> delete(@PathVariable Long id) {
        systemConfigService.removeById(id);
        return Result.ok();
    }
}
