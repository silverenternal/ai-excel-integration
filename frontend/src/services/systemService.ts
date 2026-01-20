import { fetchJson, getApiBaseUrl } from './apiClient';

export async function checkServiceStatus() {
  return fetchJson('/api/status');
}

export async function getAnalysisCenterData() {
  return fetchJson('/api/analysis-center/data');
}

export async function getSystemSettings(overrideBase?: string) {
  const base = overrideBase && overrideBase.trim() !== '' ? overrideBase.replace(/\/$/, '') : await getApiBaseUrl();
  const url = `${base}/api/analysis-center/settings`;
  const resp = await fetch(url, { headers: { 'Accept': 'application/json' } });
  if (!resp.ok) {
    let body = null;
    try { body = await resp.json(); } catch (e) { /* ignore */ }
    const err = new Error(body?.error || `HTTP ${resp.status}`);
    (err as any).status = resp.status;
    (err as any).body = body;
    throw err;
  }
  return resp.json();
}

export async function saveSystemSettings(settings: any, overrideBase?: string) {
  const base = overrideBase && overrideBase.trim() !== '' ? overrideBase.replace(/\/$/, '') : await getApiBaseUrl();
  const url = `${base}/api/analysis-center/settings`;
  const resp = await fetch(url, { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(settings) });
  if (!resp.ok) {
    let body = null;
    try { body = await resp.json(); } catch (e) { /* ignore */ }
    const err = new Error(body?.error || `HTTP ${resp.status}`);
    (err as any).status = resp.status;
    (err as any).body = body;
    throw err;
  }
  return resp.json();
}
