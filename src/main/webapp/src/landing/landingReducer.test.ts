import landingReducer, { saveEmailSuccess, emailError } from './landingReducer';

it('saves email', () => {
  const initialState: any = {};
  const newState = landingReducer(initialState, saveEmailSuccess({ status: 'subscribed' }));
  expect(newState.state).toEqual('subscribed');
});

it('throws error on email error', () => {
  const initialState: any = {};
  const newState = landingReducer(initialState, emailError(404));
  expect(newState.error).toEqual(404);
});
