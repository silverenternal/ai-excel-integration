// 运行时 API 客户端：提供 getApiBaseUrl 和 fetch helpers
let cachedApiBaseUrl: string = '';

async function getApiBaseUrl(): Promise<string> {
  if (cachedApiBaseUrl !== '') return cachedApiBaseUrl;
  try {
    const resp = await fetch('/api/config');
    if (!resp.ok) throw new Error('Cannot load config');
    const j = await resp.json();
    const returned = j.apiBaseUrl || 'http://localhost:8081';
    try {
      const locOrigin = (typeof window !== 'undefined' && window.location && window.location.origin) ? window.location.origin : '';
      if (locOrigin) {
        cachedApiBaseUrl = locOrigin;
      } else {
        cachedApiBaseUrl = returned;
      }
    } catch (e) {
      cachedApiBaseUrl = returned;
    }
    return cachedApiBaseUrl;
  } catch (e) {
    cachedApiBaseUrl = (typeof window !== 'undefined' && window.location && window.location.origin) ? window.location.origin : 'http://localhost:8081';
    return cachedApiBaseUrl;
  }
}

async function fetchJson(path: string, options?: RequestInit) {
  const base = await getApiBaseUrl();
  const url = path.startsWith('http') ? path : `${base}${path.startsWith('/') ? '' : '/'}${path}`;
  const resp = await fetch(url, options);
  if (!resp.ok) {
    let body = null;
    try { body = await resp.json(); } catch (e) { /* ignore */ }
    const err = new Error(body?.error || `HTTP ${resp.status}`);
    // attach status and body for callers
    (err as any).status = resp.status;
    (err as any).body = body;
    throw err;
  }
  return resp.json();
}

export { getApiBaseUrl, fetchJson };
