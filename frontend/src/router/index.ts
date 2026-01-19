import { createRouter, createWebHistory } from 'vue-router'
import Landing from '../pages/Landing.vue'
import Workspace from '../pages/Workspace.vue'
import Analysis from '../pages/Analysis.vue'
import Templates from '../pages/Templates.vue'
import Settings from '../pages/Settings.vue'

const routes = [
  { path: '/', name: 'Landing', component: Landing },
  { path: '/workspace', name: 'Workspace', component: Workspace },
  { path: '/analysis', name: 'Analysis', component: Analysis },
  { path: '/templates', name: 'Templates', component: Templates },
  { path: '/settings', name: 'Settings', component: Settings }
]

export const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router
