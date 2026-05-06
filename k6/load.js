import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
  vus: 500,
  duration: '30s',
};

export default function () {
  const res = http.get('http://127.0.0.1:8001/caramelo', { timeout: '10s' });

  check(res, {
    'status is 200': (r) => r.status === 200,
  });

  sleep(0.1);
}