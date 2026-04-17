<template>
  <div class="page-container">
    <div class="page-header">
      <h2>登录日志</h2>
    </div>

    <!-- 统计卡片 -->
    <a-row :gutter="16" style="margin-bottom: 16px">
      <a-col :span="4">
        <a-card>
          <a-statistic title="今日登录" :value="stats.todayCount" suffix="次">
            <template #prefix>
              <LoginOutlined />
            </template>
          </a-statistic>
        </a-card>
      </a-col>
      <a-col :span="4">
        <a-card>
          <a-statistic title="本周登录" :value="stats.weekCount" suffix="次" />
        </a-card>
      </a-col>
      <a-col :span="4">
        <a-card>
          <a-statistic title="本月登录" :value="stats.monthCount" suffix="次" />
        </a-card>
      </a-col>
      <a-col :span="4">
        <a-card>
          <a-statistic title="今日失败" :value="stats.todayFailCount" suffix="次">
            <template #prefix>
              <WarningOutlined style="color: #ff4d4f" />
            </template>
          </a-statistic>
        </a-card>
      </a-col>
      <a-col :span="4">
        <a-card>
          <a-statistic title="活跃用户" :value="stats.activeUsers" suffix="人">
            <template #prefix>
              <TeamOutlined />
            </template>
          </a-statistic>
        </a-card>
      </a-col>
      <a-col :span="4">
        <a-card>
          <a-statistic title="最近登录" :value="recentUsers.length" suffix="人">
            <template #prefix>
              <UserOutlined />
            </template>
          </a-statistic>
        </a-card>
      </a-col>
    </a-row>

    <!-- 搜索栏 -->
    <div class="search-bar">
      <a-form layout="inline">
        <a-form-item label="用户名">
          <a-input v-model:value="searchForm.username" placeholder="请输入用户名" allow-clear />
        </a-form-item>
        <a-form-item label="登录状态">
          <a-select v-model:value="searchForm.loginStatus" placeholder="请选择" allow-clear style="width: 120px">
            <a-select-option :value="1">成功</a-select-option>
            <a-select-option :value="0">失败</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="登录时间">
          <a-range-picker
            v-model:value="searchForm.timeRange"
            :show-time="{ format: 'HH:mm:ss' }"
            format="YYYY-MM-DD HH:mm:ss"
            style="width: 380px"
          />
        </a-form-item>
        <a-form-item>
          <a-space>
            <a-button type="primary" @click="handleSearch">查询</a-button>
            <a-button @click="handleReset">重置</a-button>
          </a-space>
        </a-form-item>
      </a-form>
    </div>

    <!-- 操作按钮 -->
    <div class="action-bar">
      <a-popconfirm
        title="确定要清空所有登录日志吗？此操作不可恢复！"
        @confirm="handleClear"
      >
        <a-button danger>
          <template #icon><DeleteOutlined /></template>
          清空日志
        </a-button>
      </a-popconfirm>
    </div>

    <!-- 表格 -->
    <a-table
      :columns="columns"
      :data-source="tableData"
      :loading="loading"
      :pagination="pagination"
      row-key="id"
      @change="handleTableChange"
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'loginStatus'">
          <a-tag :color="record.loginStatus === 1 ? 'green' : 'red'">
            {{ record.loginStatus === 1 ? '成功' : '失败' }}
          </a-tag>
        </template>
        <template v-if="column.key === 'loginMsg'">
          <a-tooltip :title="record.loginMsg">
            <span style="color: record.loginStatus === 1 ? '#52c41a' : '#ff4d4f'">
              {{ record.loginMsg?.substring(0, 10) }}{{ record.loginMsg?.length > 10 ? '...' : '' }}
            </span>
          </a-tooltip>
        </template>
        <template v-if="column.key === 'action'">
          <a-popconfirm
            title="确定要删除此日志吗?"
            @confirm="handleDelete(record)"
          >
            <a-button type="link" size="small" danger>删除</a-button>
          </a-popconfirm>
        </template>
      </template>
    </a-table>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import { LoginOutlined, WarningOutlined, TeamOutlined, UserOutlined, DeleteOutlined } from '@ant-design/icons-vue'
import { loginLogApi } from '@/api/system'
import type { TablePaginationConfig } from 'ant-design-vue'
import type { Dayjs } from 'dayjs'

interface SearchForm {
  username: string
  loginStatus: number | undefined
  timeRange: [Dayjs, Dayjs] | null
}

interface LoginLog {
  id: number
  userId: number
  username: string
  realName: string
  loginTime: string
  loginIp: string
  loginLocation: string
  browser: string
  os: string
  loginStatus: number
  loginMsg: string
}

const loading = ref(false)
const tableData = ref<LoginLog[]>([])

const searchForm = reactive<SearchForm>({
  username: '',
  loginStatus: undefined,
  timeRange: null
})

const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
  showSizeChanger: true,
  showQuickJumper: true,
  showTotal: (total: number) => `共 ${total} 条`
})

const stats = ref({
  todayCount: 0,
  weekCount: 0,
  monthCount: 0,
  todayFailCount: 0,
  activeUsers: 0
})

const recentUsers = ref<any[]>([])

const columns = [
  { title: '用户名', dataIndex: 'username', key: 'username', width: 120 },
  { title: '真实姓名', dataIndex: 'realName', key: 'realName', width: 100 },
  { title: '登录时间', dataIndex: 'loginTime', key: 'loginTime', width: 180 },
  { title: '登录IP', dataIndex: 'loginIp', key: 'loginIp', width: 140 },
  { title: '登录地点', dataIndex: 'loginLocation', key: 'loginLocation', width: 100 },
  { title: '浏览器', dataIndex: 'browser', key: 'browser', width: 100 },
  { title: '操作系统', dataIndex: 'os', key: 'os', width: 100 },
  { title: '登录状态', dataIndex: 'loginStatus', key: 'loginStatus', width: 80 },
  { title: '登录消息', dataIndex: 'loginMsg', key: 'loginMsg', width: 120 },
  { title: '操作', key: 'action', width: 80, fixed: 'right' }
]

const fetchData = async () => {
  loading.value = true
  try {
    const params: any = {
      pageNum: pagination.current,
      pageSize: pagination.pageSize,
      username: searchForm.username || undefined,
      loginStatus: searchForm.loginStatus
    }
    
    if (searchForm.timeRange && searchForm.timeRange.length === 2) {
      params.startTime = searchForm.timeRange[0].format('YYYY-MM-DD HH:mm:ss')
      params.endTime = searchForm.timeRange[1].format('YYYY-MM-DD HH:mm:ss')
    }
    
    const res = await loginLogApi.page(params)
    tableData.value = res.data?.records || []
    pagination.total = res.data?.total || 0
  } catch (error: any) {
    message.error(error.message || '查询失败')
  } finally {
    loading.value = false
  }
}

const fetchStats = async () => {
  try {
    const res = await loginLogApi.stats()
    stats.value = res.data || stats.value
  } catch (error) {
    console.error('获取统计失败', error)
  }
}

const fetchRecentUsers = async () => {
  try {
    const res = await loginLogApi.recent(10)
    recentUsers.value = res.data || []
  } catch (error) {
    console.error('获取最近登录用户失败', error)
  }
}

const handleSearch = () => {
  pagination.current = 1
  fetchData()
}

const handleReset = () => {
  searchForm.username = ''
  searchForm.loginStatus = undefined
  searchForm.timeRange = null
  pagination.current = 1
  fetchData()
}

const handleTableChange = (pag: TablePaginationConfig) => {
  pagination.current = pag.current || 1
  pagination.pageSize = pag.pageSize || 10
  fetchData()
}

const handleDelete = async (record: LoginLog) => {
  try {
    await loginLogApi.delete([record.id])
    message.success('删除成功')
    fetchData()
    fetchStats()
  } catch (error: any) {
    message.error(error.message || '删除失败')
  }
}

const handleClear = async () => {
  try {
    await loginLogApi.clear()
    message.success('清空成功')
    fetchData()
    fetchStats()
    fetchRecentUsers()
  } catch (error: any) {
    message.error(error.message || '清空失败')
  }
}

onMounted(() => {
  fetchData()
  fetchStats()
  fetchRecentUsers()
})
</script>

<style scoped>
.page-container {
  padding: 24px;
  background: #fff;
  min-height: calc(100vh - 64px);
}

.page-header {
  margin-bottom: 24px;
}

.page-header h2 {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
}

.search-bar {
  margin-bottom: 16px;
  padding: 16px;
  background: #fafafa;
  border-radius: 4px;
}

.action-bar {
  margin-bottom: 16px;
}
</style>
