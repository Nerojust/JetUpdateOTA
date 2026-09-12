import crypto from 'crypto';
import fs from 'fs';
import path from 'path';

export function sha256File(filePath: string): string {
  const hash = crypto.createHash('sha256');
  hash.update(fs.readFileSync(filePath));
  return hash.digest('hex');
}

export function copyIntoStorage(sourceFile: string, storageDir: string, versionCode: number): string {
  fs.mkdirSync(storageDir, { recursive: true });
  const targetPath = path.join(storageDir, `app-v${versionCode}.apk`);
  fs.copyFileSync(sourceFile, targetPath);
  return targetPath;
}
