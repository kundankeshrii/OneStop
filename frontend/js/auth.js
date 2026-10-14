// ============================================================
// ONESTOP AUTHENTICATION
// ============================================================

const AUTH_API_URL = "http://localhost:8080/api/auth";

const GOOGLE_LOGIN_URL =
    "http://localhost:8080/oauth2/authorization/google";

const TOKEN_KEY = "onestop_token";


// ============================================================
// DOM
// ============================================================

const userAccount =
    document.getElementById("userAccount");

const authOverlay =
    document.getElementById("authOverlay");

const authClose =
    document.getElementById("authClose");

const loginView =
    document.getElementById("loginView");

const signupView =
    document.getElementById("signupView");

const loginForm =
    document.getElementById("loginForm");

const signupForm =
    document.getElementById("signupForm");

const showSignupBtn =
    document.getElementById("showSignupBtn");

const showLoginBtn =
    document.getElementById("showLoginBtn");

const googleLoginBtn =
    document.getElementById("googleLoginBtn");

const googleSignupBtn =
    document.getElementById("googleSignupBtn");

const accountPopover =
    document.getElementById("accountPopover");

const accountLoginBtn =
    document.getElementById("accountLoginBtn");

const accountLogoutBtn =
    document.getElementById("accountLogoutBtn");


// ============================================================
// STATE
// ============================================================

let currentUser = null;


// ============================================================
// OPEN AUTH MODAL
// ============================================================

function openAuthModal(view = "login") {

    if (!authOverlay) {
        console.error("Auth overlay not found");
        return;
    }

    authOverlay.classList.remove("hidden");

    if (accountPopover) {
        accountPopover.classList.add("hidden");
    }

    if (view === "signup") {
        showSignupView();
    } else {
        showLoginView();
    }

    document.body.classList.add("auth-open");
}


// ============================================================
// CLOSE AUTH MODAL
// ============================================================

function closeAuthModal() {

    if (!authOverlay) {
        return;
    }

    authOverlay.classList.add("hidden");

    document.body.classList.remove("auth-open");

    clearAuthErrors();
}


// ============================================================
// LOGIN VIEW
// ============================================================

function showLoginView() {

    if (!loginView || !signupView) {
        return;
    }

    loginView.classList.remove("hidden");
    signupView.classList.add("hidden");

    clearAuthErrors();
}


// ============================================================
// SIGNUP VIEW
// ============================================================

function showSignupView() {

    if (!loginView || !signupView) {
        return;
    }

    loginView.classList.add("hidden");
    signupView.classList.remove("hidden");

    clearAuthErrors();
}


// ============================================================
// AUTH ERRORS
// ============================================================

function clearAuthErrors() {

    const loginError =
        document.getElementById("loginError");

    const signupError =
        document.getElementById("signupError");

    if (loginError) {

        loginError.textContent = "";

        loginError.classList.add("hidden");

    }

    if (signupError) {

        signupError.textContent = "";

        signupError.classList.add("hidden");

    }
}


function showLoginError(message) {

    const element =
        document.getElementById("loginError");

    if (!element) {
        return;
    }

    element.textContent = message;

    element.classList.remove("hidden");
}


function showSignupError(message) {

    const element =
        document.getElementById("signupError");

    if (!element) {
        return;
    }

    element.textContent = message;

    element.classList.remove("hidden");
}


// ============================================================
// TOKEN
// ============================================================

function getToken() {

    return localStorage.getItem(TOKEN_KEY);

}


function saveToken(token) {

    localStorage.setItem(
        TOKEN_KEY,
        token
    );

}


function removeToken() {

    localStorage.removeItem(
        TOKEN_KEY
    );

}


// ============================================================
// LOGIN
// ============================================================

if (loginForm) {

    loginForm.addEventListener(
        "submit",
        async function (event) {

            event.preventDefault();

            clearAuthErrors();

            const email =
                document
                    .getElementById("loginEmail")
                    .value
                    .trim();

            const password =
                document
                    .getElementById("loginPassword")
                    .value;

            const loginBtn =
                document.getElementById("loginBtn");

            loginBtn.disabled = true;

            loginBtn.textContent =
                "Signing in...";


            try {

                const response =
                    await fetch(
                        `${AUTH_API_URL}/login`,
                        {
                            method: "POST",

                            headers: {
                                "Content-Type":
                                    "application/json"
                            },

                            body: JSON.stringify({
                                email: email,
                                password: password
                            })
                        }
                    );


                const data =
                    await response.json();


                if (!response.ok) {

                    throw new Error(
                        data.message ||
                        "Invalid email or password"
                    );

                }


                if (!data.token) {

                    throw new Error(
                        "Authentication token was not received."
                    );

                }


                saveToken(data.token);


                currentUser = {

                    id: data.userId,

                    name: data.name,

                    email: data.email

                };


                updateAccountUI();

                closeAuthModal();


                console.log(
                    "Login successful"
                );


            } catch (error) {

                console.error(
                    "Login error:",
                    error
                );

                showLoginError(
                    error.message ||
                    "Unable to sign in."
                );

            } finally {

                loginBtn.disabled = false;

                loginBtn.textContent =
                    "Sign in";

            }

        }
    );

}


// ============================================================
// SIGNUP
// ============================================================

if (signupForm) {

    signupForm.addEventListener(
        "submit",
        async function (event) {

            event.preventDefault();

            clearAuthErrors();

            const name =
                document
                    .getElementById("signupName")
                    .value
                    .trim();

            const email =
                document
                    .getElementById("signupEmail")
                    .value
                    .trim();

            const password =
                document
                    .getElementById("signupPassword")
                    .value;


            const signupBtn =
                document.getElementById("signupBtn");


            signupBtn.disabled = true;

            signupBtn.textContent =
                "Creating account...";


            try {

                const response =
                    await fetch(
                        `${AUTH_API_URL}/signup`,
                        {
                            method: "POST",

                            headers: {
                                "Content-Type":
                                    "application/json"
                            },

                            body: JSON.stringify({
                                name: name,
                                email: email,
                                password: password
                            })
                        }
                    );


                let data = {};

                try {

                    data =
                        await response.json();

                } catch (_) {

                    // Response may be plain text.

                }


                if (!response.ok) {

                    throw new Error(
                        data.message ||
                        "Unable to create account."
                    );

                }


                document
                    .getElementById("loginEmail")
                    .value = email;

                document
                    .getElementById("loginPassword")
                    .value = "";


                showLoginView();


                showLoginError(
                    "Account created successfully. Please sign in."
                );


            } catch (error) {

                console.error(
                    "Signup error:",
                    error
                );

                showSignupError(
                    error.message ||
                    "Unable to create account."
                );

            } finally {

                signupBtn.disabled = false;

                signupBtn.textContent =
                    "Sign up";

            }

        }
    );

}


// ============================================================
// GOOGLE LOGIN
// ============================================================

function startGoogleLogin() {

    window.location.href =
        GOOGLE_LOGIN_URL;

}


if (googleLoginBtn) {

    googleLoginBtn.addEventListener(
        "click",
        startGoogleLogin
    );

}


if (googleSignupBtn) {

    googleSignupBtn.addEventListener(
        "click",
        startGoogleLogin
    );

}


// ============================================================
// ACCOUNT BUTTON
// ============================================================

if (userAccount) {

    userAccount.addEventListener(
        "click",
        function () {

            if (currentUser) {

                if (accountPopover) {

                    accountPopover.classList.toggle(
                        "hidden"
                    );

                }

            } else {

                openAuthModal("login");

            }

        }
    );

}


// ============================================================
// ACCOUNT LOGIN
// ============================================================

if (accountLoginBtn) {

    accountLoginBtn.addEventListener(
        "click",
        function () {

            if (accountPopover) {

                accountPopover.classList.add(
                    "hidden"
                );

            }

            openAuthModal("login");

        }
    );

}


// ============================================================
// LOGOUT
// ============================================================

if (accountLogoutBtn) {

    accountLogoutBtn.addEventListener(
        "click",
        function () {

            removeToken();

            currentUser = null;

            if (accountPopover) {

                accountPopover.classList.add(
                    "hidden"
                );

            }

            updateAccountUI();

        }
    );

}


// ============================================================
// SWITCH LOGIN / SIGNUP
// ============================================================

if (showSignupBtn) {

    showSignupBtn.addEventListener(
        "click",
        showSignupView
    );

}


if (showLoginBtn) {

    showLoginBtn.addEventListener(
        "click",
        showLoginView
    );

}


// ============================================================
// CLOSE BUTTON
// ============================================================

if (authClose) {

    authClose.addEventListener(
        "click",
        closeAuthModal
    );

}


// ============================================================
// CLICK OUTSIDE MODAL
// ============================================================

if (authOverlay) {

    authOverlay.addEventListener(
        "click",
        function (event) {

            if (event.target === authOverlay) {

                closeAuthModal();

            }

        }
    );

}


// ============================================================
// ESC KEY
// ============================================================

document.addEventListener(
    "keydown",
    function (event) {

        if (
            event.key === "Escape" &&
            authOverlay &&
            !authOverlay.classList.contains("hidden")
        ) {

            closeAuthModal();

        }

    }
);


// ============================================================
// ACCOUNT UI
// ============================================================

function updateAccountUI() {

    if (!userAccount) {
        return;
    }


    const avatar =
        userAccount.querySelector(
            ".user-avatar"
        );

    const name =
        userAccount.querySelector(
            ".user-name"
        );

    const status =
        userAccount.querySelector(
            ".user-status"
        );


    const popoverName =
        document.getElementById(
            "accountPopoverName"
        );

    const popoverEmail =
        document.getElementById(
            "accountPopoverEmail"
        );

    const popoverAvatar =
        document.getElementById(
            "accountPopoverAvatar"
        );


    if (currentUser) {

        const firstLetter =
            (currentUser.name || "U")
                .trim()
                .charAt(0)
                .toUpperCase();


        avatar.textContent =
            firstLetter || "U";

        name.textContent =
            currentUser.name || "User";

        status.textContent =
            currentUser.email || "";


        if (popoverAvatar) {
            popoverAvatar.textContent =
                firstLetter || "U";
        }

        if (popoverName) {
            popoverName.textContent =
                currentUser.name || "User";
        }

        if (popoverEmail) {
            popoverEmail.textContent =
                currentUser.email || "";
        }


        if (accountLoginBtn) {

            accountLoginBtn.classList.add(
                "hidden"
            );

        }

        if (accountLogoutBtn) {

            accountLogoutBtn.classList.remove(
                "hidden"
            );

        }


    } else {

        avatar.textContent = "U";

        name.textContent =
            "Guest User";

        status.textContent =
            "Continue as guest";


        if (popoverAvatar) {
            popoverAvatar.textContent = "U";
        }

        if (popoverName) {
            popoverName.textContent =
                "Guest User";
        }

        if (popoverEmail) {
            popoverEmail.textContent =
                "Continue as guest";
        }


        if (accountLoginBtn) {

            accountLoginBtn.classList.remove(
                "hidden"
            );

        }

        if (accountLogoutBtn) {

            accountLogoutBtn.classList.add(
                "hidden"
            );

        }

    }

}


// ============================================================
// RESTORE LOGIN SESSION
// ============================================================

async function restoreAuthentication() {

    const token =
        getToken();


    if (!token) {

        currentUser = null;

        updateAccountUI();

        return;

    }


    try {

        const response =
            await fetch(
                `${AUTH_API_URL}/me`,
                {
                    method: "GET",

                    headers: {
                        "Authorization":
                            `Bearer ${token}`
                    }
                }
            );


        if (!response.ok) {

            throw new Error(
                "Token expired or invalid"
            );

        }


        const user =
            await response.json();


        currentUser = {

            id: user.id,

            name: user.name,

            email: user.email,

            provider: user.provider,

            profileImage:
                user.profileImage

        };


        updateAccountUI();


    } catch (error) {

        console.warn(
            "Authentication restore failed:",
            error
        );

        removeToken();

        currentUser = null;

        updateAccountUI();

    }

}


// ============================================================
// INITIALIZE
// ============================================================

updateAccountUI();

restoreAuthentication();