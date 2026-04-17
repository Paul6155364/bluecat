<template>
  <div class="page-container">
    <div class="page-header">
      <h2>角色管理</h2>
    </div>

    <!-- 操作按钮 -->
    <div class="action-bar">
      <a-button type="primary" @click="handleAdd">
        <template #icon><PlusOutlined /></template>
        新增角色
      </a-button>
    </div>

    <!-- 表格 -->
    <a-table
      :columns="columns"
      :data-source="tableData"
      :loading="loading"
      row-key="id"
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'status'">
          <a-tag :color="record.status === 1 ? 'green' : 'red'">
            {{ record.status === 1 ? '正常' : '禁用' }}
          </a-tag>
        </template>
        <template v-if="column.key === 'action'">
          <a-space>
            <a-button type="link" size="small" @click="handleEdit(record)">编辑</a-button>
            <a-button type="link" size="small" @click="handleAssignMenu(record)">分配权限</a-button>
            <a-popconfirm
              title="确定要删除此角色吗?"
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
    >
      <a-form ref="formRef" :model="formState" :rules="rules" :label-col="{ span: 6 }" :wrapper-col="{ span: 16 }">
        <a-form-item label="角色名称" name="roleName">
          <a-input v-model:value="formState.roleName" placeholder="请输入角色名称" />
        </a-form-item>
        <a-form-item label="角色编码" name="roleCode">
          <a-input v-model:value="formState.roleCode" :disabled="isEdit" placeholder="请输入角色编码" />
        </a-form-item>
        <a-form-item label="角色描述" name="description">
          <a-textarea v-model:value="formState.description" placeholder="请输入角色描述" :rows="3" />
        </a-form-item>
        <a-form-item label="排序" name="sortOrder">
          <a-input-number v-model:value="formState.sortOrder" :min="0" />
        </a-form-item>
        <a-form-item label="状态" name="status">
          <a-radio-group v-model:value="formState.status">
            <a-radio :value="1">正常</a-radio>
            <a-radio :value="0">禁用</a-radio>
          </a-radio-group>
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- 分配权限弹窗 -->
    <a-modal
      v-model:open="menuModalVisible"
      title="分配权限"
      width="600px"
      @ok="handleMenuModalOk"
      @cancel="menuModalVisible = false"
    >
      <a-tree
        v-model:checkedKeys="checkedMenuIds"
        :tree-data="menuTreeData"
        :field-names="{ title: 'menuName', key: 'id', children: 'children' }"
        checkable
        :default-expand-all="true"
      />
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import { PlusOutlined } from '@ant-design/icons-vue'
import { roleApi, menuApi } from '@/api/system'

const loading = ref(false)
const modalVisible = ref(false)
const menuModalVisible = ref(false)
const isEdit = ref(false)
const modalTitle = ref('新增角色')
const currentRoleId = ref<number>()
const checkedMenuIds = ref<number[]>([])
const menuTreeData = ref<any[]>([])

const formRef = ref()

const formState = reactive({
  id: undefined as number | undefined,
  roleName: '',
  roleCode: '',
  description: '',
  sortOrder: 0,
  status: 1
})

const rules = {
  roleName: [{ required: true, message: '请输入角色名称', trigger: 'blur' }],
  roleCode: [{ required: true, message: '请输入角色编码', trigger: 'blur' }]
}

const columns = [
  { title: 'ID', dataIndex: 'id', key: 'id', width: 80 },
  { title: '角色名称', dataIndex: 'roleName', key: 'roleName', width: 150 },
  { title: '角色编码', dataIndex: 'roleCode', key: 'roleCode', width: 150 },
  { title: '角色描述', dataIndex: 'description', key: 'description' },
  { title: '排序', dataIndex: 'sortOrder', key: 'sortOrder', width: 80 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 80 },
  { title: '创建时间', dataIndex: 'createTime', key: 'createTime', width: 180 },
  { title: '操作', key: 'action', fixed: 'right', width: 200 }
]

const tableData = ref<any[]>([])

const fetchData = async () => {
  loading.value = true
  try {
    const res = await roleApi.list()
    tableData.value = res.data || []
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

const fetchMenuTree = async () => {
  try {
    const res = await menuApi.tree()
    menuTreeData.value = res.data || []
  } catch (error) {
    console.error(error)
  }
}

const resetForm = () => {
  formState.id = undefined
  formState.roleName = ''
  formState.roleCode = ''
  formState.description = ''
  formState.sortOrder = 0
  formState.status = 1
}

const handleAdd = () => {
  isEdit.value = false
  modalTitle.value = '新增角色'
  resetForm()
  modalVisible.value = true
}

const handleEdit = (record: any) => {
  isEdit.value = true
  modalTitle.value = '编辑角色'
  Object.assign(formState, record)
  modalVisible.value = true
}

const handleModalOk = async () => {
  try {
    await formRef.value.validate()
    if (isEdit.value) {
      await roleApi.update(formState.id as any, formState)
      message.success('编辑成功')
    } else {
      await roleApi.add(formState)
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
    await roleApi.delete(record.id)
    message.success('删除成功')
    fetchData()
  } catch (error) {
    console.error(error)
  }
}

const handleAssignMenu = async (record: any) => {
  currentRoleId.value = record.id
  try {
    const res = await roleApi.getMenus(record.id)
    checkedMenuIds.value = res.data || []
    menuModalVisible.value = true
  } catch (error) {
    console.error(error)
  }
}

const handleMenuModalOk = async () => {
  try {
    await roleApi.assignMenu({
      id: currentRoleId.value,
      menuIds: checkedMenuIds.value
    })
    message.success('分配权限成功')
    menuModalVisible.value = false
  } catch (error) {
    console.error(error)
  }
}

onMounted(() => {
  fetchData()
  fetchMenuTree()
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
