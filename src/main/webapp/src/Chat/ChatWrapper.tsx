import React from 'react';
import Chat from '@pactum-ai/pactum-chat/dist/App';
import PropTypes from 'prop-types';

interface Props {
  showPoweredBy?: boolean;
}

const ChatWrapper: React.FunctionComponent<Props> = ({ showPoweredBy }) => {
  let basePath = window.location.origin + '/api/v1';

  return <Chat showPoweredBy={showPoweredBy} apiUrl={basePath} />;
};

ChatWrapper.propTypes = {
  showPoweredBy: PropTypes.bool,
};

export default ChatWrapper;
