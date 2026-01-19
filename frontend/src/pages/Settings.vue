<template>
  <div class="settings">
    <div class="topbar">
      <h2>{{ $t('settingsTitle') }}</h2>
      <div class="muted">{{ $t('settingsSubtitle') }}</div>
    </div>

    <div class="content">
      <!-- 第一行：API 配置（全宽） -->
      <div class="row row-first">
        <div class="card">
          <h4>{{ $t('apiConfig') }}</h4>
          <div class="api-config">
            <label>API Endpoint:</label>
            <input v-model="apiEndpoint" :placeholder="apiEndpoint" />
            <button @click="saveApiConfig" class="save-btn">{{ $t('saveBtn') }}</button>
          </div>
          <div class="muted" style="margin-top: 8px;">{{ $t('currentApi', { api: apiEndpoint }) }}</div>
        </div>
      </div>

      <!-- 第二行：服务状态 与 系统配置 并排 -->
      <div class="row row-second">
        <div class="card service-status-card">
          <h4>{{ $t('serviceStatus') }}</h4>
          <div class="status-info">
                  <div class="status-item">
                    <span class="status-label">{{ $t('backendStatusLabel') }}:</span>
                    <span :class="['status-value', backendStatusClass]">{{ backendStatus }}</span>
                  </div>
                  <div class="status-item">
                    <span class="status-label">{{ $t('apiKeyLabel') }}:</span>
                    <span :class="['status-value', apiKeyStatusClass]">{{ apiKeyStatus }}</span>
                  </div>
                  <div class="status-item">
                    <span class="status-label">{{ $t('wsConfigLabel') }}:</span>
                    <span :class="['status-value', wsConfigStatusClass]">{{ wsConfigStatus }}</span>
                  </div>
          </div>
          <button @click="checkStatus" class="check-status-btn">{{ $t('checkStatusBtn') }}</button>
        </div>

        <div class="card system-config-card">
          <h4>系统配置</h4>
          <div class="system-config">
            <div class="config-item">
              <label>
                <input type="checkbox" v-model="autoSave" /> 自动保存
              </label>
            </div>
            <div class="config-item">
              <label>
                <input type="checkbox" v-model="notifications" /> 启用通知
              </label>
            </div>
            <div class="config-item">
              <label>主题: 
                <select v-model="theme">
                  <option value="light">浅色</option>
                  <option value="dark">深色</option>
                </select>
              </label>
            </div>
          </div>
          <button @click="saveSystemConfig" class="save-btn">保存配置</button>
        </div>
      </div>

      <!-- 第三行：关于（全宽） -->
      <div class="row row-third">
        <div class="card about-card">
          <h4>关于</h4>
          <div class="about-info">
            <p>AI Excel v1.0</p>
            <p>企业级AI驱动的Excel数据处理与分析平台</p>
            <p>© 2026 AI Excel Team</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { checkServiceStatus, getSystemSettings, saveSystemSettings } from '@/services/api'

const { t } = useI18n()

// API配置：默认使用页面 origin
const apiEndpoint = ref((typeof window !== 'undefined' && window.location && window.location.origin) ? window.location.origin : 'http://localhost:8081')

// 服务状态
const backendStatus = ref(t('unknown'))
const apiKeyStatus = ref(t('unknown'))
const wsConfigStatus = ref(t('unknown'))

// 系统配置
const autoSave = ref(true)
const notifications = ref(true)
const theme = ref('dark')

// 计算属性
const backendStatusClass = computed(() => {
  if (backendStatus.value === t('checking')) return 'checking'
  if (backendStatus.value === t('unableConnect')) return 'error'
  return 'success'
})

const apiKeyStatusClass = computed(() => {
  if (apiKeyStatus.value === t('checking')) return 'checking'
  if (apiKeyStatus.value === t('unableConnect')) return 'error'
  return 'success'
})

const wsConfigStatusClass = computed(() => {
  if (wsConfigStatus.value === t('checking')) return 'checking'
  if (wsConfigStatus.value === t('unableConnect')) return 'error'
  return 'success'
})

// 方法
async function saveApiConfig() {
  // 将 API Endpoint 保存到后端系统设置（仅示例字段）
  const settings = {
    apiEndpoint: apiEndpoint.value
  }

  try {
    const result = await saveSystemSettings(settings)
    if (result && result.success) {
      alert(t('savedApi', { api: apiEndpoint.value }))
    } else {
      alert(t('saveFailed'))
    }
  } catch (e) {
    console.error('保存 API 配置失败:', e)
    alert(t('saveFailed'))
  }
}

async function checkStatus() {
  backendStatus.value = t('checking')
  apiKeyStatus.value = t('checking')
  wsConfigStatus.value = t('checking')

  try {
    const result = await checkServiceStatus()

    if (result && typeof result === 'object') {
      backendStatus.value = result.status === 'available' ? t('status_active') : t('status_inactive')
      apiKeyStatus.value = result.hasApiKey ? t('api_active') : t('api_inactive')
      wsConfigStatus.value = result.apiConfigured ? t('enabled') : t('disabled')
    } else {
      backendStatus.value = t('unknown')
      apiKeyStatus.value = t('unknown')
      wsConfigStatus.value = t('unknown')
    }
  } catch (error) {
    console.error('检查服务状态失败:', error)
    backendStatus.value = t('unableConnect')
    apiKeyStatus.value = t('unableConnect')
    wsConfigStatus.value = t('unableConnect')
  }
}

async function saveSystemConfig() {
  const settings = {
    apiEndpoint: apiEndpoint.value,
    autoSave: autoSave.value,
    notifications: notifications.value,
    theme: theme.value
  }

  try {
    const result = await saveSystemSettings(settings)
    if (result.success) {
      alert('系统配置保存成功')
    }
  } catch (error) {
    console.error('保存配置失败:', error)
    alert('保存配置失败')
  }
}

// 初始化
onMounted(async () => {
  try {
    const result = await getSystemSettings()
    if (result.success) {
      const data = result.data
      apiEndpoint.value = data.apiEndpoint || apiEndpoint.value
      autoSave.value = data.autoSave ?? true
      notifications.value = data.notifications ?? true
      theme.value = data.theme || 'dark'
    }
  } catch (error) {
    console.error('获取系统设置失败:', error)
  }
})
</script>

<style scoped>
.settings {
  display: flex;
  flex-direction: column;
  height: 100%;
}

/* Ensure this page stacks rows vertically and layout second row as two columns */
.settings > .content {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.row-second {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}
.service-status-card { min-height: 220px; }
.system-config-card { min-height: 220px; }
.about-card { min-height: 120px; }

@media (max-width: 900px) {
  .row-second { grid-template-columns: 1fr; }
}

.api-config,
.status-info,
.system-config {
  margin-top: 16px;
}

.api-config input {
  width: 100%;
  padding: 8px;
  border-radius: 6px;
  border: 1px solid rgba(255, 255, 255, 0.06);
  background: transparent;
  color: inherit;
  margin-top: 8px;
  margin-bottom: 8px;
}

.status-item {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
  padding-bottom: 8px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.02);
}

.status-label {
  color: rgba(230, 238, 248, 0.8);
}

.status-value {
  font-weight: 500;
}

.status-value.success {
  color: #10b981;
}

.status-value.error {
  color: #ef4444;
}

.status-value.checking {
  color: #f59e0b;
}

.check-status-btn,
.save-btn {
  background: #7c3aed;
  color: white;
  border: 0;
  padding: 8px 16px;
  border-radius: 6px;
  cursor: pointer;
  margin-top: 12px;
}

.config-item {
  margin-bottom: 12px;
}

.about-info p {
  margin: 6px 0;
  color: rgba(230, 238, 248, 0.8);
}
</style>