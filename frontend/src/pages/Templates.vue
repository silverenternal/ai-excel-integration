<template>
  <div class="templates">
    <div class="topbar">
      <h2>{{ $t('templatesTitle') }}</h2>
      <div class="muted">{{ $t('templatesSubtitle') }}</div>
    </div>

    <div class="content">
      <!-- 第一行：模板管理 -->
      <div class="row row-first">
        <div class="card">
          <h4>{{ $t('template_management_title') }}</h4>
          <p class="muted">{{ $t('template_management_subtitle') }}</p>
          
          <div class="template-actions">
            <button @click="onApplyTemplate" class="apply-template-btn">{{ $t('applyTemplateBtn') }}</button>
            <button @click="onExportTemplates" class="export-btn">{{ $t('exportTemplates') }}</button>
            <button @click="onImportTemplates" class="import-btn">{{ $t('importTemplates') }}</button>
          </div>
        </div>
      </div>

      <!-- 第二行：其他模板横向平铺 -->
      <div class="row row-second">
        <div class="card">
          <h4>{{ $t('template_common_sales') }}</h4>
          <p>这是一个销售汇总模板，包含常用的销售统计和分析功能。</p>
          <button @click="applySalesTemplate" class="apply-btn">应用此模板</button>
        </div>

        <div class="card">
          <h4>{{ $t('template_pivot_dept_month') }}</h4>
          <p>按部门和月份的透视表模板，便于进行多维度数据分析。</p>
          <button @click="applyPivotTemplate" class="apply-btn">应用此模板</button>
        </div>

        <div class="card">
          <h4>{{ $t('template_cleaning') }}</h4>
          <p>数据清洗模板，包含去重、日期标准化等常用清洗步骤。</p>
          <button @click="applyCleaningTemplate" class="apply-btn">应用此模板</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useI18n } from 'vue-i18n'
import { getTemplateLibrary, applyTemplate } from '@/services/api'

const { t } = useI18n()

function onApplyTemplate() {
  alert(t('applyTemplateAlert', { name: t('template_common_sales') }))
}

function onExportTemplates() {
  alert(t('exportTemplatesAlert'))
}

function onImportTemplates() {
  alert(t('importTemplatesAlert'))
}

async function applySalesTemplate() {
  try {
    const result = await applyTemplate(1, {})
    if (result.success) {
      alert(`销售汇总模板应用成功: ${result.message}`)
    }
  } catch (error) {
    console.error('应用模板失败:', error)
    alert('应用模板失败')
  }
}

async function applyPivotTemplate() {
  try {
    const result = await applyTemplate(2, {})
    if (result.success) {
      alert(`透视表模板应用成功: ${result.message}`)
    }
  } catch (error) {
    console.error('应用模板失败:', error)
    alert('应用模板失败')
  }
}

async function applyCleaningTemplate() {
  try {
    const result = await applyTemplate(3, {})
    if (result.success) {
      alert(`数据清洗模板应用成功: ${result.message}`)
    }
  } catch (error) {
    console.error('应用模板失败:', error)
    alert('应用模板失败')
  }
}
</script>

<style scoped>
.templates {
  display: flex;
  flex-direction: column;
  height: 100%;
}

/* Ensure this page stacks rows vertically regardless of global .content */
.templates > .content {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.row-second {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
}
.row-second .card { min-height: 160px; }

@media (max-width: 900px) {
  .row-second { grid-template-columns: 1fr; }
}

.template-actions {
  display: flex;
  gap: 12px;
  margin-top: 16px;
  flex-wrap: wrap;
}

.apply-template-btn,
.export-btn,
.import-btn,
.apply-btn {
  background: #7c3aed;
  color: white;
  border: 0;
  padding: 8px 16px;
  border-radius: 6px;
  cursor: pointer;
}

.apply-template-btn {
  background: #10b981;
}
</style>