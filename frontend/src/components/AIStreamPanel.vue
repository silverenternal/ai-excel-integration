<template>
  <div>
    <h4>{{ $t('ai_stream_title') }}</h4>
    <div v-if="messages.length===0" class="muted">{{ $t('ai_stream_waiting') }}</div>
    <div v-for="(m, i) in messages" :key="i" class="card" style="margin-top:8px">
      <div style="white-space:pre-wrap">{{ m }}</div>
      <div v-if="hasCommands(m)" style="display:flex; gap:8px; margin-top:8px">
        <button @click="apply(m)">{{ $t('applyBtn') }}</button>
        <button @click="edit(m)">{{ $t('editBtn') }}</button>
        <button @click="ignore(m)">{{ $t('ignoreBtn') }}</button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useI18n } from 'vue-i18n'
const { t } = useI18n()
const props = defineProps<{ messages: string[] }>()
const emit = defineEmits(['applyCommands','editCommand','ignoreCommand'])

function hasCommands(m:string){
  return /\[.*\]/.test(m)
}

function apply(m:string){ emit('applyCommands', m) }
function edit(m:string){ emit('editCommand', m) }
function ignore(m:string){ emit('ignoreCommand', m) }
</script>
