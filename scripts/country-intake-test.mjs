import assert from 'node:assert/strict';
import {createRequire} from 'node:module';
import {writeFileSync} from 'node:fs';
const require=createRequire(import.meta.url);
const {chromium}=require(process.env.PLAYWRIGHT_MODULE||'playwright');
const origin=(process.env.DEMO_BASE_URL||'http://127.0.0.1:4321').replace(/\/+$/,'');
const apiOrigin=process.env.TEST_API_URL||'http://127.0.0.1:9000';
const browser=await chromium.launch({headless:true,channel:'msedge'});
const evidence=[];
const countries=(await (await fetch(apiOrigin+'/api/countries')).json()).countries;
const hydrated=page=>page.waitForFunction(()=>!document.querySelector('astro-island[ssr]'));
const control=(page,label)=>page.getByRole('combobox',{name:label,exact:true});
const root=(page,label)=>control(page,label).locator('..');
async function demo(page){await page.goto(origin+'/describe?demo');await hydrated(page);await page.getByRole('heading',{name:'Here’s what I understood.',exact:true}).waitFor();await page.waitForFunction(()=>document.querySelector('input')?.value==='Spain');}
try {
 const context=await browser.newContext({viewport:{width:390,height:844},reducedMotion:'reduce'});const page=await context.newPage();const errors=[];page.on('pageerror',e=>errors.push(e.message));
 await demo(page);
 for(const label of ['Nationality','Country you’re moving from','Which country issued your driving licence?'])assert.equal(await control(page,label).inputValue(),'Spain');
 const national=control(page,'Nationality');await national.fill('Atlantis');await page.getByText('No countries match. Try another name or choose Unknown.').waitFor();await national.press('Escape');assert.equal(await national.inputValue(),'Spain');
 await national.fill('cote');await page.getByRole('option',{name:countries.find(c=>c.code==='CI').name,exact:true}).waitFor();await national.press('ArrowDown');await national.press('ArrowUp');await national.press('Enter');assert.equal(await national.inputValue(),countries.find(c=>c.code==='CI').name);
 assert.equal(await control(page,'Country you’re moving from').inputValue(),'Spain');assert.equal(await control(page,'Which country issued your driving licence?').inputValue(),'Spain');
 await root(page,'Nationality').getByRole('button',{name:'Clear',exact:true}).click();await national.press('Escape');assert.equal(await national.inputValue(),'Unknown / not sure');
 await root(page,'Which country issued your driving licence?').getByRole('button',{name:'I don’t have a foreign driving licence',exact:true}).click();await control(page,'Which country issued your driving licence?').press('Escape');
 const response=page.waitForRequest(r=>r.url().endsWith('/api/profile')&&r.method()==='POST');await page.getByRole('button',{name:'Confirm and create my plan',exact:true}).click();const posted=(await response).postDataJSON();assert.equal(posted.nationality,null);assert.equal(posted.movingFrom,'ES');assert.equal(posted.licenceCountry,'NONE');
 await page.getByRole('heading',{name:'Your Abu Dhabi setup'}).waitFor();const profileId=await page.evaluate(()=>localStorage.getItem('life-setup-profile'));const persisted=await (await fetch(apiOrigin+'/api/plan/'+profileId)).json();assert.equal(persisted.profile.nationality,null);assert.equal(persisted.profile.movingFrom,'ES');assert.equal(persisted.profile.licenceCountry,'NONE');assert.equal(await page.evaluate(()=>document.documentElement.scrollWidth<=innerWidth),true);
 evidence.push('390px combobox search, accents, keyboard, Escape, clear, NONE and independent persisted codes');
 await page.goto(origin+'/describe');await hydrated(page);const story='I moved from Spain with my wife, baby and cat. I’m already in Abu Dhabi. My residence is being processed and I want to drive.';await page.getByRole('textbox',{name:'Your move',exact:true}).fill(story);
 const extract=page.waitForResponse(r=>r.url().endsWith('/api/profile/extract'));await page.getByRole('button',{name:'Review my answers',exact:true}).click();const result=await (await extract).json();assert.equal(result.draft.movingFrom,'ES');assert.equal(result.draft.nationality,null);assert.equal(result.draft.licenceCountry,null);assert.equal(result.draft.hasHousing,null);assert.equal(result.draft.hasEmiratesId,null);
 await page.getByRole('heading',{name:'Here’s what I understood.',exact:true}).waitFor();await page.getByRole('button',{name:'Confirm and create my plan',exact:true}).click();await page.getByText(/Please confirm the unanswered/).waitFor();evidence.push('Incomplete story preserves unknowns and requires editable confirmation');
 await page.goto(origin+'/describe');await hydrated(page);await page.getByRole('textbox',{name:'Your move',exact:true}).fill(story);await page.route('**/api/profile/extract',route=>route.fulfill({status:200,contentType:'application/json',body:JSON.stringify({draft:{},mode:'OPENAI'})}));await page.getByRole('button',{name:'Review my answers',exact:true}).click();await page.getByText('We couldn’t read the server’s reply. Please try again.').waitFor();assert.equal(await page.getByRole('textbox',{name:'Your move',exact:true}).inputValue(),story);await page.unroute('**/api/profile/extract');evidence.push('Malformed extraction preserves description');
 await context.close();
 const retryContext=await browser.newContext();const retryPage=await retryContext.newPage();let failing=true;await retryPage.route('**/api/countries',route=>failing?route.fulfill({status:503,contentType:'application/json',body:'{"message":"offline"}'}):route.continue());await retryPage.goto(origin+'/describe?demo');await hydrated(retryPage);await retryPage.getByRole('heading',{name:'Here’s what I understood.',exact:true}).waitFor();await retryPage.getByRole('button',{name:'Retry country list',exact:true}).first().waitFor();failing=false;await retryPage.getByRole('button',{name:'Retry country list',exact:true}).first().click();await retryPage.waitForFunction(()=>document.querySelector('input')?.value==='Spain');await retryContext.close();evidence.push('Country-list outage preserves demo codes and retry recovers');
 assert.deepEqual(errors,[]);writeFileSync('docs/country-intake-validation.json',JSON.stringify({date:new Date().toISOString(),origin,checks:evidence},null,2));for(const check of evidence)console.log('PASS: '+check);
} finally {await browser.close();}
