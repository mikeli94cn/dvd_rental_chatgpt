import {
  createContext,
  useContext,
  useEffect,
  useState,
  type ReactNode,
} from "react";
import { Navigate, useLocation } from "react-router";
import { Alert, Button, Spin } from "antd";
import { api, ApiError, type Account, type Movie, errorMessage } from "./api";

export interface CartItem {
  copyId: number;
  movie: Movie;
}
const Context = createContext<{
  user: Account | null;
  loading: boolean;
  refreshUser: () => Promise<void>;
  cart: CartItem[];
  setCart: React.Dispatch<React.SetStateAction<CartItem[]>>;
} | null>(null);
export function Provider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<Account | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [cart, setCart] = useState<CartItem[]>([]);
  async function refreshUser() {
    const account = await api<Account>("/auth/me").catch((e) => {
      if (e instanceof ApiError && e.status === 401) return null;
      throw e;
    });
    setUser(account);
    setError("");
  }
  useEffect(() => {
    refreshUser()
      .catch((e) => setError(errorMessage(e)))
      .finally(() => setLoading(false));
  }, []);
  if (error)
    return (
      <div className="auth-error">
        <Alert
          title="Cannot connect to the rental service"
          description={error}
          type="error"
          showIcon
          action={
            <Button
              onClick={() => {
                setLoading(true);
                refreshUser()
                  .catch((e) => setError(errorMessage(e)))
                  .finally(() => setLoading(false));
              }}
            >
              Try again
            </Button>
          }
        />
      </div>
    );
  return (
    <Context.Provider value={{ user, loading, refreshUser, cart, setCart }}>
      {children}
    </Context.Provider>
  );
}
export function useSession() {
  const value = useContext(Context);
  if (!value) throw new Error("Provider is missing");
  return value;
}
export function Protected({
  children,
  staff = false,
  admin = false,
}: {
  children: ReactNode;
  staff?: boolean;
  admin?: boolean;
}) {
  const { user, loading } = useSession();
  const location = useLocation();
  if (loading) return <Spin className="page-spinner" />;
  if (!user)
    return <Navigate to="/login" state={{ next: location.pathname }} replace />;
  if (
    (admin && user.role !== "ADMIN") ||
    (staff && user.role === "CUSTOMER") ||
    (!staff && !admin && user.role !== "CUSTOMER")
  ) {
    return (
      <Alert
        type="warning"
        title="This page is not available for your account role."
        showIcon
      />
    );
  }
  return children;
}
export function useData<T>(path: string) {
  const [data, setData] = useState<T>();
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [version, setVersion] = useState(0);
  useEffect(() => {
    let current = true;
    setLoading(true);
    setError("");
    api<T>(path)
      .then((value) => {
        if (current) setData(value);
      })
      .catch((e) => {
        if (current) setError(errorMessage(e));
      })
      .finally(() => {
        if (current) setLoading(false);
      });
    return () => {
      current = false;
    };
  }, [path, version]);
  return { data, loading, error, reload: () => setVersion((v) => v + 1) };
}
export function DataState({
  loading,
  error,
  reload,
  children,
}: {
  loading: boolean;
  error: string;
  reload: () => void;
  children: ReactNode;
}) {
  if (loading)
    return (
      <div className="loading">
        <Spin tip="Loading" />
      </div>
    );
  if (error)
    return (
      <Alert
        title="Unable to load this page"
        description={error}
        type="error"
        showIcon
        action={<Button onClick={reload}>Retry</Button>}
      />
    );
  return children;
}
