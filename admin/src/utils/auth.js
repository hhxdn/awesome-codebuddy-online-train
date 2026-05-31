const TOKEN_KEY = 'token'
const USER_KEY = 'user'
const PERMISSIONS_KEY = 'permissions'
const MENUS_KEY = 'menus'

export function getToken() {
  return localStorage.getItem(TOKEN_KEY)
}

export function setToken(token) {
  localStorage.setItem(TOKEN_KEY, token)
}

export function removeToken() {
  localStorage.removeItem(TOKEN_KEY)
}

export function getUser() {
  const user = localStorage.getItem(USER_KEY)
  return user ? JSON.parse(user) : null
}

export function setUser(user) {
  localStorage.setItem(USER_KEY, JSON.stringify(user))
}

export function removeUser() {
  localStorage.removeItem(USER_KEY)
}

export function getPermissions() {
  const perms = localStorage.getItem(PERMISSIONS_KEY)
  return perms ? JSON.parse(perms) : []
}

export function setPermissions(permissions) {
  localStorage.setItem(PERMISSIONS_KEY, JSON.stringify(permissions || []))
}

export function removePermissions() {
  localStorage.removeItem(PERMISSIONS_KEY)
}

export function getMenus() {
  const menus = localStorage.getItem(MENUS_KEY)
  return menus ? JSON.parse(menus) : []
}

export function setMenus(menus) {
  localStorage.setItem(MENUS_KEY, JSON.stringify(menus || []))
}

export function removeMenus() {
  localStorage.removeItem(MENUS_KEY)
}

export function hasPermission(code) {
  const perms = getPermissions()
  return perms.includes(code) || perms.includes('*')
}

export function clearAuth() {
  removeToken()
  removeUser()
  removePermissions()
  removeMenus()
}
