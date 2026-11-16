import { useState } from "react";
import { NavLink } from "react-router";
import {
  App,
  Button,
  Card,
  Form,
  Input,
  InputNumber,
  Modal,
  Pagination,
  Popconfirm,
  Select,
  Space,
  Statistic,
  Table,
} from "antd";
import {
  api,
  errorMessage,
  money,
  type Copy,
  type CopyStatus,
  type Dashboard,
  type Genre,
  type Movie,
  type Page,
  type Profile,
  type Rental,
  type RentalItem,
} from "./api";
import { DataState, useData, useSession } from "./state";
import { PageHeading, RentalCards } from "./pages";

export function StaffNav() {
  const { user } = useSession();
  return (
    <nav className="staff-nav" aria-label="Staff navigation">
      <NavLink end to="/staff">
        Overview
      </NavLink>
      <NavLink to="/staff/rentals">Rentals & returns</NavLink>
      <NavLink to="/staff/inventory">Inventory</NavLink>
      <NavLink to="/staff/customers">Customers</NavLink>
      <NavLink to="/staff/overdue">Overdue</NavLink>
      <NavLink to="/staff/movies">Movies</NavLink>
      <NavLink to="/staff/genres">Genres</NavLink>
      {user?.role === "ADMIN" && (
        <NavLink to="/staff/accounts">Staff accounts</NavLink>
      )}
    </nav>
  );
}
export function DashboardPage() {
  const result = useData<Dashboard>("/staff/reports/dashboard");
  return (
    <>
      <PageHeading
        label="AT THE COUNTER"
        title="Operations overview."
        description="A current view of the collection and rental activity."
      />
      <DataState {...result}>
        {result.data && (
          <div className="stats-grid">
            {Object.entries({
              "DVD copies": result.data.totalCopies,
              "Available copies": result.data.availableCopies,
              Customers: result.data.customers,
              "Active rentals": result.data.activeItems,
              "Overdue DVDs": result.data.overdueItems,
            }).map(([title, value]) => (
              <Card key={title}>
                <Statistic title={title} value={value} />
              </Card>
            ))}
          </div>
        )}
      </DataState>
    </>
  );
}
export function MoviesAdmin() {
  const [page, setPage] = useState(0);
  const result = useData<Page<Movie>>(`/movies?page=${page}`);
  const genres = useData<Page<Genre>>("/genres");
  const [editing, setEditing] = useState<Movie | "new" | null>(null);
  const [busy, setBusy] = useState(false);
  const [form] = Form.useForm();
  const { message } = App.useApp();
  function edit(movie: Movie | "new") {
    setEditing(movie);
    form.resetFields();
    form.setFieldsValue(
      movie === "new"
        ? {
            dailyRate: 2.5,
            releaseYear: new Date().getFullYear(),
            genreIds: [],
          }
        : { ...movie, genreIds: movie.genres.map((g) => g.id) },
    );
  }
  async function save(values: unknown) {
    setBusy(true);
    try {
      await api(
        editing === "new"
          ? "/staff/movies"
          : `/staff/movies/${(editing as Movie).id}`,
        editing === "new" ? "POST" : "PUT",
        values,
      );
      message.success("Movie saved.");
      setEditing(null);
      result.reload();
    } catch (e) {
      message.error(errorMessage(e));
    } finally {
      setBusy(false);
    }
  }
  return (
    <>
      <PageHeading label="COLLECTION MANAGEMENT" title="Movies." />
      <Button type="primary" onClick={() => edit("new")}>
        Add movie
      </Button>
      <DataState {...result}>
        <Table<Movie>
          rowKey="id"
          dataSource={result.data?.content}
          scroll={{ x: 650 }}
          pagination={{
            current: page + 1,
            pageSize: 20,
            total: result.data?.totalElements,
            showSizeChanger: false,
            onChange: (v) => setPage(v - 1),
          }}
          columns={[
            { title: "ID", dataIndex: "id" },
            { title: "Title", dataIndex: "title" },
            { title: "Year", dataIndex: "releaseYear" },
            { title: "Daily rate", render: (_, m) => money(m.dailyRate) },
            {
              title: "Actions",
              render: (_, movie) => (
                <Space>
                  <Button onClick={() => edit(movie)}>Edit</Button>
                  <Popconfirm
                    title="Delete this movie?"
                    description="Movies linked to inventory cannot be deleted."
                    onConfirm={async () => {
                      try {
                        await api(`/staff/movies/${movie.id}`, "DELETE");
                        result.reload();
                      } catch (e) {
                        message.error(errorMessage(e));
                      }
                    }}
                  >
                    <Button danger>Delete</Button>
                  </Popconfirm>
                </Space>
              ),
            },
          ]}
        />
      </DataState>
      <Modal
        title={editing === "new" ? "Add movie" : "Edit movie"}
        open={editing !== null}
        onCancel={() => setEditing(null)}
        onOk={() => form.submit()}
        confirmLoading={busy}
      >
        <Form form={form} layout="vertical" onFinish={save}>
          <Form.Item
            name="title"
            label="Title"
            rules={[{ required: true, whitespace: true }, { max: 200 }]}
          >
            <Input />
          </Form.Item>
          <Form.Item
            name="synopsis"
            label="Synopsis"
            rules={[{ required: true, whitespace: true }, { max: 10000 }]}
          >
            <Input.TextArea rows={4} />
          </Form.Item>
          <Form.Item
            name="releaseYear"
            label="Release year"
            rules={[{ required: true }]}
          >
            <InputNumber min={1888} max={2200} precision={0} />
          </Form.Item>
          <Form.Item
            name="dailyRate"
            label="Daily rental price (GBP)"
            rules={[{ required: true }]}
          >
            <InputNumber min={0} max={99999999.99} precision={2} />
          </Form.Item>
          <Form.Item name="genreIds" label="Genres">
            <Select
              mode="multiple"
              loading={genres.loading}
              options={genres.data?.content.map((g) => ({
                value: g.id,
                label: g.name,
              }))}
            />
          </Form.Item>
        </Form>
      </Modal>
    </>
  );
}
export function GenresAdmin() {
  const [page, setPage] = useState(0);
  const result = useData<Page<Genre>>(`/genres?page=${page}&size=20`);
  const { message } = App.useApp();
  const [form] = Form.useForm();
  const [busy, setBusy] = useState(false);
  const [editing, setEditing] = useState<Genre | null>(null);
  const [editName, setEditName] = useState("");
  return (
    <>
      <PageHeading label="COLLECTION MANAGEMENT" title="Genres." />
      <Form
        form={form}
        layout="inline"
        onFinish={async (values) => {
          setBusy(true);
          try {
            await api("/staff/genres", "POST", values);
            form.resetFields();
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
          label="Genre name"
          rules={[{ required: true, whitespace: true }, { max: 80 }]}
        >
          <Input />
        </Form.Item>
        <Button htmlType="submit" type="primary" loading={busy}>
          Add genre
        </Button>
      </Form>
      <DataState {...result}>
        <Table<Genre>
          rowKey="id"
          dataSource={result.data?.content}
          pagination={{
            current: page + 1,
            pageSize: 20,
            total: result.data?.totalElements,
            showSizeChanger: false,
            onChange: (v) => setPage(v - 1),
          }}
          columns={[
            { title: "Name", dataIndex: "name" },
            {
              title: "Actions",
              render: (_, genre) => (
                <Space>
                  <Button
                    onClick={() => {
                      setEditing(genre);
                      setEditName(genre.name);
                    }}
                  >
                    Rename
                  </Button>
                  <Popconfirm
                    title="Delete genre?"
                    onConfirm={async () => {
                      try {
                        await api(`/staff/genres/${genre.id}`, "DELETE");
                        result.reload();
                      } catch (e) {
                        message.error(errorMessage(e));
                      }
                    }}
                  >
                    <Button danger>Delete</Button>
                  </Popconfirm>
                </Space>
              ),
            },
          ]}
        />
      </DataState>
      <Modal
        title="Rename genre"
        open={!!editing}
        onCancel={() => setEditing(null)}
        onOk={async () => {
          try {
            await api(`/staff/genres/${editing?.id}`, "PUT", {
              name: editName,
            });
            setEditing(null);
            result.reload();
          } catch (e) {
            message.error(errorMessage(e));
          }
        }}
      >
        <Input
          aria-label="Genre name"
          maxLength={80}
          value={editName}
          onChange={(e) => setEditName(e.target.value)}
        />
      </Modal>
    </>
  );
}
const statuses: CopyStatus[] = [
  "AVAILABLE",
  "RENTED",
  "MAINTENANCE",
  "DAMAGED",
  "LOST",
  "RETIRED",
];
export function InventoryAdmin() {
  const [page, setPage] = useState(0);
  const result = useData<Page<Copy>>(`/staff/copies?page=${page}`);
  const { user } = useSession(),
    { message } = App.useApp();
  const [busy, setBusy] = useState(false);
  const [form] = Form.useForm();
  const [change, setChange] = useState<{
    copy: Copy;
    status: CopyStatus;
  } | null>(null);
  return (
    <>
      <PageHeading
        label="PHYSICAL COLLECTION"
        title="DVD inventory."
        description="Every barcode identifies one physical DVD. Rented copies are resolved through staff returns."
      />
      {
        <Card title="Add a physical copy">
          <Form
            form={form}
            layout="inline"
            onFinish={async (values) => {
              setBusy(true);
              try {
                await api("/staff/copies", "POST", values);
                form.resetFields();
                result.reload();
                message.success("DVD copy added.");
              } catch (e) {
                message.error(errorMessage(e));
              } finally {
                setBusy(false);
              }
            }}
          >
            <Form.Item
              name="movieId"
              label="Movie ID"
              rules={[{ required: true }]}
            >
              <InputNumber min={1} precision={0} />
            </Form.Item>
            <Form.Item
              name="barcode"
              label="Barcode"
              rules={[{ required: true, whitespace: true }, { max: 80 }]}
            >
              <Input />
            </Form.Item>
            <Button type="primary" htmlType="submit" loading={busy}>
              Add copy
            </Button>
          </Form>
        </Card>
      }
      <DataState {...result}>
        <Table<Copy>
          rowKey="id"
          dataSource={result.data?.content}
          scroll={{ x: 700 }}
          pagination={{
            current: page + 1,
            pageSize: 20,
            total: result.data?.totalElements,
            showSizeChanger: false,
            onChange: (v) => setPage(v - 1),
          }}
          columns={[
            { title: "ID", dataIndex: "id" },
            { title: "Movie ID", dataIndex: "movieId" },
            { title: "Film", dataIndex: "movieTitle" },
            { title: "Barcode", dataIndex: "barcode" },
            {
              title: "Status",
              render: (_, copy) =>
                !["RENTED", "RETIRED"].includes(copy.status) ? (
                  <Select
                    aria-label={`Status for ${copy.barcode}`}
                    value={copy.status}
                    style={{ width: 165 }}
                    options={statuses
                      .filter((s) => s !== "RENTED")
                      .map((s) => ({ value: s, label: s }))}
                    onChange={(status) => setChange({ copy, status })}
                  />
                ) : (
                  copy.status
                ),
            },
          ]}
        />
      </DataState>
      <Modal
        title="Change inventory status?"
        open={!!change}
        onCancel={() => setChange(null)}
        onOk={async () => {
          try {
            await api(`/staff/copies/${change?.copy.id}/status`, "PUT", {
              status: change?.status,
            });
            setChange(null);
            result.reload();
          } catch (e) {
            message.error(errorMessage(e));
          }
        }}
      >
        <p>
          DVD #{change?.copy.id} will be marked {change?.status}. Retired copies
          cannot be reactivated.
        </p>
      </Modal>
    </>
  );
}
export function StaffRentals() {
  const [page, setPage] = useState(0);
  const result = useData<Page<Rental>>(`/staff/rentals?page=${page}`);
  const [returning, setReturning] = useState<{
    rental: Rental;
    item: RentalItem;
  } | null>(null);
  const [inspection, setInspection] = useState<CopyStatus>("AVAILABLE");
  const [busy, setBusy] = useState(false);
  const { message } = App.useApp();
  return (
    <>
      <PageHeading
        label="AT THE COUNTER"
        title="Rentals & returns."
        description="Inspect each physical DVD before confirming its return."
      />
      <DataState {...result}>
        {result.data && (
          <>
            <RentalCards
              rentals={result.data.content}
              staff
              onReturn={(rental, item) => {
                setReturning({ rental, item });
                setInspection("AVAILABLE");
              }}
            />
            <Pagination
              current={page + 1}
              pageSize={20}
              total={result.data.totalElements}
              showSizeChanger={false}
              onChange={(v) => setPage(v - 1)}
            />
          </>
        )}
      </DataState>
      <Modal
        title="Confirm inspection and resolve rental"
        open={!!returning}
        confirmLoading={busy}
        onCancel={() => setReturning(null)}
        onOk={async () => {
          setBusy(true);
          try {
            await api(
              `/staff/rentals/${returning?.rental.id}/items/${returning?.item.id}/return`,
              "POST",
              { inspectionResult: inspection },
            );
            message.success("Return recorded.");
            setReturning(null);
            result.reload();
          } catch (e) {
            message.error(errorMessage(e));
          } finally {
            setBusy(false);
          }
        }}
      >
        <p>
          {returning?.item.movieTitle} · DVD #{returning?.item.copyId}
        </p>
        <p>
          Only choose available after the DVD passes inspection. A lost DVD
          stays out of circulation.
        </p>
        <Select
          aria-label="Inspection result"
          value={inspection}
          onChange={setInspection}
          style={{ width: "100%" }}
          options={[
            { value: "AVAILABLE", label: "Passed inspection — available" },
            { value: "MAINTENANCE", label: "Needs maintenance" },
            { value: "DAMAGED", label: "Damaged" },
            { value: "LOST", label: "Lost" },
          ]}
        />
      </Modal>
    </>
  );
}
export function CustomersAdmin() {
  const [page, setPage] = useState(0);
  const result = useData<Page<Profile>>(`/staff/customers?page=${page}`);
  const { message } = App.useApp();
  const [editing, setEditing] = useState<Profile | null>(null);
  const [form] = Form.useForm();
  const [busy, setBusy] = useState(false);
  return (
    <>
      <PageHeading label="MEMBERSHIP" title="Customers." />
      <DataState {...result}>
        <Table<Profile>
          rowKey="id"
          scroll={{ x: 600 }}
          dataSource={result.data?.content}
          pagination={{
            current: page + 1,
            pageSize: 20,
            total: result.data?.totalElements,
            showSizeChanger: false,
            onChange: (v) => setPage(v - 1),
          }}
          columns={[
            { title: "ID", dataIndex: "id" },
            { title: "Name", dataIndex: "name" },
            { title: "Email", dataIndex: "email" },
            { title: "Phone", dataIndex: "phone" },
            {
              title: "Actions",
              render: (_, p) => (
                <Button
                  onClick={() => {
                    setEditing(p);
                    form.setFieldsValue(p);
                  }}
                >
                  Edit details
                </Button>
              ),
            },
          ]}
        />
      </DataState>
      <Modal
        title="Edit customer details"
        open={!!editing}
        confirmLoading={busy}
        onCancel={() => setEditing(null)}
        onOk={() => form.submit()}
      >
        <Form
          form={form}
          layout="vertical"
          onFinish={async (values) => {
            setBusy(true);
            try {
              await api(`/staff/customers/${editing?.id}`, "PUT", values);
              setEditing(null);
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
            <Input />
          </Form.Item>
          <Form.Item name="phone" label="Phone" rules={[{ max: 40 }]}>
            <Input />
          </Form.Item>
        </Form>
      </Modal>
    </>
  );
}
export function OverdueAdmin() {
  const [page, setPage] = useState(0);
  const result = useData<Page<RentalItem>>(
    `/staff/reports/overdue?page=${page}`,
  );
  return (
    <>
      <PageHeading
        label="FOLLOW UP"
        title="Overdue DVDs."
        description="Charges shown are estimates until staff confirm a return or loss."
      />
      <DataState {...result}>
        <Table<RentalItem>
          rowKey="id"
          dataSource={result.data?.content}
          scroll={{ x: 600 }}
          pagination={{
            current: page + 1,
            pageSize: 20,
            total: result.data?.totalElements,
            showSizeChanger: false,
            onChange: (v) => setPage(v - 1),
          }}
          columns={[
            { title: "Rental #", dataIndex: "orderId" },
            { title: "DVD #", dataIndex: "copyId" },
            { title: "Film", dataIndex: "movieTitle" },
            { title: "Due", dataIndex: "dueDate" },
            {
              title: "Overdue charge",
              render: (_, i) => money(i.overdueCharge),
            },
          ]}
        />
      </DataState>
    </>
  );
}
export function StaffAccountsAdmin() {
  const [busy, setBusy] = useState(false),
    [form] = Form.useForm();
  const { message } = App.useApp();
  return (
    <>
      <PageHeading
        label="PRIVILEGED ACCESS"
        title="Create staff account."
        description="Only administrators can create accounts with staff access."
      />
      <Card className="profile-card">
        <Form
          form={form}
          layout="vertical"
          onFinish={async (values) => {
            setBusy(true);
            try {
              await api("/admin/staff", "POST", values);
              message.success("Staff account created.");
              form.resetFields();
            } catch (e) {
              message.error(errorMessage(e));
            } finally {
              setBusy(false);
            }
          }}
        >
          <Form.Item
            name="email"
            label="Staff email"
            rules={[{ required: true, type: "email" }, { max: 254 }]}
          >
            <Input type="email" />
          </Form.Item>
          <Form.Item
            name="password"
            label="Initial password"
            rules={[{ required: true, min: 12 }]}
          >
            <Input.Password autoComplete="new-password" />
          </Form.Item>
          <Button type="primary" htmlType="submit" loading={busy}>
            Create staff account
          </Button>
        </Form>
      </Card>
    </>
  );
}
