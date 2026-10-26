<#-- Nueva contraseña desde el enlace de verificación del correo. -->
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="utf-8"/>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="robots" content="noindex, nofollow"/>
    <meta name="viewport" content="width=device-width, initial-scale=1"/>
    <title>Saludablemente — Nueva contraseña</title>
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
                <h1>Nueva contraseña</h1>
            </div>
        </div>

        <p class="sal-hint">Crea una contraseña de mínimo 8 caracteres.</p>

        <#if message?has_content && (message.summary)?has_content>
            <div class="sal-alert sal-${message.type!'error'}" role="alert">
                ${kcSanitize(message.summary)?no_esc}
            </div>
        </#if>

        <form id="kc-passwd-update-form" class="sal-form" action="${url.loginAction}" method="post">
            <label class="sal-field" for="password-new">
                <span>Contraseña nueva</span>
                <input id="password-new" name="password-new" type="password" autocomplete="new-password" autofocus/>
            </label>
            <#if messagesPerField.existsError('password')>
                <span class="sal-field-error">${kcSanitize(messagesPerField.getFirstError('password',''))?no_esc}</span>
            </#if>

            <label class="sal-field" for="password-confirm">
                <span>Confirmar contraseña</span>
                <input id="password-confirm" name="password-confirm" type="password" autocomplete="new-password"/>
            </label>
            <#if messagesPerField.existsError('password-confirm')>
                <span class="sal-field-error">${kcSanitize(messagesPerField.getFirstError('password-confirm',''))?no_esc}</span>
            </#if>

            <button class="sal-btn" type="submit">Guardar contraseña</button>
        </form>
    </div>
</div>
</body>
</html>
