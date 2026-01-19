import { fetchJson } from './apiClient';

export async function getTemplateLibrary() {
  return fetchJson('/api/templates/public');
}

export async function applyTemplate(templateId: number, data: any) {
  return fetchJson(`/api/templates/apply/${templateId}`, { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(data) });
}
