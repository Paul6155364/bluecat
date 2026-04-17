<template>
  <a-layout class="layout">
    <a-layout-sider v-model:collapsed="collapsed" :trigger="null" collapsible>
      <div class="logo">
        <img src="@/assets/logo.svg" alt="logo" />
        <span v-if="!collapsed">CyberPulse</span>
      </div>
      <a-menu
        v-model:selectedKeys="selectedKeys"
        v-model:openKeys="openKeys"
        mode="inline"
        theme="dark"
      >
        <template v-for="menu in menuList" :key="menu.id">
          <!-- 有子菜单的情况 -->
          <a-sub-menu v-if="menu.children && menu.children.length > 0" :key="menu.id">
            <template #icon>
              <component :is="getIcon(menu.icon)" />
            </template>
            <template #title>{{ menu.menuName }}</template>
            <a-menu-item
              v-for="child in menu.children"
              :key="child.id"
              @click="handleMenuClick(child)"
            >
              <template #icon>
                <component :is="getIcon(child.icon)" />
              </template>
              <span>{{ child.menuName }}</span>
            </a-menu-item>
          </a-sub-menu>
          <!-- 没有子菜单的情况 -->
          <a-menu-item v-else :key="menu.id" @click="handleMenuClick(menu)">
            <template #icon>
              <component :is="getIcon(menu.icon)" />
            </template>
            <span>{{ menu.menuName }}</span>
          </a-menu-item>
        </template>
      </a-menu>
    </a-layout-sider>
    <a-layout>
      <a-layout-header class="header">
        <div class="header-left">
          <MenuUnfoldOutlined
            v-if="collapsed"
            class="trigger"
            @click="collapsed = !collapsed"
          />
          <MenuFoldOutlined
            v-else
            class="trigger"
            @click="collapsed = !collapsed"
          />
        </div>
        <div class="header-right">
          <a-dropdown>
            <a class="user-info" @click.prevent>
              <a-avatar :size="32" style="background-color: #1890ff">
                {{ userInfo?.realName?.charAt(0) || 'A' }}
              </a-avatar>
              <span class="username">{{ userInfo?.realName || '管理员' }}</span>
            </a>
            <template #overlay>
              <a-menu>
                <a-menu-item @click="showPwdModal">
                  <KeyOutlined />
                  <span style="margin-left: 8px">修改密码</span>
                </a-menu-item>
                <a-menu-item @click="handleLogout">
                  <LogoutOutlined />
                  <span style="margin-left: 8px">退出登录</span>
                </a-menu-item>
              </a-menu>
            </template>
          </a-dropdown>
        </div>
      </a-layout-header>
      <a-layout-content class="content">
        <router-view />
      </a-layout-content>
    </a-layout>

    <!-- 修改密码弹窗 -->
    <a-modal
      v-model:open="pwdModalVisible"
      title="修改密码"
      @ok="handleUpdatePwd"
      @cancel="pwdModalVisible = false"
    >
      <a-form ref="pwdFormRef" :model="pwdForm" :rules="pwdRules" :label-col="{ span: 6 }" :wrapper-col="{ span: 16 }">
        <a-form-item label="旧密码" name="oldPassword">
          <a-input-password v-model:value="pwdForm.oldPassword" placeholder="请输入旧密码" />
        </a-form-item>
        <a-form-item label="新密码" name="newPassword">
          <a-input-password v-model:value="pwdForm.newPassword" placeholder="请输入新密码" />
        </a-form-item>
        <a-form-item label="确认密码" name="confirmPassword">
          <a-input-password v-model:value="pwdForm.confirmPassword" placeholder="请再次输入新密码" />
        </a-form-item>
      </a-form>
    </a-modal>
  </a-layout>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { message } from 'ant-design-vue'
import {
  DashboardOutlined,
  StockOutlined,
  BarChartOutlined,
  SettingOutlined,
  ShopOutlined,
  DesktopOutlined,
  LineChartOutlined,
  HistoryOutlined,
  ThunderboltOutlined,
  HeatMapOutlined,
  EnvironmentOutlined,
  MenuFoldOutlined,
  MenuUnfoldOutlined,
  LogoutOutlined,
  UserOutlined,
  TeamOutlined,
  MenuOutlined,
  KeyOutlined,
  FundOutlined,
  LoginOutlined
} from '@ant-design/icons-vue'
import { post } from '@/utils/request'
import { userApi } from '@/api/system'

const router = useRouter()
const route = useRoute()

const collapsed = ref(false)
const selectedKeys = ref<string[]>(['dashboard'])
const openKeys = ref<string[]>([])
const menuList = ref<any[]>([])
const pwdModalVisible = ref(false)
const pwdFormRef = ref()

const pwdForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const validateConfirmPwd = async (_rule: any, value: string) => {
  if (value !== pwdForm.newPassword) {
    return Promise.reject('两次输入的密码不一致')
  }
  return Promise.resolve()
}

const pwdRules = {
  oldPassword: [{ required: true, message: '请输入旧密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    { validator: validateConfirmPwd, trigger: 'blur' }
  ]
}

const userInfo = computed(() => {
  const info = localStorage.getItem('userInfo')
  return info ? JSON.parse(info) : null
})

// 图标映射
const iconMap: Record<string, any> = {
  DashboardOutlined,
  StockOutlined,
  BarChartOutlined,
  SettingOutlined,
  ShopOutlined,
  DesktopOutlined,
  LineChartOutlined,
  HistoryOutlined,
  ThunderboltOutlined,
  HeatMapOutlined,
  EnvironmentOutlined,
  UserOutlined,
  TeamOutlined,
  MenuOutlined,
  FundOutlined,
  LoginOutlined
}

// 获取图标组件
const getIcon = (iconName: string) => {
  if (!iconName) return null
  return iconMap[iconName] || null
}

// 获取菜单列表
const fetchMenuList = () => {
  const menus = localStorage.getItem('menus')
  if (menus) {
    menuList.value = JSON.parse(menus)
  }
}

// 菜单点击事件
const handleMenuClick = (menu: any) => {
  if (menu.path) {
    router.push(menu.path)
  }
}

// 根据路由设置选中的菜单
const setSelectedKeys = () => {
  const path = route.path
  // 从菜单列表中找到匹配的菜单项
  const findMenuId = (menus: any[], targetPath: string): string | null => {
    for (const menu of menus) {
      if (menu.path === targetPath) {
        return String(menu.id)
      }
      if (menu.children) {
        const childId = findMenuId(menu.children, targetPath)
        if (childId) {
          openKeys.value = [String(menu.id)]
          return childId
        }
      }
    }
    return null
  }

  const menuId = findMenuId(menuList.value, path)
  if (menuId) {
    selectedKeys.value = [menuId]
  }
}

onMounted(() => {
  fetchMenuList()
  setSelectedKeys()
})

// 监听路由变化
router.afterEach(() => {
  setSelectedKeys()
})

const handleLogout = async () => {
  try {
    await post('/auth/logout')
  } catch (error) {
    console.error('Logout error:', error)
  } finally {
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
    localStorage.removeItem('menus')
    message.success('退出成功')
    router.push('/login')
  }
}

const showPwdModal = () => {
  pwdForm.oldPassword = ''
  pwdForm.newPassword = ''
  pwdForm.confirmPassword = ''
  pwdModalVisible.value = true
}

const handleUpdatePwd = async () => {
  try {
    await pwdFormRef.value.validate()
    await userApi.updatePwd({
      oldPassword: pwdForm.oldPassword,
      newPassword: pwdForm.newPassword
    })
    message.success('密码修改成功，请重新登录')
    pwdModalVisible.value = false
    // 清除登录信息并跳转到登录页
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
    localStorage.removeItem('menus')
    router.push('/login')
  } catch (error) {
    console.error(error)
  }
}
</script>

<style scoped lang="less">
.layout {
  min-height: 100vh;
}

.logo {
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(255, 255, 255, 0.1);
  margin: 16px;

  img {
    width: 32px;
    height: 32px;
  }

  span {
    color: #fff;
    font-size: 18px;
    font-weight: bold;
    margin-left: 10px;
  }
}

.header {
  background: #fff;
  padding: 0 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);

  .header-left {
    .trigger {
      font-size: 18px;
      cursor: pointer;
      transition: color 0.3s;

      &:hover {
        color: #1890ff;
      }
    }
  }

  .header-right {
    .user-info {
      display: flex;
      align-items: center;
      cursor: pointer;

      .username {
        margin-left: 10px;
        color: #333;
      }
    }
  }
}

.content {
  margin: 0;
  padding: 0;
  background: transparent;
  min-height: calc(100vh - 64px);
  overflow: auto;
}
</style>
