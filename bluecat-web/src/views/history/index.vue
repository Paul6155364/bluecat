<template>
  <div class="page-container">
    <div class="page-header">
      <h2>历史数据</h2>
    </div>

    <a-tabs v-model:activeKey="activeTab">
      <!-- 快照历史 -->
      <a-tab-pane key="snapshot" tab="快照历史">
        <div class="search-bar">
          <a-form layout="inline">
            <a-form-item label="网咖名称">
              <a-select
                v-model:value="snapshotSearch.configId"
                placeholder="请选择"
                allow-clear
                show-search
                :filter-option="filterOption"
                style="width: 180px"
                @change="handleConfigChange"
              >
                <a-select-option v-for="item in configList" :key="item.id" :value="item.id">
                  {{ item.configName }}
                </a-select-option>
              </a-select>
            </a-form-item>
            <a-form-item label="门店">
              <a-select
                v-model:value="snapshotSearch.shopId"
                placeholder="请选择"
                allow-clear
                show-search
                :filter-option="filterOption"
                style="width: 200px"
                :disabled="!snapshotSearch.configId"
              >
                <a-select-option v-for="item in shopList" :key="item.id" :value="item.id">
                  {{ item.name }}
                </a-select-option>
              </a-select>
            </a-form-item>
            <a-form-item label="开始时间">
              <a-date-picker
                v-model:value="snapshotSearch.startTime"
                show-time
                format="YYYY-MM-DD HH:mm:ss"
                placeholder="请选择"
              />
            </a-form-item>
            <a-form-item label="结束时间">
              <a-date-picker
                v-model:value="snapshotSearch.endTime"
                show-time
                format="YYYY-MM-DD HH:mm:ss"
                placeholder="请选择"
              />
            </a-form-item>
            <a-form-item>
              <a-space>
                <a-button type="primary" @click="handleSnapshotSearch">查询</a-button>
                <a-button @click="handleSnapshotReset">重置</a-button>
              </a-space>
            </a-form-item>
          </a-form>
        </div>

        <a-table
          :columns="snapshotColumns"
          :data-source="snapshotData"
          :loading="snapshotLoading"
          :pagination="snapshotPagination"
          @change="handleSnapshotTableChange"
          row-key="id"
        >
          <template #bodyCell="{ column, record }">
            <template v-if="column.key === 'occupancyRate'">
              <a-progress
                :percent="record.occupancyRate || 0"
                :stroke-color="getProgressColor(record.occupancyRate)"
                size="small"
              />
            </template>
            <template v-if="column.key === 'action'">
              <a-button type="link" size="small" @click="handleShowSnapshotDetail(record)">详情</a-button>
            </template>
          </template>
        </a-table>
      </a-tab-pane>

      <!-- 采集任务 -->
      <a-tab-pane key="task" tab="采集任务">
        <div class="search-bar">
          <a-form layout="inline">
            <a-form-item label="任务类型">
              <a-select v-model:value="taskSearch.taskType" placeholder="请选择" allow-clear style="width: 150px">
                <a-select-option value="SHOP_LIST">门店列表</a-select-option>
                <a-select-option value="MACHINE_STATUS">机器状态</a-select-option>
              </a-select>
            </a-form-item>
            <a-form-item label="状态">
              <a-select v-model:value="taskSearch.status" placeholder="请选择" allow-clear style="width: 120px">
                <a-select-option :value="0">执行中</a-select-option>
                <a-select-option :value="1">成功</a-select-option>
                <a-select-option :value="2">失败</a-select-option>
              </a-select>
            </a-form-item>
            <a-form-item>
              <a-space>
                <a-button type="primary" @click="handleTaskSearch">查询</a-button>
                <a-button @click="handleTaskReset">重置</a-button>
              </a-space>
            </a-form-item>
          </a-form>
        </div>

        <a-table
          :columns="taskColumns"
          :data-source="taskData"
          :loading="taskLoading"
          :pagination="taskPagination"
          @change="handleTaskTableChange"
          row-key="id"
        >
          <template #bodyCell="{ column, record }">
            <template v-if="column.key === 'taskType'">
              <a-tag :color="record.taskType === 'SHOP_LIST' ? 'blue' : 'green'">
                {{ record.taskType === 'SHOP_LIST' ? '门店列表' : '机器状态' }}
              </a-tag>
            </template>
            <template v-if="column.key === 'status'">
              <a-tag :color="record.status === 1 ? 'green' : record.status === 2 ? 'red' : 'blue'">
                {{ record.status === 1 ? '成功' : record.status === 2 ? '失败' : '执行中' }}
              </a-tag>
            </template>
            <template v-if="column.key === 'durationMs'">
              {{ record.durationMs }}ms
            </template>
            <template v-if="column.key === 'action'">
              <a-button type="link" size="small" @click="handleShowTaskDetail(record)">详情</a-button>
            </template>
          </template>
        </a-table>
      </a-tab-pane>

      <!-- API调用日志 -->
      <a-tab-pane key="log" tab="API日志">
        <div class="search-bar">
          <a-form layout="inline">
            <a-form-item label="API名称">
              <a-input v-model:value="logSearch.apiName" placeholder="请输入" allow-clear />
            </a-form-item>
            <a-form-item>
              <a-space>
                <a-button type="primary" @click="handleLogSearch">查询</a-button>
                <a-button @click="handleLogReset">重置</a-button>
              </a-space>
            </a-form-item>
          </a-form>
        </div>

        <a-table
          :columns="logColumns"
          :data-source="logData"
          :loading="logLoading"
          :pagination="logPagination"
          @change="handleLogTableChange"
          row-key="id"
        >
          <template #bodyCell="{ column, record }">
            <template v-if="column.key === 'status'">
              <a-tag :color="record.status === 1 ? 'green' : 'red'">
                {{ record.status === 1 ? '成功' : '失败' }}
              </a-tag>
            </template>
            <template v-if="column.key === 'durationMs'">
              {{ record.durationMs }}ms
            </template>
            <template v-if="column.key === 'action'">
              <a-button type="link" size="small" @click="handleShowLogDetail(record)">详情</a-button>
            </template>
          </template>
        </a-table>
      </a-tab-pane>
    </a-tabs>

    <!-- 任务详情弹窗 -->
    <a-modal
      v-model:open="taskDetailVisible"
      title="任务详情"
      :footer="null"
      width="700px"
    >
      <a-descriptions :column="2" bordered>
        <a-descriptions-item label="任务ID">{{ currentTask?.id }}</a-descriptions-item>
        <a-descriptions-item label="任务类型">{{ currentTask?.taskType }}</a-descriptions-item>
        <a-descriptions-item label="状态">{{ currentTask?.status === 1 ? '成功' : currentTask?.status === 2 ? '失败' : '执行中' }}</a-descriptions-item>
        <a-descriptions-item label="耗时">{{ currentTask?.durationMs }}ms</a-descriptions-item>
        <a-descriptions-item label="开始时间">{{ currentTask?.startTime }}</a-descriptions-item>
        <a-descriptions-item label="结束时间">{{ currentTask?.endTime }}</a-descriptions-item>
        <a-descriptions-item label="错误信息" :span="2">
          <span v-if="currentTask?.errorMsg" style="color: #f5222d">{{ currentTask?.errorMsg }}</span>
          <span v-else>-</span>
        </a-descriptions-item>
      </a-descriptions>
    </a-modal>

    <!-- API日志详情弹窗 -->
    <a-modal
      v-model:open="logDetailVisible"
      title="API日志详情"
      :footer="null"
      width="800px"
    >
      <a-descriptions :column="2" bordered>
        <a-descriptions-item label="API名称">{{ currentLog?.apiName }}</a-descriptions-item>
        <a-descriptions-item label="请求方法">{{ currentLog?.requestMethod }}</a-descriptions-item>
        <a-descriptions-item label="响应码">{{ currentLog?.responseCode }}</a-descriptions-item>
        <a-descriptions-item label="耗时">{{ currentLog?.durationMs }}ms</a-descriptions-item>
        <a-descriptions-item label="API地址" :span="2">{{ currentLog?.apiUrl }}</a-descriptions-item>
        <a-descriptions-item label="请求体" :span="2">
          <pre style="margin: 0; max-height: 200px; overflow: auto">{{ currentLog?.requestBody }}</pre>
        </a-descriptions-item>
        <a-descriptions-item label="响应体" :span="2">
          <pre style="margin: 0; max-height: 300px; overflow: auto">{{ JSON.stringify(currentLog?.responseBody, null, 2) }}</pre>
        </a-descriptions-item>
        <a-descriptions-item label="错误信息" :span="2" v-if="currentLog?.errorMsg">
          <span style="color: #f5222d">{{ currentLog?.errorMsg }}</span>
        </a-descriptions-item>
      </a-descriptions>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import type { TableProps } from 'ant-design-vue'
import dayjs from 'dayjs'
import { statusApi } from '@/api/status'
import { taskApi } from '@/api/task'
import { shopApi } from '@/api/shop'
import { configApi } from '@/api/config'

const activeTab = ref('snapshot')
const configList = ref<any[]>([])
const shopList = ref<any[]>([])

// 快照搜索
const snapshotSearch = reactive({
  configId: undefined as number | undefined,
  shopId: undefined as number | undefined,
  startTime: null as any,
  endTime: null as any
})

const snapshotLoading = ref(false)
const snapshotData = ref<any[]>([])
const snapshotPagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
  showSizeChanger: true,
  showTotal: (total: number) => `共 ${total} 条`
})

// 任务搜索
const taskSearch = reactive({
  taskType: undefined as string | undefined,
  status: undefined as number | undefined
})

const taskLoading = ref(false)
const taskData = ref<any[]>([])
const taskPagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
  showSizeChanger: true,
  showTotal: (total: number) => `共 ${total} 条`
})

// 日志搜索
const logSearch = reactive({
  apiName: ''
})

const logLoading = ref(false)
const logData = ref<any[]>([])
const logPagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
  showSizeChanger: true,
  showTotal: (total: number) => `共 ${total} 条`
})

// 详情弹窗
const taskDetailVisible = ref(false)
const logDetailVisible = ref(false)
const currentTask = ref<any>(null)
const currentLog = ref<any>(null)

const snapshotColumns = [
  { title: 'ID', dataIndex: 'id', key: 'id', width: 80 },
  { title: '网咖名称', dataIndex: 'configName', key: 'configName', width: 150 },
  { title: '门店名称', dataIndex: 'shopName', key: 'shopName', width: 200 },
  { title: '机器总数', dataIndex: 'totalMachines', key: 'totalMachines', width: 100 },
  { title: '空闲', dataIndex: 'freeMachines', key: 'freeMachines', width: 80 },
  { title: '占用', dataIndex: 'busyMachines', key: 'busyMachines', width: 80 },
  { title: '上座率', dataIndex: 'occupancyRate', key: 'occupancyRate', width: 150 },
  { title: '快照时间', dataIndex: 'snapshotTime', key: 'snapshotTime', width: 180 },
  { title: '操作', key: 'action', width: 80 }
]

const taskColumns = [
  { title: 'ID', dataIndex: 'id', key: 'id', width: 80 },
  { title: '配置ID', dataIndex: 'configId', key: 'configId' },
  { title: '门店ID', dataIndex: 'shopId', key: 'shopId' },
  { title: '任务类型', dataIndex: 'taskType', key: 'taskType' },
  { title: '状态', dataIndex: 'status', key: 'status' },
  { title: '耗时', dataIndex: 'durationMs', key: 'durationMs' },
  { title: '开始时间', dataIndex: 'startTime', key: 'startTime' },
  { title: '操作', key: 'action', width: 80 }
]

const logColumns = [
  { title: 'ID', dataIndex: 'id', key: 'id', width: 80 },
  { title: 'API名称', dataIndex: 'apiName', key: 'apiName' },
  { title: '请求方法', dataIndex: 'requestMethod', key: 'requestMethod', width: 80 },
  { title: '响应码', dataIndex: 'responseCode', key: 'responseCode', width: 80 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 80 },
  { title: '耗时', dataIndex: 'durationMs', key: 'durationMs', width: 100 },
  { title: '调用时间', dataIndex: 'callTime', key: 'callTime' },
  { title: '操作', key: 'action', width: 80 }
]

const filterOption = (input: string, option: any) => {
  return option.children[0].children.toLowerCase().indexOf(input.toLowerCase()) >= 0
}

const getProgressColor = (rate: number) => {
  if (rate >= 80) return '#f5222d'
  if (rate >= 60) return '#fa8c16'
  if (rate >= 40) return '#fadb14'
  return '#52c41a'
}

const fetchConfigList = async () => {
  try {
    const res = await configApi.listEnabled()
    configList.value = res.data
  } catch (error) {
    console.error(error)
  }
}

const fetchShopList = async (configId: number) => {
  try {
    const res = await shopApi.list(configId)
    shopList.value = res.data
  } catch (error) {
    console.error(error)
  }
}

const handleConfigChange = async (configId: number | undefined) => {
  snapshotSearch.shopId = undefined
  shopList.value = []
  if (configId) {
    await fetchShopList(configId)
  }
}

const fetchSnapshotData = async () => {
  snapshotLoading.value = true
  try {
    const params: any = {
      pageNum: snapshotPagination.current,
      pageSize: snapshotPagination.pageSize
    }
    if (snapshotSearch.configId) params.configId = snapshotSearch.configId
    if (snapshotSearch.shopId) params.shopId = snapshotSearch.shopId
    if (snapshotSearch.startTime) params.startTime = dayjs(snapshotSearch.startTime).format('YYYY-MM-DD HH:mm:ss')
    if (snapshotSearch.endTime) params.endTime = dayjs(snapshotSearch.endTime).format('YYYY-MM-DD HH:mm:ss')

    const res = await statusApi.pageSnapshot(params)
    snapshotData.value = res.data?.records || []
    snapshotPagination.total = res.data?.total || 0
  } catch (error) {
    console.error(error)
  } finally {
    snapshotLoading.value = false
  }
}

const fetchTaskData = async () => {
  taskLoading.value = true
  try {
    const res = await taskApi.page({
      pageNum: taskPagination.current,
      pageSize: taskPagination.pageSize,
      ...taskSearch
    })
    taskData.value = res.data.records
    taskPagination.total = res.data.total
  } catch (error) {
    console.error(error)
  } finally {
    taskLoading.value = false
  }
}

const fetchLogData = async () => {
  logLoading.value = true
  try {
    const res = await taskApi.logPage({
      pageNum: logPagination.current,
      pageSize: logPagination.pageSize,
      apiName: logSearch.apiName || undefined
    })
    logData.value = res.data.records
    logPagination.total = res.data.total
  } catch (error) {
    console.error(error)
  } finally {
    logLoading.value = false
  }
}

const handleSnapshotSearch = () => {
  snapshotPagination.current = 1
  fetchSnapshotData()
}

const handleSnapshotReset = () => {
  snapshotSearch.configId = undefined
  snapshotSearch.shopId = undefined
  snapshotSearch.startTime = null
  snapshotSearch.endTime = null
  shopList.value = []
  snapshotPagination.current = 1
  fetchSnapshotData()
}

const handleSnapshotTableChange: TableProps['onChange'] = (pag) => {
  snapshotPagination.current = pag.current || 1
  snapshotPagination.pageSize = pag.pageSize || 10
  fetchSnapshotData()
}

const handleTaskSearch = () => {
  taskPagination.current = 1
  fetchTaskData()
}

const handleTaskReset = () => {
  taskSearch.taskType = undefined
  taskSearch.status = undefined
  taskPagination.current = 1
  fetchTaskData()
}

const handleTaskTableChange: TableProps['onChange'] = (pag) => {
  taskPagination.current = pag.current || 1
  taskPagination.pageSize = pag.pageSize || 10
  fetchTaskData()
}

const handleLogSearch = () => {
  logPagination.current = 1
  fetchLogData()
}

const handleLogReset = () => {
  logSearch.apiName = ''
  logPagination.current = 1
  fetchLogData()
}

const handleLogTableChange: TableProps['onChange'] = (pag) => {
  logPagination.current = pag.current || 1
  logPagination.pageSize = pag.pageSize || 10
  fetchLogData()
}

const handleShowSnapshotDetail = async (record: any) => {
  // 可以复用status页面的详情弹窗逻辑
  console.log('snapshot detail:', record)
}

const handleShowTaskDetail = (record: any) => {
  currentTask.value = record
  taskDetailVisible.value = true
}

const handleShowLogDetail = (record: any) => {
  currentLog.value = record
  logDetailVisible.value = true
}

onMounted(() => {
  fetchConfigList()
  fetchSnapshotData()
  fetchTaskData()
  fetchLogData()
})
</script>

<style scoped lang="less">
.page-container {
  padding: 24px;
  background: #fff;
  min-height: calc(100vh - 64px);
}

.page-header {
  margin-bottom: 16px;

  h2 {
    margin: 0;
    font-size: 20px;
    font-weight: 600;
  }
}

.search-bar {
  margin-bottom: 16px;
  padding: 16px;
  background: #fafafa;
  border-radius: 4px;
}

pre {
  background: #f5f5f5;
  padding: 8px;
  border-radius: 4px;
  font-size: 12px;
}
</style>
