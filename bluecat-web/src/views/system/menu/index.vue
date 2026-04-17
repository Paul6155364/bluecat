<template>
  <div class="page-container">
    <div class="page-header">
      <h2>菜单管理</h2>
    </div>

    <!-- 操作按钮 -->
    <div class="action-bar">
      <a-button type="primary" @click="handleAdd(0)">
        <template #icon><PlusOutlined /></template>
        新增菜单
      </a-button>
    </div>

    <!-- 表格 -->
    <a-table
      :columns="columns"
      :data-source="tableData"
      :loading="loading"
      row-key="id"
      :default-expand-all-rows="true"
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'menuType'">
          <a-tag :color="getMenuTypeColor(record.menuType)">
            {{ getMenuTypeText(record.menuType) }}
          </a-tag>
        </template>
        <template v-if="column.key === 'icon'">
          <component :is="record.icon" v-if="record.icon" />
        </template>
        <template v-if="column.key === 'visible'">
          <a-tag :color="record.visible === 1 ? 'green' : 'red'">
            {{ record.visible === 1 ? '是' : '否' }}
          </a-tag>
        </template>
        <template v-if="column.key === 'status'">
          <a-tag :color="record.status === 1 ? 'green' : 'red'">
            {{ record.status === 1 ? '正常' : '禁用' }}
          </a-tag>
        </template>
        <template v-if="column.key === 'action'">
          <a-space>
            <a-button type="link" size="small" @click="handleAdd(record.id)">新增</a-button>
            <a-button type="link" size="small" @click="handleEdit(record)">编辑</a-button>
            <a-popconfirm
              title="确定要删除此菜单吗?"
              @confirm="handleDelete(record)"
            >
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
        <a-form-item label="上级菜单" name="parentId">
          <a-tree-select
            v-model:value="formState.parentId"
            :tree-data="menuTreeData"
            :field-names="{ label: 'menuName', value: 'id', children: 'children' }"
            placeholder="请选择上级菜单"
            allow-clear
            tree-default-expand-all
          />
        </a-form-item>
        <a-form-item label="菜单类型" name="menuType">
          <a-radio-group v-model:value="formState.menuType">
            <a-radio :value="1">目录</a-radio>
            <a-radio :value="2">菜单</a-radio>
            <a-radio :value="3">按钮</a-radio>
          </a-radio-group>
        </a-form-item>
        <a-form-item label="菜单名称" name="menuName">
          <a-input v-model:value="formState.menuName" placeholder="请输入菜单名称" />
        </a-form-item>
        <a-form-item label="菜单编码" name="menuCode">
          <a-input v-model:value="formState.menuCode" placeholder="请输入菜单编码" />
        </a-form-item>
        <a-form-item v-if="formState.menuType !== 3" label="路由路径" name="path">
          <a-input v-model:value="formState.path" placeholder="请输入路由路径" />
        </a-form-item>
        <a-form-item v-if="formState.menuType !== 3" label="菜单图标" name="icon">
          <a-input v-model:value="formState.icon" placeholder="请输入图标名称" />
        </a-form-item>
        <a-form-item v-if="formState.menuType === 2" label="组件路径" name="component">
          <a-input v-model:value="formState.component" placeholder="请输入组件路径" />
        </a-form-item>
        <a-form-item label="排序" name="sortOrder">
          <a-input-number v-model:value="formState.sortOrder" :min="0" />
        </a-form-item>
        <a-form-item v-if="formState.menuType !== 3" label="是否可见" name="visible">
          <a-radio-group v-model:value="formState.visible">
            <a-radio :value="1">是</a-radio>
            <a-radio :value="0">否</a-radio>
          </a-radio-group>
        </a-form-item>
        <a-form-item label="状态" name="status">
          <a-radio-group v-model:value="formState.status">
            <a-radio :value="1">正常</a-radio>
            <a-radio :value="0">禁用</a-radio>
          </a-radio-group>
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import { PlusOutlined } from '@ant-design/icons-vue'
import { menuApi } from '@/api/system'

const loading = ref(false)
const modalVisible = ref(false)
const isEdit = ref(false)
const modalTitle = ref('新增菜单')
const menuTreeData = ref<any[]>([])

const formRef = ref()

const formState = reactive({
  id: undefined as number | undefined,
  parentId: 0,
  menuName: '',
  menuCode: '',
  path: '',
  icon: '',
  component: '',
  menuType: 2,
  sortOrder: 0,
  visible: 1,
  status: 1
})

const rules = {
  menuName: [{ required: true, message: '请输入菜单名称', trigger: 'blur' }],
  menuType: [{ required: true, message: '请选择菜单类型', trigger: 'change' }]
}

const columns = [
  { title: '菜单名称', dataIndex: 'menuName', key: 'menuName', width: 200 },
  { title: '图标', dataIndex: 'icon', key: 'icon', width: 80 },
  { title: '类型', dataIndex: 'menuType', key: 'menuType', width: 80 },
  { title: '菜单编码', dataIndex: 'menuCode', key: 'menuCode', width: 150 },
  { title: '路由路径', dataIndex: 'path', key: 'path', width: 150 },
  { title: '排序', dataIndex: 'sortOrder', key: 'sortOrder', width: 80 },
  { title: '可见', dataIndex: 'visible', key: 'visible', width: 80 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 80 },
  { title: '操作', key: 'action', fixed: 'right', width: 200 }
]

const tableData = ref<any[]>([])

const getMenuTypeText = (type: number) => {
  const map: Record<number, string> = {
    1: '目录',
    2: '菜单',
    3: '按钮'
  }
  return map[type] || '未知'
}

const getMenuTypeColor = (type: number) => {
  const map: Record<number, string> = {
    1: 'blue',
    2: 'green',
    3: 'orange'
  }
  return map[type] || 'default'
}

// 扁平化树结构（备用）
// const flattenTree = (data: any[]): any[] => {
//   const result: any[] = []
//   const flatten = (list: any[]) => {
//     list.forEach(item => {
//       result.push(item)
//       if (item.children && item.children.length > 0) {
//         flatten(item.children)
//       }
//     })
//   }
//   flatten(data)
//   return result
// }

const fetchData = async () => {
  loading.value = true
  try {
    const res = await menuApi.tree()
    menuTreeData.value = res.data || []
    tableData.value = menuTreeData.value
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

const resetForm = () => {
  formState.id = undefined
  formState.parentId = 0
  formState.menuName = ''
  formState.menuCode = ''
  formState.path = ''
  formState.icon = ''
  formState.component = ''
  formState.menuType = 2
  formState.sortOrder = 0
  formState.visible = 1
  formState.status = 1
}

const handleAdd = (parentId: number) => {
  isEdit.value = false
  modalTitle.value = '新增菜单'
  resetForm()
  formState.parentId = parentId
  modalVisible.value = true
}

const handleEdit = (record: any) => {
  isEdit.value = true
  modalTitle.value = '编辑菜单'
  Object.assign(formState, record)
  modalVisible.value = true
}

const handleModalOk = async () => {
  try {
    await formRef.value.validate()
    if (isEdit.value) {
      await menuApi.update(formState.id as any, formState)
      message.success('编辑成功')
    } else {
      await menuApi.add(formState)
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
    await menuApi.delete(record.id)
    message.success('删除成功')
    fetchData()
  } catch (error) {
    console.error(error)
  }
}

onMounted(() => {
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

.action-bar {
  margin-bottom: 16px;
}
</style>
