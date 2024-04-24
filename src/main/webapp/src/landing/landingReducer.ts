import { createSlice } from 'redux-starter-kit';

export interface LandingState {
  email: any;
  state: any;
  error: any | null;
}

const initialState: LandingState = {
  email: null,
  state: null,
  error: null,
};

const landingSlice = createSlice<LandingState>({
  initialState,
  reducers: {
    saveEmailSuccess: (state, action) => {
      state.state = action.payload.status;
      state.error = undefined;
    },
    emailError: (state, action) => {
      state.state = undefined;
      state.error = action.payload;
    },
  },
});

// Extract the action creators object and the reducer
const { actions, reducer } = landingSlice;
// Extract and export each action creator by name
export const { saveEmailSuccess, emailError } = actions;
// Export the reducer, either as a default or named export
export default reducer;
