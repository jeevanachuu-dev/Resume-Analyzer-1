const ADMIN_TOKEN_KEY = "resumeiq_admin_token";
const ADMIN_USER_KEY = "resumeiq_admin_user";
const USER_TOKEN_KEY = "resumeiq_user_token";
const USER_NAME_KEY = "resumeiq_user_name";
const USER_FULL_NAME_KEY = "resumeiq_user_full_name";
const USER_EMAIL_KEY = "resumeiq_user_email";

const state = {
    jobs: [],
    lastApplication: null,
    applications: [],
    registeredUsers: [],
    comparisons: [],
    expandedApplicationId: null,
    dashboard: null,
    adminToken: localStorage.getItem(ADMIN_TOKEN_KEY),
    adminUser: localStorage.getItem(ADMIN_USER_KEY),
    userToken: localStorage.getItem(USER_TOKEN_KEY),
    userName: localStorage.getItem(USER_NAME_KEY),
    userFullName: localStorage.getItem(USER_FULL_NAME_KEY),
    userEmail: localStorage.getItem(USER_EMAIL_KEY)
};

const elements = {};

document.addEventListener("DOMContentLoaded", () => {
    cacheElements();
    bindEvents();
    initialize();
});

function cacheElements() {
    elements.tabs = document.querySelectorAll("[data-view-target]");
    elements.views = document.querySelectorAll(".view-section");
    elements.authModeButtons = document.querySelectorAll("[data-auth-mode]");
    elements.userRegisterForm = document.getElementById("userRegisterForm");
    elements.userLoginForm = document.getElementById("userLoginForm");
    elements.registerPanel = document.getElementById("registerPanel");
    elements.loginPanel = document.getElementById("loginPanel");
    elements.userSessionPanel = document.getElementById("userSessionPanel");
    elements.userSessionName = document.getElementById("userSessionName");
    elements.userSessionEmail = document.getElementById("userSessionEmail");
    elements.userSessionUsername = document.getElementById("userSessionUsername");
    elements.userLogout = document.getElementById("userLogout");
    elements.userRegisterStatus = document.getElementById("userRegisterStatus");
    elements.userLoginStatus = document.getElementById("userLoginStatus");
    elements.applicationForm = document.getElementById("applicationForm");
    elements.jobSelect = document.getElementById("jobSelect");
    elements.jobStrip = document.getElementById("jobStrip");
    elements.fileInput = elements.applicationForm.querySelector("input[name='resume']");
    elements.fileLabel = document.getElementById("fileLabel");
    elements.comparisonForm = document.getElementById("comparisonForm");
    elements.comparisonFileInput = elements.comparisonForm.querySelector("input[name='resume']");
    elements.comparisonFileLabel = document.getElementById("comparisonFileLabel");
    elements.comparisonJobOptions = document.getElementById("comparisonJobOptions");
    elements.comparisonResults = document.getElementById("comparisonResults");
    elements.dashboardTotalApplications = document.getElementById("dashboardTotalApplications");
    elements.dashboardAverageScore = document.getElementById("dashboardAverageScore");
    elements.dashboardTodayApplications = document.getElementById("dashboardTodayApplications");
    elements.topSkillsChart = document.getElementById("topSkillsChart");
    elements.dailyApplicationsChart = document.getElementById("dailyApplicationsChart");
    elements.visualScore = document.getElementById("visualScore");
    elements.toast = document.getElementById("toast");
    elements.scoreForm = document.getElementById("scoreSearchForm");
    elements.scoreApplicationId = document.getElementById("scoreApplicationId");
    elements.scoreRing = document.getElementById("scoreRing");
    elements.scoreValue = document.getElementById("scoreValue");
    elements.scoreLabel = document.getElementById("scoreLabel");
    elements.scoreName = document.getElementById("scoreName");
    elements.scoreSummary = document.getElementById("scoreSummary");
    elements.matchedSkills = document.getElementById("matchedSkills");
    elements.missingSkills = document.getElementById("missingSkills");
    elements.resumeSuggestions = document.getElementById("resumeSuggestions");
    elements.adminSort = document.getElementById("adminSort");
    elements.shortlistedOnly = document.getElementById("shortlistedOnly");
    elements.refreshAdmin = document.getElementById("refreshAdmin");
    elements.applicationsTable = document.getElementById("applicationsTable");
    elements.adminLoginPanel = document.getElementById("adminLoginPanel");
    elements.adminDashboard = document.getElementById("adminDashboard");
    elements.adminSession = document.getElementById("adminSession");
    elements.adminIdentity = document.getElementById("adminIdentity");
    elements.adminLoginForm = document.getElementById("adminLoginForm");
    elements.adminLogout = document.getElementById("adminLogout");
    elements.adminLoginStatus = document.getElementById("adminLoginStatus");
    elements.totalApplications = document.getElementById("totalApplications");
    elements.shortlistedApplications = document.getElementById("shortlistedApplications");
    elements.averageScore = document.getElementById("averageScore");
    elements.adminTotalApplications = document.getElementById("adminTotalApplications");
    elements.adminShortlistedApplications = document.getElementById("adminShortlistedApplications");
    elements.adminAverageScore = document.getElementById("adminAverageScore");
    elements.candidateRankingPanel = document.getElementById("candidateRankingPanel");
    elements.registeredUsersTable = document.getElementById("registeredUsersTable");
    elements.registeredUsersSummary = document.getElementById("registeredUsersSummary");
    elements.refreshAdminUsers = document.getElementById("refreshAdminUsers");
}

function bindEvents() {
    elements.tabs.forEach((tab) => {
        tab.addEventListener("click", (event) => {
            event.preventDefault();
            switchView(tab.dataset.viewTarget);
        });
    });

    elements.authModeButtons.forEach((button) => {
        button.addEventListener("click", () => switchAuthMode(button.dataset.authMode));
    });

    elements.fileInput.addEventListener("change", () => {
        const file = elements.fileInput.files[0];
        elements.fileLabel.textContent = file ? file.name : "Drop resume here or choose file";
    });

    elements.comparisonFileInput.addEventListener("change", () => {
        const file = elements.comparisonFileInput.files[0];
        elements.comparisonFileLabel.textContent = file ? file.name : "Drop resume here or choose file";
    });

    elements.applicationForm.addEventListener("submit", submitApplication);
    elements.comparisonForm.addEventListener("submit", submitComparison);
    elements.scoreForm.addEventListener("submit", findScore);
    elements.userRegisterForm.addEventListener("submit", registerUser);
    elements.userLoginForm.addEventListener("submit", loginUser);
    elements.userLogout.addEventListener("click", logoutUser);
    elements.adminLoginForm.addEventListener("submit", loginAdmin);
    elements.adminLogout.addEventListener("click", logoutAdmin);
    elements.adminSort.addEventListener("change", () => loadApplications().catch((error) => showToast(error.message)));
    elements.shortlistedOnly.addEventListener("change", () => loadApplications().catch((error) => showToast(error.message)));
    elements.refreshAdmin.addEventListener("click", () => {
        loadAdminData();
    });
    elements.refreshAdminUsers.addEventListener("click", () => {
        loadRegisteredUsers().catch((error) => showToast(error.message));
    });

    elements.applicationsTable.addEventListener("click", async (event) => {
        const detailsButton = event.target.closest("[data-details-id]");
        if (detailsButton) {
            toggleApplicationDetails(Number(detailsButton.dataset.detailsId));
            return;
        }

        const downloadButton = event.target.closest("[data-download-id]");
        if (downloadButton) {
            await downloadResume(downloadButton.dataset.downloadId);
            return;
        }

        const shortlistButton = event.target.closest("[data-shortlist-id]");
        if (shortlistButton) {
            const id = shortlistButton.dataset.shortlistId;
            const shortlisted = shortlistButton.dataset.shortlisted === "true";
            await updateShortlist(id, !shortlisted);
        }
    });
}

async function initialize() {
    try {
        renderAdminAuthState();
        renderUserAuthState();
        if (state.userToken) {
            await loadCurrentUser().catch(() => {
                clearUserSession();
                renderUserAuthState();
            });
        }
        await Promise.all([loadJobs(), loadStats(), loadDashboard()]);
        if (state.adminToken) {
            await loadAdminData();
        }
        refreshIcons();
    } catch (error) {
        showToast(error.message);
    }
}

function switchView(viewId) {
    elements.views.forEach((view) => view.classList.toggle("active", view.id === viewId));
    document.querySelectorAll(".tab-button").forEach((tab) => {
        tab.classList.toggle("active", tab.dataset.viewTarget === viewId);
    });
    if (viewId === "dashboardView") {
        loadDashboard().catch((error) => showToast(error.message));
    }
    refreshIcons();
}

async function loadJobs() {
    const jobs = await requestJson("/api/jobs");
    state.jobs = jobs;
    renderJobSelect(jobs);
    renderJobStrip(jobs);
    renderComparisonJobOptions(jobs);
}

function renderJobSelect(jobs) {
    if (!jobs.length) {
        elements.jobSelect.innerHTML = "<option value=''>No jobs available</option>";
        return;
    }

    elements.jobSelect.innerHTML = [
        "<option value=''>Select a job</option>",
        ...jobs.map((job) => `<option value="${job.id}">${escapeHtml(job.title)} - ${escapeHtml(job.company)} (${formatExperience(job.minExperience)})</option>`)
    ].join("");
}

function renderJobStrip(jobs) {
    elements.jobStrip.innerHTML = jobs.slice(0, 4).map((job) => `
        <article class="job-item">
            <strong>${escapeHtml(job.title)}</strong>
            <span>${escapeHtml(job.company)} / ${escapeHtml(job.location)} / ${formatExperience(job.minExperience)}</span>
        </article>
    `).join("");
}

function renderComparisonJobOptions(jobs) {
    if (!jobs.length) {
        elements.comparisonJobOptions.innerHTML = "<div class='empty-panel'>No jobs available for comparison.</div>";
        return;
    }

    elements.comparisonJobOptions.innerHTML = jobs.map((job) => `
        <label class="job-check-card">
            <input type="checkbox" name="jobIds" value="${job.id}">
            <span>
                <strong>${escapeHtml(job.title)}</strong>
                <small>${escapeHtml(job.company)} / ${formatExperience(job.minExperience)}</small>
            </span>
        </label>
    `).join("");
}

function switchAuthMode(mode) {
    const isRegister = mode === "register";
    elements.authModeButtons.forEach((button) => {
        button.classList.toggle("active", button.dataset.authMode === mode);
    });
    elements.registerPanel.classList.toggle("is-hidden", !isRegister);
    elements.loginPanel.classList.toggle("is-hidden", isRegister);
    refreshIcons();
}

async function registerUser(event) {
    event.preventDefault();
    const submitButton = elements.userRegisterForm.querySelector("button[type='submit']");
    const formData = new FormData(elements.userRegisterForm);

    try {
        submitButton.disabled = true;
        submitButton.innerHTML = "<i data-lucide='loader-circle'></i> Creating Account";
        refreshIcons();

        const session = await requestJson("/api/users/register", {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify({
                fullName: formData.get("fullName"),
                email: formData.get("email"),
                username: formData.get("username"),
                password: formData.get("password")
            })
        });

        setUserSession(session);
        elements.userRegisterForm.reset();
        renderUserAuthState();
        if (state.adminToken) {
            await loadRegisteredUsers();
        }
        showToast(`Welcome ${session.fullName}. Your account is ready.`);
    } catch (error) {
        elements.userRegisterStatus.textContent = error.message;
        showToast(error.message);
    } finally {
        submitButton.disabled = false;
        submitButton.innerHTML = "<i data-lucide='user-plus'></i> Register";
        renderUserAuthState();
        refreshIcons();
    }
}

async function loginUser(event) {
    event.preventDefault();
    const submitButton = elements.userLoginForm.querySelector("button[type='submit']");
    const formData = new FormData(elements.userLoginForm);

    try {
        submitButton.disabled = true;
        submitButton.innerHTML = "<i data-lucide='loader-circle'></i> Signing In";
        refreshIcons();

        const session = await requestJson("/api/users/login", {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify({
                username: formData.get("username"),
                password: formData.get("password")
            })
        });

        setUserSession(session);
        elements.userLoginForm.reset();
        renderUserAuthState();
        if (state.adminToken) {
            await loadRegisteredUsers();
        }
        showToast(`Logged in as ${session.username}.`);
    } catch (error) {
        elements.userLoginStatus.textContent = error.message;
        showToast(error.message);
    } finally {
        submitButton.disabled = false;
        submitButton.innerHTML = "<i data-lucide='log-in'></i> Login";
        renderUserAuthState();
        refreshIcons();
    }
}

async function logoutUser() {
    try {
        if (state.userToken) {
            await userRequestJson("/api/users/logout", { method: "POST" });
        }
    } catch (error) {
        showToast(error.message);
    } finally {
        clearUserSession();
        renderUserAuthState();
        if (state.adminToken) {
            loadRegisteredUsers().catch((error) => showToast(error.message));
        }
        showToast("User logged out.");
    }
}

async function loadCurrentUser() {
    const session = await userRequestJson("/api/users/me");
    setUserSession(session);
    renderUserAuthState();
}

function setUserSession(session) {
    state.userToken = session.token;
    state.userName = session.username;
    state.userFullName = session.fullName;
    state.userEmail = session.email;
    localStorage.setItem(USER_TOKEN_KEY, session.token);
    localStorage.setItem(USER_NAME_KEY, session.username);
    localStorage.setItem(USER_FULL_NAME_KEY, session.fullName);
    localStorage.setItem(USER_EMAIL_KEY, session.email);
}

function clearUserSession() {
    state.userToken = null;
    state.userName = null;
    state.userFullName = null;
    state.userEmail = null;
    localStorage.removeItem(USER_TOKEN_KEY);
    localStorage.removeItem(USER_NAME_KEY);
    localStorage.removeItem(USER_FULL_NAME_KEY);
    localStorage.removeItem(USER_EMAIL_KEY);
}

function renderUserAuthState() {
    const loggedIn = Boolean(state.userToken);
    elements.userSessionPanel.classList.toggle("is-hidden", !loggedIn);
    elements.registerPanel.classList.toggle("account-panel-disabled", loggedIn);
    elements.loginPanel.classList.toggle("account-panel-disabled", loggedIn);
    elements.userRegisterForm.querySelectorAll("input, button").forEach((field) => {
        field.disabled = loggedIn;
    });
    elements.userLoginForm.querySelectorAll("input, button").forEach((field) => {
        field.disabled = loggedIn;
    });
    elements.userSessionName.textContent = state.userFullName || "Registered User";
    elements.userSessionEmail.textContent = state.userEmail || "No email";
    elements.userSessionUsername.textContent = state.userName ? `@${state.userName}` : "@user";
    elements.userRegisterStatus.textContent = loggedIn ? "You are already signed in. Logout to register another account." : "Create an account to track user registration in the admin panel.";
    elements.userLoginStatus.textContent = loggedIn ? "Current user session is active." : "Login using username or email.";
    refreshIcons();
}

async function submitApplication(event) {
    event.preventDefault();
    const submitButton = elements.applicationForm.querySelector("button[type='submit']");
    const formData = new FormData(elements.applicationForm);

    try {
        submitButton.disabled = true;
        submitButton.innerHTML = "<i data-lucide='loader-circle'></i> Analyzing Resume";
        refreshIcons();

        const application = await requestJson("/api/applications", {
            method: "POST",
            body: formData
        });

        state.lastApplication = application;
        elements.applicationForm.reset();
        elements.fileLabel.textContent = "Drop resume here or choose file";
        renderScore(application);
        await Promise.all([loadStats(), loadDashboard()]);
        if (state.adminToken) {
            await loadAdminData();
        }
        switchView("scoreView");
        showToast(`Application #${application.id} submitted with score ${application.score}.`);
    } catch (error) {
        showToast(error.message);
    } finally {
        submitButton.disabled = false;
        submitButton.innerHTML = "<i data-lucide='send'></i> Submit Application";
        refreshIcons();
    }
}

async function submitComparison(event) {
    event.preventDefault();
    const submitButton = elements.comparisonForm.querySelector("button[type='submit']");
    const formData = new FormData(elements.comparisonForm);

    try {
        submitButton.disabled = true;
        submitButton.innerHTML = "<i data-lucide='loader-circle'></i> Comparing";
        refreshIcons();

        const comparisons = await requestJson("/api/applications/compare", {
            method: "POST",
            body: formData
        });

        state.comparisons = comparisons;
        renderComparisonResults(comparisons);
        showToast(`Compared resume with ${comparisons.length} job${comparisons.length === 1 ? "" : "s"}.`);
    } catch (error) {
        showToast(error.message);
    } finally {
        submitButton.disabled = false;
        submitButton.innerHTML = "<i data-lucide='scan-search'></i> Compare Resume";
        refreshIcons();
    }
}

async function findScore(event) {
    event.preventDefault();
    const id = elements.scoreApplicationId.value;
    if (!id) {
        showToast("Enter an application ID.");
        return;
    }

    try {
        const application = await requestJson(`/api/applications/${id}`);
        renderScore(application);
    } catch (error) {
        showToast(error.message);
    }
}

function renderScore(application) {
    const score = Number(application.score || 0);
    const scoreAngle = Math.max(0, Math.min(score, 100)) * 3.6;

    elements.scoreRing.style.setProperty("--score-angle", `${scoreAngle}deg`);
    elements.scoreValue.textContent = Math.round(score);
    elements.scoreLabel.textContent = application.scoreLabel;
    elements.scoreName.textContent = `${application.fullName} for ${application.job.title}`;
    elements.scoreSummary.textContent = application.scoreSummary;
    elements.visualScore.textContent = `${Math.round(score)}%`;
    elements.scoreApplicationId.value = application.id;
    renderPills(elements.matchedSkills, application.matchedSkills, "No matched skills yet");
    renderPills(elements.missingSkills, application.missingSkills, "No missing skills");
    renderSuggestions(elements.resumeSuggestions, application.suggestions);
}

function renderPills(container, items, emptyText) {
    if (!items || !items.length) {
        container.innerHTML = `<span class="pill">${escapeHtml(emptyText)}</span>`;
        return;
    }
    container.innerHTML = items.map((item) => `<span class="pill">${escapeHtml(item)}</span>`).join("");
}

function renderSuggestions(container, suggestions) {
    if (!suggestions || !suggestions.length) {
        container.innerHTML = "<li>No resume suggestions available.</li>";
        return;
    }
    container.innerHTML = suggestions.map((suggestion) => `<li>${escapeHtml(suggestion)}</li>`).join("");
}

function renderComparisonResults(comparisons) {
    if (!comparisons || !comparisons.length) {
        elements.comparisonResults.innerHTML = "<div class='empty-panel'>No comparison results found.</div>";
        return;
    }

    elements.comparisonResults.innerHTML = comparisons.map((comparison) => {
        const score = Number(comparison.score || 0);
        const matchedCount = comparison.matchedSkills?.length || 0;
        const missingCount = comparison.missingSkills?.length || 0;

        return `
            <article class="job-match-card">
                <div class="job-match-top">
                    <span class="rank-badge">#${comparison.rank}</span>
                    <div>
                        <strong>${escapeHtml(comparison.job.title)}</strong>
                        <small>${escapeHtml(comparison.job.company)} / ${escapeHtml(comparison.job.location)}</small>
                    </div>
                    <span class="score-badge">${score}</span>
                </div>
                <div class="match-meter" style="--match-width: ${Math.max(4, Math.min(score, 100))}%;">
                    <span></span>
                </div>
                <p>${escapeHtml(comparison.scoreSummary)}</p>
                <div class="match-counts">
                    <span>${matchedCount} matched</span>
                    <span>${missingCount} missing</span>
                </div>
                <div class="pill-list">${renderPillMarkup((comparison.matchedSkills || []).slice(0, 5), "No matched skills")}</div>
            </article>
        `;
    }).join("");

    refreshIcons();
}

async function loadStats() {
    const stats = await requestJson("/api/applications/stats");
    elements.totalApplications.textContent = stats.totalApplications;
    elements.shortlistedApplications.textContent = stats.shortlistedApplications;
    elements.averageScore.textContent = stats.averageScore;
}

async function loadDashboard() {
    const dashboard = await requestJson("/api/applications/dashboard");
    state.dashboard = dashboard;
    renderDashboard(dashboard);
}

function renderDashboard(dashboard) {
    elements.dashboardTotalApplications.textContent = dashboard.totalApplications;
    elements.dashboardAverageScore.textContent = dashboard.averageResumeScore;
    elements.dashboardTodayApplications.textContent = dashboard.todayApplications;
    renderTopSkillsChart(dashboard.topSkills || []);
    renderDailyApplicationsChart(dashboard.dailyApplications || []);
    refreshIcons();
}

function renderTopSkillsChart(skills) {
    if (!skills.length) {
        elements.topSkillsChart.innerHTML = "<div class='empty-panel'>No skill data yet.</div>";
        return;
    }

    const maxCount = Math.max(...skills.map((skill) => Number(skill.count || 0)), 1);
    elements.topSkillsChart.innerHTML = skills.map((skill) => {
        const count = Number(skill.count || 0);
        const width = Math.max(8, (count / maxCount) * 100);
        return `
            <div class="bar-row">
                <div class="bar-meta">
                    <strong>${escapeHtml(skill.skill)}</strong>
                    <span>${count}</span>
                </div>
                <div class="bar-track">
                    <span style="width: ${width}%;"></span>
                </div>
            </div>
        `;
    }).join("");
}

function renderDailyApplicationsChart(days) {
    if (!days.length) {
        elements.dailyApplicationsChart.innerHTML = "<div class='empty-panel'>No daily application data yet.</div>";
        return;
    }

    const maxCount = Math.max(...days.map((day) => Number(day.count || 0)), 1);
    elements.dailyApplicationsChart.innerHTML = days.map((day) => {
        const count = Number(day.count || 0);
        const height = count === 0 ? 8 : Math.max(14, (count / maxCount) * 100);
        return `
            <div class="daily-bar">
                <div class="daily-bar-track">
                    <span style="height: ${height}%;"></span>
                </div>
                <strong>${count}</strong>
                <small>${formatShortDate(day.date)}</small>
            </div>
        `;
    }).join("");
}

async function loginAdmin(event) {
    event.preventDefault();
    const submitButton = elements.adminLoginForm.querySelector("button[type='submit']");
    const formData = new FormData(elements.adminLoginForm);

    try {
        submitButton.disabled = true;
        submitButton.innerHTML = "<i data-lucide='loader-circle'></i> Checking";
        refreshIcons();

        const session = await requestJson("/api/admin/login", {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify({
                username: formData.get("username"),
                password: formData.get("password")
            })
        });

        setAdminSession(session);
        elements.adminLoginForm.reset();
        renderAdminAuthState();
        await loadAdminData();
        showToast("Admin login successful.");
    } catch (error) {
        elements.adminLoginStatus.textContent = error.message;
        showToast(error.message);
    } finally {
        submitButton.disabled = false;
        submitButton.innerHTML = "<i data-lucide='shield-check'></i> Login to Admin";
        refreshIcons();
    }
}

async function logoutAdmin() {
    try {
        if (state.adminToken) {
            await adminRequestJson("/api/admin/logout", { method: "POST" });
        }
    } catch (error) {
        showToast(error.message);
    } finally {
        clearAdminSession();
        renderAdminAuthState();
        showToast("Admin logged out.");
    }
}

async function loadAdminData() {
    if (!state.adminToken) {
        renderAdminAuthState();
        return;
    }

    try {
        await Promise.all([loadAdminStats(), loadApplications(), loadRegisteredUsers()]);
    } catch (error) {
        if (error.status === 401) {
            clearAdminSession();
            renderAdminAuthState();
        }
        throw error;
    }
}

async function loadAdminStats() {
    const stats = await adminRequestJson("/api/admin/stats");
    elements.adminTotalApplications.textContent = stats.totalApplications;
    elements.adminShortlistedApplications.textContent = stats.shortlistedApplications;
    elements.adminAverageScore.textContent = stats.averageScore;
}

function setAdminSession(session) {
    state.adminToken = session.token;
    state.adminUser = session.username;
    localStorage.setItem(ADMIN_TOKEN_KEY, session.token);
    localStorage.setItem(ADMIN_USER_KEY, session.username);
}

function clearAdminSession() {
    state.adminToken = null;
    state.adminUser = null;
    state.applications = [];
    state.expandedApplicationId = null;
    localStorage.removeItem(ADMIN_TOKEN_KEY);
    localStorage.removeItem(ADMIN_USER_KEY);
}

function renderAdminAuthState() {
    const loggedIn = Boolean(state.adminToken);
    elements.adminLoginPanel.classList.toggle("is-hidden", loggedIn);
    elements.adminDashboard.classList.toggle("is-hidden", !loggedIn);
    elements.adminSession.classList.toggle("is-hidden", !loggedIn);
    elements.adminIdentity.textContent = state.adminUser || "admin";

    if (!loggedIn) {
        elements.applicationsTable.innerHTML = "<tr><td colspan='9' class='empty-state'>Login to view applications.</td></tr>";
        elements.candidateRankingPanel.innerHTML = "<div class='empty-panel'>Login to view candidate ranking.</div>";
        elements.registeredUsersTable.innerHTML = "<tr><td colspan='7' class='empty-state'>Login to view registered users.</td></tr>";
        elements.registeredUsersSummary.textContent = "Admin access required";
        elements.adminLoginStatus.textContent = "Default local credentials are admin / admin123 unless changed in environment variables.";
    }
    refreshIcons();
}

async function loadApplications() {
    if (!state.adminToken) {
        return;
    }

    const sort = elements.adminSort.value;
    const shortlisted = elements.shortlistedOnly.checked;
    const applications = await adminRequestJson(`/api/admin/applications?sort=${encodeURIComponent(sort)}&shortlisted=${shortlisted}`);
    state.applications = applications;
    renderApplications(applications);
}

async function loadRegisteredUsers() {
    if (!state.adminToken) {
        return;
    }

    const users = await adminRequestJson("/api/admin/users");
    state.registeredUsers = users;
    renderRegisteredUsers(users);
}

function renderRegisteredUsers(users) {
    const loggedInCount = users.filter((user) => user.loggedIn).length;
    elements.registeredUsersSummary.textContent = `${users.length} registered / ${loggedInCount} logged in`;

    if (!users.length) {
        elements.registeredUsersTable.innerHTML = "<tr><td colspan='7' class='empty-state'>No registered users yet.</td></tr>";
        return;
    }

    elements.registeredUsersTable.innerHTML = users.map((user, index) => `
        <tr>
            <td>#${index + 1}</td>
            <td>
                <div class="candidate-cell">
                    <strong>${escapeHtml(user.fullName)}</strong>
                    <span>@${escapeHtml(user.username)}</span>
                </div>
            </td>
            <td>${escapeHtml(user.email)}</td>
            <td>${formatDate(user.registeredAt)}</td>
            <td>${formatDate(user.lastLoginAt)}</td>
            <td>${user.loginCount}</td>
            <td>
                <span class="status-badge ${user.loggedIn ? "shortlisted" : ""}">
                    ${user.loggedIn ? "Logged in" : "Logged out"}
                </span>
            </td>
        </tr>
    `).join("");
}

function renderApplications(applications) {
    if (!applications.length) {
        elements.applicationsTable.innerHTML = "<tr><td colspan='9' class='empty-state'>No applications found.</td></tr>";
        renderCandidateRanking([]);
        return;
    }

    elements.applicationsTable.innerHTML = applications.map(renderApplicationRows).join("");
    renderCandidateRanking(applications);

    refreshIcons();
}

function renderApplicationRows(application) {
    const expanded = state.expandedApplicationId === Number(application.id);

    return `
        <tr>
            <td><span class="rank-badge">${application.rank ? `#${application.rank}` : "-"}</span></td>
            <td>#${application.id}</td>
            <td>
                <div class="candidate-cell">
                    <strong>${escapeHtml(application.fullName)}</strong>
                    <span>${escapeHtml(application.scoreLabel)}</span>
                </div>
            </td>
            <td>
                <div class="job-cell">
                    <strong>${escapeHtml(application.job.title)}</strong>
                    <span>${escapeHtml(application.job.company)} / ${formatExperience(application.job.minExperience)}</span>
                </div>
            </td>
            <td>
                <div class="performance-cell">
                    <span class="score-badge">${application.score}</span>
                    <span class="status-badge ${application.shortlisted ? "shortlisted" : ""}">${escapeHtml(application.status)}</span>
                </div>
            </td>
            <td>
                <div class="candidate-cell">
                    <span>${escapeHtml(application.email)}</span>
                    <span>${escapeHtml(application.phone)}</span>
                </div>
            </td>
            <td>
                <button class="icon-action" type="button" data-download-id="${application.id}" title="Download resume">
                    <i data-lucide="download"></i>
                    Resume
                </button>
                <span class="file-note">${escapeHtml(application.resumeFileName)} / ${formatFileSize(application.resumeSize)}</span>
            </td>
            <td>${formatDate(application.appliedAt)}</td>
            <td>
                <div class="table-actions">
                    <button class="icon-action" type="button" data-details-id="${application.id}">
                        <i data-lucide="${expanded ? "chevron-up" : "list-collapse"}"></i>
                        ${expanded ? "Hide" : "Details"}
                    </button>
                    <button class="icon-action" type="button" data-shortlist-id="${application.id}" data-shortlisted="${application.shortlisted}">
                        <i data-lucide="${application.shortlisted ? "x-circle" : "check-circle"}"></i>
                        ${application.shortlisted ? "Remove" : "Shortlist"}
                    </button>
                </div>
            </td>
        </tr>
        ${expanded ? renderApplicationDetails(application) : ""}
    `;
}

function renderCandidateRanking(applications) {
    if (!applications.length) {
        elements.candidateRankingPanel.innerHTML = "<div class='empty-panel'>Ranked candidates will appear after applications are submitted.</div>";
        return;
    }

    const rankedApplications = [...applications]
        .sort((first, second) => Number(first.rank || 9999) - Number(second.rank || 9999))
        .slice(0, 5);

    elements.candidateRankingPanel.innerHTML = `
        <div class="ranking-heading">
            <div>
                <p class="eyebrow">Candidate ranking</p>
                <h2>Automatically ranked by resume score</h2>
            </div>
            <span>${applications.length} candidate${applications.length === 1 ? "" : "s"}</span>
        </div>
        <div class="ranking-list">
            ${rankedApplications.map((application) => `
                <article class="ranking-item">
                    <span class="rank-badge">#${application.rank}</span>
                    <div>
                        <strong>${escapeHtml(application.fullName)}</strong>
                        <small>${escapeHtml(application.job.title)} / ${escapeHtml(application.scoreLabel)}</small>
                    </div>
                    <span class="score-badge">${application.score}</span>
                </article>
            `).join("")}
        </div>
    `;
}

function renderApplicationDetails(application) {
    return `
        <tr class="details-row">
            <td colspan="9">
                <div class="application-details">
                    <section>
                        <h3>Candidate Details</h3>
                        <dl>
                            ${renderDetailItem("Experience", `${application.experienceYears} year${application.experienceYears === 1 ? "" : "s"}`)}
                            ${renderDetailItem("Education", application.education || "Not provided")}
                            ${renderDetailItem("Skills", application.skills || "Not provided")}
                            ${renderDetailItem("Resume", `${application.resumeFileName || "No file"} (${formatFileSize(application.resumeSize)})`)}
                        </dl>
                    </section>
                    <section>
                        <h3>Performance</h3>
                        <p>${escapeHtml(application.scoreSummary)}</p>
                        <div class="skill-columns compact">
                            <div>
                                <strong>Matched</strong>
                                <div class="pill-list">${renderPillMarkup(application.matchedSkills, "No matched skills")}</div>
                            </div>
                            <div>
                                <strong>Missing</strong>
                                <div class="pill-list warning">${renderPillMarkup(application.missingSkills, "No missing skills")}</div>
                            </div>
                        </div>
                    </section>
                    <section>
                        <h3>Resume Suggestions</h3>
                        <ul class="suggestion-list">
                            ${renderSuggestionMarkup(application.suggestions)}
                        </ul>
                    </section>
                </div>
            </td>
        </tr>
    `;
}

function renderDetailItem(label, value) {
    return `
        <dt>${escapeHtml(label)}</dt>
        <dd>${escapeHtml(value)}</dd>
    `;
}

function renderPillMarkup(items, emptyText) {
    const values = items && items.length ? items : [emptyText];
    return values.map((item) => `<span class="pill">${escapeHtml(item)}</span>`).join("");
}

function renderSuggestionMarkup(suggestions) {
    const values = suggestions && suggestions.length ? suggestions : ["No resume suggestions available."];
    return values.map((suggestion) => `<li>${escapeHtml(suggestion)}</li>`).join("");
}

function toggleApplicationDetails(id) {
    state.expandedApplicationId = state.expandedApplicationId === id ? null : id;
    renderApplications(state.applications);
}

async function updateShortlist(id, shortlisted) {
    try {
        const application = await adminRequestJson(`/api/admin/applications/${id}/shortlist`, {
            method: "PATCH",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify({ shortlisted })
        });
        await Promise.all([loadStats(), loadDashboard(), loadAdminData()]);
        showToast(`${application.fullName} ${shortlisted ? "shortlisted" : "removed from shortlist"}.`);
    } catch (error) {
        showToast(error.message);
    }
}

async function downloadResume(id) {
    try {
        if (!state.adminToken) {
            throw new Error("Admin login is required.");
        }

        const response = await fetch(`/api/admin/applications/${id}/resume`, {
            headers: {
                "Authorization": `Bearer ${state.adminToken}`
            }
        });

        if (!response.ok) {
            const error = await response.json().catch(() => null);
            const message = error?.message || `Download failed with status ${response.status}.`;
            const requestError = new Error(message);
            requestError.status = response.status;
            throw requestError;
        }

        const blob = await response.blob();
        const fileName = fileNameFromDisposition(response.headers.get("Content-Disposition")) || `resume-${id}`;
        const url = URL.createObjectURL(blob);
        const link = document.createElement("a");
        link.href = url;
        link.download = fileName;
        document.body.appendChild(link);
        link.click();
        link.remove();
        URL.revokeObjectURL(url);
    } catch (error) {
        if (error.status === 401) {
            clearAdminSession();
            renderAdminAuthState();
        }
        showToast(error.message);
    }
}

async function userRequestJson(url, options = {}) {
    if (!state.userToken) {
        throw new Error("User login is required.");
    }

    try {
        return await requestJson(url, {
            ...options,
            headers: {
                ...(options.headers || {}),
                "Authorization": `Bearer ${state.userToken}`
            }
        });
    } catch (error) {
        if (error.status === 401) {
            clearUserSession();
            renderUserAuthState();
        }
        throw error;
    }
}

async function adminRequestJson(url, options = {}) {
    if (!state.adminToken) {
        throw new Error("Admin login is required.");
    }

    try {
        return await requestJson(url, {
            ...options,
            headers: {
                ...(options.headers || {}),
                "Authorization": `Bearer ${state.adminToken}`
            }
        });
    } catch (error) {
        if (error.status === 401) {
            clearAdminSession();
            renderAdminAuthState();
        }
        throw error;
    }
}

async function requestJson(url, options = {}) {
    const response = await fetch(url, options);
    if (!response.ok) {
        const error = await response.json().catch(() => null);
        const requestError = new Error(error?.message || `Request failed with status ${response.status}.`);
        requestError.status = response.status;
        throw requestError;
    }
    if (response.status === 204) {
        return null;
    }
    const contentType = response.headers.get("Content-Type") || "";
    if (!contentType.includes("application/json")) {
        return response.text();
    }
    return response.json();
}

function showToast(message) {
    elements.toast.textContent = message;
    elements.toast.classList.add("show");
    window.clearTimeout(showToast.timeout);
    showToast.timeout = window.setTimeout(() => {
        elements.toast.classList.remove("show");
    }, 3400);
}

function refreshIcons() {
    if (window.lucide) {
        window.lucide.createIcons();
    }
}

function formatExperience(minExperience) {
    const years = Number(minExperience || 0);
    if (years <= 0) {
        return "Fresher friendly";
    }
    return `Min ${years} year${years === 1 ? "" : "s"}`;
}

function formatFileSize(bytes) {
    const size = Number(bytes || 0);
    if (size <= 0) {
        return "0 KB";
    }
    if (size < 1024 * 1024) {
        return `${Math.ceil(size / 1024)} KB`;
    }
    return `${(size / (1024 * 1024)).toFixed(1)} MB`;
}

function formatDate(value) {
    if (!value) {
        return "Not available";
    }
    return new Intl.DateTimeFormat("en-IN", {
        dateStyle: "medium",
        timeStyle: "short"
    }).format(new Date(value));
}

function formatShortDate(value) {
    if (!value) {
        return "";
    }
    return new Intl.DateTimeFormat("en-IN", {
        day: "2-digit",
        month: "short"
    }).format(new Date(`${value}T00:00:00`));
}

function fileNameFromDisposition(disposition) {
    if (!disposition) {
        return "";
    }
    const match = disposition.match(/filename="?([^"]+)"?/i);
    return match ? match[1] : "";
}

function escapeHtml(value) {
    return String(value ?? "")
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll('"', "&quot;")
        .replaceAll("'", "&#039;");
}
