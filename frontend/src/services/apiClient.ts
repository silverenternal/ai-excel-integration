// 运行时 API 客户端：提供 getApiBaseUrl 和 fetch helpers
let cachedApiBaseUrl: string = '';

const DEFAULT_SERVER_PORT = '8081';
const DEFAULT_LOCALHOST_BASE = `http://localhost:${DEFAULT_SERVER_PORT}`;

async function getApiBaseUrl(): Promise<string> {
  if (cachedApiBaseUrl !== '') return cachedApiBaseUrl;

  // 1) Try the server-provided config endpoint first
  try {
    const resp = await fetch('/api/config');
    if (resp.ok) {
      const j = await resp.json();
      // If server provides explicit apiBaseUrl, prefer it
      if (j.apiBaseUrl && typeof j.apiBaseUrl === 'string' && j.apiBaseUrl.trim() !== '') {
        cachedApiBaseUrl = j.apiBaseUrl.replace(/\/$/, '')
        return cachedApiBaseUrl
      }

      // If server exposes SERVER_PORT or serverPort, use it to build localhost fallback
      const serverPort = j.serverPort || j.SERVER_PORT || j.server_port || '';
      const fallbackFromServerPort = (serverPort && String(serverPort).trim() !== '') ? `http://localhost:${serverPort}` : DEFAULT_LOCALHOST_BASE;

      // prefer current page origin when available, but avoid using Vite dev server origin (5173)
      try {
        const locOrigin = (typeof window !== 'undefined' && window.location && window.location.origin) ? window.location.origin : '';
        const locPort = (typeof window !== 'undefined' && window.location && window.location.port) ? window.location.port : '';
        if (locOrigin && locPort !== '5173') {
          cachedApiBaseUrl = locOrigin;
        } else {
          // when running under Vite dev server (5173) prefer the backend fallback
          cachedApiBaseUrl = fallbackFromServerPort;
        }
      } catch (e) {
        cachedApiBaseUrl = fallbackFromServerPort;
      }
      return cachedApiBaseUrl.replace(/\/$/, '')
    }
  } catch (e) {
    // ignore and continue to next fallback
  }

  // 2) Try to use saved settings only via full base resolved later (avoid calling settings path on dev server root)
  // Skip direct /api/analysis-center/settings call here to avoid Vite dev-server 404 when no proxy is configured.

  // 3) Final fallback: prefer current origin else localhost:{DEFAULT_SERVER_PORT}
  try {
    const locOrigin = (typeof window !== 'undefined' && window.location && window.location.origin) ? window.location.origin : '';
    cachedApiBaseUrl = locOrigin || DEFAULT_LOCALHOST_BASE;
  } catch (e) {
    cachedApiBaseUrl = DEFAULT_LOCALHOST_BASE;
  }
  return cachedApiBaseUrl.replace(/\/$/, '');
}

// Allow external code to override/clear the cached base (useful after saving settings)
function setApiBaseUrl(value: string | null) {
  if (!value) {
    cachedApiBaseUrl = '';
    return;
  }
  cachedApiBaseUrl = String(value).replace(/\/$/, '');
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

export { getApiBaseUrl, fetchJson, setApiBaseUrl };
