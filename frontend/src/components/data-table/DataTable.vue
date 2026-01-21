<template>
  <div>
    <div v-if="!data || data.length===0" class="muted">{{ $t('data_noData') }}</div>
    <div v-else>
      <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:8px">
        <div class="muted">{{ $t('data_rowsCols', { rows: body.length, cols: header.length }) }}</div>
        <div style="display:flex; gap:8px">
          <button @click="addColumn">{{ $t('addColumn') }}</button>
          <button @click="addRow">{{ $t('addRow') }}</button>
          <button v-if="props.showExport" @click="$emit('export')">{{ $t('export') || '导出' }}</button>
        </div>
      </div>

      <table>
        <TableHeader 
          :headers="header" 
          :col-types="colTypes" 
          @remove-column="removeColumn" 
        />
        <TableBody 
          :body-data="body" 
          @update-cell="updateCell" 
        />
      </table>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'
import TableHeader from './TableHeader.vue'
import TableBody from './TableBody.vue'

const { t } = useI18n()

const props = defineProps<{ data: string[][], showExport?: boolean }>()
const emit = defineEmits(['updateCell','updateData','export'])

const header = computed(() => props.data[0] || [])
const body = computed(() => props.data.slice(1))

const colTypes = computed(() => {
  const h = props.data[0] || []
  const sample = props.data.slice(1, 6)
  return h.map((_, ci) => {
    let numCount = 0
    for (const r of sample) {
      const v = r[ci]
      if (v == null) continue
      if (!isNaN(Number(String(v).trim()))) numCount++
    }
    return numCount >= Math.max(1, sample.length/2) ? 'number' : 'text'
  })
})

function updateCell(r: number, c: number, value: string) {
  // r is body index (starting 0) -> map to actual row index = r+1
  emit('updateCell', { r: r+1, c, value })
}

function addColumn() {
  const newData = props.data.map((row, ri) => {
    const copy = [...row]
    if (ri === 0) copy.push('NewCol')
    else copy.push('')
    return copy
  })
  emit('updateData', newData)
}

function addRow() {
  const cols = (props.data[0] || []).length
  const newRow = Array(cols).fill('')
  const newData = [...props.data, newRow]
  emit('updateData', newData)
}

function removeColumn(ci: number) {
  const newData = props.data.map(row => {
    const copy = [...row]
    copy.splice(ci, 1)
    return copy
  })
  emit('updateData', newData)
}
</script>