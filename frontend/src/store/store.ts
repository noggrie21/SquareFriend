// redux 컴포넌트
import { configureStore } from '@reduxjs/toolkit';
import userReducer from './user';
import noteReducer from './note';
import omrReducer from './omr';
import authReducer from './auth';

// 만든 reducer 추가해주기
export const store = configureStore({
  reducer: {
    user: userReducer,
    note: noteReducer,
    omr: omrReducer,
    auth: authReducer,
  },
});

export type RootState = ReturnType<typeof store.getState>;
