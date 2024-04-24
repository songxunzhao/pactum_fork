import { shallow } from 'enzyme';
import React from 'react';
import { Privacy } from './Privacy';
import { GeneratedPrivacyContent } from './GeneratedPrivacyContent';

let props: any;

it('renders the privacy page', () => {
  props = {};
  const wrapper = shallow(<Privacy {...props} />);
  expect(wrapper.find(GeneratedPrivacyContent)).toHaveLength(1);
});
