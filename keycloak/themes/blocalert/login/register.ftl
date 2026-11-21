
<#import "template.ftl" as layout>
<#import "field.ftl" as field>
<#import "user-profile-commons.ftl" as userProfileCommons>
<#import "register-commons.ftl" as registerCommons>
<#import "password-validation.ftl" as validator>

<@layout.registrationLayout
    displayMessage=messagesPerField.exists('global')
    displayRequiredFields=true
    ; section
>

    <#if section = "header">

        <#-- Custom header is rendered inside the form card -->

    <#elseif section = "form">

        <#-- BlocAlert branding -->
        <div class="blocalert-brand">

            <img
                src="${url.resourcesPath}/img/blocalert-logo.png"
                alt="BlocAlert"
                class="blocalert-logo"
            />

        </div>

        <#-- Registration card -->
        <div class="blocalert-login-card blocalert-register-card">

            <div class="blocalert-login-header">

                <h1>Create account</h1>

                <p>Join BlocAlert and start monitoring your alerts</p>

            </div>

            <form
                id="kc-register-form"
                class="blocalert-register-form"
                action="${url.registrationAction}"
                method="post"
                novalidate="novalidate"
            >

                <#--
                    Render Keycloak user profile fields.
                    This supports username, email, first name,
                    last name and custom configured attributes.
                -->

                <@userProfileCommons.userProfileFormFields; callback, attribute>

                    <#if callback = "afterField">

                        <#-- Render password fields after username/email -->

                        <#if passwordRequired??
                            && (
                                attribute.name == 'username'
                                ||
                                (attribute.name == 'email' && realm.registrationEmailAsUsername)
                            )
                        >

                            <@field.password
                                name="password"
                                required=true
                                label=msg("password")
                                autocomplete="new-password"
                            />

                            <@field.password
                                name="password-confirm"
                                required=true
                                label=msg("passwordConfirm")
                                autocomplete="new-password"
                            />

                        </#if>

                    </#if>

                </@userProfileCommons.userProfileFormFields>


                <#-- Terms and conditions -->

                <@registerCommons.termsAcceptance/>


                <#-- Visible reCAPTCHA -->

                <#if recaptchaRequired?? && (recaptchaVisible!false)>

                    <div class="blocalert-recaptcha">

                        <div class="g-recaptcha"
                             data-size="compact"
                             data-sitekey="${recaptchaSiteKey}"
                             data-action="${recaptchaAction}">
                        </div>

                    </div>

                </#if>


                <#-- Registration button with invisible reCAPTCHA -->

                <#if recaptchaRequired?? && !(recaptchaVisible!false)>

                    <script>

                        function onSubmitRecaptcha(token) {

                            document
                                .getElementById("kc-register-form")
                                .requestSubmit();

                        }

                    </script>

                    <div id="kc-form-buttons">

                        <button
                            class="blocalert-submit g-recaptcha"
                            data-sitekey="${recaptchaSiteKey}"
                            data-callback="onSubmitRecaptcha"
                            data-action="${recaptchaAction}"
                            type="submit"
                            id="kc-submit"
                        >
                            Create Account
                        </button>

                    </div>

                <#else>

                    <div id="kc-form-buttons">

                        <button
                            type="submit"
                            id="kc-submit"
                            class="blocalert-submit"
                        >
                            Create Account
                        </button>

                    </div>

                </#if>


                <#-- Back to login -->

                <div class="blocalert-register">

                    <span>Already have an account?</span>

                    <a href="${url.loginUrl}">
                        Sign in
                    </a>

                </div>

            </form>

            <@validator.templates/>

            <@validator.script field="password"/>

        </div>

    </#if>

</@layout.registrationLayout>
