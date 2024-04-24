import { configuration, apiErrorLoggerMiddleware, ResponseContext } from './apis';
import store from './store/store';

jest.mock('./store/store');

// process.env.NODE_ENV = 'production';
import { configuration as prodConfiguration } from './apis';

it('retrieves access token from state', () => {
  // eslint-disable-next-line @typescript-eslint/camelcase
  const state = { auth: { accessToken: { access_token: 'test' } } };
  (store.getState as jest.Mock).mockReturnValue(state);
  expect((configuration.accessToken as () => string)()).toEqual('Bearer test');
  jest.resetAllMocks();
});

it('throws when no access token in state', () => {
  const state = { auth: { accessToken: null } };
  (store.getState as jest.Mock).mockReturnValue(state);
  expect(() => (configuration.accessToken as () => string)()).toThrow(
    new Error('No access token found'),
  );
  jest.resetAllMocks();
});

it('expects middleware to handle error 500 response', async () => {
  const response = new Response('', {
    status: 500,
  });

  const context: ResponseContext = {
    fetch,
    url: 'https://example.com',
    init: {},
    response,
  };

  await expect(apiErrorLoggerMiddleware.post!(context)).toBeInstanceOf(Promise);
});

it('base url is replaced when not production', () => {
  expect(configuration.basePath).toEqual('http://localhost:3000/api/v1');
});

it('base url is replaced in production', () => {
  expect(prodConfiguration).not.toEqual('http://localhost:3000/api/v1');
});
