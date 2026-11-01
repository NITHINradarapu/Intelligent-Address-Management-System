// ===================== CONFIG =====================
const API_BASE = "http://localhost:8080/api";

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
    let color = "#c62828"; // red
    if (score >= 90) color = "#2e7d32"; // green
    else if (score >= 60) color = "#e65100"; // orange
    return `<span class="confidence-bar"><span class="confidence-fill" style="width:${score}%;background:${color}"></span></span>${score.toFixed(1)}%`;
}

function statusClass(status) {
    return `status-${(status || "pending").toLowerCase()}`;
}

// ===================== TABS =====================

function showSection(name) {
    document.getElementById("customers-section").style.display = name === "customers" ? "block" : "none";
    document.getElementById("addresses-section").style.display = name === "addresses" ? "block" : "none";
    document.querySelectorAll(".tab").forEach((t) => t.classList.remove("active"));
    event.target.classList.add("active");

    if (name === "customers") loadCustomers();
}

// ===================== CUSTOMERS =====================

async function loadCustomers() {
    try {
        const res = await fetch(`${API_BASE}/customers`);
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
            tr.innerHTML = `
                <td>${c.id}</td>
                <td>${c.name}</td>
                <td>${c.email}</td>
                <td>${c.phoneNumber}</td>
                <td>${formatDate(c.createdAt)}</td>
                <td>
                    <button class="action-btn edit" onclick="startEdit(${c.id}, '${escapeHtml(c.name)}', '${escapeHtml(c.email)}', '${c.phoneNumber}')">Edit</button>
                    <button class="action-btn delete" onclick="deleteCustomer(${c.id})">Delete</button>
                </td>
            `;
            tbody.appendChild(tr);
        });
    } catch (err) {
        showToast(err.message, "error");
    }
}

function escapeHtml(str) {
    return str.replace(/'/g, "\\'").replace(/"/g, "&quot;");
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
            headers: { "Content-Type": "application/json" },
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
            headers: { "Content-Type": "application/json" },
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

// ---- Delete Customer ----

async function deleteCustomer(id) {
    if (!confirm(`Delete customer #${id}? This cannot be undone.`)) return;
    try {
        const res = await fetch(`${API_BASE}/customers/${id}`, { method: "DELETE" });
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
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(body),
        });
        if (!res.ok) {
            const err = await res.json();
            throw new Error(err.message || JSON.stringify(err));
        }
        const addr = await res.json();
        showToast(`Address created — Status: ${addr.status}, Confidence: ${addr.confidenceScore != null ? addr.confidenceScore.toFixed(1) + "%" : "N/A"}`);
        e.target.reset();
        // Auto-load addresses for this customer
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
        const res = await fetch(`${API_BASE}/customers/${customerId}/addresses`);
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
loadCustomers();

