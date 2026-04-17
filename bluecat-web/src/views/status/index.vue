<template>
  <div class="page-container">
    <div class="page-header">
      <h2>状态快照</h2>
    </div>

    <!-- 搜索栏 -->
    <div class="search-bar">
      <a-form layout="inline">
        <a-form-item label="网咖名称">
          <a-select
            v-model:value="searchForm.configId"
            placeholder="请选择网咖名称"
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
          >
            <a-select-option v-for="item in shopList" :key="item.id" :value="item.id">
              {{ item.name }}
            </a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item>
          <a-space>
            <a-button type="primary" @click="handleSearch">查询</a-button>
            <a-button @click="handleReset">重置</a-button>
          </a-space>
        </a-form-item>
      </a-form>
    </div>

    <!-- 统计卡片 -->
    <a-row :gutter="16" class="stat-cards">
      <a-col :span="6">
        <a-card>
          <a-statistic title="门店总数" :value="statData.totalShops" />
        </a-card>
      </a-col>
      <a-col :span="6">
        <a-card>
          <a-statistic title="机器总数" :value="statData.totalMachines" />
        </a-card>
      </a-col>
      <a-col :span="6">
        <a-card>
          <a-statistic title="空闲机器" :value="statData.freeMachines" :value-style="{ color: '#3f8600' }" />
        </a-card>
      </a-col>
      <a-col :span="6">
        <a-card>
          <a-statistic title="平均上座率" :value="statData.avgOccupancyRate" suffix="%" :precision="1" />
        </a-card>
      </a-col>
    </a-row>

    <!-- 门店实时状态表格 -->
    <a-card title="门店实时状态" style="margin-top: 16px">
      <a-table
        :columns="groupColumns"
        :data-source="groupedData"
        :loading="loading"
        row-key="configName"
        :expanded-row-keys="expandedKeys"
        :pagination="{ pageSize: 20 }"
        @expandedRowsChange="onExpandedRowsChange"
      >
        <template #expandedRowRender="{ record }">
          <a-table
            :columns="columns"
            :data-source="record.shops"
            row-key="id"
            size="small"
            :pagination="false"
          >
            <template #bodyCell="{ column: col, record: shop }">
              <template v-if="col.key === 'shopName'">
                <a-button type="link" @click="handleShowDetail(shop)">{{ shop.shop?.name || '-' }}</a-button>
              </template>
              <template v-if="col.key === 'occupancyRate'">
                <a-progress
                  :percent="shop.occupancyRate || 0"
                  :stroke-color="getProgressColor(shop.occupancyRate)"
                  size="small"
                />
              </template>
              <template v-if="col.key === 'snapshotTime'">
                {{ formatTime(shop.snapshotTime) }}
              </template>
            </template>
          </a-table>
        </template>
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'configName'">
            <span style="font-weight: 600">{{ record.configName }}</span>
          </template>
          <template v-if="column.key === 'occupancyRate'">
            <a-progress
              :percent="record.avgOccupancyRate || 0"
              :stroke-color="getProgressColor(record.avgOccupancyRate)"
              size="small"
            />
          </template>
        </template>
      </a-table>
    </a-card>

    <!-- 快照详情弹窗 -->
    <a-modal
      v-model:open="detailVisible"
      title="快照详情"
      :footer="null"
      width="1000px"
    >
      <a-spin :spinning="detailLoading">
        <template v-if="snapshotDetail.snapshot">
          <a-descriptions :column="4" bordered style="margin-bottom: 16px">
            <a-descriptions-item label="机器总数">{{ snapshotDetail.snapshot.totalMachines }}</a-descriptions-item>
            <a-descriptions-item label="空闲机器">{{ snapshotDetail.snapshot.freeMachines }}</a-descriptions-item>
            <a-descriptions-item label="占用机器">{{ snapshotDetail.snapshot.busyMachines }}</a-descriptions-item>
            <a-descriptions-item label="上座率">
              {{ snapshotDetail.snapshot.occupancyRate }}%
            </a-descriptions-item>
          </a-descriptions>

          <a-tabs v-model:activeKey="activeTab">
            <a-tab-pane key="areas" tab="区域状态">
              <a-table
                :columns="areaColumns"
                :data-source="snapshotDetail.areas"
                row-key="id"
                size="small"
              >
                <template #bodyCell="{ column, record }">
                  <template v-if="column.key === 'occupancyRate'">
                    <a-progress
                      :percent="record.occupancyRate || 0"
                      :stroke-color="getProgressColor(record.occupancyRate)"
                      size="small"
                    />
                  </template>
                  <template v-if="column.key === 'vipRoom'">
                    <a-tag :color="record.vipRoom === 1 ? 'gold' : 'default'">
                      {{ record.vipRoom === 1 ? 'VIP' : '普通' }}
                    </a-tag>
                  </template>
                </template>
              </a-table>
            </a-tab-pane>
            <a-tab-pane key="machines" tab="机器状态">
              <a-table
                :columns="machineColumns"
                :data-source="snapshotDetail.machines"
                row-key="id"
                size="small"
                :pagination="{ pageSize: 20 }"
              >
                <template #bodyCell="{ column, record }">
                  <template v-if="column.key === 'status'">
                    <a-tag :color="record.status === 1 ? 'green' : 'red'">
                      {{ record.status === 1 ? '空闲' : '占用' }}
                    </a-tag>
                  </template>
                </template>
              </a-table>
            </a-tab-pane>
          </a-tabs>
        </template>
      </a-spin>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { statusApi } from '@/api/status'
import { shopApi } from '@/api/shop'
import { configApi } from '@/api/config'
import dayjs from 'dayjs'

const loading = ref(false)
const detailLoading = ref(false)
const detailVisible = ref(false)
const activeTab = ref('areas')

const configList = ref<any[]>([])
const shopList = ref<any[]>([])
const expandedKeys = ref<string[]>([])

const rawTableData = ref<any[]>([])
const groupedData = ref<any[]>([])

const searchForm = reactive({
  configId: undefined as number | undefined,
  shopId: undefined as number | undefined
})

const statData = reactive({
  totalShops: 0,
  totalMachines: 0,
  freeMachines: 0,
  avgOccupancyRate: 0
})

// tableData 已移除，使用 snapshotDetail 替代

const snapshotDetail = reactive<{
  snapshot: any
  areas: any[]
  machines: any[]
}>({
  snapshot: null,
  areas: [],
  machines: []
})

const columns = [
  { title: 'ID', dataIndex: 'id', key: 'id', width: 80 },
  { title: '门店名称', key: 'shopName', width: 200 },
  { title: '机器总数', dataIndex: 'totalMachines', key: 'totalMachines', width: 100 },
  { title: '空闲', dataIndex: 'freeMachines', key: 'freeMachines', width: 80 },
  { title: '占用', dataIndex: 'busyMachines', key: 'busyMachines', width: 80 },
  { title: '上座率', dataIndex: 'occupancyRate', key: 'occupancyRate', width: 150 },
  { title: '快照时间', dataIndex: 'snapshotTime', key: 'snapshotTime', width: 180 }
]

const groupColumns = [
  { title: '网咖名称', dataIndex: 'configName', key: 'configName', width: 200 },
  { title: '门店数量', dataIndex: 'shopCount', key: 'shopCount', width: 100 },
  { title: '机器总数', dataIndex: 'totalMachines', key: 'totalMachines', width: 100 },
  { title: '空闲', dataIndex: 'freeMachines', key: 'freeMachines', width: 80 },
  { title: '占用', dataIndex: 'busyMachines', key: 'busyMachines', width: 80 },
  { title: '平均上座率', dataIndex: 'avgOccupancyRate', key: 'occupancyRate', width: 150 }
]

const areaColumns = [
  { title: '区域名称', dataIndex: 'areaName', key: 'areaName' },
  { title: '类型', dataIndex: 'vipRoom', key: 'vipRoom', width: 80 },
  { title: '总数', dataIndex: 'totalMachines', key: 'totalMachines', width: 80 },
  { title: '空闲', dataIndex: 'freeMachines', key: 'freeMachines', width: 80 },
  { title: '占用', dataIndex: 'busyMachines', key: 'busyMachines', width: 80 },
  { title: '上座率', dataIndex: 'occupancyRate', key: 'occupancyRate', width: 150 }
]

const machineColumns = [
  { title: '机器名称', dataIndex: 'comName', key: 'comName' },
  { title: '区域', dataIndex: 'areaName', key: 'areaName' },
  { title: '状态', dataIndex: 'status', key: 'status', width: 80 }
]

const getProgressColor = (rate: number) => {
  if (rate >= 80) return '#f5222d'
  if (rate >= 60) return '#fa8c16'
  if (rate >= 40) return '#fadb14'
  return '#52c41a'
}

const formatTime = (time: string) => {
  return time ? dayjs(time).format('MM-DD HH:mm:ss') : '-'
}

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

const handleConfigChange = async (configId: number | undefined) => {
  searchForm.shopId = undefined
  shopList.value = []
  if (configId) {
    await fetchShopList(configId)
  }
}

const handleSearch = () => {
  fetchData()
}

const handleReset = () => {
  searchForm.configId = undefined
  searchForm.shopId = undefined
  shopList.value = []
  fetchData()
}

const fetchData = async () => {
  loading.value = true
  try {
    const res = await statusApi.listRealtime(searchForm.configId, searchForm.shopId)
    rawTableData.value = res.data || []

    // 按网咖名称分组
    const groupMap = new Map<string, any>()
    rawTableData.value.forEach((item: any) => {
      const configName = item.configName || '-'
      if (!groupMap.has(configName)) {
        groupMap.set(configName, {
          configName,
          shops: [],
          shopCount: 0,
          totalMachines: 0,
          freeMachines: 0,
          busyMachines: 0,
          totalRate: 0
        })
      }
      const group = groupMap.get(configName)
      group.shops.push(item)
      group.shopCount++
      group.totalMachines += item.totalMachines || 0
      group.freeMachines += item.freeMachines || 0
      group.busyMachines += item.busyMachines || 0
      group.totalRate += item.occupancyRate || 0
    })

    // 计算平均上座率并按平均上座率从高到低排序
    groupedData.value = Array.from(groupMap.values())
      .map((group: any) => ({
        ...group,
        avgOccupancyRate: group.shopCount > 0 ? Math.round(group.totalRate / group.shopCount) : 0
      }))
      .sort((a: any, b: any) => b.avgOccupancyRate - a.avgOccupancyRate)

    // 计算统计数据
    let totalMachines = 0
    let freeMachines = 0
    let totalRate = 0
    rawTableData.value.forEach((item: any) => {
      totalMachines += item.totalMachines || 0
      freeMachines += item.freeMachines || 0
      totalRate += item.occupancyRate || 0
    })

    statData.totalShops = rawTableData.value.length
    statData.totalMachines = totalMachines
    statData.freeMachines = freeMachines
    statData.avgOccupancyRate = rawTableData.value.length > 0 ? totalRate / rawTableData.value.length : 0
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

const onExpandedRowsChange = (keys: string[]) => {
  expandedKeys.value = keys
}

const handleShowDetail = async (record: any) => {
  detailVisible.value = true
  detailLoading.value = true
  try {
    const res = await statusApi.getSnapshotDetail(record.id)
    snapshotDetail.snapshot = res.data.snapshot
    snapshotDetail.areas = res.data.areas || []
    snapshotDetail.machines = res.data.machines || []
  } catch (error) {
    console.error(error)
  } finally {
    detailLoading.value = false
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
  background: #f0f2f5;
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
  background: #fff;
  border-radius: 4px;
}

.stat-cards {
  .ant-card {
    :deep(.ant-statistic-title) {
      font-size: 14px;
    }
    :deep(.ant-statistic-content) {
      font-size: 28px;
    }
  }
}
</style>
