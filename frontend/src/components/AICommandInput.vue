<template>
  <div>
    <label class="muted">{{ $t('ai_command_label') }}</label>

    <div style="display:flex; gap:8px; align-items:flex-start">
      <textarea
        rows="3"
        v-model="text"
        :placeholder="$t('ai_command_placeholder')"
        @keydown="onKeydown"
        :readonly="readOnly"
        :disabled="disabled"
        style="flex:1; resize:vertical; min-height:64px"
      ></textarea>

      <div style="display:flex; flex-direction:column; gap:8px">
        <button @click="run" :disabled="loading || submitBtnDisabled" style="white-space:nowrap">
          <span v-if="!loading">{{ $t('ai_command_run') }}</span>
          <span v-else>{{ $t('loading') || 'Loading...' }}</span>
        </button>
        <button v-if="clearable" @click="clear" :disabled="!text">{{ $t('clear') || 'Clear' }}</button>
      </div>
    </div>

    <div style="display:flex; gap:8px; margin-top:8px">
      <button @click="applyTemplate('创建新列\'Sum\'并计算A列和B列的和')">{{ $t('ai_command_template') }}</button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useI18n } from 'vue-i18n'
const { t } = useI18n()

const props = defineProps({
  submitType: { type: String, default: 'enter' }, // enter | shiftEnter | cmdOrCtrlEnter | altEnter
  clearable: { type: Boolean, default: true },
  loading: { type: Boolean, default: false },
  disabled: { type: Boolean, default: false },
  readOnly: { type: Boolean, default: false },
  submitBtnDisabled: { type: Boolean, default: false }
})

const emit = defineEmits(['runCommand'])
const text = ref('')

function run(){
  if (!text.value.trim()) return
  emit('runCommand', text.value.trim())
  text.value = ''
}

function applyTemplate(v:string){ text.value = v }

function clear(){ text.value = '' }

function setText(v:string){ text.value = v }

function onKeydown(e: KeyboardEvent){
  if (props.disabled || props.readOnly) return
  const s = props.submitType
  const isEnter = e.key === 'Enter'
  const ctrlOrCmd = e.ctrlKey || e.metaKey
  if (s === 'enter' && isEnter && !e.shiftKey && !ctrlOrCmd && !e.altKey){
    e.preventDefault(); run(); return
  }
  if (s === 'shiftEnter' && isEnter && e.shiftKey){ e.preventDefault(); run(); return }
  if (s === 'cmdOrCtrlEnter' && isEnter && ctrlOrCmd){ e.preventDefault(); run(); return }
  if (s === 'altEnter' && isEnter && e.altKey){ e.preventDefault(); run(); return }
}

defineExpose({ setText })
</script>
