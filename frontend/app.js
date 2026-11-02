// ===================== CONFIG =====================
const API_BASE = "http://localhost:8080/api";

// ===================== AUTH STATE =====================

function getToken() {
    return localStorage.getItem("token");
}

function getUser() {
    const user = localStorage.getItem("user");
    return user ? JSON.parse(user) : null;
}

function saveAuth(data) {
    localStorage.setItem("token", data.token);
    localStorage.setItem("user", JSON.stringify({ username: data.username, role: data.role }));
}

function clearAuth() {
    localStorage.removeItem("token");
    localStorage.removeItem("user");
}

// Returns headers with JWT token for authenticated API calls
function authHeaders() {
    return {
        "Content-Type": "application/json",
        "Authorization": "Bearer " + getToken(),
    };
}

// ===================== HELPERS =====================

function showToast(message, type = "success") {
    const toast = document.getElementById("toast");
    toast.textContent = message;
    toast.className = `toast show ${type}`;
    setTimeout(() => (toast.className = "toast"), 3000);
}

function formatDate(dateStr) {
    if (!dateStr) return "—";
    const d = new Date(dateStr);
    return d.toLocaleDateString() + " " + d.toLocaleTimeString([], { hour: "2-digit", minute: "2-digit" });
}

function confidenceHtml(score) {
    if (score == null) return "—";
    let color = "#c62828";
    if (score >= 90) color = "#2e7d32";
    else if (score >= 60) color = "#e65100";
    return `<span class="confidence-bar"><span class="confidence-fill" style="width:${score}%;background:${color}"></span></span>${score.toFixed(1)}%`;
}

function statusClass(status) {
    return `status-${(status || "pending").toLowerCase()}`;
}

function escapeHtml(str) {
    return str.replace(/'/g, "\\'").replace(/"/g, "&quot;");
}

function isAdmin() {
    const user = getUser();
    return user && user.role === "ROLE_ADMIN";
}

// ===================== UI SWITCHING =====================

function showApp() {
    document.getElementById("auth-section").style.display = "none";
    document.getElementById("app-section").style.display = "block";

    const user = getUser();
    const roleClass = user.role === "ROLE_ADMIN" ? "admin" : "user";
    const roleLabel = user.role === "ROLE_ADMIN" ? "ADMIN" : "USER";
    document.getElementById("user-display").innerHTML =
        `${user.username} <span class="role-badge ${roleClass}">${roleLabel}</span>`;

    loadCustomers();
}

function showAuth() {
    document.getElementById("auth-section").style.display = "block";
    document.getElementById("app-section").style.display = "none";
}

function showAuthTab(tab) {
    document.getElementById("login-tab").style.display = tab === "login" ? "block" : "none";
    document.getElementById("register-tab").style.display = tab === "register" ? "block" : "none";
    document.querySelectorAll("#auth-section .tab").forEach((t) => t.classList.remove("active"));
    event.target.classList.add("active");
}

function logout() {
    clearAuth();
    showAuth();
    showToast("Logged out");
}

// ===================== AUTH TABS =====================

function showSection(name) {
    document.getElementById("customers-section").style.display = name === "customers" ? "block" : "none";
    document.getElementById("addresses-section").style.display = name === "addresses" ? "block" : "none";
    document.querySelectorAll("#app-section .tab").forEach((t) => t.classList.remove("active"));
    event.target.classList.add("active");
    if (name === "customers") loadCustomers();
}

// ===================== REGISTER =====================

document.getElementById("register-form").addEventListener("submit", async (e) => {
    e.preventDefault();
    const body = {
        username: document.getElementById("reg-username").value.trim(),
        email: document.getElementById("reg-email").value.trim(),
        password: document.getElementById("reg-password").value,
    };
    try {
        const res = await fetch(`${API_BASE}/auth/register`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(body),
        });
        if (!res.ok) {
            const err = await res.json();
            throw new Error(err.message || "Registration failed");
        }
        const data = await res.json();
        saveAuth(data);
        showToast("Registered successfully! Welcome, " + data.username);
        showApp();
    } catch (err) {
        showToast(err.message, "error");
    }
});

// ===================== LOGIN =====================

document.getElementById("login-form").addEventListener("submit", async (e) => {
    e.preventDefault();
    const body = {
        username: document.getElementById("login-username").value.trim(),
        password: document.getElementById("login-password").value,
    };
    try {
        const res = await fetch(`${API_BASE}/auth/login`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(body),
        });
        if (!res.ok) {
            const err = await res.json();
            throw new Error(err.message || "Login failed");
        }
        const data = await res.json();
        saveAuth(data);
        showToast("Welcome back, " + data.username + "!");
        showApp();
    } catch (err) {
        showToast(err.message, "error");
    }
});

// ===================== CUSTOMERS =====================

async function loadCustomers() {
    try {
        const res = await fetch(`${API_BASE}/customers`, {
            headers: authHeaders(),
        });
        if (res.status === 401 || res.status === 403) {
            clearAuth();
            showAuth();
            showToast("Session expired. Please log in again.", "error");
            return;
        }
        if (!res.ok) throw new Error("Failed to load customers");
        const data = await res.json();
        const tbody = document.querySelector("#customers-table tbody");
        tbody.innerHTML = "";
        if (data.length === 0) {
            tbody.innerHTML = '<tr><td colspan="6" style="text-align:center;color:#888;">No customers found</td></tr>';
            return;
        }
        data.forEach((c) => {
            const tr = document.createElement("tr");
            // Only show delete button for admins
            const deleteBtn = isAdmin()
                ? `<button class="action-btn delete" onclick="deleteCustomer(${c.id})">Delete</button>`
                : "";
            tr.innerHTML = `
                <td>${c.id}</td>
                <td>${c.name}</td>
                <td>${c.email}</td>
                <td>${c.phoneNumber}</td>
                <td>${formatDate(c.createdAt)}</td>
                <td>
                    <button class="action-btn edit" onclick="startEdit(${c.id}, '${escapeHtml(c.name)}', '${escapeHtml(c.email)}', '${c.phoneNumber}')">Edit</button>
                    ${deleteBtn}
                </td>
            `;
            tbody.appendChild(tr);
        });
    } catch (err) {
        showToast(err.message, "error");
    }
}

document.getElementById("customer-form").addEventListener("submit", async (e) => {
    e.preventDefault();
    const body = {
        name: document.getElementById("cust-name").value.trim(),
        email: document.getElementById("cust-email").value.trim(),
        phoneNumber: document.getElementById("cust-phone").value.trim(),
    };
    try {
        const res = await fetch(`${API_BASE}/customers`, {
            method: "POST",
            headers: authHeaders(),
            body: JSON.stringify(body),
        });
        if (!res.ok) {
            const err = await res.json();
            throw new Error(err.message || JSON.stringify(err));
        }
        showToast("Customer created successfully!");
        e.target.reset();
        loadCustomers();
    } catch (err) {
        showToast(err.message, "error");
    }
});

// ---- Edit Customer ----

function startEdit(id, name, email, phone) {
    document.getElementById("edit-customer-section").style.display = "block";
    document.getElementById("edit-cust-id").value = id;
    document.getElementById("edit-cust-name").value = name;
    document.getElementById("edit-cust-email").value = email;
    document.getElementById("edit-cust-phone").value = phone;
}

function cancelEdit() {
    document.getElementById("edit-customer-section").style.display = "none";
}

document.getElementById("edit-customer-form").addEventListener("submit", async (e) => {
    e.preventDefault();
    const id = document.getElementById("edit-cust-id").value;
    const body = {
        name: document.getElementById("edit-cust-name").value.trim(),
        email: document.getElementById("edit-cust-email").value.trim(),
        phoneNumber: document.getElementById("edit-cust-phone").value.trim(),
    };
    try {
        const res = await fetch(`${API_BASE}/customers/${id}`, {
            method: "PUT",
            headers: authHeaders(),
            body: JSON.stringify(body),
        });
        if (!res.ok) {
            const err = await res.json();
            throw new Error(err.message || JSON.stringify(err));
        }
        showToast("Customer updated!");
        cancelEdit();
        loadCustomers();
    } catch (err) {
        showToast(err.message, "error");
    }
});

// ---- Delete Customer (Admin only) ----

async function deleteCustomer(id) {
    if (!confirm(`Delete customer #${id}? This cannot be undone.`)) return;
    try {
        const res = await fetch(`${API_BASE}/customers/${id}`, {
            method: "DELETE",
            headers: authHeaders(),
        });
        if (res.status === 403) {
            showToast("Access denied — admin role required", "error");
            return;
        }
        if (!res.ok) throw new Error("Failed to delete customer");
        showToast("Customer deleted.");
        loadCustomers();
    } catch (err) {
        showToast(err.message, "error");
    }
}

// ===================== ADDRESSES =====================

document.getElementById("address-form").addEventListener("submit", async (e) => {
    e.preventDefault();
    const customerId = document.getElementById("addr-customer-id").value;
    const body = {
        rawAddress: document.getElementById("addr-raw").value.trim(),
    };
    try {
        const res = await fetch(`${API_BASE}/customers/${customerId}/addresses`, {
            method: "POST",
            headers: authHeaders(),
            body: JSON.stringify(body),
        });
        if (!res.ok) {
            const err = await res.json();
            throw new Error(err.message || JSON.stringify(err));
        }
        const addr = await res.json();
        showToast(`Address created — Status: ${addr.status}, Confidence: ${addr.confidenceScore != null ? addr.confidenceScore.toFixed(1) + "%" : "N/A"}`);
        e.target.reset();
        document.getElementById("lookup-customer-id").value = customerId;
        loadAddresses();
    } catch (err) {
        showToast(err.message, "error");
    }
});

async function loadAddresses() {
    const customerId = document.getElementById("lookup-customer-id").value;
    if (!customerId) {
        showToast("Enter a customer ID first", "error");
        return;
    }
    try {
        const res = await fetch(`${API_BASE}/customers/${customerId}/addresses`, {
            headers: authHeaders(),
        });
        if (!res.ok) throw new Error("Failed to load addresses");
        const data = await res.json();
        const tbody = document.querySelector("#addresses-table tbody");
        tbody.innerHTML = "";
        if (data.length === 0) {
            tbody.innerHTML = '<tr><td colspan="9" style="text-align:center;color:#888;">No addresses found for this customer</td></tr>';
            return;
        }
        data.forEach((a) => {
            const tr = document.createElement("tr");
            tr.onclick = () => showAddressDetail(a);
            tr.innerHTML = `
                <td>${a.id}</td>
                <td>${a.rawAddress || "—"}</td>
                <td>${a.formattedAddress || "—"}</td>
                <td>${a.city || "—"}</td>
                <td>${a.state || "—"}</td>
                <td>${a.postalCode || "—"}</td>
                <td>${confidenceHtml(a.confidenceScore)}</td>
                <td><span class="${statusClass(a.status)}">${a.status || "—"}</span></td>
                <td>${a.processingMessage || "—"}</td>
            `;
            tbody.appendChild(tr);
        });
    } catch (err) {
        showToast(err.message, "error");
    }
}

function showAddressDetail(a) {
    const container = document.getElementById("address-detail");
    const content = document.getElementById("address-detail-content");
    const fields = [
        ["ID", a.id],
        ["Customer ID", a.customerId],
        ["Status", `<span class="${statusClass(a.status)}">${a.status}</span>`],
        ["Confidence", confidenceHtml(a.confidenceScore)],
        ["Raw Address", a.rawAddress],
        ["Formatted Address", a.formattedAddress],
        ["House Number", a.houseNumber],
        ["Building Name", a.buildingName],
        ["Street", a.street],
        ["Landmark", a.landmark],
        ["Area", a.area],
        ["City", a.city],
        ["State", a.state],
        ["Postal Code", a.postalCode],
        ["Country", a.country],
        ["Message", a.processingMessage],
        ["Created", formatDate(a.createdAt)],
        ["Updated", formatDate(a.updatedAt)],
    ];
    content.innerHTML = fields
        .map((f) => `<div class="detail-item"><strong>${f[0]}:</strong> ${f[1] || "—"}</div>`)
        .join("");
    container.style.display = "block";
}

// ===================== INIT =====================
// Check if user is already logged in (token in localStorage)
if (getToken() && getUser()) {
    showApp();
} else {
    showAuth();
}
