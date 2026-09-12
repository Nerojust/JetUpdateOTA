import fs from 'fs';
import path from 'path';
import { config } from '../src/config';
import { Manifest } from '../src/services/manifestService';
import { copyIntoStorage, sha256File } from '../src/services/storageService';

function main() {
  const [apkPath, versionCodeArg, versionName, apkBaseUrl] = process.argv.slice(2);
  if (!apkPath || !versionCodeArg || !versionName || !apkBaseUrl) {
    console.error('Usage: publish-release <apkPath> <versionCode> <versionName> <apkBaseUrl>');
    process.exit(1);
  }

  const versionCode = Number(versionCodeArg);
  const checksum = sha256File(apkPath);
  const fileSize = fs.statSync(apkPath).size;
  const targetPath = copyIntoStorage(apkPath, config.storageDir, versionCode);

  const manifest: Manifest = {
    version_code: versionCode,
    version_name: versionName,
    apk_url: `${apkBaseUrl}/${path.basename(targetPath)}`,
    checksum,
    file_size: fileSize,
    force_update: false,
    rollout_percentage: 100,
    minimum_version_code: 1,
    release_date: Date.now(),
  };

  fs.mkdirSync(path.dirname(config.manifestFile), { recursive: true });
  fs.writeFileSync(config.manifestFile, JSON.stringify(manifest, null, 2));
  console.log(`Published version ${versionCode} -> ${config.manifestFile}`);
}

main();
