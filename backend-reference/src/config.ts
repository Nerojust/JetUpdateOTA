import path from 'path';

export const config = {
  port: Number(process.env.PORT ?? 3000),
  manifestFile: process.env.MANIFEST_FILE ?? path.join('data', 'manifest.json'),
  storageDir: process.env.STORAGE_DIR ?? 'storage',
  analyticsFile: process.env.ANALYTICS_FILE ?? path.join('data', 'analytics.jsonl'),
};
