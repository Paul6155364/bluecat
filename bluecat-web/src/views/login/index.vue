<template>
  <div class="login-container">
    <!-- 动态背景 -->
    <div class="bg-grid"></div>
    <div class="bg-glow"></div>
    
    <!-- 浮动粒子 -->
    <div class="particles">
      <div v-for="i in 20" :key="i" class="particle" :style="getParticleStyle(i)"></div>
    </div>

    <!-- 脉搏心电图背景 -->
    <div class="pulse-line">
      <svg viewBox="0 0 1200 100" preserveAspectRatio="none">
        <path class="pulse-path pulse-1" d="M0,50 L150,50 L180,50 L200,20 L220,80 L240,30 L260,70 L280,50 L400,50 L430,50 L450,20 L470,80 L490,30 L510,70 L530,50 L650,50 L680,50 L700,20 L720,80 L740,30 L760,70 L780,50 L900,50 L930,50 L950,20 L970,80 L990,30 L1010,70 L1030,50 L1200,50" />
        <path class="pulse-path pulse-2" d="M0,50 L150,50 L180,50 L200,20 L220,80 L240,30 L260,70 L280,50 L400,50 L430,50 L450,20 L470,80 L490,30 L510,70 L530,50 L650,50 L680,50 L700,20 L720,80 L740,30 L760,70 L780,50 L900,50 L930,50 L950,20 L970,80 L990,30 L1010,70 L1030,50 L1200,50" />
      </svg>
    </div>

    <!-- 准星装饰 - 三角洲风格 -->
    <div class="crosshair crosshair-tl"></div>
    <div class="crosshair crosshair-tr"></div>
    <div class="crosshair crosshair-bl"></div>
    <div class="crosshair crosshair-br"></div>

    <!-- 弹道轨迹线 -->
    <div class="trajectory-lines">
      <svg width="100%" height="100%" preserveAspectRatio="none">
        <line x1="0" y1="25%" x2="30%" y2="45%" class="trajectory" />
        <line x1="100%" y1="20%" x2="70%" y2="42%" class="trajectory trajectory-delay" />
        <line x1="0" y1="75%" x2="25%" y2="58%" class="trajectory trajectory-delay-2" />
        <line x1="100%" y1="80%" x2="75%" y2="55%" class="trajectory trajectory-delay-3" />
      </svg>
    </div>

    <!-- 战术网格线 -->
    <div class="tactical-grid">
      <div class="grid-line horizontal h1"></div>
      <div class="grid-line horizontal h2"></div>
      <div class="grid-line vertical v1"></div>
      <div class="grid-line vertical v2"></div>
    </div>

    <!-- 左侧情报面板 -->
    <div class="intel-panel left-panel">
      <div class="panel-header">
        <span class="panel-icon">▣</span>
        <span class="panel-title">INTEL FEED</span>
      </div>
      <div class="panel-content">
        <div class="intel-item" v-for="(item, index) in intelItems" :key="index">
          <span class="intel-time">00:{{ currentTime }}</span>
          <span class="intel-text">{{ item }}</span>
        </div>
      </div>
    </div>

    <!-- 右侧小队状态面板 -->
    <div class="intel-panel right-panel">
      <div class="panel-header">
        <span class="panel-title">SQUAD STATUS</span>
        <span class="panel-icon">▣</span>
      </div>
      <div class="panel-content">
        <div class="squad-item" v-for="squad in squadStatus" :key="squad.name">
          <span class="squad-name">{{ squad.name }}</span>
          <div class="squad-bar">
            <div class="squad-fill" :style="{ width: squad.value + '%' }"></div>
          </div>
          <span class="squad-val">{{ squad.value }}%</span>
        </div>
      </div>
    </div>

    <!-- 登录卡片 -->
    <div class="login-card">
      <div class="card-border"></div>
      
      <!-- HUD 风格角落 -->
      <div class="hud-corner hud-tl"></div>
      <div class="hud-corner hud-tr"></div>
      <div class="hud-corner hud-bl"></div>
      <div class="hud-corner hud-br"></div>
      
      <div class="card-content">
        <div class="login-header">
          <div class="logo-wrapper">
            <img src="@/assets/logo.svg" alt="logo" class="logo" />
            <div class="logo-glow"></div>
            <!-- 脉搏动画环 -->
            <div class="pulse-ring"></div>
            <div class="pulse-ring delay-1"></div>
            <div class="pulse-ring delay-2"></div>
          </div>
          <h1 class="title">
            <span class="title-text">CyberPulse</span>
            <span class="title-sub">网咖脉搏</span>
          </h1>
          <p class="slogan">实时监控 · 数据驱动 · 智能决策</p>
          
          <!-- 任务状态栏 -->
          <div class="mission-status">
            <div class="status-item">
              <span class="status-icon">◉</span>
              <span class="status-label">OPERATION</span>
              <span class="status-value">ACTIVE</span>
            </div>
            <div class="status-divider"></div>
            <div class="heartbeat-indicator">
              <span class="heart-icon">♥</span>
              <span class="bpm">{{ bpmValue }}</span>
              <span class="bpm-label">BPM</span>
            </div>
          </div>
        </div>

        <a-form
          :model="formState"
          :rules="rules"
          @finish="handleLogin"
          class="login-form"
        >
          <a-form-item name="username">
            <div class="input-wrapper">
              <div class="input-header">
                <span class="input-code">CODE: 01</span>
                <span class="input-label">OPERATOR ID</span>
              </div>
              <a-input
                v-model:value="formState.username"
                size="large"
                placeholder="输入操作员ID"
                class="cyber-input"
              >
                <template #prefix>
                  <UserOutlined class="input-icon" />
                </template>
              </a-input>
            </div>
          </a-form-item>
          
          <a-form-item name="password">
            <div class="input-wrapper">
              <div class="input-header">
                <span class="input-code">CODE: 02</span>
                <span class="input-label">SECURITY KEY</span>
              </div>
              <a-input-password
                v-model:value="formState.password"
                size="large"
                placeholder="输入安全密钥"
                class="cyber-input"
              >
                <template #prefix>
                  <LockOutlined class="input-icon" />
                </template>
              </a-input-password>
            </div>
          </a-form-item>

          <a-form-item>
            <a-button
              type="primary"
              size="large"
              html-type="submit"
              :loading="loading"
              block
              class="login-btn"
            >
              <span class="btn-icon">▶</span>
              <span class="btn-text">DEPLOY</span>
              <span class="btn-arrow">→</span>
              <span class="btn-glitch"></span>
            </a-button>
          </a-form-item>

          <div class="register-link">
            <a @click="showRegisterModal">
              <span class="link-crosshair">⊕</span>
              加入行动摸点好货
            </a>
          </div>
        </a-form>

        <!-- 注册提示弹窗 -->
        <a-modal
          v-model:open="registerVisible"
          title=""
          :footer="null"
          :width="360"
          class="register-modal"
          centered
        >
          <div class="register-content">
            <!-- 管理员信息卡片 -->
            <div class="admin-card">
              <div class="admin-avatar">
                <img src="/admin-avatar.jpg" alt="管理员头像" />
                <div class="avatar-scope"></div>
              </div>
              <div class="admin-info">
                <div class="admin-name-wrapper">
                  <span class="name-crosshair">⊗</span>
                  <h4 class="admin-name">架势贝利</h4>
                  <span class="name-crosshair">⊗</span>
                </div>
                <div class="admin-tags">
                  <span class="admin-tag">SQUAD LEADER</span>
                  <span class="admin-tag tag-rank">RANK: S</span>
                </div>
              </div>
            </div>

            <h3 class="register-title">
              <span class="title-icon">◈</span>
              联系指挥官申请入队
              <span class="title-icon">◈</span>
            </h3>
            <div class="contact-info">
              <div class="contact-item">
                <PhoneOutlined class="contact-icon" />
                <span class="contact-label">电话</span>
                <span class="contact-value phone-highlight">15828982828</span>
              </div>
              <div class="contact-item">
                <WechatOutlined class="contact-icon wechat" />
                <span class="contact-label">微信</span>
                <span class="contact-value wechat-highlight">shenjunchen_</span>
              </div>
            </div>
            <p class="register-tip">DEPLOYMENT HOURS: 09:00 - 22:00</p>
          </div>
        </a-modal>

        <div class="footer">
          <div class="scan-line"></div>
          <div class="stats-row">
            <div class="stat-item">
              <span class="stat-icon">◈</span>
              <span class="stat-value">24/7</span>
              <span class="stat-label">SURVEILLANCE</span>
            </div>
            <div class="stat-divider"></div>
            <div class="stat-item">
              <span class="stat-icon">◈</span>
              <span class="stat-value">{{ machineCount }}+</span>
              <span class="stat-label">TERMINALS</span>
            </div>
            <div class="stat-divider"></div>
            <div class="stat-item">
              <span class="stat-icon">◈</span>
              <span class="stat-value">{{ shopCount }}</span>
              <span class="stat-label">SECTORS</span>
            </div>
          </div>
          
          <!-- 战术信息 -->
          <div class="tactical-info">
            <span class="tac-line">/// TACTICAL NETWORK CONNECTED</span>
            <span class="tac-line">/// AWAITING DEPLOYMENT AUTHORIZATION</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 角落装饰 -->
    <div class="corner-decor top-left">
      <span class="corner-text">DELTA OPS</span>
      <div class="corner-line"></div>
    </div>
    <div class="corner-decor top-right">
      <div class="corner-line"></div>
      <span class="corner-text">SECURE CH</span>
    </div>
    <div class="corner-decor bottom-left">
      <span class="corner-text">v2.0.26</span>
      <div class="corner-line"></div>
    </div>
    <div class="corner-decor bottom-right">
      <div class="corner-line"></div>
      <span class="corner-text">ONLINE</span>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { UserOutlined, LockOutlined, PhoneOutlined, WechatOutlined } from '@ant-design/icons-vue'
import { post } from '@/utils/request'

const router = useRouter()
const loading = ref(false)
const machineCount = ref(0)
const shopCount = ref(0)
const bpmValue = ref(72)
const currentTime = ref('00')
const registerVisible = ref(false)

const intelItems = [
  'SYSTEM OPERATIONAL',
  'SCAN MODE: ACTIVE',
  'THREAT LEVEL: LOW',
  'NETWORK: STABLE'
]

const squadStatus = [
  { name: 'ALPHA', value: 85 },
  { name: 'BRAVO', value: 72 },
  { name: 'DELTA', value: 91 }
]

const formState = reactive({
  username: '',
  password: ''
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const handleLogin = async () => {
  try {
    loading.value = true
    const res = await post('/auth/login', formState)

    localStorage.setItem('token', res.data.token)
    localStorage.setItem('userInfo', JSON.stringify(res.data.user))
    localStorage.setItem('menus', JSON.stringify(res.data.menus || []))

    message.success('登录成功')
    router.push('/')
  } catch (error) {
    console.error('Login error:', error)
  } finally {
    loading.value = false
  }
}

// 生成随机粒子样式
const getParticleStyle = (_index: number) => {
  const size = Math.random() * 4 + 2
  const x = Math.random() * 100
  const y = Math.random() * 100
  const duration = Math.random() * 20 + 10
  const delay = Math.random() * 10
  
  return {
    width: `${size}px`,
    height: `${size}px`,
    left: `${x}%`,
    top: `${y}%`,
    animationDuration: `${duration}s`,
    animationDelay: `${delay}s`
  }
}

// 模拟统计数据动画
const animateStats = () => {
  const targetMachines = 2000
  const targetShops = 50
  const duration = 2000
  const startTime = Date.now()
  
  const animate = () => {
    const elapsed = Date.now() - startTime
    const progress = Math.min(elapsed / duration, 1)
    const easeOut = 1 - Math.pow(1 - progress, 3)
    
    machineCount.value = Math.floor(targetMachines * easeOut)
    shopCount.value = Math.floor(targetShops * easeOut)
    
    if (progress < 1) {
      requestAnimationFrame(animate)
    }
  }
  
  animate()
}

// 时间更新
let timeInterval: number
let bpmInterval: number

const updateTime = () => {
  const now = new Date()
  currentTime.value = String(now.getSeconds()).padStart(2, '0')
}

const updateBpm = () => {
  bpmValue.value = 70 + Math.floor(Math.random() * 5)
}

const showRegisterModal = () => {
  registerVisible.value = true
}

onMounted(() => {
  animateStats()
  timeInterval = window.setInterval(updateTime, 1000)
  bpmInterval = window.setInterval(updateBpm, 1000)
})

onUnmounted(() => {
  clearInterval(timeInterval)
  clearInterval(bpmInterval)
})
</script>

<style scoped lang="less">
.login-container {
  position: relative;
  display: flex;
  justify-content: center;
  align-items: center;
  width: 100%;
  height: 100vh;
  background: #0a0a0f;
  overflow: hidden;
}

// 网格背景
.bg-grid {
  position: absolute;
  inset: 0;
  background-image: 
    linear-gradient(rgba(0, 212, 255, 0.03) 1px, transparent 1px),
    linear-gradient(90deg, rgba(0, 212, 255, 0.03) 1px, transparent 1px);
  background-size: 50px 50px;
  animation: gridMove 20s linear infinite;
}

@keyframes gridMove {
  0% { transform: perspective(500px) rotateX(60deg) translateY(0); }
  100% { transform: perspective(500px) rotateX(60deg) translateY(50px); }
}

// 发光效果
.bg-glow {
  position: absolute;
  width: 600px;
  height: 600px;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(0, 212, 255, 0.15) 0%, transparent 70%);
  animation: glowPulse 4s ease-in-out infinite;
}

@keyframes glowPulse {
  0%, 100% { opacity: 0.5; transform: scale(1); }
  50% { opacity: 0.8; transform: scale(1.1); }
}

// 粒子效果
.particles {
  position: absolute;
  inset: 0;
  pointer-events: none;
}

.particle {
  position: absolute;
  background: #00d4ff;
  border-radius: 50%;
  opacity: 0;
  animation: particleFloat 15s linear infinite;
  box-shadow: 0 0 10px #00d4ff;
}

@keyframes particleFloat {
  0% { opacity: 0; transform: translateY(100vh) scale(0); }
  10% { opacity: 1; }
  90% { opacity: 1; }
  100% { opacity: 0; transform: translateY(-100vh) scale(1); }
}

// 脉搏心电图背景
.pulse-line {
  position: absolute;
  bottom: 20%;
  left: 0;
  right: 0;
  height: 100px;
  opacity: 0.2;
  pointer-events: none;
  
  svg { width: 200%; height: 100%; }
  
  .pulse-path {
    fill: none;
    stroke-width: 2;
    stroke-linecap: round;
    stroke-linejoin: round;
  }
  
  .pulse-1 {
    stroke: #00d4ff;
    stroke-dasharray: 2000;
    stroke-dashoffset: 2000;
    animation: pulseDraw 3s linear infinite;
  }
  
  .pulse-2 {
    stroke: #f72585;
    stroke-dasharray: 2000;
    stroke-dashoffset: 2000;
    animation: pulseDraw 3s linear infinite 0.1s;
    opacity: 0.5;
  }
}

@keyframes pulseDraw {
  0% { stroke-dashoffset: 2000; }
  100% { stroke-dashoffset: 0; }
}

// 准星装饰 - 三角洲风格
.crosshair {
  position: absolute;
  width: 40px;
  height: 40px;
  pointer-events: none;
  
  &::before, &::after {
    content: '';
    position: absolute;
    background: rgba(255, 71, 87, 0.6);
  }
  
  &::before {
    width: 2px;
    height: 100%;
    left: 50%;
    transform: translateX(-50%);
  }
  
  &::after {
    width: 100%;
    height: 2px;
    top: 50%;
    transform: translateY(-50%);
  }
  
  &.crosshair-tl { top: 60px; left: 60px; }
  &.crosshair-tr { top: 60px; right: 60px; }
  &.crosshair-bl { bottom: 60px; left: 60px; }
  &.crosshair-br { bottom: 60px; right: 60px; }
}

// 弹道轨迹线
.trajectory-lines {
  position: absolute;
  inset: 0;
  pointer-events: none;
  
  .trajectory {
    stroke: rgba(255, 71, 87, 0.25);
    stroke-width: 1;
    stroke-dasharray: 8 4;
    animation: trajectoryMove 4s linear infinite;
    
    &.trajectory-delay { animation-delay: 1s; }
    &.trajectory-delay-2 { animation-delay: 2s; }
    &.trajectory-delay-3 { animation-delay: 3s; }
  }
}

@keyframes trajectoryMove {
  0% { stroke-dashoffset: 0; }
  100% { stroke-dashoffset: -24; }
}

// 战术网格线
.tactical-grid {
  position: absolute;
  inset: 0;
  pointer-events: none;
  
  .grid-line {
    position: absolute;
    background: rgba(0, 212, 255, 0.08);
    
    &.horizontal {
      height: 1px;
      left: 0;
      right: 0;
      &.h1 { top: 12%; }
      &.h2 { bottom: 12%; }
    }
    
    &.vertical {
      width: 1px;
      top: 0;
      bottom: 0;
      &.v1 { left: 12%; }
      &.v2 { right: 12%; }
    }
  }
}

// 情报面板
.intel-panel {
  position: absolute;
  top: 50%;
  transform: translateY(-50%);
  width: 160px;
  background: rgba(0, 0, 0, 0.6);
  border: 1px solid rgba(0, 212, 255, 0.2);
  padding: 12px;
  font-family: monospace;
  font-size: 10px;
  z-index: 5;
  
  &.left-panel { left: 20px; }
  &.right-panel { right: 20px; }
  
  .panel-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding-bottom: 8px;
    border-bottom: 1px solid rgba(0, 212, 255, 0.2);
    margin-bottom: 8px;
    
    .panel-icon { color: #00d4ff; }
    .panel-title { color: #00d4ff; letter-spacing: 1px; }
  }
  
  .panel-content { color: #555; }
  
  .intel-item {
    display: flex;
    gap: 8px;
    margin-bottom: 6px;
    
    .intel-time { color: #333; }
    .intel-text { color: #00d4ff; opacity: 0.7; }
  }
  
  .squad-item {
    display: flex;
    align-items: center;
    gap: 8px;
    margin-bottom: 8px;
    
    .squad-name {
      color: #00d4ff;
      width: 45px;
    }
    
    .squad-bar {
      flex: 1;
      height: 4px;
      background: rgba(255, 255, 255, 0.1);
      border-radius: 2px;
      overflow: hidden;
      
      .squad-fill {
        height: 100%;
        background: linear-gradient(90deg, #00d4ff, #00ff88);
        transition: width 1s ease;
      }
    }
    
    .squad-val {
      color: #00ff88;
      width: 30px;
      text-align: right;
    }
  }
}

// 登录卡片
.login-card {
  position: relative;
  width: 420px;
  padding: 3px;
  background: linear-gradient(135deg, #00d4ff 0%, #7c3aed 50%, #f72585 100%);
  border-radius: 8px;
  animation: cardGlow 3s ease-in-out infinite;
  z-index: 10;
}

@keyframes cardGlow {
  0%, 100% { box-shadow: 0 0 30px rgba(0, 212, 255, 0.3), 0 0 60px rgba(124, 58, 237, 0.2); }
  50% { box-shadow: 0 0 50px rgba(0, 212, 255, 0.5), 0 0 80px rgba(124, 58, 237, 0.4); }
}

.card-border {
  position: absolute;
  inset: 0;
  border-radius: 8px;
  background: linear-gradient(135deg, transparent 0%, rgba(255, 255, 255, 0.1) 50%, transparent 100%);
  animation: borderShine 3s linear infinite;
}

@keyframes borderShine {
  0% { transform: translateX(-100%); }
  100% { transform: translateX(100%); }
}

// HUD 角落
.hud-corner {
  position: absolute;
  width: 16px;
  height: 16px;
  border: 2px solid rgba(0, 212, 255, 0.4);
  
  &.hud-tl { top: 6px; left: 6px; border-right: none; border-bottom: none; }
  &.hud-tr { top: 6px; right: 6px; border-left: none; border-bottom: none; }
  &.hud-bl { bottom: 6px; left: 6px; border-right: none; border-top: none; }
  &.hud-br { bottom: 6px; right: 6px; border-left: none; border-top: none; }
}

.card-content {
  background: rgba(10, 10, 20, 0.95);
  border-radius: 6px;
  padding: 36px 40px;
  backdrop-filter: blur(10px);
}

// Logo区域
.login-header {
  text-align: center;
  margin-bottom: 28px;
}

.logo-wrapper {
  position: relative;
  display: inline-block;
  margin-bottom: 14px;
}

.logo {
  width: 70px;
  height: 70px;
  position: relative;
  z-index: 1;
}

.logo-glow {
  position: absolute;
  inset: -20px;
  background: radial-gradient(circle, rgba(0, 212, 255, 0.4) 0%, transparent 70%);
  animation: logoGlow 2s ease-in-out infinite;
}

@keyframes logoGlow {
  0%, 100% { opacity: 0.5; transform: scale(0.9); }
  50% { opacity: 1; transform: scale(1.1); }
}

// 脉搏动画环
.pulse-ring {
  position: absolute;
  inset: -8px;
  border: 2px solid #00d4ff;
  border-radius: 50%;
  animation: pulseRing 2s ease-out infinite;
  
  &.delay-1 { animation-delay: 0.4s; }
  &.delay-2 { animation-delay: 0.8s; }
}

@keyframes pulseRing {
  0% { transform: scale(1); opacity: 0.8; }
  100% { transform: scale(1.5); opacity: 0; }
}

.title {
  margin: 0;
  
  .title-text {
    display: block;
    font-size: 26px;
    font-weight: 700;
    background: linear-gradient(90deg, #00d4ff, #7c3aed, #f72585);
    background-size: 200% auto;
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    animation: textGradient 3s linear infinite;
  }
  
  .title-sub {
    display: block;
    font-size: 13px;
    color: #666;
    margin-top: 4px;
    letter-spacing: 8px;
  }
}

@keyframes textGradient {
  0% { background-position: 0% center; }
  100% { background-position: 200% center; }
}

.slogan {
  color: #555;
  font-size: 11px;
  margin: 10px 0 0;
  letter-spacing: 2px;
}

// 任务状态栏
.mission-status {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 16px;
  margin-top: 14px;
  padding: 8px 16px;
  background: rgba(0, 0, 0, 0.3);
  border: 1px solid rgba(0, 212, 255, 0.15);
  border-radius: 4px;
  
  .status-item {
    display: flex;
    align-items: center;
    gap: 6px;
    
    .status-icon {
      color: #00ff88;
      font-size: 8px;
      animation: statusBlink 1s ease-in-out infinite;
    }
    
    .status-label {
      font-size: 9px;
      color: #555;
      font-family: monospace;
      letter-spacing: 1px;
    }
    
    .status-value {
      font-size: 10px;
      color: #00d4ff;
      font-family: monospace;
      font-weight: bold;
    }
  }
  
  .status-divider {
    width: 1px;
    height: 16px;
    background: rgba(255, 255, 255, 0.1);
  }
}

@keyframes statusBlink {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.3; }
}

// 心跳指示器
.heartbeat-indicator {
  display: flex;
  align-items: center;
  gap: 5px;
  
  .heart-icon {
    color: #ff4757;
    font-size: 12px;
    animation: heartBeat 1s ease-in-out infinite;
  }
  
  .bpm {
    font-size: 14px;
    font-weight: bold;
    color: #00d4ff;
    font-family: monospace;
  }
  
  .bpm-label {
    font-size: 8px;
    color: #666;
    font-family: monospace;
  }
}

@keyframes heartBeat {
  0%, 100% { transform: scale(1); }
  50% { transform: scale(1.2); }
}

// 表单样式
.login-form {
  :deep(.ant-form-item) {
    margin-bottom: 20px;
  }
  
  :deep(.ant-input-affix-wrapper) {
    background: rgba(255, 255, 255, 0.05);
    border: 1px solid rgba(0, 212, 255, 0.2);
    border-radius: 4px;
    color: #fff;
    transition: all 0.3s;
    
    &:hover, &:focus, &.ant-input-affix-wrapper-focused {
      background: rgba(255, 255, 255, 0.08);
      border-color: rgba(0, 212, 255, 0.5);
      box-shadow: 0 0 15px rgba(0, 212, 255, 0.15);
    }
  }
  
  :deep(.ant-input) {
    background: transparent;
    color: #fff;
    &::placeholder { color: #444; }
  }
  
  :deep(.ant-input-password-icon) { color: #444; }
}

.input-wrapper {
  position: relative;
}

.input-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 4px;
  
  .input-code {
    font-size: 8px;
    color: #ff4757;
    font-family: monospace;
    letter-spacing: 1px;
  }
  
  .input-label {
    font-size: 9px;
    color: #00d4ff;
    letter-spacing: 2px;
    font-family: monospace;
  }
}

.input-icon {
  color: #00d4ff;
  font-size: 14px;
}

// 登录按钮
.login-btn {
  height: 44px;
  background: linear-gradient(90deg, #00d4ff, #7c3aed);
  border: none;
  border-radius: 4px;
  font-size: 14px;
  font-weight: 600;
  letter-spacing: 3px;
  position: relative;
  overflow: hidden;
  transition: all 0.3s;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  
  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 8px 25px rgba(0, 212, 255, 0.4);
  }
  
  .btn-icon { font-size: 12px; }
  .btn-text { position: relative; z-index: 1; }
  .btn-arrow { font-size: 14px; }
  
  .btn-glitch {
    position: absolute;
    inset: 0;
    background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.2), transparent);
    transform: translateX(-100%);
    animation: btnShine 2s ease-in-out infinite;
  }
}

@keyframes btnShine {
  0% { transform: translateX(-100%); }
  50%, 100% { transform: translateX(100%); }
}

// 底部统计
.footer {
  margin-top: 20px;
  padding-top: 16px;
  border-top: 1px solid rgba(255, 255, 255, 0.05);
  position: relative;
}

.scan-line {
  position: absolute;
  left: 0;
  right: 0;
  top: 0;
  height: 1px;
  background: linear-gradient(90deg, transparent, #00d4ff, transparent);
  animation: scan 2s linear infinite;
}

@keyframes scan {
  0% { transform: translateX(-100%); }
  100% { transform: translateX(100%); }
}

.stats-row {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 16px;
}

.stat-item {
  text-align: center;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 2px;
  
  .stat-icon { color: #00d4ff; font-size: 10px; }
  .stat-value { font-size: 16px; font-weight: bold; color: #00d4ff; font-family: monospace; }
  .stat-label { font-size: 8px; color: #444; letter-spacing: 1px; }
}

.stat-divider {
  width: 1px;
  height: 28px;
  background: rgba(255, 255, 255, 0.1);
}

// 战术信息
.tactical-info {
  margin-top: 12px;
  text-align: center;
  font-family: monospace;
  font-size: 8px;
  color: #333;
  letter-spacing: 1px;
  
  .tac-line {
    display: block;
    margin-top: 2px;
  }
}

// 角落装饰
.corner-decor {
  position: absolute;
  display: flex;
  align-items: center;
  gap: 8px;
  font-family: monospace;
  font-size: 9px;
  color: #333;
  letter-spacing: 1px;
  
  &.top-left { top: 16px; left: 16px; }
  &.top-right { top: 16px; right: 16px; }
  &.bottom-left { bottom: 16px; left: 16px; }
  &.bottom-right { bottom: 16px; right: 16px; }
  
  .corner-line {
    width: 24px;
    height: 1px;
    background: rgba(255, 71, 87, 0.3);
  }
}

// 注册链接
.register-link {
  text-align: center;
  margin-top: 12px;
  
  a {
    display: inline-flex;
    align-items: center;
    gap: 8px;
    color: #ff4757;
    font-size: 13px;
    cursor: pointer;
    transition: all 0.3s;
    letter-spacing: 3px;
    font-weight: 500;
    
    .link-crosshair {
      font-size: 10px;
      animation: crosshairPulse 1.5s ease-in-out infinite;
    }
    
    &:hover {
      color: #00ff88;
      text-shadow: 0 0 15px rgba(0, 255, 136, 0.6);
      
      .link-crosshair {
        animation: crosshairSpin 0.5s ease;
      }
    }
  }
}

@keyframes crosshairPulse {
  0%, 100% { opacity: 1; transform: scale(1); }
  50% { opacity: 0.6; transform: scale(1.2); }
}

@keyframes crosshairSpin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(180deg); }
}

// 注册弹窗
.register-modal {
  :deep(.ant-modal-content) {
    background: rgba(10, 10, 20, 0.98);
    border: 1px solid rgba(0, 212, 255, 0.3);
    border-radius: 8px;
    box-shadow: 0 0 40px rgba(0, 212, 255, 0.2);
  }
  
  :deep(.ant-modal-close) {
    color: #666;
    top: 12px;
    
    &:hover { color: #00d4ff; }
  }
}

.register-content {
  padding: 24px;
  text-align: center;
}

// 管理员信息卡片
.admin-card {
  display: flex;
  align-items: center;
  gap: 16px;
  background: rgba(255, 71, 87, 0.08);
  border: 1px solid rgba(255, 71, 87, 0.3);
  border-radius: 8px;
  padding: 16px 20px;
  margin-bottom: 20px;
  position: relative;
  
  &::before {
    content: '';
    position: absolute;
    top: -1px;
    left: 20px;
    right: 20px;
    height: 2px;
    background: linear-gradient(90deg, transparent, #ff4757, transparent);
  }
}

.admin-avatar {
  position: relative;
  width: 64px;
  height: 64px;
  border-radius: 50%;
  overflow: hidden;
  border: 2px solid #ff4757;
  box-shadow: 0 0 20px rgba(255, 71, 87, 0.4);
  flex-shrink: 0;
  
  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }
  
  .avatar-scope {
    position: absolute;
    inset: -8px;
    border: 1px solid rgba(255, 71, 87, 0.4);
    border-radius: 50%;
    animation: scopePulse 2s ease-in-out infinite;
    
    &::before {
      content: '+';
      position: absolute;
      top: 50%;
      left: 50%;
      transform: translate(-50%, -50%);
      font-size: 16px;
      color: rgba(255, 71, 87, 0.6);
    }
  }
}

@keyframes scopePulse {
  0%, 100% { opacity: 0.3; transform: scale(1); }
  50% { opacity: 0.8; transform: scale(1.1); }
}

.admin-info {
  text-align: left;
  flex: 1;
  
  .admin-name-wrapper {
    display: flex;
    align-items: center;
    gap: 8px;
    margin-bottom: 8px;
    
    .name-crosshair {
      color: #ff4757;
      font-size: 10px;
      opacity: 0.6;
    }
  }
  
  .admin-name {
    color: #ff4757;
    font-size: 20px;
    font-weight: 700;
    margin: 0;
    letter-spacing: 4px;
    text-shadow: 0 0 10px rgba(255, 71, 87, 0.5);
    font-family: 'Courier New', monospace;
  }
  
  .admin-tags {
    display: flex;
    gap: 8px;
  }
  
  .admin-tag {
    display: inline-block;
    background: rgba(255, 71, 87, 0.2);
    border: 1px solid rgba(255, 71, 87, 0.4);
    color: #ff4757;
    font-size: 9px;
    padding: 2px 8px;
    border-radius: 2px;
    letter-spacing: 1px;
    font-family: monospace;
    
    &.tag-rank {
      background: rgba(0, 255, 136, 0.2);
      border-color: rgba(0, 255, 136, 0.4);
      color: #00ff88;
    }
  }
}

.register-title {
  color: #ff4757;
  font-size: 16px;
  margin-bottom: 20px;
  letter-spacing: 3px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  font-family: monospace;
  
  .title-icon {
    font-size: 10px;
    opacity: 0.6;
  }
}

.contact-info {
  background: rgba(0, 212, 255, 0.05);
  border: 1px solid rgba(0, 212, 255, 0.15);
  border-radius: 6px;
  padding: 16px;
  margin-bottom: 16px;
}

.contact-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 0;
  
  &:not(:last-child) {
    border-bottom: 1px solid rgba(255, 255, 255, 0.05);
  }
  
  .contact-icon {
    font-size: 20px;
    color: #00d4ff;
    
    &.wechat { color: #07c160; }
  }
  
  .contact-label {
    color: #666;
    font-size: 12px;
    width: 40px;
  }
  
  .contact-value {
    color: #fff;
    font-size: 16px;
    font-family: monospace;
    letter-spacing: 1px;
    
    &.phone-highlight {
      color: #00ff88;
      font-weight: bold;
      text-shadow: 0 0 10px rgba(0, 255, 136, 0.4);
    }
    
    &.wechat-highlight {
      color: #07c160;
      font-weight: bold;
      text-shadow: 0 0 10px rgba(7, 193, 96, 0.4);
    }
  }
}

.register-tip {
  color: #ff4757;
  font-size: 10px;
  margin: 0;
  letter-spacing: 2px;
  font-family: monospace;
  opacity: 0.7;
}
</style>
