<template>
  <div>
    <label class="muted">{{ $t('uploaderLabel') }}</label>

    <div class="card" style="padding:12px">
      <div
        :class="['file-upload-area', { dragover: isDragOver }]"
        id="fileDropArea"
        @drop.prevent="onDrop"
        @dragenter.prevent="onDragEnter"
        @dragover.prevent="onDragOver"
        @dragleave.prevent="onDragLeave"
        style="padding:18px; text-align:center; cursor:copy"
      >
        <svg class="cloud-icon" width="48" height="48" viewBox="0 0 16 16" fill="none" xmlns="http://www.w3.org/2000/svg">
          <path d="M4.406 3.342A4.002 4.002 0 0 1 11.5 4.5a3.5 3.5 0 0 1 .5 6.992v.008H4.5a3 3 0 0 1-.094-5.666 4.002 4.002 0 0 1 0-2.492z" fill="currentColor" opacity="0.12"/>
          <path d="M8 5.5v4M6 7.5l2-2 2 2" stroke="currentColor" stroke-width="1.2" stroke-linecap="round" stroke-linejoin="round"/>
        </svg>
        <p class="text-muted" style="margin-top:8px">{{ $t('uploaderHint') || '拖拽文件到此处或点击下方按钮上传' }}</p>

        <input
          type="file"
          id="fileInput"
          ref="fileInput"
          class="d-none"
          accept=".xlsx,.xls,.csv"
          @change="onChange"
        />

        <button class="btn btn-primary" id="browseFileBtn" @click="openBrowser">
          <i class="bi bi-folder2-open me-2"></i>{{ $t('chooseFile') || '选择文件' }}
        </button>

        <div v-if="selectedName" style="margin-top:8px" class="muted">{{ $t('currentFile') }} {{ selectedName }}</div>
      </div>

      <div v-if="progress>0" style="margin-top:8px">
        <div class="muted">{{ $t('parsingProgress', { pct: Math.round(progress*100) }) }}</div>
        <div style="background:rgba(255,255,255,0.04); height:6px; border-radius:4px; overflow:hidden; margin-top:6px">
          <div :style="{ width: (progress*100)+'%', background: 'linear-gradient(90deg,#7c3aed,#38bdf8)', height:'6px' }"></div>
        </div>
      </div>

      <div style="margin-top:8px; display:flex; gap:8px; align-items:center">
        <button @click="downloadSample">{{ $t('uploaderDownloadSample') }}</button>
        <div class="muted">{{ $t('uploaderSupport', { size: 5 }) }}</div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useI18n } from 'vue-i18n'
const { t } = useI18n()
import * as XLSX from 'xlsx'
const emit = defineEmits(['fileLoaded'])

const progress = ref(0)
const selectedName = ref('')
const fileInput = ref<HTMLInputElement | null>(null)
const isDragOver = ref(false)

function openBrowser(){
  fileInput.value && fileInput.value.click()
}

function onDragEnter(e: DragEvent){
  isDragOver.value = true
  if (e.dataTransfer) e.dataTransfer.dropEffect = 'copy'
}

function onDragOver(e: DragEvent){
  if (e.dataTransfer) e.dataTransfer.dropEffect = 'copy'
}

function onDragLeave(){
  isDragOver.value = false
}

function parseCSV(text: string){
  return text.split(/\r?\n/).filter(Boolean).map(line => line.split(/,|\t/))
}

function parseXLSX(buffer: ArrayBuffer){
  const wb = XLSX.read(buffer, { type: 'array' })
  const first = wb.SheetNames[0]
  const sheet = wb.Sheets[first]
  const aoa = XLSX.utils.sheet_to_json(sheet, { header: 1 }) as any[]
  return aoa.map(r => r.map((c:any)=> c==null ? '' : String(c)))
}

function handleFile(file: File){
  if (file.size > 5 * 1024 * 1024) {
    alert(t('fileTooLarge', { size: 5 }))
    return
  }

  progress.value = 0.05

  if (/\.xls|\.xlsx$/i.test(file.name)){
    const reader = new FileReader()
    reader.onload = () => {
      try{
        const buf = reader.result as ArrayBuffer
        progress.value = 0.6
        const data = parseXLSX(buf)
        progress.value = 1
        emit('fileLoaded', { name: file.name, data })
        selectedName.value = file.name
        setTimeout(()=> progress.value = 0, 500)
      }catch(e){ alert(t('parseFailed')); progress.value = 0 }
    }
    reader.onprogress = (e)=>{ if (e.lengthComputable) progress.value = e.loaded / e.total }
    reader.readAsArrayBuffer(file)
    return
  }

  // fallback CSV
  const reader = new FileReader()
  reader.onload = () => {
    const text = String(reader.result || '')
    const data = parseCSV(text)
    progress.value = 1
    emit('fileLoaded', { name: file.name, data })
    selectedName.value = file.name
    setTimeout(()=> progress.value = 0, 500)
  }
  reader.onprogress = (e)=>{ if (e.lengthComputable) progress.value = e.loaded / e.total }
  reader.readAsText(file)
}

function onChange(e: Event){
  const input = e.target as HTMLInputElement
  const file = input.files && input.files[0]
  if (!file) return
  handleFile(file)
}

function onDrop(e: DragEvent){
  isDragOver.value = false
  const file = e.dataTransfer && e.dataTransfer.files[0]
  if (!file) return
  handleFile(file)
}

function downloadSample(){
  const csv = 'Name,Value1,Value2\nItem1,10,20\nItem2,30,40\nItem3,50,60\n'
  const blob = new Blob([csv], { type: 'text/csv' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = 'sample.csv'
  document.body.appendChild(a)
  a.click()
  a.remove()
  URL.revokeObjectURL(url)
}
</script>
