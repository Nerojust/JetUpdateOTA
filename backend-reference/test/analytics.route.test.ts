import fs from 'fs';
import os from 'os';
import path from 'path';
import request from 'supertest';

describe('POST /api/analytics/event', () => {
  let tmpDir: string;
  let analyticsFile: string;
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  let app: any;

  beforeEach(() => {
    tmpDir = fs.mkdtempSync(path.join(os.tmpdir(), 'ota-backend-analytics-test-'));
    analyticsFile = path.join(tmpDir, 'analytics.jsonl');
    process.env.ANALYTICS_FILE = analyticsFile;
    jest.resetModules();
    // eslint-disable-next-line @typescript-eslint/no-var-requires
    app = require('../src/app').createApp();
  });

  afterEach(() => {
    fs.rmSync(tmpDir, { recursive: true, force: true });
    delete process.env.ANALYTICS_FILE;
  });

  it('accepts an event and appends it to the analytics file', async () => {
    const response = await request(app)
      .post('/api/analytics/event')
      .send({ event: 'update_available', version_code: 2, device_id: 'device-1' });

    expect(response.status).toBe(202);
    expect(response.body).toEqual({ received: true });

    const lines = fs.readFileSync(analyticsFile, 'utf-8').trim().split('\n');
    expect(lines).toHaveLength(1);
    const recorded = JSON.parse(lines[0]);
    expect(recorded.event).toBe('update_available');
    expect(recorded.version_code).toBe(2);
    expect(typeof recorded.timestamp).toBe('number');
  });
});
