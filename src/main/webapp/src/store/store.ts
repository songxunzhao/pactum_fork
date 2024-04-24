import { configureStore, getDefaultMiddleware } from 'redux-starter-kit';
import {
  connectRouter,
  LocationChangeAction,
  routerMiddleware,
  RouterState,
} from 'connected-react-router';
import { createBrowserHistory } from 'history';
import authReducer, { AuthState } from '../auth/authReducer';
import landingReducer, { LandingState } from '../landing/landingReducer';

export const history = createBrowserHistory();

export interface AppState {
  router: RouterState;
  auth: AuthState;
  landing: LandingState;
}

const store = configureStore<AppState, LocationChangeAction>({
  reducer: {
    router: connectRouter(history),
    auth: authReducer,
    landing: landingReducer,
  },
  middleware: [...getDefaultMiddleware(), routerMiddleware(history)],
});

export default store;
