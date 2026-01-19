<template>
  <div style="padding:32px">
    <h1>{{ $t('landingTitle') }}</h1>
    <p class="muted">{{ $t('landingSubtitle') }}</p>

    <div style="margin-top:24px; display:flex; gap:12px">
      <button @click="goWorkspace">{{ $t('landingGoWorkspace') }}</button>
      <button @click="openDemo">{{ $t('landingOpenDemo') }}</button>
    </div>

    <section style="margin-top:20px">
      <h3>{{ $t('landingCoreTitle') }}</h3>
      <ul>
        <li>{{ $t('landingCore1') }}</li>
        <li>{{ $t('landingCore2') }}</li>
        <li>{{ $t('landingCore3') }}</li>
        <li>{{ $t('landingCore4') }}</li>
      </ul>
    </section>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
const router = useRouter()
function goWorkspace(){ router.push({ name: 'Workspace' }) }
import { useI18n } from 'vue-i18n'
const { t } = useI18n()
function openDemo(){ alert(t('landingDemoAlert')) }
</script>

<style scoped>
.mermaid-diagram {
  background: rgba(255,255,255,0.02);
  border-radius: 6px;
  padding: 12px;
  margin-top: 12px;
}
</style>

