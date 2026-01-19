import { fetchJson } from './apiClient';

export async function uploadExcel(file: File) {
  const formData = new FormData();
  formData.append('file', file);
  return fetchJson('/api/upload', { method: 'POST', body: formData });
}

export async function processExcelWithAI(file: File, command: string) {
  const formData = new FormData();
  formData.append('file', file);
  formData.append('command', command);
  return fetchJson('/api/ai/excel-with-ai', { method: 'POST', body: formData });
}

export async function processExcelWithAIDownload(file: File, command: string) {
  const formData = new FormData();
  formData.append('file', file);
  formData.append('command', command);
  const base = await (await import('./apiClient')).getApiBaseUrl();
  const url = `${base}/api/ai/excel-with-ai-download`;
  const resp = await fetch(url, { method: 'POST', body: formData, headers: { 'Accept': 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' } });
  if (!resp.ok) {
    const body = await resp.json().catch(() => null);
    const err = new Error(body?.error || `HTTP ${resp.status}`);
    (err as any).status = resp.status; (err as any).body = body;
    throw err;
  }
  return resp.blob();
}

export async function generateExcelFormula(context: string, goal: string) {
  return fetchJson('/api/ai/generate-formula', {
    method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ context, goal })
  });
}

export async function analyzeExcelData(file: File, analysisRequest: string) {
  const formData = new FormData();
  formData.append('file', file);
  formData.append('analysisRequest', analysisRequest);
  return fetchJson('/api/ai/excel-analyze', { method: 'POST', body: formData });
}

export async function getSuggestedCharts(file: File) {
  const formData = new FormData();
  formData.append('file', file);
  return fetchJson('/api/ai/suggest-charts', { method: 'POST', body: formData });
}

export async function getExcelDataPreview(file: File) {
  const formData = new FormData();
  formData.append('file', file);
  return fetchJson('/api/excel/get-data', { method: 'POST', body: formData });
}
