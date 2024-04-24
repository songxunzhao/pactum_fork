import { createSlice } from 'redux-starter-kit';
import { TOKEN_EXP_IN, TOKEN_STORAGE_KEY } from './constants';
import moment from 'moment';

const token = (window.localStorage && localStorage.getItem(TOKEN_STORAGE_KEY)) || null;

export interface AuthState {
  accessToken: AccessToken | null;
}

export interface AccessToken {
  access_token: string;
  expires_in: number;
  scope: string;
  token_type: string;
}

const initialState: AuthState = {
  accessToken: token == null ? null : JSON.parse(token),
};

const authSlice = createSlice<AuthState>({
  slice: 'auth',
  initialState,
  reducers: {
    loginSuccess: (state, action) => {
      if (window.localStorage) {
        const currentTimestamp = moment().unix();
        const accessTokenExp = currentTimestamp + action.payload.expires_in;

        localStorage.setItem(TOKEN_STORAGE_KEY, JSON.stringify(action.payload));
        localStorage.setItem(TOKEN_EXP_IN, String(accessTokenExp));
      }
      state.accessToken = action.payload;
    },
    logoutSuccess: state => {
      if (window.localStorage) {
        localStorage.removeItem(TOKEN_STORAGE_KEY);
        localStorage.removeItem(TOKEN_EXP_IN);
      }
      state.accessToken = null;
    },
  },
});

// Extract the action creators object and the reducer
const { actions, reducer } = authSlice;
// Extract and export each action creator by name
export const { loginSuccess, logoutSuccess } = actions;
// Export the reducer, either as a default or named export
export default reducer;
