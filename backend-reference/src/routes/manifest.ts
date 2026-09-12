import { Request, Response, Router } from 'express';
import fs from 'fs';
import { config } from '../config';
import { evaluateManifest, Manifest } from '../services/manifestService';

const router = Router();

function readManifest(): Manifest {
  const raw = fs.readFileSync(config.manifestFile, 'utf-8');
  return JSON.parse(raw) as Manifest;
}

router.get('/update-manifest', (req: Request, res: Response) => {
  const currentVersionCode = Number(req.query.app_version ?? 1);
  const deviceId = String(req.query.device_id ?? 'unknown');

  const manifest = readManifest();
  const decision = evaluateManifest(manifest, currentVersionCode, deviceId);

  if (decision.hasUpdate) {
    res.json({ has_update: true, manifest: decision.manifest });
  } else {
    res.json({ has_update: false, reason: decision.reason });
  }
});

router.get('/health', (_req: Request, res: Response) => {
  res.json({ status: 'ok' });
});

export default router;
