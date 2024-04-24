/* eslint-disable @typescript-eslint/camelcase */
import React from 'react';

describe('auth actions', () => {
  it('check login submit to succeed', async () => {
    let message = false;
    try {
      const loginSuccess = require('./authReducer').loginSuccess;
      const store = require('../store/store').default;
      store.dispatch(
        loginSuccess({
          access_token:
            'eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJleHAiOjE1NTYyNDI5MDAsInVzZXJfbmFtZSI6ImdsaWdvcmlqZUBwcm9kdWNlbWVudC5jb20iLCJhdXRob3JpdGllcyI6WyJST0xFX0FkbWluIiwiUk9MRV9VU0VSIiwiUk9MRV9aYXoiXSwianRpIjoiNGQ4ZTNhNGUtMDc3ZS00MzBhLTkwOGMtMWNjOTY5YzEwNzhiIiwiY2xpZW50X2lkIjoicm1zLWZyb250ZW5kIiwic2NvcGUiOlsiKiJdfQ.BkvC1Q_S0h1QTO98AB9OVc4sP82oNLf4IQRGn7ZXo0s_Qnq1oY_TWOWGUj1qPb2M5LmKDHGo4tXfc_p1A0VA7gNnocUTkxcBjt94DyCyC72ZFr_2-SLvAbisLDkpWddjMoQs1vQFA18za-hWl-_HkcJPn9Fv79wSOgGTs4XYgyN8UXIULRLaa7JVSkwrFJ5jiwpWDShyQmrTOwcPASjgtKzle5B4g7TaL359PTYvsRl1SoG7pjS22eTaw8Y0WMH0xQvJimtLvBTWUBZc1nuDGwtfFCf1zKhVlquovRYIOzO0pSN8lNt-WZljpfgxgPXjoRL-6K32muuxX2AfqQQuEQ',
          expires_in: 43199,
        }),
      );
    } catch (e) {
      message = e.message;
    }

    expect(message).toBeFalsy();
  });

  it('check logout to succeed', async () => {
    let message = false;
    try {
      const logoutSuccess = require('./authReducer').logoutSuccess;
      const store = require('../store/store').default;
      store.dispatch(logoutSuccess());
    } catch (e) {
      message = e.message;
    }

    expect(message).toBeFalsy();
  });
});
