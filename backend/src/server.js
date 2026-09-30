import 'dotenv/config';
import cors from 'cors';
import express from 'express';

const app = express();
app.use(cors());
app.use(express.json());

// DB 연결 전에는 프론트엔드가 localStorage를 사용합니다.
// 이후 이 엔드포인트를 MySQL repository/service로 교체하세요.
app.get('/api/health', (_req, res) => res.json({ status: 'ok', database: process.env.MYSQL_DATABASE ?? 'not-configured' }));

const port = process.env.BACKEND_PORT || 4000;
app.listen(port, () => console.log(`Gudo Simdo API listening on ${port}`));
