<template>
  <div>
    <h4>{{ $t('steps_title') }}</h4>
    <div v-if="!steps || steps.length===0" class="muted">{{ $t('steps_noSteps') }}</div>
    <div v-for="(s, idx) in steps" :key="s.id" class="card" style="display:flex; gap:8px; align-items:center; margin-top:8px">
      <input type="checkbox" v-model="s.enabled" />
      <div style="flex:1; white-space:pre-wrap">{{ s.text }}</div>
      <div style="display:flex; gap:6px">
        <button @click.prevent="moveUp(idx)">{{ $t('steps_moveUp') }}</button>
        <button @click.prevent="moveDown(idx)">{{ $t('steps_moveDown') }}</button>
        <button @click.prevent="remove(idx)">{{ $t('steps_delete') }}</button>
      </div>
    </div>
    <div style="margin-top:12px; display:flex; gap:8px">
      <button @click.prevent="undo">{{ $t('steps_undo') }}</button>
      <button @click.prevent="exportSteps">{{ $t('steps_export') }}</button>
      <button @click.prevent="importSteps">{{ $t('steps_import') }}</button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { toRef } from 'vue'
import { useI18n } from 'vue-i18n'
const { t } = useI18n()
const props = defineProps<{ steps: Array<{id:number,text:string,enabled:boolean}> }>()
const emit = defineEmits(['update:steps'])
const local = toRef(props, 'steps')
let history: Array<any[]> = []

function update(newSteps:any){
  history.push(JSON.parse(JSON.stringify(local.value)));
  emit('update:steps', newSteps)
}

function undo(){
  const prev = history.pop()
  if (!prev) return
  emit('update:steps', prev)
}

function exportSteps(){
  const blob = new Blob([JSON.stringify(local.value, null, 2)], { type: 'application/json' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url; a.download = 'steps.json'; document.body.appendChild(a); a.click(); a.remove(); URL.revokeObjectURL(url)
}

function importSteps(){
  const inp = document.createElement('input')
  inp.type = 'file'; inp.accept = 'application/json'
  inp.onchange = ()=>{
    const f = inp.files && inp.files[0]
    if (!f) return
    const r = new FileReader()
    r.onload = ()=>{
      try{
        const d = JSON.parse(String(r.result || '[]'))
        emit('update:steps', d)
      }catch(e){ alert(t('importFailed')) }
    }
    r.readAsText(f)
  }
  inp.click()
}

function moveUp(i:number){
  if (i <= 0) return;
  const arr = [...local.value];
  const tmp = arr[i-1];
  arr[i-1] = arr[i];
  arr[i] = tmp;
  update(arr)
}

function moveDown(i:number){
  if (i >= local.value.length-1) return;
  const arr = [...local.value];
  const tmp = arr[i+1];
  arr[i+1] = arr[i];
  arr[i] = tmp;
  update(arr)
}

function remove(i:number){
  const arr = [...local.value];
  arr.splice(i, 1);
  update(arr)
}
</script>
