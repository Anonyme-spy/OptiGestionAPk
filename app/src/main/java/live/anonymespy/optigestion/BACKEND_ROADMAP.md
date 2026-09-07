# OptiGestion — Backend Roadmap (Node.js + MariaDB)

Not implemented yet — this is the plan for the next phase, so today's
client-side changes (data shapes in `UserModels.kt`, the `AppRepository`
structure) don't have to be reworked later. Written so it applies equally
to the Android client, a future web client and the future Python client.

## 1. Why a backend at all

Today, `AppRepository` is local-only: one device, one SharedPreferences
file, no accounts. Moving to accounts + cross-device sync means the
source of truth moves from the device to the server; the device becomes
a cache. That's the one architectural shift everything else follows from.

## 2. Account model

Two account types, matching what you described:

- **Particulier** — one user, one workspace. Simple.
- **Entreprise** — one company, several users, three roles:
  - **Admin** — full read/write: cost centers, budgets, users, company settings.
  - **RH** — scoped to payroll/labor-related cost centers and employee
    expense approvals; not full financial visibility.
  - **Employé** — can submit entries (e.g. expense claims) for their own
    cost center, read-only on company-wide dashboards (or no access to
    them at all, depending on how strict you want this).

`UserModels.kt` (added today) mirrors this: `AccountType`, `EnterpriseRole`,
`User`, `Company`. Nothing reads/writes them yet.

## 3. Suggested MariaDB schema

```sql
CREATE TABLE users (
  id CHAR(36) PRIMARY KEY,
  email VARCHAR(255) UNIQUE NOT NULL,
  password_hash VARCHAR(255) NOT NULL,
  display_name VARCHAR(120) NOT NULL,
  account_type ENUM('PARTICULIER','ENTREPRISE') NOT NULL,
  language ENUM('fr','en') NOT NULL DEFAULT 'fr',
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE companies (
  id CHAR(36) PRIMARY KEY,
  name VARCHAR(160) NOT NULL,
  owner_user_id CHAR(36) NOT NULL REFERENCES users(id)
);

-- Links a user to a company with a role. A PARTICULIER user has no row here.
CREATE TABLE company_memberships (
  user_id CHAR(36) NOT NULL REFERENCES users(id),
  company_id CHAR(36) NOT NULL REFERENCES companies(id),
  role ENUM('ADMIN','RH','EMPLOYE') NOT NULL,
  PRIMARY KEY (user_id, company_id)
);

-- Every "workspace" (a particulier user OR a company) owns its own data.
-- owner_type + owner_id is simpler than two near-identical table sets.
CREATE TABLE cost_centers (
  id CHAR(36) PRIMARY KEY,
  owner_type ENUM('USER','COMPANY') NOT NULL,
  owner_id CHAR(36) NOT NULL,
  code VARCHAR(20) NOT NULL,
  name VARCHAR(120) NOT NULL,
  icon VARCHAR(20) NOT NULL,
  monthly_budget DECIMAL(14,2) NOT NULL DEFAULT 0,
  UNIQUE KEY (owner_type, owner_id, code)
);

CREATE TABLE budget_categories (
  id CHAR(36) PRIMARY KEY,
  owner_type ENUM('USER','COMPANY') NOT NULL,
  owner_id CHAR(36) NOT NULL,
  name VARCHAR(120) NOT NULL,
  icon VARCHAR(20) NOT NULL,
  budget_amount DECIMAL(14,2) NOT NULL,
  actual_amount DECIMAL(14,2) NOT NULL DEFAULT 0
);

CREATE TABLE entries (
  id CHAR(36) PRIMARY KEY,
  owner_type ENUM('USER','COMPANY') NOT NULL,
  owner_id CHAR(36) NOT NULL,
  created_by_user_id CHAR(36) NOT NULL REFERENCES users(id),
  category VARCHAR(160) NOT NULL,
  icon VARCHAR(20) NOT NULL,
  amount DECIMAL(14,2) NOT NULL,
  is_credit BOOLEAN NOT NULL DEFAULT FALSE,
  cost_center_code VARCHAR(20),
  status ENUM('APPROVED','PENDING','REJECTED') NOT NULL DEFAULT 'PENDING',
  timestamp_millis BIGINT NOT NULL,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE settings (
  owner_type ENUM('USER','COMPANY') NOT NULL,
  owner_id CHAR(36) NOT NULL,
  currency VARCHAR(10) NOT NULL DEFAULT 'USD',
  cash_on_hand DECIMAL(14,2) NOT NULL DEFAULT 0,
  theme_mode VARCHAR(10) NOT NULL DEFAULT 'SYSTEM',
  PRIMARY KEY (owner_type, owner_id)
);
```

This maps almost 1:1 onto `SheetEntry`, `CostCenter`, `BudgetCategoryUi` —
the client models barely change, they just get an `id`/`ownerId` from the
server instead of being generated locally with `UUID.randomUUID()`.

## 4. API shape (Express + JWT)

- `POST /auth/register` — `{ email, password, displayName, accountType }`
- `POST /auth/login` — returns `{ accessToken, refreshToken, user }`
- `POST /auth/refresh`
- `GET /me` — current user + company + role
- `POST /companies` — create a company (becomes ADMIN automatically)
- `POST /companies/:id/invite` — invite by email with a role
- `GET /workspace/entries` / `POST` / `PUT /:id` / `DELETE /:id`
- `GET /workspace/cost-centers` / `POST` / `PUT /:id` / `DELETE /:id`
- `GET /workspace/budget-categories` / ...
- `GET /workspace/settings` / `PUT`
- `GET /workspace/export.csv`

"Workspace" here means: resolve `owner_type`/`owner_id` server-side from
the JWT (particulier → their own `user_id`; enterprise → their
`company_id`), so the client never has to know or send it. Role
middleware then decides what each endpoint allows per `EnterpriseRole`
(e.g. `EMPLOYE` can `POST /workspace/entries` but not `DELETE`, and
can't touch `/workspace/cost-centers` at all).

## 5. Auth on the Android client

- `androidx.security:security-crypto` for storing the refresh token
  (EncryptedSharedPreferences instead of the plain SharedPreferences
  `AppRepository` uses today).
- Retrofit + OkHttp for the API calls, with an interceptor that attaches
  `Authorization: Bearer <accessToken>` and refreshes on 401.
- `AppRepository` splits into two responsibilities: a local cache (as
  today, useful for offline) and a `SyncEngine` that reconciles local
  changes with the server — last-write-wins to start, since these are
  low-conflict-probability, low-frequency edits (not a collaborative
  document).

## 6. Why this also works for the Python client later

None of the above is Android-specific — it's a plain REST API over
JWT and a relational schema. A Python client (CLI, desktop, or a second
web frontend) talks to the exact same endpoints. The only genuinely
platform-specific piece is *where* the refresh token is stored securely.

## 7. Suggested build order

1. `users` + `auth` endpoints, particulier accounts only, no company
   concept yet — get login working end-to-end first.
2. Move `entries`/`cost_centers`/`budget_categories`/`settings` behind
   auth, particulier-only. Android client switches from
   SharedPreferences-only to "cache + sync".
3. Add `companies` + `company_memberships` + role middleware.
4. Add the Entreprise onboarding flow (create company vs join via
   invite) and role-gated UI on the client (e.g. EMPLOYE doesn't see
   the Cost Centers tab at all).
5. Only then: Python client, reusing the same API.

## 8. What NOT to build yet

- Real-time sync (websockets) — polling / pull-to-refresh is enough at
  this scale and much simpler.
- Multi-currency conversion — out of scope until asked for.
- Fine-grained per-cost-center permissions beyond the three roles above
  — start coarse, narrow later if actually needed.
