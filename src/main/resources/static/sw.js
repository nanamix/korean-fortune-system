// 🔮 한국형 만세력 운세 시스템 - Service Worker
const CACHE_NAME = 'korean-fortune-v27';
const STATIC_ASSETS = [
  '/',
  '/fortune-app.html',
  '/index.html',
  '/api-docs.html',
  '/api-test.html',
  '/manifest.json',
  '/favicon.ico'
];

// Install: cache static assets
self.addEventListener('install', event => {
  event.waitUntil(
    caches.open(CACHE_NAME).then(cache => {
      return cache.addAll(STATIC_ASSETS);
    }).catch(err => {
      console.warn('[SW] 캐시 설치 실패:', err);
    })
  );
  self.skipWaiting();
});

// Activate: clean old caches
self.addEventListener('activate', event => {
  event.waitUntil(
    caches.keys().then(keys =>
      Promise.all(
        keys.filter(k => k !== CACHE_NAME).map(k => caches.delete(k))
      )
    )
  );
  self.clients.claim();
});

// Fetch: network-first for API and HTML, cache-first for other static assets
self.addEventListener('fetch', event => {
  const url = new URL(event.request.url);

  // API calls: always network, no cache
  if (url.pathname.startsWith('/api/') || url.pathname.startsWith('/actuator/')) {
    event.respondWith(fetch(event.request).catch(() =>
      new Response(JSON.stringify({ success: false, message: '오프라인 상태입니다.' }),
        { headers: { 'Content-Type': 'application/json' } })
    ));
    return;
  }

  // Documents must reflect the currently deployed version. Fall back to cache only offline.
  if (event.request.mode === 'navigate' || url.pathname === '/' || url.pathname.endsWith('.html')) {
    event.respondWith(
      fetch(event.request).then(response => {
        if (response.ok) {
          const clone = response.clone();
          caches.open(CACHE_NAME).then(cache => cache.put(event.request, clone));
        }
        return response;
      }).catch(() => caches.match(event.request).then(cached => cached || caches.match('/index.html')))
    );
    return;
  }

  // Static assets: cache-first with network fallback
  event.respondWith(
    caches.match(event.request).then(cached => {
      if (cached) return cached;
      return fetch(event.request).then(response => {
        if (response.ok) {
          const clone = response.clone();
          caches.open(CACHE_NAME).then(cache => cache.put(event.request, clone));
        }
        return response;
      }).catch(() => caches.match('/index.html'));
    })
  );
});
