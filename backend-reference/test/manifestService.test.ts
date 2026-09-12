import { evaluateManifest, hasNewerVersion, isInRollout, Manifest } from '../src/services/manifestService';

const baseManifest: Manifest = {
  version_code: 2,
  version_name: '1.1.0',
  apk_url: 'https://cdn.example.com/app-v2.apk',
  checksum: 'abc123',
  file_size: 1000,
  force_update: false,
  rollout_percentage: 100,
  minimum_version_code: 1,
  release_date: 0,
};

describe('hasNewerVersion', () => {
  it('is true when the manifest version is greater than the current version', () => {
    expect(hasNewerVersion(baseManifest, 1)).toBe(true);
  });

  it('is false when the manifest version is not greater than the current version', () => {
    expect(hasNewerVersion(baseManifest, 2)).toBe(false);
  });
});

describe('isInRollout', () => {
  it('is always true at 100 percent rollout', () => {
    expect(isInRollout('any-device', 100)).toBe(true);
  });

  it('is always false at 0 percent rollout', () => {
    expect(isInRollout('any-device', 0)).toBe(false);
  });
});

describe('isInRollout hash stability', () => {
  it('buckets a known device id deterministically', () => {
    // computed via: Math.abs(Array.from('device-1').reduce((acc, c) => (Math.imul(31, acc) + c.charCodeAt(0)) | 0, 0)) % 100
    // device-1's bucket is 66, so it is in rollout at 67% but not at 66%.
    expect(isInRollout('device-1', 67)).toBe(true);
    expect(isInRollout('device-1', 66)).toBe(false);
  });
});

describe('evaluateManifest', () => {
  it('reports not_in_rollout when the device falls outside the rollout', () => {
    const manifest = { ...baseManifest, rollout_percentage: 0 };

    const decision = evaluateManifest(manifest, 1, 'device-1');

    expect(decision).toEqual({ hasUpdate: false, reason: 'not_in_rollout' });
  });

  it('reports up_to_date when the current version is already the newest', () => {
    const decision = evaluateManifest(baseManifest, 2, 'device-1');

    expect(decision).toEqual({ hasUpdate: false, reason: 'up_to_date' });
  });

  it('reports hasUpdate with the manifest when a newer version is in rollout', () => {
    const decision = evaluateManifest(baseManifest, 1, 'device-1');

    expect(decision).toEqual({ hasUpdate: true, manifest: baseManifest });
  });
});
