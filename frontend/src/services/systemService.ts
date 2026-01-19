import { fetchJson } from './apiClient';

export async function checkServiceStatus() {
  return fetchJson('/api/status');
}

export async function getAnalysisCenterData() {
  return fetchJson('/api/analysis-center/data');
}

export async function getSystemSettings() {
  return fetchJson('/api/analysis-center/settings');
}

export async function saveSystemSettings(settings: any) {
  return fetchJson('/api/analysis-center/settings', { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(settings) });
}
