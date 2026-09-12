import fs from 'fs';
import os from 'os';
import path from 'path';
import request from 'supertest';

describe('GET /api/update-manifest', () => {
  let tmpDir: string;
  let manifestFile: string;
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  let app: any;

  beforeEach(() => {
    tmpDir = fs.mkdtempSync(path.join(os.tmpdir(), 'ota-backend-test-'));
    manifestFile = path.join(tmpDir, 'manifest.json');
    fs.writeFileSync(
      manifestFile,
      JSON.stringify({
        version_code: 2,
        version_name: '1.1.0',
        apk_url: 'https://cdn.example.com/app-v2.apk',
        checksum: 'abc123',
        file_size: 1000,
        force_update: false,
        rollout_percentage: 100,
        minimum_version_code: 1,
        release_date: 0,
      })
    );
    process.env.MANIFEST_FILE = manifestFile;
    jest.resetModules();
    // eslint-disable-next-line @typescript-eslint/no-var-requires
    app = require('../src/app').createApp();
  });

  afterEach(() => {
    fs.rmSync(tmpDir, { recursive: true, force: true });
    delete process.env.MANIFEST_FILE;
  });

  it('returns has_update true with the manifest when a newer version is available', async () => {
    const response = await request(app)
      .get('/api/update-manifest')
      .query({ app_version: 1, device_id: 'device-1' });

    expect(response.status).toBe(200);
    expect(response.body.has_update).toBe(true);
    expect(response.body.manifest.version_code).toBe(2);
  });

  it('returns has_update false with reason up_to_date when already current', async () => {
    const response = await request(app)
      .get('/api/update-manifest')
      .query({ app_version: 2, device_id: 'device-1' });

    expect(response.status).toBe(200);
    expect(response.body).toEqual({ has_update: false, reason: 'up_to_date' });
  });

  it('returns 200 with ok on /api/health', async () => {
    const response = await request(app).get('/api/health');

    expect(response.status).toBe(200);
    expect(response.body).toEqual({ status: 'ok' });
  });
});
