import React from 'react';
import { Route } from 'react-router-dom';
import { Provider } from 'react-redux';
import store, { history } from './store/store';
import { ConnectedRouter } from 'connected-react-router';
import { AnimatedSwitch, spring } from 'react-router-transition';
import Landing from './landing';
import ReactGA from 'react-ga';
import ChatWrapper from './Chat/ChatWrapper';
import { Privacy } from './privacy/Privacy';
import './custom.scss';

function glide(val: any) {
  return spring(val, {
    stiffness: 125,
    damping: 16,
  });
}

const pageTransitions = {
  atEnter: {
    offset: 100,
  },
  atLeave: {
    offset: glide(-100),
  },
  atActive: {
    offset: glide(0),
  },
};

function initializeReactGA() {
  ReactGA.initialize('UA-146927576-1');
  ReactGA.set({ page: window.location.pathname });
  ReactGA.pageview(window.location.pathname);
}

// eslint-disable-next-line @typescript-eslint/no-unused-vars
history.listen(location => {
  ReactGA.set({ page: window.location.pathname });
  ReactGA.pageview(window.location.pathname);
});

export default function App() {
  initializeReactGA();
  return (
    <Provider store={store}>
      <ConnectedRouter history={history}>
        <AnimatedSwitch
          {...pageTransitions}
          mapStyles={(styles: any) => ({
            transform: `translateY(${styles.offset}%)`,
            height: '100%',
          })}
          className="route-wrapper"
        >
          <Route path="/chat/states/:stateId/read-only" component={ChatWrapper} />
          <Route path="/chat/states/:stateId/delay/:delay" component={ChatWrapper} />
          <Route path="/chat/states/:stateId" component={ChatWrapper} />
          <Route path="/privacy" component={Privacy} />
          <Route
            path="/v2/models/:modelId/chat/:flowId/:modelKey/:stateId/delay/:delay"
            component={ChatWrapper}
          />
          <Route
            path="/v2/models/:modelId/chat/:flowId/:modelKey/:stateId/read-only"
            component={ChatWrapper}
          />
          <Route
            path="/v2/models/:modelId/chat/:flowId/:modelKey/:stateId"
            component={ChatWrapper}
          />
          <Route path="/v2/chat/:flowId/:modelKey/:stateId/delay/:delay" component={ChatWrapper} />
          <Route path="/v2/chat/:flowId/:modelKey/:stateId/read-only" component={ChatWrapper} />
          <Route path="/v2/chat/:flowId/:stateId/read-only" component={ChatWrapper} />
          <Route path="/v2/chat/:flowId/:stateId/delay/:delay" component={ChatWrapper} />
          <Route path="/v2/chat/:flowId/:modelKey/:stateId" component={ChatWrapper} />
          <Route path="/v2/chat/:flowId/:stateId" component={ChatWrapper} />
          <Route path="/v2/chat" component={ChatWrapper} />
          <Route
            path="/models/:modelId/chat/:flowId/:modelKey/:stateId/delay/:delay"
            component={ChatWrapper}
          />
          <Route
            path="/models/:modelId/chat/:flowId/:modelKey/:stateId/read-only"
            component={ChatWrapper}
          />
          <Route path="/models/:modelId/chat/:flowId/:modelKey/:stateId" component={ChatWrapper} />
          <Route path="/chat/:flowId/:modelKey/:stateId/delay/:delay" component={ChatWrapper} />
          <Route path="/chat/:flowId/:modelKey/:stateId/read-only" component={ChatWrapper} />
          <Route path="/chat/:flowId/:stateId/read-only" component={ChatWrapper} />
          <Route path="/chat/:flowId/:stateId/delay/:delay" component={ChatWrapper} />
          <Route path="/chat/:flowId/:modelKey/:stateId" component={ChatWrapper} />
          <Route path="/chat/:flowId/:stateId" component={ChatWrapper} />
          <Route path="/chat" component={ChatWrapper} />
          <Route path="/" component={Landing} />
        </AnimatedSwitch>
      </ConnectedRouter>
    </Provider>
  );
}
