import React, { ComponentType, useEffect } from 'react';
import { connect, ResolveThunks } from 'react-redux';
import { logout as logoutAction } from './authActions';
import { AppState } from '../store/store';
import { TOKEN_EXP_IN } from './constants';
import moment from 'moment';

function getDisplayName(WrappedComponent: ComponentType<any>) {
  return WrappedComponent.displayName || WrappedComponent.name || 'Component';
}

// higher order component which will redirect to login if tried to go to without auth.
const requireAuthentication = <P extends object>(WrappedComponent: ComponentType<P>): any => {
  function Authentication({ authenticated, logout, ...original }: StateProps & DispatchProps) {
    useEffect(() => {
      function checkAuthenticatedAndRedirect() {
        if (!authenticated) {
          logout();
        } else {
          if (window.localStorage) {
            const currentTimestamp = moment().unix();
            const tokenExpTimestamp = Number(localStorage.getItem(TOKEN_EXP_IN));
            if (tokenExpTimestamp < currentTimestamp) {
              logout();
            }
          }
        }
      }

      checkAuthenticatedAndRedirect();
    }, [authenticated, logout]);

    return <div>{authenticated ? <WrappedComponent {...original as P} /> : ''}</div>;
  }

  Authentication.displayName = `requireAuthentication(${getDisplayName(WrappedComponent)})`;

  const mapStateToProps = (state: AppState) => ({
    authenticated: state.auth.accessToken,
  });

  type StateProps = ReturnType<typeof mapStateToProps>;

  const mapDispatchToProps = { logout: logoutAction };

  type DispatchProps = ResolveThunks<typeof mapDispatchToProps>;

  return connect(
    mapStateToProps,
    mapDispatchToProps,
  )(Authentication);
};

export default requireAuthentication;
