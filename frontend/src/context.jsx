import { createContext, useContext, useEffect, useState } from "react";
import { api, resetCsrf } from "./services/community";
const AppContext = createContext();
export const useApp = () => useContext(AppContext);
export function Provider({ children }) {
  const [profile, setProfile] = useState(null),
    [posts, setPosts] = useState([]),
    [loading, setLoading] = useState(true),
    [error, setError] = useState("");
  useEffect(() => {
    const expired = () => {
      setProfile(null);
      resetCsrf();
    };
    window.addEventListener("session-expired", expired);
    return () => window.removeEventListener("session-expired", expired);
  }, []);
  const refreshPosts = () => api("/posts").then(setPosts);
  const refreshProfile = (silent = false) =>
    api("/me", { silent }).then(setProfile);
  useEffect(() => {
    Promise.all([
      refreshProfile(true).catch((e) => {
        if (e.status !== 401) setError(e.message);
      }),
      refreshPosts().catch((e) => setError(e.message)),
    ]).finally(() => setLoading(false));
  }, []);
  const login = async (data) => {
    setProfile(
      await api("/auth/login", { method: "POST", body: JSON.stringify(data) }),
    );
    setError("");
  };
  const clearSession = () => {
    resetCsrf();
    setProfile(null);
  };
  const logout = async () => {
    await api("/auth/logout", { method: "POST" });
    resetCsrf();
    setProfile(null);
  };
  return (
    <AppContext.Provider
      value={{
        profile,
        posts,
        loading,
        error,
        loggedIn: !!profile,
        login,
        logout,
        clearSession,
        refreshPosts,
        refreshProfile,
      }}
    >
      {error && (
        <p role="alert" className="alert">
          {error}
        </p>
      )}
      {children}
    </AppContext.Provider>
  );
}
