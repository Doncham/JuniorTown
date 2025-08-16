import  http from 'k6/http';
import { check } from 'k6';

function getRandomPageId(min, max) {
    return Math.floor(Math.random() * (max - min + 1)) + min;
}

export let options = {
    scenarios: {
        constant_request_rate: {
            executor: 'ramping-arrival-rate',
            startRate: 167, // 초당 약 167개의 요청으로 시작
            timeUnit: '1s',
            preAllocatedVus: 50, // 사전 할당된 VU 수
            maxVUs: 100, // 최대 VU 수
            stages: [
                { duration: '1m', target: 167},
            ]
        }
    }
}

export default function () {
    const pageId = getRandomPageId(0,1);
    const params = {
        headers: {
            'Content-Type': 'application/json',
        },
    };

    let response = http.get('http://host.docker.internal:8080/api/posts/' + pageId, params);

    check(response, {
        'status is 200': (r) => r.status === 200,
    })
}