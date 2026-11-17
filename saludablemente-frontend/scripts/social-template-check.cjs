// Revisión visual de la entrada aislada browser-preview.ts. Solo usa su propio Chrome headless.
const fs = require('node:fs');
const path = require('node:path');
const http = require('node:http');
const { spawn } = require('node:child_process');
const workspace = path.resolve(__dirname, '..');
const root = path.join(workspace, 'dist/social-preview/browser');
const output = path.resolve(workspace, '../docs/social/evidencia-frontend');
fs.mkdirSync(output, { recursive: true });
const profile = path.join(workspace, '.angular', `social-review-${Date.now()}`);
fs.mkdirSync(profile, { recursive: true });
const chromePath =
  process.env.SOCIAL_CHROME_PATH || 'C:/Program Files/Google/Chrome/Application/chrome.exe';
const sleep = (ms) => new Promise((r) => setTimeout(r, ms));
let chrome, ws;
let seq = 0;
const pending = new Map();
const evidence = [];
const server = http.createServer((request, response) => {
  try {
    const fixture = new URL(request.url, 'http://localhost').pathname.match(
      /^\/social-gallery-fixtures\/(landscape|portrait|square)-(\d+)\.svg$/,
    );
    if (fixture) {
      const [w, h] =
        fixture[1] === 'portrait'
          ? [800, 1200]
          : fixture[1] === 'landscape'
            ? [1400, 850]
            : [1000, 1000];
      const n = Number(fixture[2]);
      const hue = (n * 47 + 160) % 360;
      response
        .writeHead(200, { 'Content-Type': 'image/svg+xml' })
        .end(
          `<svg xmlns="http://www.w3.org/2000/svg" width="${w}" height="${h}" viewBox="0 0 ${w} ${h}"><defs><linearGradient id="sky" x2="0" y2="1"><stop stop-color="hsl(${hue} 50% 75%)"/><stop offset="1" stop-color="hsl(${hue} 45% 35%)"/></linearGradient></defs><rect width="${w}" height="${h}" fill="url(#sky)"/><circle cx="${w * 0.72}" cy="${h * 0.26}" r="${w * 0.12}" fill="#fff4bc"/><path d="M0 ${h * 0.75}L${w * 0.35} ${h * 0.4}L${w * 0.7} ${h * 0.75}L${w} ${h * 0.5}V${h}H0Z" fill="hsl(${hue} 40% 28%)"/><text x="${w * 0.07}" y="${h * 0.9}" font-family="Arial" font-size="${w * 0.05}" fill="white">Imagen ${n + 1} · ${w} × ${h}</text></svg>`,
        );
      return;
    }
    let file = path.resolve(
      root,
      '.' + decodeURIComponent(new URL(request.url, 'http://localhost').pathname),
    );
    if (file !== root && !file.startsWith(root + path.sep)) {
      response.writeHead(403).end();
      return;
    }
    if (!fs.existsSync(file) || fs.statSync(file).isDirectory())
      file = path.join(root, 'index.html');
    const type =
      {
        '.html': 'text/html',
        '.js': 'text/javascript',
        '.css': 'text/css',
        '.svg': 'image/svg+xml',
        '.png': 'image/png',
        '.webp': 'image/webp',
        '.woff2': 'font/woff2',
      }[path.extname(file)] || 'application/octet-stream';
    response.writeHead(200, { 'Content-Type': type });
    fs.createReadStream(file).pipe(response);
  } catch {
    response.writeHead(500).end();
  }
});
function cdp(method, params = {}) {
  return new Promise((resolve, reject) => {
    const id = ++seq;
    pending.set(id, { resolve, reject });
    ws.send(JSON.stringify({ id, method, params }));
  });
}
async function evaluate(expression) {
  const result = await cdp('Runtime.evaluate', {
    expression,
    returnByValue: true,
    awaitPromise: true,
  });
  if (result.exceptionDetails) throw new Error(JSON.stringify(result.exceptionDetails));
  return result.result.value;
}
async function waitFor(expression) {
  for (let i = 0; i < 120; i++) {
    if (await evaluate(expression)) return;
    await sleep(100);
  }
  throw new Error(
    'No apareció: ' +
      expression +
      ' · ' +
      JSON.stringify(await evaluate('({url:location.href,body:document.body?.innerText})')),
  );
}
(async () => {
  await new Promise((resolve) => server.listen(0, '127.0.0.1', resolve));
  const port = server.address().port;
  chrome = spawn(
    chromePath,
    [
      '--headless=new',
      '--disable-gpu',
      '--no-first-run',
      '--disable-default-apps',
      '--disable-background-networking',
      '--remote-debugging-port=0',
      `--user-data-dir=${profile}`,
      'about:blank',
    ],
    { windowsHide: true, stdio: 'ignore' },
  );
  const active = path.join(profile, 'DevToolsActivePort');
  for (let i = 0; i < 150 && !fs.existsSync(active); i++) await sleep(100);
  if (!fs.existsSync(active)) throw new Error('Chrome no inició');
  const debugPort = fs.readFileSync(active, 'utf8').split('\n')[0];
  const tabs = await (await fetch(`http://127.0.0.1:${debugPort}/json/list`)).json();
  ws = new WebSocket(tabs.find((t) => t.type === 'page').webSocketDebuggerUrl);
  await new Promise((resolve) => ws.addEventListener('open', resolve, { once: true }));
  ws.addEventListener('message', (event) => {
    const message = JSON.parse(event.data);
    if (message.id && pending.has(message.id)) {
      const p = pending.get(message.id);
      pending.delete(message.id);
      message.error ? p.reject(new Error(message.error.message)) : p.resolve(message.result);
    }
  });
  await cdp('Page.enable');
  await cdp('Runtime.enable');
  const errors = [];
  ws.addEventListener('message', (event) => {
    const message = JSON.parse(event.data);
    if (message.method === 'Runtime.exceptionThrown') {
      errors.push(message.params.exceptionDetails.text);
      console.error('EXCEPTION', JSON.stringify(message.params.exceptionDetails));
    }
  });
  const post = JSON.parse(fs.readFileSync(path.join(output, 'respuestas-backend.json'), 'utf8'))
    .detalle.idPost;

  const pages = [
    ['muro', '/social', 'app-social-feed'],
    ['detalle', '/social/posts/' + post + '?comentar=1', 'tui-dialog social-comment'],
    ['equipo', '/social/equipos/301', 'social-team'],
    ['actividad', '/social', 'social-activity-meta'],
  ];
  async function capture(name, width, theme) {
    await sleep(350);
    const layout = await evaluate(
      "({width:innerWidth,scroll:document.documentElement.scrollWidth,theme:document.body.getAttribute('tuiTheme')})",
    );
    if (layout.scroll > width + 1) throw Error('Overflow ' + name + JSON.stringify(layout));
    const shot = await cdp('Page.captureScreenshot', { format: 'png' });
    const file = 'social-plantilla-' + name + '-' + theme + '-' + width + '.png';
    fs.writeFileSync(path.join(output, file), Buffer.from(shot.data, 'base64'));
    evidence.push({ case: name, width, theme, file, ...layout });
    console.log('OK ' + name + ' ' + theme + ' ' + width);
  }
  if (process.argv.includes('--shell-only')) {
    for (const theme of ['light', 'dark']) for (const width of [1920, 1440, 900, 390]) {
      await cdp('Emulation.setEmulatedMedia', {features:[{name:'prefers-color-scheme',value:theme}]});
      await cdp('Emulation.setDeviceMetricsOverride', {width,height:1000,deviceScaleFactor:1,mobile:width<500});
      await cdp('Page.navigate',{url:'http://127.0.0.1:'+port+'/social?tab=PARA_TI&page=0'});
      await waitFor("!!document.querySelector('.feed-composer') && !!document.querySelector('social-post-card')");
      const layout=await evaluate(`(()=>{
        const hero=document.querySelector('.social-hero').getBoundingClientRect();
        const nav=document.querySelector('.social-nav'),n=nav.getBoundingClientRect();
        const feed=document.querySelector('app-social-feed .columns').getBoundingClientRect();
        return {headerHeight:hero.height,navHeight:n.height,titleSize:parseFloat(getComputedStyle(document.querySelector('.social-hero h1')).fontSize),
          navFont:parseFloat(getComputedStyle(nav.querySelector('a')).fontSize),sameWidth:Math.abs(n.width-feed.width)<2,
          sameLeft:Math.abs(n.left-feed.left)<2,contentTop:feed.top,links:nav.querySelectorAll('a').length};
      })()`);
      if(layout.headerHeight>55 || layout.navHeight>60 || layout.titleSize>22 || layout.navFont>14 || !layout.sameWidth || !layout.sameLeft || layout.links!==5)
        throw Error('Proporción de cabecera: '+JSON.stringify(layout));
      evidence.push({case:'cabecera-proporcionada',width,theme,...layout});
      await capture('cabecera-compacta',width,theme);
    }
    if(errors.length) throw Error(errors.join('; '));
    fs.writeFileSync(path.join(output,'revision-cabecera-social.json'),JSON.stringify({environment:'Chrome aislado, datos simulados, sin backend',cases:evidence,errors},null,2));
    return;
  }
  for (const theme of ['light', 'dark'])
    for (const width of [1440, 900, 390]) {
      await cdp('Emulation.setEmulatedMedia', {
        features: [{ name: 'prefers-color-scheme', value: theme }],
      });
      await cdp('Emulation.setDeviceMetricsOverride', {
        width,
        height: 1000,
        deviceScaleFactor: 1,
        mobile: width < 500,
      });
      await cdp('Page.navigate', { url: 'http://127.0.0.1:' + port + '/social/usuarios/102' });
      await waitFor(
        "document.querySelectorAll('.friend-tile').length===9 && !!document.querySelector('social-post-card')",
      );
      await sleep(200);
      const profile = await evaluate(`(()=>{
      const side=document.querySelector('.profile-sidebar').getBoundingClientRect();
      const content=document.querySelector('.profile-content').getBoundingClientRect();
      const square=document.querySelector('.friend-tile app-avatar img').getBoundingClientRect();
      return {twoColumns:content.left>=side.right,cover:!!document.querySelector('.profile-cover img')?.naturalWidth,
        square:Math.abs(square.width-square.height)<1 && square.width===72,
        friendLinks:document.querySelectorAll('.friend-tile a').length,
        edit:[...document.querySelectorAll('button')].some(b=>b.textContent.trim()==='Editar perfil')};
    })()`);
      if (
        profile.twoColumns !== width >= 760 ||
        !profile.cover ||
        !profile.square ||
        profile.friendLinks !== 9 ||
        profile.edit
      )
        throw Error('Perfil: ' + JSON.stringify(profile));
      evidence.push({ case: 'perfil-publico-layout', width, theme, ...profile });
      await capture('perfil-publico', width, theme);
      if (await evaluate("!!document.querySelector('.cover-edit')"))
        throw Error('Edición de portada ajena');
      await evaluate("document.querySelectorAll('.friend-tile a')[1].click()");
      await waitFor(
        "location.pathname==='/social/usuarios/103' && document.querySelector('.profile-name h1')?.textContent.includes('103')",
      );
      if (
        await evaluate(
          "!!document.querySelector('.cover-edit') || !!document.querySelector('.profile-identity button')",
        )
      )
        throw Error('Perfil compañero editable');
      evidence.push({ case: 'compañero-abre-perfil-lectura', width, theme });
      await evaluate(
        "window.__socialBeforeGrid={header:document.querySelector('.profile-header'),info:document.querySelector('.profile-info'),friends:document.querySelector('.friend-mosaic'),url:location.href}",
      );
      await evaluate('document.querySelector(\'[aria-label="Vista de cuadrícula"]\').click()');
      await waitFor("document.querySelectorAll('.profile-post-preview').length===3");
      const grid = await evaluate(`(()=>{
        const before=window.__socialBeforeGrid;
        const cards=[...document.querySelectorAll('.profile-post-preview')];
        const rects=cards.map(c=>c.getBoundingClientRect());
        return {sameHeader:before.header===document.querySelector('.profile-header'),
          sameInfo:before.info===document.querySelector('.profile-info'),
          sameFriends:before.friends===document.querySelector('.friend-mosaic'),
          sameUrl:before.url===location.href,
          columns:getComputedStyle(document.querySelector('.profile-post-grid')).gridTemplateColumns.split(' ').length,
          aligned:Math.abs(rects[0].height-rects[1].height)<2,
          stats:cards.every(c=>c.querySelectorAll('.preview-stats span').length===3),
          authors:cards.every(c=>!!c.querySelector('.preview-author strong')?.textContent.trim())};
      })()`);
      if (
        !grid.sameHeader ||
        !grid.sameInfo ||
        !grid.sameFriends ||
        !grid.sameUrl ||
        !grid.aligned ||
        !grid.stats ||
        !grid.authors ||
        grid.columns !== 2
      )
        throw Error('Cuadrícula local: ' + JSON.stringify(grid));
      evidence.push({ case: 'cuadricula-cambio-local-y-tarjetas', width, theme, ...grid });
      await evaluate('document.querySelector(\'[aria-label="Vista de lista"]\').click()');
      await waitFor(
        "!!document.querySelector('.profile-content social-post-card') && !document.querySelector('.profile-post-grid')",
      );
      if (
        !(await evaluate(
          "window.__socialBeforeGrid.header===document.querySelector('.profile-header') && window.__socialBeforeGrid.url===location.href",
        ))
      )
        throw Error('Lista recarga perfil');
      await evaluate('document.querySelector(\'[aria-label="Vista de cuadrícula"]\').click()');
      await waitFor("document.querySelectorAll('.profile-post-preview').length===3");
      await capture('perfil-cuadricula', width, theme);
      await evaluate("document.querySelector('.profile-layout').scrollIntoView({block:'start'})");
      await capture('perfil-contenido', width, theme);
      await evaluate("document.querySelector('.profile-post-preview').click()");
      await waitFor("!!document.querySelector('tui-dialog .conversation-composer')");
      evidence.push({ case: 'cuadricula-abre-comentarios', width, theme });
      await cdp('Page.navigate', { url: 'http://127.0.0.1:' + port + '/social/mi-contenido' });
      await waitFor("!!document.querySelector('.profile-identity button')");
      const coverButton = await evaluate(
        "({background:getComputedStyle(document.querySelector('.cover-edit')).backgroundColor,color:getComputedStyle(document.querySelector('.cover-edit')).color})",
      );
      if (coverButton.background !== 'rgb(255, 255, 255)')
        throw Error('Fondo portada: ' + JSON.stringify(coverButton));
      evidence.push({ case: 'editar-portada-fondo-blanco', width, theme, ...coverButton });
      await evaluate("document.querySelector('.profile-identity button').click()");
      await waitFor("!!document.querySelector('social-profile-editor textarea')");
      await capture('editar-perfil', width, theme);
      await evaluate(
        `(()=>{const area=document.querySelector('social-profile-editor textarea');area.value='Presentación actualizada de prueba';area.dispatchEvent(new Event('input',{bubbles:true}));document.querySelector('social-profile-editor form').requestSubmit();})()`,
      );
      await waitFor(
        "!document.querySelector('social-profile-editor') && document.querySelector('.profile-bio').textContent.includes('actualizada')",
      );
      evidence.push({ case: 'edicion-propia-presentacion', width, theme });
      await evaluate("document.querySelector('.cover-edit').click()");
      await waitFor("!!document.querySelector('social-profile-editor .profile-edit-cover')");
      if (await evaluate("!!document.querySelector('social-profile-editor textarea')"))
        throw Error('Portada mezcla presentación');
      await capture('editar-portada', width, theme);
      await evaluate(
        "[...document.querySelectorAll('social-profile-editor button')].find(b=>b.textContent.trim()==='Cerrar').click()",
      );
      await waitFor("!document.querySelector('social-profile-editor')");
      evidence.push({ case: 'portada-propietario', width, theme });
      await cdp('Page.navigate', {
        url: 'http://127.0.0.1:' + port + '/social/usuarios/102?empty=1&vista=cuadricula',
      });
      await waitFor(
        "document.querySelector('.profile-content')?.textContent.includes('Sin publicaciones.')",
      );
      await capture('perfil-vacio', width, theme);
      await cdp('Page.navigate', { url: 'http://127.0.0.1:' + port + '/social' });
      await waitFor("!!document.querySelector('social-post-card .reaction-count')");
      const order = await evaluate(
        `(()=>{const card=document.querySelector('.activity-logo').closest('social-post-card');return card.querySelector('social-activity-meta').compareDocumentPosition(card.querySelector('.post-gallery'))&Node.DOCUMENT_POSITION_FOLLOWING;})()`,
      );
      if (!order) throw Error('Detalles después de imágenes');
      await evaluate("document.querySelector('social-post-card .reaction-count').click()");
      await waitFor("document.querySelectorAll('.reaction-person').length===3");
      const people =
        await evaluate(`(()=>{const row=document.querySelector('.reaction-person');return {tabs:document.querySelectorAll('.reaction-tabs button').length,
      avatar:!!row.querySelector('app-avatar img')?.naturalWidth,
      timeBelow:row.querySelector('time').getBoundingClientRect().top>=row.querySelector('a').getBoundingClientRect().bottom,
      noTable:!document.querySelector('.reaction-dialog table'),noSelect:!document.querySelector('.reaction-dialog select')};})()`);
      if (
        people.tabs !== 3 ||
        !people.avatar ||
        !people.timeBelow ||
        !people.noTable ||
        !people.noSelect
      )
        throw Error('Personas: ' + JSON.stringify(people));
      evidence.push({ case: 'reacciones-personas', width, theme, ...people });
      await evaluate('document.querySelector(\'.reaction-tabs [title="Me encanta"]\').click()');
      await waitFor("document.querySelectorAll('.reaction-person').length===1");
      await capture('personas-reacciones', width, theme);
      await evaluate("document.querySelector('.reaction-person a').click()");
      await waitFor(
        "location.pathname.endsWith('/usuarios/104') && !!document.querySelector('.profile-header') && !document.querySelector('.reaction-dialog')",
      );
      evidence.push({ case: 'reaccion-nombre-abre-perfil', width, theme });
      await cdp('Page.navigate', {
        url: 'http://127.0.0.1:' + port + '/social/posts/' + post + '?comentar=1',
      });
      await waitFor("!!document.querySelector('.comments-scroll social-comment .reaction-count')");
      await evaluate(
        "document.querySelector('.comments-scroll social-comment .reaction-count').click()",
      );
      await waitFor("!!document.querySelector('.reaction-dialog')");
      if (
        !(await evaluate(
          "document.querySelectorAll('.reaction-tabs button').length===1 && document.querySelector('.reaction-tabs').textContent.includes('Todos (0)')",
        ))
      )
        throw Error('Tabs vacías');
      await capture('reacciones-vacias', width, theme);
      await cdp('Page.navigate', {
        url: 'http://127.0.0.1:' + port + '/social/posts/' + post + '?comentar=1',
      });
      await waitFor("!!document.querySelector('.conversation-composer .editor-toolbar')");
      await evaluate(
        "[...document.querySelectorAll('.conversation-composer button')].find(b=>b.textContent.trim()==='Mencionar').click()",
      );
      await waitFor("!!document.querySelector('.mention-search input')");
      await evaluate(
        "(()=>{const i=document.querySelector('.mention-search input');i.value='Ana';i.dispatchEvent(new Event('input',{bubbles:true}));})()",
      );
      await waitFor("!!document.querySelector('.mention-result app-avatar img')?.naturalWidth");
      await capture('menciones-lista', width, theme);
      await evaluate('document.querySelector(\'[aria-label="Ver en tarjetas"]\').click()');
      await waitFor("!!document.querySelector('.mention-results.cards')");
      await capture('menciones-tarjetas', width, theme);
      await evaluate(
        "[...document.querySelectorAll('.mention-types button')].find(b=>b.textContent.trim()==='Actividades').click()",
      );
      await evaluate(
        "(()=>{const i=document.querySelector('.mention-search input');i.value='Caminata';i.dispatchEvent(new Event('input',{bubbles:true}));})()",
      );
      await waitFor(
        "document.querySelector('.mention-result')?.textContent.includes('Caminata saludable') && !!document.querySelector('.mention-result img')?.naturalWidth",
      );
      await capture('menciones-actividades', width, theme);
      await evaluate("document.querySelector('.mention-result').click()");
      await waitFor(
        "!document.querySelector('.mention-picker') && document.querySelector('.conversation-composer textarea')?.value.includes('@Caminata saludable')",
      );
      if (
        await evaluate(
          "document.querySelector('.conversation-composer textarea').value.includes('@actividad{')",
        )
      )
        throw Error('Token técnico visible');
      evidence.push({ case: 'mencion-nombre-logo-y-retorno-editor', width, theme });
    }
  for (const theme of process.argv.includes('--profile-only') ? [] : ['light', 'dark'])
    for (const width of [1920, 1440, 390]) {
      await cdp('Emulation.setEmulatedMedia', {
        features: [{ name: 'prefers-color-scheme', value: theme }],
      });
      await cdp('Emulation.setDeviceMetricsOverride', {
        width,
        height: 1000,
        deviceScaleFactor: 1,
        mobile: width < 500,
      });
      for (const [name, url, selector] of pages) {
        await cdp('Page.navigate', { url: 'http://127.0.0.1:' + port + url });
        await waitFor('!!document.querySelector(' + JSON.stringify(selector) + ')');
        await sleep(250);
        if (name === 'actividad')
          await evaluate(
            "document.querySelector('social-activity-meta').closest('social-post-card').scrollIntoView({block:'center'})",
          );
        if (name === 'detalle') {
          const composition = await evaluate(`(()=>{
            const context=document.querySelector('tui-dialog .discussion-post');
            const conversation=document.querySelector('tui-dialog .conversation');
            const left=context.getBoundingClientRect(),right=conversation.getBoundingClientRect();
            return {contextVisible:left.width>0,sideBySide:left.right<=right.left+1,
              singleEditor:document.querySelectorAll('tui-dialog .conversation-composer social-comment-editor').length===1,
              bubbles:document.querySelectorAll('tui-dialog .comment-bubble').length};
          })()`);
          if (
            !composition.singleEditor ||
            !composition.bubbles ||
            composition.contextVisible !== width > 900 ||
            (width > 900 && !composition.sideBySide)
          )
            throw Error('Composición MACAP: ' + JSON.stringify(composition));
          evidence.push({ case: 'macap-conversacion', width, theme, ...composition });
          const geometry = await evaluate(`(()=>{
            const d=document.querySelector('tui-dialog');
            const list=d.querySelector('.comments-scroll');
            const footer=d.querySelector('.conversation-composer');
            const before=footer.getBoundingClientRect();
            const hasPhoto=!!d.querySelector('.composer-avatar img')?.naturalWidth;
            const commentPhoto=!!d.querySelector('social-comment header app-avatar img')?.naturalWidth;
            list.scrollTop=list.scrollHeight;
            const after=footer.getBoundingClientRect();
            return {dialogLeft:d.getBoundingClientRect().left, dialogRight:d.getBoundingClientRect().right, dialogWidth:d.getBoundingClientRect().width, top:d.getBoundingClientRect().top,
              bottom:d.getBoundingClientRect().bottom, stable:Math.abs(before.top-after.top)<1,
              listScroll:list.scrollTop, composerBottom:after.bottom, hasPhoto, commentPhoto,
              composerOutsideList:!list.contains(footer), textBox:!!footer.querySelector('.text-box [type="submit"]'),
              toolbar:footer.querySelector('.editor-toolbar').textContent};
          })()`);
          if (
            !geometry.stable ||
            !geometry.composerOutsideList ||
            !geometry.textBox ||
            geometry.listScroll < 1 ||
            !geometry.hasPhoto ||
            !geometry.commentPhoto ||
            geometry.dialogLeft < 0 ||
            geometry.dialogRight > width + 1 ||
            geometry.top < 0 ||
            geometry.bottom > 1001 ||
            (width >= 1440 && geometry.dialogWidth < 900)
          )
            throw Error('Modal adaptable: ' + JSON.stringify(geometry));
          if (!geometry.toolbar.includes('Añadir archivo')) throw Error('Archivo fuera de toolbar');
          await evaluate("document.querySelector('.comments-scroll').scrollTop=0");
          evidence.push({ case: 'editor-fijo-y-avatares', width, theme, ...geometry });
          console.log('OK editor fijo y fotos ' + width + ' ' + theme);
        }
        if (name === 'muro') {
          const entry = await evaluate(`(()=>{
            const feed=document.querySelector('app-social-feed');
            const composer=feed.querySelector('.composer-prompt');
            return {compactHeader:document.querySelector('.social-hero').getBoundingClientRect().height<120,
              avatar:!!feed.querySelector('.feed-composer app-avatar img')?.naturalWidth,
              prompt:!!composer,postWidth:feed.querySelector('social-post-card').getBoundingClientRect().width};
          })()`);
          if (!entry.compactHeader || !entry.avatar || !entry.prompt || entry.postWidth > 681)
            throw Error('Entrada MACAP: ' + JSON.stringify(entry));
          evidence.push({ case: 'macap-muro', width, theme, ...entry });
        }
        if (name === 'actividad') {
          const mosaic = await evaluate(`(()=>{
            const card=document.querySelector('.activity-logo').closest('social-post-card');
            const logo=card.querySelector('.activity-logo').getBoundingClientRect();
            const title=card.querySelector('.post-title').getBoundingClientRect();
            const badge=card.querySelector('.post-labels').getBoundingClientRect();
            const grid=card.querySelector('.photo-grid');const frame=grid.getBoundingClientRect();
            const tiles=[...grid.querySelectorAll('.photo-tile')].map(t=>t.getBoundingClientRect());
            return {logoWidth:logo.width,logoHeight:logo.height,titleBeforeBadge:title.bottom<=badge.top+1,
              count:tiles.length,extra:grid.querySelector('.photo-extra')?.textContent.trim(),orientation:grid.dataset.orientation,
              filled:tiles.reduce((a,t)=>a+t.width*t.height,0)/(frame.width*frame.height),ratio:frame.width/frame.height};
          })()`);
          if (
            mosaic.logoWidth > 48 ||
            mosaic.logoHeight > 48 ||
            !mosaic.titleBeforeBadge ||
            mosaic.count !== 5 ||
            mosaic.extra !== '+10' ||
            mosaic.filled < 0.96 ||
            Math.abs(mosaic.ratio - 4 / 3) > 0.01
          )
            throw Error('Mosaico: ' + JSON.stringify(mosaic));
          evidence.push({ case: 'mosaico-logo-titulo', width, theme, ...mosaic });
        }
        await capture(name, width, theme);
        if (name === 'actividad') {
          const point = await evaluate(
            `(()=>{const b=document.querySelector('.activity-logo').closest('social-post-card').querySelectorAll('.photo-tile')[4];b.scrollIntoView({block:'center'});b.focus();const r=b.getBoundingClientRect();return {x:r.left+r.width/2,y:r.top+r.height/2};})()`,
          );
          await cdp('Input.dispatchMouseEvent', {
            type: 'mousePressed',
            button: 'left',
            clickCount: 1,
            ...point,
          });
          await cdp('Input.dispatchMouseEvent', {
            type: 'mouseReleased',
            button: 'left',
            clickCount: 1,
            ...point,
          });
          await waitFor("!!document.querySelector('.photo-viewer')");
          await sleep(350);
          const viewer = await evaluate(`(()=>{
            const v=document.querySelector('.photo-viewer');const d=v.closest('tui-dialog').getBoundingClientRect();
            return {thumbnails:v.querySelectorAll('.photo-thumbs button').length,status:v.querySelector('[role=status]').textContent.trim(),left:d.left,right:d.right,top:d.top,bottom:d.bottom};
          })()`);
          if (
            viewer.thumbnails !== 15 ||
            !viewer.status.includes('5 de 15') ||
            viewer.left < 0 ||
            viewer.right > width + 1 ||
            viewer.top < 0 ||
            viewer.bottom > 1001
          )
            throw Error('Visor: ' + JSON.stringify(viewer));
          await evaluate('document.querySelector(\'[aria-label="Imagen siguiente"]\').click()');
          await waitFor(
            "document.querySelector('.photo-viewer [role=status]').textContent.includes('6 de 15')",
          );
          await evaluate('document.querySelector(\'[aria-label="Ir a imagen 15"]\').click()');
          await waitFor(
            "document.querySelector('.photo-viewer [role=status]').textContent.includes('15 de 15')",
          );
          await cdp('Input.dispatchKeyEvent', {
            type: 'keyDown',
            key: 'ArrowRight',
            code: 'ArrowRight',
          });
          await cdp('Input.dispatchKeyEvent', {
            type: 'keyUp',
            key: 'ArrowRight',
            code: 'ArrowRight',
          });
          await waitFor(
            "document.querySelector('.photo-viewer [role=status]').textContent.includes('1 de 15')",
          );
          await evaluate('document.querySelector(\'[aria-label="Acercar imagen"]\').click()');
          await waitFor(
            "document.querySelector('[aria-label=\"Ajustar imagen\"]').textContent.includes('150')",
          );
          await evaluate('document.querySelector(\'[aria-label="Ajustar imagen"]\').click()');
          await capture('visor', width, theme);
          evidence.push({ case: 'visor-navegacion-zoom', width, theme, ...viewer });
          await cdp('Input.dispatchKeyEvent', { type: 'keyDown', key: 'Escape', code: 'Escape' });
          await cdp('Input.dispatchKeyEvent', { type: 'keyUp', key: 'Escape', code: 'Escape' });
          await waitFor("!document.querySelector('.photo-viewer')");
        }
      }
      for (const [name, scope, label] of [
        ['menu', 'social-post-card', 'Opciones'],
        ['reacciones', 'social-reactions', 'Elegir reacción'],
      ]) {
        await cdp('Page.navigate', { url: 'http://127.0.0.1:' + port + '/social' });
        await waitFor("!!document.querySelector('social-post-card')");
        await sleep(200);
        await evaluate(
          '(()=>{const b=[...document.querySelector(' +
            JSON.stringify(scope) +
            ').querySelectorAll("button")].find(b=>b.textContent.trim().includes(' +
            JSON.stringify(label) +
            '));b.focus();b.click();})()',
        );
        await waitFor("!!document.querySelector('tui-dropdown')");
        await sleep(250);
        const bounds = await evaluate(
          "(()=>{const r=document.querySelector('tui-dropdown').getBoundingClientRect();return {left:r.left,right:r.right,dialogs:document.querySelectorAll('tui-dialog').length}})()",
        );
        if (bounds.left < 0 || bounds.right > width + 1 || bounds.dialogs)
          throw Error('Popover fuera de contexto: ' + JSON.stringify(bounds));
        await capture(name, width, theme);
        if (name === 'reacciones') {
          const picker = await evaluate(
            "(()=>{const p=document.querySelector('.reaction-picker');return {buttons:p.querySelectorAll('button').length,height:p.getBoundingClientRect().height}})()",
          );
          if (picker.buttons !== 7 || picker.height > 140)
            throw Error('El selector no es compacto');
          await evaluate("document.querySelector('.reaction-picker button').focus()");
          await cdp('Input.dispatchKeyEvent', {
            type: 'keyDown',
            key: 'ArrowRight',
            code: 'ArrowRight',
            windowsVirtualKeyCode: 39,
          });
          await cdp('Input.dispatchKeyEvent', {
            type: 'keyUp',
            key: 'ArrowRight',
            code: 'ArrowRight',
            windowsVirtualKeyCode: 39,
          });
          if (
            (await evaluate("document.activeElement.getAttribute('aria-label')")) !== 'Me encanta'
          )
            throw Error('Navegación por flechas incorrecta');

          await evaluate(
            'document.querySelector(\'tui-dropdown button[aria-label="Me gusta"]\').click()',
          );
          await waitFor(
            "!!document.querySelector('social-post-card .reaction-control[data-reaction=LIKE]')",
          );
          await waitFor("!document.querySelector('tui-dropdown')");
          await sleep(350);
          await capture('like-activo', width, theme);
          await evaluate("document.querySelector('social-post-card .like-button').click()");
          await waitFor(
            "!document.querySelector('social-post-card .reaction-control').hasAttribute('data-reaction')",
          );
        } else {
          await cdp('Input.dispatchKeyEvent', {
            type: 'keyDown',
            key: 'Escape',
            code: 'Escape',
            windowsVirtualKeyCode: 27,
          });
          await cdp('Input.dispatchKeyEvent', {
            type: 'keyUp',
            key: 'Escape',
            code: 'Escape',
            windowsVirtualKeyCode: 27,
          });
          await waitFor("!document.querySelector('tui-dropdown')");
        }
      }
    }
  // Todas las distribuciones CSS, con orientación real y sin huecos ni solapamientos.
  await cdp('Emulation.setDeviceMetricsOverride', {
    width: 1440,
    height: 1000,
    deviceScaleFactor: 1,
    mobile: false,
  });
  for (const orientation of process.argv.includes('--profile-only')
    ? []
    : ['portrait', 'landscape'])
    for (const count of [1, 2, 3, 4, 5, 6, 15]) {
      await cdp('Page.navigate', {
        url: 'http://127.0.0.1:' + port + '/social?photos=' + count + '&orientation=' + orientation,
      });
      await waitFor("!!document.querySelector('.activity-logo')");
      await evaluate(
        "document.querySelector('.activity-logo').closest('social-post-card').scrollIntoView({block:'center'})",
      );
      await waitFor(
        "document.querySelector('.activity-logo').closest('social-post-card').querySelector('.photo-tile img')?.naturalWidth > 0",
      );
      await sleep(100);
      const layout = await evaluate(`(()=>{
      const grid=document.querySelector('.activity-logo').closest('social-post-card').querySelector('.photo-grid');
      const frame=grid.getBoundingClientRect();const tiles=[...grid.querySelectorAll('.photo-tile')].map(t=>t.getBoundingClientRect());
      let overlap=false; for(let i=0;i<tiles.length;i++) for(let j=i+1;j<tiles.length;j++) if(Math.min(tiles[i].right,tiles[j].right)-Math.max(tiles[i].left,tiles[j].left)>1 && Math.min(tiles[i].bottom,tiles[j].bottom)-Math.max(tiles[i].top,tiles[j].top)>1) overlap=true;
      return {count:tiles.length,orientation:grid.dataset.orientation,filled:tiles.reduce((s,t)=>s+t.width*t.height,0)/(frame.width*frame.height),extra:grid.querySelector('.photo-extra')?.textContent.trim() || null,overlap};
    })()`);
      if (
        layout.count !== Math.min(count, 5) ||
        layout.filled < 0.96 ||
        layout.overlap ||
        layout.orientation !== orientation ||
        layout.extra !== (count > 5 ? '+' + (count - 5) : null)
      )
        throw Error('Distribución ' + count + ' ' + orientation + ': ' + JSON.stringify(layout));
      evidence.push({ case: 'distribucion', total: count, ...layout });
      console.log('OK distribución ' + count + ' ' + orientation);
    }
  if (errors.length) throw new Error('Errores de navegador: ' + errors.join('; '));
  fs.writeFileSync(
    path.join(output, 'revision-plantillas.json'),
    JSON.stringify(
      {
        environment: 'Chrome headless aislado; datos simulados; sin backend',
        cases: evidence,
        errors,
      },
      null,
      2,
    ),
  );
  console.log('Revisión de navegador completa.');
})()
  .catch((error) => {
    console.error(error);
    process.exitCode = 1;
  })
  .finally(() => {
    if (ws) ws.close();
    if (chrome) chrome.kill();
    server.close();
  });
