import { saveEmail } from './landingAction';
import store from '../store/store';
import { emailError, saveEmailSuccess } from './landingReducer';

jest.mock('../store/store');

(store as any).getState = jest.fn(() => {
  return { auth: { accessToken: { accessToken: 'test' } } };
});

it('saves email ', async () => {
  const response = { email: 'test@email.com', status: 'subscribed' };
  const dispatch = jest.fn();
  (fetch as any).mockResponseOnce(JSON.stringify(response));
  await saveEmail('test@email.com')(dispatch);
  expect(dispatch).toBeCalledWith(saveEmailSuccess(response));
});

it('throws error while saving email ', async () => {
  const response = { status: 404 };
  const dispatch = jest.fn();
  (fetch as any).mockResponseOnce(JSON.stringify(response));
  await saveEmail('test@email.com')(dispatch);
  expect(dispatch).toBeCalledWith(emailError(response.status));
});
