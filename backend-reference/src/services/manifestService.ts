export interface Manifest {
  version_code: number;
  version_name: string;
  apk_url: string;
  checksum: string;
  file_size: number;
  changelog?: string;
  force_update: boolean;
  rollout_percentage: number;
  minimum_version_code: number;
  release_date: number;
}

export function hasNewerVersion(manifest: Manifest, currentVersionCode: number): boolean {
  return manifest.version_code > currentVersionCode;
}

export function isInRollout(deviceId: string, rolloutPercentage: number): boolean {
  if (rolloutPercentage >= 100) return true;
  const hash = Math.abs(
    Array.from(deviceId).reduce((acc, char) => (Math.imul(31, acc) + char.charCodeAt(0)) | 0, 0)
  );
  return hash % 100 < rolloutPercentage;
}

export type ManifestDecision =
  | { hasUpdate: true; manifest: Manifest }
  | { hasUpdate: false; reason: 'up_to_date' | 'not_in_rollout' };

export function evaluateManifest(
  manifest: Manifest,
  currentVersionCode: number,
  deviceId: string
): ManifestDecision {
  if (!isInRollout(deviceId, manifest.rollout_percentage)) {
    return { hasUpdate: false, reason: 'not_in_rollout' };
  }
  if (!hasNewerVersion(manifest, currentVersionCode)) {
    return { hasUpdate: false, reason: 'up_to_date' };
  }
  return { hasUpdate: true, manifest };
}
