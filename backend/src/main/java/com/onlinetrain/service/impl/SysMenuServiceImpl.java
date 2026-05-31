package com.onlinetrain.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.onlinetrain.entity.SysMenu;
import com.onlinetrain.entity.SysRoleMenu;
import com.onlinetrain.entity.SysUserRole;
import com.onlinetrain.entity.User;
import com.onlinetrain.mapper.SysMenuMapper;
import com.onlinetrain.mapper.SysRoleMenuMapper;
import com.onlinetrain.mapper.SysUserRoleMapper;
import com.onlinetrain.mapper.UserMapper;
import com.onlinetrain.service.SysMenuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class SysMenuServiceImpl extends ServiceImpl<SysMenuMapper, SysMenu> implements SysMenuService {

    @Autowired
    private SysUserRoleMapper sysUserRoleMapper;

    @Autowired
    private SysRoleMenuMapper sysRoleMenuMapper;

    @Autowired
    private SysMenuMapper sysMenuMapper;

    @Autowired
    private UserMapper userMapper;

    @Override
    public List<String> getUserPermissions(Long userId) {
        // 管理员拥有所有权限，无需走RBAC关联查询
        User user = userMapper.selectById(userId);
        if (user != null && "ADMIN".equals(user.getRole())) {
            return sysMenuMapper.selectList(
                    new LambdaQueryWrapper<SysMenu>()
                            .isNotNull(SysMenu::getPermissionCode)
                            .ne(SysMenu::getPermissionCode, "")
            ).stream().map(SysMenu::getPermissionCode).filter(Objects::nonNull).distinct().collect(Collectors.toList());
        }

        // 查询用户的角色ID列表
        List<Long> roleIds = sysUserRoleMapper.selectList(
                new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, userId)
        ).stream().map(SysUserRole::getRoleId).collect(Collectors.toList());

        if (roleIds.isEmpty()) return Collections.emptyList();

        // 查询角色关联的菜单ID列表
        List<Long> menuIds = sysRoleMenuMapper.selectList(
                new LambdaQueryWrapper<SysRoleMenu>().in(SysRoleMenu::getRoleId, roleIds)
        ).stream().map(SysRoleMenu::getMenuId).collect(Collectors.toList());

        if (menuIds.isEmpty()) return Collections.emptyList();

        // 查询菜单中的权限标识
        return sysMenuMapper.selectList(
                new LambdaQueryWrapper<SysMenu>()
                        .in(SysMenu::getId, menuIds)
                        .isNotNull(SysMenu::getPermissionCode)
                        .ne(SysMenu::getPermissionCode, "")
        ).stream().map(SysMenu::getPermissionCode).filter(Objects::nonNull).distinct().collect(Collectors.toList());
    }

    @Override
    public List<SysMenu> getUserMenus(Long userId) {
        // 管理员拥有所有菜单，无需走RBAC关联查询
        User user = userMapper.selectById(userId);
        if (user != null && "ADMIN".equals(user.getRole())) {
            List<SysMenu> allMenus = sysMenuMapper.selectList(
                    new LambdaQueryWrapper<SysMenu>()
                            .eq(SysMenu::getType, "MENU")
                            .eq(SysMenu::getVisible, 1)
                            .orderByAsc(SysMenu::getSortOrder)
            );
            return buildTree(allMenus);
        }

        List<String> permissions = getUserPermissions(userId);
        if (permissions.isEmpty()) return Collections.emptyList();

        // 查询所有菜单权限对应的菜单
        Set<String> permSet = new HashSet<>(permissions);
        List<SysMenu> allMenus = sysMenuMapper.selectList(
                new LambdaQueryWrapper<SysMenu>()
                        .eq(SysMenu::getType, "MENU")
                        .eq(SysMenu::getVisible, 1)
                        .orderByAsc(SysMenu::getSortOrder)
        );

        // 过滤出用户有权限的菜单（包括有子菜单的父菜单）
        Set<Long> allowedMenuIds = new HashSet<>();
        // 收集所有BUTTON类型权限对应的parent_id
        List<SysMenu> buttonMenus = sysMenuMapper.selectList(
                new LambdaQueryWrapper<SysMenu>()
                        .eq(SysMenu::getType, "BUTTON")
        );
        for (SysMenu btn : buttonMenus) {
            if (btn.getPermissionCode() != null && permSet.contains(btn.getPermissionCode())) {
                allowedMenuIds.add(btn.getParentId());
            }
        }

        // 筛选用户可见的菜单
        List<SysMenu> visible = new ArrayList<>();
        for (SysMenu menu : allMenus) {
            if (menu.getPermissionCode() != null && permSet.contains(menu.getPermissionCode())) {
                visible.add(menu);
                allowedMenuIds.add(menu.getId());
            } else if (menu.getPermissionCode() == null || menu.getPermissionCode().isEmpty()) {
                // 无权限码的菜单（父菜单），检查是否有子菜单可见
                visible.add(menu);
            }
        }

        // 构建菜单树
        return buildTree(visible);
    }

    private List<SysMenu> buildTree(List<SysMenu> menus) {
        Map<Long, List<SysMenu>> childrenMap = new HashMap<>();
        List<SysMenu> roots = new ArrayList<>();

        for (SysMenu menu : menus) {
            if (menu.getParentId() == null || menu.getParentId() == 0) {
                roots.add(menu);
            } else {
                childrenMap.computeIfAbsent(menu.getParentId(), k -> new ArrayList<>()).add(menu);
            }
        }

        // 只保留有子菜单或无子菜单但在allowed中的根菜单
        List<SysMenu> result = new ArrayList<>();
        for (SysMenu root : roots) {
            List<SysMenu> children = childrenMap.get(root.getId());
            if (children != null && !children.isEmpty()) {
                root.setChildren(children);
                result.add(root);
            } else if (root.getPath() != null && !root.getPath().isEmpty()) {
                result.add(root);
            }
        }

        return result;
    }

    @Override
    public List<Long> getRoleMenuIds(Long roleId) {
        return sysRoleMenuMapper.selectList(
                new LambdaQueryWrapper<SysRoleMenu>().eq(SysRoleMenu::getRoleId, roleId)
        ).stream().map(SysRoleMenu::getMenuId).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void saveRoleMenus(Long roleId, List<Long> menuIds) {
        sysRoleMenuMapper.delete(new LambdaQueryWrapper<SysRoleMenu>().eq(SysRoleMenu::getRoleId, roleId));
        if (menuIds != null) {
            for (Long menuId : menuIds) {
                SysRoleMenu rm = new SysRoleMenu();
                rm.setRoleId(roleId);
                rm.setMenuId(menuId);
                sysRoleMenuMapper.insert(rm);
            }
        }
    }
}
