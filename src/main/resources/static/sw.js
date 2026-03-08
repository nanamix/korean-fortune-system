// 🔮 한국형 만세력 운세 시스템 - Service Worker
const CACHE_NAME = 'korean-fortune-v1';
const STATIC_ASSETS = [
  '/',
  '/fortune-app.html',
  '/index.html',
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

// Fetch: network-first for API calls, cache-first for static assets
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
