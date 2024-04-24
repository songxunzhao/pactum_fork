import { shallow } from 'enzyme';
import React from 'react';
import { Header } from './Header';

let props: any;

it('renders the header with logo', () => {
  props = {};
  const wrapper = shallow(<Header {...props} />);
  expect(wrapper.find('img')).toHaveLength(1);
});
