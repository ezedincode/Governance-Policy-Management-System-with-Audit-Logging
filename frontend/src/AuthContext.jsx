import { createContext, useContext, useEffect, useMemo, useState } from 'react';
import api, { tokenStorage, decodeJwt } from './api';

const AuthContext = createContext(null);
export const useAuth = () => useContext(AuthContext);

export function AuthProvider({ children }) {
  const [token, setToken] = useState(() => tokenStorage.get());
  const [user, setUser] = useState(null);

  useEffect(() => {
    if (token) {
      const claims = decodeJwt(token);
      setUser(claims ? { username: claims.sub, role: claims.role } : null);
    } else {
      setUser(null);
    }
  }, [token]);

  const login = async ({ username, password }) => {
    const { data } = await api.post('/auth/login', { username, password });
    tokenStorage.set(data.accessToken);
    setToken(data.accessToken);
    return data;
  };

  const register = async (payload) => {
    const { data } = await api.post('/auth/register', payload);
    return data;
  };

  const logout = () => {
    tokenStorage.clear();
    setToken(null);
  };

  const value = useMemo(
    () => ({ token, user, isAuthenticated: !!token, login, register, logout }),
    [token, user]
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}
