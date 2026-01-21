<template>
  <thead>
    <tr>
      <th v-for="(headerItem, index) in headers" :key="index" style="position:relative;">
        <div style="position:relative; padding-right:18px">
          <div>{{ headerItem || $t('col_default', { n: index+1 }) }}</div>
          <div class="muted" style="font-size:11px">{{ colTypes[index] === 'number' ? $t('type_number') : $t('type_text') }}</div>
          <button class="col-remove" @click.prevent="removeColumn(index)" aria-label="删除列">×</button>
        </div>
      </th>
    </tr>
  </thead>
</template>

<script setup lang="ts">
import { useI18n } from 'vue-i18n'

const { t } = useI18n()

interface Props {
  headers: string[];
  colTypes: string[];
}

const props = defineProps<Props>()

const emit = defineEmits<{
  removeColumn: [index: number]
}>()

function removeColumn(index: number) {
  emit('removeColumn', index)
}
</script>