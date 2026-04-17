import { get, post, put, del } from '@/utils/request'

/**
 * 用户管理 API
 */
export const userApi = {
  // 用户列表
  list: (params: any) => get('/system/user/list', params),

  // 新增用户
  add: (data: any) => post('/system/user', data),

  // 编辑用户
  update: (id: number, data: any) => put(`/system/user/${id}`, data),

  // 删除用户
  delete: (id: number) => del(`/system/user/${id}`),

  // 重置密码
  resetPwd: (id: number) => put(`/system/user/resetPwd/${id}`),

  // 分配角色
  assignRole: (data: any) => put('/system/user/assignRole', data),

  // 获取用户角色
  getRoles: (userId: number) => get(`/system/user/roles/${userId}`),

  // 修改密码
  updatePwd: (data: { oldPassword: string; newPassword: string }) => put('/system/user/updatePwd', data),

  // ==================== 数据权限管理 ====================

  // 获取用户授权的网吧配置列表
  getUserConfigs: (userId: number) => get(`/system/user/configs/${userId}`),

  // 保存用户授权网吧配置
  saveUserConfigs: (userId: number, configIds: number[]) => post(`/system/user/configs/${userId}`, configIds),

  // 获取所有网吧配置列表（用于授权选择）
  getAllConfigs: () => get('/system/user/all-configs'),

  // 获取用户未授权的网吧配置列表
  getUnauthorizedConfigs: (userId: number) => get(`/system/user/unauthorized-configs/${userId}`),

  // 更新用户数据权限范围
  updateDataScope: (userId: number, dataScope: number) => put(`/system/user/data-scope/${userId}`, { dataScope }),

  // 批量授权网吧配置给多个用户
  batchAssignConfigs: (userIds: number[], configIds: number[]) => post('/system/user/batch-configs', { userIds, configIds })
}

/**
 * 登录日志 API
 */
export const loginLogApi = {
  // 分页查询
  page: (params: any) => get('/system/login-log/page', params),

  // 登录统计
  stats: () => get('/system/login-log/stats'),

  // 最近登录用户
  recent: (limit: number = 10) => get('/system/login-log/recent', { limit }),

  // 删除日志
  delete: (ids: number[]) => del(`/system/login-log/${ids.join(',')}`),

  // 清空日志
  clear: () => del('/system/login-log/clear')
}

/**
 * 角色管理 API
 */
export const roleApi = {
  // 角色列表
  list: () => get('/system/role/list'),

  // 新增角色
  add: (data: any) => post('/system/role', data),

  // 编辑角色
  update: (id: number, data: any) => put(`/system/role/${id}`, data),

  // 删除角色
  delete: (id: number) => del(`/system/role/${id}`),

  // 分配菜单
  assignMenu: (data: any) => put('/system/role/assignMenu', data),

  // 获取角色菜单
  getMenus: (roleId: number) => get(`/system/role/menus/${roleId}`)
}

/**
 * 菜单管理 API
 */
export const menuApi = {
  // 菜单树
  tree: () => get('/system/menu/tree'),

  // 用户菜单
  userMenus: () => get('/system/menu/userMenus'),

  // 新增菜单
  add: (data: any) => post('/system/menu', data),

  // 编辑菜单
  update: (id: number, data: any) => put(`/system/menu/${id}`, data),

  // 删除菜单
  delete: (id: number) => del(`/system/menu/${id}`)
}
