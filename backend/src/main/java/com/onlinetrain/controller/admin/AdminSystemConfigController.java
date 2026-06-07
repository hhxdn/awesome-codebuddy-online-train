package com.onlinetrain.controller.admin;

import com.onlinetrain.common.Result;
import com.onlinetrain.entity.SystemConfig;
import com.onlinetrain.service.SystemConfigService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@Api(tags = "管理端-系统配置管理")
public class AdminSystemConfigController {

    @Autowired
    private SystemConfigService systemConfigService;

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
