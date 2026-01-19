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
            <DataTable :data="tableData" @updateCell="onUpdateCell" @updateData="onUpdateData" />
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

const fileName = ref('')
const tableData = ref<Array<string[]>>([])
const aiMessages = ref<Array<{ id: number; role: string; text: string; placeholder?: boolean }>>([])
const aiActive = ref(false)
// history of past chat interactions
const chatHistory = ref<Array<{ id: number, command: string, timestamp: number, messages: string[], ignored?: string }>>([])
const { t } = useI18n()
const aiInput = ref<any>(null)
const activeTab = ref('overview')

function onFileLoaded(payload: { name: string; data: string[][] }) {
  fileName.value = payload.name
  tableData.value = payload.data
  aiMessages.value = []
  aiActive.value = false
  saveToStorage()
}

function onRunCommand(command: string) {
  // activate AI chat view
  aiActive.value = true
  // save a snapshot of current messages to history (do not reset aiMessages)
  chatHistory.value.push({ id: Date.now(), command, timestamp: Date.now(), messages: aiMessages.value.map(m => m.text) })

  // push the user message
  aiMessages.value.push({ id: Date.now(), role: 'user', text: command })

  // insert a placeholder AI bubble
  const placeholderId = Date.now() + Math.floor(Math.random() * 1000)
  aiMessages.value.push({ id: placeholderId, role: 'ai', text: '', placeholder: true })

  // after 3s replace placeholder with AI response
  setTimeout(() => {
    const resp = t('ai_example_resp')
    // find placeholder index
    const idx = aiMessages.value.findIndex(m => m.id === placeholderId)
    if (idx !== -1) {
      aiMessages.value.splice(idx, 1, { id: Date.now(), role: 'ai', text: resp })
    } else {
      aiMessages.value.push({ id: Date.now(), role: 'ai', text: resp })
    }
    // optionally append steps message
    const stepsMsg = `${t('steps_title')}：\n1. ${t('placeholder_step1')}\n2. ${t('placeholder_step2')}`
    aiMessages.value.push({ id: Date.now() + 2, role: 'ai', text: stepsMsg })
  }, 3000)
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
      const expr = m2[2]
      const targetCol = colLetterToIndex(cellRef.replace(/\d+/, ''))
      const targetRow = Number(cellRef.replace(/[^0-9]/g, ''))
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
