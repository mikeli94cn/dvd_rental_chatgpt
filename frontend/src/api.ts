export interface Page<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}
export interface Account {
  id: number;
  email: string;
  role: "CUSTOMER" | "STAFF" | "ADMIN";
}
export interface Genre {
  id: number;
  name: string;
}
export interface Movie {
  id: number;
  title: string;
  synopsis: string;
  releaseYear: number;
  dailyRate: number;
  genres: Genre[];
}
export interface Profile {
  id: number;
  email: string;
  name: string;
  phone: string | null;
}
export type CopyStatus =
  | "AVAILABLE"
  | "RENTED"
  | "MAINTENANCE"
  | "DAMAGED"
  | "LOST"
  | "RETIRED";
export interface Copy {
  id: number;
  movieId: number;
  movieTitle: string;
  barcode: string;
  status: CopyStatus;
}
export interface RentalItem {
  id: number;
  orderId: number;
  copyId: number;
  movieTitle: string;
  dueDate: string;
  returnedAt: string | null;
  status: "ACTIVE" | "RETURNED" | "LOST";
  dailyRate: number;
  overdueDailyRate: number;
  rentalCharge: number;
  overdueCharge: number;
  overdueEstimate: boolean;
}
export interface Rental {
  id: number;
  customerId: number;
  createdAt: string;
  status: string;
  currency: string;
  rentalTotal: number;
  overdueTotal: number;
  items: RentalItem[];
}
export interface Dashboard {
  totalCopies: number;
  availableCopies: number;
  customers: number;
  activeItems: number;
  overdueItems: number;
}
export class ApiError extends Error {
  constructor(
    public status: number,
    message: string,
  ) {
    super(message);
  }
}
async function read<T>(response: Response): Promise<T> {
  if (!response.ok) {
    const problem = await response.json().catch(() => ({}));
    throw new ApiError(
      response.status,
      problem.detail || problem.title || `Request failed (${response.status})`,
    );
  }
  if (response.status === 204) return undefined as T;
  return response.json() as Promise<T>;
}
export async function api<T>(
  path: string,
  method = "GET",
  body?: unknown,
): Promise<T> {
  const headers: Record<string, string> = {};
  if (method !== "GET") {
    const csrf = await fetch("/api/v1/auth/csrf", {
      credentials: "same-origin",
      cache: "no-store",
    }).then(read<{ headerName: string; token: string }>);
    headers[csrf.headerName] = csrf.token;
    headers["Content-Type"] = "application/json";
  }
  return fetch("/api/v1" + path, {
    method,
    credentials: "same-origin",
    cache: "no-store",
    headers,
    body: body === undefined ? undefined : JSON.stringify(body),
  }).then(read<T>);
}
export async function login(email: string, password: string) {
  const csrf = await api<{ headerName: string; token: string }>("/auth/csrf");
  await fetch("/api/v1/auth/login", {
    method: "POST",
    credentials: "same-origin",
    headers: {
      "Content-Type": "application/x-www-form-urlencoded",
      [csrf.headerName]: csrf.token,
    },
    body: new URLSearchParams({ email, password }),
  }).then(read<void>);
}
export const money = (amount: number) =>
  new Intl.NumberFormat("en-GB", { style: "currency", currency: "GBP" }).format(
    amount,
  );
export const errorMessage = (error: unknown) =>
  error instanceof Error
    ? error.message
    : "Something went wrong. Please try again.";
