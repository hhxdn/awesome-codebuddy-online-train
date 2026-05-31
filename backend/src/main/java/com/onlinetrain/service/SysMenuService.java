package com.onlinetrain.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.onlinetrain.entity.SysMenu;
import java.util.List;

public interface SysMenuService extends IService<SysMenu> {

    /**
     * 获取用户的所有权限标识（通过角色）
     */
    List<String> getUserPermissions(Long userId);

    /**
     * 获取用户可见的菜单树（通过角色）
     */
    List<SysMenu> getUserMenus(Long userId);

    /**
     * 获取角色拥有的菜单ID列表
     */
    List<Long> getRoleMenuIds(Long roleId);

    /**
     * 保存角色菜单关联
     */
    void saveRoleMenus(Long roleId, List<Long> menuIds);
}
