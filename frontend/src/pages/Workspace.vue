<template>
  <div style="padding:16px">
    <!-- workspace grid: left column contains topbar(row1) + preview(row2); right column is sidebar -->
    <div class="workspace-grid">
      <div class="topbar">
            <div class="muted">{{ $t('currentFile') }} {{ fileName || $t('noFile') }}</div>
            <div style="margin-left:auto; display:flex; gap:8px; align-items:center;">
              <button class="new-chat-btn" @click="newChat" :title="t('newChat')" :aria-label="t('newChat')">
                <span class="new-chat-circle" aria-hidden="true">
                  <svg class="new-chat-plus" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg" role="img"
                    aria-hidden="true">
                    <line x1="12" y1="5" x2="12" y2="19" stroke="#8505a8" stroke-width="2" stroke-linecap="round" />
                    <line x1="5" y1="12" x2="19" y2="12" stroke="#8505a8" stroke-width="2" stroke-linecap="round" />
                  </svg>
                </span>
                <span class="new-chat-label">{{ t('newChat') }}</span>
              </button>
              
            </div>
            <!-- Error banner with retry -->
            <div v-if="lastError" style="margin-top:8px; display:flex; gap:8px; align-items:center;">
              <div style="color:#ffcccc; background:rgba(255,0,0,0.06); padding:8px; border-radius:6px; flex:1">{{ lastError }}</div>
              <button class="btn" @click="retryLastCommand" v-if="failedCommand">{{ t('retry') || '重试' }}</button>
            </div>
          </div>
      <section class="preview card" style="flex:1; min-width:0; max-height:calc((100vh - 200px)); overflow:auto;">
        <div
          style="display:flex; align-items:center; gap:8px; padding:12px 16px; border-bottom:1px solid rgba(255,255,255,0.03)">
          <div style="display:flex; gap:8px">
            <button :class="['tab-btn', activeTab === 'overview' ? 'active' : '']" @click="activeTab = 'overview'">{{
              $t('dataPreview') }}</button>
            <button :class="['tab-btn', activeTab === 'analysis' ? 'active' : '']" @click="activeTab = 'analysis'">{{
              $t('analysis') }}</button>
            <button :class="['tab-btn', activeTab === 'templates' ? 'active' : '']" @click="activeTab = 'templates'">{{
              $t('templates') }}</button>
          </div>
          <div style="margin-left:auto; font-size:13px" class="muted">{{ $t('currentFile') }} {{ fileName ||
            $t('noFile') }}</div>
        </div>

        <div style="padding:16px">
          <div v-show="activeTab === 'overview'">
            <h4 style="margin-top:0">{{ $t('dataPreview') }}</h4>
            <DataTable :data="tableData" @updateCell="onUpdateCell" @updateData="onUpdateData" :showExport="true" @export="onExport" />
          </div>

          <div v-show="activeTab === 'analysis'">
            <Analysis />
          </div>

          <div v-show="activeTab === 'templates'">
            <Templates />
          </div>
        </div>
      </section>
      <aside class="right" style="width:420px;height:840px; display:flex; flex-direction:column; gap:12px;">
        <div class="card" style="display:flex; flex-direction:column; gap:12px;">
          <!-- Chat box: uploader (hidden after run), AI stream + steps (shown after run), and command input -->
          <div class="chat-box" style="display:flex; flex-direction:column; gap:8px;">
            <!-- Uploader: hidden when aiActive -->
            <div v-if="!aiActive">
              <FileUploader @fileLoaded="onFileLoaded" />
            </div>

            <!-- AI Stream and Steps: shown when aiActive -->
            <div v-if="aiActive" style="display:flex; flex-direction:column; gap:8px;">
              <div class="ai-stream-container">
                <ChatBubbleList :messages="aiMessages" />
              </div>
              <!-- steps are included inside aiMessages as a single card message -->
            </div>

            <!-- Command input removed from chat box; moved to floating input at bottom-right -->
          </div>
        </div>
        <!-- Floating AI command input (aligned inside right sidebar) -->
        <div class="floating-ai-input">
          <ChatInput ref="aiInput" @send="onRunCommand" />
        </div>
      </aside>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import FileUploader from '../components/FileUploader.vue'
import DataTable from '../components/DataTable.vue'
import ChatBubbleList from '../components/ChatBubbleList.vue'
import ChatInput from '../components/ChatInput.vue'
import Analysis from './Analysis.vue'
import Templates from './Templates.vue'
import { getExcelDataPreview, processExcelWithAI, processExcelAndChat, uploadExcel, processExcelWithAIDownload, chat, runAllApis } from '../services/aiService'

const fileName = ref('')
const tableData = ref<Array<string[]>>([])
const aiMessages = ref<Array<{ id: number; role: string; text: string; placeholder?: boolean }>>([])
const aiActive = ref(false)
const lastFile = ref<File | null>(null)
const lastCommand = ref<string>('')
const lastError = ref<string | null>(null)
const failedCommand = ref<string | null>(null)
// history of past chat interactions
const chatHistory = ref<Array<{ id: number, command: string, timestamp: number, messages: string[], ignored?: string }>>([])
const { t } = useI18n()
const aiInput = ref<any>(null)
const activeTab = ref('overview')

async function onFileLoaded(payload: { name: string; data: string[][]; file?: File }) {
  fileName.value = payload.name
  tableData.value = payload.data
  aiMessages.value = []
  aiActive.value = false
  // store raw file if available for backend operations
  if (payload.file) {
    lastFile.value = payload.file
    // try server-side preview; fall back to client-parsed data on error
    try {
      const resp = await getExcelDataPreview(payload.file)
      // server returns parsed data under `data` or `excelDataPreview`; accept both
      if (resp && resp.data) tableData.value = resp.data as string[][]
      else if (resp && resp.excelDataPreview) tableData.value = resp.excelDataPreview as string[][]
      else tableData.value = payload.data
      } catch (e) {
      // ignore and keep client preview
      tableData.value = payload.data
    }
    // NOTE: 不在文件加载时自动触发所有 AI 接口（避免误调用如 suggest-charts）。
    // 如果需要针对性测试，请在 Settings 中启用或手动调用 runAllApis。
  } else {
    lastFile.value = null
  }
  saveToStorage()
}

async function onRunCommand(command: string) {
  // activate AI chat view
  aiActive.value = true
  lastError.value = null
  failedCommand.value = null
  lastCommand.value = command
  // save a snapshot of current messages to history (do not reset aiMessages)
  chatHistory.value.push({ id: Date.now(), command, timestamp: Date.now(), messages: aiMessages.value.map(m => m.text) })

  // push the user message
  aiMessages.value.push({ id: Date.now(), role: 'user', text: command })

  // If we have a file uploaded, call backend; otherwise fall back to local simulation
  if (lastFile.value) {
    const placeholderId = Date.now() + Math.floor(Math.random() * 1000)
    aiMessages.value.push({ id: placeholderId, role: 'ai', text: '', placeholder: true })
    try {
      const { preview, aiResp } = await processExcelAndChat(lastFile.value, command)
      // If preview contains parsed array data, replace the tableData shown in the UI
      try {
        if (preview) {
          if (Array.isArray(preview.data)) {
            tableData.value = preview.data as string[][]
          } else if (preview.excelDataPreview && typeof preview.excelDataPreview === 'string') {
            // try parse JSON-encoded preview, otherwise leave as-is
            try {
              const maybe = JSON.parse(preview.excelDataPreview)
              if (Array.isArray(maybe)) tableData.value = maybe as string[][]
            } catch (e) {
              // not JSON, ignore
            }
          }
        }
      } catch (e) { /* ignore table update errors */ }

      // Use aiResp.aiResponse if available (contains command sequence or text), fallback to message/result
      const aiText = (aiResp && (aiResp.aiResponse || aiResp.message || aiResp.result || JSON.stringify(aiResp))) as string
      const idx = aiMessages.value.findIndex(m => m.id === placeholderId)
      if (idx !== -1) aiMessages.value.splice(idx, 1, { id: Date.now(), role: 'ai', text: aiText })
      else aiMessages.value.push({ id: Date.now(), role: 'ai', text: aiText })
    } catch (err:any) {
      const idx = aiMessages.value.findIndex(m => m.id === placeholderId)
      const msg = (err && (err.body?.error || err.message)) || '服务端错误'
      // if preview was attached to error, update table and show a short note
      if (err && err.preview) {
        try {
          const p = err.preview
          if (Array.isArray(p.data)) tableData.value = p.data as string[][]
          else if (p.excelDataPreview && typeof p.excelDataPreview === 'string') {
            try { const maybe = JSON.parse(p.excelDataPreview); if (Array.isArray(maybe)) tableData.value = maybe as string[][] } catch (e) {}
          }
        } catch (e) {}
      }
      lastError.value = msg
      failedCommand.value = command
      if (idx !== -1) aiMessages.value.splice(idx, 1, { id: Date.now(), role: 'ai', text: msg })
      else aiMessages.value.push({ id: Date.now(), role: 'ai', text: msg })
    }
  } else {
    // No uploaded file: call backend chat API
    const placeholderId = Date.now() + Math.floor(Math.random() * 1000)
    aiMessages.value.push({ id: placeholderId, role: 'ai', text: '', placeholder: true })
    try {
      const resp = await chat(command)
      const text = (resp && (resp.message || resp.result || JSON.stringify(resp))) as string
      const idx = aiMessages.value.findIndex(m => m.id === placeholderId)
      if (idx !== -1) aiMessages.value.splice(idx, 1, { id: Date.now(), role: 'ai', text })
      else aiMessages.value.push({ id: Date.now(), role: 'ai', text })
    } catch (err:any) {
      const idx = aiMessages.value.findIndex(m => m.id === placeholderId)
      const msg = (err && (err.body?.error || err.message)) || '服务端错误'
      lastError.value = msg
      failedCommand.value = command
      if (idx !== -1) aiMessages.value.splice(idx, 1, { id: Date.now(), role: 'ai', text: msg })
      else aiMessages.value.push({ id: Date.now(), role: 'ai', text: msg })
    }
  }
}

async function onExport() {
  if (!lastFile.value || !lastCommand.value) {
    alert('需要已上传的文件和最近的命令才能导出')
    return
  }
  try {
    const blob = await processExcelWithAIDownload(lastFile.value, lastCommand.value)
    const url = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    const name = fileName.value ? `modified_${fileName.value}` : 'modified.xlsx'
    a.download = name
    document.body.appendChild(a)
    a.click()
    a.remove()
    URL.revokeObjectURL(url)
  } catch (e:any) {
    const msg = (e && (e.body?.error || e.message)) || '导出失败'
    lastError.value = msg
    failedCommand.value = lastCommand.value
    alert(msg)
  }
}

function retryLastCommand(){
  if (failedCommand.value) onRunCommand(failedCommand.value)
}

function newChat() {
  // start a fresh chat: clear messages and deactivate AI view
  aiActive.value = false
  aiMessages.value = []
  // clear loaded excel file and table data, persist change
  fileName.value = ''
  tableData.value = []
  saveToStorage()
  // record this new chat in history as an empty session marker
  chatHistory.value.push({ id: Date.now(), command: '', timestamp: Date.now(), messages: [] })
}

function onEditCommand(m: string) {
  // open AI view and populate the floating input for editing
  aiActive.value = true
  // set the input text to the command/message (strip UI labels if needed)
  const content = String(m || '')
  // try to set via exposed method
  if (aiInput.value && typeof aiInput.value.setText === 'function') {
    aiInput.value.setText(content)
  }
}

function onIgnoreCommand(m: any) {
  // remove message by text or id
  let idx = -1
  if (typeof m === 'number') idx = aiMessages.value.findIndex(x => x.id === m)
  else if (typeof m === 'string') idx = aiMessages.value.findIndex(x => x.text === m)
  else if (m && typeof m.id === 'number') idx = aiMessages.value.findIndex(x => x.id === m.id)
  if (idx !== -1) aiMessages.value.splice(idx, 1)
  // record ignored message in history
  chatHistory.value.push({ id: Date.now(), command: '', timestamp: Date.now(), messages: [], ignored: String(m && (m.id || m) || m) })
}

function onUpdateCell({ r, c, value }: { r: number, c: number, value: string }) {
  if (!tableData.value[r]) return
  tableData.value[r][c] = value
  saveToStorage()
}

function onUpdateData(newData: string[][]) { tableData.value = newData; saveToStorage() }

function saveToStorage() {
  try {
    const payload = { name: fileName.value, data: tableData.value }
    localStorage.setItem('aiexcel_workspace_file', JSON.stringify(payload))
  } catch (e) { }
}

function loadFromStorage() {
  try {
    const s = localStorage.getItem('aiexcel_workspace_file')
    if (!s) return
    const p = JSON.parse(s)
    if (p && Array.isArray(p.data)) {
      fileName.value = p.name || ''
      tableData.value = p.data || []
    }
  } catch (e) { }
}

onMounted(() => loadFromStorage())

function colLetterToIndex(letters: string) { let n = 0; for (let i = 0; i < letters.length; i++) { n = n * 26 + (letters.charCodeAt(i) - 64) } return n - 1 }
function evalFormula(expr: string, grid: string[][]) {
  const replaced = expr.replace(/([A-Z]+\d+)/g, (m) => {
    const col = colLetterToIndex(m.replace(/\d+/, ''))
    const row = Number(m.replace(/[^0-9]/g, ''))
    const v = (grid[row - 1] && grid[row - 1][col]) || '0'
    return String(Number(v) || 0)
  })
  try { // @ts-ignore
    return eval(replaced.replace(/[＝，]/g, ''))
  } catch (e) { return '' }
}

function applyCommands(raw: string) {
  const lines = raw.split('\n').map(l => l.trim()).filter(Boolean)
  for (const ln of lines) {
    const m = ln.match(/\[INSERT_COLUMN:(\d+):([^\]]+)\]/i)
    if (m) {
      const idx = Number(m[1])
      const name = m[2]
      if (!tableData.value[0]) tableData.value[0] = []
      const colIndex = Math.min(idx, tableData.value[0].length)
      for (let r = 0; r < tableData.value.length; r++) {
        const row = tableData.value[r]
        if (r === 0) row.splice(colIndex, 0, name)
        else row.splice(colIndex, 0, '')
      }
      continue
    }
    const m2 = ln.match(/\[APPLY_FORMULA:([A-Z]+\d+):=(.+)\]/i)
    if (m2) {
      const cellRef = m2[1]
      const expr = m2[2].trim()
      const targetCol = colLetterToIndex(cellRef.replace(/\d+/, ''))
      const targetRow = Number(cellRef.replace(/[^0-9]/g, ''))

      // Ensure table has enough rows
      while (tableData.value.length < targetRow) {
        // create empty row with same number of columns as header (or 0)
        const cols = tableData.value[0] ? tableData.value[0].length : 0
        const newRow = Array.from({ length: cols }, () => '')
        tableData.value.push(newRow)
      }
      // Ensure each row has enough columns
      for (let r = 0; r < tableData.value.length; r++) {
        while ((tableData.value[r] || []).length <= targetCol) {
          if (!tableData.value[r]) tableData.value[r] = []
          tableData.value[r].push('')
        }
      }

      // Support SUM(range) like SUM(B2:B15)
      const sumMatch = expr.match(/^SUM\(([A-Z]+\d+):([A-Z]+\d+)\)$/i)
      if (sumMatch) {
        const startRef = sumMatch[1]
        const endRef = sumMatch[2]
        const startCol = colLetterToIndex(startRef.replace(/\d+/, ''))
        const startRow = Number(startRef.replace(/[^0-9]/g, ''))
        const endCol = colLetterToIndex(endRef.replace(/\d+/, ''))
        const endRow = Number(endRef.replace(/[^0-9]/g, ''))

        let total = 0
        for (let r = Math.min(startRow, endRow); r <= Math.max(startRow, endRow); r++) {
          for (let c = Math.min(startCol, endCol); c <= Math.max(startCol, endCol); c++) {
            const row = tableData.value[r - 1]
            if (!row) continue
            const cell = row[c]
            const num = Number(String(cell || '').replace(/[^0-9.\-]/g, ''))
            if (!isNaN(num)) total += num
          }
        }
        tableData.value[targetRow - 1][targetCol] = String(total)
        continue
      }

      // Fallback: evaluate simple expressions by replacing cell refs with numeric values
      const value = evalFormula(expr, tableData.value)
      if (tableData.value[targetRow - 1]) tableData.value[targetRow - 1][targetCol] = String(value)
      continue
    }
  }
  saveToStorage()
}

function onApplyCommands(m: string) { applyCommands(m) }
</script>

<style scoped>
/* grid to ensure topbar and preview share the same main column width */
.workspace-grid {
  display: grid;
  grid-template-columns: 1fr 420px;
  grid-template-rows: auto 1fr;
  gap: 12px;
  align-items: start;
}

.workspace-grid>.topbar {
  grid-column: 1 / 2;
  grid-row: 1;
}

.workspace-grid>.preview {
  grid-column: 1 / 2;
  grid-row: 2;
}

.workspace-grid>.right {
  grid-column: 2 / 3;
  grid-row: 1 / 3;
}

.tab-btn {
  background: transparent;
  border: 1px solid transparent;
  padding: 6px 12px;
  border-radius: 6px;
  cursor: pointer;
  color: #cfcfd6;
}

.tab-btn.active {
  background: #8505a8;
  /* workspace accent color */
  border-color: transparent;
  color: #fff;
}

.workspace-tabs {
  border-bottom: 1px solid #eee
}

.tab-btn:hover {
  filter: brightness(1.05);
}
</style>
