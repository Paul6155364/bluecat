<template>
  <div class="page-container">
    <div class="page-header">
      <h2>门店管理</h2>
    </div>

    <!-- 搜索栏 -->
    <div class="search-bar">
      <a-form layout="inline">
        <a-form-item label="网吧配置">
          <a-select
            v-model:value="searchForm.configId"
            placeholder="请选择"
            allow-clear
            style="width: 200px"
          >
            <a-select-option v-for="item in configList" :key="item.id" :value="item.id">
              {{ item.configName }}
            </a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="门店名称">
          <a-input v-model:value="searchForm.name" placeholder="请输入门店名称" allow-clear />
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
      @change="handleTableChange"
      row-key="id"
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'name'">
          <a-button type="link" @click="handleShowDetail(record)">{{ record.name }}</a-button>
        </template>
        <template v-if="column.key === 'status'">
          <a-tag :color="record.status === 2 ? 'green' : 'default'">
            {{ record.status === 2 ? '营业中' : '其他' }}
          </a-tag>
        </template>
        <template v-if="column.key === 'haveRoom'">
          <a-tag :color="record.haveRoom === 1 ? 'blue' : 'default'">
            {{ record.haveRoom === 1 ? '有' : '无' }}
          </a-tag>
        </template>
        <template v-if="column.key === 'action'">
          <a-space>
            <a-button type="link" size="small" @click="handleShowAreas(record)">区域</a-button>
            <a-button type="link" size="small" @click="handleShowMachines(record)">机器</a-button>
          </a-space>
        </template>
      </template>
    </a-table>

    <!-- 门店详情弹窗 -->
    <a-modal
      v-model:open="detailVisible"
      title="门店详情"
      :footer="null"
      width="800px"
    >
      <a-descriptions :column="2" bordered>
        <a-descriptions-item label="门店名称">{{ currentShop?.name }}</a-descriptions-item>
        <a-descriptions-item label="门店编号">{{ currentShop?.snbid }}</a-descriptions-item>
        <a-descriptions-item label="省份">{{ currentShop?.provinceName }}</a-descriptions-item>
        <a-descriptions-item label="城市">{{ currentShop?.cityName }}</a-descriptions-item>
        <a-descriptions-item label="区县">{{ currentShop?.zoneName }}</a-descriptions-item>
        <a-descriptions-item label="地址">{{ currentShop?.address }}</a-descriptions-item>
        <a-descriptions-item label="前台电话">{{ currentShop?.stel }}</a-descriptions-item>
        <a-descriptions-item label="老板电话">{{ currentShop?.sbossTel }}</a-descriptions-item>
        <a-descriptions-item label="WiFi名称">{{ currentShop?.wifiName }}</a-descriptions-item>
        <a-descriptions-item label="WiFi密码">{{ currentShop?.wifiPwd }}</a-descriptions-item>
        <a-descriptions-item label="QQ">{{ currentShop?.qq }}</a-descriptions-item>
        <a-descriptions-item label="营业状态">
          <a-tag :color="currentShop?.status === 2 ? 'green' : 'default'">
            {{ currentShop?.status === 2 ? '营业中' : '其他' }}
          </a-tag>
        </a-descriptions-item>
      </a-descriptions>
    </a-modal>

    <!-- 区域列表弹窗 -->
    <a-modal
      v-model:open="areaVisible"
      title="门店区域"
      :footer="null"
      width="600px"
    >
      <a-table
        :columns="areaColumns"
        :data-source="areaList"
        :loading="areaLoading"
        row-key="id"
        size="small"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'allow'">
            <a-tag :color="record.allow === 1 ? 'green' : 'red'">
              {{ record.allow === 1 ? '允许' : '禁止' }}
            </a-tag>
          </template>
        </template>
      </a-table>
    </a-modal>

    <!-- 机器列表弹窗 -->
    <a-modal
      v-model:open="machineVisible"
      title="门店机器"
      :footer="null"
      width="800px"
    >
      <a-table
        :columns="machineColumns"
        :data-source="machineList"
        :loading="machineLoading"
        row-key="id"
        size="small"
      />
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import type { TableProps } from 'ant-design-vue'
import { shopApi } from '@/api/shop'
import { configApi } from '@/api/config'
import { machineApi } from '@/api/machine'

const loading = ref(false)
const areaLoading = ref(false)
const machineLoading = ref(false)
const detailVisible = ref(false)
const areaVisible = ref(false)
const machineVisible = ref(false)

const configList = ref<any[]>([])
const currentShop = ref<any>(null)
const areaList = ref<any[]>([])
const machineList = ref<any[]>([])

const searchForm = reactive({
  configId: undefined as number | undefined,
  name: ''
})

const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
  showSizeChanger: true,
  showTotal: (total: number) => `共 ${total} 条`
})

const columns = [
  { title: 'ID', dataIndex: 'id', key: 'id', width: 80 },
  { title: '门店名称', dataIndex: 'name', key: 'name', width: 200, ellipsis: true },
  { title: '门店编号', dataIndex: 'snbid', key: 'snbid', width: 120 },
  { title: '省份', dataIndex: 'provinceName', key: 'provinceName', width: 80 },
  { title: '城市', dataIndex: 'cityName', key: 'cityName', width: 80 },
  { title: '地址', dataIndex: 'address', key: 'address', ellipsis: true },
  { title: '前台电话', dataIndex: 'stel', key: 'stel', width: 120 },
  { title: '营业状态', dataIndex: 'status', key: 'status', width: 100 },
  { title: '操作', key: 'action', width: 150 }
]

const areaColumns = [
  { title: 'ID', dataIndex: 'id', key: 'id', width: 80 },
  { title: '区域名称', dataIndex: 'areaName', key: 'areaName' },
  { title: '允许预订', dataIndex: 'allow', key: 'allow' },
  { title: '最小预订数', dataIndex: 'minNum', key: 'minNum' },
  { title: '排序', dataIndex: 'sortOrder', key: 'sortOrder' }
]

const machineColumns = [
  { title: 'ID', dataIndex: 'id', key: 'id', width: 80 },
  { title: '机器名称', dataIndex: 'comName', key: 'comName' },
  { title: '所属区域', dataIndex: 'areaName', key: 'areaName' },
  { title: '更新时间', dataIndex: 'updateTime', key: 'updateTime' }
]

const tableData = ref<any[]>([])

const fetchConfigList = async () => {
  try {
    const res = await configApi.listEnabled()
    configList.value = res.data
  } catch (error) {
    console.error(error)
  }
}

const fetchData = async () => {
  loading.value = true
  try {
    const res = await shopApi.page({
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
  searchForm.name = ''
  pagination.current = 1
  fetchData()
}

const handleTableChange: TableProps['onChange'] = (pag) => {
  pagination.current = pag.current || 1
  pagination.pageSize = pag.pageSize || 10
  fetchData()
}

const handleShowDetail = (record: any) => {
  currentShop.value = record
  detailVisible.value = true
}

const handleShowAreas = async (record: any) => {
  areaVisible.value = true
  areaLoading.value = true
  try {
    const res = await shopApi.listAreas(record.id)
    areaList.value = res.data
  } catch (error) {
    console.error(error)
  } finally {
    areaLoading.value = false
  }
}

const handleShowMachines = async (record: any) => {
  machineVisible.value = true
  machineLoading.value = true
  try {
    const res = await machineApi.listByShop(record.id)
    machineList.value = res.data
  } catch (error) {
    console.error(error)
  } finally {
    machineLoading.value = false
  }
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
</style>
