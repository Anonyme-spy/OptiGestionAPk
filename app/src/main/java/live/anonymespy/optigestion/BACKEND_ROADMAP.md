# OptiGestion — Final Backend Roadmap (Node.js + MariaDB)

This document is the definitive guide for an AI or Developer to implement the OptiGestion backend. It aligns perfectly with the Android client's data structures and sync logic.

## 1. Core Architecture
- **Tech Stack**: Node.js (TypeScript), Express.js, MariaDB.
- **API Style**: RESTful, JSON-only payloads.
- **Security**: JWT-based authentication (Access + Refresh tokens).
- **Multi-Tenancy**: "Workspace" concept. A workspace is either a `USER_ID` (Personal) or a `COMPANY_ID` (Business).

## 2. MariaDB Schema (Production Ready)

```sql
-- Users Table
CREATE TABLE users (
  id CHAR(36) PRIMARY KEY,
  email VARCHAR(255) UNIQUE,
  password_hash VARCHAR(255),
  display_name VARCHAR(120) NOT NULL,
  phone VARCHAR(20),
  avatar_url VARCHAR(255),
  job_title VARCHAR(120),
  bio TEXT,
  app_logo VARCHAR(50) DEFAULT 'default',
  account_type ENUM('GUEST','PARTICULIER','ENTREPRISE') NOT NULL,
  company_id CHAR(36),
  company_name VARCHAR(160),
  company_industry VARCHAR(100),
  created_at_millis BIGINT NOT NULL,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Companies Table
CREATE TABLE companies (
  id CHAR(36) PRIMARY KEY,
  name VARCHAR(160) NOT NULL,
  registration_number VARCHAR(50),
  industry VARCHAR(100),
  address TEXT,
  website VARCHAR(255),
  tax_id VARCHAR(50),
  logo_url VARCHAR(255),
  owner_id CHAR(36) NOT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (owner_id) REFERENCES users(id)
);

-- Memberships (RBAC)
CREATE TABLE memberships (
  user_id CHAR(36) NOT NULL,
  company_id CHAR(36) NOT NULL,
  role ENUM('OWNER','ADMIN','RH','COMPTABLE','EMPLOYE') NOT NULL,
  PRIMARY KEY (user_id, company_id),
  FOREIGN KEY (user_id) REFERENCES users(id),
  FOREIGN KEY (company_id) REFERENCES companies(id)
);

-- Entries Table (Financial Ledger)
CREATE TABLE entries (
  id CHAR(36) PRIMARY KEY,
  workspace_id CHAR(36) NOT NULL,
  created_by_user_id CHAR(36) NOT NULL,
  category VARCHAR(160) NOT NULL,
  icon VARCHAR(20) NOT NULL,
  amount_ttc DECIMAL(14,2) NOT NULL,
  amount_ht DECIMAL(14,2) NOT NULL,
  tax_rate DECIMAL(5,2) NOT NULL DEFAULT 0,
  is_credit BOOLEAN NOT NULL DEFAULT FALSE,
  cost_center_code VARCHAR(20),
  ledger_account VARCHAR(20),
  status ENUM('APPROVED','PENDING','REJECTED') NOT NULL DEFAULT 'PENDING',
  timestamp_millis BIGINT NOT NULL,
  version INT NOT NULL DEFAULT 1,
  updated_at_millis BIGINT NOT NULL,
  deleted BOOLEAN NOT NULL DEFAULT FALSE,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (created_by_user_id) REFERENCES users(id)
);

-- Cost Centers
CREATE TABLE cost_centers (
  id CHAR(36) PRIMARY KEY,
  workspace_id CHAR(36) NOT NULL,
  created_by_user_id CHAR(36) NOT NULL,
  code VARCHAR(20) NOT NULL,
  name VARCHAR(120) NOT NULL,
  icon VARCHAR(20) NOT NULL,
  monthly_budget DECIMAL(14,2) NOT NULL DEFAULT 0,
  version INT NOT NULL DEFAULT 1,
  updated_at_millis BIGINT NOT NULL,
  deleted BOOLEAN NOT NULL DEFAULT FALSE,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (created_by_user_id) REFERENCES users(id)
);

-- Budgets
CREATE TABLE budgets (
  id CHAR(36) PRIMARY KEY,
  workspace_id CHAR(36) NOT NULL,
  created_by_user_id CHAR(36) NOT NULL,
  name VARCHAR(120) NOT NULL,
  icon VARCHAR(20) NOT NULL,
  budget_amount DECIMAL(14,2) NOT NULL,
  ledger_account VARCHAR(20),
  version INT NOT NULL DEFAULT 1,
  updated_at_millis BIGINT NOT NULL,
  deleted BOOLEAN NOT NULL DEFAULT FALSE,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (created_by_user_id) REFERENCES users(id)
);
```

## 3. API Endpoints

### Authentication (Online Only)
- **Constraint**: Signup and Login require an active internet connection.
- `POST /auth/register`: Create a `PARTICULIER` or `ENTREPRISE` (as `OWNER`). Returns `AuthResponse`.
- `POST /auth/login`: Issue JWTs. Returns `AuthResponse`.
- `POST /auth/refresh`: Renew tokens. Returns `AuthResponse`.

### Workspace & Sync (Critical)
- `GET /sync/pull?since_version=X`: Fetch all records across all tables where `version > X`.
- `POST /sync/push`: Batch upload local changes. The backend must increment the global `version` and return the new server state.

### File Management
- `POST /upload/avatar`: Store user avatar. Return signed URL.
- `POST /upload/logo`: Store company logo. Return signed URL.

## 4. Business Logic Requirements (AI Implementation)
1. **RBAC Middleware**: Before any workspace request, verify the user's role in `memberships`.
    - `EMPLOYE` cannot see company-wide budgets or cost centers.
    - `EMPLOYE` can only `GET` entries where `created_by_user_id == req.user.id`.
    - `RH` can see labor-specific cost centers (filter by code prefix if needed).
2. **Sync Conflict Resolution**: Use **Timestamp-based Server Wins**. If a push contains a record with an older `updated_at_millis` than the server, ignore the push for that record.
3. **VAT Calculation**: Mirror the client logic. If a push only contains `amount_ttc` and `tax_rate`, calculate `amount_ht` server-side to ensure consistency.

## 5. Development Phases
1. **Infrastructure**: Setup Node + MariaDB + Docker.
2. **Auth & Profile**: Implement JWT and User profile management.
3. **Sync Engine**: The most important part. Handle the versioning logic.
4. **Roles**: Implement the RBAC middleware and filter the JSON responses.
5. **Testing**: Write unit tests for the VAT logic and Sync conflict resolution.
