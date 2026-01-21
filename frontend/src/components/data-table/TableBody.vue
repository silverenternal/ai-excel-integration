<template>
  <tbody>
    <tr v-for="(row, rowIndex) in bodyData" :key="rowIndex">
      <td v-for="(cell, colIndex) in row" :key="colIndex">
        <input 
          :value="cell" 
          @input="e => updateCell(rowIndex, colIndex, (e.target as HTMLInputElement).value)" 
        />
      </td>
    </tr>
  </tbody>
</template>

<script setup lang="ts">
interface Props {
  bodyData: string[][];
}

const props = defineProps<Props>()

const emit = defineEmits<{
  updateCell: [row: number, col: number, value: string]
}>()

function updateCell(row: number, col: number, value: string) {
  emit('updateCell', row, col, value)
}
</script>