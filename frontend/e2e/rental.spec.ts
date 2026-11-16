import { test, expect } from "@playwright/test";

test("administrator catalog setup, customer rental, and staff-confirmed return", async ({
  browser,
}) => {
  const adminEmail = process.env.E2E_ADMIN_EMAIL;
  const adminPassword = process.env.E2E_ADMIN_PASSWORD;
  if (!adminEmail || !adminPassword)
    throw new Error(
      "Set E2E_ADMIN_EMAIL and E2E_ADMIN_PASSWORD for a disposable local dev stack.",
    );
  const errors: string[] = [];
  const admin = await browser.newPage();
  admin.on("pageerror", (e) => errors.push(e.message));
  await admin.goto("/login");
  await admin.getByLabel("Email address").fill(adminEmail);
  await admin.getByLabel("Password", { exact: true }).fill(adminPassword);
  await admin
    .locator("main")
    .getByRole("button", { name: "Sign in", exact: true })
    .click();
  await expect(admin.getByRole("button", { name: "Sign out" })).toBeVisible();
  await admin.goto("/staff/movies");
  const title = `Film night ${Date.now()}`;
  await admin.getByRole("button", { name: "Add movie", exact: true }).click();
  const dialog = admin.getByRole("dialog");
  await dialog.getByLabel("Title", { exact: true }).fill(title);
  await dialog
    .getByLabel("Synopsis")
    .fill("A film selected for the browser verification.");
  const movieSaved = admin.waitForResponse(
    (r) =>
      r.url().endsWith("/api/v1/staff/movies") &&
      r.request().method() === "POST",
  );
  await dialog.getByRole("button", { name: "OK", exact: true }).click();
  const movie = await (await movieSaved).json();
  await expect(dialog).not.toBeVisible();
  await admin.goto("/staff/inventory");
  await admin.getByLabel("Movie ID", { exact: true }).fill(String(movie.id));
  await admin.getByLabel("Barcode").fill(`BROWSER-${Date.now()}`);
  await admin.getByRole("button", { name: "Add copy", exact: true }).click();
  await expect(admin.getByText("DVD copy added.")).toBeVisible();

  const customer = await browser.newPage({
    viewport: { width: 390, height: 844 },
  });
  customer.on("pageerror", (e) => errors.push(e.message));
  await customer.goto("/register");
  await customer.getByLabel("Your name").fill("Browser Customer");
  await customer
    .getByLabel("Email address")
    .fill(`browser-${Date.now()}@example.test`);
  await customer
    .getByLabel("Password", { exact: true })
    .fill("Customer-browser-123");
  await customer
    .getByRole("button", { name: "Create account", exact: true })
    .click();
  await expect(
    customer.getByRole("button", { name: "Sign out" }),
  ).toBeVisible();
  await customer.goto(`/movies/${movie.id}`);
  await customer.getByRole("button", { name: "Add a DVD to basket" }).click();
  await customer.getByRole("link", { name: /View basket/ }).click();
  await expect(customer.getByRole("heading", { name: title })).toBeVisible();
  await customer.getByRole("button", { name: "Confirm rental" }).click();
  await expect(
    customer.getByRole("heading", { name: "You're all set." }),
  ).toBeVisible();
  const orderId = customer.url().match(/rentals\/(\d+)\/confirmation/)![1];
  await expect(
    customer.getByText("ACTIVE", { exact: true }).first(),
  ).toBeVisible();
  await customer.screenshot({
    path: "test-results/customer-confirmation.png",
    fullPage: true,
  });
  expect(
    await customer.evaluate(
      () => document.documentElement.scrollWidth <= window.innerWidth,
    ),
  ).toBe(true);

  await admin.goto("/staff/rentals");
  const card = admin
    .locator(".ant-card")
    .filter({ hasText: `Rental #${orderId}` });
  await card.getByRole("button", { name: "Inspect & return" }).click();
  await admin
    .getByRole("dialog")
    .getByRole("button", { name: "OK", exact: true })
    .click();
  await expect(card.getByText("CLOSED", { exact: true })).toBeVisible();
  await customer.goto("/rentals");
  await expect(customer.getByText("CLOSED", { exact: true })).toBeVisible();
  await admin.screenshot({
    path: "test-results/staff-returns.png",
    fullPage: true,
  });
  expect(errors).toEqual([]);
  await customer.close();
  await admin.close();
});
