package com.onlinetrain.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.onlinetrain.common.Result;
import com.onlinetrain.entity.SysMenu;
import com.onlinetrain.service.SysMenuService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@Api(tags = "管理端-菜单管理")
public class AdminMenuController {

    @Autowired
    private SysMenuService sysMenuService;

    @GetMapping("/menus")
    @ApiOperation("菜单列表（树形）")
    public Result<List<SysMenu>> list() {
        List<SysMenu> all = sysMenuService.list(
                new LambdaQueryWrapper<SysMenu>().orderByAsc(SysMenu::getSortOrder));
        return Result.ok(buildTree(all));
    }

    @GetMapping("/menus/all")
    @ApiOperation("所有菜单（平铺，分配权限用）")
    public Result<List<SysMenu>> all() {
        return Result.ok(sysMenuService.list(
                new LambdaQueryWrapper<SysMenu>().orderByAsc(SysMenu::getSortOrder)));
    }

    @PostMapping("/menus")
    @ApiOperation("创建菜单")
    public Result<SysMenu> create(@RequestBody SysMenu menu) {
        sysMenuService.save(menu);
        return Result.ok(menu);
    }

    @PutMapping("/menus/{id}")
    @ApiOperation("更新菜单")
    public Result<SysMenu> update(@PathVariable Long id, @RequestBody SysMenu menu) {
        menu.setId(id);
        sysMenuService.updateById(menu);
        return Result.ok(menu);
    }

    @DeleteMapping("/menus/{id}")
    @ApiOperation("删除菜单")
    public Result<Void> delete(@PathVariable Long id) {
        // 级联删除子菜单
        deleteChildren(id);
        sysMenuService.removeById(id);
        return Result.ok();
    }

    private void deleteChildren(Long parentId) {
        List<SysMenu> children = sysMenuService.list(
                new LambdaQueryWrapper<SysMenu>().eq(SysMenu::getParentId, parentId));
        for (SysMenu child : children) {
            deleteChildren(child.getId());
            sysMenuService.removeById(child.getId());
        }
    }

    private List<SysMenu> buildTree(List<SysMenu> menus) {
        java.util.Map<Long, List<SysMenu>> childrenMap = new java.util.HashMap<>();
        java.util.List<SysMenu> roots = new java.util.ArrayList<>();
        for (SysMenu menu : menus) {
            if (menu.getParentId() == null || menu.getParentId() == 0) {
                roots.add(menu);
            } else {
                childrenMap.computeIfAbsent(menu.getParentId(), k -> new java.util.ArrayList<>()).add(menu);
            }
        }
        for (SysMenu root : roots) {
            root.setChildren(childrenMap.get(root.getId()));
        }
        return roots;
    }
}
