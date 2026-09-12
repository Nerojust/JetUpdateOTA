import express, { Express } from 'express';
import cors from 'cors';
import { config } from './config';
import { errorHandler } from './middleware/errorHandler';
import analyticsRoutes from './routes/analytics';
import manifestRoutes from './routes/manifest';

export function createApp(): Express {
  const app = express();
  app.use(cors());
  app.use(express.json());
  app.use('/storage', express.static(config.storageDir));
  app.use('/api', manifestRoutes);
  app.use('/api/analytics', analyticsRoutes);
  app.use(errorHandler);
  return app;
}

if (require.main === module) {
  createApp().listen(config.port, () => {
    console.log(`OTA backend listening on port ${config.port}`);
  });
}
