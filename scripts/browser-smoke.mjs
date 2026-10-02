import assert from 'node:assert/strict';
import {createRequire} from 'node:module';
import {mkdirSync,writeFileSync} from 'node:fs';
import {resolve} from 'node:path';
const require=createRequire(import.meta.url);
const {chromium}=require(process.env.PLAYWRIGHT_MODULE||'playwright');
const browser=await chromium.launch({headless:true,channel:process.env.PLAYWRIGHT_CHANNEL||'msedge'});
const origin=(process.env.DEMO_BASE_URL||'http://127.0.0.1:4321').replace(/\/+$/,'');
const evidence=[];mkdirSync('docs',{recursive:true});
const contexts=[];
const task=(page,title)=>page.locator('.task-open').filter({has:page.getByText(title,{exact:true})});
async function visible(locator){await locator.waitFor({state:'visible'});}
async function hydrated(page){await page.waitForFunction(()=>!document.querySelector('astro-island[ssr]'));}
async function demo(page){await page.goto(origin);await hydrated(page);await page.getByRole('button',{name:'Try the demo',exact:true}).click();await visible(page.getByRole('heading',{name:'Your Abu Dhabi setup'}));}
async function ask(page){await page.getByRole('button',{name:'Ask Life Setup',exact:true}).click();await page.getByRole('button',{name:'Can I exchange my driving licence now?',exact:true}).click();await page.getByRole('button',{name:'Ask',exact:true}).click();await visible(page.locator('.answer'));return page.locator('.answer').innerText();}
async function close(page){await page.getByRole('button',{name:'Close panel',exact:true}).click();}
async function screenshot(page,name){await page.evaluate(()=>window.scrollTo(0,0));await page.screenshot({path:resolve('docs',name),fullPage:true});}
async function context(options={}){const c=await browser.newContext(options);contexts.push(c);const page=await c.newPage();const errors=[];page.on('pageerror',error=>errors.push(error.message));return {c,page,errors};}
function record(name,details={}){evidence.push({name,result:'passed',...details});console.log('PASS: '+name);}
try{
 for(const mobile of [false,true]){
  const {c,page,errors}=await context({viewport:mobile?{width:390,height:844}:{width:1440,height:1000},reducedMotion:mobile?'reduce':'no-preference'});
  await demo(page);
  if(mobile){
   const boxes=await page.locator('.next-step,.task-sections,.plan-aside').evaluateAll(nodes=>nodes.map(node=>({class:node.className,top:node.getBoundingClientRect().top,display:getComputedStyle(node).display})));
   assert(boxes[0].top<boxes[1].top);assert.notEqual(boxes[2].display,'none');
   assert.equal(await page.evaluate(()=>matchMedia('(prefers-reduced-motion: reduce)').matches),true);
   assert.equal(await page.locator('.task-card').first().evaluate(node=>getComputedStyle(node).transitionDuration),'0s');
   await screenshot(page,'windows-plan-mobile.png');
  }
  await task(page,'Check your driving licence options').click();await visible(page.getByText('You can’t do this yet.',{exact:true}));
  await page.keyboard.press('Escape');
  assert.equal(await task(page,'Check your driving licence options').evaluate(node=>node===document.activeElement),true);
  assert((await ask(page)).startsWith('Not yet.'));await close(page);
  await task(page,'Get your Emirates ID').click();await page.getByRole('button',{name:'Emirates ID received',exact:true}).click();
  await visible(page.getByText(/Good news\. Two steps are now ready\./));
  assert.deepEqual((await page.locator('.newly-ready .task-title').allTextContents()).sort(),['Check your driving licence options','Open a bank account'].sort());
  assert.equal(await page.locator('.next-step .text-link').evaluate(node=>node===document.activeElement),true);
  if(!mobile)await screenshot(page,'windows-plan-desktop.png');
  const answer=await ask(page);assert(answer.startsWith('Yes.'));assert(answer.includes('confirm whether you qualify'));
  const official=page.locator('.answer a');assert((await official.getAttribute('href')).startsWith('https://'));await close(page);
  await page.getByRole('button',{name:'Reset demo profile',exact:true}).click();await visible(page.getByText('Demo reset. Meet your next steps.',{exact:true}));
  await page.getByRole('button',{name:'Ask Life Setup',exact:true}).click();assert.equal(await page.locator('.answer').count(),0);await close(page);
  await task(page,'Check your driving licence options').click();await visible(page.getByText('You can’t do this yet.',{exact:true}));await close(page);
  assert.equal(await page.evaluate(()=>document.documentElement.scrollWidth<=innerWidth),true);
  assert.deepEqual(errors,[]);record(mobile?'390px demo, reset, reduced motion and no overflow':'Desktop demo, exact unlock, assistant and keyboard focus');await c.close();
 }
 {
  const {c,page,errors}=await context();await page.goto(origin+'/onboarding');await hydrated(page);
  await page.getByRole('textbox',{name:'Nationality',exact:true}).fill('Spanish');await page.getByRole('textbox',{name:'Country you’re moving from',exact:true}).fill('Spain');
  for(let step=0;step<6;step++){await page.getByRole('button',{name:'Continue',exact:true}).click();await page.waitForFunction(()=>document.activeElement?.classList.contains('question'));}
  await page.getByRole('button',{name:'No',exact:true}).click();await page.getByRole('button',{name:'See my plan',exact:true}).click();await visible(page.getByRole('heading',{name:'Your Abu Dhabi setup'}));
  for(const title of ['Check your driving licence options','Bring your family','Find a school','Bring your pet'])assert.equal(await task(page,title).count(),0);
  assert.equal(await page.locator('.task-card').count(),7);
  await page.getByRole('link',{name:'Edit your answers'}).click();await hydrated(page);await visible(page.getByRole('textbox',{name:'Nationality',exact:true}));assert.equal(await page.getByRole('textbox',{name:'Nationality',exact:true}).inputValue(),'Spanish');
  await page.getByRole('button',{name:'Continue',exact:true}).click();await page.getByRole('button',{name:'With family',exact:true}).click();await page.getByRole('button',{name:'Yes',exact:true}).click();
  for(let step=1;step<5;step++)await page.getByRole('button',{name:'Continue',exact:true}).click();
  await page.getByRole('button',{name:'Yes',exact:true}).click();await page.getByRole('textbox',{name:/Which country issued/}).fill('Spain');await page.getByRole('button',{name:'Continue',exact:true}).click();await page.getByRole('button',{name:'Yes',exact:true}).click();await page.getByRole('button',{name:'See my plan',exact:true}).click();await visible(page.getByRole('heading',{name:'Your Abu Dhabi setup'}));assert.equal(await page.locator('.task-card').count(),11);
  const housingTitle=(await page.locator('.task-title').allTextContents()).find(title=>/home/i.test(title));assert(housingTitle);
  await task(page,housingTitle).click();await page.getByRole('button',{name:'Save for later',exact:true}).click();await visible(page.getByRole('button',{name:'Move back to my plan',exact:true}));await page.getByRole('button',{name:'Move back to my plan',exact:true}).click();await visible(page.getByRole('button',{name:'Mark this step done',exact:true}));
  await page.getByRole('button',{name:'Mark this step done',exact:true}).click();await page.locator('.done-section summary').click();await task(page,housingTitle).click();await page.getByRole('button',{name:'Undo completion',exact:true}).click();await visible(page.getByRole('button',{name:'Mark this step done',exact:true}));await close(page);
  assert.deepEqual(errors,[]);record('Conditional onboarding, saved profile edits, defer/resume and undo');await c.close();
 }
 {
  const {c,page,errors}=await context();await demo(page);
  await page.route('**/api/**',route=>route.abort('failed'));
  await task(page,'Get your Emirates ID').click();await page.getByRole('button',{name:'Emirates ID received',exact:true}).click();await visible(page.getByText(/We can’t reach your plan right now/));
  assert.equal(await page.locator('.task-card').count(),11);await page.unroute('**/api/**');await page.getByRole('button',{name:'Try again',exact:true}).click();await visible(page.getByText(/Good news\. Two steps are now ready\./));
  assert((await ask(page)).startsWith('Yes.'));const previous=await page.locator('.answer').innerText();await page.route('**/api/**',route=>route.abort('failed'));await page.getByRole('button',{name:'Ask',exact:true}).click();await visible(page.getByText(/We can’t reach your plan right now/));assert.equal(await page.locator('.answer').innerText(),previous);await page.unroute('**/api/**');await close(page);
  await page.getByRole('link',{name:'Edit your answers'}).click();await hydrated(page);await visible(page.getByRole('textbox',{name:'Nationality',exact:true}));
  await page.route('**/api/**',route=>route.abort('failed'));
  for(let step=0;step<6;step++)await page.getByRole('button',{name:'Continue',exact:true}).click();await page.getByRole('button',{name:'See my plan',exact:true}).click();await visible(page.getByText(/We can’t reach your plan right now/));assert.equal(await page.getByRole('heading',{name:'Are you bringing a pet?'}).count(),1);await page.unroute('**/api/**');await page.getByRole('button',{name:'Try again',exact:true}).click();await visible(page.getByRole('heading',{name:'Your Abu Dhabi setup'}));
  await page.route('**/api/**',route=>route.abort('failed'));await page.reload();await visible(page.getByText(/We can’t reach your plan right now/));await page.unroute('**/api/**');await page.getByRole('button',{name:'Try again',exact:true}).click();await visible(page.getByRole('heading',{name:'Your Abu Dhabi setup'}));
  assert.deepEqual(errors,[]);record('Unavailable backend: action, chat, edit submission and load retries preserve work');await c.close();
 }
 {
  const {c,page,errors}=await context();await demo(page);
  for(const phase of ['load','edit-load','action','chat','edit-submit']){
   await demo(page);
   if(phase==='load'||phase==='edit-load'){await page.evaluate(()=>localStorage.setItem('life-setup-profile','missing-session'));await page.goto(origin+(phase==='load'?'/plan':'/onboarding?edit'));}
   else{
    if(phase==='edit-submit'){await page.getByRole('link',{name:'Edit your answers'}).click();await hydrated(page);await visible(page.getByRole('textbox',{name:'Nationality',exact:true}));for(let step=0;step<6;step++)await page.getByRole('button',{name:'Continue',exact:true}).click();}
    await page.route('**/api/**',route=>route.fulfill({status:404,contentType:'application/json',body:JSON.stringify({message:'Session expired.'})}));
    if(phase==='action'){await task(page,'Get your Emirates ID').click();await page.getByRole('button',{name:'Emirates ID received',exact:true}).click();}
    if(phase==='chat'){await page.getByRole('button',{name:'Ask Life Setup',exact:true}).click();await page.getByRole('button',{name:'Can I exchange my driving licence now?',exact:true}).click();await page.getByRole('button',{name:'Ask',exact:true}).click();}
    if(phase==='edit-submit')await page.getByRole('button',{name:'See my plan',exact:true}).click();
   }
   await visible(page.getByRole('heading',{name:'Let’s make a new plan.',exact:true}));assert.equal(await page.evaluate(()=>localStorage.getItem('life-setup-profile')),null);await visible(page.getByRole('link',{name:'Get started',exact:true}));await page.unroute('**/api/**');
  }
  assert.deepEqual(errors,[]);record('Expired sessions recover during load, editing, task actions and chat');await c.close();
 }
 writeFileSync('docs/windows-browser-validation.json',JSON.stringify({date:new Date().toISOString(),browser:'Microsoft Edge',checks:evidence},null,2));
}catch(error){for(const c of contexts){for(const page of c.pages()){if(!page.isClosed()){await page.screenshot({path:resolve('docs','windows-test-failure.png'),fullPage:true}).catch(()=>{});console.error((await page.locator('main').innerText().catch(()=>'')));}}}throw error;}
finally{await Promise.all(contexts.map(c=>c.close().catch(()=>{})));await browser.close();}
