import { Action, Dispatch } from 'redux';
import { PayloadAction } from 'redux-starter-kit';
import * as api from '../apis';
import { emailError, saveEmailSuccess } from './landingReducer';
import ReactGA from 'react-ga';

export const saveEmail = (email: string) => async (
  dispatch: Dispatch<PayloadAction>,
): Promise<Action> => {
  try {
    const emailState = await api.saveEmail(email);
    if (emailState.status !== 'subscribed') {
      return dispatch(emailError(emailState.status));
    }
    return dispatch(saveEmailSuccess(emailState));
  } catch (error) {
    dispatch(emailError(error.message));
    throw error;
  }
};

export const sendEmailToGA = (category: string) => {
  ReactGA.event({
    category: category,
    action: 'Sent',
  });
};
