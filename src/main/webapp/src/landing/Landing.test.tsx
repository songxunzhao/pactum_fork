import { shallow } from 'enzyme';
import React from 'react';
import ChatWrapper from '../Chat/ChatWrapper';
import { Landing } from './Landing';

let props: any;

it('renders the Chat demo on success', () => {
  props = {
    chat: {
      steps: [],
    },
  };

  const wrapper = shallow(<Landing {...props} />);
  expect(wrapper.find(ChatWrapper)).toHaveLength(1);
});

it('renders subscription success message on subscription success', () => {
  props = {
    state: 'subscribed',
  };

  const wrapper = shallow(<Landing {...props} />);
  expect(wrapper.contains(<p className="text-success">Thanks we{"'"}ll be in touch.</p>)).toEqual(
    true,
  );
});

it('renders corresponding error message on error code 404', () => {
  props = {
    error: 'Other errors',
  };

  const wrapper = shallow(<Landing {...props} />);
  expect(
    wrapper.contains(
      <p className="text-danger">There was an error while subscribing. Please try again!</p>,
    ),
  ).toEqual(true);
});

it('renders corresponding error message on error code 409', () => {
  props = {
    error: 'Member Exists',
  };

  const wrapper = shallow(<Landing {...props} />);
  expect(
    wrapper.contains(
      <p className="text-danger">This email address has already been sent to us.</p>,
    ),
  ).toEqual(true);
});
