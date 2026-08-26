/* NovaBank frontend — plain HTML/CSS/JS, no framework required.
   Change API_BASE if your Spring Boot server uses another host/port/context path. */
const API_BASE = localStorage.getItem("novabank_api_base") || "http://localhost:8080";

const state = {
  route: "dashboard",
  banks: [],
  accounts: [],
  addresses: [],
  loading: false,
  accountFilter: "ALL",
  bankPage: 0,
  bankPageSize: 8,
  bankSort: "id"
};

const app = document.getElementById("app");
const modalBackdrop = document.getElementById("modalBackdrop");
const modal = document.getElementById("modal");
const toastStack = document.getElementById("toastStack");

const accountTypes = ["SAVINGS","CURRENT","SALARY","FIXED_DEPOSIT","RECURRING_DEPOSIT"];

const api = {
  async request(path, options = {}) {
    const controller = new AbortController();
    const timer = setTimeout(() => controller.abort(), 15000);
    try {
      const res = await fetch(API_BASE + path, {
        ...options,
        headers: {"Content-Type":"application/json", ...(options.headers || {})},
        signal: controller.signal
      });
      const text = await res.text();
      let body = null;
      try { body = text ? JSON.parse(text) : null; } catch { body = text; }
      if (!res.ok) {
        const message = body?.message || body?.error || body || `Request failed (${res.status})`;
        throw new Error(String(message));
      }
      return body;
    } catch (e) {
      if (e.name === "AbortError") throw new Error("Request timed out. Check that Spring Boot is running.");
      if (e instanceof TypeError) throw new Error("Unable to reach the banking service. Please make sure the application is running.");
      throw e;
    } finally { clearTimeout(timer); }
  },
  get(path){ return this.request(path); },
  post(path, data){ return this.request(path,{method:"POST",body:JSON.stringify(data)}); },
  patch(path, data){ return this.request(path,{method:"PATCH",body:data===undefined?undefined:JSON.stringify(data)}); },
  del(path){ return this.request(path,{method:"DELETE"}); }
};

function unwrap(res){ return res?.data ?? res; }
function money(v){ return new Intl.NumberFormat("en-IN",{style:"currency",currency:"INR",maximumFractionDigits:2}).format(Number(v||0)); }
function initials(name="User"){ return name.split(/\s+/).filter(Boolean).slice(0,2).map(x=>x[0]).join("").toUpperCase(); }
function esc(v){ return String(v ?? "").replace(/[&<>"']/g,m=>({"&":"&amp;","<":"&lt;",">":"&gt;",'"':"&quot;","'":"&#039;"}[m])); }
function showToast(message,type="success"){ const el=document.createElement("div");el.className=`toast ${type}`;el.textContent=message;toastStack.appendChild(el);setTimeout(()=>el.remove(),4200); }
function setLoading(on){ state.loading=on; }
function safeData(res){ const d=unwrap(res); return Array.isArray(d)?d:(d==null?[]:[d]); }

function openModal(title, subtitle, body, actions=""){
  modal.innerHTML=`<div class="modal-head"><div><h2>${title}</h2><div class="card-sub">${subtitle||""}</div></div><button class="close" onclick="closeModal()">×</button></div>${body}<div class="modal-actions">${actions||'<button class="btn" onclick="closeModal()">Close</button>'}</div>`;
  modalBackdrop.classList.remove("hidden");
}
function closeModal(){ modalBackdrop.classList.add("hidden"); modal.innerHTML=""; }
window.closeModal=closeModal;

function setRoute(route){
  state.route=route;
  document.querySelectorAll(".nav-item").forEach(x=>x.classList.toggle("active",x.dataset.route===route));
  const titles={dashboard:"Dashboard",accounts:"Accounts",banks:"Banks & Branches",transactions:"Transactions",addresses:"Addresses"};
  document.getElementById("pageTitle").textContent=titles[route]||"Dashboard";
  document.getElementById("sidebar").classList.remove("open");
  render();
}
document.querySelectorAll(".nav-item").forEach(btn=>btn.addEventListener("click",()=>setRoute(btn.dataset.route)));
document.getElementById("mobileMenu").onclick=()=>document.getElementById("sidebar").classList.toggle("open");
document.getElementById("themeToggle").onclick=()=>{document.body.classList.toggle("dark");localStorage.setItem("novabank_theme",document.body.classList.contains("dark")?"dark":"light")};
if(localStorage.getItem("novabank_theme")==="dark")document.body.classList.add("dark");

async function loadDashboard(){
  const [banksRes,accountsRes]=await Promise.allSettled([api.get("/api/bank/all"),api.get("/api/account/all")]);
  state.banks=banksRes.status==="fulfilled"?safeData(banksRes.value):[];
  state.accounts=accountsRes.status==="fulfilled"?safeData(accountsRes.value):[];
  renderDashboard();
}
function renderDashboard(){
  const totalBalance=state.accounts.reduce((s,a)=>s+Number(a.balance||0),0);
  const savings=state.accounts.filter(a=>a.accountType==="SAVINGS").length;
  const currents=state.accounts.filter(a=>a.accountType==="CURRENT").length;
  const recent=[...state.accounts].sort((a,b)=>(b.id||0)-(a.id||0)).slice(0,6);
  app.innerHTML=`
  <div class="page-head"><div><div class="eyebrow">Overview</div><h1>Good to see you.</h1><p>Monitor your banking network, accounts and transactions from one place.</p></div>
  <div class="head-actions"><button class="btn" onclick="loadDashboard()">↻ Refresh</button><button class="btn primary" onclick="showAccountForm()">+ New account</button></div></div>
  <div class="grid stats">
    <div class="stat-card"><div class="stat-top"><span>Total banks</span><span class="stat-icon">▣</span></div><div class="stat-value">${state.banks.length}</div><div class="stat-note">Registered banks</div></div>
    <div class="stat-card"><div class="stat-top"><span>Total accounts</span><span class="stat-icon">◉</span></div><div class="stat-value">${state.accounts.length}</div><div class="stat-note">${savings} savings · ${currents} current</div></div>
    <div class="stat-card"><div class="stat-top"><span>Portfolio balance</span><span class="stat-icon">₹</span></div><div class="stat-value">${money(totalBalance)}</div><div class="stat-note good">Across all registered accounts</div></div>
    <div class="stat-card"><div class="stat-top"><span>Avg. balance</span><span class="stat-icon">↗</span></div><div class="stat-value">${money(state.accounts.length?totalBalance/state.accounts.length:0)}</div><div class="stat-note">Per account</div></div>
  </div>
  <div class="grid two-col">
    <div class="card"><div class="card-head"><div><div class="card-title">Recent accounts</div><div class="card-sub">Latest account activity</div></div><button class="link-btn" onclick="setRoute('accounts')">View all →</button></div>
      ${recent.length?`<div class="table-wrap"><table class="table"><thead><tr><th>Account holder</th><th>Account</th><th>Type</th><th>Balance</th></tr></thead><tbody>${recent.map(accountRow).join("")}</tbody></table></div>`:`<div class="empty"><strong>No account records</strong>Create your first account to populate the dashboard.</div>`}
    </div>
    <div class="card"><div class="card-head"><div><div class="card-title">Bank network</div><div class="card-sub">Registered branches</div></div><button class="link-btn" onclick="setRoute('banks')">Manage →</button></div>
      <div class="grid" style="gap:10px">${state.banks.slice(0,4).map(b=>`<div class="bank-card"><div><h3>${esc(b.bankName)}</h3><p>${esc(b.branchName||"Branch")}</p><p class="mono">${esc(b.ifsc||"—")}</p></div><span class="badge blue">Active</span></div>`).join("") || `<div class="empty"><strong>No banks</strong>Add a bank to start.</div>`}</div>
    </div>
  </div>`;
}
function accountRow(a){
  return `<tr><td><div class="person"><span class="person-avatar">${initials(a.accountHolderName)}</span><div><strong>${esc(a.accountHolderName)}</strong><div class="muted">ID #${esc(a.id)}</div></div></div></td><td class="mono">${esc(a.accountNumber)}</td><td><span class="badge blue">${esc(a.accountType)}</span></td><td class="amount">${money(a.balance)}</td></tr>`;
}

async function renderAccounts(){
  app.innerHTML=`<div class="page-head"><div><div class="eyebrow">Accounts</div><h1>Customer accounts</h1><p>Search, inspect, create, fund and manage every account.</p></div><div class="head-actions"><button class="btn" onclick="loadAccounts()">↻ Refresh</button><button class="btn primary" onclick="showAccountForm()">+ New account</button></div></div><div class="card"><div class="toolbar"><div class="search"><input id="accountSearch" placeholder="Search holder, account number or ID…" oninput="filterAccounts()"></div><div class="filter-row"><select class="select-sm" id="accountTypeFilter" onchange="filterAccounts()"><option value="ALL">All types</option>${accountTypes.map(x=>`<option>${x}</option>`).join("")}</select><select class="select-sm" id="balanceSort" onchange="sortAccounts()"><option value="">Sort</option><option value="balance">Balance high → low</option><option value="holder">Holder A → Z</option><option value="id">Newest</option></select></div></div><div id="accountTable"></div></div>`;
  await loadAccounts();
}
async function loadAccounts(){
  try{state.accounts=safeData(await api.get("/api/account/all"));renderAccountTable();}catch(e){renderAccountTable(e);showToast(e.message,"error")}
}
function filterAccounts(){renderAccountTable()}
function sortAccounts(){renderAccountTable()}
function renderAccountTable(error){
  if(!document.getElementById("accountTable"))return;
  const q=(document.getElementById("accountSearch")?.value||"").toLowerCase(), type=document.getElementById("accountTypeFilter")?.value||"ALL", sort=document.getElementById("balanceSort")?.value||"";
  let list=state.accounts.filter(a=>(type==="ALL"||a.accountType===type)&&(!q||[a.accountHolderName,a.accountNumber,a.id,a.accountType].join(" ").toLowerCase().includes(q)));
  if(sort==="balance")list.sort((a,b)=>(b.balance||0)-(a.balance||0)); else if(sort==="holder")list.sort((a,b)=>String(a.accountHolderName).localeCompare(String(b.accountHolderName))); else list.sort((a,b)=>(b.id||0)-(a.id||0));
  document.getElementById("accountTable").innerHTML=error?`<div class="empty"><strong>Could not load accounts</strong>${esc(error.message)}</div>`:list.length?`<div class="table-wrap"><table class="table"><thead><tr><th>Holder</th><th>Account number</th><th>Type</th><th>Bank</th><th>Balance</th><th>Actions</th></tr></thead><tbody>${list.map(a=>`<tr><td>${`<div class="person"><span class="person-avatar">${initials(a.accountHolderName)}</span><strong>${esc(a.accountHolderName)}</strong></div>`}</td><td class="mono">${esc(a.accountNumber)}</td><td><span class="badge blue">${esc(a.accountType)}</span></td><td>${esc(a.bank?.bankName||"Bank #"+(a.bank?.id??"—"))}</td><td class="amount">${money(a.balance)}</td><td><div class="filter-row"><button class="btn" onclick="showAccount(${a.id})">View</button><button class="btn" onclick="showMoneyForm('deposit',${a.accountNumber})">Deposit</button><button class="btn" onclick="showMoneyForm('withdraw',${a.accountNumber})">Withdraw</button><button class="btn danger" onclick="deleteAccount(${a.id})">Delete</button></div></td></tr>`).join("")}</tbody></table></div>`:`<div class="empty"><strong>No matching accounts</strong>Try another search or create a new account.</div>`;
}
async function showAccount(id){
  try{const a=unwrap(await api.get(`/api/account/${id}`));openModal(`Account #${a.id}`,"Account overview",`<div class="detail-grid">
  <div class="detail"><small>Holder</small><strong>${esc(a.accountHolderName)}</strong></div><div class="detail"><small>Account number</small><strong class="mono">${esc(a.accountNumber)}</strong></div>
  <div class="detail"><small>Type</small><strong>${esc(a.accountType)}</strong></div><div class="detail"><small>Balance</small><strong>${money(a.balance)}</strong></div>
  <div class="detail"><small>Bank</small><strong>${esc(a.bank?.bankName||"—")}</strong></div><div class="detail"><small>Bank ID</small><strong>${esc(a.bank?.id||"—")}</strong></div>
  </div>`, `<button class="btn" onclick="showMoneyForm('deposit',${a.accountNumber})">Deposit</button><button class="btn" onclick="showMoneyForm('withdraw',${a.accountNumber})">Withdraw</button><button class="btn" onclick="showTransferForm(${a.accountNumber})">Transfer</button>`)}catch(e){showToast(e.message,"error")}
}
function showAccountForm(){
  openModal("Create account","Add a new customer account",`<form id="accountForm"><div class="form-grid">
  <div class="field"><label>Account number</label><input name="accountNumber" type="number" required></div>
  <div class="field"><label>Holder name</label><input name="accountHolderName" required></div>
  <div class="field"><label>Account type</label><select name="accountType">${accountTypes.map(x=>`<option>${x}</option>`).join("")}</select></div>
  <div class="field"><label>Opening balance</label><input name="balance" type="number" step="0.01" min="0" value="5000" required></div>
  <div class="field full"><label>Bank ID</label><input name="bankId" type="number" required placeholder="Existing bank ID"></div></div></form>`,
  `<button class="btn" onclick="closeModal()">Cancel</button><button class="btn primary" onclick="createAccount()">Create account</button>`);
}
async function createAccount(){
  const f=document.getElementById("accountForm"); if(!f.reportValidity())return;
  const x=Object.fromEntries(new FormData(f).entries());
  const payload={accountNumber:Number(x.accountNumber),accountHolderName:x.accountHolderName,accountType:x.accountType,balance:Number(x.balance),bank:{id:Number(x.bankId)}};
  try{const r=unwrap(await api.post("/api/account/save",payload));closeModal();showToast("Account created successfully");state.accounts.unshift(r);renderAccountTable()}catch(e){showToast(e.message,"error")}
}
async function deleteAccount(id){
  if(!confirm(`Delete account #${id}?`))return;
  try{await api.del(`/api/account/${id}`);state.accounts=state.accounts.filter(a=>a.id!==id);renderAccountTable();showToast("Account deleted")}catch(e){showToast(e.message,"error")}
}
function showMoneyForm(kind,accountNumber=""){
  const deposit=kind==="deposit";
  const label=deposit?"Deposit money":"Withdraw money";
  openModal(label,deposit?"Add funds to a customer account":"Withdraw funds from a customer account",`<form id="moneyForm" class="form-grid"><div class="field full"><label>Account number</label><input name="accountNumber" value="${esc(accountNumber)}" type="number" inputmode="numeric" placeholder="Enter account number" required autofocus></div><div class="field full"><label>Amount (INR)</label><input name="amount" type="number" min="0.01" step="0.01" placeholder="0.00" required></div></form>`,
  `<button class="btn" onclick="closeModal()">Cancel</button><button class="btn primary" onclick="submitMoney('${kind}')">${deposit?"Deposit":"Withdraw"}</button>`);
}
async function submitMoney(kind){
  const x=Object.fromEntries(new FormData(document.getElementById("moneyForm")).entries());
  const accountNumber=x.accountNumber, amount=Number(x.amount);
  if(!(amount>0)||!accountNumber)return;
  try{const r=unwrap(await api.patch(`/api/account/${kind}/${accountNumber}/${amount}`));closeModal();showToast(`${kind==="deposit"?"Deposit":"Withdrawal"} completed successfully`);const idx=state.accounts.findIndex(a=>a.accountNumber==accountNumber);if(idx>=0)state.accounts[idx]=r;renderAccountTable()}catch(e){showToast(e.message,"error")}
}
function showTransferForm(sender=""){
  openModal("Transfer funds","Send money securely between two existing accounts",`<form id="transferForm"><div class="form-grid"><div class="field"><label>From account</label><input name="sender" value="${esc(sender)}" type="number" inputmode="numeric" placeholder="Sender account" required></div><div class="field"><label>To account</label><input name="receiver" type="number" inputmode="numeric" placeholder="Receiver account" required></div><div class="field full"><label>Amount (INR)</label><input name="amount" type="number" min="0.01" step="0.01" placeholder="0.00" required></div></div></form>`,`<button class="btn" onclick="closeModal()">Cancel</button><button class="btn primary" onclick="submitTransfer()">Transfer now</button>`);
}
async function submitTransfer(){
  const x=Object.fromEntries(new FormData(document.getElementById("transferForm")).entries());const amount=Number(x.amount);
  if(!(amount>0))return;
  try{const r=unwrap(await api.patch(`/api/account/transfer/${x.sender}/${x.receiver}/${amount}`));closeModal();showToast(r||"Transfer completed");loadAccounts()}catch(e){showToast(e.message,"error")}
}

async function renderBanks(){
  app.innerHTML=`<div class="page-head"><div><div class="eyebrow">Banking network</div><h1>Banks & branches</h1><p>Manage institutions, IFSC codes, branches and contact details.</p></div><div class="head-actions"><button class="btn" onclick="loadBanks()">↻ Refresh</button><button class="btn primary" onclick="showBankForm()">+ Add bank</button></div></div><div class="card"><div class="toolbar"><div class="search"><input id="bankSearch" placeholder="Search bank, IFSC or branch…" oninput="filterBanks()"></div><div class="filter-row"><select class="select-sm" id="bankSort" onchange="loadBankPage(0)"><option value="id">Sort by ID</option><option value="bankName">Bank name</option><option value="branchName">Branch</option><option value="ifsc">IFSC</option></select></div></div><div id="bankGrid"></div><div id="bankPagination"></div></div>`;
  await loadBanks();
}
async function loadBanks(){try{state.banks=safeData(await api.get("/api/bank/all"));renderBankGrid()}catch(e){showToast(e.message,"error");renderBankGrid(e)}}
async function loadBankPage(page=0){
  state.bankPage=page;state.bankSort=document.getElementById("bankSort")?.value||"id";
  try{const res=await api.get(`/api/bank/page/${page}/${state.bankPageSize}/sort/${encodeURIComponent(state.bankSort)}`);const data=unwrap(res);state.banks=data?.content||safeData(res);renderBankGrid()}catch(e){showToast(e.message,"error")}
}
function filterBanks(){renderBankGrid()}
function renderBankGrid(error){
  const q=(document.getElementById("bankSearch")?.value||"").toLowerCase();let list=state.banks.filter(b=>!q||[b.bankName,b.ifsc,b.branchName,b.contact].join(" ").toLowerCase().includes(q));
  document.getElementById("bankGrid").innerHTML=error?`<div class="empty"><strong>Could not load banks</strong>${esc(error.message)}</div>`:list.length?`<div class="grid bank-grid">${list.map(b=>`<div class="bank-card"><div><span class="badge green">BANK #${esc(b.id)}</span><h3 style="margin-top:10px">${esc(b.bankName)}</h3><p>${esc(b.branchName||"—")}</p><p class="mono">${esc(b.ifsc||"—")}</p><p>☎ ${esc(b.contact||"—")}</p><p>⌖ ${esc(b.address?.city||"—")}, ${esc(b.address?.state||"")}</p></div><div class="bank-actions"><button class="btn" onclick="showBank(${b.id})">View</button><button class="btn danger" onclick="deleteBank(${b.id})">Delete</button></div></div>`).join("")}</div>`:`<div class="empty"><strong>No banks found</strong>Add a bank or change your search.</div>`;
}
async function showBank(id){
  try{const b=unwrap(await api.get(`/api/bank/${id}`));const addr=b.address||{};openModal(b.bankName,"Bank details",`<div class="detail-grid">
  <div class="detail"><small>Bank ID</small><strong>${esc(b.id)}</strong></div><div class="detail"><small>IFSC</small><strong class="mono">${esc(b.ifsc)}</strong></div>
  <div class="detail"><small>Branch</small><strong>${esc(b.branchName)}</strong></div><div class="detail"><small>Contact</small><strong>${esc(b.contact)}</strong></div>
  <div class="detail"><small>Street</small><strong>${esc(addr.street)}</strong></div><div class="detail"><small>City / State</small><strong>${esc(addr.city)}, ${esc(addr.state)}</strong></div>
  <div class="detail"><small>Pincode</small><strong>${esc(addr.pincode)}</strong></div><div class="detail"><small>Address ID</small><strong>${esc(addr.id)}</strong></div>
  </div>`,`<button class="btn" onclick="showBankAccounts(${b.id})">View accounts</button><button class="btn" onclick="showAddress(${addr.id})">View address</button>`)}catch(e){showToast(e.message,"error")}
}
function showBankForm(){
  openModal("Add bank","Register a bank and its branch address",`<form id="bankForm"><div class="form-grid">
  <div class="field"><label>Bank name</label><input name="bankName" required></div><div class="field"><label>IFSC</label><input name="ifsc" required></div>
  <div class="field"><label>Branch name</label><input name="branchName" required></div><div class="field"><label>Contact</label><input name="contact" type="tel" pattern="[0-9]{10}" maxlength="10" required></div>
  <div class="field"><label>Street</label><input name="street" required></div><div class="field"><label>City</label><input name="city" required></div>
  <div class="field"><label>State</label><input name="state" required></div><div class="field"><label>Pincode</label><input name="pincode" type="text" pattern="[0-9]{6}" maxlength="6" required></div>
  </div></form>`,`<button class="btn" onclick="closeModal()">Cancel</button><button class="btn primary" onclick="createBank()">Create bank</button>`);
}
async function createBank(){
  const f=document.getElementById("bankForm");if(!f.reportValidity())return;const x=Object.fromEntries(new FormData(f).entries());
  const payload={bankName:x.bankName,ifsc:x.ifsc,branchName:x.branchName,contact:Number(x.contact),address:{street:x.street,city:x.city,state:x.state,pincode:Number(x.pincode)}};
  try{const b=unwrap(await api.post("/api/bank/save",payload));closeModal();state.banks.unshift(b);showToast("Bank created successfully");renderBankGrid()}catch(e){showToast(e.message,"error")}
}
async function deleteBank(id){if(!confirm(`Delete bank #${id}? Linked accounts may prevent this bank from being deleted.`))return;try{await api.del(`/api/bank/${id}`);state.banks=state.banks.filter(b=>b.id!==id);renderBankGrid();showToast("Bank deleted")}catch(e){showToast(e.message,"error")}}
async function showBankAccounts(bankId){
  try{const list=safeData(await api.get(`/api/account/bank/${bankId}`));openModal(`Bank #${bankId} accounts`,"Accounts linked to this bank",list.length?`<div class="table-wrap"><table class="table"><thead><tr><th>Holder</th><th>Number</th><th>Type</th><th>Balance</th></tr></thead><tbody>${list.map(a=>`<tr><td>${esc(a.accountHolderName)}</td><td class="mono">${esc(a.accountNumber)}</td><td>${esc(a.accountType)}</td><td class="amount">${money(a.balance)}</td></tr>`).join("")}</tbody></table></div>`:`<div class="empty">No accounts for this bank.</div>`)}catch(e){showToast(e.message,"error")}
}

async function renderTransactions(){
  app.innerHTML=`<div class="page-head"><div><div class="eyebrow">Money movement</div><h1>Transactions</h1><p>Move money securely and check account balances in real time.</p></div><div class="head-actions"><button class="btn" onclick="checkConnection()">● Live status</button></div></div>
  <div class="transaction-hero"><div class="transaction-hero-copy"><span class="hero-icon">↔</span><div><div class="eyebrow light">Secure banking</div><h2>Move money with confidence.</h2><p>Deposit, withdraw or transfer funds between active accounts.</p></div></div></div>
  <div class="grid transaction-grid"><div class="card action-card"><div class="card-head"><div><div class="card-title">Quick actions</div><div class="card-sub">Choose a transaction to get started</div></div></div><div class="action-grid">
  <button class="action-tile deposit" onclick="showMoneyForm('deposit')"><span class="action-symbol">＋</span><span><strong>Deposit</strong><small>Add money to an account</small></span><span class="action-arrow">→</span></button>
  <button class="action-tile withdraw" onclick="showMoneyForm('withdraw')"><span class="action-symbol">−</span><span><strong>Withdraw</strong><small>Take money from an account</small></span><span class="action-arrow">→</span></button>
  <button class="action-tile transfer" onclick="showTransferForm()"><span class="action-symbol">↔</span><span><strong>Transfer</strong><small>Send money to another account</small></span><span class="action-arrow">→</span></button></div></div>
  <div class="card lookup-card"><div class="card-head"><div><div class="card-title">Check account</div><div class="card-sub">View the latest available balance and account details</div></div><span class="live-chip"><span></span> Live</span></div>
  <form id="lookupForm" class="lookup-form"><div class="field"><label>Account number</label><input name="accountNumber" type="number" inputmode="numeric" placeholder="Enter account number" required></div><button class="btn primary lookup-btn" type="submit">View account</button></form><div id="lookupResult"></div></div></div>`;
  document.getElementById("lookupForm").onsubmit=async e=>{e.preventDefault();const n=new FormData(e.target).get("accountNumber");try{const a=unwrap(await api.get(`/api/account/accountNumber/${n}`));document.getElementById("lookupResult").innerHTML=`<div class="detail-grid" style="margin-top:18px"><div class="detail"><small>Holder</small><strong>${esc(a.accountHolderName)}</strong></div><div class="detail"><small>Type</small><strong>${esc(a.accountType)}</strong></div><div class="detail"><small>Balance</small><strong>${money(a.balance)}</strong></div><div class="detail"><small>Bank</small><strong>${esc(a.bank?.bankName||"—")}</strong></div></div>`}catch(x){document.getElementById("lookupResult").innerHTML=`<div class="empty">${esc(x.message)}</div>`}};
}
async function renderAddresses(){
  app.innerHTML=`<div class="page-head"><div><div class="eyebrow">Locations</div><h1>Address tools</h1><p>Find addresses by ID, bank, city or city + street and update an existing address.</p></div></div>
  <div class="grid two-col"><div class="card"><div class="card-head"><div class="card-title">Find address</div></div><form id="addressLookup" class="grid" style="gap:12px">
  <div class="field"><label>Lookup type</label><select name="type" id="addressType"><option value="id">Address ID</option><option value="bank">Bank ID</option><option value="city">City</option><option value="cityStreet">City + Street</option></select></div>
  <div id="addressFields"></div><button class="btn primary">Search</button></form><div id="addressResult"></div></div>
  <div class="card"><div class="card-head"><div class="card-title">Update address</div></div><form id="addressUpdate" class="form-grid">
  <div class="field full"><label>Address ID</label><input name="id" type="number" required></div><div class="field"><label>Street</label><input name="street"></div><div class="field"><label>City</label><input name="city"></div><div class="field"><label>State</label><input name="state"></div><div class="field"><label>Pincode</label><input name="pincode" type="number"></div><div class="field full"><button class="btn primary">Update address</button></div></form></div></div>`;
  const fields=document.getElementById("addressFields");
  const updateFields=()=>fields.innerHTML=document.getElementById("addressType").value==="cityStreet"?`<div class="form-grid"><div class="field"><label>City</label><input name="city"></div><div class="field"><label>Street</label><input name="street"></div></div>`:`<div class="field"><label>${document.getElementById("addressType").value==="city"?"City":"ID"}</label><input name="value" ${document.getElementById("addressType").value==="city"?"":"type=\"number\""} required></div>`;
  document.getElementById("addressType").onchange=updateFields;updateFields();
  document.getElementById("addressLookup").onsubmit=async e=>{e.preventDefault();const type=e.target.type.value;const d=new FormData(e.target);try{let r;if(type==="id")r=unwrap(await api.get(`/api/address/${d.get("value")}`));else if(type==="bank")r=unwrap(await api.get(`/api/address/bank/${d.get("value")}`));else if(type==="city")r=safeData(await api.get(`/api/address/city/${encodeURIComponent(d.get("value"))}`));else r=unwrap(await api.get(`/api/address/city/${encodeURIComponent(d.get("city"))}/street/${encodeURIComponent(d.get("street"))}`));document.getElementById("addressResult").innerHTML=Array.isArray(r)?renderAddressList(r):renderAddressList([r])}catch(x){document.getElementById("addressResult").innerHTML=`<div class="empty">${esc(x.message)}</div>`}};
  document.getElementById("addressUpdate").onsubmit=async e=>{e.preventDefault();const x=Object.fromEntries(new FormData(e.target).entries());const data={};["street","city","state","pincode"].forEach(k=>{if(x[k])data[k]=k==="pincode"?Number(x[k]):x[k]});try{const r=unwrap(await api.patch(`/api/address/${x.id}`,data));showToast(r||"Address updated");e.target.reset()}catch(x){showToast(x.message,"error")}};
}
function renderAddressList(list){return list.length?`<div class="grid" style="gap:9px;margin-top:18px">${list.map(a=>`<div class="detail"><small>Address #${esc(a.id)}</small><strong>${esc(a.street)}, ${esc(a.city)}, ${esc(a.state)} — ${esc(a.pincode)}</strong></div>`).join("")}</div>`:`<div class="empty">No address records.</div>`}
async function showAddress(id){if(!id){showToast("This bank has no address ID","error");return}try{const a=unwrap(await api.get(`/api/address/${id}`));openModal("Address details","Branch location details",`<div class="detail-grid"><div class="detail"><small>Street</small><strong>${esc(a.street)}</strong></div><div class="detail"><small>City</small><strong>${esc(a.city)}</strong></div><div class="detail"><small>State</small><strong>${esc(a.state)}</strong></div><div class="detail"><small>Pincode</small><strong>${esc(a.pincode)}</strong></div></div>`,`<button class="btn" onclick="closeModal()">Close</button>`)}catch(e){showToast(e.message,"error")}}

function render(){
  if(state.route==="dashboard"){loadDashboard();return}
  if(state.route==="accounts"){renderAccounts();return}
  if(state.route==="banks"){renderBanks();return}
  if(state.route==="transactions"){renderTransactions();return}
  if(state.route==="addresses"){renderAddresses();return}
}
render();checkConnection();
