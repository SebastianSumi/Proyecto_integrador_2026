<#-- Tema de login propio "saludablemente".
     Keycloak sigue en el camino (mismo flujo, mismos tokens, mismo SSO):
     solo cambia la interfaz. Hereda el resto de plantillas del tema padre. -->
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="utf-8"/>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="robots" content="noindex, nofollow"/>
    <meta name="viewport" content="width=device-width, initial-scale=1"/>
    <title>Saludablemente — Iniciar sesión</title>
    <link rel="icon" href="${url.resourcesPath}/img/logo.svg"/>
    <link rel="stylesheet" href="${url.resourcesPath}/css/login.css"/>
</head>
<body>
<div class="sal-fondo">
    <div class="sal-card">
        <a class="sal-volver" href="${properties.frontendLoginUrl!'/login'}">&#8592; Volver</a>
        <div class="sal-marca">
            <img class="sal-logo" src="${url.resourcesPath}/img/logo.svg" alt="Saludablemente"/>
            <div>
                <span class="sal-badge">Saludablemente ERP</span>
                <h1>Iniciar sesión</h1>
            </div>
        </div>

        <p class="sal-hint">Inicia sesión para continuar a tu cuenta.</p>

        <#if message?has_content && (message.summary)?has_content>
            <div class="sal-alert sal-${message.type!'error'}" role="alert">
                ${kcSanitize(message.summary)?no_esc}
            </div>
        </#if>

        <form id="kc-form-login" class="sal-form" action="${url.loginAction}" method="post">
            <label class="sal-field" for="username">
                <span>Correo electrónico</span>
                <input id="username" name="username" type="email" autocomplete="email"
                       value="${(login.username)!''}" autofocus/>
            </label>
            <#if messagesPerField.existsError('username')>
                <span class="sal-field-error">${kcSanitize(messagesPerField.getFirstError('username',''))?no_esc}</span>
            </#if>

            <label class="sal-field" for="password">
                <span>Contraseña</span>
                <input id="password" name="password" type="password" autocomplete="current-password"/>
            </label>
            <#if messagesPerField.existsError('password')>
                <span class="sal-field-error">${kcSanitize(messagesPerField.getFirstError('password',''))?no_esc}</span>
            </#if>

                <div class="sal-row">
                    <a class="sal-link" href="${url.loginResetCredentialsUrl}">¿Olvidaste tu contraseña?</a>
                </div>

            <input type="hidden" id="id-hidden-input" name="credentialId" value="${auth.selectedCredential!''}"/>

            <#if realm.rememberMe>
                <label class="sal-check">
                    <input id="rememberMe" name="remember-me" type="checkbox"
                           <#if login.rememberMe?? && login.rememberMe=="on">checked</#if>/>
                    Mantener la sesión iniciada
                </label>
            </#if>

            <button class="sal-btn" name="login" id="kc-login" type="submit">Iniciar sesión</button>
        </form>

        <#if properties.frontendRegistroUrl?has_content>
            <aside class="sal-crear">
                ¿Necesitas una cuenta? <a class="sal-link" href="${properties.frontendRegistroUrl}">Crea una</a>
            </aside>
        <#elseif realm.password && realm.registrationAllowed && !(registrationDisabled!false)>
            <aside class="sal-crear">
                ¿Necesitas una cuenta? <a class="sal-link" href="${url.registrationUrl}">Crea una</a>
            </aside>
        </#if>
    </div>
</div>
</body>
</html>
