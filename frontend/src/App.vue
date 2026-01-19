<template>
  <div class="app">
    <aside class="sidebar card">
      <nav style="margin-top:16px; display:flex; flex-direction:column; gap:8px">
        <router-link class="nav-link" to="/">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg"><path d="M3 11L12 3l9 8v8a1 1 0 0 1-1 1h-5v-6H9v6H4a1 1 0 0 1-1-1v-8z" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/></svg>
          <span>{{ $t('landing') }}</span>
        </router-link>

        <router-link class="nav-link" to="/workspace">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg"><rect x="3" y="3" width="8" height="8" rx="1" stroke="currentColor" stroke-width="1.5"/><rect x="13" y="3" width="8" height="8" rx="1" stroke="currentColor" stroke-width="1.5"/><rect x="3" y="13" width="8" height="8" rx="1" stroke="currentColor" stroke-width="1.5"/><rect x="13" y="13" width="8" height="8" rx="1" stroke="currentColor" stroke-width="1.5"/></svg>
          <span>{{ $t('workspace') }}</span>
        </router-link>

        <!-- Analysis and Templates moved into Workspace top tabs -->

        <router-link class="nav-link" to="/settings">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg"><path d="M12 15.5A3.5 3.5 0 1 0 12 8.5a3.5 3.5 0 0 0 0 7z" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/><path d="M19.4 15a1.65 1.65 0 0 0 .33 1.82l.06.06a2 2 0 0 1-2.83 2.83l-.06-.06a1.65 1.65 0 0 0-1.82-.33 1.65 1.65 0 0 0-1 1.51V21a2 2 0 0 1-4 0v-.09a1.65 1.65 0 0 0-1-1.51 1.65 1.65 0 0 0-1.82.33l-.06.06a2 2 0 0 1-2.83-2.83l.06-.06a1.65 1.65 0 0 0 .33-1.82 1.65 1.65 0 0 0-1.51-1H3a2 2 0 0 1 0-4h.09c.65 0 1.16-.49 1.51-1A1.65 1.65 0 0 0 4.79 6.6l-.06-.06A2 2 0 0 1 7.56 3.7l.06.06c.45.45 1.1.57 1.66.33.17-.07.35-.12.54-.15V4a2 2 0 0 1 4 0v.09c.19.03.37.08.54.15.56.24 1.21.12 1.66-.33l.06-.06a2 2 0 0 1 2.83 2.83l-.06.06c-.35.35-.46 0-1.51 1z" stroke="currentColor" stroke-width="1" stroke-linecap="round" stroke-linejoin="round"/></svg>
          <span>{{ $t('settings') }}</span>
        </router-link>
      </nav>
      <div style="margin-top:auto; display:flex; align-items:center; gap:8px">
        <div style="font-size:12px" class="muted">{{ $t('prototype') }}</div>
        <div style="display:flex; gap:6px; margin-left:8px">
          <button @click="setLocale('zh')" :class="['lang-btn', locale==='zh' ? 'active' : '']">中</button>
          <button @click="setLocale('en')" :class="['lang-btn', locale==='en' ? 'active' : '']">EN</button>
        </div>
      </div>
    </aside>



    <!-- Floating AI Excel FAB temporarily disabled. Uncomment to re-enable.
    <div class="ai-excel-fab" role="img" aria-label="AI Excel">
      <svg viewBox="0 0 64 64" xmlns="http://www.w3.org/2000/svg" aria-hidden="true">
        <defs>
          <linearGradient id="g" x1="0" x2="1" y1="0" y2="1">
            <stop offset="0" stop-color="#0f9d58" />
            <stop offset="1" stop-color="#34a853" />
          </linearGradient>
        </defs>
        <rect x="2" y="2" width="60" height="60" rx="10" fill="url(#g)" />
        <g transform="translate(0,0)" fill="#fff">
          <polygon points="18,44 26,20 34,44 30,44 28,34 24,34 22,44" />
          <rect x="40" y="20" width="6" height="24" rx="1" />
        </g>
        <g fill="#fff" opacity="0.18">
          <rect x="38" y="38" width="18" height="18" rx="3" />
        </g>
        <g fill="#19692f">
          <rect x="42" y="42" width="4" height="4" rx="0.5" />
          <rect x="48" y="42" width="4" height="4" rx="0.5" />
          <rect x="54" y="42" width="4" height="4" rx="0.5" />
          <rect x="42" y="48" width="4" height="4" rx="0.5" />
          <rect x="48" y="48" width="4" height="4" rx="0.5" />
          <rect x="54" y="48" width="4" height="4" rx="0.5" />
        </g>
      </svg>
    </div>
    -->

    <div class="main">
      <router-view />
    </div>
  </div>
</template>

<script setup lang="ts">
import { useI18n } from 'vue-i18n'
import { computed } from 'vue'

const { locale } = useI18n()

function setLocale(l: string) {
  locale.value = l
}

</script>

<style scoped>
.ai-excel-fab {
  position: fixed;
  right: 18px;
  top: 18px;
  width: 56px;
  height: 56px;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 6px 18px rgba(3,30,14,0.12);
  border-radius: 12px;
  cursor: pointer;
  z-index: 1200;
  transition: transform .15s ease, box-shadow .15s ease;
  background: transparent;
}
.ai-excel-fab svg { width: 48px; height: 48px; display: block; }
.ai-excel-fab:hover { transform: translateY(-3px); box-shadow: 0 10px 26px rgba(3,30,14,0.18); }
</style>
