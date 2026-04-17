<template>
  <div class="pk-arena">
    <!-- 选择门店 -->
    <a-card title="选择PK门店" size="small" style="margin-bottom: 16px">
      <a-row :gutter="16" align="middle">
        <a-col :span="8">
          <a-select
            v-model:value="selectedRelationId"
            placeholder="选择已保存的PK关系"
            allow-clear
            style="width: 100%"
            :loading="relationLoading"
            @change="handleRelationChange"
          >
            <a-select-option v-for="item in relationList" :key="item.id" :value="item.id">
              {{ item.name }}
            </a-select-option>
          </a-select>
        </a-col>
        <a-col :span="8">
          <a-space>
            <a-button type="primary" ghost @click="showAddRelationModal">
              <plus-outlined /> 保存PK关系
            </a-button>
            <a-button @click="showEditRelationModal" :disabled="!selectedRelationId">
              <edit-outlined /> 编辑
            </a-button>
            <a-popconfirm title="确定删除此PK关系?" @confirm="handleDeleteRelation">
              <a-button danger :disabled="!selectedRelationId">
                <delete-outlined /> 删除
              </a-button>
            </a-popconfirm>
          </a-space>
        </a-col>
      </a-row>
      <a-row :gutter="16" align="middle" style="margin-top: 12px">
        <a-col :span="16">
          <a-select
            v-model:value="selectedShopIds"
            mode="multiple"
            placeholder="请选择要对比的门店（最多10个）"
            :max-tag-count="10"
            :max-count="10"
            show-search
            :filter-option="filterShopOption"
            style="width: 100%"
            :options="shopOptions"
            :loading="shopLoading"
          />
        </a-col>
        <a-col :span="8">
          <a-space>
            <a-button type="primary" :disabled="selectedShopIds.length < 2 || selectedShopIds.length > 10" @click="startPk" :loading="pkLoading">
              <thunderbolt-outlined /> 开始PK
            </a-button>
            <a-button @click="resetSelection">
              <reload-outlined /> 重置
            </a-button>
          </a-space>
        </a-col>
      </a-row>
    </a-card>

    <!-- PK竞技场 -->
    <div v-if="pkData.shops.length >= 2" class="arena-container">
      <!-- 冠军展示 -->
      <a-card class="champion-card" v-if="pkData.shops.length > 0">
        <div class="champion-content">
          <div class="trophy">
            <span class="trophy-icon">🏆</span>
            <div class="crown">👑</div>
          </div>
          <div class="champion-info">
            <div class="champion-name">{{ pkData.shops[0].shopName }}</div>
            <div class="champion-rate">
              <span class="rate-value">{{ pkData.shops[0].occupancyRate }}</span>
              <span class="rate-unit">%</span>
            </div>
            <div class="champion-label">当前上座率冠军</div>
          </div>
          <div class="confetti">
            <span v-for="i in 20" :key="i" class="confetti-piece" :style="confettiStyle(i)"></span>
          </div>
        </div>
      </a-card>

      <!-- VS对战区域 -->
      <a-row :gutter="[16, 16]" class="battle-row">
        <template v-for="(shop, index) in pkData.shops" :key="shop.shopId">
          <a-col :span="getCardSpan">
            <a-card
              class="battle-card"
              :class="{ 'winner': index === 0, 'loser': index === pkData.shops.length - 1 }"
            >
              <!-- 排名徽章 -->
              <div class="rank-badge" :class="`rank-${index + 1}`">
                <span v-if="index === 0">🥇</span>
                <span v-else-if="index === 1">🥈</span>
                <span v-else-if="index === 2">🥉</span>
                <span v-else>{{ index + 1 }}</span>
              </div>

              <!-- 门店名称 -->
              <div class="shop-name">{{ shop.shopName }}</div>

              <!-- 核心指标 -->
              <div class="main-rate">
                <a-progress 
                  type="dashboard" 
                  :percent="shop.occupancyRate" 
                  :strokeColor="getProgressColor(shop.occupancyRate)"
                  :trailColor="'#f0f0f0'"
                  :strokeWidth="pkData.shops.length > 5 ? 6 : 10"
                  :width="pkData.shops.length > 5 ? 80 : 120"
                >
                  <template #format="percent">
                    <span class="rate-number" :style="{fontSize: pkData.shops.length > 5 ? '20px' : '32px'}">{{ percent }}</span>
                    <span class="rate-unit">%</span>
                  </template>
                </a-progress>
                <div class="rate-label">当前上座率</div>
              </div>

              <!-- 详细指标 -->
              <div class="stats-grid">
                <div class="stat-item">
                  <div class="stat-value">{{ shop.totalMachines }}</div>
                  <div class="stat-label">总机器</div>
                </div>
                <div class="stat-item">
                  <div class="stat-value busy">{{ shop.busyMachines }}</div>
                  <div class="stat-label">占用中</div>
                </div>
                <div class="stat-item">
                  <div class="stat-value free">{{ shop.freeMachines }}</div>
                  <div class="stat-label">空闲</div>
                </div>
                <div class="stat-item">
                  <div class="stat-value">{{ shop.avgOccupancyRate }}</div>
                  <div class="stat-label">平均上座率</div>
                </div>
              </div>

              <!-- 趋势图 - 门店少于等于5个时显示 -->
              <div v-if="pkData.shops.length <= 5" class="trend-chart" :ref="(el: any) => setTrendChartRef(el, index)"></div>

              <!-- 极值 - 门店少于等于5个时显示 -->
              <div v-if="pkData.shops.length <= 5" class="extreme-values">
                <span class="max">最高: {{ shop.maxOccupancyRate }}%</span>
                <span class="min">最低: {{ shop.minOccupancyRate }}%</span>
              </div>
            </a-card>
          </a-col>
          <template v-if="index < pkData.shops.length - 1 && pkData.shops.length === 2">
            <a-col :span="1" class="vs-col" :key="'vs-' + index">
              <div class="vs-badge">
                <span class="vs-text">VS</span>
              </div>
            </a-col>
          </template>
        </template>
      </a-row>

      <!-- 对比柱状图 -->
      <a-card title="多维度对比" style="margin-top: 16px">
        <div ref="compareChart" style="height: 350px"></div>
      </a-card>

      <!-- 雷达图对比 -->
      <a-row :gutter="16" style="margin-top: 16px">
        <a-col :span="12">
          <a-card title="能力雷达图">
            <div ref="radarChart" style="height: 350px"></div>
          </a-card>
        </a-col>
        <a-col :span="12">
          <a-card title="数据统计">
            <a-descriptions :column="1" bordered size="small">
              <a-descriptions-item label="对比时间">
                {{ formatTime(pkData.rankTime) }}
              </a-descriptions-item>
              <a-descriptions-item label="参与门店">
                {{ pkData.shops.length }} 家
              </a-descriptions-item>
              <a-descriptions-item label="最高上座率">
                <span style="color: #52c41a; font-weight: bold;">
                  {{ Math.max(...pkData.shops.map(s => s.occupancyRate)) }}%
                </span>
              </a-descriptions-item>
              <a-descriptions-item label="最低上座率">
                <span style="color: #ff4d4f; font-weight: bold;">
                  {{ Math.min(...pkData.shops.map(s => s.occupancyRate)) }}%
                </span>
              </a-descriptions-item>
              <a-descriptions-item label="平均差距">
                <span style="color: #1890ff; font-weight: bold;">
                  {{ avgGap }}%
                </span>
              </a-descriptions-item>
            </a-descriptions>
          </a-card>
        </a-col>
      </a-row>
    </div>

    <!-- 空状态 -->
    <a-card v-else>
      <a-empty description="请选择至少2个门店开始PK对比">
        <template #image>
          <span style="font-size: 80px">⚔️</span>
        </template>
      </a-empty>
    </a-card>

    <!-- 保存PK关系弹窗 -->
    <a-modal
      v-model:open="relationModalVisible"
      :title="relationModalTitle"
      @ok="handleSaveRelation"
      @cancel="relationModalVisible = false"
    >
      <a-form :model="relationForm" :label-col="{ span: 6 }" :wrapper-col="{ span: 16 }">
        <a-form-item label="PK关系名称" required>
          <a-input v-model:value="relationForm.name" placeholder="请输入PK关系名称" />
        </a-form-item>
        <a-form-item label="主门店">
          <a-select
            v-model:value="relationForm.mainShopId"
            placeholder="请选择主门店"
            show-search
            :filter-option="filterShopOption"
            style="width: 100%"
            :options="shopOptions"
            @change="handleMainShopChange"
          />
        </a-form-item>
        <a-form-item label="PK对手门店" required>
          <a-select
            v-model:value="relationForm.competitorShopIds"
            mode="multiple"
            placeholder="请选择PK对手门店（最多9个）"
            :max-count="9"
            show-search
            :filter-option="filterShopOption"
            style="width: 100%"
            :options="shopOptions"
            @change="handleCompetitorShopChange"
          />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, nextTick } from 'vue'
import { useRoute } from 'vue-router'
import { ThunderboltOutlined, ReloadOutlined, PlusOutlined, EditOutlined, DeleteOutlined } from '@ant-design/icons-vue'
import { message } from 'ant-design-vue'
import * as echarts from 'echarts'
import { analysisApi } from '@/api/analysis'
import { shopPkRelationApi } from '@/api/shop'

const route = useRoute()

// PK关系
const relationList = ref<any[]>([])
const relationLoading = ref(false)
const selectedRelationId = ref<number | undefined>()
const relationModalVisible = ref(false)
const relationModalTitle = ref('保存PK关系')
const relationForm = reactive({
  id: undefined as number | undefined,
  name: '',
  mainShopId: undefined as number | undefined,
  mainShopName: '',
  competitorShopIds: [] as number[],
  competitorShopNames: [] as string[]
})

// 门店选择
const shopOptions = ref<{ value: number; label: string }[]>([])
const selectedShopIds = ref<number[]>([])
const shopLoading = ref(false)
const pkLoading = ref(false)

// PK数据
const pkData = reactive<{
  shops: any[]
  rankTime: string
}>({
  shops: [],
  rankTime: ''
})

// 图表引用
const trendChartRefs = ref<(HTMLElement | null)[]>([])
const compareChart = ref<HTMLElement | null>(null)
const radarChart = ref<HTMLElement | null>(null)

// 设置趋势图引用
const setTrendChartRef = (el: any, index: number) => {
  trendChartRefs.value[index] = el as HTMLElement | null
}

let chartInstances: echarts.ECharts[] = []

// 颜色配置
const COLORS = ['#ff6b6b', '#00d4ff', '#52c41a', '#faad14', '#722ed1', '#eb2f96']

// 加载门店列表
const loadShops = async () => {
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

// 加载PK关系列表
const loadRelations = async () => {
  relationLoading.value = true
  try {
    const res = await shopPkRelationApi.list()
    relationList.value = res.data || []
  } catch (error) {
    console.error(error)
  } finally {
    relationLoading.value = false
  }
}

// 选择PK关系
const handleRelationChange = async (id: number | undefined) => {
  if (!id) {
    selectedRelationId.value = undefined
    return
  }
  
  const relation = relationList.value.find(r => r.id === id)
  if (relation) {
    selectedRelationId.value = id
    // 解析门店ID
    const mainId = relation.mainShopId
    const competitorIds = relation.competitorShopIds?.split(',').map((s: string) => Number(s)) || []
    selectedShopIds.value = [mainId, ...competitorIds]
    // 自动开始PK
    startPk()
  }
}

// 显示新增PK关系弹窗
const showAddRelationModal = () => {
  if (selectedShopIds.value.length < 2) {
    message.warning('请先选择至少2个门店')
    return
  }
  relationModalTitle.value = '保存PK关系'
  relationForm.id = undefined
  relationForm.name = ''
  relationForm.mainShopId = selectedShopIds.value[0]
  relationForm.mainShopName = shopOptions.value.find(s => s.value === selectedShopIds.value[0])?.label || ''
  relationForm.competitorShopIds = selectedShopIds.value.slice(1)
  relationForm.competitorShopNames = selectedShopIds.value.slice(1).map(id => 
    shopOptions.value.find(s => s.value === id)?.label || ''
  )
  relationModalVisible.value = true
}

// 显示编辑PK关系弹窗
const showEditRelationModal = async () => {
  if (!selectedRelationId.value) return
  
  try {
    const res = await shopPkRelationApi.getById(selectedRelationId.value)
    const relation = res.data
    relationModalTitle.value = '编辑PK关系'
    relationForm.id = relation.id
    relationForm.name = relation.name
    relationForm.mainShopId = relation.mainShopId
    relationForm.mainShopName = relation.mainShopName
    relationForm.competitorShopIds = relation.competitorShopIds?.split(',').map((s: string) => Number(s)) || []
    relationForm.competitorShopNames = relation.competitorShopNames?.split(',') || []
    relationModalVisible.value = true
  } catch (error) {
    console.error(error)
  }
}

// 保存PK关系
const handleSaveRelation = async () => {
  if (!relationForm.name) {
    message.warning('请输入PK关系名称')
    return
  }
  if (!relationForm.mainShopId && relationForm.competitorShopIds.length === 0) {
    message.warning('请选择门店')
    return
  }
  
  try {
    const data = {
      name: relationForm.name,
      mainShopId: relationForm.mainShopId,
      mainShopName: relationForm.mainShopName,
      competitorShopIds: relationForm.competitorShopIds,
      competitorShopNames: relationForm.competitorShopNames
    }
    
    if (relationForm.id) {
      await shopPkRelationApi.update(relationForm.id, data)
      message.success('编辑成功')
    } else {
      await shopPkRelationApi.add(data)
      message.success('保存成功')
    }
    
    relationModalVisible.value = false
    loadRelations()
  } catch (error) {
    console.error(error)
  }
}

// 删除PK关系
const handleDeleteRelation = async () => {
  if (!selectedRelationId.value) return
  
  try {
    await shopPkRelationApi.delete(selectedRelationId.value)
    message.success('删除成功')
    selectedRelationId.value = undefined
    loadRelations()
  } catch (error) {
    console.error(error)
  }
}

// 主门店变更
const handleMainShopChange = (id: number) => {
  relationForm.mainShopName = shopOptions.value.find(s => s.value === id)?.label || ''
}

// PK对手门店变更
const handleCompetitorShopChange = (ids: number[]) => {
  relationForm.competitorShopNames = ids.map(id => 
    shopOptions.value.find(s => s.value === id)?.label || ''
  )
}

// 门店搜索过滤
const filterShopOption = (input: string, option: any) => {
  return option.label.toLowerCase().indexOf(input.toLowerCase()) >= 0
}

// 开始PK
const startPk = async () => {
  if (selectedShopIds.value.length < 2) {
    message.warning('请至少选择2个门店')
    return
  }
  if (selectedShopIds.value.length > 10) {
    message.warning('最多只能选择10个门店')
    return
  }

  pkLoading.value = true
  try {
    const res = await analysisApi.pkShops(selectedShopIds.value)
    pkData.shops = res.data?.shops || []
    pkData.rankTime = res.data?.rankTime || ''
    
    await nextTick()
    renderCharts()
  } catch (error) {
    console.error(error)
    message.error('获取PK数据失败')
  } finally {
    pkLoading.value = false
  }
}

// 重置选择
const resetSelection = () => {
  selectedShopIds.value = []
  pkData.shops = []
  pkData.rankTime = ''
  chartInstances.forEach(chart => chart?.dispose())
  chartInstances = []
}

// 渲染图表
const renderCharts = () => {
  // 销毁旧图表
  chartInstances.forEach(chart => chart?.dispose())
  chartInstances = []

  // 趋势图
  pkData.shops.forEach((shop, index) => {
    const el = trendChartRefs.value[index]
    if (el) {
      const chart = echarts.init(el)
      const trend = shop.trend || []
      
      chart.setOption({
        grid: { top: 10, right: 10, bottom: 20, left: 30 },
        xAxis: {
          type: 'category',
          data: trend.map((t: any) => formatTime(t.time)),
          axisLabel: { fontSize: 10, rotate: 45 },
          axisLine: { lineStyle: { color: '#ddd' } }
        },
        yAxis: {
          type: 'value',
          min: 0,
          max: 100,
          axisLabel: { fontSize: 10 },
          splitLine: { lineStyle: { type: 'dashed' } }
        },
        series: [{
          type: 'line',
          data: trend.map((t: any) => t.rate),
          smooth: true,
          symbol: 'none',
          lineStyle: { width: 2, color: COLORS[index] },
          areaStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: COLORS[index] + '40' },
              { offset: 1, color: COLORS[index] + '00' }
            ])
          }
        }]
      })
      
      chartInstances.push(chart)
    }
  })

  // 对比柱状图
  if (compareChart.value) {
    const chart = echarts.init(compareChart.value)
    
    chart.setOption({
      tooltip: { trigger: 'axis' },
      legend: { 
        data: pkData.shops.map((s: any) => s.shopName),
        bottom: 0
      },
      grid: { top: 30, right: 20, bottom: 60, left: 50 },
      xAxis: {
        type: 'category',
        data: ['当前上座率', '平均上座率', '最高上座率', '机器数量'],
        axisLine: { lineStyle: { color: '#ddd' } }
      },
      yAxis: {
        type: 'value',
        splitLine: { lineStyle: { type: 'dashed' } }
      },
      series: pkData.shops.map((shop: any, index: number) => ({
        name: shop.shopName,
        type: 'bar',
        data: [shop.occupancyRate, shop.avgOccupancyRate, shop.maxOccupancyRate, shop.totalMachines],
        itemStyle: { 
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: COLORS[index] },
            { offset: 1, color: COLORS[index] + '80' }
          ]),
          borderRadius: [4, 4, 0, 0]
        },
        barMaxWidth: 40
      }))
    })
    
    chartInstances.push(chart)
  }

  // 雷达图
  if (radarChart.value) {
    const chart = echarts.init(radarChart.value)
    
    const maxValues = {
      occupancyRate: 100,
      avgOccupancyRate: 100,
      maxOccupancyRate: 100,
      totalMachines: Math.max(...pkData.shops.map((s: any) => s.totalMachines)) || 100,
      busyMachines: Math.max(...pkData.shops.map((s: any) => s.busyMachines)) || 100
    }
    
    chart.setOption({
      tooltip: {},
      legend: { 
        data: pkData.shops.map((s: any) => s.shopName),
        bottom: 0
      },
      radar: {
        indicator: [
          { name: '当前上座率', max: maxValues.occupancyRate },
          { name: '平均上座率', max: maxValues.avgOccupancyRate },
          { name: '最高上座率', max: maxValues.maxOccupancyRate },
          { name: '机器总数', max: maxValues.totalMachines },
          { name: '占用数量', max: maxValues.busyMachines }
        ],
        center: ['50%', '45%'],
        radius: '60%',
        axisLine: { lineStyle: { color: '#ddd' } },
        splitLine: { lineStyle: { color: '#eee' } },
        splitArea: { show: true, areaStyle: { color: ['rgba(250,250,250,0.3)', 'rgba(200,200,200,0.3)'] } }
      },
      series: [{
        type: 'radar',
        data: pkData.shops.map((shop: any, index: number) => ({
          name: shop.shopName,
          value: [shop.occupancyRate, shop.avgOccupancyRate, shop.maxOccupancyRate, shop.totalMachines, shop.busyMachines],
          areaStyle: { color: COLORS[index] + '30' },
          lineStyle: { color: COLORS[index], width: 2 },
          itemStyle: { color: COLORS[index] }
        }))
      }]
    })
    
    chartInstances.push(chart)
  }
}

// 辅助函数
const formatTime = (time: string) => {
  if (!time) return '-'
  const date = new Date(time)
  return `${date.getMonth() + 1}/${date.getDate()} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
}

const getProgressColor = (rate: number) => {
  if (rate >= 80) return '#52c41a'
  if (rate >= 60) return '#1890ff'
  if (rate >= 40) return '#faad14'
  return '#ff4d4f'
}

const confettiStyle = (index: number) => {
  const colors = ['#ff6b6b', '#00d4ff', '#52c41a', '#faad14', '#eb2f96']
  return {
    '--delay': `${Math.random() * 3}s`,
    '--duration': `${3 + Math.random() * 2}s`,
    '--x': `${Math.random() * 100}%`,
    '--color': colors[index % colors.length]
  }
}

const avgGap = computed(() => {
  if (pkData.shops.length < 2) return 0
  const rates = pkData.shops.map(s => s.occupancyRate)
  const max = Math.max(...rates)
  const min = Math.min(...rates)
  return Math.round((max - min) * 10) / 10
})

// 计算卡片布局
const getCardSpan = computed(() => {
  const count = pkData.shops.length
  if (count === 0) return 24
  // 最多一行显示5个
  const perRow = Math.min(5, count)
  return Math.floor(24 / perRow)
})

// 监听窗口大小变化
const handleResize = () => {
  chartInstances.forEach(chart => chart?.resize())
}

onMounted(async () => {
  await loadShops()
  await loadRelations()
  window.addEventListener('resize', handleResize)

  // 检查 URL 参数，如果有 relationId 则自动加载
  const relationId = route.query.relationId
  if (relationId) {
    handleRelationChange(Number(relationId))
  }
})
</script>

<style scoped lang="less">
.pk-arena {
  padding: 24px;
  background: #f0f2f5;
  min-height: calc(100vh - 64px);
}

.champion-card {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  margin-bottom: 16px;
  
  .champion-content {
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 20px;
    position: relative;
    overflow: hidden;
  }
  
  .trophy {
    position: relative;
    margin-right: 30px;
    
    .trophy-icon {
      font-size: 80px;
      animation: float 3s ease-in-out infinite;
    }
    
    .crown {
      position: absolute;
      top: -15px;
      left: 50%;
      transform: translateX(-50%);
      font-size: 30px;
      animation: bounce 1s ease infinite;
    }
  }
  
  .champion-info {
    text-align: center;
    color: #fff;
    z-index: 1;
    
    .champion-name {
      font-size: 28px;
      font-weight: bold;
      margin-bottom: 10px;
      text-shadow: 2px 2px 4px rgba(0,0,0,0.3);
    }
    
    .champion-rate {
      .rate-value {
        font-size: 60px;
        font-weight: bold;
        background: linear-gradient(to right, #ffd700, #ffea00);
        -webkit-background-clip: text;
        -webkit-text-fill-color: transparent;
        text-shadow: none;
      }
      
      .rate-unit {
        font-size: 24px;
        color: #ffd700;
      }
    }
    
    .champion-label {
      font-size: 14px;
      opacity: 0.9;
      margin-top: 5px;
    }
  }
  
  .confetti {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    pointer-events: none;
    
    .confetti-piece {
      position: absolute;
      width: 10px;
      height: 10px;
      background: var(--color);
      left: var(--x);
      top: -10px;
      opacity: 0;
      animation: confetti-fall var(--duration) var(--delay) infinite;
    }
  }
}

@keyframes float {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-10px); }
}

@keyframes bounce {
  0%, 100% { transform: translateX(-50%) translateY(0); }
  50% { transform: translateX(-50%) translateY(-5px); }
}

@keyframes confetti-fall {
  0% { transform: translateY(0) rotate(0deg); opacity: 1; }
  100% { transform: translateY(200px) rotate(720deg); opacity: 0; }
}

.battle-row {
  margin-top: 16px;
}

.battle-card {
  position: relative;
  transition: all 0.3s;
  
  &.winner {
    border: 2px solid #ffd700;
    box-shadow: 0 0 20px rgba(255, 215, 0, 0.3);
  }
  
  &.loser {
    opacity: 0.85;
  }
  
  .rank-badge {
    position: absolute;
    top: -10px;
    right: 10px;
    width: 36px;
    height: 36px;
    border-radius: 50%;
    background: #f0f0f0;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 18px;
    box-shadow: 0 2px 8px rgba(0,0,0,0.1);
    
    &.rank-1 {
      background: linear-gradient(135deg, #ffd700, #ffec8b);
    }
    
    &.rank-2 {
      background: linear-gradient(135deg, #c0c0c0, #e8e8e8);
    }
    
    &.rank-3 {
      background: linear-gradient(135deg, #cd7f32, #daa520);
    }
  }
  
  .shop-name {
    font-size: 14px;
    font-weight: bold;
    text-align: center;
    margin-bottom: 12px;
    padding-top: 10px;
    color: #333;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
  
  .main-rate {
    display: flex;
    flex-direction: column;
    align-items: center;
    margin-bottom: 12px;

    .rate-number {
      font-size: 32px;
      font-weight: bold;
    }

    .rate-unit {
      font-size: 12px;
      color: #666;
    }

    .rate-label {
      font-size: 11px;
      color: #999;
      margin-top: 4px;
    }
  }
  
  .stats-grid {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 8px;
    margin-bottom: 12px;

    .stat-item {
      text-align: center;
      padding: 8px 6px;
      background: #fafafa;
      border-radius: 6px;

      .stat-value {
        font-size: 18px;
        font-weight: bold;
        color: #1890ff;

        &.busy { color: #ff4d4f; }
        &.free { color: #52c41a; }
      }

      .stat-label {
        font-size: 11px;
        color: #999;
        margin-top: 2px;
      }
    }
  }
  
  .trend-chart {
    height: 120px;
    margin-bottom: 12px;
  }
  
  .extreme-values {
    display: flex;
    justify-content: space-between;
    font-size: 12px;
    color: #999;
    padding-top: 12px;
    border-top: 1px solid #f0f0f0;
    
    .max { color: #52c41a; }
    .min { color: #ff4d4f; }
  }
}

.vs-col {
  display: flex;
  align-items: center;
  justify-content: center;
  
  .vs-badge {
    width: 50px;
    height: 50px;
    border-radius: 50%;
    background: linear-gradient(135deg, #ff6b6b, #ff4757);
    display: flex;
    align-items: center;
    justify-content: center;
    box-shadow: 0 4px 15px rgba(255, 75, 75, 0.4);
    animation: pulse 2s infinite;
    
    .vs-text {
      color: #fff;
      font-weight: bold;
      font-size: 16px;
      font-style: italic;
    }
  }
}

@keyframes pulse {
  0%, 100% { transform: scale(1); }
  50% { transform: scale(1.1); }
}
</style>
