package com.onlinetrain.controller.api;

import com.onlinetrain.common.Result;
import com.onlinetrain.entity.NewsModule;
import com.onlinetrain.entity.SystemConfig;
import com.onlinetrain.service.NewsModuleService;
import com.onlinetrain.service.SystemConfigService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/config")
@Api(tags = "H5-配置接口")
public class H5ConfigController {

    @Autowired
    private NewsModuleService newsModuleService;

    @Autowired
    private SystemConfigService systemConfigService;

    @GetMapping("/news-modules")
    @ApiOperation("获取启用的新闻模块列表")
    public Result<List<NewsModule>> newsModules() {
        List<NewsModule> list = newsModuleService.lambdaQuery()
                .eq(NewsModule::getStatus, 1)
                .orderByAsc(NewsModule::getSortOrder)
                .list();
        return Result.ok(list);
    }

    @GetMapping("/enrollment-conditions")
    @ApiOperation("获取报考条件")
    public Result<Map<String, String>> enrollmentConditions() {
        Map<String, String> result = new HashMap<>();
        SystemConfig config = systemConfigService.lambdaQuery()
                .eq(SystemConfig::getConfigKey, "enrollment_conditions")
                .one();
        result.put("content", config != null ? config.getConfigValue() : "1. 遵纪守法，品行端正\n2. 具有大专及以上学历\n3. 身体健康，无不良嗜好\n4. 具备相关工作经验优先");
        return Result.ok(result);
    }

}
