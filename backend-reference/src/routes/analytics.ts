import { Request, Response, Router } from 'express';
import fs from 'fs';
import path from 'path';
import { config } from '../config';

const router = Router();

router.post('/event', (req: Request, res: Response) => {
  const event = { ...req.body, timestamp: req.body.timestamp ?? Date.now() };
  fs.mkdirSync(path.dirname(config.analyticsFile), { recursive: true });
  fs.appendFileSync(config.analyticsFile, JSON.stringify(event) + '\n');
  res.status(202).json({ received: true });
});

export default router;
