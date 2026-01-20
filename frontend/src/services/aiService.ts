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

export async function chatStream(message: string) {
  return fetchJson('/api/ai/chat-stream', {
    method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ message })
  });
}

export async function chatSse(message: string) {
  // Simple fetch to GET SSE endpoint for testing; will return raw text stream if server responds.
  const base = await (await import('./apiClient')).getApiBaseUrl();
  const url = `${base}/api/ai/chat-sse?message=${encodeURIComponent(message)}`;
  const resp = await fetch(url);
  if (!resp.ok) {
    const body = await resp.json().catch(() => null);
    const err = new Error(body?.error || `HTTP ${resp.status}`);
    (err as any).status = resp.status; (err as any).body = body;
    throw err;
  }
  const text = await resp.text().catch(() => '');
  return { text };
}

export async function createChart(file: File, chartType: string, targetColumn: string) {
  const formData = new FormData();
  formData.append('file', file);
  formData.append('chartType', chartType);
  formData.append('targetColumn', targetColumn);
  return fetchJson('/api/excel/create-chart', { method: 'POST', body: formData });
}

export async function sortData(file: File, sortColumn: string, sortOrder: string) {
  const formData = new FormData();
  formData.append('file', file);
  formData.append('sortColumn', sortColumn);
  formData.append('sortOrder', sortOrder);
  return fetchJson('/api/excel/sort-data', { method: 'POST', body: formData });
}

export async function filterData(file: File, filterColumn: string, filterValue: string) {
  const formData = new FormData();
  formData.append('file', file);
  formData.append('filterColumn', filterColumn);
  formData.append('filterValue', filterValue);
  return fetchJson('/api/excel/filter-data', { method: 'POST', body: formData });
}

export async function chat(message: string) {
  return fetchJson('/api/ai/chat', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ message })
  });
}

// Combines data preview and AI processing:
export async function processExcelAndChat(file: File, command: string) {
  // 1) get parsed array preview via data analysis API
  let preview: any = null
  try {
    preview = await getExcelDataPreview(file)
  } catch (e) {
    preview = null
  }

  // 2) send file + command to ai/excel-with-ai (this returns ai response + commandResults etc.)
  let aiResp: any = null
  try {
    aiResp = await processExcelWithAI(file, command)
  } catch (e) {
    // rethrow so caller can show error; attach preview if available
    (e as any).preview = preview
    throw e
  }

  return { preview, aiResp }
}

// For testing: call many AI and data-analysis endpoints sequentially and return a summary
export async function runAllApis(file: File, command = '测试调用所有API') {
  const summary: Record<string, any> = {}
  try {
    summary.upload = await uploadExcel(file).catch(e => ({ error: String(e) }))
  } catch (e) { summary.upload = { error: String(e) } }

  try { summary.getData = await getExcelDataPreview(file).catch(e => ({ error: String(e) })) } catch (e) { summary.getData = { error: String(e) } }

  try { summary.aiProcess = await processExcelWithAI(file, command).catch(e => ({ error: String(e) })) } catch (e) { summary.aiProcess = { error: String(e) } }

  try {
    // call download endpoint but do not trigger a user download; just fetch blob and report size
    const blob = await processExcelWithAIDownload(file, command).catch(e => ({ error: String(e) }))
    if (blob && blob instanceof Blob) summary.download = { size: blob.size }
    else summary.download = blob
  } catch (e) { summary.download = { error: String(e) } }

  try { summary.generateFormula = await generateExcelFormula('A列为值，B列为值', '计算A-B').catch(e => ({ error: String(e) })) } catch (e) { summary.generateFormula = { error: String(e) } }

  try { summary.analyze = await analyzeExcelData(file, '分析销售趋势').catch(e => ({ error: String(e) })) } catch (e) { summary.analyze = { error: String(e) } }

  try { summary.suggestCharts = await getSuggestedCharts(file).catch(e => ({ error: String(e) })) } catch (e) { summary.suggestCharts = { error: String(e) } }

  try { summary.chat = await chat('请告诉我这份表格的摘要').catch(e => ({ error: String(e) })) } catch (e) { summary.chat = { error: String(e) } }

  try { summary.chatStream = await chatStream('流式测试消息').catch(e => ({ error: String(e) })) } catch (e) { summary.chatStream = { error: String(e) } }

  try { summary.chatSse = await chatSse('SSE测试消息').catch(e => ({ error: String(e) })) } catch (e) { summary.chatSse = { error: String(e) } }

  try { summary.createChart = await createChart(file, 'bar', 'Sales').catch(e => ({ error: String(e) })) } catch (e) { summary.createChart = { error: String(e) } }

  try { summary.sortData = await sortData(file, 'Sales', 'DESC').catch(e => ({ error: String(e) })) } catch (e) { summary.sortData = { error: String(e) } }

  try { summary.filterData = await filterData(file, 'Region', 'North').catch(e => ({ error: String(e) })) } catch (e) { summary.filterData = { error: String(e) } }

  return summary
}
