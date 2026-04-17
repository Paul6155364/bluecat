import type { Directive, DirectiveBinding } from 'vue'

/**
 * 权限指令
 * 使用方式: v-permission="'user:add'"
 */
export const permission: Directive = {
  mounted(el: HTMLElement, binding: DirectiveBinding) {
    const { value } = binding
    const menus = localStorage.getItem('menus')

    if (!menus) {
      el.parentNode?.removeChild(el)
      return
    }

    const menuList = JSON.parse(menus)
    const permissions = getPermissions(menuList)

    if (value && typeof value === 'string') {
      if (!permissions.includes(value)) {
        el.parentNode?.removeChild(el)
      }
    }
  }
}

/**
 * 从菜单列表中提取所有权限编码
 */
function getPermissions(menus: any[]): string[] {
  const permissions: string[] = []

  function extractPermissions(menuList: any[]) {
    menuList.forEach(menu => {
      if (menu.menuCode) {
        permissions.push(menu.menuCode)
      }
      if (menu.children && menu.children.length > 0) {
        extractPermissions(menu.children)
      }
    })
  }

  extractPermissions(menus)
  return permissions
}

/**
 * 角色指令
 * 使用方式: v-role="'admin'"
 */
export const role: Directive = {
  mounted(el: HTMLElement, binding: DirectiveBinding) {
    const { value } = binding
    const userInfo = localStorage.getItem('userInfo')

    if (!userInfo) {
      el.parentNode?.removeChild(el)
      return
    }

    const user = JSON.parse(userInfo)
    const roles = user.roles || []

    if (value && typeof value === 'string') {
      if (!roles.some((role: any) => role.roleCode === value)) {
        el.parentNode?.removeChild(el)
      }
    }
  }
}
