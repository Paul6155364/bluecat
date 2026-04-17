<template>
  <div class="page-container">
    <div class="page-header">
      <h2>网吧配置管理</h2>
    </div>

    <!-- 搜索栏 -->
    <div class="search-bar">
      <a-form layout="inline">
        <a-form-item label="配置名称">
          <a-input v-model:value="searchForm.configName" placeholder="请输入配置名称" allow-clear />
        </a-form-item>
        <a-form-item label="状态">
          <a-select v-model:value="searchForm.status" placeholder="请选择" allow-clear style="width: 120px">
            <a-select-option :value="1">启用</a-select-option>
            <a-select-option :value="0">禁用</a-select-option>
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

    <!-- 操作栏 -->
    <div class="action-bar">
      <a-space>
        <a-button type="primary" @click="handleAdd">新增配置</a-button>
        <a-button type="default" @click="handleCollectAll" :loading="collectLoading">
          立即采集全部
        </a-button>
      </a-space>
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
        <template v-if="column.key === 'status'">
          <a-tag :color="record.status === 1 ? 'green' : 'red'">
            {{ record.status === 1 ? '启用' : '禁用' }}
          </a-tag>
        </template>
        <template v-if="column.key === 'tokenExpireTime'">
          {{ record.tokenExpireTime || '-' }}
        </template>
        <template v-if="column.key === 'action'">
          <a-space>
            <a-button type="link" size="small" @click="handleTestToken(record)" :loading="record.testing">
              测试
            </a-button>
            <a-button type="link" size="small" @click="handleRefreshToken(record)" :loading="record.refreshing">
              刷新
            </a-button>
            <a-button type="link" size="small" @click="handleCollect(record)" :loading="record.collecting">
              采集
            </a-button>
            <a-button type="link" size="small" @click="handleEdit(record)">编辑</a-button>
            <a-button
              type="link"
              size="small"
              @click="handleToggleStatus(record)"
            >
              {{ record.status === 1 ? '禁用' : '启用' }}
            </a-button>
            <a-popconfirm title="确定删除?" @confirm="handleDelete(record)">
              <a-button type="link" size="small" danger>删除</a-button>
            </a-popconfirm>
          </a-space>
        </template>
      </template>
    </a-table>

    <!-- 新增/编辑弹窗 -->
    <a-modal
      v-model:open="modalVisible"
      :title="modalTitle"
      @ok="handleSubmit"
      :confirm-loading="submitLoading"
      width="600px"
    >
      <a-form
        ref="formRef"
        :model="form"
        :rules="rules"
        :label-col="{ span: 6 }"
        :wrapper-col="{ span: 16 }"
      >
        <a-form-item label="配置名称" name="configName">
          <a-input v-model:value="form.configName" placeholder="请输入配置名称" />
        </a-form-item>
        <a-form-item label="Snbid" name="snbid">
          <a-input v-model:value="form.snbid" placeholder="请输入网吧主账号snbid" />
        </a-form-item>
        <a-form-item label="AppID" name="appId">
          <a-input v-model:value="form.appId" placeholder="请输入AppID" />
        </a-form-item>
        <a-form-item label="JWT Token" name="jwtToken">
          <a-textarea
            v-model:value="form.jwtToken"
            placeholder="请输入JWT Token"
            :rows="3"
          />
        </a-form-item>
        <a-form-item label="Cookie" name="cookie">
          <a-textarea
            v-model:value="form.cookie"
            placeholder="请输入Cookie(可选)"
            :rows="2"
          />
        </a-form-item>
        <a-form-item label="Token过期时间" name="tokenExpireTime">
          <a-date-picker
            v-model:value="form.tokenExpireTime"
            show-time
            format="YYYY-MM-DD HH:mm:ss"
            placeholder="请选择过期时间"
            style="width: 100%"
          />
        </a-form-item>
        <a-form-item label="Token来源" name="tokenSource">
          <a-input v-model:value="form.tokenSource" placeholder="请输入Token来源" />
        </a-form-item>
        <a-form-item label="状态" name="status">
          <a-radio-group v-model:value="form.status">
            <a-radio :value="1">启用</a-radio>
            <a-radio :value="0">禁用</a-radio>
          </a-radio-group>
        </a-form-item>
        <a-form-item label="备注" name="remark">
          <a-textarea v-model:value="form.remark" placeholder="请输入备注" :rows="2" />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import type { TableProps, FormInstance } from 'ant-design-vue'
import dayjs from 'dayjs'
import { configApi } from '@/api/config'
import { taskApi } from '@/api/task'
import { statusApi } from '@/api/status'

const loading = ref(false)
const submitLoading = ref(false)
const collectLoading = ref(false)
const modalVisible = ref(false)
const modalTitle = ref('新增配置')
const formRef = ref<FormInstance>()

const searchForm = reactive({
  configName: '',
  status: undefined as number | undefined
})

const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
  showSizeChanger: true,
  showTotal: (total: number) => `共 ${total} 条`
})

const columns = [
  { title: 'ID', dataIndex: 'id', key: 'id', width: 60 },
  { title: '配置名称', dataIndex: 'configName', key: 'configName', width: 120 },
  { title: 'Snbid', dataIndex: 'snbid', key: 'snbid', width: 100 },
  { title: 'AppID', dataIndex: 'appId', key: 'appId', width: 140 },
  { title: 'Token来源', dataIndex: 'tokenSource', key: 'tokenSource', width: 100 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 80 },
  { title: 'Token过期时间', dataIndex: 'tokenExpireTime', key: 'tokenExpireTime', width: 110 },
  { title: '数据采集时间', dataIndex: 'lastCollectTime', key: 'lastCollectTime', width: 110 },
  { title: '创建时间', dataIndex: 'createTime', key: 'createTime', width: 110 },
  { title: '操作', key: 'action', width: 280 }
]

const tableData = ref<any[]>([])

const form = reactive({
  id: undefined as number | undefined,
  configName: '',
  snbid: '',
  appId: '',
  jwtToken: '',
  cookie: '',
  tokenExpireTime: null as any,
  tokenSource: '',
  status: 1,
  remark: ''
})

const rules = {
  configName: [{ required: true, message: '请输入配置名称' }],
  snbid: [{ required: true, message: '请输入snbid' }],
  appId: [{ required: true, message: '请输入AppID' }],
  jwtToken: [{ required: true, message: '请输入JWT Token' }]
}

const fetchData = async () => {
  loading.value = true
  try {
    const res = await configApi.page({
      pageNum: pagination.current,
      pageSize: pagination.pageSize,
      ...searchForm
    })

    // 获取所有配置的实时状态，提取最新采集时间
    const realtimeRes = await statusApi.listRealtime()
    const realtimeData = realtimeRes.data || []

    // 按 configId 分组，获取每个配置的最新采集时间
    const collectTimeMap: Record<number, string> = {}
    realtimeData.forEach((item: any) => {
      if (item.configId && item.snapshotTime) {
        const existTime = collectTimeMap[item.configId]
        if (!existTime || new Date(item.snapshotTime) > new Date(existTime)) {
          collectTimeMap[item.configId] = item.snapshotTime
        }
      }
    })

    // 格式化时间函数
    const formatTime = (time: string) => {
      if (!time || time === '-') return '-'
      // 处理 ISO 格式时间 2026-04-17T20:37:34 -> 2026-04-17 20:37:34
      return time.replace('T', ' ')
    }

    // 合并数据
    tableData.value = res.data.records.map((record: any) => ({
      ...record,
      lastCollectTime: formatTime(collectTimeMap[record.id]) || '-'
    }))
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
  searchForm.configName = ''
  searchForm.status = undefined
  pagination.current = 1
  fetchData()
}

const handleTableChange: TableProps['onChange'] = (pag) => {
  pagination.current = pag.current || 1
  pagination.pageSize = pag.pageSize || 10
  fetchData()
}

const handleAdd = () => {
  modalTitle.value = '新增配置'
  resetForm()
  modalVisible.value = true
}

const handleEdit = (record: any) => {
  modalTitle.value = '编辑配置'
  Object.assign(form, {
    ...record,
    tokenExpireTime: record.tokenExpireTime ? dayjs(record.tokenExpireTime) : null
  })
  modalVisible.value = true
}

const resetForm = () => {
  form.id = undefined
  form.configName = ''
  form.snbid = ''
  form.appId = ''
  form.jwtToken = ''
  form.cookie = ''
  form.tokenExpireTime = null
  form.tokenSource = ''
  form.status = 1
  form.remark = ''
  formRef.value?.resetFields()
}

const handleSubmit = async () => {
  try {
    await formRef.value?.validate()
    submitLoading.value = true

    const submitData = {
      ...form,
      tokenExpireTime: form.tokenExpireTime ? dayjs(form.tokenExpireTime).format('YYYY-MM-DD HH:mm:ss') : null
    }

    if (form.id) {
      await configApi.update(submitData)
      message.success('更新成功')
    } else {
      await configApi.save(submitData)
      message.success('新增成功')
    }

    modalVisible.value = false
    fetchData()
  } catch (error) {
    console.error(error)
  } finally {
    submitLoading.value = false
  }
}

const handleToggleStatus = async (record: any) => {
  try {
    const newStatus = record.status === 1 ? 0 : 1
    await configApi.updateStatus(record.id, newStatus)
    message.success(newStatus === 1 ? '启用成功' : '禁用成功')
    fetchData()
  } catch (error) {
    console.error(error)
  }
}

const handleDelete = async (record: any) => {
  try {
    await configApi.delete(record.id)
    message.success('删除成功')
    fetchData()
  } catch (error) {
    console.error(error)
  }
}

// 采集全部配置
const handleCollectAll = async () => {
  collectLoading.value = true
  try {
    await taskApi.executeAll()
    message.success('数据采集任务已触发，请稍后查看结果')
  } catch (error) {
    console.error(error)
  } finally {
    collectLoading.value = false
  }
}

// 采集单个配置
const handleCollect = async (record: any) => {
  record.collecting = true
  try {
    await taskApi.executeByConfigId(record.id)
    message.success(`配置【${record.configName}】采集任务已触发`)
  } catch (error) {
    console.error(error)
  } finally {
    record.collecting = false
  }
}

// 测试Token
const handleTestToken = async (record: any) => {
  record.testing = true
  try {
    const res = await configApi.testToken(record.id)
    if (res.code === 200) {
      message.success(`Token有效 - snbid: ${res.data?.result?.snbid || '未知'}`)
    } else {
      message.error(res.message || 'Token无效')
    }
  } catch (error: any) {
    message.error(error.message || 'Token测试失败')
  } finally {
    record.testing = false
  }
}

// 刷新Token
const handleRefreshToken = async (record: any) => {
  record.refreshing = true
  try {
    const res = await configApi.refreshToken(record.id)
    if (res.code === 200) {
      message.success('Token刷新成功，新Token有效期2小时')
      fetchData() // 刷新列表数据
    } else {
      message.error(res.message || 'Token刷新失败')
    }
  } catch (error: any) {
    message.error(error.message || 'Token刷新失败')
  } finally {
    record.refreshing = false
  }
}

onMounted(() => {
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

.action-bar {
  margin-bottom: 16px;
}
</style>
