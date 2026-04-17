<template>
  <div class="page-container">
    <div class="page-header">
      <h2>用户管理</h2>
    </div>

    <!-- 搜索栏 -->
    <div class="search-bar">
      <a-form layout="inline">
        <a-form-item label="用户名">
          <a-input v-model:value="searchForm.username" placeholder="请输入用户名" allow-clear />
        </a-form-item>
        <a-form-item label="真实姓名">
          <a-input v-model:value="searchForm.realName" placeholder="请输入真实姓名" allow-clear />
        </a-form-item>
        <a-form-item label="状态">
          <a-select v-model:value="searchForm.status" placeholder="请选择状态" allow-clear style="width: 120px">
            <a-select-option :value="1">正常</a-select-option>
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

    <!-- 操作按钮 -->
    <div class="action-bar">
      <a-button type="primary" @click="handleAdd">
        <template #icon><PlusOutlined /></template>
        新增用户
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
        <template v-if="column.key === 'status'">
          <a-tag :color="record.status === 1 ? 'green' : 'red'">
            {{ record.status === 1 ? '正常' : '禁用' }}
          </a-tag>
        </template>
        <template v-if="column.key === 'action'">
          <a-space>
            <a-button type="link" size="small" @click="handleEdit(record)">编辑</a-button>
            <a-button type="link" size="small" @click="handleAssignRole(record)">分配角色</a-button>
            <a-button type="link" size="small" @click="handleDataScope(record)">数据权限</a-button>
            <a-button type="link" size="small" @click="handleResetPwd(record)">重置密码</a-button>
            <a-popconfirm
              title="确定要删除此用户吗?"
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
        <a-form-item label="用户名" name="username">
          <a-input v-model:value="formState.username" :disabled="isEdit" placeholder="请输入用户名" />
        </a-form-item>
        <a-form-item v-if="!isEdit" label="密码" name="password">
          <a-input-password v-model:value="formState.password" placeholder="请输入密码，默认123456" />
        </a-form-item>
        <a-form-item label="真实姓名" name="realName">
          <a-input v-model:value="formState.realName" placeholder="请输入真实姓名" />
        </a-form-item>
        <a-form-item label="手机号" name="phone">
          <a-input v-model:value="formState.phone" placeholder="请输入手机号" />
        </a-form-item>
        <a-form-item label="邮箱" name="email">
          <a-input v-model:value="formState.email" placeholder="请输入邮箱" />
        </a-form-item>
        <a-form-item label="状态" name="status">
          <a-radio-group v-model:value="formState.status">
            <a-radio :value="1">正常</a-radio>
            <a-radio :value="0">禁用</a-radio>
          </a-radio-group>
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- 分配角色弹窗 -->
    <a-modal
      v-model:open="roleModalVisible"
      title="分配角色"
      @ok="handleRoleModalOk"
      @cancel="roleModalVisible = false"
    >
      <a-form :label-col="{ span: 4 }" :wrapper-col="{ span: 20 }">
        <a-form-item label="角色">
          <a-checkbox-group v-model:value="selectedRoleIds">
            <a-checkbox v-for="role in roleList" :key="role.id" :value="role.id">
              {{ role.roleName }}
            </a-checkbox>
          </a-checkbox-group>
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- 数据权限配置弹窗 -->
    <a-modal
      v-model:open="dataScopeModalVisible"
      title="数据权限配置"
      width="800px"
      @ok="handleDataScopeModalOk"
      @cancel="dataScopeModalVisible = false"
    >
      <a-form :label-col="{ span: 4 }" :wrapper-col="{ span: 20 }">
        <a-form-item label="数据范围">
          <a-radio-group v-model:value="dataScopeForm.dataScope" @change="handleDataScopeChange">
            <a-radio :value="1">仅授权配置</a-radio>
            <a-radio :value="2">全部数据</a-radio>
          </a-radio-group>
        </a-form-item>
        <a-form-item v-if="dataScopeForm.dataScope === 1" label="授权配置">
          <a-transfer
            v-model:target-keys="dataScopeForm.configIds"
            :data-source="allConfigs"
            :titles="['未授权', '已授权']"
            :render="(item: any) => item.configName"
            :list-style="{
              width: '350px',
              height: '400px'
            }"
            show-search
            :filter-option="filterOption"
          />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import { PlusOutlined } from '@ant-design/icons-vue'
import { userApi, roleApi } from '@/api/system'
import type { TableProps } from 'ant-design-vue'

const loading = ref(false)
const modalVisible = ref(false)
const roleModalVisible = ref(false)
const dataScopeModalVisible = ref(false)
const isEdit = ref(false)
const modalTitle = ref('新增用户')
const currentUserId = ref<number>()
const selectedRoleIds = ref<number[]>([])
const roleList = ref<any[]>([])
const allConfigs = ref<any[]>([])

const formRef = ref()

const dataScopeForm = reactive({
  dataScope: 1,
  configIds: [] as number[]
})

const searchForm = reactive({
  username: '',
  realName: '',
  status: undefined as number | undefined
})

const formState = reactive({
  id: undefined as number | undefined,
  username: '',
  password: '',
  realName: '',
  phone: '',
  email: '',
  status: 1
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  realName: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }]
}

const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
  showSizeChanger: true,
  showTotal: (total: number) => `共 ${total} 条`
})

const columns = [
  { title: 'ID', dataIndex: 'id', key: 'id', width: 80 },
  { title: '用户名', dataIndex: 'username', key: 'username', width: 120 },
  { title: '真实姓名', dataIndex: 'realName', key: 'realName', width: 120 },
  { title: '手机号', dataIndex: 'phone', key: 'phone', width: 130 },
  { title: '邮箱', dataIndex: 'email', key: 'email', width: 200 },
  { title: '数据权限', dataIndex: 'dataScope', key: 'dataScope', width: 110, customRender: ({ record }: any) => {
    return record.dataScope === 2 ? '全部数据' : '仅授权配置'
  }},
  { title: '状态', dataIndex: 'status', key: 'status', width: 80 },
  { title: '创建时间', dataIndex: 'createTime', key: 'createTime', width: 180 },
  { title: '操作', key: 'action', fixed: 'right', width: 360 }
]

const tableData = ref<any[]>([])

const fetchData = async () => {
  loading.value = true
  try {
    const res = await userApi.list({
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

const fetchRoleList = async () => {
  try {
    const res = await roleApi.list()
    roleList.value = res.data
  } catch (error) {
    console.error(error)
  }
}

const handleSearch = () => {
  pagination.current = 1
  fetchData()
}

const handleReset = () => {
  searchForm.username = ''
  searchForm.realName = ''
  searchForm.status = undefined
  pagination.current = 1
  fetchData()
}

const handleTableChange: TableProps['onChange'] = (pag) => {
  pagination.current = pag.current || 1
  pagination.pageSize = pag.pageSize || 10
  fetchData()
}

const resetForm = () => {
  formState.username = ''
  formState.password = ''
  formState.realName = ''
  formState.phone = ''
  formState.email = ''
  formState.status = 1
}

const handleAdd = () => {
  isEdit.value = false
  modalTitle.value = '新增用户'
  resetForm()
  modalVisible.value = true
}

const handleEdit = async (record: any) => {
  isEdit.value = true
  modalTitle.value = '编辑用户'
  Object.assign(formState, record)
  modalVisible.value = true
}

const handleModalOk = async () => {
  try {
    await formRef.value.validate()
    if (isEdit.value) {
      await userApi.update(formState.id as any, formState)
      message.success('编辑成功')
    } else {
      await userApi.add(formState)
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
    await userApi.delete(record.id)
    message.success('删除成功')
    fetchData()
  } catch (error) {
    console.error(error)
  }
}

const handleResetPwd = async (record: any) => {
  try {
    await userApi.resetPwd(record.id)
    message.success('密码已重置为: 123456')
  } catch (error) {
    console.error(error)
  }
}

const handleAssignRole = async (record: any) => {
  currentUserId.value = record.id
  try {
    const res = await userApi.getRoles(record.id)
    selectedRoleIds.value = res.data || []
    roleModalVisible.value = true
  } catch (error) {
    console.error(error)
  }
}

const handleRoleModalOk = async () => {
  try {
    await userApi.assignRole({
      id: currentUserId.value,
      roleIds: selectedRoleIds.value
    })
    message.success('分配角色成功')
    roleModalVisible.value = false
    fetchData()
  } catch (error) {
    console.error(error)
  }
}

// ==================== 数据权限管理 ====================

const handleDataScope = async (record: any) => {
  currentUserId.value = record.id
  
  try {
    // 获取所有网吧配置
    const configsRes = await userApi.getAllConfigs()
    allConfigs.value = configsRes.data.map((config: any) => ({
      key: config.id,
      configName: config.configName,
      ...config
    }))

    // 获取用户数据权限
    dataScopeForm.dataScope = record.dataScope || 1
    
    // 如果不是全部数据权限，获取用户已授权的网吧配置
    if (dataScopeForm.dataScope === 1) {
      const userConfigsRes = await userApi.getUserConfigs(record.id)
      dataScopeForm.configIds = userConfigsRes.data.map((config: any) => config.id)
    } else {
      dataScopeForm.configIds = []
    }

    dataScopeModalVisible.value = true
  } catch (error) {
    console.error(error)
  }
}

const handleDataScopeChange = () => {
  // 切换数据权限范围时，清空配置选择
  if (dataScopeForm.dataScope === 2) {
    dataScopeForm.configIds = []
  }
}

const filterOption = (inputValue: string, option: any) => {
  return option.configName.toLowerCase().indexOf(inputValue.toLowerCase()) > -1
}

const handleDataScopeModalOk = async () => {
  try {
    // 更新数据权限范围
    await userApi.updateDataScope(currentUserId.value!, dataScopeForm.dataScope)
    
    // 如果是仅授权配置，保存网吧配置授权
    if (dataScopeForm.dataScope === 1) {
      await userApi.saveUserConfigs(currentUserId.value!, dataScopeForm.configIds)
    }
    
    message.success('数据权限配置成功')
    dataScopeModalVisible.value = false
    fetchData()
  } catch (error) {
    console.error(error)
  }
}

onMounted(() => {
  fetchData()
  fetchRoleList()
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

.action-bar {
  margin-bottom: 16px;
}
</style>
