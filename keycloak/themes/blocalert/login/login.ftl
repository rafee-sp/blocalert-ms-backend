
<#import "template.ftl" as layout>

<@layout.registrationLayout
    displayMessage=true
    displayInfo=false
    ; section
>

    <#if section = "header">

        <#-- Suppress the default Keycloak header -->

    <#elseif section = "form">

        <#-- BlocAlert logo -->
        <div class="blocalert-brand">
            <img
                src="${url.resourcesPath}/img/blocalert-logo.png"
                alt="BlocAlert"
                class="blocalert-logo"
            />
        </div>

        <#-- Login card -->
        <div class="blocalert-login-card">

            <div class="blocalert-login-header">
                <h1>Welcome back</h1>
                <p>Sign in to continue to BlocAlert</p>
            </div>

            <form
                id="kc-form-login"
                action="${url.loginAction}"
                method="post"
            >

                <#-- Username / Email -->
                <div class="blocalert-field">

                    <label for="username">
                        <#if !realm.loginWithEmailAllowed>
                            Username
                        <#elseif !realm.registrationEmailAsUsername>
                            Username or Email address
                        <#else>
                            Email address
                        </#if>
                    </label>

                    <input
                        id="username"
                        name="username"
                        type="text"
                        value="${(login.username!'')}"
                        autocomplete="username"
                        autofocus
                        required
                    />

                </div>

                <#-- Password -->
                <div class="blocalert-field">

                    <label for="password">
                        Password
                    </label>

                    <div class="blocalert-password-wrapper">

                        <input
                            id="password"
                            name="password"
                            type="password"
                            autocomplete="current-password"
                            required
                        />

                        <button
                            type="button"
                            class="blocalert-password-toggle"
                            onclick="togglePassword()"
                            aria-label="Show password"
                            id="password-toggle"
                        >
                            <svg
                                xmlns="http://www.w3.org/2000/svg"
                                width="18"
                                height="18"
                                viewBox="0 0 24 24"
                                fill="none"
                                stroke="currentColor"
                                stroke-width="2"
                                stroke-linecap="round"
                                stroke-linejoin="round"
                            >
                                <path d="M2 12s3.5-7 10-7 10 7 10 7-3.5 7-10 7-10-7-10-7z"/>
                                <circle cx="12" cy="12" r="3"/>
                            </svg>
                        </button>

                    </div>

                </div>

                <#-- Login errors -->
                <#if messagesPerField.existsError('username','password')>

                    <div class="blocalert-error" role="alert">
                        ${kcSanitize(
                            messagesPerField.getFirstError('username','password')
                        )?no_esc}
                    </div>

                </#if>

                <#-- Remember me / Forgot password -->
                <div class="blocalert-options">

                    <#if realm.rememberMe>

                        <label class="blocalert-remember">

                            <input
                                id="rememberMe"
                                name="rememberMe"
                                type="checkbox"
                                <#if login.rememberMe?? && login.rememberMe>
                                    checked
                                </#if>
                            />

                            <span>Remember me</span>

                        </label>

                    </#if>

                    <#if realm.resetPasswordAllowed>

                        <a href="${url.loginResetCredentialsUrl}">
                            Forgot password?
                        </a>

                    </#if>

                </div>

                <#-- Submit -->
                <button
                    type="submit"
                    class="blocalert-submit"
                    name="login"
                    id="kc-login"
                >
                    Sign In
                </button>

            </form>

            <#-- Registration -->
            <#if realm.registrationAllowed>

                <div class="blocalert-register">

                    <span>Don't have an account?</span>

                    <a href="${url.registrationUrl}">
                        Create account
                    </a>

                </div>

            </#if>

        </div>

    <#elseif section = "socialProviders">

        <#if realm.password && social?? && social.providers?has_content>

            <div class="blocalert-social-section">

                <div class="blocalert-divider">
                    <span>OR</span>
                </div>

                <#list social.providers as p>

                    <a
                        href="${p.loginUrl}"
                        class="blocalert-google"
                        id="social-${p.alias}"
                    >

                        <#if p.alias == "google">
                            <span class="google-icon">G</span>
                        </#if>

                        <span>
                            Continue with ${p.displayName}
                        </span>

                    </a>

                </#list>

            </div>

        </#if>

    </#if>

</@layout.registrationLayout>


<script>

    function togglePassword() {

        const password = document.getElementById("password");
        const toggle = document.getElementById("password-toggle");

        if (!password || !toggle) {
            return;
        }

        if (password.type === "password") {

            password.type = "text";
            toggle.setAttribute("aria-label", "Hide password");

        } else {

            password.type = "password";
            toggle.setAttribute("aria-label", "Show password");

        }

    }

</script>
