import { BASE_PATH, Configuration, Middleware, ResponseContext } from './api';
import store from './store/store';
import Swal from 'sweetalert2';

let basePath = BASE_PATH;

if (process.env.NODE_ENV !== 'production') {
  basePath = 'http://localhost:3000/api/v1';
}

const fetchAccessToken = () => {
  const state = store.getState();
  const { accessToken } = state.auth;
  if (accessToken) {
    return `Bearer ${accessToken.access_token}`;
  }
  throw new Error('No access token found');
};

export const apiErrorLoggerMiddleware: Middleware = {
  post(context: ResponseContext): Promise<Response | void> {
    if (context.response.status === 500) {
      Swal.fire({
        type: 'error',
        title: 'Oops...',
        text: context.response.statusText,
      });
    }
    return Promise.resolve();
  },
};

export const configuration = new Configuration({
  basePath,
  accessToken: fetchAccessToken,
  middleware: [apiErrorLoggerMiddleware],
});

export * from './api';

async function handleError(response: Response) {
  if (!response.ok) {
    const errorResponse = await response.json();
    console.log(errorResponse.message);
    const error = new Error(errorResponse.message);
    error.name = errorResponse.name;
    throw error;
  }
}

function post(url: string, data = {}) {
  return fetch(url, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(data),
  }).then(async response => {
    await handleError(response);
    return response.json();
  });
}

export function saveEmail(email: string) {
  return post('/api/v1/emails', email);
}
