export const requestConfig = {
  timeout: 15000,
  credentials: 'include',
  errorConfig: {
    errorHandler(error: unknown) {
      throw error;
    },
  },
};
