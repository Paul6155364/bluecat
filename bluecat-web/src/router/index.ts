import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'

const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/index.vue'),
    meta: { title: '登录', requiresAuth: false }
  },
  {
    path: '/',
    name: 'Layout',
    component: () => import('@/layouts/BasicLayout.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/index.vue'),
        meta: { title: '数据大屏', icon: 'DashboardOutlined' }
      },
      {
        path: 'competitor',
        name: 'Competitor',
        component: () => import('@/views/competitor/index.vue'),
        meta: { title: '竞品大屏', icon: 'StockOutlined' }
      },
      {
        path: 'analysis',
        name: 'Analysis',
        component: () => import('@/views/analysis/index.vue'),
        meta: { title: '经营分析', icon: 'BarChartOutlined' }
      },
      {
        path: 'report',
        name: 'BusinessReport',
        component: () => import('@/views/report/index.vue'),
        meta: { title: '经营数据分析', icon: 'FundOutlined' }
      },
      {
        path: 'config',
        name: 'Config',
        component: () => import('@/views/config/index.vue'),
        meta: { title: '网吧配置', icon: 'SettingOutlined' }
      },
      {
        path: 'shop',
        name: 'Shop',
        component: () => import('@/views/shop/index.vue'),
        meta: { title: '门店管理', icon: 'ShopOutlined' }
      },
      {
        path: 'machine',
        name: 'Machine',
        component: () => import('@/views/machine/index.vue'),
        meta: { title: '机器监控', icon: 'DesktopOutlined' }
      },
      {
        path: 'status',
        name: 'Status',
        component: () => import('@/views/status/index.vue'),
        meta: { title: '状态快照', icon: 'LineChartOutlined' }
      },
      {
        path: 'history',
        name: 'History',
        component: () => import('@/views/history/index.vue'),
        meta: { title: '历史数据', icon: 'HistoryOutlined' }
      },
      {
        path: 'pk-relation',
        name: 'PkRelation',
        component: () => import('@/views/system/pk/index.vue'),
        meta: { title: 'PK关系管理', icon: 'ThunderboltOutlined' }
      },
      {
        path: 'pk',
        name: 'PkArena',
        component: () => import('@/views/pk/index.vue'),
        meta: { title: '门店PK', icon: 'ThunderboltOutlined' }
      },
      {
        path: 'heatmap',
        name: 'Heatmap',
        component: () => import('@/views/heatmap/index.vue'),
        meta: { title: '时段热力图', icon: 'HeatMapOutlined' }
      },
      {
        path: 'site-selection',
        name: 'SiteSelection',
        component: () => import('@/views/site-selection/index.vue'),
        meta: { title: '新店选址', icon: 'EnvironmentOutlined' }
      },
      // 系统管理
      {
        path: 'system/user',
        name: 'SystemUser',
        component: () => import('@/views/system/user/index.vue'),
        meta: { title: '用户管理', icon: 'UserOutlined' }
      },
      {
        path: 'system/role',
        name: 'SystemRole',
        component: () => import('@/views/system/role/index.vue'),
        meta: { title: '角色管理', icon: 'TeamOutlined' }
      },
      {
        path: 'system/menu',
        name: 'SystemMenu',
        component: () => import('@/views/system/menu/index.vue'),
        meta: { title: '菜单管理', icon: 'MenuOutlined' }
      },
      {
        path: 'system/login-log',
        name: 'SystemLoginLog',
        component: () => import('@/views/system/login-log/index.vue'),
        meta: { title: '登录日志', icon: 'LoginOutlined' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, _from, next) => {
  // 设置页面标题
  document.title = `${to.meta.title || 'CyberPulse'} - CyberPulse 网咖脉搏`

  // 登录校验
  const token = localStorage.getItem('token')
  if (to.meta.requiresAuth !== false && !token) {
    next({ name: 'Login', query: { redirect: to.fullPath } })
  } else {
    next()
  }
})

export default router
