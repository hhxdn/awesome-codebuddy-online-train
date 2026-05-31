package com.onlinetrain.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.onlinetrain.common.PageResult;
import com.onlinetrain.common.Result;
import com.onlinetrain.entity.SysRole;
import com.onlinetrain.entity.SysRoleMenu;
import com.onlinetrain.entity.SysUserRole;
import com.onlinetrain.mapper.SysRoleMenuMapper;
import com.onlinetrain.mapper.SysUserRoleMapper;
import com.onlinetrain.service.SysMenuService;
import com.onlinetrain.service.SysRoleService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@Api(tags = "管理端-角色管理")
public class AdminRoleController {

    @Autowired
    private SysRoleService sysRoleService;

    @Autowired
    private SysMenuService sysMenuService;

    @Autowired
    private SysRoleMenuMapper sysRoleMenuMapper;

    @Autowired
    private SysUserRoleMapper sysUserRoleMapper;

    @GetMapping("/roles")
    @ApiOperation("角色列表")
    public Result<PageResult<SysRole>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        Page<SysRole> pageParam = new Page<>(page, size);
        return Result.ok(PageResult.of(sysRoleService.page(pageParam,
                new LambdaQueryWrapper<SysRole>().orderByDesc(SysRole::getCreateTime))));
    }

    @GetMapping("/roles/all")
    @ApiOperation("所有角色（下拉用）")
    public Result<List<SysRole>> all() {
        return Result.ok(sysRoleService.list(
                new LambdaQueryWrapper<SysRole>().eq(SysRole::getStatus, 1)));
    }

    @PostMapping("/roles")
    @ApiOperation("创建角色")
    public Result<SysRole> create(@RequestBody SysRole role) {
        sysRoleService.save(role);
        return Result.ok(role);
    }

    @PutMapping("/roles/{id}")
    @ApiOperation("更新角色")
    public Result<SysRole> update(@PathVariable Long id, @RequestBody SysRole role) {
        role.setId(id);
        sysRoleService.updateById(role);
        return Result.ok(role);
    }

    @DeleteMapping("/roles/{id}")
    @ApiOperation("删除角色")
    public Result<Void> delete(@PathVariable Long id) {
        sysRoleMenuMapper.delete(new LambdaQueryWrapper<SysRoleMenu>().eq(SysRoleMenu::getRoleId, id));
        sysUserRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getRoleId, id));
        sysRoleService.removeById(id);
        return Result.ok();
    }

    @GetMapping("/roles/{id}/menus")
    @ApiOperation("获取角色菜单")
    public Result<List<Long>> getRoleMenus(@PathVariable Long id) {
        return Result.ok(sysMenuService.getRoleMenuIds(id));
    }

    @PutMapping("/roles/{id}/menus")
    @ApiOperation("保存角色菜单")
    public Result<Void> saveRoleMenus(@PathVariable Long id, @RequestBody Map<String, List<Long>> params) {
        sysMenuService.saveRoleMenus(id, params.get("menuIds"));
        return Result.ok();
    }

    @PutMapping("/users/{userId}/roles")
    @ApiOperation("设置用户角色")
    public Result<Void> setUserRoles(@PathVariable Long userId, @RequestBody Map<String, List<Long>> params) {
        sysUserRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, userId));
        List<Long> roleIds = params.get("roleIds");
        if (roleIds != null) {
            for (Long roleId : roleIds) {
                SysUserRole ur = new SysUserRole();
                ur.setUserId(userId);
                ur.setRoleId(roleId);
                sysUserRoleMapper.insert(ur);
            }
        }
        return Result.ok();
    }

    @GetMapping("/users/{userId}/roles")
    @ApiOperation("获取用户角色")
    public Result<List<Long>> getUserRoles(@PathVariable Long userId) {
        return Result.ok(sysUserRoleMapper.selectList(
                new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, userId)
        ).stream().map(SysUserRole::getRoleId).collect(Collectors.toList()));
    }
}
