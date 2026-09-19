<#import "template.ftl" as layout>

<@layout.registrationLayout
    displayMessage=true
    displayInfo=false
    ; section
>

<#if section = "header">

    <div class="blocalert-brand">
        <img
            src="${url.resourcesPath}/img/blocalert-logo.png"
            alt="BlocAlert"
            class="blocalert-logo"
        />
    </div>

<#elseif section = "form">

    <div class="blocalert-login-card">

        <div class="blocalert-login-header">
            <h1>Welcome</h1>
            <p>Log in to BlocAlert</p>
        </div>

        <form
            id="kc-form-login"
            action="${url.loginAction}"
            method="post"
        >

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
                />

            </div>

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
                    />

                    <button
                        type="button"
                        class="blocalert-password-toggle"
                        onclick="togglePassword()"
                        aria-label="Show password"
                    >
                        👁
                    </button>

                </div>

            </div>

            <#if messagesPerField.existsError('username','password')>

                <div class="blocalert-error">
                    ${kcSanitize(
                        messagesPerField.getFirstError('username','password')
                    )?no_esc}
                </div>

            </#if>

            <div class="blocalert-options">

                <#if realm.resetPasswordAllowed>

                    <a href="${url.loginResetCredentialsUrl}">
                        Forgot password?
                    </a>

                </#if>

            </div>

            <button
                type="submit"
                class="blocalert-submit"
                name="login"
                id="kc-login"
            >
                Continue
            </button>

        </form>

        <#if realm.registrationAllowed>

            <div class="blocalert-register">

                <span>Don't have an account?</span>

                <a href="${url.registrationUrl}">
                    Sign up
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
                >

                    <#if p.alias == "google">

                        <span class="google-icon">
                            G
                        </span>

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

    const password =
        document.getElementById("password");

    if (password.type === "password") {

        password.type = "text";

    } else {

        password.type = "password";

    }

}

</script>