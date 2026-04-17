<template>
  <div class="page-container">
    <div class="page-header">
      <h2>机器监控</h2>
    </div>

    <!-- 搜索栏 -->
    <div class="search-bar">
      <a-form layout="inline">
        <a-form-item label="网吧配置">
          <a-select
            v-model:value="searchForm.configId"
            placeholder="请选择网吧配置"
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
            v-model:value="searchForm.shopId"
            placeholder="请选择门店"
            allow-clear
            show-search
            :filter-option="filterOption"
            style="width: 200px"
            :disabled="!searchForm.configId"
            @change="handleShopChange"
          >
            <a-select-option v-for="item in shopList" :key="item.id" :value="item.id">
              {{ item.name }}
            </a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="区域">
          <a-select
            v-model:value="searchForm.areaName"
            placeholder="请选择区域"
            allow-clear
            style="width: 150px"
            :disabled="!searchForm.shopId"
          >
            <a-select-option v-for="item in areaList" :key="item.areaName" :value="item.areaName">
              {{ item.areaName }}
            </a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="机器名称">
          <a-input v-model:value="searchForm.comName" placeholder="请输入机器名称" allow-clear style="width: 140px" />
        </a-form-item>
        <a-form-item>
          <a-space>
            <a-button type="primary" @click="handleSearch">查询</a-button>
            <a-button @click="handleReset">重置</a-button>
          </a-space>
        </a-form-item>
      </a-form>
    </div>

    <!-- 数据表格 -->
    <a-table
      :columns="columns"
      :data-source="tableData"
      :loading="loading"
      :pagination="pagination"
      :scroll="{ x: 900 }"
      @change="handleTableChange"
      row-key="id"
      size="middle"
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'comName'">
          <span style="font-family: monospace; font-weight: 500">{{ record.comName }}</span>
        </template>
        <template v-if="column.key === 'action'">
          <a-button type="link" size="small" @click="handleShowHistory(record)">状态历史</a-button>
        </template>
      </template>
    </a-table>

    <!-- 状态历史弹窗 -->
    <a-modal
      v-model:open="historyVisible"
      title="机器状态历史"
      :footer="null"
      width="900px"
    >
      <div class="history-header">
        <span>机器: {{ currentMachine?.comName }}</span>
        <span style="margin-left: 20px">区域: {{ currentMachine?.areaName }}</span>
      </div>
      <a-table
        :columns="historyColumns"
        :data-source="historyList"
        :loading="historyLoading"
        row-key="id"
        size="small"
        :pagination="historyPagination"
        @change="handleHistoryTableChange"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'status'">
            <a-tag :color="record.status === 1 ? 'green' : 'red'">
              {{ record.status === 1 ? '空闲' : '占用' }}
            </a-tag>
          </template>
        </template>
      </a-table>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import type { TableProps } from 'ant-design-vue'
import { machineApi } from '@/api/machine'
import { shopApi } from '@/api/shop'
import { configApi } from '@/api/config'
import { statusApi } from '@/api/status'

const loading = ref(false)
const historyLoading = ref(false)
const historyVisible = ref(false)

const configList = ref<any[]>([])
const shopList = ref<any[]>([])
const areaList = ref<any[]>([])
const currentMachine = ref<any>(null)
const historyList = ref<any[]>([])

const searchForm = reactive({
  configId: undefined as number | undefined,
  shopId: undefined as number | undefined,
  areaName: undefined as string | undefined,
  comName: ''
})

const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
  showSizeChanger: true,
  showTotal: (total: number) => `共 ${total} 条`
})

const historyPagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
  showSizeChanger: true,
  showTotal: (total: number) => `共 ${total} 条`
})

const columns = [
  { title: 'ID', dataIndex: 'id', key: 'id', width: 80 },
  { title: '机器名称', dataIndex: 'comName', key: 'comName', width: 120 },
  { title: '所属区域', dataIndex: 'areaName', key: 'areaName', width: 150 },
  { title: '最后下线时间', dataIndex: 'lastOfflineTime', key: 'lastOfflineTime', width: 180 },
  { title: '更新时间', dataIndex: 'updateTime', key: 'updateTime' },
  { title: '操作', key: 'action', width: 100, fixed: 'right' }
]

const historyColumns = [
  { title: 'ID', dataIndex: 'id', key: 'id', width: 80 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 80 },
  { title: '快照时间', dataIndex: 'snapshotTime', key: 'snapshotTime' },
  { title: '创建时间', dataIndex: 'createTime', key: 'createTime' }
]

const tableData = ref<any[]>([])

const filterOption = (input: string, option: any) => {
  return option.children[0].children.toLowerCase().indexOf(input.toLowerCase()) >= 0
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

const fetchData = async () => {
  loading.value = true
  try {
    const res = await machineApi.page({
      pageNum: pagination.current,
      pageSize: pagination.pageSize,
      ...searchForm
    })
    tableData.value = res.data.records
    pagination.total = res.data.total
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.current = 1
  fetchData()
}

const handleReset = () => {
  searchForm.configId = undefined
  searchForm.shopId = undefined
  searchForm.areaName = undefined
  searchForm.comName = ''
  shopList.value = []
  areaList.value = []
  pagination.current = 1
  fetchData()
}

const handleConfigChange = async (configId: number | undefined) => {
  searchForm.shopId = undefined
  searchForm.areaName = undefined
  shopList.value = []
  areaList.value = []
  if (configId) {
    await fetchShopList(configId)
  }
}

const handleShopChange = async (shopId: number | undefined) => {
  searchForm.areaName = undefined
  areaList.value = []
  if (shopId) {
    try {
      const res = await shopApi.listAreas(shopId)
      areaList.value = res.data
    } catch (error) {
      console.error(error)
    }
  }
}

const handleTableChange: TableProps['onChange'] = (pag) => {
  pagination.current = pag.current || 1
  pagination.pageSize = pag.pageSize || 10
  fetchData()
}

const handleShowHistory = async (record: any) => {
  currentMachine.value = record
  historyVisible.value = true
  historyPagination.current = 1
  fetchHistoryData()
}

const fetchHistoryData = async () => {
  if (!currentMachine.value) return
  
  historyLoading.value = true
  try {
    const res = await statusApi.pageMachineHistory({
      pageNum: historyPagination.current,
      pageSize: historyPagination.pageSize,
      shopId: currentMachine.value.shopId,
      machineId: currentMachine.value.id
    })
    historyList.value = res.data.records
    historyPagination.total = res.data.total
  } catch (error) {
    console.error(error)
  } finally {
    historyLoading.value = false
  }
}

const handleHistoryTableChange: TableProps['onChange'] = (pag) => {
  historyPagination.current = pag.current || 1
  historyPagination.pageSize = pag.pageSize || 10
  fetchHistoryData()
}

onMounted(() => {
  fetchConfigList()
  fetchData()
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

.history-header {
  margin-bottom: 16px;
  font-weight: 500;
}

:deep(.ant-table) {
  .ant-table-thead > tr > th {
    background: #f5f7fa;
    font-weight: 600;
    color: #333;
  }

  .ant-table-tbody > tr > td {
    padding: 12px 16px;
  }

  .ant-table-tbody > tr:hover > td {
    background: #f0f7ff;
  }
}
</style>
