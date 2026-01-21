<template>
  <div class="chat-input">
    <textarea
      v-model="text"
      @keydown.enter.exact.prevent="onSend"
      :placeholder="placeholder"
      rows="1"
      ref="ta"
    ></textarea>
    <button class="send-btn" @click="onSend" :aria-label="sendLabel">⮕</button>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, nextTick } from 'vue'

const emit = defineEmits(['send'])
const text = ref('')
const ta = ref<HTMLTextAreaElement | null>(null)
const placeholder = '请输入内容'
const sendLabel = 'send'

function onSend(){
  const v = String(text.value || '').trim()
  if (!v) return
  emit('send', v)
  text.value = ''
  // keep focus
  nextTick(()=> ta.value?.focus())
}

function setText(v: string){ text.value = v }
defineExpose({ setText })

onMounted(()=>{
  ta.value?.addEventListener('input', ()=>{
    if(ta.value) {
      ta.value.style.height = 'auto'
      ta.value.style.height = Math.min(200, ta.value.scrollHeight) + 'px'
    }
  })
})
</script>

<style scoped>
.chat-input {
  position: relative;
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px;
  background: #0f1720;
  border-radius: 10px;
  border: 1px solid rgba(255,255,255,0.06);
}
.chat-input textarea {
  flex: 1;
  resize: none;
  background: transparent;
  border: none;
  outline: none;
  color: #cfd3da;
  font-size: 14px;
  padding: 8px;
  padding-right: 64px; /* leave space for floating send button */
  line-height: 1.3;
}
.chat-input textarea::placeholder {
  color: rgba(255,255,255,0.28);
}
.send-btn {
  position: absolute;
  right: 12px;
  top: 50%;
  transform: translateY(-50%);
  width: 40px;
  height: 40px;
  border-radius: 50%;
  border: none;
  background: #fff;
  color: #0f1720;
  font-weight: 700;
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  justify-content: center;
}
</style>
