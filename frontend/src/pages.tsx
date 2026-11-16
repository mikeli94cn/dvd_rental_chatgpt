import { useState } from "react";
import { Link, useLocation, useNavigate, useParams } from "react-router";
import {
  Alert,
  App,
  Button,
  Card,
  Empty,
  Form,
  Input,
  InputNumber,
  Pagination,
  Select,
  Space,
  Table,
  Tag,
} from "antd";
import { ArrowRightOutlined, CheckCircleOutlined, DiscIcon } from "./ui";
import {
  api,
  errorMessage,
  login,
  money,
  type Movie,
  type Page,
  type Profile,
  type Rental,
  type RentalItem,
} from "./api";
import { DataState, useData, useSession } from "./state";

export function Home() {
  return (
    <>
      <section className="hero">
        <div className="hero-text">
          <p className="eyebrow">YOUR NEXT FILM NIGHT STARTS HERE</p>
          <h1>
            Good films.
            <br />
            Real discs.
            <br />
            <em>Yours to discover.</em>
          </h1>
          <p className="hero-description">
            Find a film, reserve a DVD, and collect it from our rental counter.
            A little less scrolling. A little more cinema.
          </p>
          <Link to="/catalog" className="button-link">
            Explore the collection <ArrowRightOutlined />
          </Link>
          <div className="hero-note">
            Physical DVDs · Local collection · Prices in GBP
          </div>
        </div>
        <div className="hero-art" aria-hidden="true">
          <div className="disc">
            <div className="disc-hole" />
          </div>
          <div className="ticket">
            <span>DVD RENTAL SYSTEM</span>
            <strong>
              Make a night
              <br />
              of it.
            </strong>
            <div className="ticket-rule" />
            <span>ADMIT ONE FILM LOVER</span>
          </div>
          <span className="art-caption">THE ORIGINAL WAY TO PRESS PLAY.</span>
        </div>
      </section>
      <section className="steps">
        <div>
          <span>01</span>
          <h3>Find your film</h3>
          <p>Browse the catalog and check physical copy availability.</p>
        </div>
        <div>
          <span>02</span>
          <h3>Choose your time</h3>
          <p>
            Rent for 1–30 days. See the confirmed fee and due date before
            collection.
          </p>
        </div>
        <div>
          <span>03</span>
          <h3>Collect. Watch. Return.</h3>
          <p>
            Collect at the counter and return your disc for staff inspection.
          </p>
        </div>
      </section>
    </>
  );
}

export function Catalog() {
  const [query, setQuery] = useState("");
  const [page, setPage] = useState(0);
  const [sort, setSort] = useState("title");
  const result = useData<Page<Movie>>(
    `/movies?query=${encodeURIComponent(query)}&page=${page}&size=12&sort=${sort}`,
  );
  return (
    <>
      <PageHeading
        label="THE COLLECTION"
        title="Find your next favourite."
        description="Browse our film library. Open a title to see available DVD copies."
      />
      <div className="catalog-controls">
        <Input.Search
          aria-label="Search movie titles"
          placeholder="Search for a film…"
          allowClear
          enterButton="Search"
          onSearch={(v) => {
            setQuery(v);
            setPage(0);
          }}
        />
        <Select
          aria-label="Sort films"
          value={sort}
          onChange={(v) => {
            setSort(v);
            setPage(0);
          }}
          options={[
            { value: "title", label: "Title A–Z" },
            { value: "releaseYear", label: "Release year" },
            { value: "dailyRate", label: "Daily price" },
          ]}
        />
      </div>
      <DataState {...result}>
        {result.data?.content.length ? (
          <>
            <div className="movie-grid">
              {result.data.content.map((movie, i) => (
                <Link
                  key={movie.id}
                  to={`/movies/${movie.id}`}
                  className="movie-link"
                >
                  <article className="movie-card">
                    <div className={`movie-art palette-${i % 4}`}>
                      <span className="movie-year">{movie.releaseYear}</span>
                      <DiscIcon />
                      <strong>{movie.title}</strong>
                      <span>
                        DVD /{" "}
                        {movie.genres.map((g) => g.name).join(" · ") || "FILM"}
                      </span>
                    </div>
                    <div className="movie-meta">
                      <h3>{movie.title}</h3>
                      <span>
                        {money(movie.dailyRate)} <small>/ day</small>
                      </span>
                    </div>
                    <p>{movie.synopsis}</p>
                    <span className="text-link">
                      View film & availability <ArrowRightOutlined />
                    </span>
                  </article>
                </Link>
              ))}
            </div>
            <Pagination
              current={page + 1}
              pageSize={12}
              total={result.data.totalElements}
              showSizeChanger={false}
              onChange={(v) => setPage(v - 1)}
            />
          </>
        ) : (
          <Empty
            description={
              query
                ? "No films match your search."
                : "The collection is empty. Please check back soon."
            }
          />
        )}
      </DataState>
    </>
  );
}

export function MovieDetails() {
  const { id } = useParams();
  const result = useData<Movie>(`/movies/${id}`);
  const copies = useData<Page<{ id: number }>>(`/movies/${id}/availability`);
  const { user, cart, setCart } = useSession();
  const { message } = App.useApp();
  const navigate = useNavigate();
  const available =
    copies.data?.content.filter(
      (copy) => !cart.some((item) => item.copyId === copy.id),
    ) ?? [];
  return (
    <>
      <Link to="/catalog" className="back-link">
        ← Back to collection
      </Link>
      <DataState {...result}>
        {result.data && (
          <div className="detail-grid">
            <div className="movie-art detail-art palette-0">
              <span>{result.data.releaseYear}</span>
              <DiscIcon />
              <strong>{result.data.title}</strong>
              <span>PHYSICAL DVD</span>
            </div>
            <div>
              <p className="eyebrow">{result.data.releaseYear} · DVD</p>
              <h1>{result.data.title}</h1>
              <Space wrap>
                {result.data.genres.map((g) => (
                  <Tag key={g.id}>{g.name}</Tag>
                ))}
              </Space>
              <p className="synopsis">{result.data.synopsis}</p>
              <p className="detail-price">
                {money(result.data.dailyRate)} <small>per day</small>
              </p>
              <DataState {...copies}>
                <Tag color={available.length ? "green" : "default"}>
                  {copies.data?.totalElements ?? 0} copies available
                </Tag>
                <p>
                  Choose a rental period at checkout. Collection and returns are
                  at our physical counter.
                </p>
                <Button
                  type="primary"
                  size="large"
                  disabled={
                    (user?.role !== "CUSTOMER" && !!user) ||
                    !available.length ||
                    cart.length >= 10
                  }
                  onClick={() => {
                    if (!user) {
                      navigate("/login", { state: { next: `/movies/${id}` } });
                      return;
                    }
                    setCart([
                      ...cart,
                      { copyId: available[0].id, movie: result.data! },
                    ]);
                    message.success("DVD added to your rental basket.");
                  }}
                >
                  {!user ? "Sign in to rent" : "Add a DVD to basket"}
                </Button>
                {cart.length > 0 && (
                  <Link to="/cart" className="cart-link">
                    View basket ({cart.length}) →
                  </Link>
                )}
                <p className="muted">
                  Availability is confirmed when you place your rental. Maximum
                  10 copies per order.
                </p>
              </DataState>
            </div>
          </div>
        )}
      </DataState>
    </>
  );
}

export function AuthPage({ register = false }: { register?: boolean }) {
  const [busy, setBusy] = useState(false),
    [error, setError] = useState("");
  const navigate = useNavigate(),
    location = useLocation();
  const { refreshUser, user } = useSession();
  async function submit(values: {
    email: string;
    password: string;
    name?: string;
  }) {
    setBusy(true);
    setError("");
    try {
      if (register) await api("/auth/register", "POST", values);
      await login(values.email, values.password);
      await refreshUser();
      const next = location.state?.next;
      navigate(
        typeof next === "string" &&
          next.startsWith("/") &&
          !next.startsWith("//")
          ? next
          : "/catalog",
        { replace: true },
      );
    } catch (e) {
      setError(errorMessage(e));
    } finally {
      setBusy(false);
    }
  }
  return (
    <div className="auth-card">
      <p className="eyebrow">WELCOME TO THE COLLECTION</p>
      <h1>
        {register ? "Your film nights, sorted." : "Good to see you again."}
      </h1>
      <p>
        {register
          ? "Create an account to rent DVDs and keep track of your films."
          : "Sign in to manage your rentals."}
      </p>
      {user && (
        <Alert type="info" title={`Currently signed in as ${user.email}`} />
      )}
      <Form layout="vertical" onFinish={submit} requiredMark={false}>
        {register && (
          <Form.Item
            label="Your name"
            name="name"
            rules={[{ required: true, whitespace: true }, { max: 120 }]}
          >
            <Input autoComplete="name" />
          </Form.Item>
        )}
        <Form.Item
          label="Email address"
          name="email"
          rules={[{ required: true }, { type: "email" }, { max: 254 }]}
        >
          <Input autoComplete="email" type="email" />
        </Form.Item>
        <Form.Item
          label="Password"
          name="password"
          rules={[
            { required: true },
            ...(register
              ? [
                  { min: 12, message: "Use at least 12 characters." },
                  {
                    validator: (_: unknown, value: string) =>
                      !value || new TextEncoder().encode(value).length <= 72
                        ? Promise.resolve()
                        : Promise.reject(
                            new Error("Use at most 72 UTF-8 bytes."),
                          ),
                  },
                ]
              : []),
          ]}
        >
          <Input.Password
            autoComplete={register ? "new-password" : "current-password"}
          />
        </Form.Item>
        {error && (
          <Alert className="form-error" title={error} type="error" showIcon />
        )}
        <Button
          type="primary"
          htmlType="submit"
          block
          size="large"
          loading={busy}
        >
          {register ? "Create account" : "Sign in"}
        </Button>
      </Form>
      <p className="auth-switch">
        {register ? "Already a member? " : "New to the collection? "}
        <Link to={register ? "/login" : "/register"} state={location.state}>
          {register ? "Sign in" : "Create an account"}
        </Link>
      </p>
    </div>
  );
}

export function Cart() {
  const { cart, setCart } = useSession();
  const [days, setDays] = useState(3);
  const [busy, setBusy] = useState(false),
    [error, setError] = useState("");
  const navigate = useNavigate();
  const estimate = cart.reduce(
    (sum, item) => sum + item.movie.dailyRate * days,
    0,
  );
  async function checkout() {
    setBusy(true);
    setError("");
    try {
      const rental = await api<Rental>("/rentals", "POST", {
        copyIds: cart.map((item) => item.copyId),
        days,
      });
      setCart([]);
      navigate(`/rentals/${rental.id}/confirmation`);
    } catch (e) {
      setError(errorMessage(e));
    } finally {
      setBusy(false);
    }
  }
  return (
    <>
      <PageHeading
        label="READY FOR FILM NIGHT"
        title="Your rental basket."
        description="Collect and return your DVDs at the rental counter."
      />
      {cart.length ? (
        <div className="basket-grid">
          <div>
            {cart.map((item) => (
              <Card key={item.copyId} className="basket-item">
                <div className="between">
                  <div>
                    <h3>{item.movie.title}</h3>
                    <p>
                      {money(item.movie.dailyRate)} per day · DVD copy #
                      {item.copyId}
                    </p>
                  </div>
                  <Button
                    onClick={() =>
                      setCart(cart.filter((c) => c.copyId !== item.copyId))
                    }
                  >
                    Remove
                  </Button>
                </div>
              </Card>
            ))}
          </div>
          <Card title="Rental details">
            <Form layout="vertical">
              <Form.Item label="Rental period (days)">
                <InputNumber
                  min={1}
                  max={30}
                  precision={0}
                  value={days}
                  onChange={(value) => setDays(value ?? 1)}
                />
              </Form.Item>
            </Form>
            <div className="between">
              <span>Estimated rental fee</span>
              <strong>{money(estimate)}</strong>
            </div>
            <p className="muted">
              The server confirms availability, prices, and due dates when you
              place the rental. Overdue fees may apply after the due date.
            </p>
            {error && (
              <Alert type="error" title={error} className="form-error" />
            )}
            <Button
              block
              type="primary"
              size="large"
              loading={busy}
              onClick={checkout}
            >
              Confirm rental
            </Button>
          </Card>
        </div>
      ) : (
        <Empty description="Your basket is ready for a good film.">
          <Link to="/catalog" className="button-link">
            Browse films
          </Link>
        </Empty>
      )}
    </>
  );
}

export function RentalCards({
  rentals,
  staff = false,
  onReturn,
}: {
  rentals: Rental[];
  staff?: boolean;
  onReturn?: (r: Rental, i: RentalItem) => void;
}) {
  if (!rentals.length) return <Empty description="No rentals yet." />;
  return (
    <div className="rental-list">
      {rentals.map((rental) => (
        <Card
          key={rental.id}
          title={
            <Space wrap>
              <span>Rental #{rental.id}</span>
              <Tag color={rental.status === "ACTIVE" ? "gold" : "green"}>
                {rental.status}
              </Tag>
            </Space>
          }
          extra={
            <span>
              {new Date(rental.createdAt).toLocaleDateString("en-GB")}
            </span>
          }
        >
          {staff && <p>Customer #{rental.customerId}</p>}
          <Table<RentalItem>
            rowKey="id"
            pagination={false}
            scroll={{ x: 650 }}
            dataSource={rental.items}
            columns={[
              { title: "Film", dataIndex: "movieTitle" },
              { title: "DVD", dataIndex: "copyId" },
              { title: "Due date", dataIndex: "dueDate" },
              {
                title: "Status",
                render: (_, item) => (
                  <Tag color={item.status === "ACTIVE" ? "gold" : "default"}>
                    {item.status}
                  </Tag>
                ),
              },
              {
                title: "Rental fee",
                render: (_, item) => money(item.rentalCharge),
              },
              {
                title: "Overdue fee",
                render: (_, item) => (
                  <span>
                    {money(item.overdueCharge)}
                    {item.overdueEstimate ? " (est.)" : ""}
                  </span>
                ),
              },
              ...(staff
                ? [
                    {
                      title: "Return",
                      render: (_: unknown, item: RentalItem) =>
                        item.status === "ACTIVE" && (
                          <Button onClick={() => onReturn?.(rental, item)}>
                            Inspect & return
                          </Button>
                        ),
                    },
                  ]
                : []),
            ]}
          />
          <p className="rental-total">
            Rental fee {money(rental.rentalTotal)} · Overdue fee{" "}
            {money(rental.overdueTotal)}
          </p>
        </Card>
      ))}
    </div>
  );
}
export function MyRentals() {
  const [page, setPage] = useState(0);
  const result = useData<Page<Rental>>(`/rentals?page=${page}`);
  return (
    <>
      <PageHeading
        label="YOUR FILM JOURNEY"
        title="My rentals."
        description="Active rentals and past favourites. Staff will confirm your return after inspection."
      />
      <DataState {...result}>
        {result.data && (
          <>
            <RentalCards rentals={result.data.content} />
            <Pagination
              current={page + 1}
              total={result.data.totalElements}
              pageSize={20}
              showSizeChanger={false}
              onChange={(v) => setPage(v - 1)}
            />
          </>
        )}
      </DataState>
    </>
  );
}
export function Confirmation() {
  const { id } = useParams();
  const result = useData<Rental>(`/rentals/${id}`);
  return (
    <DataState {...result}>
      {result.data && (
        <>
          <div className="confirmation">
            <CheckCircleOutlined />
            <p className="eyebrow">YOUR NEXT FILM NIGHT IS BOOKED</p>
            <h1>You're all set.</h1>
            <p>
              Rental #{result.data.id} is confirmed. Please collect your DVDs at
              the counter.
            </p>
            <Link to="/rentals" className="button-link">
              View my rentals
            </Link>
          </div>
          <RentalCards rentals={[result.data]} />
        </>
      )}
    </DataState>
  );
}
export function AccountPage() {
  const result = useData<Profile>("/customers/me");
  const [busy, setBusy] = useState(false);
  const { message } = App.useApp();
  return (
    <>
      <PageHeading label="YOUR MEMBERSHIP" title="Account details." />
      <DataState {...result}>
        {result.data && (
          <Card className="profile-card">
            <p>{result.data.email}</p>
            <Form
              layout="vertical"
              initialValues={result.data}
              onFinish={async (values) => {
                setBusy(true);
                try {
                  await api("/customers/me", "PUT", values);
                  message.success("Profile updated.");
                  result.reload();
                } catch (e) {
                  message.error(errorMessage(e));
                } finally {
                  setBusy(false);
                }
              }}
            >
              <Form.Item
                name="name"
                label="Name"
                rules={[{ required: true, whitespace: true }, { max: 120 }]}
              >
                <Input autoComplete="name" />
              </Form.Item>
              <Form.Item
                name="phone"
                label="Phone (optional)"
                rules={[{ max: 40 }]}
              >
                <Input autoComplete="tel" />
              </Form.Item>
              <Button type="primary" htmlType="submit" loading={busy}>
                Save details
              </Button>
            </Form>
          </Card>
        )}
      </DataState>
    </>
  );
}
export function PageHeading({
  label,
  title,
  description,
}: {
  label: string;
  title: string;
  description?: string;
}) {
  return (
    <div className="page-heading">
      <p className="eyebrow">{label}</p>
      <h1>{title}</h1>
      {description && <p>{description}</p>}
    </div>
  );
}
