/* eslint-disable @typescript-eslint/camelcase */

import { TOKEN_EXP_IN, TOKEN_STORAGE_KEY } from './constants';
import React from 'react';
import { mount } from 'enzyme';
import App from '../App';

xit('authentication failed when token is expired', () => {
  const TOKEN_EXP_IN_VALUE = '1554965535';

  const token =
    'eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJleHAiOjE1NTYyNDI5MDAsInVzZXJfbmFtZSI6ImdsaWdvcmlqZUBwcm9kdWNlbWVudC5jb20iLCJhdXRob3JpdGllcyI6WyJST0xFX0FkbWluIiwiUk9MRV9VU0VSIiwiUk9MRV9aYXoiXSwianRpIjoiNGQ4ZTNhNGUtMDc3ZS00MzBhLTkwOGMtMWNjOTY5YzEwNzhiIiwiY2xpZW50X2lkIjoicm1zLWZyb250ZW5kIiwic2NvcGUiOlsiKiJdfQ.BkvC1Q_S0h1QTO98AB9OVc4sP82oNLf4IQRGn7ZXo0s_Qnq1oY_TWOWGUj1qPb2M5LmKDHGo4tXfc_p1A0VA7gNnocUTkxcBjt94DyCyC72ZFr_2-SLvAbisLDkpWddjMoQs1vQFA18za-hWl-_HkcJPn9Fv79wSOgGTs4XYgyN8UXIULRLaa7JVSkwrFJ5jiwpWDShyQmrTOwcPASjgtKzle5B4g7TaL359PTYvsRl1SoG7pjS22eTaw8Y0WMH0xQvJimtLvBTWUBZc1nuDGwtfFCf1zKhVlquovRYIOzO0pSN8lNt-WZljpfgxgPXjoRL-6K32muuxX2AfqQQuEQ';

  localStorage.setItem(TOKEN_EXP_IN, TOKEN_EXP_IN_VALUE);
  localStorage.setItem(TOKEN_STORAGE_KEY, token);

  const store = require('../store/store').default;
  store.dispatch(
    require('./authReducer').loginSuccess({
      access_token: token,
      expires_in: -1,
    }),
  );

  mount(<App />);

  expect(localStorage.length).toBe(0);
});
