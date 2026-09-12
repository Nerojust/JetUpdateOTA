import fs from 'fs';
import os from 'os';
import path from 'path';
import { copyIntoStorage, sha256File } from '../src/services/storageService';

describe('sha256File', () => {
  it('computes the sha256 hex digest of a file', () => {
    const tmpDir = fs.mkdtempSync(path.join(os.tmpdir(), 'ota-storage-test-'));
    const file = path.join(tmpDir, 'sample.txt');
    fs.writeFileSync(file, 'hello world');

    const digest = sha256File(file);

    expect(digest).toBe('b94d27b9934d3e08a52e52d7da7dabfac484efe37a5380ee9088f7ace2efcde9');
    fs.rmSync(tmpDir, { recursive: true, force: true });
  });
});

describe('copyIntoStorage', () => {
  it('copies the file into the storage dir named by version code', () => {
    const tmpDir = fs.mkdtempSync(path.join(os.tmpdir(), 'ota-storage-test-'));
    const source = path.join(tmpDir, 'app.apk');
    fs.writeFileSync(source, 'apk-bytes');
    const storageDir = path.join(tmpDir, 'storage');

    const target = copyIntoStorage(source, storageDir, 3);

    expect(target).toBe(path.join(storageDir, 'app-v3.apk'));
    expect(fs.readFileSync(target, 'utf-8')).toBe('apk-bytes');
    fs.rmSync(tmpDir, { recursive: true, force: true });
  });
});
