<template>
  <div class="chat-bubble-list">
    <div v-for="m in messages" :key="m.id" :class="['bubble-row', m.role === 'user' ? 'user' : 'ai']">
      <div class="bubble" :class="{placeholder: m.placeholder}">
        <div v-if="!m.placeholder" v-html="escapeHtml(m.text)"></div>
        <div v-else class="placeholder-dot">
          <span></span><span></span><span></span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
const props = defineProps<{ messages: Array<{ id: number; role: string; text: string; placeholder?: boolean }> }>()

function escapeHtml(s: string) {
  // small sanitizer: convert newlines to <br>
  return String(s).replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;').replace(/\n/g, '<br>')
}
</script>

<style scoped>
.chat-bubble-list { display:flex; flex-direction:column; gap:8px; padding:8px; max-height:60vh; overflow:auto }
.bubble-row { display:flex }
.bubble-row.user { justify-content:flex-end }
.bubble-row.ai { justify-content:flex-start }
.bubble { max-width:78%; padding:10px 14px; border-radius:14px; font-size:14px; line-height:1.4 }
/* apply styles on the inner .bubble based on the parent row role */
.bubble-row.user .bubble {
  background: linear-gradient(90deg,#8a2be2,#6d04c4) !important;
  color: #fff !important;
  box-shadow: 0 6px 18px rgba(109,4,196,0.12) !important;
  border: 1px solid rgba(109,4,196,0.18) !important;
}
.bubble-row.ai .bubble {
  background: rgba(255,255,255,0.04) !important;
  color: #fff !important;
}
.bubble.placeholder { background: rgba(255,255,255,0.04) !important; color:transparent !important }
.placeholder-dot { display:flex; gap:6px; align-items:center; }
.placeholder-dot span { width:8px; height:8px; background: #fff; border-radius:50%; opacity:0.18; animation: blink 1s infinite }
.placeholder-dot span:nth-child(2){ animation-delay:0.15s }
.placeholder-dot span:nth-child(3){ animation-delay:0.3s }
@keyframes blink{0%{opacity:0.18}50%{opacity:0.9}100%{opacity:0.18}}
</style>
