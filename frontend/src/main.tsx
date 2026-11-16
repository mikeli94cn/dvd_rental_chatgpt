import { createRoot } from "react-dom/client";
import {
  BrowserRouter,
  Link,
  NavLink,
  Outlet,
  Route,
  Routes,
  useNavigate,
} from "react-router";
import { App, Badge, Button, ConfigProvider, Result } from "antd";
import { ShoppingOutlined } from "@ant-design/icons";
import { api, errorMessage } from "./api";
import { Protected, Provider, useSession } from "./state";
import {
  AccountPage,
  AuthPage,
  Cart,
  Catalog,
  Confirmation,
  Home,
  MovieDetails,
  MyRentals,
} from "./pages";
import {
  CustomersAdmin,
  DashboardPage,
  GenresAdmin,
  InventoryAdmin,
  MoviesAdmin,
  OverdueAdmin,
  StaffAccountsAdmin,
  StaffNav,
  StaffRentals,
} from "./admin";
import "./style.css";

function Shell() {
  const { user, loading, refreshUser, cart, setCart } = useSession();
  const { message } = App.useApp();
  const navigate = useNavigate();
  return (
    <div className="site-shell">
      <a className="skip-link" href="#main">
        Skip to content
      </a>
      <header className="site-header">
        <Link to="/" className="brand">
          <span className="brand-mark">▶</span>
          <span>
            DVD<span className="brand-sub">RENTAL SYSTEM</span>
          </span>
        </Link>
        <nav aria-label="Main navigation">
          <NavLink to="/catalog">The collection</NavLink>
          {user?.role === "CUSTOMER" && (
            <>
              <NavLink to="/rentals">My rentals</NavLink>
              <NavLink to="/account">Account</NavLink>
            </>
          )}
          {user && user.role !== "CUSTOMER" && (
            <NavLink to="/staff">Staff workspace</NavLink>
          )}
        </nav>
        <div className="header-actions">
          {user?.role === "CUSTOMER" && (
            <Link
              to="/cart"
              aria-label={`Rental basket with ${cart.length} DVDs`}
            >
              <Badge count={cart.length} color="#a85e38">
                <ShoppingOutlined style={{ fontSize: 23 }} />
              </Badge>
            </Link>
          )}
          {!loading &&
            (user ? (
              <Button
                onClick={async () => {
                  try {
                    await api("/auth/logout", "POST");
                    setCart([]);
                    await refreshUser();
                    navigate("/");
                  } catch (e) {
                    message.error(errorMessage(e));
                  }
                }}
              >
                Sign out
              </Button>
            ) : (
              <Link to="/login" className="button-link secondary">
                Sign in
              </Link>
            ))}
        </div>
      </header>
      <main id="main">
        <Outlet />
      </main>
      <footer>
        <Link to="/" className="footer-brand">
          DVD Rental System
        </Link>
        <span>For the love of a good film.</span>
        <span>Local collection & returns · GBP</span>
      </footer>
    </div>
  );
}
function StaffLayout() {
  return (
    <Protected staff>
      <StaffNav />
      <Outlet />
    </Protected>
  );
}
function Application() {
  return (
    <ConfigProvider
      theme={{
        token: {
          colorPrimary: "#26534d",
          colorInfo: "#26534d",
          colorBgLayout: "#f6f4ed",
          colorText: "#182c2c",
          borderRadius: 8,
          fontFamily: "Inter, ui-sans-serif, system-ui, sans-serif",
        },
      }}
    >
      <App>
        <BrowserRouter>
          <Provider>
            <Routes>
              <Route element={<Shell />}>
                <Route index element={<Home />} />
                <Route path="catalog" element={<Catalog />} />
                <Route path="movies/:id" element={<MovieDetails />} />
                <Route path="login" element={<AuthPage />} />
                <Route path="register" element={<AuthPage register />} />
                <Route
                  path="cart"
                  element={
                    <Protected>
                      <Cart />
                    </Protected>
                  }
                />
                <Route
                  path="rentals"
                  element={
                    <Protected>
                      <MyRentals />
                    </Protected>
                  }
                />
                <Route
                  path="rentals/:id/confirmation"
                  element={
                    <Protected>
                      <Confirmation />
                    </Protected>
                  }
                />
                <Route
                  path="account"
                  element={
                    <Protected>
                      <AccountPage />
                    </Protected>
                  }
                />
                <Route path="staff" element={<StaffLayout />}>
                  <Route index element={<DashboardPage />} />
                  <Route path="rentals" element={<StaffRentals />} />
                  <Route path="inventory" element={<InventoryAdmin />} />
                  <Route path="customers" element={<CustomersAdmin />} />
                  <Route path="overdue" element={<OverdueAdmin />} />
                  <Route path="movies" element={<MoviesAdmin />} />
                  <Route path="genres" element={<GenresAdmin />} />
                  <Route
                    path="accounts"
                    element={
                      <Protected admin>
                        <StaffAccountsAdmin />
                      </Protected>
                    }
                  />
                </Route>
                <Route
                  path="*"
                  element={
                    <Result
                      status="404"
                      title="Page not found"
                      extra={<Link to="/">Back home</Link>}
                    />
                  }
                />
              </Route>
            </Routes>
          </Provider>
        </BrowserRouter>
      </App>
    </ConfigProvider>
  );
}
createRoot(document.getElementById("root")!).render(<Application />);
