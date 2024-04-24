import { Action, Dispatch } from 'redux';
import { logoutSuccess } from './authReducer';
import { PayloadAction } from 'redux-starter-kit';
import { push } from 'connected-react-router';

export const redirectToLogin = () => async (dispatch: Dispatch<PayloadAction>): Promise<Action> => {
  return dispatch(push('/login'));
};

export const logout = () => async (dispatch: Dispatch<PayloadAction>): Promise<Action> => {
  redirectToLogin()(dispatch);
  return dispatch(logoutSuccess(''));
};
