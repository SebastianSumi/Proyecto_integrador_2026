<#-- Recuperar acceso: pide el correo y Keycloak envía el enlace de verificación. -->
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="utf-8"/>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="robots" content="noindex, nofollow"/>
    <meta name="viewport" content="width=device-width, initial-scale=1"/>
    <title>Saludablemente — Recuperar acceso</title>
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
                <h1>Recuperar acceso</h1>
            </div>
        </div>

        <p class="sal-hint">Escribe tu correo y te enviaremos un enlace para crear una nueva contraseña.</p>

        <#if message?has_content && (message.summary)?has_content>
            <div class="sal-alert sal-${message.type!'error'}" role="alert">
                ${kcSanitize(message.summary)?no_esc}
            </div>
        </#if>

        <form id="kc-reset-password-form" class="sal-form" action="${url.loginAction}" method="post">
            <label class="sal-field" for="username">
                <span>Correo electrónico</span>
                <input id="username" name="username" type="email" autocomplete="email"
                       value="${(auth.attemptedUsername)!''}" autofocus/>
            </label>
            <#if messagesPerField.existsError('username')>
                <span class="sal-field-error">${kcSanitize(messagesPerField.getFirstError('username',''))?no_esc}</span>
            </#if>

            <button class="sal-btn" type="submit">Enviar enlace</button>
        </form>

        <aside class="sal-crear">
            <a class="sal-link" href="${url.loginUrl}">Volver a iniciar sesión</a>
        </aside>
    </div>
</div>
</body>
</html>
