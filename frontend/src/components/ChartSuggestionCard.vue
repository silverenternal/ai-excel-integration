<template>
  <el-card class="chart-suggestion-card" shadow="hover">
    <template #header>
      <div class="card-header">
        <span>{{ suggestion.title }}</span>
        <el-tag :type="getConfidenceTagType(suggestion.confidence)">{{ 
          $t('confidenceLevel', { level: suggestion.confidence }) 
        }}</el-tag>
      </div>
    </template>
    
    <div class="chart-preview">
      <div v-if="suggestion.examplePreview" class="preview-content">
        {{ suggestion.examplePreview }}
      </div>
      <div v-else class="no-preview">
        {{ $t('noPreviewAvailable') }}
      </div>
    </div>
    
    <div class="suggestion-actions">
      <el-button type="primary" @click="applySuggestion">{{ $t('applyBtn') }}</el-button>
      <el-button @click="viewDetails">{{ $t('viewDetails') }}</el-button>
    </div>
  </el-card>
</template>

<script setup lang="ts">
import { ElCard, ElTag, ElButton } from 'element-plus'
import { useI18n } from 'vue-i18n'

const { t } = useI18n()

interface Suggestion {
  title: string
  description: string
  confidence: 'high' | 'medium' | 'low'
  examplePreview?: string
  chartType: string
}

const props = defineProps<{
  suggestion: Suggestion
}>()

const emit = defineEmits(['apply', 'view-details'])

function getConfidenceTagType(confidence: string) {
  switch (confidence) {
    case 'high': return 'success'
    case 'medium': return 'warning'
    case 'low': return 'info'
    default: return 'info'
  }
}

function applySuggestion() {
  emit('apply', props.suggestion)
}

function viewDetails() {
  emit('view-details', props.suggestion)
}
</script>

<style scoped>
.chart-suggestion-card {
  margin-bottom: 16px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.chart-preview {
  min-height: 100px;
  padding: 12px;
  background-color: #f8f9fa;
  border-radius: 4px;
  margin-bottom: 16px;
}

.preview-content {
  white-space: pre-wrap;
  font-family: monospace;
  font-size: 12px;
  color: #666;
}

.no-preview {
  color: #999;
  font-style: italic;
}

.suggestion-actions {
  display: flex;
  gap: 8px;
}
</style>