<template>
  <div class="page-container">
    <div class="page-header">
      <h2>PK关系管理</h2>
    </div>

    <!-- 操作按钮 -->
    <div class="action-bar">
      <a-button type="primary" @click="handleAdd">
        <template #icon><PlusOutlined /></template>
        新增PK关系
      </a-button>
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
        <template v-if="column.key === 'mainShopName'">
          <a-tag color="blue">{{ record.mainShopName || record.mainShopId }}</a-tag>
        </template>
        <template v-if="column.key === 'competitorShopNames'">
          <a-tag v-for="(name, idx) in getCompetitorNames(record)" :key="idx" color="orange" style="margin: 2px">
            {{ name }}
          </a-tag>
        </template>
        <template v-if="column.key === 'action'">
          <a-space>
            <a-button type="link" size="small" @click="handlePk(record)">PK</a-button>
            <a-button type="link" size="small" @click="handleEdit(record)">编辑</a-button>
            <a-popconfirm title="确定删除此PK关系?" @confirm="handleDelete(record)">
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
      @ok="handleModalOk"
      @cancel="handleModalCancel"
      width="600px"
    >
      <a-form ref="formRef" :model="formState" :rules="rules" :label-col="{ span: 6 }" :wrapper-col="{ span: 16 }">
        <a-form-item label="PK关系名称" name="name">
          <a-input v-model:value="formState.name" placeholder="请输入PK关系名称" />
        </a-form-item>
        <a-form-item label="主门店" name="mainShopId">
          <a-select
            v-model:value="formState.mainShopId"
            placeholder="请选择主门店"
            show-search
            :filter-option="filterShopOption"
            style="width: 100%"
            :options="shopOptions"
            :loading="shopLoading"
            @change="handleMainShopChange"
          />
        </a-form-item>
        <a-form-item label="PK对手门店" name="competitorShopIds">
          <a-select
            v-model:value="formState.competitorShopIds"
            mode="multiple"
            placeholder="请选择PK对手门店（最多9个）"
            :max-count="9"
            :max-tag-count="5"
            show-search
            :filter-option="filterShopOption"
            style="width: 100%"
            :options="shopOptions"
            :loading="shopLoading"
            @change="handleCompetitorShopChange"
          />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { PlusOutlined } from '@ant-design/icons-vue'
import { shopPkRelationApi } from '@/api/shop'
import { analysisApi } from '@/api/analysis'
import type { TableProps, FormInstance } from 'ant-design-vue'

const router = useRouter()
const loading = ref(false)
const shopLoading = ref(false)
const modalVisible = ref(false)
const modalTitle = ref('新增PK关系')
const formRef = ref<FormInstance>()

const shopOptions = ref<{ value: number; label: string }[]>([])
const tableData = ref<any[]>([])

const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
  showSizeChanger: true,
  showTotal: (total: number) => `共 ${total} 条`
})

const columns = [
  { title: 'ID', dataIndex: 'id', key: 'id', width: 80 },
  { title: 'PK关系名称', dataIndex: 'name', key: 'name', width: 200 },
  { title: '主门店', dataIndex: 'mainShopName', key: 'mainShopName', width: 150 },
  { title: 'PK对手门店', dataIndex: 'competitorShopNames', key: 'competitorShopNames' },
  { title: '创建时间', dataIndex: 'createTime', key: 'createTime', width: 180 },
  { title: '操作', key: 'action', width: 200 }
]

const formState = reactive({
  id: undefined as number | undefined,
  name: '',
  mainShopId: undefined as number | undefined,
  mainShopName: '',
  competitorShopIds: [] as number[],
  competitorShopNames: [] as string[]
})

const rules = {
  name: [{ required: true, message: '请输入PK关系名称', trigger: 'blur' }],
  competitorShopIds: [{ required: true, message: '请选择PK对手门店', trigger: 'change' }]
}

const getCompetitorNames = (record: any) => {
  if (record.competitorShopNames) {
    return record.competitorShopNames.split(',')
  }
  if (record.competitorShopIds) {
    return record.competitorShopIds.split(',')
  }
  return []
}

const fetchShops = async () => {
  shopLoading.value = true
  try {
    const res = await analysisApi.listShops()
    shopOptions.value = (res.data || []).map((shop: any) => ({
      value: shop.id,
      label: shop.name
    }))
  } catch (error) {
    console.error(error)
  } finally {
    shopLoading.value = false
  }
}

const fetchData = async () => {
  loading.value = true
  try {
    const res = await shopPkRelationApi.list()
    tableData.value = res.data || []
    pagination.total = tableData.value.length
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

const handleTableChange: TableProps['onChange'] = (pag) => {
  pagination.current = pag.current || 1
  pagination.pageSize = pag.pageSize || 10
}

const handleMainShopChange = (id: number) => {
  formState.mainShopName = shopOptions.value.find(s => s.value === id)?.label || ''
}

const handleCompetitorShopChange = (ids: number[]) => {
  // 限制最多选择9个
  if (ids.length > 9) {
    message.warning('最多只能选择9个PK对手门店')
    formState.competitorShopIds = ids.slice(0, 9)
    formState.competitorShopNames = ids.slice(0, 9).map(id =>
      shopOptions.value.find(s => s.value === id)?.label || ''
    )
    return
  }
  formState.competitorShopNames = ids.map(id =>
    shopOptions.value.find(s => s.value === id)?.label || ''
  )
}

// 门店搜索过滤
const filterShopOption = (input: string, option: any) => {
  return option.label.toLowerCase().indexOf(input.toLowerCase()) >= 0
}

const resetForm = () => {
  formState.id = undefined
  formState.name = ''
  formState.mainShopId = undefined
  formState.mainShopName = ''
  formState.competitorShopIds = []
  formState.competitorShopNames = []
  formRef.value?.resetFields()
}

const handleAdd = () => {
  modalTitle.value = '新增PK关系'
  resetForm()
  modalVisible.value = true
}

const handleEdit = (record: any) => {
  modalTitle.value = '编辑PK关系'
  formState.id = record.id
  formState.name = record.name
  formState.mainShopId = record.mainShopId
  formState.mainShopName = record.mainShopName
  formState.competitorShopIds = record.competitorShopIds?.split(',').map((s: string) => Number(s)) || []
  formState.competitorShopNames = record.competitorShopNames?.split(',') || []
  modalVisible.value = true
}

const handleModalOk = async () => {
  try {
    await formRef.value?.validate()
    
    const data = {
      name: formState.name,
      mainShopId: formState.mainShopId,
      mainShopName: formState.mainShopName,
      competitorShopIds: formState.competitorShopIds,
      competitorShopNames: formState.competitorShopNames
    }

    if (formState.id) {
      await shopPkRelationApi.update(formState.id, data)
      message.success('编辑成功')
    } else {
      await shopPkRelationApi.add(data)
      message.success('新增成功')
    }

    modalVisible.value = false
    fetchData()
  } catch (error) {
    console.error(error)
  }
}

const handleModalCancel = () => {
  modalVisible.value = false
  formRef.value?.resetFields()
}

const handleDelete = async (record: any) => {
  try {
    await shopPkRelationApi.delete(record.id)
    message.success('删除成功')
    fetchData()
  } catch (error) {
    console.error(error)
  }
}

const handlePk = (record: any) => {
  router.push({
    path: '/pk',
    query: { relationId: record.id }
  })
}

onMounted(() => {
  fetchShops()
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

.action-bar {
  margin-bottom: 16px;
}
</style>
